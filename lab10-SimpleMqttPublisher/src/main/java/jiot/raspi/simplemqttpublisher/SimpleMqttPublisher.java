/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.simplemqttpublisher;

import java.util.Scanner;

/**
 *
 * @author yjkim
 */
public class SimpleMqttPublisher {
    
    private static void outputMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }
    
    public static void main(String[] args) {
        // To-do: create a SimpleMqttPublisher object
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a message (or 'q' to exit): ");
        for (String msg = scanner.nextLine(); !msg.equals("q"); msg = scanner.nextLine()) {
            System.out.println("Enter a QoS level(0, 1, 2): ");
            int qos = Integer.parseInt(scanner.nextLine());
            outputMsg("Publishing '" + msg + "' with qos=" + qos + "...");
            
            // To-do: publish input message...
            
            System.out.println("Enter a message (or 'q' to exit): ");
        }
        outputMsg("MQTT Publisher is closing...");
        // To-do: close MQTT client
        scanner.close();        
    }
    
}
