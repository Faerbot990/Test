package com.api.Autonova.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataCheckUtil {

    private static final String PASSWORD_PATTERN = "(?=.*[0-9])(?=.*[a-z])[0-9a-z]{6,10}";


    private static Pattern pDomainNameOnly;
    private static final String DOMAIN__NAME__PATTERN = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,20}$";

    static {
        pDomainNameOnly = Pattern.compile(DOMAIN__NAME__PATTERN);
    }


    private Pattern pattern;
    private Matcher matcher;

    public boolean checkStrongPass(String pass){
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(pass);
        return matcher.matches();
    }


    //метод дял проверки наименования атрибута
    public boolean checkUaAttribute(String attrName){
        return FilterUtil.searchName("Ua", attrName);
    }

    //for domain
    public static boolean isValidDomainName(String domainName) {
        return pDomainNameOnly.matcher(domainName).find();
    }


}
