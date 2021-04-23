package com.api.Autonova.components.handlers;

import com.api.Autonova.utils.Constants;
import org.springframework.stereotype.Component;

@Component
public class AttrPricesHandler {

    public String handlePrice(String price){
        //обрезаем цену, оставляя только целую часть
        if(price != null && price.trim().length() > 0 && price.contains(".")){
            return price.substring(0, price.indexOf("."));
        }else if(price != null && price.trim().length() > 0){
            return price;
        }else {
            return "0";
        }
    }
}
