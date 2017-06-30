/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.EJB;

import com.shop.EJB.util.JsfUtil;
import com.shop.Entities.Customer;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author aries
 */
@Stateful
public class CustomerFacade extends AbstractFacade<Customer> {

    @PersistenceContext(unitName = "ShopOnlinePU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CustomerFacade() {
        super(Customer.class);
    }
    
    /**
     * This method get customers by customer name.
     * 
     * @param customerName
     * @return customers
     */
    public List<Customer> findByName(String customerName) {
        List<Customer> c = new ArrayList<>();
        try{
            c = (List<Customer>) this.em.createNamedQuery("Customer.findByName")
                .setParameter("name", customerName).getResultList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "There is no customer "+ customerName);
        }
        return c;
    }
    
}
