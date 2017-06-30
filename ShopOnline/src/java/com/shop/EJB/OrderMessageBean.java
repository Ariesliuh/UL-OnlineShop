/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.EJB;

import com.shop.EJB.util.JsfUtil;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author aries
 */
@JMSDestinationDefinition(name = "java:app/jms/OrderMessageBean", interfaceName = "javax.jms.Queue", resourceAdapter = "jmsra", destinationName = "OrderMessageBean")
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:app/jms/OrderMessageBean")
    ,
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class OrderMessageBean implements MessageListener {
    
    public OrderMessageBean() {
    }
    
    @Override
    public void onMessage(Message message) {
        TextMessage msg;
        try {
            if (message instanceof TextMessage) {
                msg = (TextMessage) message;   
                JsfUtil.addLog(((TextMessage) message).getText());
            }
            
        } catch (Throwable te) {
        }
    }
    
}
