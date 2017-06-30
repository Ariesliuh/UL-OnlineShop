/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.EJB;

import com.shop.EJB.util.JsfUtil;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author aries
 */
@JMSDestinationDefinition(name = "java:app/jms/ProductMessageBean", interfaceName = "javax.jms.Queue", resourceAdapter = "jmsra", destinationName = "ProductMessageBean")
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/ProductMessageBean")
    ,
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ProductMessageBean implements MessageListener {
    @Resource
    private MessageDrivenContext mdContext;
    
    @PersistenceContext(unitName = "ShopOnlinePU")
    private EntityManager em;
    
    public ProductMessageBean() {
    }
    
    /**
     * This method listening sendMessage event.
     * 
     * @param message 
     */
    @Override
    public void onMessage(Message message) {
        
        TextMessage msg;
        try {
            if (message instanceof TextMessage) {
                msg = (TextMessage) message;  
                // add the message to log
                JsfUtil.addLog(((TextMessage) message).getText());
            }
        } catch (Throwable te) {
        }
    }

    public void save(Object object) {
        em.persist(object);
    }
    
}
