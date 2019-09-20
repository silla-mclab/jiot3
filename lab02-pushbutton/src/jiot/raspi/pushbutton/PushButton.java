/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.pushbutton;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;

/**
 *
 * @author yjkim
 */
public class PushButton {
    private static final String LED_PIN = "GPIO17";
    private static final String LED_BTN = "GPIO23";
    private static final String EXIT_BTN = "GPIO24";
    
    private GPIOPin ledPin;
    private GPIOPin ledBtnPin;
    private GPIOPin exitBtnPin;
    
    public PushButton() throws IOException {
        super();
        
        this.ledPin = DeviceManager.open(LED_PIN, GPIOPin.class);
        this.ledBtnPin = DeviceManager.open(LED_BTN, GPIOPin.class);
        this.exitBtnPin = DeviceManager.open(EXIT_BTN, GPIOPin.class);
        System.out.println("Devices successfully opened...");  
    }
    
    public void run() {
        boolean exit = false;
        boolean ledValue;
        
        while (!exit) {
            try {
                ledValue = !ledBtnPin.getValue();
                ledPin.setValue(ledValue);
                System.out.println("LED: " + (ledValue ? "On" : "Off"));

                exit = !exitBtnPin.getValue();
                Thread.sleep(1000);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(PushButton.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.out.println("Exit...");
        close();
    }
    
    public void close() {
        try {
            ledPin.close();
            ledBtnPin.close();
            exitBtnPin.close();
        } catch (IOException ex) {
            Logger.getLogger(PushButton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            PushButton pushButton = new PushButton();
            pushButton.run();
        } catch (IOException ex) {
            Logger.getLogger(PushButton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
