package com.api.Autonova.models.site;

import java.util.Comparator;

public class ProductInList {

    private String name;
    private String code;
    private String article;
    private String seoUrl;

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

    public static Comparator<ProductInList> COMPARE_BY_NAME() {
        return new Comparator<ProductInList>() {
            public int compare(ProductInList one, ProductInList other) {
                return one.name.compareTo(other.name);
            }
        };
    }

    public String getSeoUrl() {
        return seoUrl;
    }

    public void setSeoUrl(String seoUrl) {
        this.seoUrl = seoUrl;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }
}

