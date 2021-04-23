package com.api.Autonova.models.site;

import java.io.Serializable;
import java.util.List;

public class AttributeWithValues implements Serializable {

    private String name;
    private String title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
