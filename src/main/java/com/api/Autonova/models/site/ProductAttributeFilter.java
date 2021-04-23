package com.api.Autonova.models.site;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


public class ProductAttributeFilter implements Serializable {

    public ProductAttributeFilter(){}

    public ProductAttributeFilter(String name, List<String> values){
        this.name = name;
        this.values = values;
    }


    private String name;

    private List<String> values;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
