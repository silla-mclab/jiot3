/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.controlpointcoapserver;

import jiot.raspi.things.ControlPointContainer;
import org.eclipse.californium.core.CoapServer;

/**
 *
 * @author yjkim
 */
public class ControlPointCoAPServer extends CoapServer {
    private ControlPointContainer cpContainer = null;

    @Override
    public synchronized void start() {
        cpContainer = ControlPointContainer.getInstance();
        cpContainer.start();
        
        cpContainer.getControlPoints().forEach((cp) -> {
            this.add(new ControlPointResource(cp));
        });
        
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public synchronized void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
        
        if (cpContainer != null) {
            cpContainer.stop();
        }
    }
}
