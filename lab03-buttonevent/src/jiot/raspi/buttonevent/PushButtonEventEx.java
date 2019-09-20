/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.buttonevent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

/**
 *
 * @author yjkim
 */
public class PushButtonEventEx implements Runnable {
    private static final String LED_PIN = "GPIO17";
    private static final String LED_BTN = "GPIO23";
    private static final String EXIT_BTN = "GPIO24";
    private static final String PIR_PIN = "GPIO25";
    
    private GPIOPin ledPin = null;
    private GPIOPin ledBtnPin = null;
    private GPIOPin exitBtnPin = null;
    private GPIOPin pirPin = null;
    
    private volatile boolean togglingStop = false, exit = false;
    
    public PushButtonEventEx() throws IOException {
        this.ledPin = DeviceManager.open(LED_PIN, GPIOPin.class);
        this.ledBtnPin = DeviceManager.open(LED_BTN, GPIOPin.class);
        this.exitBtnPin = DeviceManager.open(EXIT_BTN, GPIOPin.class);
        this.pirPin = DeviceManager.open(PIR_PIN, GPIOPin.class);
        
        ledBtnPin.setInputListener(new PinListener() {
            @Override
            public void valueChanged(PinEvent pe) {
                if (!pe.getValue()) {
                    togglingStop = !togglingStop;
                }
            }
        });
        
        exitBtnPin.setInputListener(new PinListener() {
            @Override
            public void valueChanged(PinEvent pe) {
                if (!pe.getValue()) {
                    exit = true;
                }
            }
        });
        
        pirPin.setInputListener(new PinListener() {
            @Override
            public void valueChanged(PinEvent pe) {
                togglingStop = !pe.getValue();
            }
        });
        
        
        System.out.println("LED & button devices successfully opened...");  
    }
    
    public void close() {
        try {
            ledPin.close();
            ledBtnPin.close();
            exitBtnPin.close();
        } catch (IOException ex) {
            Logger.getLogger(PushButtonEventEx.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        System.out.println("Start LED toggling...");
        
        while (!exit) {
            if (!togglingStop) {
                try {
                    ledPin.setValue(true);
                    Thread.sleep(500);

                    ledPin.setValue(false);
                    Thread.sleep(500);
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(PushButtonEventEx.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
                
        System.out.println("Exit...");
        close();
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Thread t = new Thread(new PushButtonEventEx());
            t.start();
        } catch (IOException ex) {
            Logger.getLogger(PushButtonEventEx.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
