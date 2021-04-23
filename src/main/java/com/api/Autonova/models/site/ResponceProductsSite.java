package com.api.Autonova.models.site;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

public class ResponceProductsSite implements Serializable {

    private List<ProductForSite> products = null;

    private PaginationModel pagination = null;


    public List<ProductForSite> getProducts() {
        return products;
    }

    public void setProducts(List<ProductForSite> products) {
        this.products = products;
    }

    public PaginationModel getPagination() {
        return pagination;
    }

    public void setPagination(PaginationModel pagination) {
        this.pagination = pagination;
    }

}
