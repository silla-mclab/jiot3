/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.uart_dev;

import java.io.IOException;

/**
 *
 * @author yjkim
 */
public enum SHT11 {
    GET_TMP("TMP\n"),
    GET_HMD("HMD\n"),
    GET_ACK("ACK\n");
    
    public String cmd;
    
    private SHT11(String cmd) {
        this.cmd = cmd;
    }

    public void send(UARTRPI uart) throws IOException, InterruptedException {
        uart.sendSync(this.cmd);
    }
}
