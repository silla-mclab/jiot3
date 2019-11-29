/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.simplemqttpublisher;

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
public class SimpleMqttPublisher {
    private static final String CLIENT_ID = "simplemqttpublisher";
    private static final String TOPIC = "Sports";
    
    private MqttClient client = null;
    private String uri = null;

    public SimpleMqttPublisher() {
        uri = System.getProperty("mqtt.server");
        if (uri == null) uri = "tcp://localhost:1883";
        outputMsg("URI = " + uri);
        String tmpDir = System.getProperty("java.io.tmpdir");
        outputMsg("Temp Dir. = " + tmpDir);
        
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
        MqttConnectOptions connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        
        try {
            client = new MqttClient(uri, CLIENT_ID, dataStore);
            
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable thrwbl) {
                    outputMsg("Connection Lost. try to exit...");
                    System.exit(-1);
                }

                @Override
                public void messageArrived(String string, MqttMessage mm) throws Exception {
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken imdt) {
                    try {
                        outputMsg(String.format("Delivered - [%s] message: %s", new Date(), imdt.getMessage()));
                    } catch (MqttException ex) {
                        Logger.getLogger(SimpleMqttPublisher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            client.connect(connOpt);
        } catch (MqttException ex) {
            Logger.getLogger(SimpleMqttPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void publish(String payload, int qos) {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        if (client != null) {
            try {
                client.publish(TOPIC, message);
            } catch (MqttException ex) {
                Logger.getLogger(SimpleMqttPublisher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void close() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
                client = null;
            } catch (MqttException ex) {
                Logger.getLogger(SimpleMqttPublisher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void outputMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }
    
    public static void main(String[] args) {
        SimpleMqttPublisher publisher = new SimpleMqttPublisher();
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a message (or 'q' to exit): ");
        for (String msg = scanner.nextLine(); !msg.equals("q"); msg = scanner.nextLine()) {
            System.out.println("Enter a QoS level(0, 1, 2): ");
            int qos = Integer.parseInt(scanner.nextLine());
            outputMsg("Publishing '" + msg + "' with qos=" + qos + "...");
            publisher.publish(msg, qos);
            System.out.println("Enter a message (or 'q' to exit): ");
        }
        outputMsg("MQTT Publisher is closing...");
        publisher.close();
        scanner.close();        
    }
    
}
