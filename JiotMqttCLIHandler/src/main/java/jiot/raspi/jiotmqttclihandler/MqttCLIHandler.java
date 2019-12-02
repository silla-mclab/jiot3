/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.jiotmqttclihandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import jiot.raspi.cli.CommandInterpreter;
import jiot.raspi.things.ControlPoint;
import jiot.raspi.things.FloatInputSupport;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 *
 * @author yjkim
 */
public class MqttCLIHandler implements Observer {
    public static final String TOPIC_PREFIX = "jiot/mqtt/thing/";
    public static final String TOPIC_COMMAND = TOPIC_PREFIX + "+/%s/command";
    public static final String TOPIC_RESULT = TOPIC_PREFIX + "%s/result";
    public static final String TOPIC_BROADCAST = TOPIC_PREFIX + "%s/broadcast";
    
    private ExecutorService cmdExecThreadPool = Executors.newCachedThreadPool();
    private String brokerURI = null;
    private String clientId = null;
    private String commandTopic = null;
    private String broadcastTopic = null;
    private MqttClient client = null;

    public MqttCLIHandler() throws UnknownHostException, Exception {
        brokerURI = System.getProperty("mqtt.server", "tcp://127.0.0.1:1883");
        outputMsg("MQTT broker URI: " + brokerURI);
        
        String ipAddress = getLocalIPAddress();
        if (ipAddress == null) {
            throw new Exception("Cannot find IP address of this thing");
        }
        outputMsg("Thing's IP address: " + ipAddress);
//        clientId = "cli_" + ipAddress.replace('.', '_');
        clientId = "cli";
        
        commandTopic = String.format(TOPIC_COMMAND, clientId);
        broadcastTopic = String.format(TOPIC_BROADCAST, clientId);
        outputMsg("Command topic: " + commandTopic);
        outputMsg("Broadcast topic: " + broadcastTopic);
        
        String tmpDir = System.getProperty("java.io.tmpdir");
        outputMsg("Data store dir.: " + tmpDir);
        
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence();
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
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                final String[] clientInfo = topic.substring(TOPIC_PREFIX.length()).split("/");
                final String commandStr = message.toString();
                outputMsg("Message received from " + topic +": " + message);
                
                cmdExecThreadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        String[] command = commandStr.split(" ");
                        String result = null;
                        
                        result = CommandInterpreter.getInstance().execute(command);
                        if (result == null || result.length() == 0) 
                            result = "OK";
                        
                        try {                       
                            publish(clientInfo[0], result, 0);
                        } catch (MqttException ex) {
                            Logger.getLogger(MqttCLIHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken imdt) {
                try {
                    outputMsg(String.format("Delivered - [%s] message: %s", new Date(), imdt.getMessage()));
                } catch (MqttException ex) {
                    Logger.getLogger(MqttCLIHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }      
        });
        
        client.connect(connOpt);
        outputMsg("Connect successfully to MQTT Broker...");
        client.subscribe(commandTopic);
    }
            
    public void publish(String subClientId, String payload, int qos) throws MqttException {
        MqttTopic resultTopic = client.getTopic(String.format(TOPIC_RESULT, subClientId));
        resultTopic.publish(payload.getBytes(), qos, false);
//        outputMsg("Published result: " + payload);
    }
    
    public void broadcast(String payload) throws MqttException {
        MqttTopic topic = client.getTopic(broadcastTopic);
        topic.publish(payload.getBytes(), 0, false);
    }
        
    public void close() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
                client = null;
            } catch (MqttException ex) {
                Logger.getLogger(MqttCLIHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String getLocalIPAddress() throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        return localhost.getHostAddress().trim();
    }
    
    public void outputMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }

    @Override
    public void update(Observable obv, Object arg) {
        if (obv instanceof ControlPoint) {
            ControlPoint cp = (ControlPoint)obv;
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("type", "cov");
            jsonObj.addProperty("handlerId", clientId);
            jsonObj.addProperty("pointId", cp.getId());
            jsonObj.addProperty("pointName", cp.getName());
            jsonObj.addProperty("value", 
                String.valueOf((cp instanceof FloatInputSupport)? ((FloatInputSupport)cp).getFloatValue() : cp.getPresentValue()));
            String payload = (new Gson()).toJson(jsonObj);
            outputMsg(String.format("Control Point(%s)'s state is changed...", cp.getName()));
            try {
                broadcast(payload);
            } catch (MqttException ex) {
                Logger.getLogger(MqttCLIHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
