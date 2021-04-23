package com.api.Autonova.models.site;

import java.io.Serializable;
import java.util.List;


public class ProductFilterModel implements Serializable {

    private List<ProductAttributeFilter> attributes;

    private List<ProductAttributeFilter> characteristics;


    public List<ProductAttributeFilter> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ProductAttributeFilter> attributes) {
        this.attributes = attributes;
    }

    public List<ProductAttributeFilter> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<ProductAttributeFilter> characteristics) {
        this.characteristics = characteristics;
    }
}
