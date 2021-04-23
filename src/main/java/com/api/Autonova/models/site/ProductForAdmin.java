package com.api.Autonova.models.site;

import java.util.Comparator;

public class ProductForAdmin {

    public static String SORT;

    private String name;
    private String code;
    private String image;
    private String priceFrom;
    private String priceTo;
    private String amount;
    private String manufacturerName;
    private String categoryName;
    private String subcategoryName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(String priceFrom) {
        this.priceFrom = priceFrom;
    }

    public String getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(String priceTo) {
        this.priceTo = priceTo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public void setSubcategoryName(String subcategoryName) {
        this.subcategoryName = subcategoryName;
    }


    public static Comparator<ProductForAdmin> COMPARE_BY(String key) {
        return new Comparator<ProductForAdmin>() {
            public int compare(ProductForAdmin one, ProductForAdmin other) {
                try{
                    switch (key){
                        case "name":
                            return one.name.compareTo(other.name);
                        case "code":
                            return one.code.compareTo(other.code);
                        case "priceFrom":
                            return one.priceFrom.compareTo(other.priceFrom);
                        case "priceTo":
                            return one.priceTo.compareTo(other.priceTo);
                        case "amount":
                            return one.amount.compareTo(other.amount);
                        case "manufacturerName":
                            return one.manufacturerName.compareTo(other.manufacturerName);
                        case "categoryName":
                            return one.categoryName.compareTo(other.categoryName);
                        case "subcategoryName":
                            return one.subcategoryName.compareTo(other.subcategoryName);
                        default:
                            return one.name.compareTo(other.name);
                    }
                }catch (NullPointerException e){
                    return 1;
                }
            }
        };
    }
}

