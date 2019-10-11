/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.mcp3208;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.spibus.SPIDevice;

/**
 *
 * @author yjkim
 */
public class MCP3208AsyncPolling implements Runnable {

    private static final int CMD_BIT = 0x060000;        // 24-bit command
    
    private SPIDevice spiDev = null;
    private GPIOPin ssPin = null;

    public MCP3208AsyncPolling(String spiDevId, String ssPinId) throws IOException {
        this.spiDev = DeviceManager.open(spiDevId, SPIDevice.class);
        this.ssPin = DeviceManager.open(ssPinId, GPIOPin.class);        
    }
    
    public void close() throws IOException {
        if (spiDev != null)  spiDev.close();
        if (ssPin != null)  ssPin.close();         
    }
    
    public int analogRead(int channel) throws IOException {
        ByteBuffer out = ByteBuffer.allocate(3);
        ByteBuffer in = ByteBuffer.allocate(3);
        
        channel = (channel << 14) | CMD_BIT;
        out.put((byte)((channel >> 16) & 0xff));
        out.put((byte)((channel >> 8) & 0xff));
        out.put((byte)(channel  & 0xff));
        out.flip();
        
        ssPin.setValue(false);       // MCP3208 CS - Active Low
        spiDev.writeAndRead(out, in);
        ssPin.setValue(true);
        
        int highByte = (int)(in.get(1) & 0x1f);
        int lowByte = (int)(in.get(2) & 0xff);
        
        return (highByte << 8) | lowByte;
    }

    @Override
    public void run() {
        try {
            System.out.println("Channel(0): " + analogRead(0));
            System.out.println("Channel(1): " + analogRead(1));
        } catch (IOException ex) {
            Logger.getLogger(MCP3208AsyncPolling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void start() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture future = executor.scheduleWithFixedDelay(this, 0, 1, TimeUnit.SECONDS);
        executor.schedule(new Runnable() { 
            @Override
            public void run() {
                future.cancel(false);
                executor.shutdown();
                try {
                    close();
                } catch (IOException ex) {
                    Logger.getLogger(MCP3208AsyncPolling.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 30, TimeUnit.SECONDS);
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            MCP3208AsyncPolling adcDev;
            
            adcDev = new MCP3208AsyncPolling("SPI0.1", "GPIO7");
            adcDev.start();
        } catch (IOException ex) {
            Logger.getLogger(MCP3208AsyncPolling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
