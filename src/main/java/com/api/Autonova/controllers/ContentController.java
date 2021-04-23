package com.api.Autonova.controllers;

import com.api.Autonova.components.cache.CacheUpdateComponent;
import com.api.Autonova.dao.ProductsSiteDao;
import com.api.Autonova.exceptions.AccessException;
import com.api.Autonova.exceptions.BadRequestException;
import com.api.Autonova.exceptions.NotFoundException;
import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.*;
import com.api.Autonova.models.site.*;
import com.api.Autonova.repository.*;
import com.api.Autonova.utils.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping(value = "/api")
public class ContentController {

    @Autowired
    ProductsRepository productsRepository;

    @Autowired
    ProductAttributesRepository productAttributesRepository;

    @Autowired
    ProductCharacteristicsRepository productCharacteristicsRepository;

    @Autowired
    CarsRepository carsRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    PageDescriptionRepository pageDescriptionRepositorytory;

    @Autowired
    PageImageRepository pageImageRepository;

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    AttributesPatternsRepository attributesPatternsRepository;

    @Autowired
    OENumbersDataRepository oeNumbersDataRepository;

    @Autowired
    PartnersRepository partnersRepository;


    @Autowired
    CarsProductRepository carsProductRepository;

    @Autowired
    ProductsSiteDao productsSiteDao;


    @RequestMapping(value = "/testS", method = RequestMethod.GET)
    public List<ProductAttribute> testFindByCode(){
        List<ProductAttribute> attributes = productAttributesRepository.findAllByProductCode("C2536_59");
        System.out.println("ID - " + attributes.get(1).getProduct().getId());
        return attributes;
    }



    @Autowired
    CacheUpdateComponent cacheUpdateComponent;

    @RequestMapping(value = "/testCache", method = RequestMethod.GET)
    public  void testCache() {
        cacheUpdateComponent.clearProductsDataCache();
        cacheUpdateComponent.clearCarsDataCache();
    }








//PRODUCTS
    @RequestMapping(value = "/searchProducts", method = RequestMethod.GET)
    public ResponceSearchProducts searchProducts(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                                 @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "5") int limit){

        List<Product> productsByArticle = productsSiteDao.findProductForSearch(language, Constants.PRODUCT_ATTR_ARTICLE, query, 0 , limit - 1);

        List<Product> productsByName = null;

        if(language.equals(Constants.LANGUAGE_RU)){
            productsByName = productsSiteDao.findProductForSearch(language, Constants.PRODUCT_ATTR_NAME, query, 0 , limit - 1);
        }else {
            productsByName = productsSiteDao.findProductForSearch(language, Constants.PRODUCT_ATTR_NAME_UA, query, 0 , limit - 1);
        }

        ListSearchUtil searchUtil = new ListSearchUtil();

        List<ProductInList> productsResultByArticle = new ArrayList<>();
        List<ProductInList> productsResultByName = new ArrayList<>();

        if(productsByName != null){
            for (Product productItem : productsByName) {
                productsResultByName.add(makeProductForSearch(language, searchUtil, productItem));
            }
        }

        if(productsByArticle != null){
            for(Product productItem : productsByArticle) {
                productsResultByArticle.add(makeProductForSearch(language, searchUtil, productItem));
            }
        }

        ResponceSearchProducts result = new ResponceSearchProducts();
        result.setResults_by_code(productsResultByArticle);
        result.setResults_by_name(productsResultByName);
        return result;
    }
    private ProductInList makeProductForSearch(String language, ListSearchUtil searchUtil, Product productItem){
        ProductInList productInList = new ProductInList();
        productInList.setCode(productItem.getCode());
        List<ProductAttribute> attributes = productItem.getAttributes();
        if(language.equals(Constants.LANGUAGE_RU)){
            productInList.setName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_NAME));
        }else {
            productInList.setName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_NAME_UA));
        }
        productInList.setSeoUrl(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SEO_URL));
        productInList.setArticle(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_ARTICLE));
        return productInList;
    }




    //for filters
    @Cacheable(value = "attributes", key="{#language, #filterModel, #query}")
    @RequestMapping(value = "/getAvailiableAttributeVariants", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AttributeWithValues> getAllAttrVariables(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                                         @RequestParam(defaultValue = "") String model_name, @RequestParam(defaultValue = "") String manu_name,
                                                         @RequestParam(defaultValue = "") String type_name, @RequestParam(defaultValue = "") String year,
                                                         @RequestBody ProductFilterModel filterModel,
                                                         @RequestParam(defaultValue = "") String query){

        //List<AttributesPattern> patternFilterList = (List<AttributesPattern>) attributesPatternsRepository.findAll();
        List<AttributesPattern> patternFilterList = (List<AttributesPattern>) attributesPatternsRepository.findAllByFilter(true);

        //make variant names
        List<String> attrNames = new ArrayList<>();
        if(patternFilterList != null){
            for(AttributesPattern attributesPatternItem : patternFilterList){

                if(language.equals(Constants.LANGUAGE_RU)){
                    //gen lang attr AND IGNORE PRICES
                    if(!attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_NAME_UA) && !attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_CATEGORY_NAME_UA) &&
                            !attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_SUBCATEGORY_NAME_UA) &&
                            !attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_FROM) && !attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_TO)){
                        attrNames.add(attributesPatternItem.getName());
                    }
                }else {
                    if(!attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_NAME) && !attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_CATEGORY_NAME) &&
                            !attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_SUBCATEGORY_NAME) &&
                            !attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_FROM) && !attributesPatternItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_TO)){
                        attrNames.add(attributesPatternItem.getName());
                    }
                }
            }
        }


//custom params
        String priceFromFilter = "0";
        String priceToFilter = "0";
        if(filterModel != null && filterModel.getAttributes() != null){
            for(ProductAttributeFilter attributeItem : filterModel.getAttributes()){
                if(attributeItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_FROM) && attributeItem.getValues() != null && attributeItem.getValues().size() > 0){
                    priceFromFilter = attributeItem.getValues().get(0);
                }
                if(attributeItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_TO) && attributeItem.getValues() != null && attributeItem.getValues().size() > 0){
                    priceToFilter = attributeItem.getValues().get(0);
                }
            }
        }

        //List<String> codes = getProductForFilter(language, query, filterModel).get("codesForFilter");
        //List<String> codes = productsDAO.findAllCodesByFilter(language, filterModel, false);
        List<Integer> ids = productsSiteDao.findAllIdsByFilter(language, filterModel, query, false);
        //List<String> codesPriceIgnore = getProductForFilter(language, query, filterModel).get("codesForFilterPriceIgnore");
        //List<String> codesPriceIgnore = productsDAO.findAllCodesByFilter(language, filterModel, true);
        List<Integer> idsPriceIgnore = productsSiteDao.findAllIdsByFilter(language, filterModel, query, true);

        List<AttributeWithValues> pricesVariant = getPricesForFilter(language, idsPriceIgnore, priceFromFilter, priceToFilter);



        List<ProductAttributeFilterGetting> filtersGetting = productAttributesRepository.getAttrsVariantsByNameAndIds(attrNames.isEmpty() ? null : attrNames, ids.isEmpty() ? null : ids);

        List<AttributeWithValues> attributeVariants = new ArrayList<>();
        ListSearchUtil searchUtil = new ListSearchUtil();
        if(patternFilterList != null){
            for(AttributesPattern attributesPatternItem : patternFilterList){
                AttributeWithValues attributeWithValues = new AttributeWithValues();
                attributeWithValues.setName(attributesPatternItem.getName());
                if(language.equals(Constants.LANGUAGE_RU)){
                    attributeWithValues.setTitle(attributesPatternItem.getTitle());
                }else {
                    attributeWithValues.setTitle(attributesPatternItem.getTitleUa());
                }
                List<String> variables = searchUtil.cutAttsValuesByName(filtersGetting, attributesPatternItem.getName());
                switch (attributesPatternItem.getName()) {

                    case Constants.PRODUCT_ATTR_AMOUNT:
                        //УНИКАЛЬНЫЙ МОМЕНТ ДЛЯ НАЛИЧИЯ (переводим значения чисельные в булевые)
                        if (variables != null && variables.size() > 0) {
                            List<Boolean> variablesBoolean = new ArrayList<>();
                            for (String variableItem : variables) {
                                try {
                                    int itemInt = Integer.parseInt(variableItem);
                                    if (itemInt > 0) {
                                        variablesBoolean.add(true);
                                    } else {
                                        variablesBoolean.add(false);
                                    }
                                } catch (NumberFormatException e) {
                                }
                            }
                            //check all boolean list
                            boolean amountTrue = false;
                            boolean amountFalse = false;
                            for (Boolean item : variablesBoolean) {
                                if (item) {
                                    amountTrue = true;
                                } else {
                                    amountFalse = true;
                                }
                            }
                            //make true \ false result
                            List<String> variablesNew = new ArrayList<>();
                            if (amountTrue) {
                                variablesNew.add("true");
                            }
                            if (amountFalse) {
                                variablesNew.add("false");
                            }

//sort USE UTIL
                            Comparator<String> cmp = new Comparator<String>() {
                                public int compare(String o1, String o2) {
                                    return o1.compareTo(o2);
                                }
                            };
                            variablesNew.sort(cmp);

                            attributeWithValues.setValues(variablesNew);
                            attributeVariants.add(attributeWithValues);
                        }
                        break;

                    case Constants.PRODUCT_ATTR_PRICE_FROM:
                    case Constants.PRODUCT_ATTR_PRICE_TO:
                        //вынес этот блок в отдельный метод - цены не фильтруем
                        break;

                    default:
                        if (variables != null && variables.size() > 0) {
//dell all empty
                            Iterator<String> it = variables.iterator();
                            while (it.hasNext()) {
                                String varItem = it.next();
                                if (varItem.trim().length() == 0) {
                                    it.remove();
                                }
                            }

//sort USE UTIL
                            Comparator<String> cmp = new Comparator<String>() {
                                public int compare(String o1, String o2) {
                                    return o1.compareTo(o2);
                                }
                            };
                            variables.sort(cmp);

                            if (variables.size() > 0) {
                                attributeWithValues.setValues(variables);
                                attributeVariants.add(attributeWithValues);
                            }
                        }
                        break;
                }

            }
        }
        //add prices  - добавляем отдельно фильтры цен ибо они не должны фильтроваться самими собой
        attributeVariants.addAll(pricesVariant);

        return attributeVariants;
    }

    //for characteristics filter
    @Cacheable(value = "characteristics", key="{#language, #filterModel, #query}")
    @RequestMapping(value = "/getAvailiableCharacteristicVariants", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AttributeWithValues> getAllCharVariables(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                                         @RequestParam(defaultValue = "") String model_name, @RequestParam(defaultValue = "") String manu_name,
                                                         @RequestParam(defaultValue = "") String type_name, @RequestParam(defaultValue = "") String year,
                                                         @RequestBody ProductFilterModel filterModel,
                                                         @RequestParam(defaultValue = "") String query){


        //List<String> codes = getProductForFilter(language, query, filterModel).get("codesForFilter");
        //List<String> codes = productsDAO.findAllCodesByFilter(language, filterModel, false);
        List<Integer> ids = productsSiteDao.findAllIdsByFilter(language, filterModel, query, false);


        List<String> characteristicsNames = new ArrayList<>();
        List<ProductAttributeFilterGetting> filtersGetting = null;
        if(language.equals(Constants.LANGUAGE_RU)){
            characteristicsNames = productCharacteristicsRepository.getUniqueCharacteristicsByAttrShortName();
            filtersGetting = productCharacteristicsRepository.getCharacteristicVariantsInIds(ids.size() > 0 ? ids : null);
        }else {
            characteristicsNames = productCharacteristicsRepository.getUniqueCharacteristicsByAttrShortNameUA();
            filtersGetting = productCharacteristicsRepository.getCharacteristicVariantsInIdsUa(ids.size() > 0 ? ids : null);
        }

        List<AttributeWithValues> characteristicsVariants = new ArrayList<>();
        ListSearchUtil searchUtil = new ListSearchUtil();
        for(String characteristicItem : characteristicsNames){

            AttributeWithValues characteristicVariant = new AttributeWithValues();
            characteristicVariant.setName(characteristicItem);
            characteristicVariant.setTitle(characteristicItem);
            List<String> variables = searchUtil.cutAttsValuesByName(filtersGetting, characteristicItem);

            if(variables != null && variables.size() > 0){
//sort USE UTIL
                Comparator<String> cmp = new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                };
                variables.sort(cmp);
                characteristicVariant.setValues(variables);
                characteristicsVariants.add(characteristicVariant);
            }
        }

        return characteristicsVariants;
    }

    @Cacheable(value = "cars", key="{#language, #filterModel, #query}")
    @RequestMapping(value = "/getAvailiableCarDataVariants", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponceAvailiableCarDataVariants getCarsDataVariables(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                                         @RequestBody ProductFilterModel filterModel, @RequestParam(defaultValue = "") String query){
        ResponceAvailiableCarDataVariants result = new ResponceAvailiableCarDataVariants();

        List<Integer> ids = productsSiteDao.findAllIdsByFilter(language, filterModel, query, false);
        List<Integer> carsIds = carsProductRepository.getCarsByIds(ids.size() > 0 ? ids : null);
        List<Car> cars = carsRepository.findCarsDataByIds(carsIds.size() > 0 ? carsIds : null);

        String filterManuName = null;
        String filterModelName = null;
        String filterTypeName = null;
        String filterYear = null;

//фильтровать сами данные авто можно только по первым значениям массива фильтров
        if(filterModel != null && filterModel.getAttributes() != null && filterModel.getAttributes().size() > 0){
            for(ProductAttributeFilter filterItem : filterModel.getAttributes()){
                if(filterItem.getName().equals("manu_name") && filterItem.getValues().size() > 0){
                    filterManuName = filterItem.getValues().get(0);
                }else if(filterItem.getName().equals("model_name") && filterItem.getValues().size() > 0){
                    filterModelName = filterItem.getValues().get(0);
                }else if(filterItem.getName().equals("type_name") && filterItem.getValues().size() > 0){
                    filterTypeName = filterItem.getValues().get(0);
                }else if(filterItem.getName().equals("year") && filterItem.getValues().size() > 0){
                    filterYear = filterItem.getValues().get(0);
                }
            }
            //filter
            Iterator<Car> it2 = cars.iterator();
            while (it2.hasNext()) {
                Car item = it2.next();
                if(filterManuName != null && !FilterUtil.searchName(item.getManuName(), filterManuName.trim())){
                    it2.remove();
                }else if(filterModelName != null && !FilterUtil.searchName(item.getModelName(), filterModelName.trim())){
                    it2.remove();
                }else if(filterTypeName != null && !FilterUtil.searchName(item.getTypeName(), filterTypeName.trim())){
                    it2.remove();
                }else if(filterYear != null){
                    try{
                        int yearFilter = Integer.parseInt(filterYear.trim());

                        int yearCarFrom = 0;
                        if(item.getYearOfConstrFrom().length() >= 4){
                            yearCarFrom = Integer.parseInt(item.getYearOfConstrFrom().substring(0, 4));
                        }else {
                            yearCarFrom = Integer.parseInt(item.getYearOfConstrFrom());
                        }
                        int yearCarTo = 0;
                        if(item.getYearOfConstrTo().length() >= 4){
                            yearCarTo = Integer.parseInt(item.getYearOfConstrTo().substring(0, 4));
                        }else {
                            yearCarTo = Integer.parseInt(item.getYearOfConstrTo());
                        }

                        if(yearFilter < yearCarFrom || yearFilter > yearCarTo){
                            it2.remove();
                        }

                    }catch (NumberFormatException | NullPointerException e){ }
                }
            }
        }


        Set<String> manuNames = new HashSet<String>();
        Set<String> modelNames = new HashSet<String>();
        Set<String> typeNames = new HashSet<String>();
        Set<Integer> years = new HashSet<Integer>();

        for (Car carItem: cars){
            manuNames.add(carItem.getManuName());
            modelNames.add(carItem.getModelName());
            typeNames.add(carItem.getTypeName());

            try {
                String yearFrom = carItem.getYearOfConstrFrom().length() >= 4 ? carItem.getYearOfConstrFrom().substring(0, 4) : null;
                String yearTo = carItem.getYearOfConstrTo().length() >= 4 ? carItem.getYearOfConstrTo().substring(0, 4) : null;
                years.add(Integer.parseInt(yearFrom));
                years.add(Integer.parseInt(yearTo));
            }catch (NumberFormatException | NullPointerException e){}
        }


        Comparator<String> cmp = new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };
        Comparator<Integer> cmp2 = new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };

        List<String> manuNamesList = new ArrayList<>(manuNames);
        manuNamesList.sort(cmp);
        List<String> modelNamesList = new ArrayList<>(modelNames);
        modelNamesList.sort(cmp);
        List<String> typeNamesList = new ArrayList<>(typeNames);
        typeNamesList.sort(cmp);
        List<Integer> yearsList = new ArrayList<>(years);
        yearsList.sort(cmp2);

        //set data
        result.setManu_name(manuNamesList);
        //result.setManu_name(carsRepository.findDistinctManuNamesByIds(carsIds));
        result.setModel_name(modelNamesList);
        //result.setModel_name(carsRepository.findDistinctModelNamesByIds(carsIds));
        result.setType_name(typeNamesList);
        //result.setType_name(carsRepository.findDistinctTypeNamesByIds(carsIds));
        result.setYear(yearsList);

        return result;
    }

    //возврат минимального и максимального значения из всех цен
    @RequestMapping(value = "/getAvailiablePriceVariants", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAvailiablePriceVariants(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language) throws JSONException {

        List<JSONObject> result = new ArrayList<>();

        List<String> codes = new ArrayList<>();
        //List<Product> products = (List<Product>) productsRepository.findAll();
        List<Product> products = null;
        List<ProductAttribute> attributesAllPriceFrom = (List<ProductAttribute>) productAttributesRepository.findAllByName(Constants.PRODUCT_ATTR_PRICE_FROM);
        List<ProductAttribute> attributesAllPriceTo = (List<ProductAttribute>) productAttributesRepository.findAllByName(Constants.PRODUCT_ATTR_PRICE_TO);
        List<ProductAttribute> attributesAll = new ArrayList<>();
        if(attributesAllPriceFrom != null && attributesAllPriceFrom.size() > 0){
            attributesAll.addAll(attributesAllPriceFrom);
        }
        if(attributesAllPriceTo != null && attributesAllPriceTo.size() > 0){
            attributesAll.addAll(attributesAllPriceTo);
        }
        ListSearchUtil searchUtil = new ListSearchUtil();
        for (Product productItem : products) {
            List<ProductAttribute> attributes = searchUtil.cutAttsByProductCode(attributesAll, productItem.getCode());
            if (attributes != null && attributes.size() > 0) {
                codes.add(productItem.getCode());
            }
        }

        //get variables
        List<String> attributeNames = new ArrayList<>();
        attributeNames.add(Constants.PRODUCT_ATTR_PRICE_FROM);
        attributeNames.add(Constants.PRODUCT_ATTR_PRICE_TO);

        for(String attrName : attributeNames){
            List<String> variables = productAttributesRepository.findDistinctAttrValues(attrName, codes.isEmpty() ? null : codes);
            if(variables != null && variables.size() > 0){
                //dell all empty
                Iterator<String> it = variables.iterator();
                while (it.hasNext()) {
                    String varItem = it.next();
                    if(varItem.trim().length() == 0){
                        it.remove();
                    }
                }
                if(variables.size() > 0){
                    Integer topPrice = null;  //top - minimum or maximum
                    try {
                        topPrice = (int) Double.parseDouble(variables.get(0));
                    }catch (NumberFormatException | NullPointerException e){}

                    for(String variableItem : variables){
                        try {
                            int checkValue = (int) Double.parseDouble(variableItem);
                            if(topPrice != null){

                                if(attrName.equals(Constants.PRODUCT_ATTR_PRICE_FROM)){
                                    //ищем минимальное
                                    if(checkValue < topPrice){
                                        topPrice = checkValue;
                                    }
                                }else {
                                    //ищем максимальное
                                    if(checkValue > topPrice){
                                        topPrice = checkValue;
                                    }
                                }
                            }else {
                                topPrice = checkValue;
                            }
                        }catch (NumberFormatException | NullPointerException e){}
                    }

                    if(topPrice != null){
                        variables.clear();
                        variables.add(String.valueOf(topPrice));
                    }else {
                        variables.clear();
                        variables.add("0");
                    }

                    JSONObject data = new JSONObject();
                    data.put("name", attrName);
                    JSONArray array = new JSONArray();
                    for(String s : variables){
                        array.put(s);
                    }
                    data.put("values", array);
                    result.add(data);
                }
            }
        }
        return result.toString();
    }

    @RequestMapping(value = "/searchCategories", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public String searchCategories(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                   @RequestParam(defaultValue = "") String query,
                                   @RequestParam(defaultValue = "5") int limit) throws JSONException {

        List<JSONObject> result = new ArrayList<>();
        List<String> variables = null;
        if(language.equals(Constants.LANGUAGE_RU)){
            variables = productAttributesRepository.findDistinctAttrValuesInAll(Constants.PRODUCT_ATTR_CATEGORY_NAME);
        }else {
            variables = productAttributesRepository.findDistinctAttrValuesInAll(Constants.PRODUCT_ATTR_CATEGORY_NAME_UA);
        }
        if(variables != null && variables.size() > 0){
            //dell all empty
            Iterator<String> it = variables.iterator();
            while (it.hasNext()) {
                String varItem = it.next();
                if(varItem.trim().length() == 0){
                    it.remove();
                }
            }
            //filter query
            if(query.trim().length() > 0){
                Iterator<String> it2 = variables.iterator();
                while (it2.hasNext()) {
                    String item = it2.next();
                    if (!FilterUtil.searchName(item, query.trim())) {
                        it2.remove();
                    }
                }
            }
            /*
//sort USE UTIL
            Comparator<String> cmp = new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            };
            variables.sort(cmp);*/

            //paginations
            variables = variables.subList(0, Math.min(variables.size(), limit));

            //return
            for (String item: variables){
                result.add(new JSONObject()
                        .put("category", item)
                );
            }
        }
        return result.toString();
    }

    @RequestMapping(value = "/searchSubcategories", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public String searchSubcategories(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                   @RequestParam(defaultValue = "") String query,
                                   @RequestParam(defaultValue = "") String category,
                                   @RequestParam(defaultValue = "5") int limit) throws JSONException {

        List<JSONObject> result = new ArrayList<>();



        List<String> codesForFilter = new ArrayList<>();

        //запрос не учитывает товары без атрибутов
        //List<Product> products = (List<Product>) productsRepository.findAll();

        //get all attrs
        List<ProductAttribute> attributes = null;
        if(language.equals(Constants.LANGUAGE_RU)){
            attributes = (List<ProductAttribute>) productAttributesRepository.findAllByName(Constants.PRODUCT_ATTR_CATEGORY_NAME);
        }else {
            attributes = (List<ProductAttribute>) productAttributesRepository.findAllByName(Constants.PRODUCT_ATTR_CATEGORY_NAME_UA);
        }

        if(attributes != null){
            for(ProductAttribute attributeItem : attributes){
                if (attributeItem.getValue() != null && FilterUtil.searchName(attributeItem.getValue(), category.trim())) {
                    codesForFilter.add(attributeItem.getProductCode());
                }
            }
        }

        List<String> variables = null;
        if(language.equals(Constants.LANGUAGE_RU)){
            variables = productAttributesRepository.findDistinctAttrValues(Constants.PRODUCT_ATTR_SUBCATEGORY_NAME, codesForFilter.isEmpty() ? null : codesForFilter);
        }else {
            variables = productAttributesRepository.findDistinctAttrValues(Constants.PRODUCT_ATTR_SUBCATEGORY_NAME_UA, codesForFilter.isEmpty() ? null : codesForFilter);
        }
        if(variables != null && variables.size() > 0){
            //dell all empty
            Iterator<String> it = variables.iterator();
            while (it.hasNext()) {
                String varItem = it.next();
                if(varItem.trim().length() == 0){
                    it.remove();
                }
            }
            //filter
            if(query.trim().length() > 0){
                Iterator<String> it2 = variables.iterator();
                while (it2.hasNext()) {
                    String item = it2.next();
                    if (!FilterUtil.searchName(item, query.trim())) {
                        it2.remove();
                    }
                }
            }
//sort USE UTIL
            Comparator<String> cmp = new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            };
            variables.sort(cmp);

            //paginations
            variables = variables.subList(0, Math.min(variables.size(), limit));

            //return
            for (String item: variables){
                result.add(new JSONObject()
                        .put("subcategory", item)
                );
            }
        }
        return result.toString();
    }


    @Cacheable(value = "products", key="{#filterModel, #language, #page, #limit, #order, #sort, #query}")
    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ResponceProductsSite  getProducts(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int limit,
                                            @RequestParam(defaultValue = Constants.PRODUCT_ATTR_FEATURED) String order, @RequestParam(defaultValue = Constants.SORT_VECTOR_DESC) String sort,
                                            @RequestParam(defaultValue = "") String query,
                                            @RequestBody ProductFilterModel filterModel) {

        List<Product> products = productsSiteDao.findProductsByFilter(language, filterModel, query,  sort, order, limit * Math.max(page-1, 0), limit);

        int queryCount = productsSiteDao.findAllIdsByFilter(language, filterModel, query, false).size();

        SettingModel baseUrl = settingRepository.findSettingModelByName(Constants.SETTING_BASE_PRODUCT_LINK);
        SettingModel secondaryUrlParams = settingRepository.findSettingModelByName(Constants.SETTING_PRODUCT_LINK_SECONDARY_PARAMS);
        ListSearchUtil searchUtil = new ListSearchUtil();

        if(products != null){

            List<ProductForSite> productsFind = new ArrayList<>();
            for(Product productItem : products){
                List<ProductAttribute> attributes = productItem.getAttributes();
                if(attributes != null){
                    productsFind.add(makeProductFromAttrs(language, productItem.getCode(), attributes, baseUrl, secondaryUrlParams,  searchUtil));
                }
            }

            PaginationModel paginationModel = makePaginationModel(queryCount, page, limit, order, sort);

            ResponceProductsSite responceProductsSite = new ResponceProductsSite();
            responceProductsSite.setProducts(productsFind);
            responceProductsSite.setPagination(paginationModel);

            return responceProductsSite;

        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    //get b SEO if SEO = null, get by code
    @RequestMapping(value = "/products/{code}", method = RequestMethod.GET)
    public ProductAllData  getProductData(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                          @PathVariable(name = "code") String code) {
        try {
            return getProductDataByCode(language, code);
        }catch (NotFoundException e){
            return getProductDataBySEOUrl(language, code);
        }
    }

    @RequestMapping(value = "/products/code/{code}", method = RequestMethod.GET)
    public ProductAllData  getProductDataByCode(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                          @PathVariable(name = "code") String code) {

        Product product = productsRepository.findProductByCode(code);
        if(product != null){

            ListSearchUtil searchUtil = new ListSearchUtil();
            //List<Car> allCars = (List<Car>) carsRepository.findAll();
            SettingModel baseUrl = settingRepository.findSettingModelByName(Constants.SETTING_BASE_PRODUCT_LINK);
            SettingModel secondaryUrlParams = settingRepository.findSettingModelByName(Constants.SETTING_PRODUCT_LINK_SECONDARY_PARAMS);
            ProductAllData productAllData = new ProductAllData();
            productAllData.setCode(product.getCode());
            //List<ProductAttribute> attributes = productAttributesRepository.findAllByProductCode(code);
            List<ProductAttribute> attributes = product.getAttributes();
            if(attributes != null){
                if(language.equals(Constants.LANGUAGE_RU)){
                    productAllData.setName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_NAME));
                    productAllData.setCategoryName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_CATEGORY_NAME));
                    productAllData.setSubcategoryName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SUBCATEGORY_NAME));
                    productAllData.setDescription(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_DESCRIPTION));
                    //seo
                    productAllData.setSeoDescription(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SEO_TITLE));
                    productAllData.setSeoDescription(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SEO_DESCRIPTION));
                    productAllData.setSeoH1(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H1));
                    productAllData.setSeoH2(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H2));
                    productAllData.setSeoH3(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H3));
                    productAllData.setSeoH4(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H4));
                    productAllData.setSeoH5(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H5));
                    productAllData.setSeoH6(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H6));
                }else {
                    productAllData.setName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_NAME_UA));
                    productAllData.setCategoryName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_CATEGORY_NAME_UA));
                    productAllData.setSubcategoryName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SUBCATEGORY_NAME_UA));
                    productAllData.setDescription(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_DESCRIPTION_UA));
                    //seo
                    productAllData.setSeoDescription(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SEO_TITLE_UA));
                    productAllData.setSeoDescription(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SEO_DESCRIPTION_UA));
                    productAllData.setSeoH1(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H1_UA));
                    productAllData.setSeoH2(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H2_UA));
                    productAllData.setSeoH3(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H3_UA));
                    productAllData.setSeoH4(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H4_UA));
                    productAllData.setSeoH5(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H5_UA));
                    productAllData.setSeoH6(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_H6_UA));
                }
                productAllData.setArticle(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_ARTICLE));
                productAllData.setManufacturerName(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_MANUFACTURER_NAME));
                productAllData.setSeoUrl(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SEO_URL));
                productAllData.setPriceFrom(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_PRICE_FROM));
                productAllData.setPriceTo(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_PRICE_TO));
                productAllData.setAmount(searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_AMOUNT));

                productAllData.setCarList(mareCarsForSite(language, new ArrayList<Car>(product.getCars())));

                //image amd imageList
                String imagesStr = searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_IMAGE_LIST);
                List<String> imageList = AttributeParseUtil.parseImagesListAttrFromDB(imagesStr);

                //image
                if(imageList.size() > 0){
                    productAllData.setImage(imageList.get(0));
                }
                //image list
                productAllData.setImageList(imageList);

                //analog list
                String analogsStr = searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_ANALOG_LIST);
                List<String> analogsList = AttributeParseUtil.parseListAttrFromDB(analogsStr);
                List<Integer> analogIds = productsRepository.findAllProductsIdByCodes(analogsList.size() > 0 ? analogsList : null);
                List<ProductAttribute> allAnalogAttrs = productAttributesRepository.findAllInIds(analogIds.size() > 0 ? analogIds : null);
                //make
                List<ProductForSite> analogs = new ArrayList<>();
                for(Integer analogId : analogIds){
                    List<ProductAttribute> productAnalogAttrs = searchUtil.cutAttsByProductId(allAnalogAttrs, analogId);
                    if(productAnalogAttrs != null && productAnalogAttrs.size() > 0){
                        ProductForSite productAnalog = new ProductForSite();
                        productAnalog.setCode(productAnalogAttrs.get(0).getProduct().getCode());
                        productAnalog = makeProductFromAttrs(language, productAnalogAttrs.get(0).getProduct().getCode(), productAnalogAttrs, baseUrl, secondaryUrlParams, searchUtil);
                        analogs.add(productAnalog);
                    }
                }
                productAllData.setAnalogList(analogs);

                //oe list
                List<OENumberData> oeNumbers = oeNumbersDataRepository.findAllByParentId(product.getId());
                productAllData.setOeList(oeNumbers);

                //characteristics
                //List<ProductCharacteristic> characteristics = productCharacteristicsRepository.findAllByProductCode(code);
                if(product.getCharacteristics() != null){
                    List<ProductCharacteristicSite> productCharacteristicSite = new ArrayList<>();
                    for(ProductCharacteristic characteristicItemDB : product.getCharacteristics()){
                        ProductCharacteristicSite characteristicSite = new ProductCharacteristicSite();
                        if(language.equals(Constants.LANGUAGE_RU)){
                            characteristicSite.setAttrName(characteristicItemDB.getAttrName());
                            characteristicSite.setAttrShortName(characteristicItemDB.getAttrShortName());
                            characteristicSite.setAttrValue(characteristicItemDB.getAttrValue());
                            characteristicSite.setAttrUnit(characteristicItemDB.getAttrUnit());
                        }else {
                            characteristicSite.setAttrName(characteristicItemDB.getAttrNameUa());
                            characteristicSite.setAttrShortName(characteristicItemDB.getAttrShortNameUa());
                            characteristicSite.setAttrValue(characteristicItemDB.getAttrValueUa());
                            characteristicSite.setAttrUnit(characteristicItemDB.getAttrUnit());
                        }
                        productCharacteristicSite.add(characteristicSite);
                    }
                    productAllData.setCharacteristicsList(productCharacteristicSite);
                }
                //set link
                if(baseUrl != null){
                    String brandId = searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID);
                   productAllData.setLink(GenerateDataUtil.generateProductLink(language, baseUrl.getValue(), productAllData.getCode(), brandId, secondaryUrlParams, attributes, searchUtil));
                }
            }
            return productAllData;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/products/seo/{seo_url}", method = RequestMethod.GET)
    public ProductAllData  getProductDataBySEOUrl(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                                  @PathVariable(name = "seo_url") String seo_url) {
        ProductAttribute productAttribute = productAttributesRepository.findFirstByNameAndValue(Constants.PRODUCT_ATTR_SEO_URL, seo_url);
        if(productAttribute != null){
            return getProductDataByCode(language, productAttribute.getProductCode());
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/products/{code}/cars", method = RequestMethod.GET)
    public List<CarForSite> getProductDataCars(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                          @PathVariable(name = "code") String code, @RequestParam(defaultValue = "") String manuName) {
        Product product = productsRepository.findProductByCode(code);
        if(product != null){
            return mareCarsForSite(language, productsRepository.findAllCarsByProductAndManu(product.getId(), manuName));
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/searchMarksByProduct", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> searchMarksTest(@RequestParam(defaultValue = "") String code) {
        return carsRepository.getDistinctManuNamesByProduct(code);
    }

//CARS - OLD METHODS
    @RequestMapping(value = "/searchMarks", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public String searchMarks(@RequestParam(defaultValue = "") String query,
                              @RequestParam(defaultValue = "") String model,
                              @RequestParam(defaultValue = "") String year,
                              @RequestParam(defaultValue = "") String modification,
                              @RequestParam(defaultValue = "5") int limit) throws JSONException {
        List<JSONObject> result = new ArrayList<>();
        List<String> resultList = new ArrayList<>();
        List<String> manuNames = carsRepository.findDistinctManuNames();
        List<Car> allCars = (List<Car>) carsRepository.findAll();
        if(manuNames != null){

//clear empty values
            Iterator<String> it = manuNames.iterator();
            while (it.hasNext()) {
                String nameItem = it.next();
                if (nameItem.trim().length() == 0) {
                    it.remove();
                }
            }

            //filters
            if(query.trim().length() > 0){
                Iterator<String> it2 = manuNames.iterator();
                while (it2.hasNext()) {
                    String nameItem = it2.next();
                    if (nameItem == null || !FilterUtil.searchName(nameItem, query.trim())) {
                        it2.remove();
                    }
                }
            }
            ListSearchUtil searchUtil = new ListSearchUtil();
            for (String manuItem : manuNames){
                //List<Car> carsByManu = carsRepository.findAllByManuName(manuItem);
                List<Car> carsByManu = searchUtil.findCarByManuName(allCars, manuItem);
                boolean passManu = false;
                if(carsByManu != null){
                    List<Boolean> carsPass = new ArrayList<>();
                    for(Car carItem : carsByManu){
                        boolean passCar = true;
                        if(model.trim().length() > 0){
                            if (carItem == null || !FilterUtil.searchName(carItem.getModelName(), model.trim())) {
                                passCar = false;
                            }
                        }
                        if(year.trim().length() > 0){
                            try{
                                int yearFilter = Integer.parseInt(year.trim());

                                int yearCarFrom = 0;
                                if(carItem.getYearOfConstrFrom().length() >= 4){
                                    yearCarFrom = Integer.parseInt(carItem.getYearOfConstrFrom().substring(0, 4));
                                }else {
                                    yearCarFrom = Integer.parseInt(carItem.getYearOfConstrFrom());
                                }
                                int yearCarTo = 0;
                                if(carItem.getYearOfConstrTo().length() >= 4){
                                    yearCarTo = Integer.parseInt(carItem.getYearOfConstrTo().substring(0, 4));
                                }else {
                                    yearCarTo = Integer.parseInt(carItem.getYearOfConstrTo());
                                }

                                if(yearFilter < yearCarFrom || yearFilter > yearCarTo){
                                    passCar = false;
                                }

                            }catch (NumberFormatException | NullPointerException e){ }
                        }
                        if(modification.trim().length() > 0){
                            if (carItem == null || !FilterUtil.searchName(carItem.getTypeName(), modification.trim())) {
                                passCar = false;
                            }
                        }
                        carsPass.add(passCar);
                    }
                    //check if one car have - pass
                    for (Boolean isPass : carsPass){
                        if(isPass){
                            passManu = true;
                        }
                    }
                }
                if(passManu){
                    resultList.add(manuItem);
                }
            }

            //paginations
            if(limit > 0){
                resultList = resultList.subList(0, Math.min(resultList.size(), limit));
            }
            for (String s: resultList){
                if(s != null ){
                    JSONObject tt = new JSONObject();
                    tt.put("mark", s);
                    result.add(tt);

                    //result.add(new JSONObject().put("mark", s.getManuName()));

                }
            }
        }
        return result.toString();
    }


    @RequestMapping(value = "/searchModels", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String searchModels(@RequestParam(defaultValue = "") String mark,
                               @RequestParam(defaultValue = "") String modification,
                               @RequestParam(defaultValue = "") String year,
            @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "5") int limit) throws JSONException {
        List<JSONObject> result = new ArrayList<>();
        List<Car> cars = (List<Car>) carsRepository.findAll();
        if(cars != null){

            //filters
            if(mark.trim().length() > 0){
                Iterator<Car> it = cars.iterator();
                while (it.hasNext()) {
                    Car carItem = it.next();
                    if (carItem.getManuName() == null || !FilterUtil.searchName(carItem.getManuName(), mark.trim())) {
                        it.remove();
                    }
                }
            }
            if(query.trim().length() > 0){
                Iterator<Car> it2 = cars.iterator();
                while (it2.hasNext()) {
                    Car carItem = it2.next();
                    if (carItem.getModelName() == null || !FilterUtil.searchName(carItem.getModelName(), query.trim())) {
                        it2.remove();
                    }
                }
            }

            if(year.trim().length() > 0){
                Iterator<Car> it2 = cars.iterator();
                while (it2.hasNext()) {
                    Car carItem = it2.next();
                    try{
                        int yearFilter = Integer.parseInt(year.trim());

                        int yearCarFrom = 0;
                        if(carItem.getYearOfConstrFrom().length() >= 4){
                            yearCarFrom = Integer.parseInt(carItem.getYearOfConstrFrom().substring(0, 4));
                        }else {
                            yearCarFrom = Integer.parseInt(carItem.getYearOfConstrFrom());
                        }
                        int yearCarTo = 0;
                        if(carItem.getYearOfConstrTo().length() >= 4){
                            yearCarTo = Integer.parseInt(carItem.getYearOfConstrTo().substring(0, 4));
                        }else {
                            yearCarTo = Integer.parseInt(carItem.getYearOfConstrTo());
                        }

                        if(yearFilter < yearCarFrom || yearFilter > yearCarTo){
                            it2.remove();
                        }

                    }catch (NumberFormatException | NullPointerException e){ }

                }
            }

            if(modification.trim().length() > 0){
                Iterator<Car> it3 = cars.iterator();
                while (it3.hasNext()) {
                    Car carItem = it3.next();
                    if (carItem.getTypeName() == null || !FilterUtil.searchName(carItem.getTypeName(), modification.trim())) {
                        it3.remove();
                    }
                }
            }

            //group by model
            Map<String, List<Car>> map = new HashMap<String, List<Car>>();
            List<Car> groupCars = new ArrayList<>();
            for (Car car : cars) {
                boolean isNew = true;
                for(Car groupCar : groupCars){
                    if(car.getModelName().equals(groupCar.getModelName())){
                        isNew = false;
                    }
                }
                if(isNew){
                    groupCars.add(car);
                }
            }

            //sort
            groupCars.sort(Car.COMPARE_BY_MODEL_NAME());
            //paginations
            groupCars = groupCars.subList(0, Math.min(groupCars.size(), limit));
            //return
            for (Car car: groupCars){
                result.add(new JSONObject()
                        .put("mark", car.getManuName())
                        .put("model", car.getModelName())
                );
            }
        }
        return result.toString();
    }

    @RequestMapping(value = "/searchModifications", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String searchModifications(@RequestParam(defaultValue = "") String mark,
                               @RequestParam(defaultValue = "") String model,
                               @RequestParam(defaultValue = "") String year,
                               @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "5") int limit) throws JSONException {
        List<JSONObject> result = new ArrayList<>();
        List<Car> cars = (List<Car>) carsRepository.findAll();
        if(cars != null){
            //filters
            if(mark.trim().length() > 0){
                Iterator<Car> it = cars.iterator();
                while (it.hasNext()) {
                    Car carItem = it.next();
                    if (carItem.getManuName() == null || !FilterUtil.searchName(carItem.getManuName(), mark.trim())) {
                        it.remove();
                    }
                }
            }
            if(query.trim().length() > 0){
                Iterator<Car> it2 = cars.iterator();
                while (it2.hasNext()) {
                    Car carItem = it2.next();
                    if (carItem.getTypeName() == null || !FilterUtil.searchName(carItem.getTypeName(), query.trim())) {
                        it2.remove();
                    }
                }
            }
            if(year.trim().length() > 0){
                Iterator<Car> it3 = cars.iterator();
                while (it3.hasNext()) {
                    Car carItem = it3.next();
                    try{
                        int yearFilter = Integer.parseInt(year.trim());

                        int yearCarFrom = 0;
                        if(carItem.getYearOfConstrFrom().length() >= 4){
                            yearCarFrom = Integer.parseInt(carItem.getYearOfConstrFrom().substring(0, 4));
                        }else {
                            yearCarFrom = Integer.parseInt(carItem.getYearOfConstrFrom());
                        }
                        int yearCarTo = 0;
                        if(carItem.getYearOfConstrTo().length() >= 4){
                            yearCarTo = Integer.parseInt(carItem.getYearOfConstrTo().substring(0, 4));
                        }else {
                            yearCarTo = Integer.parseInt(carItem.getYearOfConstrTo());
                        }

                        if(yearFilter < yearCarFrom || yearFilter > yearCarTo){
                            it3.remove();
                        }

                    }catch (NumberFormatException | NullPointerException e){ }

                }
            }
            if(model.trim().length() > 0){
                Iterator<Car> it4 = cars.iterator();
                while (it4.hasNext()) {
                    Car carItem = it4.next();
                    if (carItem.getModelName() == null || !FilterUtil.searchName(carItem.getModelName(), model.trim())) {
                        it4.remove();
                    }
                }
            }
            //group by model
            Map<String, List<Car>> map = new HashMap<String, List<Car>>();
            List<Car> groupCars = new ArrayList<>();
            for (Car car : cars) {
                boolean isNew = true;
                for(Car groupCar : groupCars){
                    if(car.getTypeName().equals(groupCar.getTypeName())){
                        isNew = false;
                    }
                }
                if(isNew){
                    groupCars.add(car);
                }
            }
            //sort
            groupCars.sort(Car.COMPARE_BY_TYPE_NAME());
            //paginations
            groupCars = groupCars.subList(0, Math.min(groupCars.size(), limit));
            //return
            for (Car car: groupCars){
                result.add(new JSONObject()
                        .put("modification", car.getTypeName())
                );
            }
        }
        return result.toString();
    }

    @RequestMapping(value = "/searchYears", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String searchYears(@RequestParam(defaultValue = "") String query,
                              @RequestParam(defaultValue = "") String mark,
                                      @RequestParam(defaultValue = "") String model,
                                      @RequestParam(defaultValue = "") String modification,
                                      @RequestParam(defaultValue = "5") int limit) throws JSONException {
        List<JSONObject> result = new ArrayList<>();
        List<Car> cars = (List<Car>) carsRepository.findAll();
        if(cars != null){
            //filters

           /* if(query.trim().length() > 0){
                Iterator<Car> it2 = cars.iterator();
                while (it2.hasNext()) {
                    Car carItem = it2.next();
                    if (carItem.getYearOfConstrFrom() == null || !FilterUtil.searchName(carItem.getYearOfConstrFrom(), query.trim())) {
                        it2.remove();
                    }
                }
            }*/

            if(mark.trim().length() > 0){
                Iterator<Car> it = cars.iterator();
                while (it.hasNext()) {
                    Car carItem = it.next();
                    if (carItem.getManuName() == null || !FilterUtil.searchName(carItem.getManuName(), mark.trim())) {
                        it.remove();
                    }
                }
            }

            if(modification.trim().length() > 0){
                Iterator<Car> it3 = cars.iterator();
                while (it3.hasNext()) {
                    Car carItem = it3.next();
                    if (carItem.getTypeName() == null || !FilterUtil.searchName(carItem.getTypeName(), modification.trim())) {
                        it3.remove();
                    }
                }
            }
            if(model.trim().length() > 0){
                Iterator<Car> it4 = cars.iterator();
                while (it4.hasNext()) {
                    Car carItem = it4.next();
                    if (carItem.getModelName() == null || !FilterUtil.searchName(carItem.getModelName(), model.trim())) {
                        it4.remove();
                    }
                }
            }

            //group by model
/*           Map<String, List<Car>> map = new HashMap<String, List<Car>>();
            List<Car> groupCars = new ArrayList<>();
            for (Car car : cars) {
                boolean isNew = true;
                for(Car groupCar : groupCars){
                    if(car.getYearOfConstrFrom() != null && groupCar.getYearOfConstrFrom() != null && car.getYearOfConstrTo() != null && groupCar.getYearOfConstrTo() != null &&
                            car.getYearOfConstrFrom().equals(groupCar.getYearOfConstrFrom()) && car.getYearOfConstrTo().equals(groupCar.getYearOfConstrTo())){
                        isNew = false;
                    }
                }
                if(isNew){
                    groupCars.add(car);
                }
            }
*/

            List<Integer> yearsAll = new ArrayList<>();
            for (Car car : cars) {
                try {
                    String yearFrom = car.getYearOfConstrFrom().length() >= 4 ? car.getYearOfConstrFrom().substring(0, 4) : null;
                    String yearTo = car.getYearOfConstrTo().length() >= 4 ? car.getYearOfConstrTo().substring(0, 4) : null;
                    yearsAll.add(Integer.parseInt(yearFrom));
                    yearsAll.add(Integer.parseInt(yearTo));
                }catch (NumberFormatException | NullPointerException e){}
            }
//Distinct util
            List<Integer> years = new ArrayList<>(
                    new HashSet<>(yearsAll));


            if(query.trim().length() > 0){
                Iterator<Integer> it2 = years.iterator();
                while (it2.hasNext()) {
                    Integer year = it2.next();
                    if (!FilterUtil.searchName(String.valueOf(year), query.trim())) {
                        it2.remove();
                    }
                }
            }



//sort USE UTIL
            Comparator<Integer> cmp = new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    return o1.compareTo(o2);
                }
            };
            years.sort(cmp);

            //paginations
            years = years.subList(0, Math.min(years.size(), limit));
            //return
            for (Integer yearItem : years){
                result.add(new JSONObject()
                        .put("year", String.valueOf(yearItem))
                );
            }
            /*for (Car car: groupCars){
                result.add(new JSONObject()
                        .put("yearFrom", car.getYearOfConstrFrom().length() >= 4 ? car.getYearOfConstrFrom().substring(0, 4) : car.getYearOfConstrFrom())
                        .put("yearTo", car.getYearOfConstrTo().length() >= 4 ? car.getYearOfConstrTo().substring(0, 4) : car.getYearOfConstrTo())
                );
            }*/
        }
        return result.toString();
    }

//PAGES
    @RequestMapping(value = "/pages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PageAllData> getPages(@RequestParam(defaultValue = Constants.LANGUAGE_UA) String language) throws JSONException {

        List<PageAllData> result = new ArrayList<>();
        List<Page> pages = pageRepository.findAllByShowInHeaderAndStatus(true, true);
        if(pages != null){
            //for images
            SettingModel basePath = settingRepository.findSettingModelByName(Constants.SETTING_BASE_PATH);
            SettingModel baseFolder = settingRepository.findSettingModelByName(Constants.SETTING_BASE_RESOURCES_FOLDER);

            for(Page page : pages){

                if(page != null){
                    PageAllData pageAllData = new PageAllData();
                    pageAllData.setId(page.getId());
                    pageAllData.setSeo_url(page.getSeo_url());
                    pageAllData.setShow_in_header(page.isShowInHeader());
                    pageAllData.setTemplate(page.getTemplate());
                    //pageAllData.setImage(page.getImage());
                    pageAllData.setImage(GenerateDataUtil.generateImageURL(page.getImage(), baseFolder, basePath));
                    pageAllData.setStatus(page.isStatus());
                    pageAllData.setIco_block_2_1(GenerateDataUtil.generateImageURL(page.getIco_block_2_1(), baseFolder, basePath));
                    pageAllData.setIco_block_2_2(GenerateDataUtil.generateImageURL(page.getIco_block_2_2(), baseFolder, basePath));
                    pageAllData.setIco_block_2_3(GenerateDataUtil.generateImageURL(page.getIco_block_2_3(), baseFolder, basePath));
                    List<PageImage> images = pageImageRepository.findAllByPageId(page.getId());
                    if(images != null) {
                        for(PageImage imageItem : images){
                            String name = imageItem.getImage();
                            imageItem.setImage(GenerateDataUtil.generateImageURL(name, baseFolder, basePath));
                        }
                        pageAllData.setImages(images);
                    }
                    PageDescription description = pageDescriptionRepositorytory.findFirstByPageIdAndLanguage(page.getId(), language);
                    if(description != null){
                        pageAllData.setMeta_title(description.getMeta_title());
                        pageAllData.setMeta_keyword(description.getMeta_keyword());
                        pageAllData.setMeta_description(description.getMeta_description());
                        pageAllData.setTitle(description.getTitle());
                        pageAllData.setTitle_banner(description.getTitle_banner());
                        pageAllData.setDescription(description.getDescription());
                        pageAllData.setBlock_1_title(description.getBlock_1_title());
                        pageAllData.setBlock_1_description(description.getBlock_1_description());
                        pageAllData.setBlock_2_title(description.getBlock_2_title());
                        pageAllData.setBlock_2_1_title(description.getBlock_2_1_title());
                        pageAllData.setBlock_2_1_description(description.getBlock_2_1_description());
                        pageAllData.setBlock_2_2_title(description.getBlock_2_2_title());
                        pageAllData.setBlock_2_2_description(description.getBlock_2_2_description());
                        pageAllData.setBlock_2_3_title(description.getBlock_2_3_title());
                        pageAllData.setBlock_2_3_description(description.getBlock_2_3_description());
                        pageAllData.setBlock_3_title(description.getBlock_3_title());
                    }
                    result.add(pageAllData);
                }

            }
        }
        return result;
    }

//PARTNERS
    //validate partner
    @RequestMapping(value = "/validatePartner", method = RequestMethod.GET)
    public void validatePartner(@RequestHeader("Authorization") String token, @RequestParam String domain)  {

        //trim if have:
        domain = domain.replaceAll("https://","");
        domain = domain.replaceAll("http://","");
        domain = domain.replaceAll("www.","");
        domain = domain.replaceAll("/","");

        Partner partner = partnersRepository.findPartnerByDomain(domain);
        if(partner != null){
            if(token.trim().length() == 0 || !partner.getPartnerToken().equals(token)){
                throw new AccessException(Constants.PARTNER_TOKEN_EXCEPTIONS);
            }
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }


//SETTING
    @RequestMapping(value = "/setting", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SettingModel> getSetting() {
        return (List<SettingModel>) settingRepository.findAll();
    }

    @RequestMapping(value = "/setting/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public SettingModel getSettingItem(@PathVariable(value = "name") String name) {
        SettingModel settingItem = settingRepository.findSettingModelByName(name);
        if(settingItem != null){
            return settingItem;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

//SEND FORM
    @RequestMapping(value = "/contactRequest", method = RequestMethod.POST)
    public void sendContactRequest(@RequestParam String catalog_url_name, @RequestParam String name, @RequestParam String phone, @RequestParam String message) throws IOException, MessagingException {
        if(name.trim().length() > 0 && phone.trim().length() > 0 && message.trim().length() > 0){

            SettingModel receiving_email = settingRepository.findSettingModelByName(Constants.SETTING_FORM_RECEIVING_EMAIL);

            final Properties properties = new Properties();
            properties.load(ContentController.class.getClassLoader().getResourceAsStream("mail.properties"));
            //create session
            Session mailSession = Session.getDefaultInstance(properties);
            MimeMessage messageMime = new MimeMessage(mailSession);
            //message.setFrom(new InternetAddress("euralistest@gmail.com"));
            messageMime.setFrom(new InternetAddress("avtonova-d@fairtech.marketing"));

            if(receiving_email != null && receiving_email.getValue() != null && receiving_email.getValue().trim().length() > 0){
                messageMime.addRecipient(Message.RecipientType.TO, new InternetAddress(receiving_email.getValue()));
            }else {
                //error not set in setting
                throw new ServerException(Constants.ERROR_API_SETTING_NOT_FOUND + Constants.SETTING_FORM_RECEIVING_EMAIL);
            }

            messageMime.setSubject("Сообщение от пользователя сайта-каталога " + catalog_url_name);
            //max message length - 500
            messageMime.setText("Имя пользователя: '" + name + "'. Номер пользователя: " + phone + "'. Сообщение:\n " +
                    message.trim().substring(0, message.length() >= 500 ? 500 : message.length()));
            //create transport
            Transport transport = mailSession.getTransport();
            transport.connect(null, "9mZl7RXqJ1j3");
            transport.sendMessage(messageMime, messageMime.getAllRecipients());
            transport.close();
        }else {
            throw new BadRequestException(Constants.ERROR_WRONG_INPUT_PARAM + "parameters 'name', 'phone' and 'message' must not have empty values");
        }
    }


//PRIVATE METHODS
    private ProductForSite makeProductFromAttrs(@NotNull String language, String code, @NotNull List<ProductAttribute> attributes,
                                                 SettingModel baseUrl, SettingModel secondaryUrlParams, ListSearchUtil searchUtil){
        ProductForSite productForSite = new ProductForSite();
        productForSite.setCode(code);

        for(ProductAttribute attrItem : attributes){
            switch (attrItem.getName()){

                case Constants.PRODUCT_ATTR_ARTICLE:
                    productForSite.setArticle(attrItem.getValue());
                    break;

                case Constants.PRODUCT_ATTR_SEO_URL:
                    productForSite.setSeoUrl(attrItem.getValue());
                    break;

                case Constants.PRODUCT_ATTR_NAME:
                    if(language.equals(Constants.LANGUAGE_RU)){
                        productForSite.setName(attrItem.getValue());
                    }
                    break;

                case Constants.PRODUCT_ATTR_NAME_UA:
                    if(!language.equals(Constants.LANGUAGE_RU)){
                        productForSite.setName(attrItem.getValue());
                    }
                    break;

                case Constants.PRODUCT_ATTR_IMAGE_LIST:
                    List<String> imageList = AttributeParseUtil.parseImagesListAttrFromDB(attrItem.getValue());
                    if(imageList.size() > 0){
                        productForSite.setImage(imageList.get(0));
                    }
                    break;

                case Constants.PRODUCT_ATTR_FEATURED:
                    productForSite.setFeatured(Boolean.parseBoolean(attrItem.getValue()));
                    break;

                case Constants.PRODUCT_ATTR_PRICE_FROM:
                    productForSite.setPriceFrom(attrItem.getValue());
                    break;

                case Constants.PRODUCT_ATTR_PRICE_TO:
                    productForSite.setPriceTo(attrItem.getValue());
                    break;

                case Constants.PRODUCT_ATTR_AMOUNT:
                    productForSite.setAmount(attrItem.getValue());
                    break;

                case Constants.PRODUCT_ATTR_MANUFACTURER_NAME:
                    productForSite.setManufacturerName(attrItem.getValue());
                    break;

                case Constants.PRODUCT_ATTR_MANUFACTURER_BRAND_ID:
                    //set link
                    if(baseUrl != null){
                        String brandId = attrItem.getValue();
                        productForSite.setLink(GenerateDataUtil.generateProductLink(language, baseUrl.getValue(), productForSite.getCode(), brandId, secondaryUrlParams, attributes, searchUtil));
                    }
                    break;
                case Constants.PRODUCT_ATTR_CAR_LIST:
                    String carsStr = attrItem.getValue();
                    List<Integer> carIds = AttributeParseUtil.parseListAttrFromDBToListInt(carsStr);
                    //set value - set distinct manu names
                    //.setCarList(searchUtil.findDistinctManuNamesFromAllByCarIds(cars));
                    productForSite.setCarList(carsRepository.findDistinctManuNamesByIds(carIds.size() > 0 ? carIds : null));
                    break;
            }
        }

        return productForSite;
    }

    private PaginationModel makePaginationModel(int productsSize, int page, int limit, String order, String sort){
        PaginationModel paginationModel = new PaginationModel();
        paginationModel.setTotalItems(productsSize);
        paginationModel.setLimit(limit);
        //all pages
        int allPages = 0;
        if(productsSize > 0){
            allPages = productsSize / Math.min(productsSize, limit);
            if(productsSize % Math.min(productsSize, limit) > 0){
                allPages += 1;
            }
        }
        paginationModel.setPages(allPages);
        paginationModel.setCurrentPage(page);
        paginationModel.setOrder(order);
        paginationModel.setSort(sort);
        return paginationModel;
    }


    private List<CarForSite> mareCarsForSite(@NotNull String language, List<Car> cars){
        List<CarForSite> carsForSite = new ArrayList();
        if(cars != null){
            for(Car carItem: cars){
                if(carItem != null){
                    carItem.makeCorrectYears(); //cut technical day
                    CarForSite carSiteItem = new CarForSite(language,
                                carItem.getId(), carItem.getCarId(), carItem.getManuName(),
                                carItem.getYearOfConstrFrom(), carItem.getYearOfConstrTo(),
                                carItem.getModelName(), carItem.getTypeName(), carItem.getManuId(), carItem.getModId(),
                                carItem.getConstructionType(), carItem.getConstructionTypeUa(),
                                carItem.getFuelType(), carItem.getFuelTypeUa()
                            );
                    carsForSite.add(carSiteItem);
                }
            }
        }
        return carsForSite;
    }


    private List<AttributeWithValues> getPricesForFilter(String language, List<Integer> ids,  String priceFromFilter, String priceToFilter){

        List<AttributeWithValues> priceVariants = new ArrayList<>();

        //get variables
        List<AttributesPattern> attributes = new ArrayList<>();
        AttributesPattern attributePriceFrom = attributesPatternsRepository.findAttributesPatternByName(Constants.PRODUCT_ATTR_PRICE_FROM);
        if(attributePriceFrom != null){
            attributes.add(attributePriceFrom);
        }
        AttributesPattern attributePriceTo = attributesPatternsRepository.findAttributesPatternByName(Constants.PRODUCT_ATTR_PRICE_TO);
        if(attributePriceTo != null){
            attributes.add(attributePriceTo);
        }

        for(AttributesPattern attrItem : attributes){
            List<String> variables = productAttributesRepository.findDistinctAttrValuesByIds(attrItem.getName(), ids.isEmpty() ? null : ids);
            if(variables != null && variables.size() > 0){
                //dell all empty
                Iterator<String> it = variables.iterator();
                while (it.hasNext()) {
                    String varItem = it.next();
                    if(varItem.trim().length() == 0){
                        it.remove();
                    }
                }
                if(variables.size() > 0){
                    Integer topPrice = null;  //top - minimum or maximum
                    try {
                        topPrice = (int) Double.parseDouble(variables.get(0));
                    }catch (NumberFormatException | NullPointerException e){}

                    for(String variableItem : variables){
                        try {
                            int checkValue = (int) Double.parseDouble(variableItem);
                            if(topPrice != null){

                                if(attrItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_FROM)){
                                    //ищем минимальное
                                    if(checkValue < topPrice){
                                        topPrice = checkValue;
                                    }
                                }else {
                                    //ищем максимальное
                                    if(checkValue > topPrice){
                                        topPrice = checkValue;
                                    }
                                }
                            }else {
                                topPrice = checkValue;
                            }
                        }catch (NumberFormatException | NullPointerException e){}
                    }

                    if(topPrice != null){
                        variables.clear();
                        variables.add(String.valueOf(topPrice));
                    }else {
                        variables.clear();
                        variables.add("0");
                    }

                    //set data
                    AttributeWithValues attributeWithValuesCustom = new AttributeWithValues();
                    AttributeWithValues attributeWithValuesReal = new AttributeWithValues();
                    if(attrItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_FROM)){
                        attributeWithValuesCustom.setName("priceMin");

                        //change original param
                        List<String> variantsFilter = new ArrayList<>();
                        variantsFilter.add(priceFromFilter);
                        attributeWithValuesReal.setValues(variantsFilter);
                    }else {
                        attributeWithValuesCustom.setName("priceMax");

                        //change original param
                        List<String> variantsFilter = new ArrayList<>();
                        variantsFilter.add(priceToFilter);
                        attributeWithValuesReal.setValues(variantsFilter);
                    }

                    if(language.equals(Constants.LANGUAGE_RU)){
                        attributeWithValuesCustom.setTitle(attrItem.getTitle());
                        attributeWithValuesReal.setTitle(attrItem.getTitle());
                    }else {
                        attributeWithValuesCustom.setTitle(attrItem.getTitleUa());
                        attributeWithValuesReal.setTitle(attrItem.getTitleUa());
                    }
                    attributeWithValuesCustom.setValues(variables);
                    priceVariants.add(attributeWithValuesCustom);
                    //add original param
                    attributeWithValuesReal.setName(attrItem.getName());
                    priceVariants.add(attributeWithValuesReal);

                }
            }
        }


        return priceVariants;
    }


}
