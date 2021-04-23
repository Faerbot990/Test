package com.api.Autonova.utils;

import com.api.Autonova.models.*;
import com.api.Autonova.models.site.ProductAttributeFilterGetting;

import javax.validation.constraints.NotNull;
import java.util.*;

public class ListSearchUtil {



//Cars

    public List<Car> findCarByManuName(List<Car> allCars, String manuName){
        List<Car> result = new ArrayList<>();
        if(allCars != null){
            for(Car carItem : allCars){
                if(carItem.getManuName().equals(manuName)){
                    result.add(carItem);
                }
            }
        }
        return result;
    }

    public List<Car> findCarsFromAllByCarIds(List<Car> allCars, List<Integer> carIds){
        List<Car> result = new ArrayList<>();
        if(carIds != null && allCars != null && allCars.size() > 0 && carIds.size() > 0){
            for(int idItem : carIds){
                for(Car carItem : allCars){
                    if(idItem == carItem.getCarId()){
                        result.add(carItem);
                        break;
                    }
                }
            }
        }
        return result;
    }

//Attrs

    public String fingAttrValueByName(@NotNull List<ProductAttribute> attributes, String name){
        for(ProductAttribute attrItem : attributes){
            if(attrItem.getName().equals(name)){
                return attrItem.getValue();
            }
        }
        return "";
    }
    public ProductAttribute findAttrItemByProductCode(List<ProductAttribute> allAttrs, String code){
        if(allAttrs != null){
            for(ProductAttribute attrItem : allAttrs){
                if(attrItem.getProductCode().equals(code)){
                    return attrItem;
                }
            }
        }
        return null;
    }

    public ProductAttribute findAttrItemByNameAndProductCode(List<ProductAttribute> allAttrs, String name, String code){
        if(allAttrs != null){
            for(ProductAttribute attrItem : allAttrs){
                if(attrItem.getName().equals(name) && attrItem.getProductCode().equals(code)){
                    return attrItem;
                }
            }
        }
        return null;
    }

    public List<ProductAttribute> findAttsByProductCode(List<ProductAttribute> allAttrs, String code){
        List<ProductAttribute> result = new ArrayList<>();
        if(allAttrs != null){
            for(ProductAttribute attrItem : allAttrs){
                if(attrItem.getProductCode().equals(code)){
                    result.add(attrItem);
                }
            }
        }
        return result;
    }

    //метод находиn атрибуты по коду и вырезает их из списка
    public List<ProductAttribute> cutAttsByProductCode(List<ProductAttribute> allAttrs, String code){
        List<ProductAttribute> result = new ArrayList<>();
        if(allAttrs != null){

            Iterator<ProductAttribute> it = allAttrs.iterator();
            while (it.hasNext()) {
                ProductAttribute attrItem = it.next();
                if(attrItem.getProductCode().equals(code)){
                    result.add(attrItem);
                    it.remove();
                }
            }
        }
        return result;
    }

    public List<ProductAttribute> cutAttsByProductId(List<ProductAttribute> allAttrs, int productId){
        List<ProductAttribute> result = new ArrayList<>();
        if(allAttrs != null){

            Iterator<ProductAttribute> it = allAttrs.iterator();
            while (it.hasNext()) {
                ProductAttribute attrItem = it.next();
                if(attrItem.getProduct().getId() == productId){
                    result.add(attrItem);
                    it.remove();
                }
            }
        }
        return result;
    }

    public List<String> cutAttsValuesByName(List<ProductAttributeFilterGetting> allAttrs, String name){
        List<String> result = new ArrayList<>();
        if(allAttrs != null){
            Iterator<ProductAttributeFilterGetting> it = allAttrs.iterator();
            while (it.hasNext()) {
                ProductAttributeFilterGetting attrItem = it.next();
                if(attrItem.getName().equals(name)){
                    result.add(attrItem.getValue());
                    it.remove();
                }
            }
        }
        return result;
    }



    //characteristics
    public String fingCharacteristicValueByNameAndCode(String language, @NotNull List<ProductCharacteristic> characteristics, String name, String productCode){
        for(ProductCharacteristic characteristicItem : characteristics){
            if(language.equals(Constants.LANGUAGE_RU)){
                if(characteristicItem.getAttrShortName().equals(name) && characteristicItem.getProductCode().equals(productCode)){
                    return characteristicItem.getAttrValue();
                }
            }else {
                if(characteristicItem.getAttrShortNameUa().equals(name) && characteristicItem.getProductCode().equals(productCode)){
                    return characteristicItem.getAttrValueUa();
                }
            }
        }
        return "";
    }

    //Attr pattern
    public AttributesPattern findAttrPatternByName(@NotNull List<AttributesPattern> patterns, String name){
        for(AttributesPattern patternItem : patterns){
            if(patternItem.getName().equals(name)){
                return patternItem;
            }
        }
        return null;
    }


    //for updated methods (prices and amounts)

    public int findAttrIndexByProductCodeAndName(List<ProductAttribute> attributes, String code, String attrName){
        int index = -1;
        if(attributes != null){
            for(int i = 0; i < attributes.size(); i++){
                if(attributes.get(i).getProductCode().equals(code) && attributes.get(i).getName().equals(attrName)){
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public int findProductIndexByCode(List<Product> products, String productCode){
        int index = -1;
        if(products != null){
            for(int i = 0; i < products.size(); i++){
                if(products.get(i).getCode() != null && products.get(i).getCode().equals(productCode)){
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
}
