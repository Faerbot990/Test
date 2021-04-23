package com.api.Autonova.utils;

import org.apache.commons.codec.binary.Base64;

public class Base64Coder {

    public String encode(String str){
        byte[] bytesEncoded = Base64.encodeBase64(str.getBytes());
        return new String(bytesEncoded);
    }

    public String decode(String bytesEncoded){
        byte[] valueDecoded = Base64.decodeBase64(bytesEncoded);
        return new String(valueDecoded);
    }

}
