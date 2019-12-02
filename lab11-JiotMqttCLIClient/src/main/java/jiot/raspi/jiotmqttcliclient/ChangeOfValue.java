/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.jiotmqttcliclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 *
 * @author yjkim
 */
public class ChangeOfValue {
    public static final String TYPE = "cov";
    
    private String handlerId;
    private int pointId;
    private String pointName;
    private float value;
    
    public ChangeOfValue(String handlerId, int pointId, String pointName, float value) {
        this.handlerId = handlerId;
        this.pointId = pointId;
        this.pointName = pointName;
        this.value = value;
    }
    
    public ChangeOfValue(JsonObject jsonObj) {
        this.handlerId = jsonObj.get("handlerId").getAsString();
        this.pointId = jsonObj.get("pointId").getAsInt();
        this.pointName = jsonObj.get("pointName").getAsString();
        this.value = jsonObj.get("value").getAsFloat();
    }

    public String getHandlerId() {
        return handlerId;
    }

    public int getPointId() {
        return pointId;
    }

    public String getPointName() {
        return pointName;
    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("type", TYPE);
        jsonObj.addProperty("handlerId", handlerId);
        jsonObj.addProperty("pointId", pointId);
        jsonObj.addProperty("pointName", pointName);
        jsonObj.addProperty("value", value);
        return (new Gson()).toJson(jsonObj);       
    }  
}
