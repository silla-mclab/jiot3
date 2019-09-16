/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi;

import java.io.IOException;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;

/**
 *
 * @author yjkim
 */
public class HelloLED {
    public static final String LED_PIN="GPIO17";
    public static final int INTERVAL = 1000;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("LED Blinking using Device IO Library...");
        
        try {
            GPIOPin ledPin = DeviceManager.open(LED_PIN, GPIOPin.class);
            boolean value = false;
            for (int i=0; i<20; i++) {
                ledPin.setValue(value = !value);
                if (value)  System.out.println("LED On...");
                else  System.out.println("LED Off...");
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException ex) {
//            Logger.getLogger(HelloLED.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }
}
