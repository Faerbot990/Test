package com.api.Autonova.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "product_characteristic")
public class ProductCharacteristic {

    public ProductCharacteristic(int localid, int id, String attrShortName, String attrShortNameUa, String attrName,
                                 String attrNameUa, String attrUnit){
        this.localid = localid;
        this.id = id;
        this.attrShortName = attrShortName;
        this.attrShortNameUa = attrShortNameUa;
        this.attrName = attrName;
        this.attrNameUa = attrNameUa;
        this.attrUnit = attrUnit;
    }

    public ProductCharacteristic(){}

    @Id
    @Column(name = "localid")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int localid;

    //1C id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "attr_name")
    private String attrName;

    @Column(name = "attr_name_ua")
    private String attrNameUa;

    @Column(name = "attr_short_name")
    private String attrShortName;

    @Column(name = "attr_short_name_ua")
    private String attrShortNameUa;

    @Column(name = "attr_value")
    private String attrValue;

    @Column(name = "attr_value_ua")
    private String attrValueUa;

    @Column(name = "attr_unit")
    private String attrUnit;

    @Column(name = "product_code")
    private String productCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", referencedColumnName = "id")
    private Product product;

    public int getLocalid() {
        return localid;
    }

    public void setLocalid(int localid) {
        this.localid = localid;
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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    @JsonIgnore
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
