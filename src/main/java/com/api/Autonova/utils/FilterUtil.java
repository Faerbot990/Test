package com.api.Autonova.utils;

public class FilterUtil {

    //ПОИСК ИМЕНИ ПО СЛОВУ
    public static boolean searchName(String search, String what) {

        //clear if have symbol
        search = search.replaceAll("[\\(\\)\\[\\]\\{\\}]","");
        what = what.replaceAll("[\\(\\)\\[\\]\\{\\}]","");

        //check
        return compareString(search, what)||compareFirstCharAllSrtingr(search, what);
    }


    public static boolean compareString(String search, String what){
        return !search.replaceAll(what,"_").equalsIgnoreCase(search);
    }
    public static boolean compareFirstCharAllSrtingr(String search, String what) {
        return !search.replaceAll(upperCaseAllFirst(what),"_").equals(search);
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
