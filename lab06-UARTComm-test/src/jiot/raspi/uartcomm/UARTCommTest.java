/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.uartcomm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jiot.raspi.uart_dev.drivers.SHT11Device;

/**
 *
 * @author yjkim
 */
public class UARTCommTest {
    private SHT11Device sht11Dev = null;
    
    public UARTCommTest() throws IOException {
        sht11Dev = new SHT11Device();
    }
    
    public void run() throws InterruptedException, IOException {
        while (!sht11Dev.isActive()) {
            System.out.println("SHT11 device not active yet...");
            Thread.sleep(5000);
        }
        System.out.println("SHT11 device is active...");
        
        for (int i=0; i<10; i++) {
            System.out.println("Temperature = " + sht11Dev.getTemperature());
            Thread.sleep(500);
            System.out.println("Humidity = " + sht11Dev.getHumidity());
            Thread.sleep(1000);
        }
        sht11Dev.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UARTCommTest test = new UARTCommTest();
            test.run();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(UARTCommTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
