package com.api.Autonova.components.handlers;

import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.Product;
import com.api.Autonova.models.ProductCharacteristic;
import com.api.Autonova.repository.ProductCharacteristicsRepository;
import com.api.Autonova.services.external_api.OneCApiService;
import com.api.Autonova.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Component
public class AttrSpecListHandler {

    @Autowired
    OneCApiService oneCApiService;

    @Autowired
    ProductCharacteristicsRepository productCharacteristicsRepository;

    private Logger logger = LoggerFactory.getLogger(AttrSpecListHandler.class);

    public String convertCharacteristicsToString(@NotNull List<ProductCharacteristic> list){
        String ids = "";
        for(ProductCharacteristic characteristic : list){
            if(characteristic != null && characteristic.getId() != 0){
                if(ids.trim().length() > 0){
                    ids += ",";
                }
                ids += characteristic.getId();
            }
        }
        return ids;
    }

    public List<ProductCharacteristic> findNewCharacteristicFromTecDoc(List<ProductCharacteristic> charactFrom1C, List<ProductCharacteristic> charactFromTecDoc){
        List<ProductCharacteristic> charactNew = new ArrayList<>();
        //Ищем новые х-ки в текдок которых нету в 1С
        if(charactFromTecDoc != null && charactFrom1C != null){
            for(ProductCharacteristic itemTecDoc : charactFromTecDoc){
                boolean isNew = true;
                if(itemTecDoc != null && itemTecDoc.getAttrShortName() != null){
                    for(ProductCharacteristic item1C : charactFrom1C){
                        if(item1C != null && item1C.getAttrShortName() != null &&
                                item1C.getAttrShortName().trim().equals(itemTecDoc.getAttrShortName().trim()) ){
                            isNew = false;
                            break;
                        }
                    }
                }else {
                    isNew = false;
                }
                if(isNew){
                    charactNew.add(itemTecDoc);
                }
            }
        }
        return charactNew;
    }

    public void addNewCharacteristicsTo1C(List<ProductCharacteristic> charactsNew, String productCode){
        //add new characteristics from tec doc to 1C
        for(ProductCharacteristic itemNew : charactsNew){
            if(itemNew != null && itemNew.getAttrShortName() != null){
//ИЗ ТЕК ДОКА НЕТУ ДРУГОГО ЯЗЫКА УКР ЗНАЧЕНИЯ ЗАПОЛНЯЕМ РУССКИМИ
                itemNew.setAttrNameUa(itemNew.getAttrName());
                itemNew.setAttrShortNameUa(itemNew.getAttrShortName());
                itemNew.setAttrValueUa(itemNew.getAttrValue());
                try{
                    oneCApiService.addCharacteristic(productCode, itemNew);
                }catch (ServerException e){
                    logger.error(e.getMessage());
                    logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "specification: " + "'" + itemNew.getAttrShortName() + "'" + " from: " + productCode);
                }
            }else {
                logger.error(Constants.MAIN_SERVICE_ERROR_SAVE_CHARACTERISTIC  + "null" + " "
                        + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + productCode);
            }
        }
    }

    public void saveAllCharacteristicsToDB(List<ProductCharacteristic> charactFrom1C, String productCode, Product product){
        //List<ProductCharacteristic> charactFromDB = productCharacteristicsRepository.findAllByProductCode(productCode);
        List<ProductCharacteristic> charactFromDB = product.getCharacteristics();
        if(charactFrom1C != null){
            //удаляем х-ки которых нету в 1С но есть в БД
            List<ProductCharacteristic> charactDell = new ArrayList<>();
            if(charactFromDB != null){
                for(ProductCharacteristic itemDB : charactFromDB){
                    boolean dellItem = true;
                    for(ProductCharacteristic item1C : charactFrom1C){
                        if(item1C != null){
                            if(itemDB.getAttrShortName().equals(item1C.getAttrShortName())){
                                dellItem = false;
                            }
                        }
                    }
                    if(dellItem){
                        charactDell.add(itemDB);
                    }
                }
                //dell
                for(ProductCharacteristic itemDell : charactDell){
                    productCharacteristicsRepository.deleteByProductCodeAndId(itemDell.getProductCode(),itemDell.getId());
                }
            }
            //проверяем БД и переодновляем либо добавляем новые х-ки
            for(ProductCharacteristic item1C : charactFrom1C){
                if(item1C != null && item1C.getId() != 0 && item1C.getAttrShortName() != null){
                    saveCharacteristicToDB(charactFromDB, item1C, productCode, product);
                }
            }
        }
    }

    private void saveCharacteristicToDB(List<ProductCharacteristic> charactFromDB, @NotNull ProductCharacteristic characteristicNew, String productCode,
                                        Product product){
        boolean newInDB = true;
        try{
            if(charactFromDB != null){
                for(ProductCharacteristic itemDB : charactFromDB){
                    if(characteristicNew.getAttrShortName() != null && characteristicNew.getAttrShortName().equals(itemDB.getAttrShortName())){
                        newInDB = false;
                        //update value
                        characteristicNew.setLocalid(itemDB.getLocalid());
                        characteristicNew.setProductCode(productCode);
                        characteristicNew.setProduct(product);
//ВРЕМЕННАЯ функция для дублирования значения
                        if(characteristicNew.getAttrNameUa() == null || characteristicNew.getAttrNameUa().trim().length() == 0){
                            characteristicNew.setAttrNameUa(characteristicNew.getAttrName());
                        }
                        if(characteristicNew.getAttrShortNameUa() == null || characteristicNew.getAttrShortNameUa().trim().length() == 0){
                            characteristicNew.setAttrShortNameUa(characteristicNew.getAttrShortName());
                        }
                        //в этом параметре пока встречались только числовые значение, возможно дублирование необходимо оставить
                        if(characteristicNew.getAttrValueUa() == null || characteristicNew.getAttrValueUa().trim().length() == 0){
                            characteristicNew.setAttrValueUa(characteristicNew.getAttrValue());
                        }
                    //удаления спец символа Ø, который получили из внешний апи
                        characteristicNew.setAttrName(characteristicNew.getAttrName().replaceAll("Ø","Диаметр").trim());
                        characteristicNew.setAttrNameUa(characteristicNew.getAttrNameUa().replaceAll("Ø","Діаметр").trim());
                        characteristicNew.setAttrShortName(characteristicNew.getAttrShortName().replaceAll("Ø","Диаметр").trim());
                        characteristicNew.setAttrShortNameUa(characteristicNew.getAttrShortNameUa().replaceAll("Ø","Діаметр").trim());
                        productCharacteristicsRepository.save(characteristicNew);
                    }
                }
            }
            //if characterisctic from 1C new for DB
            if(newInDB){
                characteristicNew.setProductCode(productCode);
                characteristicNew.setProduct(product);
//ВРЕМЕННАЯ функция для дублирования значения
                if(characteristicNew.getAttrNameUa() == null || characteristicNew.getAttrNameUa().trim().length() == 0){
                    characteristicNew.setAttrNameUa(characteristicNew.getAttrName());
                }
                if(characteristicNew.getAttrShortNameUa() == null || characteristicNew.getAttrShortNameUa().trim().length() == 0){
                    characteristicNew.setAttrShortNameUa(characteristicNew.getAttrShortName());
                }
                //в этом параметре пока встречались только числовые значение, возможно дублирование необходимо оставить
                if(characteristicNew.getAttrValueUa() == null || characteristicNew.getAttrValueUa().trim().length() == 0){
                    characteristicNew.setAttrValueUa(characteristicNew.getAttrValue());
                }
                //удаления спец символа Ø, который получили из внешний апи  - ОЧИЩАЮ СИМВОЛ РАНЕЕ
                //characteristicNew.setAttrName(characteristicNew.getAttrName().replaceAll("Ø","Диаметр").trim());
                //characteristicNew.setAttrNameUa(characteristicNew.getAttrNameUa().replaceAll("Ø","Діаметр").trim());
                //characteristicNew.setAttrShortName(characteristicNew.getAttrShortName().replaceAll("Ø","Диаметр").trim());
                //characteristicNew.setAttrShortNameUa(characteristicNew.getAttrShortNameUa().replaceAll("Ø","Діаметр").trim());
                productCharacteristicsRepository.save(characteristicNew);
            }
        }catch (DataIntegrityViolationException e){
            logger.error(e.getMessage());
            logger.error(Constants.MYSQL_ITEM_VALUE_EXCEEDED + "'saveCharacteristicToDB'" + " from " + "'" + productCode + "'" + " with attr "
                    + "'" + characteristicNew.getAttrShortName() + "'");
        }
    }

    public void changeIncorrectSymbols(List<ProductCharacteristic> characteristics){
        if(characteristics != null){
            for(ProductCharacteristic characteristicItem : characteristics){
                if(characteristicItem != null && characteristicItem.getAttrShortName() != null
                    && characteristicItem.getAttrShortName().contains("Ø")){
                    characteristicItem.setAttrShortName(characteristicItem.getAttrShortName().replaceAll("Ø","Диаметр").trim());
                }
                if(characteristicItem != null && characteristicItem.getAttrName() != null
                        && characteristicItem.getAttrName().contains("Ø")){
                    characteristicItem.setAttrName(characteristicItem.getAttrName().replaceAll("Ø","Диаметр").trim());
                }
            }
        }
    }

}
