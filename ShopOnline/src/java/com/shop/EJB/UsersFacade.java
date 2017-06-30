/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.EJB;

import com.shop.Entities.Users;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author aries
 */
@Stateful
public class UsersFacade extends AbstractFacade<Users> {

    @PersistenceContext(unitName = "ShopOnlinePU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    /**
     * This method gets the user from database and returns the user's detail.
     * 
     * @param username finds the user by username
     * @return the user.
     */
    public Users findByUserName(String username) {
        Users currentUser = null;
        try{
            currentUser = (Users) this.em.createNamedQuery("Users.findByUsername")
                .setParameter("username", username).getSingleResult();
        } catch (Exception e) {
            
        }
        return currentUser;
    }
    
    public UsersFacade() {
        super(Users.class);
    }
    
    
    
}
