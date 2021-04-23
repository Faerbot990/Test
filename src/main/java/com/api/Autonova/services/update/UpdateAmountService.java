package com.api.Autonova.services.update;


import com.api.Autonova.components.ProcessUpdateStatusManipulator;
import com.api.Autonova.components.cache.CacheUpdateComponent;
import com.api.Autonova.components.handlers.AttrPricesHandler;
import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.Product;
import com.api.Autonova.models.ProductAttribute;
import com.api.Autonova.models.ProductUpdatedAttrs;
import com.api.Autonova.models.site.SettingModel;
import com.api.Autonova.repository.ProductAttributesRepository;
import com.api.Autonova.repository.ProductsRepository;
import com.api.Autonova.repository.SettingRepository;
import com.api.Autonova.services.external_api.OneCApiService;
import com.api.Autonova.utils.Constants;
import com.api.Autonova.utils.DateGetUtil;
import com.api.Autonova.utils.ExceptionsUtil;
import com.api.Autonova.utils.ListSearchUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UpdateAmountService {

    @Autowired
    AttrPricesHandler attrPricesHandler;

    @Autowired
    OneCApiService oneCApiService;

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    ProductsRepository productsRepository;

    @Autowired
    ProductAttributesRepository productAttributesRepository;

    @Autowired
    ProcessUpdateStatusManipulator processUpdateStatusManipulator;

    @Autowired
    CacheUpdateComponent cacheUpdateComponent;

    private Logger logger = LoggerFactory.getLogger(UpdateAmountService.class);
    private ListSearchUtil listSearchUtil = null;

    private static final int DEFAULT_NORMAL_LIMIT = 1000;

    public void update(){
        if(!processUpdateStatusManipulator.checkWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_STATUS) &&
                !processUpdateStatusManipulator.checkWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_AMOUNT_STATUS)){
            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_AMOUNT_STATUS,true);

            //обязательное действие перед запросом в 1С АПИ - обновление доступов, на случай изменения
            oneCApiService.updateAccess();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader reader = mapper.readerFor(new TypeReference<List<ProductUpdatedAttrs>>() {});

            if(listSearchUtil == null){ listSearchUtil = new ListSearchUtil(); }

            logger.info("Amount update: " + new Date());

            long productCount = productsRepository.getAllProductsCount();

            int LIMIT = getLimit();
            int PAGE = (int) productCount % LIMIT == 0 ? (int) productCount/ LIMIT : ((int)productCount / LIMIT) + 1;

            for(int i = 0; i < PAGE; i++){
                try {

                    List<Product> products = productsRepository.getAllIdsAndCodes(new PageRequest(i, LIMIT));

                    List[] lists = DateGetUtil.getIdsAndCodesFromProducts(products);
                    List<String> codes = lists[1];
                    List<Integer> ids = lists[0];

                    List<String> neededAttrPattern = makeNeededAttrsList();

                    List<ProductAttribute> attributes = productAttributesRepository.getAttrsInIdsAndName(ids.size() > 0 ? ids : null, Constants.PRODUCT_ATTR_AMOUNT);

                    List<ProductUpdatedAttrs> productUpdatedAttrs = null;
                    JsonNode dataFrom1C = oneCApiService.getProductsDataByCodes(codes, neededAttrPattern);

                    try {
                        productUpdatedAttrs = reader.readValue(dataFrom1C);
                    } catch (IOException e) {
                        logger.error(Constants.ERROR_READ_DATA + dataFrom1C);
                    }

                    if(productUpdatedAttrs != null){

                        //make result
                        List<ProductAttribute> attributesToUpdate = new ArrayList<>();

                        for(ProductUpdatedAttrs productUpdatedAttrsItem : productUpdatedAttrs){

                            Integer indexProduct = null;

                            ProductAttribute amountAttr = null;
                            if(productUpdatedAttrsItem.getAmount() != null){
                                int indexOldAmount = listSearchUtil.findAttrIndexByProductCodeAndName(attributes, productUpdatedAttrsItem.getCode(), Constants.PRODUCT_ATTR_AMOUNT);
                                if(indexOldAmount >= 0){
                                    //check if value updated
                                    if(!attributes.get(indexOldAmount).getValue().equals(productUpdatedAttrsItem.getAmount())){
                                        amountAttr = attributes.get(indexOldAmount);
                                        amountAttr.setValue(productUpdatedAttrsItem.getAmount());
                                        attributesToUpdate.add(amountAttr);
                                    }
                                }else {
                                    indexProduct = listSearchUtil.findProductIndexByCode(products, productUpdatedAttrsItem.getCode());
                                    if(indexProduct >= 0){
                                        amountAttr = new ProductAttribute();
                                        amountAttr.setName(Constants.PRODUCT_ATTR_AMOUNT);
                                        amountAttr.setProduct(products.get(indexProduct));
                                        amountAttr.setProductCode(products.get(indexProduct).getCode());
                                        amountAttr.setValue(productUpdatedAttrsItem.getAmount());
                                        attributesToUpdate.add(amountAttr);
                                    }
                                }
                            }
                        }

                        //save products page
                        productAttributesRepository.save(attributesToUpdate);
                        //clear cache
                        cacheUpdateComponent.clearProductsDataCache();
                        attributesToUpdate = null;
                    }

                    products = null;
                    attributes = null;
                    dataFrom1C = null;
                    productUpdatedAttrs = null;

                }catch (Exception e){
                    logger.error(Constants.ERROR_GLOBAL);
                    logger.error(e.getClass().getName());
                    logger.error(e.getMessage());
                    logger.error(ExceptionsUtil.getStackTrace(e.getStackTrace()));
                }
            }

            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_AMOUNT_STATUS,false);
        }else {
            throw new ServerException(Constants.ERROR_UPDATE_ALREADY_RUNNING);
        }
    }

    private List<String> makeNeededAttrsList(){
        List<String> attrNames = new ArrayList<>();
        attrNames.add(Constants.PRODUCT_ATTR_AMOUNT);
        return attrNames;
    }

    private int getLimit(){
        SettingModel settingLimit = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_AMOUNT_LIMIT_PRODUCTS);
        try {
            int limitFromDB = Integer.parseInt(settingLimit.getValue());
            if(limitFromDB > 0 && limitFromDB <= DEFAULT_NORMAL_LIMIT){
                return limitFromDB;
            }else {
                return DEFAULT_NORMAL_LIMIT;
            }
        }catch (NullPointerException | NumberFormatException e){
            return DEFAULT_NORMAL_LIMIT;
        }
    }
}
