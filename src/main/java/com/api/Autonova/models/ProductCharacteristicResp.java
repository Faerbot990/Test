package com.api.Autonova.models;

import javax.persistence.*;


public class ProductCharacteristicResp {


    @Column(name = "attrName")
    private String attrName;

    @Column(name = "attrValue")
    private String attrValue;

    @Column(name = "attrUnit")
    private String attrUnit;


    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }

    public String getAttrUnit() {
        return attrUnit;
    }

    public void setAttrUnit(String attrUnit) {
        this.attrUnit = attrUnit;
    }
}
