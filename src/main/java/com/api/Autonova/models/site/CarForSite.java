package com.api.Autonova.models.site;


import com.api.Autonova.utils.Constants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;


public class CarForSite implements Serializable {

    public CarForSite(){}

    public CarForSite(String language,
                      int id, int carId, String manuName, String yearOfConstrFrom, String yearOfConstrTo,
                      String modelName, String typeName, int manuId, int modId,
                      String constructionType, String constructionTypeUa,
                      String fuelType, String fuelTypeUa){

        this.id = id;
        this.carId = carId;
        this.manuName = manuName;
        this.yearOfConstrFrom = yearOfConstrFrom;
        this.yearOfConstrTo = yearOfConstrTo;
        this.modelName = modelName;
        this.typeName = typeName;
        this.manuId = manuId;
        this.modId = modId;

        if(language.equals(Constants.LANGUAGE_RU)){
            this.constructionType = constructionType;
            this.fuelType = fuelType;
        }else {
            this.constructionType = constructionTypeUa;
            this.fuelType = fuelTypeUa;
        }
    }


    private int id;
    private int carId;
    private String manuName;
    private String yearOfConstrFrom;
    private String yearOfConstrTo;
    private String modelName;
    private String typeName;
    private String constructionType;
    private String fuelType;
    private int manuId;
    private int modId;



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

    public static Comparator<CarForSite> COMPARE_BY_MODEL_NAME() {
        return new Comparator<CarForSite>() {
            public int compare(CarForSite one, CarForSite other) {
                return one.modelName.compareTo(other.modelName);
            }
        };
    }
}
