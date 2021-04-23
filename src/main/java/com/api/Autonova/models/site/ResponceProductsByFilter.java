package com.api.Autonova.models.site;

import java.util.List;

public class ResponceProductsByFilter {

    private List<ProductForAdmin> products = null;

    private PaginationModel pagination = null;


    public List<ProductForAdmin> getProducts() {
        return products;
    }

    public void setProducts(List<ProductForAdmin> products) {
        this.products = products;
    }

    public PaginationModel getPagination() {
        return pagination;
    }

    public void setPagination(PaginationModel pagination) {
        this.pagination = pagination;
    }
}
