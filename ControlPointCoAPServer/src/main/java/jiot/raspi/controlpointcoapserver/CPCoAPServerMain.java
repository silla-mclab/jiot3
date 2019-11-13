/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.controlpointcoapserver;

/**
 *
 * @author yjkim
 */
public class CPCoAPServerMain {
    
    private static Thread mainThread; 
    
    public static void main(String[] args) {
        mainThread = Thread.currentThread();
        
        ControlPointCoAPServer server = new ControlPointCoAPServer();
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("CoAP server is now shutdowned...");
                server.stop();
                mainThread.interrupt();
            }          
        });
    }
    
}
