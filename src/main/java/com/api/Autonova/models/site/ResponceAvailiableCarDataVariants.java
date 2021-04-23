package com.api.Autonova.models.site;

import java.io.Serializable;
import java.util.List;

public class ResponceAvailiableCarDataVariants implements Serializable {

    private List<String> manu_name = null;
    private List<String> model_name = null;
    private List<String> type_name = null;
    private List<Integer> year = null;

    public List<String> getManu_name() {
        return manu_name;
    }

    public void setManu_name(List<String> manu_name) {
        this.manu_name = manu_name;
    }

    public List<String> getModel_name() {
        return model_name;
    }

    public void setModel_name(List<String> model_name) {
        this.model_name = model_name;
    }

    public List<String> getType_name() {
        return type_name;
    }

    public void setType_name(List<String> type_name) {
        this.type_name = type_name;
    }

    public List<Integer> getYear() {
        return year;
    }

    public void setYear(List<Integer> year) {
        this.year = year;
    }
}
