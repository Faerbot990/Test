package com.api.Autonova.services.update;


import com.api.Autonova.components.ProcessUpdateStatusManipulator;
import com.api.Autonova.components.cache.CacheUpdateComponent;
import com.api.Autonova.controllers.ContentController;
import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.Product;
import com.api.Autonova.models.ProductAttribute;
import com.api.Autonova.models.ProductUpdatedAttrs;
import com.api.Autonova.models.site.SettingModel;
import com.api.Autonova.repository.ProductAttributesRepository;
import com.api.Autonova.repository.ProductsRepository;
import com.api.Autonova.repository.SettingRepository;
import com.api.Autonova.services.external_api.OneCApiService;
import com.api.Autonova.components.handlers.AttrPricesHandler;
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
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class UpdatePricesService {

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

    private Logger logger = LoggerFactory.getLogger(UpdatePricesService.class);
    private ListSearchUtil listSearchUtil = null;

    private static final int DEFAULT_NORMAL_LIMIT = 1000;

    public void update(){
        if(!processUpdateStatusManipulator.checkWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_STATUS) &&
                !processUpdateStatusManipulator.checkWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_PRICES_STATUS)){
            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_PRICES_STATUS,true);

            //обязательное действие перед запросом в 1С АПИ - обновление доступов, на случай изменения
            oneCApiService.updateAccess();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader reader = mapper.readerFor(new TypeReference<List<ProductUpdatedAttrs>>() {});

            if(listSearchUtil == null){ listSearchUtil = new ListSearchUtil(); }

            logger.info("Prices update: " + new Date());

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

                    List<ProductAttribute> attributes = productAttributesRepository.getAttrsInIdsAndNames(ids.size() > 0 ? ids : null, neededAttrPattern);

                    List<ProductUpdatedAttrs> productPrices = null;
                    JsonNode dataFrom1C = oneCApiService.getProductsDataByCodes(codes, neededAttrPattern);

                    try {
                        productPrices = reader.readValue(dataFrom1C);
                    } catch (IOException e) {
                        logger.error(Constants.ERROR_READ_DATA + dataFrom1C);
                    }

                    if(productPrices != null){

                        //make result
                        List<ProductAttribute> attributesToUpdate = new ArrayList<>();

                        for(ProductUpdatedAttrs productUpdatedAttrsItem : productPrices){

                            Integer indexProduct = null;

                            //priceFrom
                            ProductAttribute priceFromAttr = null;
                            if(productUpdatedAttrsItem.getPriceFrom() != null){
                                int indexOldPriceFrom = listSearchUtil.findAttrIndexByProductCodeAndName(attributes, productUpdatedAttrsItem.getCode(), Constants.PRODUCT_ATTR_PRICE_FROM);
                                if(indexOldPriceFrom >= 0){
                                    String valueNew = attrPricesHandler.handlePrice(productUpdatedAttrsItem.getPriceFrom());
                                    //check if value updated
                                    if(!attributes.get(indexOldPriceFrom).getValue().equals(valueNew)){
                                        priceFromAttr = attributes.get(indexOldPriceFrom);
                                        priceFromAttr.setValue(valueNew);
                                        attributesToUpdate.add(priceFromAttr);
                                    }
                                    valueNew = null;
                                }else {

                                    if(indexProduct == null){
                                        indexProduct = listSearchUtil.findProductIndexByCode(products, productUpdatedAttrsItem.getCode());
                                    }

                                    if(indexProduct >= 0){
                                        priceFromAttr = new ProductAttribute();
                                        priceFromAttr.setName(Constants.PRODUCT_ATTR_PRICE_FROM);
                                        priceFromAttr.setProduct(products.get(indexProduct));
                                        priceFromAttr.setProductCode(products.get(indexProduct).getCode());
                                        priceFromAttr.setValue(attrPricesHandler.handlePrice(productUpdatedAttrsItem.getPriceFrom()));
                                        attributesToUpdate.add(priceFromAttr);
                                    }
                                }
                            }

                            //priceTo
                            ProductAttribute priceToAttr = null;
                            if(productUpdatedAttrsItem.getPriceTo() != null){
                                int indexOldPriceTo = listSearchUtil.findAttrIndexByProductCodeAndName(attributes, productUpdatedAttrsItem.getCode(), Constants.PRODUCT_ATTR_PRICE_TO);
                                if(indexOldPriceTo >= 0){
                                    String valueNew = attrPricesHandler.handlePrice(productUpdatedAttrsItem.getPriceTo());
                                    //check if value updated
                                    if(!attributes.get(indexOldPriceTo).getValue().equals(valueNew)){
                                        priceToAttr = attributes.get(indexOldPriceTo);
                                        priceToAttr.setValue(valueNew);
                                        attributesToUpdate.add(priceToAttr);
                                    }
                                    valueNew = null;
                                }else {
                                    if(indexProduct == null){
                                        indexProduct = listSearchUtil.findProductIndexByCode(products, productUpdatedAttrsItem.getCode());
                                    }
                                    if(indexProduct >= 0){
                                        priceToAttr = new ProductAttribute();
                                        priceToAttr.setName(Constants.PRODUCT_ATTR_PRICE_TO);
                                        priceToAttr.setProduct(products.get(indexProduct));
                                        priceToAttr.setProductCode(products.get(indexProduct).getCode());
                                        priceToAttr.setValue(attrPricesHandler.handlePrice(productUpdatedAttrsItem.getPriceTo()));
                                        attributesToUpdate.add(priceToAttr);
                                    }
                                }
                            }

                            //priceSale
                            ProductAttribute priceSaleAttr = null;
                            if(productUpdatedAttrsItem.getPriceSale() != null){
                                int indexOldPriceSale = listSearchUtil.findAttrIndexByProductCodeAndName(attributes, productUpdatedAttrsItem.getCode(), Constants.PRODUCT_ATTR_PRICE_SALE);
                                if(indexOldPriceSale >= 0){
                                    String valueNew = attrPricesHandler.handlePrice(productUpdatedAttrsItem.getPriceSale());
                                    //check if value updated
                                    if(!attributes.get(indexOldPriceSale).getValue().equals(valueNew)){
                                        priceSaleAttr = attributes.get(indexOldPriceSale);
                                        priceSaleAttr.setValue(valueNew);
                                        attributesToUpdate.add(priceSaleAttr);
                                    }
                                    valueNew = null;
                                }else {
                                    if(indexProduct == null){
                                        indexProduct = listSearchUtil.findProductIndexByCode(products, productUpdatedAttrsItem.getCode());
                                    }
                                    if(indexProduct >= 0){
                                        priceSaleAttr = new ProductAttribute();
                                        priceSaleAttr.setName(Constants.PRODUCT_ATTR_PRICE_SALE);
                                        priceSaleAttr.setProduct(products.get(indexProduct));
                                        priceSaleAttr.setProductCode(products.get(indexProduct).getCode());
                                        priceSaleAttr.setValue(attrPricesHandler.handlePrice(productUpdatedAttrsItem.getPriceSale()));
                                        attributesToUpdate.add(priceSaleAttr);
                                    }
                                }
                            }

                            //sale
                            ProductAttribute saleAttr = null;
                            if(productUpdatedAttrsItem.getSale() != null){
                                int indexOldSale = listSearchUtil.findAttrIndexByProductCodeAndName(attributes, productUpdatedAttrsItem.getCode(), Constants.PRODUCT_ATTR_SALE);
                                if(indexOldSale >= 0){
                                    //check if value updated
                                    if(!attributes.get(indexOldSale).getValue().equals(productUpdatedAttrsItem.getSale())){
                                        saleAttr = attributes.get(indexOldSale);
                                        saleAttr.setValue(productUpdatedAttrsItem.getSale());
                                        attributesToUpdate.add(saleAttr);
                                    }
                                }else {
                                    if(indexProduct == null){
                                        indexProduct = listSearchUtil.findProductIndexByCode(products, productUpdatedAttrsItem.getCode());
                                    }
                                    if(indexProduct >= 0){
                                        saleAttr = new ProductAttribute();
                                        saleAttr.setName(Constants.PRODUCT_ATTR_SALE);
                                        saleAttr.setProduct(products.get(indexProduct));
                                        saleAttr.setProductCode(products.get(indexProduct).getCode());
                                        saleAttr.setValue(productUpdatedAttrsItem.getSale());
                                        attributesToUpdate.add(saleAttr);
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
                    productPrices = null;

                }catch (Exception e){
                    logger.error(Constants.ERROR_GLOBAL);
                    logger.error(e.getClass().getName());
                    logger.error(e.getMessage());
                    logger.error(ExceptionsUtil.getStackTrace(e.getStackTrace()));
                }
            }

            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_PRICES_STATUS,false);

        }else {
            throw new ServerException(Constants.ERROR_UPDATE_ALREADY_RUNNING);
        }
    }

    private List<String> makeNeededAttrsList(){
        List<String> attrNames = new ArrayList<>();
        attrNames.add(Constants.PRODUCT_ATTR_PRICE_FROM);
        attrNames.add(Constants.PRODUCT_ATTR_PRICE_TO);
        attrNames.add(Constants.PRODUCT_ATTR_PRICE_SALE);
        attrNames.add(Constants.PRODUCT_ATTR_SALE);
        return attrNames;
    }

    private int getLimit(){
        SettingModel settingLimit = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_PRICES_LIMIT_PRODUCTS);
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
