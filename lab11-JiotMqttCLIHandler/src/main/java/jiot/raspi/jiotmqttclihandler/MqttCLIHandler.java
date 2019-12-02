/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.jiotmqttclihandler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.paho.client.mqttv3.MqttClient;

/**
 *
 * @author yjkim
 */
public class MqttCLIHandler {
    public static final String TOPIC_PREFIX = "jiot/mqtt/thing/";
    public static final String TOPIC_COMMAND = TOPIC_PREFIX + "+/%s/command";
    public static final String TOPIC_RESULT = TOPIC_PREFIX + "%s/result";
    public static final String TOPIC_BROADCAST = TOPIC_PREFIX + "%s/broadcast";
    
    private ExecutorService cmdExecThreadPool = Executors.newCachedThreadPool();
    private String brokerURI = null;
    private String clientId = null;
    private String commandTopicName = null;
    private String broadcastTopicName = null;
    private MqttClient client = null;

    
    public String getLocalIPAddress() throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        return localhost.getHostAddress().trim();
    }
    
    public void outputMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }

}
