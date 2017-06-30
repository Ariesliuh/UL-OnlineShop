/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.EJB;

import com.shop.Entities.OrderStatus;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author aries
 */
@Stateless
public class OrderStatusFacade extends AbstractFacade<OrderStatus> {

    @PersistenceContext(unitName = "ShopOnlinePU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
   
    public OrderStatusFacade() {
        super(OrderStatus.class);
    }
    
}
