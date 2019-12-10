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
        this.clientId = clientId;
        this.bdHandler = bdHandler;
        
        brokerURI = System.getProperty("mqtt.server", "tcp://127.0.01:1883");
        outputMsg("MQTT broker URI: " + brokerURI);
        
        commandTopicName = String.format(TOPIC_COMMAND, clientId, handlerId);
        resultTopicName = String.format(TOPIC_RESULT, clientId);
        broadcastTopicName = String.format(TOPIC_BROADCAST, handlerId);
        outputMsg("Command Topic: " + commandTopicName);
        outputMsg("Result Topic: " + resultTopicName);
        outputMsg("Broadcast Topic: " + broadcastTopicName);
        
        String tmpDir = System.getProperty("java.io.tmpdir");
        outputMsg("Data store dir.: " + tmpDir);
        
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
        MqttConnectOptions connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        
        client = new MqttClient(brokerURI, clientId, dataStore);
        
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable thrwbl) {
                outputMsg("Connection lost. Try to exit...");
                System.exit(-1);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken imdt) {
                try {
                    outputMsg(String.format("Delivered - [%s] message: %s", new Date(), imdt.getMessage()));
                } catch (MqttException ex) {
                    Logger.getLogger(MqttCLIConsole.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.endsWith("broadcast")) {
                    JsonParser parser = new JsonParser();
                    JsonObject jsonObj = parser.parse(message.toString()).getAsJsonObject();
                    String msgType = jsonObj.get("type").getAsString();
                    if (msgType.equals(ChangeOfValue.TYPE)) {
                        bdHandler.saveBigData(new ChangeOfValue(jsonObj));
//                        outputMsg("Received broadcast message: " + message);
                    }
                    else {
                        outputMsg("Received broadcast message: " + message);
                    }
                }
                else if (topic.endsWith("result")) {
                    outputMsg("Result: " + message);
                }
                displayPrompt();
            }
        });
        
        client.connect(connOpt);
        client.subscribe(resultTopicName);
        client.subscribe(broadcastTopicName);
    }
    
    public void publish(String payload, int qos) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        client.publish(commandTopicName, message);
    }
    
    public void close() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
                client = null;
            } catch (MqttException ex) {
                Logger.getLogger(MqttCLIConsole.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void displayPrompt() {
        System.out.print("Input command or 'q' to exit: ");
    }
    
    public static void outputMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }    
}
