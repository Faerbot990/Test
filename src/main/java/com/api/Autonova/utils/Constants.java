package com.api.Autonova.utils;

public class Constants {

    //Language types
    static public final String LANGUAGE_UA = "ua";
    static public final String LANGUAGE_RU = "ru";

    //attrs for work
    static public final String PRODUCT_ATTR_CODE = "code";
    static public final String PRODUCT_ATTR_ARTICLE = "article";
    static public final String PRODUCT_ATTR_NAME = "name";
    static public final String PRODUCT_ATTR_NAME_UA = "nameUa";
    static public final String PRODUCT_ATTR_MANUFACTURER_BRAND_ID = "manufacturerBrandId";
    //attrs for short update
    static public final String PRODUCT_ATTR_PRICE_FROM = "priceFrom";
    static public final String PRODUCT_ATTR_PRICE_TO = "priceTo";
    static public final String PRODUCT_ATTR_PRICE_SALE = "priceSale";
    static public final String PRODUCT_ATTR_AMOUNT = "amount";
    static public final String PRODUCT_ATTR_SALE = "sale";
    //other attrs
    static public final String PRODUCT_ATTR_MANUFACTURER_ID = "manufacturerId";
    static public final String PRODUCT_ATTR_MANUFACTURER_NAME = "manufacturerName";
    static public final String PRODUCT_ATTR_CATEGORY_ID = "categoryId";
    static public final String PRODUCT_ATTR_CATEGORY_NAME = "categoryName";
    static public final String PRODUCT_ATTR_CATEGORY_NAME_UA = "categoryNameUa";
    static public final String PRODUCT_ATTR_SUBCATEGORY_ID = "subcategoryId";
    static public final String PRODUCT_ATTR_SUBCATEGORY_NAME = "subcategoryName";
    static public final String PRODUCT_ATTR_SUBCATEGORY_NAME_UA = "subcategoryNameUa";
    static public final String PRODUCT_ATTR_FEATURED = "featured";
    static public final String PRODUCT_ATTR_DESCRIPTION = "description";
    static public final String PRODUCT_ATTR_DESCRIPTION_UA = "descriptionUa";
    static public final String PRODUCT_ATTR_IDENTIFIER = "identifier";

    //SEO attrs
    static public final String PRODUCT_ATTR_SEO_URL = "SEOUrl";
    static public final String PRODUCT_ATTR_SEO_TITLE = "SEOTitle";
    static public final String PRODUCT_ATTR_SEO_TITLE_UA = "SEOTitleUa";
    static public final String PRODUCT_ATTR_SEO_DESCRIPTION = "SEODescription";
    static public final String PRODUCT_ATTR_SEO_DESCRIPTION_UA = "SEODescriptionUa";
    static public final String PRODUCT_ATTR_H1 = "H1";
    static public final String PRODUCT_ATTR_H1_UA = "H1Ua";
    static public final String PRODUCT_ATTR_H2 = "H2";
    static public final String PRODUCT_ATTR_H2_UA = "H2Ua";
    static public final String PRODUCT_ATTR_H3 = "H3";
    static public final String PRODUCT_ATTR_H3_UA = "H3Ua";
    static public final String PRODUCT_ATTR_H4 = "H4";
    static public final String PRODUCT_ATTR_H4_UA = "H4Ua";
    static public final String PRODUCT_ATTR_H5 = "H5";
    static public final String PRODUCT_ATTR_H5_UA = "H5Ua";
    static public final String PRODUCT_ATTR_H6 = "H6";
    static public final String PRODUCT_ATTR_H6_UA = "H6Ua";


    //attrs-list
    static public final String PRODUCT_ATTR_IMAGE_LIST = "imageList";
    static public final String PRODUCT_ATTR_ANALOG_LIST = "analogList";
    static public final String PRODUCT_ATTR_OE_LIST = "oeList";
    static public final String PRODUCT_ATTR_CAR_LIST = "carList";
    static public final String PRODUCT_ATTR_SPEC_LIST = "specList";
    //attrs-list items
    static public final String PRODUCT_ATTR_CAR_LIST_ITEM = "carId";
    static public final String PRODUCT_ATTR_OE_LIST_ITEM = "code";
    static public final String PRODUCT_ATTR_ANALOG_LIST_ITEM = "code";

    //1C resp eroor
    static public final String ONEC_RESP_ERROR_PARAM = "error";


    //TecDoc 'getProductData' content variants
    static public final String TEC_DOC_ATTR_IMMEDIATE_ATTS = "immediateAttributs";
    static public final String TEC_DOC_ATTR_OE_NUMBERS = "oenNumbers";

    //Setting params
    static public final String SETTING_BASE_PATH = "base_path";
    static public final String SETTING_BASE_PRODUCT_LINK = "base_product_link";
    static public final String SETTING_PRODUCT_LINK_SECONDARY_PARAMS = "product_link_secondary_params";
    static public final String SETTING_FORM_RECEIVING_EMAIL = "form_receiving_email";
    static public final String SETTING_BASE_RESOURCES_FOLDER = "base_resources_folder";
    //statuses
    static public final String SETTING_SYSTEM_UPDATE_STATUS = "system_update_status";
    static public final String SETTING_SYSTEM_UPDATE_BY_CODES_STATUS = "system_update_by_codes_status";
    static public final String SETTING_SYSTEM_UPDATE_PRICES_STATUS = "system_update_prices_status";
    static public final String SETTING_SYSTEM_UPDATE_AMOUNT_STATUS = "system_update_amount_status";
    static public final String SETTING_SYSTEM_GET_EXCEL_STATUS = "system_get_excel_status";
    static public final String SETTING_SYSTEM_GET_XML_STATUS = "system_get_xml_status";
    //limits
    static public final String SETTING_SYSTEM_UPDATE_LIMIT_PRODUCTS = "system_update_limit_products";
    static public final String SETTING_SYSTEM_UPDATE_PRICES_LIMIT_PRODUCTS = "system_update_prices_limit_products";
    static public final String SETTING_SYSTEM_UPDATE_AMOUNT_LIMIT_PRODUCTS = "system_update_amount_limit_products";


//exceptions
    static public final String ADMIN_PASSWORD_PATTER = "The password must contain numbers and symbols and have a length of 6 to 10 characters";
    static public final String ADMIN_TOKEN_EXCEPTIONS = "Wrong admin token";
    static public final String ADMIN_PERMISSION_EXCEPTIONS = "Not have permissions";
    static public final String PARTNER_TOKEN_EXCEPTIONS = "Wrong partner token";
    static public final String USERNAME_EXCEPTIONS = "Username already used";
    static public final String ACCESS_1C_EXCEPTIONS = "Need 1C API access";
    static public final String ACCESS_TECDOC_EXCEPTIONS = "Need TecDoc API access";
    static public final String ERROR_WRONG_INPUT_PARAM = "Wrong input param: ";
    static public final String ERROR_API_SETTING_NOT_FOUND = "API setting not found: ";
    static public final String ERROR_NOT_VALIDE_DOMAIN = "Invalid domain";
    static public final String ERROR_UPDATE_ALREADY_RUNNING = "The update is already running";
    static public final String ERROR_PROCESS_ALREADY_RUNNING = "This process is already running";

    static public final String ERROR_RECEIVING_REQUEST = "Error receiving request ";
    static public final String ERROR_READ_DATA = "Error read data: ";
    static public final String ERROR_READING_DATA = "Error reading data ";
    static public final String ERROR_PRODUCT_NOT_SAVE_IN_DB = "The product is not saved in the database: ";

    static public final String ERROR_DATA_NOT_FOUND = "Data not found";
    static public final String ERROR_PRODUCT_NOT_FOUND = "Product not found";
    static public final String ERROR_NOT_FOUND = "Not found";
    static public final String ERROR_REQUEST_1C_INTEGRATION = "The request need 1C API integration.";
    static public final String ERROR_PART_FROM_PRODUCT = " from product: ";
    static public final String ERROR_FILE_EMPTY = "Loaded file is empty";
    static public final String ERROR_FILES_ARRAY_EMPTY = "Loaded files array is empty";
    static public final String ERROR_FILES_FOLDER = "Error files folder";

    static public final String ERROR_GLOBAL = "Something wrong: ";

    static public final String ERROR_FILTER_LINK_INCORRECT = "Incorrect filter link data";
    static public final String ERROR_FILTER_LINK_ALREADY_CREATED = "Filter link already created";

    //Admin controller
    static public final String ERROR_ATTR_NAME_USED = "This attribute name is already in use.";

    //Short service exeption
    static public final String SHORT_SERVICE_ERROR_ATTR_REQUEST_NOT_FOUND = "This attribute is not found in request: ";
    static public final String SHORT_SERVICE_ERROR_ATTR_DB_NOT_FOUND = "This attribute is not found in database: ";


    // Main service exceptions
    static public final String MAIN_SERVICE_DATA_ERROR = "Error receiving data from 1C API, could not get: ";
    static public final String MAIN_SERVICE_PRODUCT_ERROR = "In 1C, no product was found with the number: ";
    static public final String MAIN_SERVICE_ERROR_SAVE_CHARACTERISTIC = "Error saving specification: ";
    static public final String MAIN_SERVICE_ERROR_SAVE_CAR = "Error saving car: ";
    static public final String MAIN_SERVICE_ERROR_PART_PRODUCT = "from product: ";
    static public final String MAIN_SERVICE_ERROR_UPDATE_ATTR = "Unable to update attribute: ";
    static public final String MAIN_SERVICE_ERROR_ATTR_NOT_UPDATED = "Attribute not updated: ";

    // 1C API exceptions
    static public final String ONEC_API_ERROR_INTERNAL = "Internal error 1C API: ";
    static public final String ONEC_API_ERROR_OFF_SSL = "Off SLL error";

    //save and update
    static public final String ONEC_API_ERROR_FIND_ATTR_PART1 = "Unable to find attribute ";
    static public final String ONEC_API_ERROR_FIND_ATTR_IN_DB_FOR_UPDATE = " in database, the data is not update in the database but updated in 1C API";
    static public final String ONEC_API_ERROR_FIND_ATTR_IN_1C_RESP_FOR_UPDATE = " in 1C API response, the data is not update in the database but updated in 1C API";

    static public final String ONEC_API_ERROR_SAVE_1C_API = "The data is not save to 1C API: ";
    static public final String ONEC_API_ERROR_CAN_NOT_READ_AND_SAVE = "Can not read the answer 1C API, the data is not save to the database but saved to 1C API: ";
    static public final String ONEC_API_ERROR_CAN_NOT_READ_AND_UPDATE = "Can not read the answer 1C API, the data is not update in the database but updated in 1C API: ";
    static public final String ONEC_API_ERROR_CAN_NOT_READ_AND_DELETE = "Can not read the answer 1C API, the data is not delete in the database but deleted in 1C API: ";

    //TecDoc Error
    static public final String TECDOC_ERROR_GET_ARTICLE = "Could not get the article from TecDoc API";

    //MYSQL Error
    static public final String MYSQL_ITEM_VALUE_EXCEEDED = "The object is not recorded in the database, value is exceeded:";

//access external API's names
    static public final String ACCESS_NAME_1C = "1C API";
    static public final String ACCESS_NAME_TECDOC = "TecDoc API";



//FOR ADMIN PANEL AND SITE WORK

    static public final String SORT_VECTOR_ASC = "ASC";
    static public final String SORT_VECTOR_DESC = "DESC";


    static public final String DEFAULT_NOT_USE_VALUE = "default not use value";



}
