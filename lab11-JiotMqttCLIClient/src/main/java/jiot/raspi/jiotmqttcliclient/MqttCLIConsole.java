/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.jiotmqttcliclient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Date;
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
public class MqttCLIConsole {
    public static final String TOPIC_PREFIX = "jiot/mqtt/thing/";
    public static final String TOPIC_COMMAND = TOPIC_PREFIX + "%s/%s/command";
    public static final String TOPIC_RESULT = TOPIC_PREFIX + "%s/result";
    public static final String TOPIC_BROADCAST = TOPIC_PREFIX + "%s/broadcast";
    
    private String brokerURI = null;
    private String clientId = null;
    private String commandTopicName = null;
    private String resultTopicName = null;
    private String broadcastTopicName = null;
    private MqttClient client = null;
    private BigDataHandler bdHandler = null;

    public MqttCLIConsole(String clientId, String handlerId, BigDataHandler bdHandler) throws MqttException {
    }
    
    public void publish(String payload, int qos) throws MqttException {
    }
    
    public void close() {
    }
    
    public void displayPrompt() {
        System.out.print("Input command or 'q' to exit: ");
    }
    
    public static void outputMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }    
}
