package com.api.Autonova.utils;

public class ExceptionsUtil {


    public static String getStackTrace(StackTraceElement[] elements) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : elements) {
            sb.append("\n");
            sb.append(element.toString());
        }
        return sb.toString();
    }



}
