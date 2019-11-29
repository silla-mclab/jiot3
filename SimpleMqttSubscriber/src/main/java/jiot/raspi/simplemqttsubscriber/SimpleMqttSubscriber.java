/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.simplemqttsubscriber;

import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 *
 * @author yjkim
 */
public class SimpleMqttSubscriber {
    public static final String CLIENT_ID = "simplemqttsubscriber";
    public static final String TOPIC = "Sports";
    
    private MqttClient client = null;
    private String uri = null;

    public SimpleMqttSubscriber() {
        uri = System.getProperty("mqtt.server", "tcp://localhost:1883");
        outputMsg("URI: " + uri);
        String tmpDir = System.getProperty("java.io.tmpdir");
        outputMsg("Temp Dir.: " + tmpDir);
        
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
        MqttConnectOptions connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        
        try {
            client = new MqttClient(uri, CLIENT_ID, dataStore);
            
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable thrwbl) {
                    outputMsg("Connection lost. try to exit...");
                    System.exit(-1);
                }

                @Override
                public void messageArrived(String string, MqttMessage mm) throws Exception {
                    outputMsg(String.format("Arrived - [%s] message: %s", new Date(), mm));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken imdt) {
                }
            });
            
            client.connect(connOpt);
        } catch (MqttException ex) {
            Logger.getLogger(SimpleMqttSubscriber.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void subscribe() {
        try {
            client.subscribe(TOPIC);
        } catch (MqttException ex) {
            Logger.getLogger(SimpleMqttSubscriber.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
                client = null;
            } catch (MqttException ex) {
                Logger.getLogger(SimpleMqttSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void outputMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }
    
    public static void main(String[] args) {
        SimpleMqttSubscriber subscriber = new SimpleMqttSubscriber();
        subscriber.subscribe();
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input enter to exit...");
        scanner.nextLine();
        
        outputMsg("MQTT subscriber is closing...");
        subscriber.close();
        scanner.close();
    }    
}
