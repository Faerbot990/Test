package com.api.Autonova.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.context.annotation.Primary;

import javax.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "cars")
public class Car {

    public Car(String manuName, String modelName, String yearOfConstrFrom,
               String yearOfConstrTo, String typeName){
        this.modelName = modelName;
        this.yearOfConstrFrom = yearOfConstrFrom;
        this.manuName = manuName;
        this.yearOfConstrTo = yearOfConstrTo;
        this.typeName = typeName;
    }

    public Car(){}


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "car_id")
    private int carId;

    @Column(name = "manu_name")
    private String manuName;

    @Column(name = "year_of_constr_from")
    private String yearOfConstrFrom;

    @Column(name = "year_of_constr_to")
    private String yearOfConstrTo;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "construction_type")
    private String constructionType;

    @Column(name = "construction_type_ua")
    private String constructionTypeUa;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "fuel_type_ua")
    private String fuelTypeUa;

    @Column(name = "manu_id")
    private int manuId;

    @Column(name = "mod_id")
    private int modId;

    @ManyToMany(mappedBy = "cars")
    private Set<Product> products = new HashSet<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public String getManuName() {
        return manuName;
    }

    public void setManuName(String manuName) {
        this.manuName = manuName;
    }

    public String getYearOfConstrFrom() {
        return yearOfConstrFrom;
    }

    public void setYearOfConstrFrom(String yearOfConstrFrom) {
        this.yearOfConstrFrom = yearOfConstrFrom;
    }

    public String getYearOfConstrTo() {
        return yearOfConstrTo;
    }

    public void setYearOfConstrTo(String yearOfConstrTo) {
        this.yearOfConstrTo = yearOfConstrTo;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getConstructionType() {
        return constructionType;
    }

    public void setConstructionType(String constructionType) {
        this.constructionType = constructionType;
    }


    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public int getManuId() {
        return manuId;
    }

    public void setManuId(int manuId) {
        this.manuId = manuId;
    }

    public int getModId() {
        return modId;
    }

    public void setModId(int modId) {
        this.modId = modId;
    }

    public String getConstructionTypeUa() {
        return constructionTypeUa;
    }

    public void setConstructionTypeUa(String constructionTypeUa) {
        this.constructionTypeUa = constructionTypeUa;
    }

    public String getFuelTypeUa() {
        return fuelTypeUa;
    }

    public void setFuelTypeUa(String fuelTypeUa) {
        this.fuelTypeUa = fuelTypeUa;
    }


    public static Comparator<Car> COMPARE_BY_MODEL_NAME() {
        return new Comparator<Car>() {
            public int compare(Car one, Car other) {
                return one.modelName.compareTo(other.modelName);
            }
        };
    }

    public static Comparator<Car> COMPARE_BY_TYPE_NAME() {
        return new Comparator<Car>() {
            public int compare(Car one, Car other) {
                return one.typeName.compareTo(other.typeName);
            }
        };
    }

    public static Comparator<Car> COMPARE_BY_YEAR_FROM() {
        return new Comparator<Car>() {
            public int compare(Car one, Car other) {
                return one.yearOfConstrFrom.compareTo(other.yearOfConstrFrom);
            }
        };
    }

    @JsonIgnore
    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }


    public void makeCorrectYears(){
        if(yearOfConstrTo != null && yearOfConstrTo.trim().length() >= 7){
            yearOfConstrTo = yearOfConstrTo.substring(0, 7);
        }
        if(yearOfConstrFrom != null && yearOfConstrFrom.trim().length() >= 7){
            yearOfConstrFrom = yearOfConstrFrom.substring(0, 7);
        }
    }

}
