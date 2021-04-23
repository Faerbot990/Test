package com.api.Autonova.services.update;


import com.api.Autonova.components.ProcessUpdateStatusManipulator;
import com.api.Autonova.components.cache.CacheUpdateComponent;
import com.api.Autonova.components.handlers.*;
import com.api.Autonova.exceptions.AccessException;
import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.*;
import com.api.Autonova.models.site.SettingModel;
import com.api.Autonova.repository.AttributesPatternsRepository;
import com.api.Autonova.repository.ProductAttributesRepository;
import com.api.Autonova.repository.ProductsRepository;
import com.api.Autonova.repository.SettingRepository;
import com.api.Autonova.services.external_api.OneCApiService;
import com.api.Autonova.services.external_api.TecDocApiService;
import com.api.Autonova.utils.Constants;
import com.api.Autonova.utils.ExceptionsUtil;
import com.api.Autonova.utils.ListSearchUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.sun.istack.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

@Service
public class UpdateMainService {

    //Repositories
    @Autowired
    AttributesPatternsRepository attributesPatternsRepository;

    @Autowired
    ProductsRepository productsRepository;

    @Autowired
    ProductAttributesRepository productAttributesRepository;


    //Services external API
    @Autowired
    OneCApiService oneCApiService;

    @Autowired
    TecDocApiService tecDocApiService;

    //Attr handlers
    @Autowired
    AttrCarListHandler attrCarListHandler;

    @Autowired
    AttrImageListHandler attrImageListHandler;

    @Autowired
    AttrSpecListHandler attrSpecListHandler;

    @Autowired
    AttrOEListHandler attrOEListHandler;

    @Autowired
    AttrAnalogListHandler attrAnalogListHandler;

    @Autowired
    AttrPricesHandler attrPricesHandler;

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    ProcessUpdateStatusManipulator processUpdateStatusManipulator;

    @Autowired
    CacheUpdateComponent cacheUpdateComponent;

    private Logger logger = LoggerFactory.getLogger(UpdateMainService.class);

    private ListSearchUtil listSearchUtil = null;

    private String updateTime = null;

    private static final int DEFAULT_NORMAL_LIMIT = 100;

    public void updateMain(){
        if(!processUpdateStatusManipulator.checkWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_STATUS)){
            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_STATUS, true);
            try {
                setupEnvironment();
                //start service
                logger.info("Main update: " + updateTime);
                //update all cars
                updateAllCars();
                cacheUpdateComponent.clearProductsDataCache();
                cacheUpdateComponent.clearCarsDataCache();
                logger.info("------ Car update complete");
                //update all categories - products - attrs - attr items
                updateCategories();
                cacheUpdateComponent.clearProductsDataCache();
                cacheUpdateComponent.clearCarsDataCache();
                logger.info("------ Products update complete");
            }catch (Exception e){
                logger.error(Constants.ERROR_GLOBAL);
                logger.error(e.getClass().getName());
                logger.error(e.getMessage());
                logger.error(ExceptionsUtil.getStackTrace(e.getStackTrace()));
            }
            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_STATUS,false);
        }else {
            throw new ServerException(Constants.ERROR_UPDATE_ALREADY_RUNNING);
        }
    }

    //Обновление данных по кодам
    public void updateByCodes(@NotNull List<String> codes) {
        if(!processUpdateStatusManipulator.checkWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_BY_CODES_STATUS)) {
            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_BY_CODES_STATUS, true);

            setupEnvironment();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader reader = mapper.readerFor(new TypeReference<List<JsonNode>>() {});

            //start service
            logger.info("Products by code update: " + updateTime);

            int productCount = codes.size();
            int LIMIT = getLimit();
            int PAGE = productCount % LIMIT == 0 ? (int) productCount/ LIMIT : ((int)productCount / LIMIT) + 1;

            for(int i = 0; i < PAGE; i++){
                try {

                    List<JsonNode> products = null;
                    JsonNode dataFrom1C = oneCApiService.getProductsDataByCodes(codes.subList(LIMIT * i, Math.min(productCount, (LIMIT*i)+LIMIT)), makeNeededAttrsList());
                    try {
                        products = reader.readValue(dataFrom1C);
                    } catch (IOException e) {
                        logger.error(Constants.ERROR_READ_DATA + dataFrom1C);
                    }
                    updateProducts(products);
                    dataFrom1C = null;
                    products = null;
                }catch (Exception e){
                    logger.error(Constants.ERROR_GLOBAL);
                    logger.error(e.getClass().getName());
                    logger.error(e.getMessage());
                    logger.error(ExceptionsUtil.getStackTrace(e.getStackTrace()));
                }
            }
            cacheUpdateComponent.clearProductsDataCache();
            mapper = null;
            reader = null;

            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_UPDATE_BY_CODES_STATUS, false);
        }else {
            throw new ServerException(Constants.ERROR_PROCESS_ALREADY_RUNNING);
        }
    }

    //WORK WITH CARS
    //Общий метод, не зависящий от ЗЧ берет все авто из 1С и переобновляем в БД что бы не повторять это в каждой ЗЧ
    private void updateAllCars(){
        List<Car> allCarsFrom1C = oneCApiService.getAllCars();
        if(allCarsFrom1C != null){

            //int carStep = allCarsFrom1C.size();
            //System.out.println("------- CARS ALL" + carStep);

            for(Car carItem : allCarsFrom1C){
                //System.out.println("------- CAR STEP " + carStep + " FROM " + carsAll);
                //convert date value
                try{
                    //технически добавляю -01 первый день месяца что бы хранить дату в БД
                    String dataFrom = carItem.getYearOfConstrFrom().substring(0, 4) + "-" + carItem.getYearOfConstrFrom().substring(4, 6) + "-01";
                    carItem.setYearOfConstrFrom(dataFrom);
                    dataFrom = null;
                }catch (StringIndexOutOfBoundsException | NullPointerException e){
                    carItem.setYearOfConstrFrom(null);
                }
                try{
                    //технически добавляю -01 первый день месяца что бы хранить дату в БД
                    String dataTo = carItem.getYearOfConstrTo().substring(0, 4) + "-" + carItem.getYearOfConstrTo().substring(4, 6) + "-01";
                    carItem.setYearOfConstrTo(dataTo);
                    dataTo = null;
                }catch (StringIndexOutOfBoundsException | NullPointerException e){
                    carItem.setYearOfConstrTo(null);
                }
                attrCarListHandler.saveCarToDB(carItem);
                //carStep -= 1;
            }
        }
        allCarsFrom1C = null;
        //Here can create method for del old car
    }



    //update all categories
    private void updateCategories(){
        //get all categories
        List<CategoryInResponse> allCategories = oneCApiService.getCategories();

        if(allCategories != null){
            //get products from all categories

            int categoryAll = allCategories.size();
            int categoryStep = allCategories.size();
            System.out.println("------- CATEGORY ALL " + categoryStep);

            for(CategoryInResponse category : allCategories){
                //if is not subcategory
                if(category.getCategory().getParent() == 0){
                    List<JsonNode> productsFromCategory = null;

                    int amount = getLimit();
                    int offset = 0;

                    do {
                        try {
                            productsFromCategory = oneCApiService.getProductsDataWithAttrsByCategory(String.valueOf(category.getCategory().getId()), makeNeededAttrsList(), offset, amount);
                            updateProducts(productsFromCategory);
                            cacheUpdateComponent.clearProductsDataCache();
                            cacheUpdateComponent.clearCarsDataCache();
                            System.out.println("---------------------------- COMPLETE PRODUCTS  - " + offset + " - " + (offset + amount));
                        }catch (Exception e){
                            logger.error(Constants.ERROR_GLOBAL +  "category " + category.getCategory().getId() + " with: offset = " + offset + " and amount = " + amount + ", are not updated.");
                            logger.error(e.getClass().getName());
                            logger.error(e.getMessage());
                            logger.error(ExceptionsUtil.getStackTrace(e.getStackTrace()));
                        }

                        offset += amount;

                    }while (productsFromCategory != null && productsFromCategory.size() > 0);
                    productsFromCategory = null;
                }

                System.out.println("------- CATEGORY STEP " + categoryStep + " FROM " + categoryAll);
                categoryStep -= 1;
            }
        }else {
            throw new ServerException(Constants.MAIN_SERVICE_DATA_ERROR + "categories");
        }
    }

    private void updateProducts(List<JsonNode> productsFrom1C){
        if(productsFrom1C != null && productsFrom1C.size() > 0){

            List<String> codes = new ArrayList<>();

            List<JsonNode> productsFrom1CNormal = new ArrayList<>();

            //filter "normal" product and get codes
            for(JsonNode attrItem : productsFrom1C){
                int priceFrom = 0;
                int priceTo = 0;
                try {
                    priceFrom = attrItem.get(Constants.PRODUCT_ATTR_PRICE_FROM).asInt(0);
                    priceTo = attrItem.get(Constants.PRODUCT_ATTR_PRICE_TO).asInt(0);
                }catch (NullPointerException e){}

                if(attrItem.get(Constants.PRODUCT_ATTR_CODE) == null || attrItem.get(Constants.PRODUCT_ATTR_CODE).asText().trim().length() == 0 ||
                        priceFrom <= 0 || priceTo <= 0){
                    logger.error(Constants.ERROR_PRODUCT_NOT_SAVE_IN_DB + attrItem);
                }else {
                    codes.add(attrItem.get(Constants.PRODUCT_ATTR_CODE).asText());
                    productsFrom1CNormal.add(attrItem);
                }
            }

            //get old products by codes
            List<Product> productsOld = productsRepository.findAllInCodes(codes.size() > 0 ? codes : null);

            //update
            List<Product> productsOldToUpdate = new ArrayList<>();
            List<Product> productsNewToUpdate = new ArrayList<>();
            List<ProductAttribute> attributesToUpdate = new ArrayList<>();
            List<JsonNode> attributesToUpdateNew = new ArrayList<>(); //save after save new products

            for(JsonNode productItemAttrs : productsFrom1CNormal){

                String code = productItemAttrs.get(Constants.PRODUCT_ATTR_CODE).asText();

                int indexOldProduct = listSearchUtil.findProductIndexByCode(productsOld, code);

                Product product = null;
                if(indexOldProduct >= 0){

                    product = productsOld.get(indexOldProduct);
                    if(updateTime != null){ product.setUpdateTime(updateTime); }
                    productsOldToUpdate.add(product);
                    //сразу обрабатываем атрибуты ЗЧ, который уже в БД
                    attributesToUpdate.addAll(handleAllAttributesForProduct(productItemAttrs, product));

                }else {

                    product = new Product();
                    product.setCode(code);
                    if(updateTime != null){ product.setUpdateTime(updateTime); }
                    productsNewToUpdate.add(product);
                    attributesToUpdateNew.add(productItemAttrs);
                }
            }

            productsRepository.save(productsOldToUpdate);
            productsRepository.save(productsNewToUpdate);

            //обрабатываем атрибуты новых ЗЧ после их сохранения (получив id из БД
            for(JsonNode productItemAttrs : attributesToUpdateNew){
                int indexProduct = listSearchUtil.findProductIndexByCode(productsNewToUpdate, productItemAttrs.get(Constants.PRODUCT_ATTR_CODE).asText());
                if(indexProduct >= 0){
                    attributesToUpdate.addAll(handleAllAttributesForProduct(productItemAttrs, productsNewToUpdate.get(indexProduct)));
                }
            }

            productAttributesRepository.save(attributesToUpdate);

            //clear data
            codes = null;
            productsOld = null;
            productsOldToUpdate = null;
            productsNewToUpdate = null;
            attributesToUpdate = null;
            attributesToUpdateNew = null;
            productsFrom1C = null;
            productsFrom1CNormal = null;
        }
    }


    //ADD and synchronized all atributes to product
    public List<ProductAttribute> handleAllAttributesForProduct(JsonNode productData, Product product) {  //private

        if(listSearchUtil == null) {listSearchUtil = new ListSearchUtil();}

        List<ProductAttribute> attributesResult = new ArrayList<>();

        List<ProductAttribute> attributesOld = product.getAttributes();

        ProductAttribute attribute;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //for ignore another param in model
        ObjectReader reader = null;

        //get TecDoc inner article
        String articleTecDocId = null;
        if(productData.get(Constants.PRODUCT_ATTR_ARTICLE) != null && productData.get(Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID) != null){
            //get local TecDoc article
            try{
                articleTecDocId = tecDocApiService.getLocalArticle(productData.get(Constants.PRODUCT_ATTR_ARTICLE).asText(), productData.get(Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID).asText());
            }catch (AccessException e){
                logger.error(e.getMessage());
            }
        }

        //handle attrs
        for(Iterator<String> it = productData.fieldNames(); it.hasNext();) {
            String name = it.next();

            attribute = new ProductAttribute();
            attribute.setName(name);
            attribute.setProduct(product);
            //code key проверен NotNull ранее в методе saveProduct
            attribute.setProductCode(productData.get(Constants.PRODUCT_ATTR_CODE).asText());

            //if attr like LIST
            switch (name){
                case Constants.PRODUCT_ATTR_ANALOG_LIST:
                    reader = mapper.readerFor(new TypeReference<List<ProductAnalog>>() {});
                    try {
                        List<ProductAnalog> analogsFrom1C = reader.readValue(productData.get(name));
                        if(analogsFrom1C != null){
//НЕОБХОДИМ ЗАПРОС В TecDoc на получение АНАЛОГОВ
                            /*List<ProductAnalogSite>  analogsFromTecDoc = new ArrayList<>();
//ТАК Же необходим КОНВЕРТЕР кодов из тек док перед записью в 1С
                            List<ProductAnalogSite> analogsNew = findNewAnalogsFromTecDoc(analogsFromTecDoc, analogsFrom1C);
                            if(analogsNew != null && analogsNew.size() > 0){
                                List<ProductAnalogSite> analogsUpdated = addNewAnalogsTo1C(analogsNew, attribute.getProductCode());
                                if(analogsUpdated != null){
                                    analogsFrom1C.clear();
                                    analogsFrom1C.addAll(analogsUpdated);
                                }
                            }*/

//В ЭТОМ МОМЕНТЕ ПО СОГЛАСОВАНИЯМ ЗАПИСЫВАЕМ НОВЫЕ В БД
//ЗДЕСЬ ПОКА ЗАКОМЕНТИРУЮ СОГЛАСОВАТЬ ИБО ЭТОТ МОМЕНТ ЗАГРОМОЖДАЕТ БД
//                            initAnalogs(analogsFrom1C);
                            attribute.setValue(attrAnalogListHandler.convertAnalogsToString(analogsFrom1C));
                        }else {
                            logger.error(Constants.MAIN_SERVICE_ERROR_UPDATE_ATTR + Constants.PRODUCT_ATTR_ANALOG_LIST + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + attribute.getProductCode());
                        }
                        analogsFrom1C = null;
                    } catch (IOException e) {
                        logger.error(Constants.MAIN_SERVICE_ERROR_UPDATE_ATTR + Constants.PRODUCT_ATTR_ANALOG_LIST + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + attribute.getProductCode());
                        e.printStackTrace();
                    }
                    reader = null;
                    break;

                case Constants.PRODUCT_ATTR_CAR_LIST:
                    //Request for all cars from current product
                    List<Car> carsFrom1C = oneCApiService.getAllCarsForProduct(attribute.getProductCode());
                    if(carsFrom1C != null){
                        if(articleTecDocId != null){
                            //TecDoc request
                            List<Car> carsFromTecDoc = null;
                            try {
                                carsFromTecDoc = tecDocApiService.getCarsForProduct(articleTecDocId);
                            }catch (AccessException e){
                                logger.error(e.getMessage());
                            }
                            //ищем новые в TecDoc
                            List<Car> carsNew = attrCarListHandler.findNewCarsFromTecDoc(carsFrom1C, carsFromTecDoc);
                            carsFromTecDoc = null;
                            if(carsNew != null && carsNew.size() > 0){
                                //записываем новые в 1С и автоматически сохраняем в БД
                                attrCarListHandler.addNewCarsTo1C(carsNew, attribute.getProductCode());
                                //add new cars to all
                                carsFrom1C.addAll(carsNew);  //техническое добавление всех новых идентификаторов (Возможно нужно запрашивать снова все авто ЗЧ но это нагрузит +1 запросом)
                                //update attr in 1C
                                try{
                                    oneCApiService.updateProductAttribute(attribute.getProductCode(), Constants.PRODUCT_ATTR_CAR_LIST, attrCarListHandler.convertCarsToJSONArray(carsFrom1C));
                                    //Request 2 for all cars from current product (if add news)
                                    List<Car> carsFrom1CUpdated = oneCApiService.getAllCarsForProduct(attribute.getProductCode());
                                    if(carsFrom1CUpdated != null){
                                        carsFrom1C.clear();
                                        carsFrom1C.addAll(carsFrom1CUpdated);
                                    }
                                }catch (ServerException e){
                                    logger.error(e.getMessage());
                                    logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "attribute: " + "'" + Constants.PRODUCT_ATTR_CAR_LIST + "'" + " from product:" + "'" + attribute.getProductCode() + "'");
                                }
                            }
                            carsNew = null;
                            attribute.setValue(attrCarListHandler.convertCarsToString(carsFrom1C));
                            attrCarListHandler.makeCarsToProductLink(attribute);
                        }else {
                            attribute.setValue(attrCarListHandler.convertCarsToString(carsFrom1C));
                            attrCarListHandler.makeCarsToProductLink(attribute);
                        }
                    }else {
                        logger.error(Constants.MAIN_SERVICE_ERROR_UPDATE_ATTR + Constants.PRODUCT_ATTR_CAR_LIST + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + attribute.getProductCode());
                    }
                    carsFrom1C = null;
                    break;

                case Constants.PRODUCT_ATTR_OE_LIST:
                    reader = mapper.readerFor(new TypeReference<List<ProductOENumber>>() {});
                    try {
                        List<ProductOENumber> numbersFrom1C = reader.readValue(productData.get(name));
                        if(numbersFrom1C != null){
/*
                            String articleTecDocId = null;
                            if(productData.get(Constants.PRODUCT_ATTR_ARTICLE) != null && productData.get(Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID) != null){
                                //get local TecDoc article
//прописать exceptions для сервиса в случае потери доступов
                                articleTecDocId = tecDocApiService.getLocalArticle(productData.get(Constants.PRODUCT_ATTR_ARTICLE).asText(), productData.get(Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID).asText());
                            }
                            //request to TecDoc
//                           List<ProductOENumber> numbersFromTecDoc = tecDocApiService.getProductOENumbers(articleTecDocId, productData.get(Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID).asText());
                             List<ProductOENumber> numbersFromTecDoc = new ArrayList<>();

                            List<ProductOENumber> numbersNew = attrOEListHandler.findNewOENumbersFromTecDoc(numbersFromTecDoc, numbersFrom1C);
                            if(numbersNew != null && numbersNew.size() > 0){
//НЕОБХОДИМО ПЕРЕКОНВЕРТИРОВАТЬ номера поставщиков из TecDoc в 1С  но это необходимо сделать ранее перед numbersNew
                                //List<ProductOENumber> numbersUpdated = attrOEListHandler.addNewOENumbersTo1C(numbersNew, attribute.getProductCode());
                                List<ProductOENumber> numbersUpdated = null;
                                if(numbersUpdated != null){
                                    numbersFrom1C.clear();  //здесь не очищать а добавлять к старым
                                    numbersFrom1C.addAll(numbersUpdated);
                                }
                            }
 */
                            attribute.setValue(attrOEListHandler.convertOENumbersToString(numbersFrom1C));
                            //формируем таблицу oeList data для сайта
                            attrOEListHandler.makeOeListDataByProduct(numbersFrom1C, product);
                        }else {
                            logger.error(Constants.MAIN_SERVICE_ERROR_UPDATE_ATTR + Constants.PRODUCT_ATTR_OE_LIST + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + attribute.getProductCode());
                        }
                        numbersFrom1C = null;
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        logger.error(Constants.MAIN_SERVICE_ERROR_UPDATE_ATTR + Constants.PRODUCT_ATTR_OE_LIST + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + attribute.getProductCode());
                    }
                    reader = null;
                    break;


                case Constants.PRODUCT_ATTR_SPEC_LIST:
                    //from 1C
                    List<ProductCharacteristic> charactFrom1C = oneCApiService.getProductCharacteristics(attribute.getProductCode());
                    if(charactFrom1C != null){
                        //Убираем недопустимые символы из наименований
                        attrSpecListHandler.changeIncorrectSymbols(charactFrom1C);  //метод добавлен из-за значений которые уже были занесены в 1С с ошибочными символами

                        //get from tecdoc
                        if(articleTecDocId != null){
                            //request to TecDoc
                            List<ProductCharacteristic> charactFromTecDoc = null;
                            try {
                                charactFromTecDoc = tecDocApiService.getProductCharacteristics(articleTecDocId, productData.get(Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID).asText());
                            }catch (AccessException e){
                                logger.error(e.getMessage());
                            }
                            //Убираем недопустимые символы из наименований
                            attrSpecListHandler.changeIncorrectSymbols(charactFromTecDoc);
                            //Характеристики, которые нашли в TecDoc, которых нету в 1С
                            List<ProductCharacteristic> charactsNew = attrSpecListHandler.findNewCharacteristicFromTecDoc(charactFrom1C, charactFromTecDoc);
                            if(charactsNew != null && charactsNew.size() > 0){
                                //ДОБАВЛЯЕМ НОВЫЕ Х-ки в 1С
                                attrSpecListHandler.addNewCharacteristicsTo1C(charactsNew, attribute.getProductCode());
                                //Request 2 for all characteristics from current product (if add news)
                                List<ProductCharacteristic> charactsFrom1CUpdated = oneCApiService.getProductCharacteristics(attribute.getProductCode());
                                if(charactsFrom1CUpdated != null){
                                    charactFrom1C.clear();
                                    charactFrom1C.addAll(charactsFrom1CUpdated);
                                }
                            }
                        }

                        //переобновляем/сохраняем в БД
                        attrSpecListHandler.saveAllCharacteristicsToDB(charactFrom1C, attribute.getProductCode(), product);
                        //записываем в атрибут
                        attribute.setValue(attrSpecListHandler.convertCharacteristicsToString(charactFrom1C));
                    }else {
                        logger.error(Constants.MAIN_SERVICE_ERROR_UPDATE_ATTR + Constants.PRODUCT_ATTR_SPEC_LIST + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + attribute.getProductCode());
                    }
                    charactFrom1C = null;
                    break;


                case Constants.PRODUCT_ATTR_IMAGE_LIST:
                    reader = mapper.readerFor(new TypeReference<List<ProductImage>>() {});
                    try {
                        List<ProductImage> listImages = reader.readValue(productData.get(name));
                        if(listImages != null){
                            attribute.setValue(attrImageListHandler.convertImagesToString(listImages));
                        }else {
                            logger.error(Constants.MAIN_SERVICE_ERROR_UPDATE_ATTR + Constants.PRODUCT_ATTR_IMAGE_LIST + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + attribute.getProductCode());
                        }
                        listImages = null;
                    }catch (IOException e) {
                        logger.error(e.getMessage());
                        logger.error(Constants.MAIN_SERVICE_ERROR_UPDATE_ATTR + Constants.PRODUCT_ATTR_IMAGE_LIST + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + attribute.getProductCode());
                    }
                    reader = null;
                    break;

                case Constants.PRODUCT_ATTR_PRICE_FROM:
                case Constants.PRODUCT_ATTR_PRICE_TO:
                case Constants.PRODUCT_ATTR_PRICE_SALE:
                    String price = productData.get(name).asText();
                    price = attrPricesHandler.handlePrice(price);
                    attribute.setValue(price);
                    price = null;
                    break;

//ВРЕМЕННЫЕ ПРАВКИ для дублирования языкавых атрибутов
                case Constants.PRODUCT_ATTR_NAME_UA:
                    if(productData.get(name).asText().trim().length() == 0){
                        attribute.setValue(productData.get(Constants.PRODUCT_ATTR_NAME).asText());
                    }else {
                        attribute.setValue(productData.get(name).asText());
                    }
                    break;
                case Constants.PRODUCT_ATTR_CATEGORY_NAME_UA:
                    if(productData.get(name).asText().trim().length() == 0){
                        attribute.setValue(productData.get(Constants.PRODUCT_ATTR_CATEGORY_NAME).asText());
                    }else {
                        attribute.setValue(productData.get(name).asText());
                    }
                    break;
                case Constants.PRODUCT_ATTR_SUBCATEGORY_NAME_UA:
                    if(productData.get(name).asText().trim().length() == 0){
                        attribute.setValue(productData.get(Constants.PRODUCT_ATTR_SUBCATEGORY_NAME).asText());
                    }else {
                        attribute.setValue(productData.get(name).asText());
                    }
                    break;

                default:
                    attribute.setValue(productData.get(name).asText());
                    break;
            }

            //save attr
//ДОБАВЛЯЮ ТЕСТОВУЮ ПРОВЕРКУ НА ДЛИНУ до 4000 символов
            if(attribute.getValue() != null && attribute.getValue().length() < 4000){
                ProductAttribute oldAttribute = listSearchUtil.findAttrItemByNameAndProductCode(attributesOld, attribute.getName(), attribute.getProductCode());
                if(oldAttribute != null){

                    if(oldAttribute.getValue() == null || !attribute.getValue().equals(oldAttribute.getValue())){
                        //if old value - null OR if old and new values not equals
                        attribute.setId(oldAttribute.getId());
                        attributesResult.add(attribute);
                    }

                }else {
                    attributesResult.add(attribute);
                }
            }else {
                logger.error(Constants.MAIN_SERVICE_ERROR_ATTR_NOT_UPDATED + "'" + attribute.getName() + "'" + Constants.ERROR_PART_FROM_PRODUCT + "'" + attribute.getProductCode() + "'");
            }
        }

        return attributesResult;
    }

    //метод для обновления состояния всех необходимых систем перед обновлением
    private void setupEnvironment(){
        //init access
        oneCApiService.updateAccess();
        tecDocApiService.updateAccess();
        //check date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        updateTime = sdf.format(c.getTime());
        //init util
        if(listSearchUtil == null){
            listSearchUtil = new ListSearchUtil();
        }
    }

    private List<String> makeNeededAttrsList(){
        List<String> attrNames = new ArrayList<>();
        attrNames.add(Constants.PRODUCT_ATTR_ARTICLE);
        attrNames.add(Constants.PRODUCT_ATTR_NAME);
        attrNames.add(Constants.PRODUCT_ATTR_NAME_UA);
        attrNames.add(Constants.PRODUCT_ATTR_MANUFACTURER_ID);
        attrNames.add(Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID);
        attrNames.add(Constants.PRODUCT_ATTR_MANUFACTURER_NAME);
        attrNames.add(Constants.PRODUCT_ATTR_PRICE_FROM);
        attrNames.add(Constants.PRODUCT_ATTR_PRICE_TO);
        attrNames.add(Constants.PRODUCT_ATTR_AMOUNT);
        attrNames.add(Constants.PRODUCT_ATTR_SALE);
        attrNames.add(Constants.PRODUCT_ATTR_PRICE_SALE);
        attrNames.add(Constants.PRODUCT_ATTR_CATEGORY_ID);
        attrNames.add(Constants.PRODUCT_ATTR_CATEGORY_NAME);
        attrNames.add(Constants.PRODUCT_ATTR_CATEGORY_NAME_UA);
        attrNames.add(Constants.PRODUCT_ATTR_SUBCATEGORY_ID);
        attrNames.add(Constants.PRODUCT_ATTR_SUBCATEGORY_NAME);
        attrNames.add(Constants.PRODUCT_ATTR_SUBCATEGORY_NAME_UA);
        attrNames.add(Constants.PRODUCT_ATTR_IDENTIFIER);
        attrNames.add(Constants.PRODUCT_ATTR_FEATURED);
        attrNames.add(Constants.PRODUCT_ATTR_DESCRIPTION);
        attrNames.add(Constants.PRODUCT_ATTR_DESCRIPTION_UA);
        //lists
        attrNames.add(Constants.PRODUCT_ATTR_IMAGE_LIST);
        attrNames.add(Constants.PRODUCT_ATTR_ANALOG_LIST);
        attrNames.add(Constants.PRODUCT_ATTR_OE_LIST);
        attrNames.add(Constants.PRODUCT_ATTR_SPEC_LIST);
        attrNames.add(Constants.PRODUCT_ATTR_CAR_LIST);
        //SEO
        attrNames.add(Constants.PRODUCT_ATTR_H1);
        attrNames.add(Constants.PRODUCT_ATTR_H1_UA);
        attrNames.add(Constants.PRODUCT_ATTR_H2);
        attrNames.add(Constants.PRODUCT_ATTR_H2_UA);
        attrNames.add(Constants.PRODUCT_ATTR_H3);
        attrNames.add(Constants.PRODUCT_ATTR_H3_UA);
        attrNames.add(Constants.PRODUCT_ATTR_H4);
        attrNames.add(Constants.PRODUCT_ATTR_H4_UA);
        attrNames.add(Constants.PRODUCT_ATTR_H5);
        attrNames.add(Constants.PRODUCT_ATTR_H5_UA);
        attrNames.add(Constants.PRODUCT_ATTR_H6);
        attrNames.add(Constants.PRODUCT_ATTR_H6_UA);
        attrNames.add(Constants.PRODUCT_ATTR_SEO_TITLE);
        attrNames.add(Constants.PRODUCT_ATTR_SEO_TITLE_UA);
        attrNames.add(Constants.PRODUCT_ATTR_SEO_DESCRIPTION);
        attrNames.add(Constants.PRODUCT_ATTR_SEO_DESCRIPTION_UA);
        attrNames.add(Constants.PRODUCT_ATTR_SEO_URL);
        return attrNames;
    }

    private int getLimit(){
        SettingModel settingLimit = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_LIMIT_PRODUCTS);
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
