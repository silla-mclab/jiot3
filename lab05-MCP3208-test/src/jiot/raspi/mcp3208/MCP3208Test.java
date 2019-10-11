/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.mcp3208;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jiot.raspi.spi_dev.SPIRPI;
import jiot.raspi.spi_dev.drivers.MCP3208Device;

/**
 *
 * @author yjkim
 */
public class MCP3208Test implements Runnable {

    private MCP3208Device adDev = null; 
    
    public MCP3208Test() throws IOException {
        adDev = new MCP3208Device(SPIRPI.CE1);
    }

    @Override
    public void run() {
        try {
            System.out.println("Channel(0): " + adDev.analogRead(0));
            System.out.println("Channel(1): " + adDev.analogRead(1));
        } catch (IOException ex) {
            Logger.getLogger(MCP3208Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture future = executor.scheduleWithFixedDelay(this, 0, 1, TimeUnit.SECONDS);
        executor.schedule(() -> {
            future.cancel(false);
            executor.shutdown();
            adDev.close();
        }, 30, TimeUnit.SECONDS);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            MCP3208Test testObj = new MCP3208Test();
            testObj.start();
        } catch (IOException ex) {
            Logger.getLogger(MCP3208Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
