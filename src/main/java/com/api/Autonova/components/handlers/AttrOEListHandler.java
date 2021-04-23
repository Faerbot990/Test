package com.api.Autonova.components.handlers;

import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.Product;
import com.api.Autonova.models.ProductOENumber;
import com.api.Autonova.models.ProductUpdatedAttrs;
import com.api.Autonova.models.site.OENumberData;
import com.api.Autonova.repository.OENumbersDataRepository;
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
public class AttrOEListHandler {

    @Autowired
    OneCApiService oneCApiService;


    @Autowired
    OENumbersDataRepository oeNumbersDataRepository;

    private Logger logger = LoggerFactory.getLogger(AttrOEListHandler.class);

    //собираем список кодов в строку
    public String convertOENumbersToString(@NotNull List<ProductOENumber> list){
        String numbers = "";
        for(ProductOENumber number : list){
            if(number != null && number.getCode() != null){
                if(numbers.trim().length() > 0){
                    numbers += ",";
                }
                numbers += number.getCode().trim();
            }
        }
        return numbers;
    }
    //собираем список OE Numbers в строку (для передачи в метод изменения атрибута в 1С)
    private String convertOENumbersToJSONArray(@NotNull List<ProductOENumber> list){
        String numbers = "";
        for(ProductOENumber item : list){
            if(item != null && item.getCode() != null && item.getCode().trim().length() > 0){
                if(numbers.trim().length() > 0){
                    numbers += ",";
                }else {
                    numbers += "[";
                }
                numbers += "{\"" + Constants.PRODUCT_ATTR_OE_LIST_ITEM + "\"" + ":"+ item.getCode().trim() + "}";
            }
        }
        if(numbers.trim().length() > 0){
            numbers += "]";
        }
        return numbers;
    }


    public List<ProductOENumber> findNewOENumbersFromTecDoc(List<ProductOENumber> listFromTecDoc, List<ProductOENumber> listFrom1C){

//ЗДЕСЬ НЕОБХОДИМ КОНВЕРТЕР КОДОВ ИЗ ТЕКДОК В КОДЫ ИЗ 1С, конвертируем listFromTecDoc и получаем соответствия котов 1С-TecDoc

        List<ProductOENumber> listNew = new ArrayList<>();
        if(listFromTecDoc != null && listFrom1C != null){
            for(ProductOENumber itemTecDoc : listFromTecDoc){
                boolean isNew = true;
                if(itemTecDoc != null && itemTecDoc.getCode() != null){
                    for(ProductOENumber item1C : listFrom1C){
                        if(item1C != null && item1C.getCode() != null &&
                                item1C.getCode().trim().equals(itemTecDoc.getCode().trim())){
                            isNew = false;
                        }
                    }
                }else {
                    isNew = false;
                }
                if(isNew){
                    listNew.add(itemTecDoc);
                }
            }
        }
        return listNew;
    }

    public List<ProductOENumber> addNewOENumbersTo1C(List<ProductOENumber> listNew, String productCode){
        try{
            JsonNode updatedData = oneCApiService.updateProductAttribute(productCode, Constants.PRODUCT_ATTR_OE_LIST, convertOENumbersToJSONArray(listNew));
            //get updated product data
            if(updatedData != null && updatedData.get(Constants.PRODUCT_ATTR_OE_LIST) != null){
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //for ignore another param in model
                ObjectReader reader = mapper.readerFor(new TypeReference<List<ProductOENumber>>() {});
                try {
                    return reader.readValue(updatedData.get(Constants.PRODUCT_ATTR_OE_LIST));
                }catch (IOException e) {
                    logger.error(e.getMessage());
                    logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "attribute: " + "'" + Constants.PRODUCT_ATTR_OE_LIST + "'" + " from product:" + "'" + productCode + "'");
                    return null;
                }
            }else {
                logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "attribute: " + "'" + Constants.PRODUCT_ATTR_OE_LIST + "'" + " from product:" + "'" + productCode + "'");
                return null;
            }
        }catch (ServerException e){
            logger.error(e.getMessage());
            logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "attribute: " + "'" + Constants.PRODUCT_ATTR_OE_LIST + "'" + " from product:" + "'" + productCode + "'");
            return null;
        }
    }

    //for site - generate table
    public void makeOeListDataByProduct(List<ProductOENumber> numbersFrom1C, Product product){
        if(numbersFrom1C != null){

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader reader = mapper.readerFor(new TypeReference<List<JsonNode>>() {});

            List<OENumberData> dellNumbers = new ArrayList<>();
            List<OENumberData> newNumbers = new ArrayList<>();
            List<OENumberData> oldNumbers = oeNumbersDataRepository.findAllByParentId(product.getId());

            //update from 1C with all data
            JsonNode dataFrom1C = oneCApiService.getProductsDataByCodes(makeOeStringList(numbersFrom1C), makeNeededOEAttrsList());
            List<JsonNode> OENumbersDataFrom1C = null;
            try {
                OENumbersDataFrom1C = reader.readValue(dataFrom1C);
            }catch (IOException e) {
                logger.error(Constants.ERROR_READ_DATA + dataFrom1C);
            }

            if(OENumbersDataFrom1C != null){
                for(JsonNode oeNumberDataItem : OENumbersDataFrom1C) {
                    if (oeNumberDataItem != null && oeNumberDataItem.get(Constants.PRODUCT_ATTR_CODE) != null && oeNumberDataItem.get(Constants.PRODUCT_ATTR_ARTICLE) != null && oeNumberDataItem.get(Constants.PRODUCT_ATTR_MANUFACTURER_NAME) != null &&
                            oeNumberDataItem.get(Constants.PRODUCT_ATTR_MANUFACTURER_ID) != null) {
                        OENumberData oeNumberData = new OENumberData();
                        oeNumberData.setCode(oeNumberDataItem.get(Constants.PRODUCT_ATTR_CODE).asText());
                        oeNumberData.setParentCode(product.getCode());
                        oeNumberData.setParentId(product.getId());
                        oeNumberData.setManufacturerId(oeNumberDataItem.get(Constants.PRODUCT_ATTR_MANUFACTURER_ID).asText());
                        oeNumberData.setManufacturerName(oeNumberDataItem.get(Constants.PRODUCT_ATTR_MANUFACTURER_NAME).asText());
                        oeNumberData.setArticle(oeNumberDataItem.get(Constants.PRODUCT_ATTR_ARTICLE).asText());
                        newNumbers.add(oeNumberData);
                    }
                }
                //update DB and dell old
                if(oldNumbers != null){
                    for(OENumberData oldNumberItem : oldNumbers){
                        boolean notFound = true;
                        for (OENumberData newNumberItem : newNumbers){
                            if(oldNumberItem.getCode().equals(newNumberItem.getCode()) &&
                                    oldNumberItem.getParentId() == newNumberItem.getParentId()){
                                notFound = false;
                                newNumberItem.setId(oldNumberItem.getId());
                            }
                        }
                        if(notFound){
                            dellNumbers.add(oldNumberItem);
                        }
                    }
                }

                //delete old
                for(OENumberData dellItem : dellNumbers){
                    oeNumbersDataRepository.deleteById(dellItem.getId());
                }
                //save or update new
                oeNumbersDataRepository.save(newNumbers);
            }
            //clear memory
            mapper = null;
            reader = null;
            dellNumbers = null;
            oldNumbers = null;
            newNumbers = null;
        }
    }
    private List<String> makeOeStringList(List<ProductOENumber> numbersFrom1C){
        ArrayList<String> result = new ArrayList<>();
        if(numbersFrom1C != null){
            for(ProductOENumber oeNumberItem : numbersFrom1C){
                result.add(oeNumberItem.getCode());
            }
        }
        return result;
    }
    private List<String> makeNeededOEAttrsList(){
        List<String> attrNames = new ArrayList<>();
        attrNames.add(Constants.PRODUCT_ATTR_ARTICLE);
        attrNames.add(Constants.PRODUCT_ATTR_MANUFACTURER_ID);
        attrNames.add(Constants.PRODUCT_ATTR_MANUFACTURER_NAME);
        return attrNames;
    }
}
