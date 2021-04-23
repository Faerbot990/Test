package com.api.Autonova.services.external_api;

import com.api.Autonova.exceptions.AccessException;
import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.*;
import com.api.Autonova.repository.AccessRepository;
import com.api.Autonova.utils.AesEncriptionUtil;
import com.api.Autonova.utils.Base64Coder;
import com.api.Autonova.utils.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OneCApiService {

    Logger logger = LoggerFactory.getLogger(OneCApiService.class);
    //String BASIC_URL = "https://hs.autonovad.ua/andhsj/hs/catalog/";

    @Autowired
    AccessRepository accessRepository;


    private ExternalAccess accessAPIData = null;

    private boolean checkAccess(ExternalAccess accessAPIData){
        if(accessAPIData != null &&
                accessAPIData.getLogin() != null && accessAPIData.getPass() != null && accessAPIData.getToken() != null){
            return true;
        }else {
            throw new AccessException(Constants.ACCESS_1C_EXCEPTIONS);
        }
    }

    //set actual access
    public void updateAccess(){
        accessAPIData = accessRepository.findByName(Constants.ACCESS_NAME_1C);
    }

    //Create request methods
    private HttpComponentsClientHttpRequestFactory setOffSLL(){
    //SET OFF SSL Request
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = null;
        HttpComponentsClientHttpRequestFactory requestFactory = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
            requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            return requestFactory;
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.error(Constants.ONEC_API_ERROR_OFF_SSL);
            return null;
        }
    }

    private ResponseEntity<ResponseOneCAPI> createRequest(String method, String queryParam, String patchParam){

        String exceptionMethodName = "";

        RestTemplate restTemplate = new RestTemplate(setOffSLL());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                        +":"+
                            AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        HttpEntity<String> request = new HttpEntity<String>(headers);

        UriComponentsBuilder urlBuilder = null;
        
        switch (method){

            case "getManufacturers":
                exceptionMethodName = "getManufacturers";
                urlBuilder = UriComponentsBuilder
                        .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/manufacturer/")
                        // Add query parameter
                        .queryParam("token", accessAPIData.getToken());
                break;
            
            case "getCategories":
                exceptionMethodName = "getCategories";
                urlBuilder = UriComponentsBuilder
                        .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/categories/")
                        // Add query parameter
                        .queryParam("token", accessAPIData.getToken());
                break;

            case "getAllProducts":
                exceptionMethodName = "getAllProducts";
                urlBuilder = UriComponentsBuilder
                        .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/article/")
                        // Add query parameter
                        .queryParam("token", accessAPIData.getToken())
                        .queryParam("category", queryParam);
                break;

            case "getProductData":
                exceptionMethodName = "getProductData";
                urlBuilder = UriComponentsBuilder
                        .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/article/" + patchParam)
                        // Add query parameter
                        .queryParam("token", accessAPIData.getToken());
                break;

            default:
                return null;
        }

        try {
            return restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.GET,
                    request,
                    ResponseOneCAPI.class);
        }catch (HttpClientErrorException e) {
            logger.error(e.toString() + ". From method: " +  "'" + exceptionMethodName +  "'" + ". Body: " + e.getResponseBodyAsString());
            return null;
        }
    }


//REQUESTS

    //Manufacturers
    public List<Manufacturer> getManufacturers(){
        checkAccess(accessAPIData);
        ResponseEntity<ResponseOneCAPI> resp = createRequest( "getManufacturers", null, null);
        if(resp != null && resp.getBody() != null && resp.getBody().getStatus() != null && resp.getBody().getStatus() == 200 &&
                resp.getBody().getData() != null){

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader reader = mapper.readerFor(new TypeReference<List<Manufacturer>>() {});
            try {
                return reader.readValue(resp.getBody().getData());
            } catch (IOException e) {
                logger.error(Constants.ERROR_READING_DATA + "'getManufacturers'");
                return null;
            }
        }else {
            logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getManufacturers'");
            return null;
        }
    }

    //Categories
    public List<CategoryInResponse> getCategories(){

        checkAccess(accessAPIData);
        ResponseEntity<ResponseOneCAPI> resp = createRequest("getCategories", null, null);
        if(resp != null && resp.getBody() != null && resp.getBody().getStatus() != null && resp.getBody().getStatus() == 200 &&
            resp.getBody().getData() != null){

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader reader = mapper.readerFor(new TypeReference<List<CategoryInResponse>>() {});

            try {
                return reader.readValue(resp.getBody().getData());
            } catch (IOException e) {
                logger.error(Constants.ERROR_READING_DATA + "'getCategories'");
                return null;
            }
        }else {
            logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getCategories'");
            return null;
        }
    }


//get all products from category
    public List<Product> getAllProducts(String category){

        checkAccess(accessAPIData);
        ResponseEntity<ResponseOneCAPI> resp = createRequest("getAllProducts", category, null);
        if(resp != null && resp.getBody() != null && resp.getBody().getStatus() != null && resp.getBody().getStatus() == 200 &&
                resp.getBody().getData() != null){

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader reader = mapper.readerFor(new TypeReference<List<Product>>() {});

            try {
                return reader.readValue(resp.getBody().getData());
            } catch (IOException e) {
                logger.error(Constants.ERROR_READING_DATA + "'getAllProducts'" + " from " + "'" + category + "'");
                return null;
            }
        }else {
            logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getAllProducts'" + " from " + "'" + category + "'");
            return null;
        }
    }

    public JsonNode getProductData(String code) {
        checkAccess(accessAPIData);
        ResponseEntity<ResponseOneCAPI> resp = createRequest("getProductData", null, code);
        if(resp != null && resp.getBody() != null && resp.getBody().getStatus() != null && resp.getBody().getStatus() == 200 &&
                 resp.getBody().getData() != null){
            return resp.getBody().getData();
        }else {
            logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getProductData'" + " from " + "'" + code + "'");
            return null;
        }
    }

//get products by codes WITH current attrs
    public JsonNode getProductsDataByCodes(List<String> codes, List<String> attrs) {
        checkAccess(accessAPIData);
        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                        + ":"
                        + AesEncriptionUtil.decrypt(accessAPIData.getPass())));

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/article/")
                // Add query parameter
                .queryParam("token", accessAPIData.getToken());

        JSONObject inputJson = new JSONObject();


        try {
            inputJson.put("codes", new JSONArray(codes));
            inputJson.put("fields", new JSONArray(attrs));
        }catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<String>(inputJson.toString(), headers);

        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.POST,
                    request,
                    ResponseOneCAPI.class);

            if(resp != null && resp.getStatusCode() == HttpStatus.OK ){
                if(resp.getBody() != null && resp.getBody().getData() != null){
                    return resp.getBody().getData();
                }else {
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataByCodes'" + " from " + "'" + Arrays.toString(codes.toArray()) + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataByCodes'" + " from " + "'" + Arrays.toString(codes.toArray()) + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'" + ". "
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataByCodes'" + " from " + "'" + Arrays.toString(codes.toArray()) + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataByCodes'" + " from " + "'" + Arrays.toString(codes.toArray()) + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }



    //get products by category WITH current attrs
    public List<JsonNode> getProductsDataWithAttrsByCategory(String category, List<String> attrs, int offset, int amount) {
        checkAccess(accessAPIData);
        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                        + ":"
                        + AesEncriptionUtil.decrypt(accessAPIData.getPass())));

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/article/")
                // Add query parameter
                .queryParam("token", accessAPIData.getToken())
                .queryParam("category", category);

        JSONObject inputJson = new JSONObject();


        try {
            inputJson.put("fields", new JSONArray(attrs));
            inputJson.put("offset", offset);
            inputJson.put("amount", amount);
        }catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<String>(inputJson.toString(), headers);

        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.POST,
                    request,
                    ResponseOneCAPI.class);

            if(resp != null && resp.getStatusCode() == HttpStatus.OK ){
                if(resp.getBody() != null && resp.getBody().getData() != null){

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    ObjectReader reader = mapper.readerFor(new TypeReference<List<JsonNode>>() {});
                    try {
                        return reader.readValue(resp.getBody().getData());
                    } catch (IOException e) {
                        logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataWithAttrsByCategory'" + " from category " + "'" + category + "'" + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'");
                        return null;
                    }


                }else {
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataWithAttrsByCategory'" + " from category " + "'" + category + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataWithAttrsByCategory'" + " from category " + "'" + category + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'" + ". "
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataWithAttrsByCategory'" + " from category " + "'" + category + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'getProductsDataWithAttrsByCategory'" + " from category " + "'" + category + "'" + " where attrs " + "'"  + Arrays.toString(attrs.toArray()) + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }


//WORK WITH ATTR
    public boolean addAttribute(String name){
//ОЖИДАЮ ЗАПРОС ДЛЯ ДОБАВЛЕНИЯ НОВОГО АТРИБУТА В 1С API
        //Добавим новый атрибут
        //После нужно обновлять систему
        //return false;
        checkAccess(accessAPIData);
        throw new ServerException(Constants.ERROR_REQUEST_1C_INTEGRATION);
    }

    public boolean updateAttribute(String name, String newName){
//ОЖИДАЮ ЗАПРОС ДЛЯ ИЗМЕНЕНИЕ СТРУКТУРЫ(ИМЕНИ) АТРИБУТА В 1С API
        //Изменяем структуру
        //После нужно локально поменять в БД все атрибуты с данным именем
        //return false;
        checkAccess(accessAPIData);
        throw new ServerException(Constants.ERROR_REQUEST_1C_INTEGRATION);
    }


    public JsonNode updateProductAttribute(String code, String name, String value){
        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                            + ":"
                            + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/article/" + code)
                // Add query parameter
                .queryParam("token", accessAPIData.getToken());

        JSONObject inputJson = new JSONObject();
        try {
            switch (name){
                case Constants.PRODUCT_ATTR_CAR_LIST:
                    inputJson.put(name, new JSONArray(value));
                    break;
                case Constants.PRODUCT_ATTR_ANALOG_LIST:
                    inputJson.put(name, new JSONArray(value));
                    break;
                case Constants.PRODUCT_ATTR_OE_LIST:
                    inputJson.put(name, new JSONArray(value));
                    break;
                default:
                    inputJson.put(name, value);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<String>(inputJson.toString(), headers);
        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.PATCH,
                    request,
                    ResponseOneCAPI.class);

            if(resp != null && resp.getStatusCode() == HttpStatus.OK){
                if(resp.getBody() != null && resp.getBody().getData() != null){
                    return resp.getBody().getData();
                }else {
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'updateProductAttribute'" + " from " + "'" + code + "'" + " with attr " + "'" + name + "'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'updateProductAttribute'" + " from " + "'" + code + "'" + " with attr " + "'" + name + "'" + ". "+
                            Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus() + " error:" + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'updateProductAttribute'" + " from " + "'" + code + "'" + " with attr " + "'" + name + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'updateProductAttribute'" + " from " + "'" + code + "'" + " with attr " + "'" + name + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }




//WORK WITH CARS

    public List<Car> getAllCars(){
        checkAccess(accessAPIData);
        RestTemplate restTemplate = new RestTemplate(setOffSLL());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        HttpEntity<String> request = new HttpEntity<String>(headers);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/car")
                // Add query parameter
                .queryParam("token", accessAPIData.getToken());

        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.GET,
                    request,
                    ResponseOneCAPI.class);

            if(resp != null && resp.getBody() != null && resp.getBody().getStatus() != null && resp.getBody().getStatus() == 200 &&
                    resp.getBody().getData() != null){

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ObjectReader reader = mapper.readerFor(new TypeReference<List<Car>>() {});

                try {
                    return reader.readValue(resp.getBody().getData());
                } catch (IOException e) {
                    logger.error(Constants.ERROR_READING_DATA + "'getAllCars'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getAllCars'" + ". "
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                    return null;
                }else {
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getAllCars'");
                    return null;
                }
            }
        }catch (HttpClientErrorException e){
            logger.error(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'getAllCars'" + ". Body: " + e.getResponseBodyAsString());
            return null;
        }
    }

    public List<Car> getAllCarsForProduct(String code){
        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        HttpEntity<String> request = new HttpEntity<String>(headers);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/car")
                // Add query parameter
                .queryParam("token", accessAPIData.getToken())
                .queryParam("code", code);


        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.GET,
                    request,
                    ResponseOneCAPI.class);


            if(resp != null && resp.getBody() != null && resp.getBody().getStatus() != null && resp.getBody().getStatus() == 200 &&
                    resp.getBody().getData() != null){

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ObjectReader reader = mapper.readerFor(new TypeReference<List<Car>>() {});

                try {
                    return reader.readValue(resp.getBody().getData());
                } catch (IOException e) {
                    logger.error(Constants.ERROR_READING_DATA + "'getAllCarsForProduct'" + " from " + "'" + code + "'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getAllCarsForProduct'" + " from " + "'" + code + "'" + ". "
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                    return null;
                }else {
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getAllCarsForProduct'" + " from " + "'" + code + "'");
                    return null;
                }
            }
        }catch (HttpClientErrorException e){
            logger.error(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'getAllCarsForProduct'" + " from " + "'" + code
                    + ". Body: " + e.getResponseBodyAsString());
            return null;
        }
    }

    public Car getCar(int carId){
        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        HttpEntity<String> request = new HttpEntity<String>(headers);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/car/" + carId)
                // Add query parameter
                .queryParam("token", accessAPIData.getToken());


        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.GET,
                    request,
                    ResponseOneCAPI.class);


            if(resp != null && resp.getBody() != null && resp.getBody().getStatus() != null && resp.getBody().getStatus() == 200 &&
                    resp.getBody().getData() != null){

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ObjectReader reader = mapper.readerFor(new TypeReference<Car>() {});

                try {
                    return reader.readValue(resp.getBody().getData());
                } catch (IOException e) {
                    logger.error(Constants.ERROR_READING_DATA + "'getCar'" + " from " + "'" + carId + "'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getCar'" + " from " + "'" + carId + "'" + ". "
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                    return null;
                }else {
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getCar'" + " from " + "'" + carId + "'");
                    return null;
                }
            }
        }catch (HttpClientErrorException e){
            logger.error(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'getCar'" + " from " + "'" + carId
                    + ". Body: " + e.getResponseBodyAsString());
            return null;
        }
    }

    public Car addCar(@NotNull Car carNew){
        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        //HttpEntity<String> request = new HttpEntity<String>(headers);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/car")
                // Add query parameter
                .queryParam("token", accessAPIData.getToken());


        JSONObject inputJson = new JSONObject();

        try {
            inputJson.put("carId", carNew.getCarId());
            inputJson.put("constructionType", carNew.getConstructionType() != null ? carNew.getConstructionType() : "");
            inputJson.put("constructionTypeUa", carNew.getConstructionTypeUa() != null ? carNew.getConstructionTypeUa() : "");
            inputJson.put("fuelType", carNew.getFuelType() != null ? carNew.getFuelType() : "");
            inputJson.put("fuelTypeUa", carNew.getFuelTypeUa() != null ? carNew.getFuelTypeUa() : "");
            inputJson.put("manuId", carNew.getManuId());
            inputJson.put("manuName", carNew.getManuName() != null ? carNew.getManuName() : "");
            inputJson.put("modId", carNew.getModId());
            inputJson.put("modelName", carNew.getModelName() != null ? carNew.getModelName() : "");
            inputJson.put("typeName", carNew.getTypeName() != null ? carNew.getTypeName() : "");
            inputJson.put("yearOfConstrFrom", carNew.getYearOfConstrFrom() != null ? carNew.getYearOfConstrFrom() : "");
            inputJson.put("yearOfConstrTo", carNew.getYearOfConstrTo() != null ? carNew.getYearOfConstrTo() : "");
        } catch (JSONException e) {
            return null;
        }

        HttpEntity<String> request = new HttpEntity<String>(inputJson.toString(), headers);
        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.POST,
                    request,
                    ResponseOneCAPI.class);

            if(resp != null && resp.getStatusCode() == HttpStatus.CREATED ){
                if(resp.getBody() != null && resp.getBody().getData() != null){
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    ObjectReader reader = mapper.readerFor(new TypeReference<Car>() {});
                    try {
                        return reader.readValue(resp.getBody().getData());
                    } catch (IOException e) {
                        logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_SAVE + "'addCar'");
                        return null;
                    }
                }else {
                    logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_SAVE + "'addCar'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'addCar'" + " from " + "'" + carNew.getCarId() + "'"+ ". "
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'addCar'" + " from " + "'" + carNew.getCarId() + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " + "'addCar'" + " from " + "'" + carNew.getCarId() + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }


    public Car updateCar(@NotNull Car car){
        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                        + ":"
                        + AesEncriptionUtil.decrypt(accessAPIData.getPass())));

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/car/" + car.getCarId())
                // Add query parameter
                .queryParam("token", accessAPIData.getToken());

        JSONObject inputJson = new JSONObject();

        try {
            inputJson.put("carId", car.getCarId());
            inputJson.put("constructionType", car.getYearOfConstrTo());
            inputJson.put("constructionTypeUa", car.getConstructionTypeUa());
            inputJson.put("fuelType", car.getFuelType());
            inputJson.put("fuelTypeUa", car.getFuelTypeUa());
            inputJson.put("manuId", car.getManuId());
            inputJson.put("manuName", car.getManuName());
            inputJson.put("modId", car.getModId());
            inputJson.put("modelName", car.getModelName());
            inputJson.put("typeName", car.getTypeName());
            inputJson.put("yearOfConstrFrom", car.getYearOfConstrFrom());
            inputJson.put("yearOfConstrTo", car.getYearOfConstrTo());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<String>(inputJson.toString(), headers);

        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.PATCH,
                    request,
                    ResponseOneCAPI.class);

            if(resp != null && resp.getStatusCode() == HttpStatus.OK ){
                if(resp.getBody() != null && resp.getBody().getData() != null){
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    ObjectReader reader = mapper.readerFor(new TypeReference<Car>() {});
                    try {
                        return reader.readValue(resp.getBody().getData());
                    } catch (IOException e) {
                        logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE + "'updateCar'" + " from " + "'" + car.getCarId() + "'");
                        return null;
                    }
                }else {
                    logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE + "'updateCar'" + " from " + "'" + car.getCarId() + "'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'updateCar'" + " from " + "'" + car.getCarId() + "'" + ". "
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'updateCar'" + " from " + "'" + car.getCarId() + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'updateCar'" + " from " + "'" + car.getCarId() + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }


    public ResponseOneCAPI deleteCar(int carId){
        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        HttpEntity<String> request = new HttpEntity<String>(headers);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/car/" + carId)
                // Add query params
                .queryParam("token", accessAPIData.getToken());
        try {
            ResponseEntity<ResponseOneCAPI> response = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.DELETE,
                    request,
                    ResponseOneCAPI.class);

            if(response != null && response.getStatusCode() == HttpStatus.OK){
                if(response.getBody() == null){
                    logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE + "'deleteCar'" + " from " + "'" + carId + "'");
                }
                return response.getBody();
            }else {
                if(response != null && response.getBody() != null && response.getBody().getData() != null && response.getBody().getStatus() != null
                        && response.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'deleteCar'" + " from " + "'" + carId + "'" + ". "
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + response.getBody().getStatus()
                            + " error: " + response.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'deleteCar'" + " from " + "'" + carId + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'deleteCar'" + " from " + "'" + carId + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }



//WORK WITH CHARACTERISTICS
    public List<ProductCharacteristic> getProductCharacteristics(String code) {
        checkAccess(accessAPIData);
        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        //headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        HttpEntity<String> request = new HttpEntity<String>(headers);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/specification/")
                // Add query parameter
                .queryParam("token", accessAPIData.getToken())
                .queryParam("code", code);

        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.GET,
                    request,
                    ResponseOneCAPI.class);

            if(resp != null && resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null && resp.getBody().getData() != null){
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ObjectReader reader = mapper.readerFor(new TypeReference<List<ProductCharacteristic>>() {});

                try {
                    List<ProductCharacteristic> characteristics = reader.readValue(resp.getBody().getData());
                    for (ProductCharacteristic item : characteristics){
                        item.setProductCode(code);
                    }
                    return characteristics;
                } catch (IOException e) {
                    logger.error(Constants.ERROR_READING_DATA + "'getProductCharacteristics'" + " from " + "'" + code + "'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getProductCharacteristics'" + " from " + "'" + code + "'"
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                    return null;
                }else {
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getProductCharacteristics'" + " from " + "'" + code + "'");
                    return null;
                }
            }
        }catch (HttpClientErrorException e){
            logger.error(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'getProductCharacteristics'" + " from " + "'" + code + "'"
                    + ". Body: " + e.getResponseBodyAsString());
            return null;
        }
    }

    public ProductCharacteristic getCharacteristic(String productCode, String characteristicId){
        checkAccess(accessAPIData);
        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        HttpEntity<String> request = new HttpEntity<String>(headers);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/specification/" + characteristicId)
                // Add query parameter
                .queryParam("token", accessAPIData.getToken())
                .queryParam("code", productCode);

        try{
            ResponseEntity<ResponseOneCAPI> resp = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.GET,
                    request,
                    ResponseOneCAPI.class);

            if(resp != null && resp.getBody() != null && resp.getBody().getStatus() != null && resp.getBody().getStatus() == 200 &&
                    resp.getBody().getData() != null){
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ObjectReader reader = mapper.readerFor(new TypeReference<ProductCharacteristic>() {});
                try {
                    return reader.readValue(resp.getBody().getData());
                } catch (IOException e) {
                    logger.error(Constants.ERROR_READING_DATA + "'getCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + characteristicId + "'");
                    return null;
                }
            }else {
                if(resp != null && resp.getBody() != null && resp.getBody().getData() != null && resp.getBody().getStatus() != null
                        && resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + characteristicId + "'"
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + resp.getBody().getStatus()
                            + " error: " + resp.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                    return null;
                }else {
                    logger.error(Constants.ERROR_RECEIVING_REQUEST + "'getCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + characteristicId + "'");
                    return null;
                }
            }
        }catch (HttpClientErrorException e){
            logger.error(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'getCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + characteristicId + "'"
                    + ". Body: " + e.getResponseBodyAsString());
            return null;
        }
    }

    //ADD NEW CHARACT FOR PRODUCT
    public ProductCharacteristic addCharacteristic(String productCode, @NotNull ProductCharacteristic productCharacteristic){
        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/specification/")
                // Add query parameter
                .queryParam("token", accessAPIData.getToken())
                .queryParam("code", productCode);

        JSONObject inputJson = new JSONObject();

        try {
            inputJson.put("attrId", ""); //параметр не нужен для работы но оставили его ибо без него запрос в 1С не добавляем х-ку
            inputJson.put("attrName", productCharacteristic.getAttrName() != null ? productCharacteristic.getAttrName() : "");
            inputJson.put("attrNameUa", productCharacteristic.getAttrNameUa() != null ? productCharacteristic.getAttrNameUa() : "");
            inputJson.put("attrShortName", productCharacteristic.getAttrShortName() != null ? productCharacteristic.getAttrShortName() : "");
            inputJson.put("attrShortNameUa", productCharacteristic.getAttrShortNameUa() != null ? productCharacteristic.getAttrShortNameUa() : "");
            inputJson.put("attrUnit", productCharacteristic.getAttrUnit() != null ? productCharacteristic.getAttrUnit() : "");
            inputJson.put("attrValue", productCharacteristic.getAttrValue() != null ? productCharacteristic.getAttrValue() : "");
            inputJson.put("attrValueUa", productCharacteristic.getAttrValueUa() != null ? productCharacteristic.getAttrValueUa() : "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<String>(inputJson.toString(), headers);

        try{
            ResponseEntity<ResponseOneCAPI> response = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.POST,
                    request,
                    ResponseOneCAPI.class);

            if(response != null && response.getStatusCode() == HttpStatus.CREATED){
                if(response.getBody() != null && response.getBody().getData() != null){
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    ObjectReader reader = mapper.readerFor(new TypeReference<ProductCharacteristic>() {});
                    try {
                        return reader.readValue(response.getBody().getData());
                    } catch (IOException e) {
                        logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_SAVE + "'addCharacteristic'" + " from " + "'" + productCode + "'");
                        return null;
                    }
                }else {
                    logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_SAVE + "'addCharacteristic'" + " from " + "'" + productCode + "'");
                    return null;
                }
            }else {
                if(response != null && response.getBody() != null && response.getBody().getData() != null && response.getBody().getStatus() != null
                        && response.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'addCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + productCharacteristic.getAttrShortName() + "'"
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + response.getBody().getStatus()
                            + " error: " + response.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'addCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + productCharacteristic.getAttrShortName() + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'addCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + productCharacteristic.getAttrShortName() + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }


    public ProductCharacteristic updateCharacteristic(String productCode, ProductCharacteristic productCharacteristic){
        checkAccess(accessAPIData);
        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/specification/" + productCharacteristic.getId())
                // Add query parameter
                .queryParam("token", accessAPIData.getToken())
                .queryParam("code", productCode);

        JSONObject inputJson = new JSONObject();

        try {
            inputJson.put("attrName", productCharacteristic.getAttrName() != null ? productCharacteristic.getAttrName() : "");
            inputJson.put("attrNameUa", productCharacteristic.getAttrNameUa() != null ? productCharacteristic.getAttrNameUa() : "");
            inputJson.put("attrShortName", productCharacteristic.getAttrShortName() != null ? productCharacteristic.getAttrShortName() : "");
            inputJson.put("attrShortNameUa", productCharacteristic.getAttrShortNameUa() != null ? productCharacteristic.getAttrShortNameUa() : "");
            inputJson.put("attrUnit", productCharacteristic.getAttrUnit() != null ? productCharacteristic.getAttrUnit() : "");
            inputJson.put("attrValue", productCharacteristic.getAttrValue() != null ? productCharacteristic.getAttrValue() : "");
            inputJson.put("attrValueUa", productCharacteristic.getAttrValueUa() != null ? productCharacteristic.getAttrValueUa() : "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<String>(inputJson.toString(), headers);

        try{
            ResponseEntity<ResponseOneCAPI> response = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.PATCH,
                    request,
                    ResponseOneCAPI.class);

            if(response != null && response.getStatusCode() == HttpStatus.OK ){
                if(response.getBody() != null && response.getBody().getData() != null){
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    ObjectReader reader = mapper.readerFor(new TypeReference<ProductCharacteristic>() {});
                    try {
                        return reader.readValue(response.getBody().getData());
                    } catch (IOException e) {
                        logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE + "'updateCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + productCharacteristic.getId() + "'");
                        return null;
                    }
                }else {
                    logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE + "'updateCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + productCharacteristic.getId() + "'");
                    return null;
                }
            }else {
                if(response != null && response.getBody() != null && response.getBody().getData() != null && response.getBody().getStatus() != null
                        && response.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'updateCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + productCharacteristic.getId() + "'"
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + response.getBody().getStatus()
                            + " error: " + response.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'updateCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + productCharacteristic.getId() + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'updateCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + productCharacteristic.getId() + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }


    public ResponseOneCAPI deleteCharacteristic(String productCode, int characteristicId){
        checkAccess(accessAPIData);

        RestTemplate restTemplate = new RestTemplate(setOffSLL());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " +
                new Base64Coder().encode(
                        AesEncriptionUtil.decrypt(accessAPIData.getLogin())
                                + ":"
                                + AesEncriptionUtil.decrypt(accessAPIData.getPass())
                ));

        HttpEntity<String> request = new HttpEntity<String>(headers);
        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString("https://hs.autonovad.ua/andhsj/hs/catalog/specification/" + characteristicId)
                // Add query params
                .queryParam("token", accessAPIData.getToken())
                .queryParam("code", productCode);

        try {
            ResponseEntity<ResponseOneCAPI> response = restTemplate.exchange(
                    urlBuilder.build().toUri(),
                    HttpMethod.DELETE,
                    request,
                    ResponseOneCAPI.class);

            if(response != null && response.getStatusCode() == HttpStatus.OK){
                if(response.getBody() == null){
                    logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE + "'deleteCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + characteristicId + "'");
                }
                return response.getBody();
            }else {
                if(response != null && response.getBody() != null && response.getBody().getData() != null && response.getBody().getStatus() != null
                        && response.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM) != null){
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'deleteCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + characteristicId + "'"
                            + Constants.ONEC_API_ERROR_INTERNAL + "internal status: " + response.getBody().getStatus()
                            + " error: " + response.getBody().getData().get(Constants.ONEC_RESP_ERROR_PARAM).asText());
                }else {
                    throw new ServerException(Constants.ERROR_RECEIVING_REQUEST + "'deleteCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + characteristicId + "'");
                }
            }
        }catch (HttpClientErrorException e){
            throw new ServerException(Constants.ONEC_API_ERROR_INTERNAL + e.toString() + ". From method: " +  "'deleteCharacteristic'" + " from " + "'" + productCode + "'" + " with specification " + "'" + characteristicId + "'"
                    + ". Body: " + e.getResponseBodyAsString());
        }
    }
}
