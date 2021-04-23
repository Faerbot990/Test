package com.api.Autonova.models.site;

import java.util.List;

public class ResponceSearchProducts {

    private List<ProductInList> results_by_name = null;
    private List<ProductInList> results_by_code  = null;

    public List<ProductInList> getResults_by_name() {
        return results_by_name;
    }

    public void setResults_by_name(List<ProductInList> results_by_name) {
        this.results_by_name = results_by_name;
    }

    public List<ProductInList> getResults_by_code() {
        return results_by_code;
    }

    public void setResults_by_code(List<ProductInList> results_by_code) {
        this.results_by_code = results_by_code;
    }
}
