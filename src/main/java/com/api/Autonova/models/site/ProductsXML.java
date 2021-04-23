package com.api.Autonova.models.site;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ProductsXML {

    private List<Product> products = new ArrayList<>();

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }


    public static class Product {

        private String code;

        private List<ProductAttribute> attributes = null;

        private List<ProductCharacteristic> characteristics = null;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public List<ProductAttribute> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<ProductAttribute> attributes) {
            this.attributes = attributes;
        }

        public List<ProductCharacteristic> getCharacteristics() {
            return characteristics;
        }

        public void setCharacteristics(List<ProductCharacteristic> characteristics) {
            this.characteristics = characteristics;
        }
    }


    public static class ProductAttribute {

        private String name;
        private String value;

        public ProductAttribute(){}

        public ProductAttribute(com.api.Autonova.models.ProductAttribute attribute){
            this.name = attribute.getName();
            this.value = attribute.getValue();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class ProductCharacteristic {

        private int id;
        private String attrName;
        private String attrNameUa;
        private String attrShortName;
        private String attrShortNameUa;
        private String attrValue;
        private String attrValueUa;
        private String attrUnit;

        public ProductCharacteristic(){}

        public ProductCharacteristic(com.api.Autonova.models.ProductCharacteristic characteristic){
            this.id = characteristic.getId();
            this.attrName = characteristic.getAttrName();
            this.attrNameUa = characteristic.getAttrNameUa();
            this.attrShortName = characteristic.getAttrShortName();
            this.attrShortNameUa = characteristic.getAttrShortNameUa();
            this.attrValue = characteristic.getAttrValue();
            this.attrValueUa = characteristic.getAttrValueUa();
            this.attrUnit = characteristic.getAttrUnit();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getAttrName() {
            return attrName;
        }

        public void setAttrName(String attrName) {
            this.attrName = attrName;
        }

        public String getAttrNameUa() {
            return attrNameUa;
        }

        public void setAttrNameUa(String attrNameUa) {
            this.attrNameUa = attrNameUa;
        }

        public String getAttrShortName() {
            return attrShortName;
        }

        public void setAttrShortName(String attrShortName) {
            this.attrShortName = attrShortName;
        }

        public String getAttrShortNameUa() {
            return attrShortNameUa;
        }

        public void setAttrShortNameUa(String attrShortNameUa) {
            this.attrShortNameUa = attrShortNameUa;
        }

        public String getAttrValue() {
            return attrValue;
        }

        public void setAttrValue(String attrValue) {
            this.attrValue = attrValue;
        }

        public String getAttrValueUa() {
            return attrValueUa;
        }

        public void setAttrValueUa(String attrValueUa) {
            this.attrValueUa = attrValueUa;
        }

        public String getAttrUnit() {
            return attrUnit;
        }

        public void setAttrUnit(String attrUnit) {
            this.attrUnit = attrUnit;
        }
    }
}
