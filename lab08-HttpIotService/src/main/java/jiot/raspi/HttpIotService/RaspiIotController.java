/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jiot.raspi.HttpIotService;

import java.util.Observable;
import java.util.Observer;
import javax.annotation.PreDestroy;
import jiot.raspi.things.ControlPoint;
import jiot.raspi.things.ControlPointContainer;
import jiot.raspi.things.OutputControlPoint;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author yjkim
 */
@RestController
public class RaspiIotController implements Observer {
    private ControlPointContainer cpContainer = null;

    public RaspiIotController() {
        cpContainer = ControlPointContainer.getInstance();
        cpContainer.start();
        
        cpContainer.getControlPoints().forEach((cp) -> {
            cp.addObserver(this);
        });
        
        System.out.println(">>> Start CP Container and register Observer to CPs...");
    }
    
    @PreDestroy
    public void close() {
        if (cpContainer != null) {
            cpContainer.stop();
            System.out.println(">>> Stop CP Container...");
        }
    }

    @Override
    public void update(Observable obj, Object arg) {
        if (obj instanceof ControlPoint) {
            ControlPoint cp = (ControlPoint)obj;
            if (arg == null) {
                write("[Observer] Changed value (" + cp.getName() +"): " + cp.getPresentValue());
            }
            else {
                if (arg.toString().equals("name")) {
                    write("[Observer] Changed name (" + cp.getName() +"): " + cp.getName());
                }
                else {
                    write("[Observer] Changed (" + cp.getName() +"): " + arg);
                }
            }
        }
    }
    
    private void write(String msg) {
        System.out.println(msg);
    }
    
    
    @RequestMapping(value="/ledOn/{ledId}", method=RequestMethod.GET)
    @ResponseBody
    public String turnOnLed(@PathVariable("ledId") String ledId) {
        int ledNum = Integer.parseInt(ledId);
        if (ledNum < 1 || ledNum > 3) {
            return "LED ID parameter is wrong, which must be from 1 to 3.";
        }
        else {
            int ledCPId = ControlPointContainer.ControlPointID.LED1.getId() + (ledNum-1);
            OutputControlPoint ledCP = (OutputControlPoint)cpContainer.getControlPoint(ledCPId);
            ledCP.setPresentValue(1);
            return "LED #" + ledId + " is turn on.";
        }
    }
    
    @RequestMapping(value="/ledOff/{ledId}", method=RequestMethod.GET)
    @ResponseBody
    public String turnOffLed(@PathVariable("ledId") String ledId) {
        int ledNum = Integer.parseInt(ledId);
        if (ledNum < 1 || ledNum > 3) {
            return "LED ID parameter is wrong, which must be from 1 to 3.";
        }
        else {
            int ledCPId = ControlPointContainer.ControlPointID.LED1.getId() + (ledNum-1);
            OutputControlPoint ledCP = (OutputControlPoint)cpContainer.getControlPoint(ledCPId);
            ledCP.setPresentValue(0);
            return "LED #" + ledId + " is turn off.";
        }
    }
    
    // Request URL: /getButton?btn=1
    @RequestMapping(value="/getButton", method=RequestMethod.GET)
    @ResponseBody
    public String getButton(@RequestParam("btn")String btnId) {
        int btnNum = Integer.parseInt(btnId);
        if (btnNum < 1 || btnNum > 2) {
            return "Button ID parameter is wrong, which must be from 1 to 2.";
        }
        else {
            int btnCPId = ControlPointContainer.ControlPointID.BTN1.getId() + (btnNum-1);
            ControlPoint btnCP = cpContainer.getControlPoint(btnCPId);
            int value = btnCP.getPresentValue();
            return "Button #" + btnId + " Input = " + value;
        }
    }
}
