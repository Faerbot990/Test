package com.api.Autonova.models.site;


import java.io.Serializable;

public class ProductCharacteristicSite implements Serializable {


    private String attrName;
    private String attrShortName;
    private String attrValue;
    private String attrUnit;


    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }


    public String getAttrShortName() {
        return attrShortName;
    }

    public void setAttrShortName(String attrShortName) {
        this.attrShortName = attrShortName;
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
