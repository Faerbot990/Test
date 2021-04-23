package com.api.Autonova.models.site;

import com.api.Autonova.models.Car;

import java.util.List;

public class ResponceSearchCars {

    private List<Car> resultsByCarId = null;
    private List<Car> resultsByManuName = null;
    private List<Car> resultsByModelName = null;

    public List<Car> getResultsByCarId() {
        return resultsByCarId;
    }

    public void setResultsByCarId(List<Car> resultsByCarId) {
        this.resultsByCarId = resultsByCarId;
    }

    public List<Car> getResultsByManuName() {
        return resultsByManuName;
    }

    public void setResultsByManuName(List<Car> resultsByManuName) {
        this.resultsByManuName = resultsByManuName;
    }

    public List<Car> getResultsByModelName() {
        return resultsByModelName;
    }

    public void setResultsByModelName(List<Car> resultsByModelName) {
        this.resultsByModelName = resultsByModelName;
    }
}
