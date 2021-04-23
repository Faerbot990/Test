package com.api.Autonova.services.external_api;

import com.api.Autonova.exceptions.AccessException;
import com.api.Autonova.models.*;
import com.api.Autonova.repository.AccessRepository;
import com.api.Autonova.services.external_api.OneCApiService;
import com.api.Autonova.utils.AesEncriptionUtil;
import com.api.Autonova.utils.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TecDocApiService {
    //СЕРВИС ДЛЯ ВЗАИМОДЕЙСТВИЯ С АПИ TecDoc

    @Autowired
    AccessRepository accessRepository;

    private ExternalAccess accessAPIData = null;

    private final static String TECDOC_API_URL = "https://webservice.tecalliance.services/pegasus-3-0/services/TecdocToCatDLB.jsonEndpoint?";

    private Logger logger = LoggerFactory.getLogger(TecDocApiService.class);


    private boolean checkAccess(ExternalAccess accessAPIData){
        if(accessAPIData != null && accessAPIData.getLogin() != null && accessAPIData.getToken() != null){
            return true;
        }else {
            throw new AccessException(Constants.ACCESS_TECDOC_EXCEPTIONS);
        }
    }

    //set actual access
    public void updateAccess(){
        accessAPIData = accessRepository.findByName(Constants.ACCESS_NAME_TECDOC);
    }


    private ResponseEntity<ResponseTecDocAPI> makeRequest(JSONObject inputJsonMethod){

        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString(TECDOC_API_URL)
                // Add query parameter
                .queryParam("api_key", accessAPIData.getToken());

        HttpEntity<String> request = new HttpEntity<String>(inputJsonMethod.toString(), headers);
        return restTemplate.exchange(
                urlBuilder.build().toUri(),
                HttpMethod.POST,
                request,
                ResponseTecDocAPI.class);
    }

    private ResponseEntity<JsonNode> makeRequestJsonNode(JSONObject inputJsonMethod){

        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString(TECDOC_API_URL)
                // Add query parameter
                .queryParam("api_key", accessAPIData.getToken());

        HttpEntity<String> request = new HttpEntity<String>(inputJsonMethod.toString(), headers);
        return restTemplate.exchange(
                urlBuilder.build().toUri(),
                HttpMethod.POST,
                request,
                JsonNode.class);
    }


    public String getLocalArticle(String articleFrom1C, String brandId){
        checkAccess(accessAPIData);

        JSONObject inputJson = new JSONObject();
        JSONObject methodJson = new JSONObject();
        try {
            methodJson.put("provider", AesEncriptionUtil.decrypt(accessAPIData.getLogin()));
            methodJson.put("articleCountry", "RU");
            methodJson.put("lang", "RU");
            methodJson.put("country", "RU");

            methodJson.put("brandId", brandId);
            methodJson.put("articleNumber", articleFrom1C);
            methodJson.put("numberType", 0);
            methodJson.put("searchExact", true);

            inputJson.put("getArticleDirectSearchAllNumbersWithState", methodJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseEntity<ResponseTecDocAPI> resp = makeRequest(inputJson);

        if(resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null && resp.getBody().getData() != null &&
                resp.getBody().getData().get("array") != null && resp.getBody().getData().get("array").size() > 0 &&
                resp.getBody().getData().get("array").get(0).get("articleId") != null){
            return resp.getBody().getData().get("array").get(0).get("articleId").asText();
        }else {
            logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getLocalArticle'" + " from " + "'" + articleFrom1C + "'" + " with manufacturerBrandId " + "'" + brandId + "'");
            return null;
        }

    }

    //get product data (immediateAttributs or oenNumbers)
    private JsonNode getProductData(String article, String brandId, String ATTR_VARIANT){
        checkAccess(accessAPIData);

        JSONObject inputJson = new JSONObject();
        JSONObject methodJson = new JSONObject();
        try {
            methodJson.put("provider", AesEncriptionUtil.decrypt(accessAPIData.getLogin()));
            methodJson.put("articleCountry", "RU");
            methodJson.put("lang", "RU");

            methodJson.put("brandId", brandId);
            //article
            JSONObject articleIdJson = new JSONObject();
            JSONArray articlesArray = new JSONArray();
            articlesArray.put(article);
            articleIdJson.put("array", articlesArray);
            methodJson.put("articleId", articleIdJson);
            methodJson.put("immediateAttributs", true);
            methodJson.put("oeNumbers", true);

            inputJson.put("getDirectArticlesByIds6", methodJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseEntity<ResponseTecDocAPI> resp = makeRequest(inputJson);
        if(resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null && resp.getBody().getData() != null &&
                resp.getBody().getData().get("array") != null && resp.getBody().getData().get("array").size() > 0 &&
                resp.getBody().getData().get("array").get(0).get(ATTR_VARIANT) != null){
            return resp.getBody().getData().get("array").get(0).get(ATTR_VARIANT).get("array");
        }else {
            logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getProductData'" + " from " + "'" + article + "'" + " with " + "'" + brandId + "'");
            return null;
        }
    }

    public List<ProductCharacteristic> getProductCharacteristics(String article, String brandId){
        checkAccess(accessAPIData);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //for ignore another param in model
        ObjectReader reader = mapper.readerFor(new TypeReference<List<ProductCharacteristic>>() {});
        try {
            return reader.readValue(getProductData(article, brandId, Constants.TEC_DOC_ATTR_IMMEDIATE_ATTS));
        }catch (IOException | NullPointerException e) {
            return null;
        }
    }

    public List<ProductOENumber> getProductOENumbers(String article, String brandId){
        checkAccess(accessAPIData);

        JsonNode oeNumbers = getProductData(article, brandId, Constants.TEC_DOC_ATTR_OE_NUMBERS);
        List<ProductOENumber> numbersList = new ArrayList<>();
        if(oeNumbers != null && oeNumbers.get(0) != null){
            for(int i = 0; i < oeNumbers.size(); i++){
                if(oeNumbers.get(i) != null && oeNumbers.get(i).get("oeNumber") != null){
                    ProductOENumber numberItem = new ProductOENumber();
                    numberItem.setCode(oeNumbers.get(i).get("oeNumber").asText());
                    numbersList.add(numberItem);
                }
            }
        }
        return numbersList;
    }

    //get cars id's for product
    private JsonNode getIdCarsForProduct(String article){
        checkAccess(accessAPIData);

        JSONObject inputJson = new JSONObject();
        JSONObject methodJson = new JSONObject();

        try {
            methodJson.put("provider", AesEncriptionUtil.decrypt(accessAPIData.getLogin()));
            methodJson.put("articleCountry", "RU");
            methodJson.put("country", "RU");
            methodJson.put("lang", "ru");

            methodJson.put("articleId", article);
            methodJson.put("linkingTargetType", "P");

            inputJson.put("getArticleLinkedAllLinkingTarget4", methodJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseEntity<ResponseTecDocAPI> resp = makeRequest(inputJson);

        if(resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null && resp.getBody().getData() != null &&
                resp.getBody().getData().get("array") != null && resp.getBody().getData().get("array").size() > 0 &&
                resp.getBody().getData().get("array").get(0).get("articleLinkages") != null){
            return resp.getBody().getData().get("array").get(0).get("articleLinkages").get("array");
        }else {
            logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getIdCarsForProduct'" + " from " + "'" + article + "'");
            return null;
        }
    }

    //get all data for car id's
    private List<Car> getCarsData(JSONArray carIds){
        checkAccess(accessAPIData);

        JSONObject inputJson = new JSONObject();
        JSONObject methodJson = new JSONObject();

        try {
            methodJson.put("provider", AesEncriptionUtil.decrypt(accessAPIData.getLogin()));
            methodJson.put("articleCountry", "RU");
            methodJson.put("lang", "RU");

            methodJson.put("countriesCarSelection", "RU");
            methodJson.put("country", "RU");

            JSONObject carIdsJson = new JSONObject();
            carIdsJson.put("array", carIds); //carIdsArray
            methodJson.put("carIds", carIdsJson);

            inputJson.put("getVehicleByIds3", methodJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseEntity<ResponseTecDocAPI> resp = makeRequest(inputJson);
        if(resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null && resp.getBody().getData() != null &&
                resp.getBody().getData().get("array") != null){

            List<Car> cars = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //for ignore another param in model
            ObjectReader reader = mapper.readerFor(new TypeReference<Car>() {});

            for(int i = 0; i < resp.getBody().getData().get("array").size(); i++){
                if(resp.getBody().getData().get("array").get(i).get("vehicleDetails") != null){
                    try {
                        Car carItem = reader.readValue(resp.getBody().getData().get("array").get(i).get("vehicleDetails"));
                        cars.add(carItem);
                    } catch (IOException e) {}
                }
            }
            return cars;
        }else {
            logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getCarsData'");
            return null;
        }
    }

    //get cars with data for product
    public List<Car> getCarsForProduct(String article){
        checkAccess(accessAPIData);

        List<Car> carsArray = new ArrayList<>();
        JsonNode carIds = getIdCarsForProduct(article);
        int arraySize = carIds != null ? carIds.size() : 0;
        int steps = 0;
        //request API get car data limit
        int limit = 25;
        if(arraySize != 0){
            steps = arraySize / Math.min(arraySize, limit);
            if(arraySize % Math.min(arraySize, limit) > 0){
                steps += 1;
            }
        }
        //get cars
        for (int i = 0; i < steps; i++){
            JSONArray carIdsPart = new JSONArray();
            int offset = limit * i;
            for(int j = offset; j < Math.min(arraySize, offset + limit); j++){
                //get all data for stack(by limit) vars
                if(carIds.get(j).get("linkingTargetId") != null) {
                    carIdsPart.put(carIds.get(j).get("linkingTargetId").asText());
                }
            }
            //request to get car data
            List<Car> carsDataPart = getCarsData(carIdsPart);
            //save stack to all list
            if(carsDataPart != null){
                carsArray.addAll(carsDataPart);
            }
        }
        return carsArray;
    }


//НУЖНО ПРОРАБОТАТЬ ВЫВОД АНАЛОГОВ

//REQUESTS FOR 1C MANIPULATION

    public JsonNode getCategories(boolean assemblyGroupFacetOptionsEnabled, String linkingTargetType, boolean includeCompleteTree,
                                  int perPage, int page){
        checkAccess(accessAPIData);

        JSONObject inputJson = new JSONObject();
        JSONObject methodJson = new JSONObject();

        try {
            methodJson.put("provider", AesEncriptionUtil.decrypt(accessAPIData.getLogin()));
            methodJson.put("articleCountry", "RU");
            methodJson.put("lang", "RU");

            methodJson.put("country", "RU");

            methodJson.put("perPage", perPage);
            methodJson.put("page", page);

            JSONObject assemblyGroupFacetOptionsJson = new JSONObject();
            assemblyGroupFacetOptionsJson.put("enabled", assemblyGroupFacetOptionsEnabled);
            assemblyGroupFacetOptionsJson.put("assemblyGroupType", linkingTargetType);
            assemblyGroupFacetOptionsJson.put("includeCompleteTree", includeCompleteTree);

            methodJson.put("assemblyGroupFacetOptions", assemblyGroupFacetOptionsJson);
            inputJson.put("getArticles", methodJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return makeRequestJsonNode(inputJson).getBody();
    }

    public JsonNode getCategoriesByArticle(String searchQuery, int searchType, int dataSupplierIds,
                                           boolean assemblyGroupFacetOptionsEnabled, String assemblyGroupType, boolean includeCompleteTree,
                                           int perPage, int page){
        checkAccess(accessAPIData);

        JSONObject inputJson = new JSONObject();
        JSONObject methodJson = new JSONObject();

        try {
            methodJson.put("provider", AesEncriptionUtil.decrypt(accessAPIData.getLogin()));
            methodJson.put("articleCountry", "RU");
            methodJson.put("lang", "RU");

            methodJson.put("searchQuery", searchQuery);
            methodJson.put("searchType", searchType);
            methodJson.put("dataSupplierIds", dataSupplierIds);

            methodJson.put("perPage", perPage);
            methodJson.put("page", page);

            JSONObject assemblyGroupFacetOptionsJson = new JSONObject();
            assemblyGroupFacetOptionsJson.put("enabled", assemblyGroupFacetOptionsEnabled);
            assemblyGroupFacetOptionsJson.put("assemblyGroupType", assemblyGroupType);
            assemblyGroupFacetOptionsJson.put("includeCompleteTree", includeCompleteTree);

            methodJson.put("assemblyGroupFacetOptions", assemblyGroupFacetOptionsJson);

            inputJson.put("getArticles", methodJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return makeRequestJsonNode(inputJson).getBody();
    }
}
