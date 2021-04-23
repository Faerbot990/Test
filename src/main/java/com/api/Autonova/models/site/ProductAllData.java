package com.api.Autonova.models.site;

import java.io.Serializable;
import java.util.List;


public class ProductAllData implements Serializable {

    private String name;
    private String code;
    private String article;
    private String image;
    private String description;
    private String priceFrom;
    private String priceTo;
    private String amount;
    private String manufacturerName;
    private String categoryName;
    private String subcategoryName;
    private List<CarForSite> carList;
    private List<String> imageList;
    private List<ProductForSite> analogList;
    private List<OENumberData> oeList;
    private List<ProductCharacteristicSite> characteristicsList;
    private String link;
    private String seoUrl;
    private String seoDescription;
    private String seoH1;
    private String seoH2;
    private String seoH3;
    private String seoH4;
    private String seoH5;
    private String seoH6;

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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public List<ProductForSite> getAnalogList() {
        return analogList;
    }

    public void setAnalogList(List<ProductForSite> analogList) {
        this.analogList = analogList;
    }

    public List<ProductCharacteristicSite> getCharacteristicsList() {
        return characteristicsList;
    }

    public void setCharacteristicsList(List<ProductCharacteristicSite> characteristicsList) {
        this.characteristicsList = characteristicsList;
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

    public String getSeoUrl() {
        return seoUrl;
    }

    public void setSeoUrl(String seoUrl) {
        this.seoUrl = seoUrl;
    }

    public String getSeoDescription() {
        return seoDescription;
    }

    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }

    public String getSeoH1() {
        return seoH1;
    }

    public void setSeoH1(String seoH1) {
        this.seoH1 = seoH1;
    }

    public String getSeoH2() {
        return seoH2;
    }

    public void setSeoH2(String seoH2) {
        this.seoH2 = seoH2;
    }

    public String getSeoH3() {
        return seoH3;
    }

    public void setSeoH3(String seoH3) {
        this.seoH3 = seoH3;
    }

    public String getSeoH4() {
        return seoH4;
    }

    public void setSeoH4(String seoH4) {
        this.seoH4 = seoH4;
    }

    public String getSeoH5() {
        return seoH5;
    }

    public void setSeoH5(String seoH5) {
        this.seoH5 = seoH5;
    }

    public String getSeoH6() {
        return seoH6;
    }

    public void setSeoH6(String seoH6) {
        this.seoH6 = seoH6;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<OENumberData> getOeList() {
        return oeList;
    }

    public void setOeList(List<OENumberData> oeList) {
        this.oeList = oeList;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public List<CarForSite> getCarList() {
        return carList;
    }

    public void setCarList(List<CarForSite> carList) {
        this.carList = carList;
    }
}

