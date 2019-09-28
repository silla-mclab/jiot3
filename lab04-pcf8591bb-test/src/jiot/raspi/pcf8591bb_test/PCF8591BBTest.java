/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.pcf8591bb_test;

import java.io.IOException;
import jiot.raspi.i2c_dev.I2CUtils;
import jiot.raspi.i2c_dev.drivers.PCF8591Device;

/**
 *
 * @author yjkim
 */
public class PCF8591BBTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            PCF8591Device adConverter = new PCF8591Device();
            
            adConverter.analogRead(0);
            I2CUtils.I2Cdelay(500);
            adConverter.analogRead(0);
            I2CUtils.I2Cdelay(500);
            System.out.println("Potentimeter Input: " + adConverter.analogRead(0));
            I2CUtils.I2Cdelay(1000);
            
            adConverter.analogRead(1);
            I2CUtils.I2Cdelay(500);
            adConverter.analogRead(01);
            I2CUtils.I2Cdelay(500);
            System.out.println("Photoregister Input: " + adConverter.analogRead(1));
            I2CUtils.I2Cdelay(1000);
            
            System.out.println("LED Dimming...");
            for (int i=255; i>=0; i-=10) {
                adConverter.analogWrite(i);
                I2CUtils.I2Cdelay(250);
            }
            
            adConverter.analogWrite(0);     // LED Off            
        } catch(IOException ex) {
            System.out.println("WARNING: " + ex.getMessage());
        }
    }
    
}
