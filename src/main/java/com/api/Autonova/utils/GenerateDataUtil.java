package com.api.Autonova.utils;

import com.api.Autonova.models.ProductAttribute;
import com.api.Autonova.models.site.SettingModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GenerateDataUtil {


    public static String generateToken(){
        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabsdefghjklmnopqrstuvwxyz1234567890";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
        }
        return sb.toString();
    }

    public static String generateProductLink(String language, String baseURL, String productCode, String brand, SettingModel secondaryParamsString, List<ProductAttribute> attributes, ListSearchUtil searchUtil){
        String secondaryParams = generateProductLincSecondaryParams(language, secondaryParamsString, attributes, searchUtil);
        return baseURL + "?id=search" + "&" + "s=" + productCode + "&" + "brand=" + brand + "&" + secondaryParams;
    }

    public static String generateProductLincSecondaryParams(String language, SettingModel secondaryParamsString, List<ProductAttribute> attributes, ListSearchUtil searchUtil){
        String result = "";

        if(secondaryParamsString != null && secondaryParamsString.getValue() != null && secondaryParamsString.getValue().trim().length() > 0){
            List<String> listItems = new ArrayList<String>();
            listItems = Arrays.asList(secondaryParamsString.getValue().split("\\s*,\\s*"));

            for (String itemAttr : listItems){
                if(itemAttr.trim().length() > 0){

                    String value = "";

                    if(itemAttr.equals(Constants.PRODUCT_ATTR_NAME)){
                        if(language.equals(Constants.LANGUAGE_RU)){
                            value = searchUtil.fingAttrValueByName(attributes, itemAttr);
                        }else {
                            value = searchUtil.fingAttrValueByName(attributes, Constants.PRODUCT_ATTR_NAME_UA);
                        }
                    }else {
                        value = searchUtil.fingAttrValueByName(attributes, itemAttr);
                    }


                    if(result.trim().length() > 0){
                        result += "&";
                    }
                    result += itemAttr + "=" + value;
                }
            }
        }
        return result;
    }



    public static String generateRandomName(){
        String lexiconForRandom = "ABCDEFGHIJKLMNOPQRSTUVWXYZabsdefghjklmnopqrstuvwxyz1234567890";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(lexiconForRandom.charAt(random.nextInt(lexiconForRandom.length())));
        }
        return sb.toString();
    }

    public static String generateImageURL(String imgName, SettingModel resourcesFolderSetting, SettingModel basePath){
        if(resourcesFolderSetting != null && resourcesFolderSetting.getValue() != null && resourcesFolderSetting.getValue().trim().length() > 0 &&
                basePath != null && basePath.getValue() != null && basePath.getValue().trim().length() > 0){
            return basePath.getValue() + resourcesFolderSetting.getValue() + "/" + imgName;
        }else {
            return "/" + imgName;
        }
    }
}
