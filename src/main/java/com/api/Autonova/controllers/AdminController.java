package com.api.Autonova.controllers;

import com.api.Autonova.components.ProcessUpdateStatusManipulator;
import com.api.Autonova.components.cache.CacheUpdateComponent;
import com.api.Autonova.dao.ProductsAdminDao;
import com.api.Autonova.services.external_api.TecDocApiService;
import com.api.Autonova.components.handlers.AttrAnalogListHandler;
import com.api.Autonova.components.handlers.AttrCarListHandler;
import com.api.Autonova.components.handlers.AttrOEListHandler;
import com.api.Autonova.components.handlers.AttrSpecListHandler;
import com.api.Autonova.exceptions.AccessException;
import com.api.Autonova.exceptions.BadRequestException;
import com.api.Autonova.exceptions.NotFoundException;
import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.*;
import com.api.Autonova.models.site.*;
import com.api.Autonova.repository.*;
import com.api.Autonova.services.update.UpdateAmountService;
import com.api.Autonova.services.update.UpdateMainService;
import com.api.Autonova.services.external_api.OneCApiService;
import com.api.Autonova.services.update.UpdatePricesService;
import com.api.Autonova.utils.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping(value = "/administration")
public class AdminController {

    private static final String NOT_CHANGE_VALUE = "DEFAULT NOT CHANGE VALUE";



    @Autowired
    UpdateMainService updateMainService;
    @Autowired
    UpdateAmountService updateAmountService;
    @Autowired
    UpdatePricesService updatePricesService;

    @Autowired
    AttrCarListHandler attrCarListHandler;
    @Autowired
    AttrSpecListHandler attrSpecListHandler;
    @Autowired
    AttrOEListHandler attrOEListHandler;
    @Autowired
    AttrAnalogListHandler attrAnalogListHandler;


    @Autowired
    OneCApiService oneCApiService;
    @Autowired
    TecDocApiService tecDocApiService;


    @Autowired
    CarsRepository carsRepository;
    @Autowired
    ProductCharacteristicsRepository productCharacteristicsRepository;
    @Autowired
    AccessRepository accessRepository;

    @Autowired
    ProductsRepository productsRepository;

    @Autowired
    AttributesPatternsRepository attributesPatternsRepository;

    @Autowired
    ProductAttributesRepository productAttributesRepository;

    @Autowired
    AnalyticsRepository analyticsRepository;

    @Autowired
    PageRepository pageRepository;
    @Autowired
    PageDescriptionRepository pageDescriptionRepository;
    @Autowired
    PageImageRepository pageImageRepository;

    @Autowired
    PartnersRepository partnersRepository;

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CarsProductRepository carsProductRepository;

    @Autowired
    ProductsAdminDao productsAdminDao;

    @Autowired
    OENumbersDataRepository oeNumbersDataRepository;

    @Autowired
    FilterLinkRepository filterLinkRepository;

    @Autowired
    ProcessUpdateStatusManipulator processUpdateStatusManipulator;

    @Autowired
    CacheUpdateComponent cacheUpdateComponent;


    private Logger logger = LoggerFactory.getLogger(AdminController.class);

    //метод для проверки аутентификации
    private boolean checkPermission(String token){
        if(token != null && token.trim().length() > 0 && userRepository.findUserByToken(token) != null &&
                userRepository.findUserByToken(token).isStatus()){
            return true;
        }else {
            throw new AccessException(Constants.ADMIN_TOKEN_EXCEPTIONS);
        }
    }

    //MANIPULATED METHODS
    private void updateProductAttrInDB(@NotNull JsonNode product, String name, String productCode){
        if(product.get(name) != null){
            String attrNewValue = "";
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //for ignore another param in model
            ObjectReader reader = null;
            switch (name){
                case Constants.PRODUCT_ATTR_SPEC_LIST:
                    reader = mapper.readerFor(new TypeReference<List<ProductCharacteristic>>() {});
                    try {
                        List<ProductCharacteristic> characteristics = reader.readValue(product.get(name));
                        if(characteristics != null){
                            attrNewValue = attrSpecListHandler.convertCharacteristicsToString(characteristics);
                        }
                    }catch (IOException e) {}
                    break;
                case Constants.PRODUCT_ATTR_CAR_LIST:
                    reader = mapper.readerFor(new TypeReference<List<Car>>() {});
                    try {
                        List<Car> cars = reader.readValue(product.get(name));
                        if(cars != null){
                            attrNewValue = attrCarListHandler.convertCarsToString(cars);
                        }
                    }catch (IOException e) {}
                    break;
                case Constants.PRODUCT_ATTR_ANALOG_LIST:
                    reader = mapper.readerFor(new TypeReference<List<ProductAnalog>>() {});
                    try {
                        List<ProductAnalog> analogs = reader.readValue(product.get(name));
                        if(analogs != null){
                            attrNewValue = attrAnalogListHandler.convertAnalogsToString(analogs);
                        }
                    }catch (IOException e) {}
                    break;
                case Constants.PRODUCT_ATTR_OE_LIST:
                    reader = mapper.readerFor(new TypeReference<List<ProductOENumber>>() {});
                    try {
                        List<ProductOENumber> oeNumbers = reader.readValue(product.get(name));
                        if(oeNumbers != null){
                            attrNewValue = attrOEListHandler.convertOENumbersToString(oeNumbers);
                        }
                    }catch (IOException e) {}
                    break;
                default:
                    attrNewValue = product.get(name).asText();
                    break;
            }
            ProductAttribute productAttribute = productAttributesRepository.findProductAttributeByNameAndProductCode(name, productCode);
            if(productAttribute != null){
                productAttribute.setValue(attrNewValue);
                productAttributesRepository.save(productAttribute);

                //если мы изменили атрибут связей с авто, нужно переобновиться связи в таблице
                if(productAttribute.getName().equals(Constants.PRODUCT_ATTR_CAR_LIST)){
                    attrCarListHandler.makeCarsToProductLink(productAttribute);
                }

                //очищаем старый кэш
                cacheUpdateComponent.clearProductsDataCache();
            }else {
                logger.error(Constants.ONEC_API_ERROR_FIND_ATTR_PART1 + "'" + name + "'" + " from " + productCode
                        + Constants.ONEC_API_ERROR_FIND_ATTR_IN_DB_FOR_UPDATE);
            }
        }else {
            logger.error(Constants.ONEC_API_ERROR_FIND_ATTR_PART1 + "'" + name + "'" + " from " + productCode
                    + Constants.ONEC_API_ERROR_FIND_ATTR_IN_1C_RESP_FOR_UPDATE);
        }
    }


    //SYSTEM START
    //Star update service
    @RequestMapping(value = "/service/updateMain", method = RequestMethod.POST)
    public void updateSystemMain(@RequestHeader("Authorization") String authToken){
        checkPermission(authToken);
        updateMainService.updateMain();
    }
    //Start update prices service
    @RequestMapping(value = "/service/updatePrices", method = RequestMethod.POST)
    public void updateSystemPrices(@RequestHeader("Authorization") String authToken){
        checkPermission(authToken);
        updatePricesService.update();
    }
    //Star update amount of products service
    @RequestMapping(value = "/service/updateAmount", method = RequestMethod.POST)
    public void updateSystemAmount(@RequestHeader("Authorization") String authToken){
        checkPermission(authToken);
        updateAmountService.update();
    }

    //Get products from XML
    @RequestMapping(value = "/service/getXML", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> generateXML(@RequestHeader("Authorization") String authToken,
            @RequestBody List<String> productsFilter) throws JAXBException, IOException {
        checkPermission(authToken);

        if(!processUpdateStatusManipulator.checkWorkingStatus(Constants.SETTING_SYSTEM_GET_XML_STATUS)) {
            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_GET_XML_STATUS, true);

            //start
            ProductsXML productsXML = new ProductsXML();
            List<ProductsXML.Product> productsListXML = new ArrayList<>();
            List<Product> productsFromDB = new ArrayList<>();
            //set products for return
            if(productsFilter != null && productsFilter.size() > 0){
                productsFromDB = productsRepository.findAllInCodes(productsFilter);
            /*for (String productCode : productsFilter){
                Product product = productsRepository.findProductByCode(productCode);
                if(product != null){
                    Product productItem = new Product();
                    productItem.setCode(productCode);
                    productsFromDB.add(productItem);
                }
            }*/
            }else {
                productsFromDB = (List<Product>) productsRepository.findAll();
            }
            //get all data for current products
            if(productsFromDB != null){
                for(Product productItem : productsFromDB){
                    ProductsXML.Product productItemXML = new ProductsXML.Product();
                    productItemXML.setCode(productItem.getCode());
                    //List<ProductAttribute> attributes = productAttributesRepository.findAllByProductCode(productItem.getCode());
                    List<ProductAttribute> attributes = productItem.getAttributes();
                    if(attributes != null){
                        List<ProductsXML.ProductAttribute> attributesXML = new ArrayList<>();
                        for(ProductAttribute attrItem : attributes){
                            ProductsXML.ProductAttribute attributeXML = new ProductsXML.ProductAttribute(attrItem);
                            attributesXML.add(attributeXML);
                        }
                        productItemXML.setAttributes(attributesXML);
                    }
                    //List<ProductCharacteristic> characteristics = productCharacteristicsRepository.findAllByProductCode(productItem.getCode());
                    List<ProductCharacteristic> characteristics = productItem.getCharacteristics();
                    if(characteristics != null){
                        List<ProductsXML.ProductCharacteristic> characteristicsXML = new ArrayList<>();
                        for(ProductCharacteristic characteristicItem : characteristics){
                            ProductsXML.ProductCharacteristic characteristicXML = new ProductsXML.ProductCharacteristic(characteristicItem);
                            characteristicsXML.add(characteristicXML);
                        }
                        productItemXML.setCharacteristics(characteristicsXML);
                    }
                    productsListXML.add(productItemXML);
                }
            }
            productsXML.setProducts(productsListXML);

            File file = new File("Products.xml");
            //convert to xml
            JAXBContext jaxbContext = JAXBContext.newInstance(ProductsXML.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(productsXML, file);
            //return fille
            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Products.xml");

            Path path = Paths.get(file.getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_GET_XML_STATUS, false);
            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    //.contentType(MediaType.parseMediaType("application/octet-stream"))
                    .contentType(MediaType.APPLICATION_XML)
                    .body(resource);
        }else {
            throw new ServerException(Constants.ERROR_PROCESS_ALREADY_RUNNING);
        }
    }

    //Get products from EXEL
    @RequestMapping(value = "/service/getExcel", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> generateEXEL(@RequestHeader("Authorization") String authToken,
                                                            @RequestBody List<String> productsFilter) throws IOException {
        checkPermission(authToken);

        if(!processUpdateStatusManipulator.checkWorkingStatus(Constants.SETTING_SYSTEM_GET_EXCEL_STATUS)) {
            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_GET_EXCEL_STATUS, true);

            //start
            ProductsXML productsXML = new ProductsXML();
            List<ProductsXML.Product> productsListXML = new ArrayList<>();
            List<Product> productsFromDB = new ArrayList<>();
            //set products for return
            if(productsFilter != null && productsFilter.size() > 0){
                productsFromDB = productsRepository.findAllInCodes(productsFilter);
            }else {
                productsFromDB = (List<Product>) productsRepository.findAll();
            }
            //get all data for current products
            if(productsFromDB != null){
                for(Product productItem : productsFromDB){
                    ProductsXML.Product productItemXML = new ProductsXML.Product();
                    productItemXML.setCode(productItem.getCode());
                    //List<ProductAttribute> attributes = productAttributesRepository.findAllByProductCode(productItem.getCode());
                    List<ProductAttribute> attributes = productItem.getAttributes();
                    if(attributes != null){
                        List<ProductsXML.ProductAttribute> attributesXML = new ArrayList<>();
                        for(ProductAttribute attrItem : attributes){
                            ProductsXML.ProductAttribute attributeXML = new ProductsXML.ProductAttribute(attrItem);
                            attributesXML.add(attributeXML);
                        }
                        productItemXML.setAttributes(attributesXML);
                    }
                    //List<ProductCharacteristic> characteristics = productCharacteristicsRepository.findAllByProductCode(productItem.getCode());
                    List<ProductCharacteristic> characteristics = productItem.getCharacteristics();
                    if(characteristics != null){
                        List<ProductsXML.ProductCharacteristic> characteristicsXML = new ArrayList<>();
                        for(ProductCharacteristic characteristicItem : characteristics){
                            ProductsXML.ProductCharacteristic characteristicXML = new ProductsXML.ProductCharacteristic(characteristicItem);
                            characteristicsXML.add(characteristicXML);
                        }
                        productItemXML.setCharacteristics(characteristicsXML);
                    }
                    productsListXML.add(productItemXML);
                }
            }
            productsXML.setProducts(productsListXML);

            File file = new File("Products.xlsx");

            try
            {
                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet sheet = workbook.createSheet("sheet1");// creating a blank sheet
                int rownum = 0;

                //create title
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                style.setFont(font);
                Row row = sheet.createRow(rownum);
                Cell cell = row.createCell(0);
                cell.setCellValue("product");
                cell.setCellStyle(style);
                cell = row.createCell(1);
                cell.setCellValue("attribute name");
                cell.setCellStyle(style);
                cell = row.createCell(2);
                cell.setCellValue("attribute value");
                cell.setCellStyle(style);
                cell = row.createCell(3);
                cell.setCellValue("characteristic id");
                cell.setCellStyle(style);
                cell = row.createCell(4);
                cell.setCellValue("characteristic name");
                cell.setCellStyle(style);
                cell = row.createCell(5);
                cell.setCellValue("characteristic name ua");
                cell.setCellStyle(style);
                cell = row.createCell(6);
                cell.setCellValue("characteristic short name");
                cell.setCellStyle(style);
                cell = row.createCell(7);
                cell.setCellValue("characteristic short name ua");
                cell.setCellStyle(style);
                cell = row.createCell(8);
                cell.setCellValue("characteristic unit");
                cell.setCellStyle(style);
                cell = row.createCell(9);
                cell.setCellValue("characteristic value");
                cell.setCellStyle(style);
                cell = row.createCell(10);
                cell.setCellValue("characteristic value ua");
                cell.setCellStyle(style);

                //make file
                for (ProductsXML.Product productItem : productsXML.getProducts()) {
                    rownum++;
                    row = sheet.createRow(rownum);
                    cell = row.createCell(0);
                    cell.setCellValue(productItem.getCode());

                    for(ProductsXML.ProductAttribute attrItem : productItem.getAttributes()){
                        rownum++;
                        row = sheet.createRow(rownum);
                        cell = row.createCell(1);
                        cell.setCellValue(attrItem.getName());
                        cell = row.createCell(2);
                        cell.setCellValue(attrItem.getValue());
                    }
                    for(ProductsXML.ProductCharacteristic charactItem : productItem.getCharacteristics()){
                        rownum++;
                        row = sheet.createRow(rownum);
                        cell = row.createCell(3);
                        cell.setCellValue(charactItem.getId());
                        cell = row.createCell(4);
                        cell.setCellValue(charactItem.getAttrName());
                        cell = row.createCell(5);
                        cell.setCellValue(charactItem.getAttrNameUa());
                        cell = row.createCell(6);
                        cell.setCellValue(charactItem.getAttrShortName());
                        cell = row.createCell(7);
                        cell.setCellValue(charactItem.getAttrShortNameUa());
                        cell = row.createCell(8);
                        cell.setCellValue(charactItem.getAttrUnit());
                        cell = row.createCell(9);
                        cell.setCellValue(charactItem.getAttrValue());
                        cell = row.createCell(10);
                        cell.setCellValue(charactItem.getAttrValueUa());
                    }
                }

                FileOutputStream out = new FileOutputStream(file); // file name with path
                workbook.write(out);
                out.close();

            }catch (Exception e){
                e.printStackTrace();
                processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_GET_EXCEL_STATUS, false);
            }

            //return fille
            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Products.xlsx");

            Path path = Paths.get(file.getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            processUpdateStatusManipulator.updateWorkingStatus(Constants.SETTING_SYSTEM_GET_EXCEL_STATUS, false);
            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    //.contentType(MediaType.parseMediaType("application/octet-stream"))
                    //.contentType(MediaType.APPLICATION_XML)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    //.contentType(MediaType.parseMediaType("application/vnd.openxmlformats"))
                    //.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(resource);
        }else {
            throw new ServerException(Constants.ERROR_PROCESS_ALREADY_RUNNING);
        }
    }

//ACCESS
    //set external api`s access
    @RequestMapping(value = "/access", method = RequestMethod.POST)
    public void setExternalAccess(@RequestHeader("Authorization") String authToken,
                                     @RequestParam String name, @RequestParam String token,
                                     @RequestParam String login, @RequestParam String pass){
        checkPermission(authToken);

        if(login.trim().length() > 0){
            login = AesEncriptionUtil.encrypt(login);
        }
        if(pass.trim().length() > 0){
            pass = AesEncriptionUtil.encrypt(pass);
        }

        ExternalAccess access = accessRepository.findByName(name);
        if(access != null){
            access.setLogin(login);
            access.setPass(pass);
            access.setToken(token);
            accessRepository.save(access);
        }else{
            //add new access
            ExternalAccess access1 = new ExternalAccess();
            access1.setName(name);
            access1.setLogin(login);
            access1.setPass(pass);
            access1.setToken(token);
            accessRepository.save(access1);
        }
    }


//ATTRIBUTE
    //add new attribute  NOT WORK
    /*@RequestMapping(value = "/attribute", method = RequestMethod.POST)
    public boolean addArticle(@RequestHeader("Authorization") String token, @RequestParam String name){
        checkPermission(token);
        return oneCApiService.addAttribute(name);
    }*/
    //update attribute   NOT WORK
    /*@RequestMapping(value = "/attribute/{name}", method = RequestMethod.POST)
    public boolean updateArticle(@RequestHeader("Authorization") String token,
                                 @PathVariable(value = "name") String name, @RequestParam String newName){
        checkPermission(token);
        if(newName.trim().length() > 0){
            return oneCApiService.updateAttribute(name, newName.trim());
        }else {
            throw new BadRequestException(Constants.ERROR_WRONG_INPUT_PARAM + "'newName'");
        }
    }*/
    //update attribute in current product
    @RequestMapping(value = "/attribute/{name}/{code}", method = RequestMethod.POST)
    public JsonNode updateProductAttribute(@RequestHeader("Authorization") String token,
                                         @PathVariable(value = "name") String name, @PathVariable(value = "code") String code,
                                         @RequestParam String value){
        checkPermission(token);

        oneCApiService.updateAccess();
        JsonNode updatedProduct = oneCApiService.updateProductAttribute(code, name.trim(), value.trim());

        if(updatedProduct != null){
            updateProductAttrInDB(updatedProduct, name, code);
        }else {
            logger.error(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_SAVE + "'updateProductAttribute'" + " from " + "'" + code + "'" + " with attr " + "'" + name + "'");
        }
        return updatedProduct;
    }


//CARS
/*
    @RequestMapping(value = "/car", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public User addCar(@RequestHeader("Authorization") String token,
                                  @RequestParam int carId, @RequestParam String constructionType, @RequestParam String constructionTypeUa,
                                  @RequestParam String fuelType, @RequestParam String fuelTypeUa, @RequestParam int manuId,
                                  @RequestParam String manuName, @RequestParam int modId, @RequestParam String modelName,
                                  @RequestParam String typeName, @RequestParam String yearOfConstrFrom, @RequestParam String yearOfConstrTo){
        //ВОЗМОЖНО НЕ ВСЕ ПАРАМЕТРЫ ОБЯЗАТЕЛЬНЫЕ МОЖНО ВВЕСТИ ПУСТЫЕ ЗНАЧЕНИЯ
        checkPermission(token);
        User car =  oneCApiService.addCar(carId, constructionType.trim(),  constructionTypeUa.trim(), fuelType.trim(),
                fuelTypeUa.trim(), manuId, manuName.trim(), modId, modelName.trim(), typeName.trim(),
                yearOfConstrFrom.trim(), yearOfConstrTo.trim());

        if(car != null){
            //find car in DB
            //find for carId  NOT id
            User carInDB = carsRepository.findCarByCarId(car.getCarId());
            if(carInDB != null){
                //update old
                //carInDB.setCarId(car.getCarId());
                carInDB.setConstructionType(car.getConstructionType());
                carInDB.setConstructionTypeUa(car.getConstructionTypeUa());
                carInDB.setFuelType(car.getFuelType());
                carInDB.setFuelTypeUa(car.getFuelTypeUa());
                carInDB.setManuId(car.getManuId());
                carInDB.setManuName(car.getManuName());
                carInDB.setModId(car.getModId());
                carInDB.setModelName(car.getModelName());
                carInDB.setTypeName(car.getTypeName());
                carInDB.setYearOfConstrFrom(car.getYearOfConstrFrom());
                carInDB.setYearOfConstrTo(car.getYearOfConstrTo());
                carsRepository.save(carInDB);
            }else {
                //save new
                carsRepository.save(car);
            }
            return car;
        }else {
            //Ошибка из 1С АПИ нельзя обработать ответ для сохранения локально
            throw new ServerException(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_SAVE);
        }
    }

    @RequestMapping(value = "/car/{id}", method = RequestMethod.POST)
    public User updateCar(@RequestHeader("Authorization") String token,
                             @PathVariable(value = "id") int carId,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String manuName,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String yearOfConstrFrom,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String yearOfConstrTo,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String modelName,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String typeName,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String constructionType,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String constructionTypeUa,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String fuelType,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE) String fuelTypeUa,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE_INT) int manuId,
                             @RequestParam(defaultValue = NOT_CHANGE_VALUE_INT) int modId
                             ){

        checkPermission(token);

        User car = carsRepository.findCarByCarId(carId);
        if(car != null){
            if(!manuName.equals(NOT_CHANGE_VALUE)){
                car.setManuName(manuName);
            }
            if(!yearOfConstrFrom.equals(NOT_CHANGE_VALUE)){
                car.setYearOfConstrFrom(yearOfConstrFrom);
            }
            if(!yearOfConstrTo.equals(NOT_CHANGE_VALUE)){
                car.setYearOfConstrTo(yearOfConstrTo);
            }
            if(!modelName.equals(NOT_CHANGE_VALUE)){
                car.setModelName(modelName);
            }
            if(!typeName.equals(NOT_CHANGE_VALUE)){
                car.setTypeName(typeName);
            }
            if(!constructionType.equals(NOT_CHANGE_VALUE)){
                car.setConstructionType(constructionType);
            }
            if(!constructionTypeUa.equals(NOT_CHANGE_VALUE)){
                car.setConstructionTypeUa(constructionTypeUa);
            }
            if(!fuelType.equals(NOT_CHANGE_VALUE)){
                car.setFuelType(fuelType);
            }
            if(!fuelTypeUa.equals(NOT_CHANGE_VALUE)){
                car.setFuelTypeUa(fuelTypeUa);
            }
            if(manuId != Integer.parseInt(NOT_CHANGE_VALUE_INT)){
                car.setManuId(manuId);
            }
            if(modId != Integer.parseInt(NOT_CHANGE_VALUE_INT)){
                car.setModId(modId);
            }

            //if update in 1C = true
            User updatedCar = oneCApiService.updateCar(car);
            if(updatedCar != null){
                //UPDATE IN DB
                updatedCar.setCarId(car.getCarId());
                carsRepository.save(updatedCar);
                return updatedCar;
            }else {
                //Ошибка из 1С АПИ нельзя обработать ответ для ОБНОВЛЕНИЯ локально
                throw new ServerException(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE);
            }
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/car/{id}", method = RequestMethod.DELETE)
    public ResponseOneCAPI deleteCar(@RequestHeader("Authorization") String token, @PathVariable(value = "id") int carId){
        checkPermission(token);
        //ВОЗМОЖНО нужно переобновлять все атрибуты применимости где встречался этот carId НО возможно не надо это делать ибо нагрузит систему, а потом и так переобновится
        User car = carsRepository.findCarByCarId(carId);
        if(car != null){
            ResponseOneCAPI response = oneCApiService.deleteCar(car.getCarId());
            //if dell in 1C = true
            if(response != null){
                //del car in BD
                carsRepository.deleteByCarId(carId);
                return response;
            }else {
                //car not del in DB
                throw new ServerException(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_DELETE);
            }
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }*/


//CHARACTERISTIC
    @RequestMapping(value = "/characteristic", method = RequestMethod.GET)
    public List<ProductCharacteristic> getCharacteristic(@RequestHeader("Authorization") String token,
                                                         @RequestParam(defaultValue = "") String query,
                                                         @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int limit) {
        checkPermission(token);
        List<ProductCharacteristic> characteristics = productCharacteristicsRepository.getUniqueCharacteristicsModelByAttrShortName(new PageRequest(Math.max(page - 1, 0), limit));
        if(characteristics != null){
            //filter
            if(query.trim().length() > 0){
                Iterator<ProductCharacteristic> it = characteristics.iterator();
                while (it.hasNext()) {
                    ProductCharacteristic attrItem = it.next();
                    if (!FilterUtil.searchName(attrItem.getAttrShortName(), query.trim()) && !FilterUtil.searchName(attrItem.getAttrShortNameUa(), query.trim())) {
                        it.remove();
                    }
                }
            }
            return characteristics;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }

    }

    @RequestMapping(value = "/characteristic/{code}", method = RequestMethod.POST)
    public ProductCharacteristic addCharacteristic(@RequestHeader("Authorization") String token,
                                     @PathVariable(value = "code") String code,
                                     @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrName,
                                     @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrNameUa,
                                     @RequestParam String attrShortName,
                                     @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrShortNameUa,
                                     @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrUnit,
                                     @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrValue,
                                     @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrValueUa){

        checkPermission(token);
        oneCApiService.updateAccess();

        Product product = productsRepository.findProductByCode(code);
        if(product == null){
            throw new NotFoundException(Constants.ERROR_PRODUCT_NOT_FOUND);
        }
        if(attrShortName.trim().length() > 0){
            ProductCharacteristic characteristicUpdated = new ProductCharacteristic();
            characteristicUpdated.setAttrShortName(attrShortName.trim());
            if(!attrName.equals(NOT_CHANGE_VALUE)){
                characteristicUpdated.setAttrName(attrName.trim());
            }
            if(!attrNameUa.equals(NOT_CHANGE_VALUE)){
                characteristicUpdated.setAttrNameUa(attrNameUa.trim());
            }
            if(!attrShortNameUa.equals(NOT_CHANGE_VALUE)){
                characteristicUpdated.setAttrShortNameUa(attrShortNameUa.trim());
            }
            if(!attrUnit.equals(NOT_CHANGE_VALUE)){
                characteristicUpdated.setAttrUnit(attrUnit.trim());
            }
            if(!attrValue.equals(NOT_CHANGE_VALUE)){
                characteristicUpdated.setAttrValue(attrValue.trim());
            }
            if(!attrValueUa.equals(NOT_CHANGE_VALUE)){
                characteristicUpdated.setAttrValueUa(attrValueUa.trim());
            }

            ProductCharacteristic characteristicNew = oneCApiService.addCharacteristic(code.trim(), characteristicUpdated);
            //if change in 1C  UPDATE IN DB
            if(characteristicNew != null){
                //find from BD
                ProductCharacteristic characteristicDB = productCharacteristicsRepository.findByProductCodeAndId(code, characteristicNew.getId());
                if(characteristicDB != null){
                    //update old
                    characteristicNew.setLocalid(characteristicDB.getLocalid());
                    characteristicNew.setProductCode(characteristicDB.getProductCode());
                    characteristicNew.setProduct(product);
                    productCharacteristicsRepository.save(characteristicNew);
                }else {
                    //save new
                    characteristicNew.setProductCode(code);
                    characteristicNew.setProduct(product);
                    productCharacteristicsRepository.save(characteristicNew);
                }
                //update in product attr specifications
                JsonNode productData = oneCApiService.getProductData(code);
                if(productData != null){
                    updateProductAttrInDB(productData, Constants.PRODUCT_ATTR_SPEC_LIST, code);
                }
                return characteristicNew;
            }else {
                throw new ServerException(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_SAVE);
            }
        }else {
            throw new BadRequestException(Constants.ERROR_WRONG_INPUT_PARAM + " " + "attrShortName");
        }
    }

    @RequestMapping(value = "/characteristic/{code}/{id}", method = RequestMethod.POST)
    public ProductCharacteristic updateCharacteristic(@RequestHeader("Authorization") String token,
                                        @PathVariable(value = "code") String code,
                                        @PathVariable(value = "id") int id,
                                                      @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrName,
                                                      @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrNameUa,
                                                      @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrShortName,
                                                      @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrShortNameUa,
                                                      @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrUnit,
                                                      @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrValue,
                                                      @RequestParam(defaultValue = NOT_CHANGE_VALUE) String attrValueUa){

        checkPermission(token);
        oneCApiService.updateAccess();

        Product product = productsRepository.findProductByCode(code);
        if(product == null){
            throw new NotFoundException(Constants.ERROR_PRODUCT_NOT_FOUND);
        }
        ProductCharacteristic characteristicDB = productCharacteristicsRepository.findByProductCodeAndId(code, id);
        if(characteristicDB != null){
            if(!attrName.equals(NOT_CHANGE_VALUE) ){
                characteristicDB.setAttrName(attrName.trim());
            }
            if(!attrNameUa.equals(NOT_CHANGE_VALUE) ){
                characteristicDB.setAttrNameUa(attrNameUa.trim());
            }
            if(!attrShortName.equals(NOT_CHANGE_VALUE) && attrShortName.trim().length() > 0){
                characteristicDB.setAttrShortName(attrShortName.trim());
            }
            if(!attrShortNameUa.equals(NOT_CHANGE_VALUE) ){
                characteristicDB.setAttrShortNameUa(attrShortNameUa.trim());
            }
            if(!attrUnit.equals(NOT_CHANGE_VALUE)){
                characteristicDB.setAttrUnit(attrUnit.trim());
            }
            if(!attrValue.equals(NOT_CHANGE_VALUE)){
                characteristicDB.setAttrValue(attrValue.trim());
            }
            if(!attrValueUa.equals(NOT_CHANGE_VALUE)){
                characteristicDB.setAttrValueUa(attrValueUa.trim());
            }

            ProductCharacteristic updatedCharacteristic = oneCApiService.updateCharacteristic(code, characteristicDB);
            //if update in 1C = true
            if(updatedCharacteristic != null){
                updatedCharacteristic.setLocalid(characteristicDB.getLocalid());
                updatedCharacteristic.setProductCode(characteristicDB.getProductCode());
                updatedCharacteristic.setProduct(product);
                productCharacteristicsRepository.save(updatedCharacteristic);
                return updatedCharacteristic;
            }else {
                throw new ServerException(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE);
            }
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/characteristic/{code}/{id}", method = RequestMethod.DELETE)
    public ResponseOneCAPI deleteCharacteristic(@RequestHeader("Authorization") String token,
                                                @PathVariable(value = "code") String code, @PathVariable(value = "id") int id){
        checkPermission(token);
        oneCApiService.updateAccess();

        ProductCharacteristic productCharacteristic = productCharacteristicsRepository.findByProductCodeAndId(code, id);
        if(productCharacteristic != null){
            ResponseOneCAPI response = oneCApiService.deleteCharacteristic(code, id);
            //if dell in 1C = true
            if(response != null){
                //del car in BD
                productCharacteristicsRepository.deleteByProductCodeAndId(code, id);
                //update in product attr specifications
                JsonNode productData = oneCApiService.getProductData(code);
                if(productData != null){
                    updateProductAttrInDB(productData, Constants.PRODUCT_ATTR_SPEC_LIST, code);
                }
                return response;
            }else {
                //car not del in DB
                throw new ServerException(Constants.ONEC_API_ERROR_CAN_NOT_READ_AND_DELETE);
            }
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }


//REQUESTS FOR ADMIN PANEL AND SITE (requests do not touch 1C API)

//PRODUCT
    @RequestMapping(value = "/product", method = RequestMethod.POST)
    public void addProduct(@RequestHeader("Authorization") String token, @RequestBody List<String> products){
        checkPermission(token);
        updateMainService.updateByCodes(products);
    }

    @RequestMapping(value = "/product/{code}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getProduct(@RequestHeader("Authorization") String token, @PathVariable(value = "code") String code,
                             @RequestParam(defaultValue = "1") String page, @RequestParam(defaultValue = "9") String limit) throws JSONException {
        checkPermission(token);
        Product product = productsRepository.findProductByCode(code);
        if(product != null){
            ObjectMapper objectMapper = new ObjectMapper();
            JSONObject resp = new JSONObject();
            resp.put("id", product.getId());
            resp.put("updateTime", product.getUpdateTime() != null && product.getUpdateTime().length() > 19 ? product.getUpdateTime().substring(0, 19) : product.getUpdateTime());
            //attrs
            //List<ProductAttribute> attributes = productAttributesRepository.findAllByProductCode(code);
            List<ProductAttribute> attributes = product.getAttributes();
            JSONArray attributesJson = new JSONArray();
            if(attributes != null){
                List<AttributesPattern> attributesPatternsList = (List) attributesPatternsRepository.findAll();
                ListSearchUtil searchUtil = new ListSearchUtil();
                for(ProductAttribute attrItem : attributes){
                    JSONObject objectAttrs = new JSONObject();
                    objectAttrs.put("id", attrItem.getId());
                    objectAttrs.put("name", attrItem.getName());
                    objectAttrs.put("value", attrItem.getValue());
                    //AttributesPattern attributesPatternItem = attributesPatternsRepository.findAttributesPatternByName(attrItem.getName());
                    AttributesPattern attributesPatternItem = searchUtil.findAttrPatternByName(attributesPatternsList, attrItem.getName());
                    if(attributesPatternItem != null){
                        objectAttrs.put("writeAbility", attributesPatternItem.isWriteAbility());
                    }else {
                        objectAttrs.put("writeAbility", "false");
                    }

                    attributesJson.put(objectAttrs);
                }
            }
            resp.put("attributes", attributesJson);
            //characteristics
            //List<ProductCharacteristic> characteristics = productCharacteristicsRepository.findAllByProductCode(code);
            List<ProductCharacteristic> characteristics = product.getCharacteristics();
            JSONArray characteristicsJson = new JSONArray();
            /*if(characteristics != null){
                for(ProductCharacteristic charItem : characteristics){
                    JSONObject objectAttrs = new JSONObject();
                    objectAttrs.put("id", charItem.getId());
                    objectAttrs.put("name", charItem.getAttrName());
                    objectAttrs.put("nameUa", charItem.getAttrShortNameUa());
                    objectAttrs.put("value", charItem.getAttrValue());
                    objectAttrs.put("valueUa", charItem.getAttrValueUa());
                    objectAttrs.put("unit", charItem.getAttrUnit());
                    characteristicsJson.put(objectAttrs);
                }
            }*/
            try {
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(characteristics);
                characteristicsJson = new JSONArray(json);
            } catch(Exception e) {
                e.printStackTrace();
            }

            resp.put("characteristics", characteristicsJson);

            //cars
            JSONArray carsJson = new JSONArray();
            try {
                //String carsStr = new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_CAR_LIST);
                //List<Integer> carList = AttributeParseUtil.parseListAttrFromDBToListInt(carsStr);
                //List<Car> cars = carsRepository.findAllForProduct(carList.isEmpty() ? null : carList);
                List<Car> cars = new ArrayList<Car>(product.getCars());
                //List<Car> cars = productsRepository.findCarsByProduct(product, new PageRequest(0, 1));
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cars);
                carsJson = new JSONArray(json);
            } catch(Exception e) {
                e.printStackTrace();
            }
            resp.put("cars", carsJson);

            //analogs
            JSONArray analogsJson = new JSONArray();
            try {
                String analogsStr = new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_ANALOG_LIST);
                List<String> analogsList = AttributeParseUtil.parseListAttrFromDB(analogsStr);
                List<ProductAttribute> attributesForAnalog = productAttributesRepository.findAttrsForAnalog(analogsList.size() > 0 ? analogsList : null,
                            Constants.PRODUCT_ATTR_NAME, Constants.PRODUCT_ATTR_NAME_UA);
                for(String analogItem : analogsList){
                    JSONObject objectAnalog = new JSONObject();
                    objectAnalog.put(Constants.PRODUCT_ATTR_CODE, analogItem);
                    //List<ProductAttribute> productAnalogAttrs = productAttributesRepository.findAllByProductCode(analogItem);
                    if(attributesForAnalog != null && attributesForAnalog.size() > 0){
                        List<ProductAttribute> attributesCurrent = new ListSearchUtil().cutAttsByProductCode(attributesForAnalog, analogItem);
                        if(attributesCurrent != null && attributesCurrent.size() > 0){
                            for(ProductAttribute attributeItem : attributesCurrent){
                                if(attributeItem.getName().equals(Constants.PRODUCT_ATTR_NAME)){
                                    objectAnalog.put(Constants.PRODUCT_ATTR_NAME, attributeItem.getValue());
                                }else if(attributeItem.getName().equals(Constants.PRODUCT_ATTR_NAME_UA)){
                                    objectAnalog.put(Constants.PRODUCT_ATTR_NAME_UA, attributeItem.getValue());
                                }
                            }
                            analogsJson.put(objectAnalog);
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            resp.put("analogs", analogsJson);
            return resp.toString();
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/product/{code}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateProduct(@RequestHeader("Authorization") String token, @PathVariable(value = "code") String code,
                                @RequestBody JsonNode updateProductData) throws JSONException {
        checkPermission(token);
        Product product = productsRepository.findProductByCode(code);
        if(product != null){
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //for ignore another param in model
            ObjectReader reader = null;
            if(updateProductData.get("attributes") != null){
                try {
                    reader = mapper.readerFor(new TypeReference<List<ProductAttribute>>() {});
                    List<ProductAttribute> attributes = reader.readValue(updateProductData.get("attributes"));
                    for(ProductAttribute attributeItem : attributes){
                        try{
                            updateProductAttribute(token, attributeItem.getName(), code, attributeItem.getValue());
                        }catch (ServerException e){
                            logger.error("Attribute" + " '" + attributeItem.getName() + "'" +
                                    Constants.ERROR_PART_FROM_PRODUCT + "'" + code + "' " + "not updated. " + e.getMessage());
                        }
                    }
                } catch (IOException e) {}
            }
            if(updateProductData.get("characteristics") != null){
                try {
                    reader = mapper.readerFor(new TypeReference<List<ProductCharacteristic>>() {});
                    List<ProductCharacteristic> characteristics = reader.readValue(updateProductData.get("characteristics"));
                    for(ProductCharacteristic characteristicItem : characteristics){
                        try{
                            updateCharacteristic(token, code, characteristicItem.getId(),
                                    characteristicItem.getAttrName(),
                                    characteristicItem.getAttrNameUa(),
                                    characteristicItem.getAttrShortName(),
                                    characteristicItem.getAttrShortNameUa(),
                                    characteristicItem.getAttrUnit(),
                                    characteristicItem.getAttrValue(),
                                    characteristicItem.getAttrValueUa()
                            );
                        }catch (ServerException | NotFoundException e){
                            logger.error("Сharacteristic" + " '" + characteristicItem.getId() + "'" +
                                    Constants.ERROR_PART_FROM_PRODUCT + "'" + code + "' " + "not updated. " + e.getMessage());
                        }
                    }
                } catch (IOException e) {}
            }
//            return getProduct(token, code);
            return null;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/product/editGroup", method = RequestMethod.POST)
    public void updateProductsGroup(@RequestHeader("Authorization") String token, @RequestBody JsonNode updateProductData) {
        checkPermission(token);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //for ignore another param in model

        if(updateProductData.get("codes") != null){
            ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {});
            try {
                List<String> productCodes = reader.readValue(updateProductData.get("codes"));
                for (String code : productCodes){
                    try {
                        updateProduct(token, code, updateProductData);
                    }catch (NotFoundException | JSONException e){
                        logger.error(e.getMessage() + ": product with code " + "'" + code + "'");
                    }
                }
            } catch (IOException e) {
                throw new BadRequestException(Constants.ERROR_WRONG_INPUT_PARAM + "'codes'");
            }
        }
    }

    @RequestMapping(value = "/product/editGroup/filter", method = RequestMethod.POST)
    public void updateProductsGroup(@RequestHeader("Authorization") String token, @RequestBody JsonNode updateProductData,
                                    @RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                    @RequestParam(defaultValue = "") String code, @RequestParam(defaultValue = "") String name,
                                    @RequestParam(defaultValue = "") String category, @RequestParam(defaultValue = "") String manufacturer,
                                    @RequestParam(defaultValue = "") String amount, @RequestParam(defaultValue = "") String priceFrom,
                                    @RequestParam(defaultValue = "") String priceTo) {
        checkPermission(token);
        List<String> products = productsAdminDao.findProductsCodesByFilter(language, code, name, category, manufacturer, amount, priceFrom, priceTo);
        if(products != null){
            for (String codeItem : products){
                try {
                    updateProduct(token, codeItem, updateProductData);
                }catch (NotFoundException | JSONException e){
                    logger.error(e.getMessage() + ": product with code " + "'" + code + "'");
                }
            }
        }
    }

    //Delete only from catalog (not dell in 1C)
    @RequestMapping(value = "/product/{code}", method = RequestMethod.DELETE)
    public void deleteProduct(@RequestHeader("Authorization") String token, @PathVariable(value = "code") String code) {
        checkPermission(token);
        Product product = productsRepository.findProductByCode(code);
        if(product != null){
            //dell all attrs
            productAttributesRepository.deleteAllByProduct(product);
            //dell all characteristics
            productCharacteristicsRepository.deleteAllByProduct(product);
            //dell all link product - cars
            carsProductRepository.deleteAllByProductId(product.getId());
            //dell all oeList data for product
            oeNumbersDataRepository.deleteAllByParentCode(product.getCode());
            //dell product
            productsRepository.deleteByCode(code);
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    //Delete only from catalog (not dell in 1C)
    @RequestMapping(value = "/product/deleteGroup", method = RequestMethod.DELETE)
    public void deleteProductsGroup(@RequestHeader("Authorization") String token, @RequestBody List<String> productDeleteCodes) {
        checkPermission(token);
        for(String code : productDeleteCodes){
            try{
                deleteProduct(token, code);
            }catch (NotFoundException e){
                logger.error("Product " + "'" + code + "'" + " not deleted. " + e.getMessage());
            }
        }
    }

    //Delete only from catalog (not dell in 1C)
    @RequestMapping(value = "/product/deleteAll", method = RequestMethod.DELETE)
    public void deleteProductsAll(@RequestHeader("Authorization") String token) {
        checkPermission(token);
        //dell all attrs
        productAttributesRepository.deleteAll();
        //dell all characteristics
        productCharacteristicsRepository.deleteAll();
        //dell all link product - cars
        carsProductRepository.deleteAll();
        //dell all oeList data for product
        oeNumbersDataRepository.deleteAll();
        //dell product
        productsRepository.deleteAll();
    }

    @RequestMapping(value = "/product/filter", method = RequestMethod.GET)
    public ResponceProductsByFilter getProductsByFilter(@RequestHeader("Authorization") String token,
                                                        @RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                                        @RequestParam(defaultValue = "") String code, @RequestParam(defaultValue = "") String name,
                                                        @RequestParam(defaultValue = "") String category, @RequestParam(defaultValue = "") String manufacturer,
                                                        @RequestParam(defaultValue = "") String amount, @RequestParam(defaultValue = "") String priceFrom,
                                                        @RequestParam(defaultValue = "") String priceTo,
                                                        @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int limit,
                                                        @RequestParam(defaultValue = "name") String order, @RequestParam(defaultValue = Constants.SORT_VECTOR_ASC) String sort) {
        checkPermission(token);
        //List<Product> products = (List<Product>) productsRepository.findAll();
        List<Product> products = productsAdminDao.findProductsByFilter(language, code, name, category, manufacturer, amount, priceFrom, priceTo, sort, order, limit * Math.max(page-1, 0), limit);
        if(products != null){

//ВОЗМОЖНО заменить запрос - нужно получать только количество
            //int allProductCount = productsDAO.findProductsCountByFilterAdmin(language, code, name, category, manufacturer, amount, priceFrom, priceTo).intValue();
            int allProductCount = productsAdminDao.findProductsIdsByFilter(language, code, name, category, manufacturer, amount, priceFrom, priceTo).size();

            ResponceProductsByFilter responceProductsByFilter = new ResponceProductsByFilter();

            List<ProductForAdmin> productsData = new ArrayList<>();
            for(Product productItem : products){
                productsData.add(makeProductForAdmin(language, productItem));
            }


            //pagination model
            limit = limit > 0 ? limit : 9;
            PaginationModel paginationModel = new PaginationModel();
            paginationModel.setTotalItems(allProductCount);
            paginationModel.setLimit(limit);
            //all pages
            int allPages = 0;
            if(productsData.size() > 0) {
                allPages = allProductCount / Math.min(productsData.size(), limit);
                if (productsData.size() % Math.min(productsData.size(), limit) > 0) {
                    allPages += 1;
                }
            }
            paginationModel.setPages(allPages);
            page = page > 0 ? page : 1;
            paginationModel.setCurrentPage(page);
            paginationModel.setOrder(order);
            paginationModel.setSort(sort);

            //make return
            responceProductsByFilter.setProducts(productsData);
            responceProductsByFilter.setPagination(paginationModel);
            return responceProductsByFilter;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/product/searchProducts", method = RequestMethod.GET)
    public  List<ProductForAdmin> searchProducts(@RequestHeader("Authorization") String token,
                                                   @RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                                   @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "3") int limit) {
        checkPermission(token);

        //List<Product> products = (List<Product>) productsRepository.findAll();
        List<Product> products = null;
        if(language.equals(Constants.LANGUAGE_RU)){
            products = (List<Product>) productsRepository.findProductsByNameOrCode(Constants.PRODUCT_ATTR_NAME, Constants.PRODUCT_ATTR_CODE, "%" + query + "%", new PageRequest(0, Math.max(limit, 1)));
        }else {
            products = (List<Product>) productsRepository.findProductsByNameOrCode(Constants.PRODUCT_ATTR_NAME_UA, Constants.PRODUCT_ATTR_CODE, "%" + query + "%", new PageRequest(0, Math.max(limit, 1)));
        }

        if(products != null){
            List<ProductForAdmin> productsFromDB = new ArrayList<>();
            for(Product productItem : products){
                productsFromDB.add(makeProductForAdmin(language, productItem));
            }
            return  productsFromDB;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }



///CARS
    @RequestMapping(value = "/car/{car_id}", method = RequestMethod.GET)
    public  Car getCar(@RequestHeader("Authorization") String token,
                                                 @RequestParam(defaultValue = Constants.LANGUAGE_UA) String language,
                                                 @PathVariable(value = "car_id") int car_id) {
        checkPermission(token);
        Car car = carsRepository.findCarByCarId(car_id);
        if(car != null){
            return car;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/car/searchCars", method = RequestMethod.GET)
    public ResponceSearchCars searchCars(@RequestHeader("Authorization") String token,
                                         @RequestParam(defaultValue = "") String query,
                                         @RequestParam(defaultValue = "3") int limit) {
        checkPermission(token);
        ResponceSearchCars responceSearchCars = new ResponceSearchCars();
        List<Car> resultsByCarId = new ArrayList<>();
        List<Car> resultsByManuName = new ArrayList<>();
        List<Car> resultsByModelName = new ArrayList<>();
        try{
            resultsByCarId = carsRepository.findAllByCarIdLike(Integer.parseInt(query), new PageRequest(0, Math.max(limit, 3)));
        }catch (NumberFormatException e){}
        if(query.trim().length() > 0){
            query = "%" + query.trim() + "%";
            resultsByManuName = carsRepository.findAllByManuNameLike(query, new PageRequest(0, Math.max(limit, 1)));
            resultsByModelName = carsRepository.findAllByModelNameLike(query, new PageRequest(0, Math.max(limit, 1)));
        }

        //make responce
        responceSearchCars.setResultsByCarId(resultsByCarId);
        responceSearchCars.setResultsByManuName(resultsByManuName);
        responceSearchCars.setResultsByModelName(resultsByModelName);
        return responceSearchCars;
    }


///ATTRS PATTERN (ALL ATTRIBUTES)
    @RequestMapping(value = "/attribute", method = RequestMethod.GET)
    public List<AttributesPattern> getAttributesPattern(@RequestHeader("Authorization") String token,
                       @RequestParam(defaultValue = "") String query) {
        checkPermission(token);
        List<AttributesPattern> patternList = (List<AttributesPattern>) attributesPatternsRepository.findAll();
        if(patternList != null){
            //filter
            if(query.trim().length() > 0){
                Iterator<AttributesPattern> it = patternList.iterator();
                while (it.hasNext()) {
                    AttributesPattern attrItem = it.next();
                    if (!FilterUtil.searchName(attrItem.getName(), query.trim())) {
                        it.remove();
                    }
                }
            }
            return patternList;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/attribute/{name}", method = RequestMethod.POST)
    public List<AttributesPattern>  updateAttributePatternItem(@RequestHeader("Authorization") String token, @PathVariable(value = "name") String name,
                                           @RequestParam(defaultValue = Constants.DEFAULT_NOT_USE_VALUE) String title,
                                           @RequestParam(defaultValue = Constants.DEFAULT_NOT_USE_VALUE) String title_ua,
                                           @RequestParam(defaultValue = Constants.DEFAULT_NOT_USE_VALUE) String filter,
                                           @RequestParam(defaultValue = Constants.DEFAULT_NOT_USE_VALUE) String write_ability) {
        checkPermission(token);

        AttributesPattern attributesPattern = attributesPatternsRepository.findAttributesPatternByName(name);
        if(attributesPattern != null){
            if(!title.equals(Constants.DEFAULT_NOT_USE_VALUE)){
                attributesPattern.setTitle(title);
            }
            if(!title_ua.equals(Constants.DEFAULT_NOT_USE_VALUE)){
                attributesPattern.setTitleUa(title_ua);
            }
            if(!filter.equals(Constants.DEFAULT_NOT_USE_VALUE)){
                attributesPattern.setFilter(Boolean.parseBoolean(filter));
            }
            if(!write_ability.equals(Constants.DEFAULT_NOT_USE_VALUE)){
                attributesPattern.setWriteAbility(Boolean.parseBoolean(write_ability));
            }
            attributesPatternsRepository.save(attributesPattern);
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
        return getAttributesPattern(token, "");
    }



///MANUFACTURERS
    @RequestMapping(value = "/manufacturers", method = RequestMethod.GET)
    public List<Manufacturer> getManufacturers(@RequestHeader("Authorization") String token,
                                                        @RequestParam(defaultValue = "") String query) {
        checkPermission(token);
        oneCApiService.updateAccess();

        List<Manufacturer> manufacturers = oneCApiService.getManufacturers();
        if(manufacturers != null){
            //filter
            if(query.trim().length() > 0){
                Iterator<Manufacturer> it = manufacturers.iterator();
                while (it.hasNext()) {
                    Manufacturer manuItem = it.next();
                    if (!FilterUtil.searchName(manuItem.getManufacturerName(), query.trim())) {
                        it.remove();
                    }
                }
            }
            return manufacturers;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

///ANALYTICS
    @RequestMapping(value = "/analytics", method = RequestMethod.GET)
    public List<Analytics> getAnalytics(@RequestHeader("Authorization") String token,
                                        @RequestParam(defaultValue = "") String domain, @RequestParam(defaultValue = "-999") int partner_id,
                                        @RequestParam(defaultValue = "") String product_link, @RequestParam(defaultValue = "") String date_from,
                                        @RequestParam(defaultValue = "") String date_to,
                                        @RequestParam(defaultValue = "-999") int total,
                                        @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int limit) {
        checkPermission(token);

        List<Analytics> analytics = analyticsRepository.getAnalytics();
        if(analytics != null){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //filters
            if(domain.trim().length() > 0){
                Iterator<Analytics> it = analytics.iterator();
                while (it.hasNext()) {
                    Analytics analitycsItem = it.next();
                    if (!FilterUtil.searchName(analitycsItem.getDomain(), domain.trim())) {
                        it.remove();
                    }
                }
            }
            if(partner_id != -999){
                Iterator<Analytics> it = analytics.iterator();
                while (it.hasNext()) {
                    Analytics analitycsItem = it.next();
                    if (analitycsItem.getPartner_id() != partner_id) {
                        it.remove();
                    }
                }
            }
            if(product_link.trim().length() > 0){
                Iterator<Analytics> it = analytics.iterator();
                while (it.hasNext()) {
                    Analytics analitycsItem = it.next();
                    if (!FilterUtil.searchName(analitycsItem.getProduct_link(), product_link.trim())) {
                        it.remove();
                    }
                }
            }
            if(date_from.trim().length() > 0){
                Iterator<Analytics> it = analytics.iterator();
                Date date1 = null;
                Date date2 = null;
                while (it.hasNext()) {
                    Analytics analitycsItem = it.next();
                    try {
                        date1 = dateFormat.parse(analitycsItem.getDate_added());
                        date2 = dateFormat.parse(date_from.trim());
                        if (date1.compareTo(date2) < 0) {
                            it.remove();
                        }
                    } catch (ParseException e) {
                    }
                }
            }
            if(date_to.trim().length() > 0){
                Iterator<Analytics> it = analytics.iterator();
                Date date1 = null;
                Date date2 = null;
                while (it.hasNext()) {
                    Analytics analitycsItem = it.next();
                    try {
                        date1 = dateFormat.parse(analitycsItem.getDate_added());
                        date2 = dateFormat.parse(date_to.trim());
                        if (date1.compareTo(date2) > 0) {
                            it.remove();
                        }
                    } catch (ParseException e) { }
                }
            }

            if(total != -999){
                Iterator<Analytics> it = analytics.iterator();
                while (it.hasNext()) {
                    Analytics analitycsItem = it.next();
                    if (analitycsItem.getTotal() != total) {
                        it.remove();
                    }
                }
            }
            //paginations
            limit = limit > 0 ? limit : 9;
            page = page > 0 ? page : 1;
            int offset = (limit * page) - limit;

            return analytics.subList(Math.min(analytics.size(), offset), Math.min(analytics.size(), offset + limit));
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }


//PAGES
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public List<PageInList> getPages(@RequestHeader("Authorization") String token) {
        checkPermission(token);
        List<PageInList> pages = new ArrayList<>();
        List<Page> pagesFromDB = (List<Page>) pageRepository.findAll();
        if(pagesFromDB != null){
            for(Page pageItem: pagesFromDB){
                PageInList pageInList = new PageInList();
                pageInList.setId(pageItem.getId());
                PageDescription descriptionUa = pageDescriptionRepository.findFirstByPageIdAndLanguage(pageItem.getId(), Constants.LANGUAGE_UA);
                if(descriptionUa != null){
                    pageInList.setTitle_ua(descriptionUa.getTitle());
                }
                PageDescription descriptionRu = pageDescriptionRepository.findFirstByPageIdAndLanguage(pageItem.getId(), Constants.LANGUAGE_RU);
                if(descriptionRu != null){
                    pageInList.setTitle_ru(descriptionRu.getTitle());
                }
                pages.add(pageInList);
            }
        }
        return pages;
    }


    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public Page addPage(@RequestHeader("Authorization") String token,
                        @RequestBody Page page) {
        checkPermission(token);
        pageRepository.save(page);
        if(page.getDescriptions() != null){
            //ua
            if(page.getDescriptions().getUa() != null){
                PageDescription descriptionUa = page.getDescriptions().getUa();
                descriptionUa.setPageId(page.getId());
                descriptionUa.setLanguage(Constants.LANGUAGE_UA);
                pageDescriptionRepository.save(descriptionUa);
            }
            //ru
            if(page.getDescriptions().getRu() != null){
                PageDescription descriptionRu = page.getDescriptions().getRu();
                descriptionRu.setPageId(page.getId());
                descriptionRu.setLanguage(Constants.LANGUAGE_RU);
                pageDescriptionRepository.save(descriptionRu);
            }
        }
        //images
        List<PageImage> pageImages = page.getImages();
        if(pageImages != null){
            for(PageImage image : pageImages){
                if(image != null){
                    image.setPageId(page.getId());
                    pageImageRepository.save(image);
                }
            }
        }
        return getPage(token, page.getId());
    }

    @RequestMapping(value = "/page/{id}", method = RequestMethod.GET)
    public Page getPage(@RequestHeader("Authorization") String token,
                        @PathVariable(value = "id") int id) {
        checkPermission(token);

        Page page = pageRepository.findPageById(id);
        if(page != null){
            //description
            Page.Descriptions descriptions = new Page.Descriptions();
            PageDescription descriptionUa = pageDescriptionRepository.findFirstByPageIdAndLanguage(id, Constants.LANGUAGE_UA);
            if(descriptionUa != null){
                descriptions.setUa(descriptionUa);
            }
            PageDescription descriptionRu = pageDescriptionRepository.findFirstByPageIdAndLanguage(id, Constants.LANGUAGE_RU);
            if(descriptionRu != null){
                descriptions.setRu(descriptionRu);
            }
            page.setDescriptions(descriptions);
            //images
            List<PageImage> images = pageImageRepository.findAllByPageId(id);
            if(images != null){
                page.setImages(images);
            }
            return page;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/page/{id}", method = RequestMethod.POST)
    public Page updatePage(@RequestHeader("Authorization") String token,
                           @PathVariable(value = "id") int id,
                           @RequestBody Page pageNew) {
        checkPermission(token);
        Page pageFromDB = pageRepository.findPageById(id);
        if(pageFromDB != null){
            pageNew.setId(pageFromDB.getId());
            pageRepository.save(pageNew);
            //description
            pageDescriptionRepository.deleteAllByPageId(pageNew.getId());
            if(pageNew.getDescriptions() != null){
                //ua
                if(pageNew.getDescriptions().getUa() != null){
                    PageDescription descriptionUa = pageNew.getDescriptions().getUa();
                    descriptionUa.setPageId(pageNew.getId());
                    descriptionUa.setLanguage(Constants.LANGUAGE_UA);
                    pageDescriptionRepository.save(descriptionUa);
                }
                //ru
                if(pageNew.getDescriptions().getRu() != null){
                    PageDescription descriptionRu = pageNew.getDescriptions().getRu();
                    descriptionRu.setPageId(pageNew.getId());
                    descriptionRu.setLanguage(Constants.LANGUAGE_RU);
                    pageDescriptionRepository.save(descriptionRu);
                }
            }
            //images
            pageImageRepository.deleteAllByPageId(pageNew.getId());
            List<PageImage> pageImages = pageNew.getImages();
            if(pageImages != null){
                for(PageImage image : pageImages){
                    if(image != null){
                        image.setPageId(pageNew.getId());
                        pageImageRepository.save(image);
                    }
                }
            }
            return getPage(token, pageNew.getId());
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/page/{id}", method = RequestMethod.DELETE)
    public void deletePage(@RequestHeader("Authorization") String token,
                           @PathVariable(value = "id") int id) {
        checkPermission(token);
        Page pageFromDB = pageRepository.findPageById(id);
        if(pageFromDB != null){
            pageRepository.deleteById(id);
            pageDescriptionRepository.deleteAllByPageId(id);
            pageImageRepository.deleteAllByPageId(id);
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }


//PARTNERS
    @RequestMapping(value = "/partner", method = RequestMethod.GET)
    public List<Partner> getPartners(@RequestHeader("Authorization") String token) {
        checkPermission(token);
        return (List<Partner>) partnersRepository.findAll();
    }

    @RequestMapping(value = "/partner", method = RequestMethod.POST)
    public Partner addPartner(@RequestHeader("Authorization") String token, @RequestParam String domain) {
        checkPermission(token);
        if(domain.trim().length() > 0){

            //domain validation
            if(!DataCheckUtil.isValidDomainName(domain)){
                throw new BadRequestException(Constants.ERROR_NOT_VALIDE_DOMAIN);
            }

            Partner partner = new Partner();
            partner.setDomain(domain);
            //Generate token
            String partnerToken = GenerateDataUtil.generateToken();
            while(partnersRepository.findFirstByPartnerToken(partnerToken) != null){
                partnerToken = GenerateDataUtil.generateToken();
            }
            //Save token
            partner.setPartnerToken(partnerToken);
            partnersRepository.save(partner);
            return partnersRepository.findById(partner.getId());
        }else {
            throw new BadRequestException(Constants.ERROR_WRONG_INPUT_PARAM + "'domain'");
        }
    }

    @RequestMapping(value = "/partner/{id}", method = RequestMethod.DELETE)
    public void deletePartner(@RequestHeader("Authorization") String token, @PathVariable(value = "id") int id) {
        checkPermission(token);
        Partner partner = partnersRepository.findById(id);
        if(partner != null){
            partnersRepository.deleteById(id);
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }


//SETTING
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public List<SettingModel> getSettings(@RequestHeader("Authorization") String token) {
        checkPermission(token);
        return (List<SettingModel>) settingRepository.findAll();
    }

    @RequestMapping(value = "/setting", method = RequestMethod.POST)
    public List<SettingModel> updateSettings(@RequestHeader("Authorization") String token, @RequestBody List<SettingModel> settings) {
        checkPermission(token);
        if(settings != null){
            settingRepository.deleteAll();
            settingRepository.save(settings);
        }
        return (List<SettingModel>) settingRepository.findAll();
    }

    @RequestMapping(value = "/setting/{name}", method = RequestMethod.GET)
    public SettingModel getSettings(@RequestHeader("Authorization") String token, @PathVariable(value = "name") String name) {
        checkPermission(token);
        SettingModel settingItem = settingRepository.findSettingModelByName(name);
        if(settingItem != null){
            return settingItem;
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }


//USERS
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public List<User> getUsers(@RequestHeader("Authorization") String token) {
        checkPermission(token);
        return (List<User>) userRepository.findAllByMaster(false);
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public User addUser(@RequestHeader("Authorization") String token,
                               @RequestParam String username, @RequestParam String password,
                               @RequestParam boolean status) {
        checkPermission(token);
        if(username != null && username.trim().length() > 0) {
            User userFromDb = userRepository.findUsersByUsername(username);
            if(userFromDb == null){
                User user = new User();
                user.setUsername(username);
                if(new DataCheckUtil().checkStrongPass(password)){
                    user.setPassword(AesEncriptionUtil.encrypt(password));
                }else {
                    throw new BadRequestException(Constants.ADMIN_PASSWORD_PATTER);
                }
                user.setStatus(status);
                //set date
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                user.setDateAdded(dtf.format(now));
                userRepository.save(user);
                return getUser(token, user.getId());
            }else {
                throw new BadRequestException(Constants.USERNAME_EXCEPTIONS);
            }
        }else {
            throw new BadRequestException(Constants.ERROR_WRONG_INPUT_PARAM + "'username'");
        }
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public User getUser(@RequestHeader("Authorization") String token, @PathVariable(value = "id") int id) {
        checkPermission(token);

        //check role who use request
        User userWhoManipulate = userRepository.findUserByToken(token);

        User user = userRepository.findUserById(id);
        if(user != null){

            if(!user.isMaster() || userWhoManipulate != null && userWhoManipulate.isMaster() && userWhoManipulate.getId() == user.getId()){
                return user;
            }else {
                throw new AccessException(Constants.ADMIN_PERMISSION_EXCEPTIONS);

            }
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST)
    public User updateUser(@RequestHeader("Authorization") String token, @PathVariable(value = "id") int id,
                           @RequestParam String username, @RequestParam(defaultValue = Constants.DEFAULT_NOT_USE_VALUE) String password,
                           @RequestParam boolean status) {
        checkPermission(token);

        //check role who use request
        User userWhoManipulate = userRepository.findUserByToken(token);

        User user = userRepository.findUserById(id);
        if(user != null){

            if(!user.isMaster() || userWhoManipulate != null && userWhoManipulate.isMaster() && userWhoManipulate.getId() == user.getId()){

                User userFromDb = userRepository.findUsersByUsername(username);
                if(userFromDb == null || userFromDb != null && userFromDb.getId() == id){
                    user.setUsername(username);
                    if(!password.equals(Constants.DEFAULT_NOT_USE_VALUE)){
                        if(new DataCheckUtil().checkStrongPass(password)){
                            user.setPassword(AesEncriptionUtil.encrypt(password));
                        }else {
                            throw new BadRequestException(Constants.ADMIN_PASSWORD_PATTER);
                        }
                    }
                    user.setStatus(status);
                    userRepository.save(user);
                    return getUser(token, user.getId());
                }else {
                    throw new BadRequestException(Constants.USERNAME_EXCEPTIONS);
                }

            }else {
                throw new AccessException(Constants.ADMIN_PERMISSION_EXCEPTIONS);
            }
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@RequestHeader("Authorization") String token, @PathVariable(value = "id") int id) {
        checkPermission(token);
        User user = userRepository.findUserById(id);
        if(user != null){
            userRepository.deleteById(id);
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public UserAuthorization userLogin(@RequestParam String username, @RequestParam String password) {
        User user = userRepository.findUserByUsernameAndStatus(username, true);
        if(user != null){
            if(user.getPassword().equals(AesEncriptionUtil.encrypt(password.trim()))){
                //Generate token
                String token = GenerateDataUtil.generateToken();
                while(userRepository.findUserByToken(token) != null){
                    token = GenerateDataUtil.generateToken();
                }
                //Save token
                user.setToken(token);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate localDate = LocalDate.now();
                user.setTokenExpire(dtf.format(localDate));
                userRepository.save(user);
                //return
                UserAuthorization userAuthorization = new UserAuthorization();
                userAuthorization.setId(user.getId());
                userAuthorization.setToken(user.getToken());
                return userAuthorization;
            }else {
                throw new BadRequestException(Constants.ERROR_WRONG_INPUT_PARAM + "'password'");
            }
        }else {
            throw new BadRequestException(Constants.ERROR_WRONG_INPUT_PARAM + "'username'");
        }
    }

    @RequestMapping(value = "/user/logout", method = RequestMethod.POST)
    public void userLogin(@RequestHeader("Authorization") String token) {
        User user = userRepository.findUserByToken(token);
        if(user != null){
            user.setToken(null);
            user.setTokenExpire(null);
            userRepository.save(user);
        }else {
            throw new NotFoundException(Constants.ERROR_NOT_FOUND);
        }
    }

//files
    //upload files
    @RequestMapping(value = "/resources", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String addFile(@RequestHeader("Authorization") String token,
                         @RequestParam("file") MultipartFile file) {
        checkPermission(token);

        //set upload folder
        SettingModel baseFolder = settingRepository.findSettingModelByName(Constants.SETTING_BASE_RESOURCES_FOLDER);
        if(baseFolder != null && baseFolder.getValue() != null && baseFolder.getValue().trim().length() > 0){

            if(file != null && !file.isEmpty()){
                String UPLOADED_FOLDER = new FileSystemResource("").getFile().getAbsolutePath() + baseFolder.getValue().trim();

                //get format
                String format = "";
                String fileOriginalName = file.getOriginalFilename();
                int index = fileOriginalName.lastIndexOf(".");
                if(index >= 0){
                    format = fileOriginalName.substring(index, fileOriginalName.length());
                }

                File folder = new File(UPLOADED_FOLDER);
                try{
                    //generate unique name
                    String fileName = "";
                    boolean uniqueName = false;
                    //check name in catalog
                    while (!uniqueName){
                        fileName = GenerateDataUtil.generateRandomName() + format;
                        uniqueName = true;
                        for (File f : folder.listFiles()) {
                            if(f.isFile() && fileName.equals(f.getName())){
                                uniqueName = false;
                            }
                        }
                    }
                    //save file
                    byte[] bytes = file.getBytes();
                    BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(new File(UPLOADED_FOLDER, fileName)));
                    stream.write(bytes);
                    stream.close();
                    //return
                    JSONObject result = new JSONObject();
                    result.put("fileName", fileName);
                    return result.toString();
                }catch (Exception e){
                    throw new ServerException(Constants.ERROR_FILES_FOLDER);
                }
            }else {
                throw new ServerException(Constants.ERROR_FILE_EMPTY);
            }

        }else {
            throw new ServerException(Constants.ERROR_API_SETTING_NOT_FOUND + Constants.SETTING_BASE_RESOURCES_FOLDER);
        }
    }


//filter links

    @RequestMapping(value = "/filter/links", method = RequestMethod.GET)
    public Iterable<FilterLink> getFilterLinks(@RequestHeader("Authorization") String token) {
        checkPermission(token);
        return filterLinkRepository.findAll();
    }

    @RequestMapping(value = "/filter/links", method = RequestMethod.POST)
    public Iterable<FilterLink> addFilterLink(@RequestHeader("Authorization") String token,
                                              @RequestParam String title, @RequestParam String link) {
        checkPermission(token);
        if(title.trim().length() > 0 && link.trim().length() > 0){
            if(filterLinkRepository.findByLink(link) == null){
                FilterLink filterLink = new FilterLink();
                filterLink.setTitle(title);
                filterLink.setLink(link);
                filterLinkRepository.save(filterLink);
            }else {
                throw new BadRequestException(Constants.ERROR_FILTER_LINK_ALREADY_CREATED);
            }
        }else {
            throw new BadRequestException(Constants.ERROR_FILTER_LINK_INCORRECT);
        }
        return getFilterLinks(token);
    }

    @RequestMapping(value = "/filter/links/{id}", method = RequestMethod.POST)
    public Iterable<FilterLink> updateFilterLink(@RequestHeader("Authorization") String token, @PathVariable(value = "id") int id,
                                              @RequestParam(defaultValue = "") String title, @RequestParam(defaultValue = "") String link) {
        checkPermission(token);
        FilterLink filterLink = filterLinkRepository.findById(id);
        if(filterLink != null){
            if(title.trim().length() > 0){
                filterLink.setTitle(title);
            }
            if(link.trim().length() > 0){
                FilterLink filterLinkOLD = filterLinkRepository.findByLink(link);
                if(filterLinkOLD == null || filterLink.getId() == filterLinkOLD.getId()){
                    filterLink.setLink(link);
                }else {
                    throw new BadRequestException(Constants.ERROR_FILTER_LINK_ALREADY_CREATED);
                }
            }
            filterLinkRepository.save(filterLink);
            return getFilterLinks(token);
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/filter/links/{id}", method = RequestMethod.DELETE)
    public Iterable<FilterLink> deleteFilterLink(@RequestHeader("Authorization") String token, @PathVariable(value = "id") int id) {
        checkPermission(token);
        FilterLink filterLink = filterLinkRepository.findById(id);
        if(filterLink != null){
            filterLinkRepository.deleteById(id);
            return getFilterLinks(token);
        }else {
            throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
        }
    }





    //PRIVATE METHODS
    private ProductForAdmin makeProductForAdmin(@NotNull String language, Product product){
        ProductForAdmin productForAdmin = new ProductForAdmin();
        productForAdmin.setCode(product.getCode());
        //List<ProductAttribute> attributes = productAttributesRepository.findAllByProductCode(productCode);
        List<ProductAttribute> attributes = product.getAttributes();
        if(attributes != null && attributes.size() > 0){
            if(language.equals(Constants.LANGUAGE_RU)){
                productForAdmin.setName(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_NAME));
                productForAdmin.setCategoryName(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_CATEGORY_NAME));
                productForAdmin.setSubcategoryName(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SUBCATEGORY_NAME));
            }else {
                productForAdmin.setName(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_NAME_UA));
                productForAdmin.setCategoryName(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_CATEGORY_NAME_UA));
                productForAdmin.setSubcategoryName(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_SUBCATEGORY_NAME_UA));
            }
            //productForAdmin.setCode(new ListSearchUtil().fingAttrValueByName(attributes, "code"));
            //image
            String imagesStr = new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_IMAGE_LIST);
            List<String> imageList = AttributeParseUtil.parseImagesListAttrFromDB(imagesStr);
            if(imageList.size() > 0){
                productForAdmin.setImage(imageList.get(0));
            }
            productForAdmin.setManufacturerName(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_MANUFACTURER_NAME));
            productForAdmin.setPriceFrom(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_PRICE_FROM));
            productForAdmin.setPriceTo(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_PRICE_TO));
            productForAdmin.setAmount(new ListSearchUtil().fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_AMOUNT));
        }
        return productForAdmin;
    }


}


