/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.simplemqttsubscriber;

import java.util.Scanner;

/**
 *
 * @author yjkim
 */
public class SimpleMqttSubscriber {
    
    private static void outputMsg(String msg) {
        System.out.println("[DEBUG] " + msg);
    }
    
    public static void main(String[] args) {
        // To-do:
        // create a SimpleMqttSubscriber object
        // start subscribing by MQTT client...
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input enter to exit...");
        scanner.nextLine();
        
        scanner.close();
        outputMsg("MQTT subscriber is closing...");
        // To-do: close MQTT client...
    }    
}
