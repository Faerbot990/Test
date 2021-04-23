package com.api.Autonova.models.site;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class ProductForSite implements Serializable {

    private String name;
    private String code;
    private String article;
    private String seoUrl;
    private String image;
    private String manufacturerName;
    private boolean featured;
    private String priceFrom;
    private String priceTo;
    private String amount;
    private List<String> carList;
    private String link;


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


    public List<String> getCarList() {
        return carList;
    }

    public void setCarList(List<String> carList) {
        this.carList = carList;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public static Comparator<ProductForSite> COMPARE_BY(String key) {
        return new Comparator<ProductForSite>() {
            public int compare(ProductForSite one, ProductForSite other) {
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
                        case "featured":
                            return Boolean.compare(one.featured, other.featured);
                        default:
                            return one.name.compareTo(other.name);
                    }
                }catch (NullPointerException e){
                    return 1;
                }
            }
        };
    }

    public String getSeoUrl() {
        return seoUrl;
    }

    public void setSeoUrl(String seoUrl) {
        this.seoUrl = seoUrl;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }
}

