package com.api.Autonova.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributeParseUtil {

    //from string with "|" to list string
    public static List<String> parseImagesListAttrFromDB(String imagesStr){
        List<String> listImages = new ArrayList<String>();
        if(imagesStr != null && imagesStr.trim().length() > 0){
            listImages = Arrays.asList(imagesStr.split("\\|"));
        }
        return listImages;
    }

    //from string with "," to list string
    public static List<String> parseListAttrFromDB(String listStr){
        List<String> listItems = new ArrayList<String>();
        if(listStr != null && listStr.trim().length() > 0){
            listItems = Arrays.asList(listStr.split("\\s*,\\s*"));
        }
        return listItems;
    }

    //from string with "," to list int
    public static List<Integer> parseListAttrFromDBToListInt(String listStr){
        List<Integer> listItems = new ArrayList<Integer>();
        if(listStr != null && listStr.trim().length() > 0){
            List<String> listItemsString = Arrays.asList(listStr.split("\\s*,\\s*"));
            for (String listItem : listItemsString){
                listItems.add(Integer.parseInt(listItem));
            }
        }
        return listItems;
    }
}
