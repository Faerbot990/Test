package com.api.Autonova.models;

import com.fasterxml.jackson.databind.JsonNode;

public class ResponseTecDocAPI {

    private Integer status;
    private JsonNode data;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
