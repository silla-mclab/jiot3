/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.jiotmqttcliclient;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author yjkim
 */
public class MqttCLIClient {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        outputPrompt("Input client ID: ");
        String clientId = scanner.nextLine();
        outputPrompt("Input CLI handler ID: ");
        String handlerId = scanner.nextLine();
        BigDataHandler bdHandler = new BigDataHandler();
        
        try {
            MqttCLIConsole.outputMsg("MQTT CLI Console connecting...");
            MqttCLIConsole console = new MqttCLIConsole(clientId, handlerId, bdHandler);
            
            console.displayPrompt();
            for (String line=scanner.nextLine(); 
                !line.trim().equals("q"); line=scanner.nextLine()) {
                line = line.trim();
                
                if (line.trim().length() == 0)
                    continue;
                
                if (line.equals("display")) {
//                    bdHandler.displayBigData();
                    console.displayPrompt();
                }
                else if (line.equals("clear")) {
//                    bdHandler.clearBigData();
                    console.displayPrompt();
                }
                else {
                    console.publish(line, 0);
                    MqttCLIConsole.outputMsg("Waitting result...");
                }
            }
            console.close();
        } catch (MqttException ex) {
            Logger.getLogger(MqttCLIClient.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private static void outputPrompt(String msg) {
        System.out.print(msg);
    } 
}
