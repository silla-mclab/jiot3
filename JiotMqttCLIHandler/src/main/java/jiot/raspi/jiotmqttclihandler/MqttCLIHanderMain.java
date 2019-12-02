/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.jiotmqttclihandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import jiot.raspi.things.ControlPointContainer;

/**
 *
 * @author yjkim
 */
public class MqttCLIHanderMain {
    private static ControlPointContainer cpContainer = null;
    private static MqttCLIHandler cliHandler = null;
    
    public static void close() {
        if (cliHandler != null)  cliHandler.close();
        if (cpContainer != null)  cpContainer.stop();
    }
    
    public static void main(String[] args) {
        try {
            cpContainer = ControlPointContainer.getInstance();
            cpContainer.start();
            
            cliHandler = new MqttCLIHandler();
            cpContainer.getControlPoints().forEach((cp) -> {
                cp.addObserver(cliHandler);
            });
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("[DEBUG] MQTT CLI Handler is shutdowed... ");
                    close();
                }
            });
            
            for(;;) {
                Thread.sleep(1000);
            }
            
        } catch (Exception ex) {
            Logger.getLogger(MqttCLIHanderMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
