package com.api.Autonova.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
public class Product implements Serializable {

    public Product(){}


    public Product(int id, String code){
        this.id = id;
        this.code = code;
    }


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "code")
    private String code;

    @Column(name = "update_time")
    private String updateTime;

    @OneToMany(fetch=FetchType.LAZY, mappedBy="product")
    private List<ProductAttribute> attributes;

    @OneToMany(fetch=FetchType.LAZY, mappedBy="product")
    private List<ProductCharacteristic> characteristics;


    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
            name = "cars_products",
            joinColumns = { @JoinColumn(name = "product_id") },
            inverseJoinColumns = { @JoinColumn(name = "car_id") }
    )
    private Set<Car> cars = new HashSet<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public Set<Car> getCars() {
        return cars;
    }

    public void setCars(Set<Car> cars) {
        this.cars = cars;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

}
