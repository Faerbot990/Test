package com.api.Autonova.components.handlers;

import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.Product;
import com.api.Autonova.models.ProductAnalog;
import com.api.Autonova.repository.ProductsRepository;
import com.api.Autonova.services.external_api.OneCApiService;
import com.api.Autonova.utils.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AttrAnalogListHandler {

    @Autowired
    OneCApiService oneCApiService;

    @Autowired
    ProductsRepository productsRepository;

    private Logger logger = LoggerFactory.getLogger(AttrAnalogListHandler.class);

    //convert list to string with `,`
    public String convertAnalogsToString(@NotNull List<ProductAnalog> list){
        String analogs = "";
        for(ProductAnalog item : list){
            if(item != null && item.getCode() != null){
                if(analogs.trim().length() > 0){
                    analogs += ",";
                }
                analogs += item.getCode().trim();
            }
        }
        return analogs;
    }

    //convert list analogs to JSON array string (for transfer to the attribute change method in 1C)
    private String convertAnalogsToJSONArray(@NotNull List<ProductAnalog> list){
        String analogs = "";
        for(ProductAnalog item : list){
            if(item != null && item.getCode() != null && item.getCode().trim().length() > 0){
                if(analogs.trim().length() > 0){
                    analogs += ",";
                }else {
                    analogs += "[";
                }
                analogs += "{\"" + Constants.PRODUCT_ATTR_ANALOG_LIST_ITEM + "\"" + ":"+ item.getCode().trim() + "}";
            }
        }
        if(analogs.trim().length() > 0){
            analogs += "]";
        }
        return analogs;
    }

    private List<ProductAnalog> findNewAnalogsFromTecDoc(List<ProductAnalog> analogsFromTecDoc, List<ProductAnalog> analogsFrom1C){
        List<ProductAnalog> analogsNew = new ArrayList<>();
        if(analogsFromTecDoc != null && analogsFrom1C != null){
            for(ProductAnalog itemTecDoc : analogsFromTecDoc){
                boolean isNew = true;
                if(itemTecDoc != null && itemTecDoc.getCode() != null){
                    for(ProductAnalog item1C : analogsFrom1C){
                        if(item1C != null && item1C.getCode() != null &&
                                item1C.getCode().trim().equals(itemTecDoc.getCode().trim())){
                            isNew = false;
                        }
                    }
                }else {
                    isNew = false;
                }
                if(isNew){
                    analogsNew.add(itemTecDoc);
                }
            }
        }
        return analogsNew;
    }

//ЗДЕСЬ НЕОБХОДИМ КОНВЕРТЕР КОДОВ ИЗ ТЕКДОК В КОДЫ ИЗ 1С, конвертируем listFromTecDoc и получаем соответствия котов 1С-TecDoc

    private List<ProductAnalog> addNewAnalogsTo1C(List<ProductAnalog> analogsNew, String productCode){
        try{
            JsonNode updatedData = oneCApiService.updateProductAttribute(productCode, Constants.PRODUCT_ATTR_ANALOG_LIST, convertAnalogsToJSONArray(analogsNew));
            //get updated product data
            if(updatedData != null && updatedData.get(Constants.PRODUCT_ATTR_ANALOG_LIST) != null){
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //for ignore another param in model
                ObjectReader reader = mapper.readerFor(new TypeReference<List<ProductAnalog>>() {});
                try {
                    return reader.readValue(updatedData.get(Constants.PRODUCT_ATTR_ANALOG_LIST));
                }catch (IOException e) {
                    logger.error(e.getMessage());
                    logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "attribute: " + "'" + Constants.PRODUCT_ATTR_ANALOG_LIST + "'" + " from product:" + "'" + productCode + "'");
                    return null;
                }
            }else {
                logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "attribute: " + "'" + Constants.PRODUCT_ATTR_ANALOG_LIST + "'" + " from product:" + "'" + productCode + "'");
                return null;
            }
        }catch (ServerException e){
            logger.error(e.getMessage());
            logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "attribute: " + "'" + Constants.PRODUCT_ATTR_ANALOG_LIST + "'" + " from product:" + "'" + productCode + "'");
            return null;
        }
    }

//МОМЕНТ АНАЛОГОВ В РАЗРАБОТКЕ НА ОЖИДАНИИ
    //Прорабатываем все товары с артикулом, если нет в БД, спрашиваем в 1С, потом в ТекДок, если нет то создаём пустой товар
    private void initAnalogs(List<ProductAnalog> listFrom1C){
        for(ProductAnalog analog : listFrom1C){
            if(analog != null && analog.getCode() != null && analog.getCode().trim().length() > 0){
                //check product id DB
                if(productsRepository.findProductByCode(analog.getCode()) == null){
                    //add new
                    Product productNew = new Product();
                    productNew.setCode(analog.getCode().trim());
                    productsRepository.save(productNew);

                    //find in 1C
                /*JsonNode productFromOneC = oneCApiService.getProductData(analog.getCode());
                if(productFromOneC != null){
                    //ДОБАВИТЬ ВСЕ АТРИБУТЫ  возможно другим методом ибо если основным, будет рекурсия ибо внутри там так же вызов аналогов
                }else {
                    //find  in TecDoc
                    //Невозможно ибо запрос делаем по артикулу, которые получаем в продукте из 1С
                }*/
                }
            }
        }
    }


}
