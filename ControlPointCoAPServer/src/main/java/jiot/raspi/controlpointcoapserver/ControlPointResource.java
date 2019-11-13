/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.controlpointcoapserver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Observable;
import java.util.Observer;
import jiot.raspi.things.ControlPoint;
import jiot.raspi.things.FloatInputSupport;
import jiot.raspi.things.OutputControlPoint;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 *
 * @author yjkim
 */
public class ControlPointResource extends CoapResource {
    private ControlPoint cp = null;
    
    public ControlPointResource(ControlPoint cp) {
        super(String.valueOf(cp.getId()));
        this.cp = cp;
        addChildResources();
    }
    
    private String stringfyJsonWithSingleProperty(String name, String value) {
        JsonObject object = new JsonObject();
        object.addProperty(name, value);
        return (new Gson()).toJson(object);
    }
    
    private String getPropertyFromJson(String json, String prop) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        return element.getAsJsonObject().get(prop).getAsString();
    }
    
    private void addChildResources() {
        add(new CoapResource("properties") {
            private CoapResource initialize() {
                setObservable(true);
                setObserveType(CoAP.Type.CON);
                getAttributes().setObservable();
                
                cp.addObserver(new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        if (arg != null) {
                            changed();
                        }
                    }
                });
                
                return this;
            }

            @Override
            public void handlePOST(CoapExchange exchange) {
                String jsonStr = exchange.getRequestText();
                String response = null;
                System.out.println("[DEBUG] POST " + getURI() + " " + jsonStr);
                if (jsonStr != null) {
                    String name = getPropertyFromJson(jsonStr, "Name");
                    if (name != null) {
                        cp.setName(name);
                        response = stringfyJsonWithSingleProperty("result", "true");
                    }
                    else {
                        response = stringfyJsonWithSingleProperty("result", "Illegal post request");
                    }
                }
                else {
                    response = stringfyJsonWithSingleProperty("result", "No payload");
                }
                System.out.println("[DEBUG] Response " + (response != null ? response : "Error"));
            }

            @Override
            public void handleGET(CoapExchange exchange) {
                System.out.println("[DEBUG] GET " + getURI());
                JsonObject object = new JsonObject();
                object.addProperty("Id", cp.getId());
                object.addProperty("Type", cp.getType().name());
                object.addProperty("Name", cp.getName());
                object.addProperty("Enbaled", cp.isEnabled());
                String response = (new Gson()).toJson(object);
                exchange.respond(response);
                System.out.println("[DEBUG] Response " + response);
            }
            
        }.initialize());
    
        add(new CoapResource("value") {
            private CoapResource initialize() {
                setObservable(true);
                setObserveType(CoAP.Type.CON);
                getAttributes().setObservable();
                
                cp.addObserver(new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        if (arg != null) {
                            changed();
                        }
                    }
                });
                
                return this;
            }

            @Override
            public void handlePOST(CoapExchange exchange) {
                String jsonStr = exchange.getRequestText();
                String response = null;
                System.out.println("[DEBUG] POST " + getURI() + " " + jsonStr);
                if ((cp instanceof OutputControlPoint) & (jsonStr != null)) {
                    int value = Integer.parseInt(getPropertyFromJson(jsonStr, "value"));
                    ((OutputControlPoint)cp).setPresentValue(value);
                    response = stringfyJsonWithSingleProperty("result", "true");
                }
                else {
                    response = stringfyJsonWithSingleProperty("result", "No payload or CP is not output type");
                }
                System.out.println("[DEBUG] Response " + (response != null ? response : "Error"));
            }

            @Override
            public void handleGET(CoapExchange exchange) {
                System.out.println("[DEBUG] GET " + getURI());
                String response = stringfyJsonWithSingleProperty("value", 
                    String.valueOf(cp instanceof FloatInputSupport ? ((FloatInputSupport)cp).getFloatValue() : cp.getPresentValue()));
                exchange.respond(response);
                System.out.println("[DEBUG] Response " + response);
            }
            
        }.initialize());
    }
}
