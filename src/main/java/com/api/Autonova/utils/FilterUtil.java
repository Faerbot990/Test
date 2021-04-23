package com.api.Autonova.utils;

public class FilterUtil {

    //ПОИСК ИМЕНИ ПО СЛОВУ
    public static boolean searchName(String search, String what) {

        //clear if have symbol
        search = search.replaceAll("[\\(\\)\\[\\]\\{\\}]","");
        what = what.replaceAll("[\\(\\)\\[\\]\\{\\}]","");

        //check
        if(!search.replaceAll(what,"_").equals(search)) {
            return true;
        }
        if(!search.replaceAll(upperCaseAllFirst(what),"_").equals(search)) {
            return true;
        }
        if(!search.replaceAll(what.toLowerCase(),"_").equals(search)) {
            return true;
        }
        if(!search.replaceAll(what.toUpperCase(),"_").equals(search)) {
            return true;
        }
        return false;
    }



    //private util
    private static String upperCaseAllFirst(String value) {
        char[] array = value.toCharArray();
        // Uppercase first letter.
        array[0] = Character.toUpperCase(array[0]);

        // Uppercase all letters that follow a whitespace character.
        for (int i = 1; i < array.length; i++) {
            if (Character.isWhitespace(array[i - 1])) {
                array[i] = Character.toUpperCase(array[i]);
            }
        }

        // Result.
        return new String(array);
    }
}
