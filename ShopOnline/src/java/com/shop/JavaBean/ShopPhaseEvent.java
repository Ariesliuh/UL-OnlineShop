package com.shop.JavaBean;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aries
 */
import com.shop.Entities.Users;
import com.sun.faces.context.SessionMap;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class ShopPhaseEvent implements PhaseListener {
    private static final String ROOTURI = "/ShopOnline/faces";

    @Override
    public void afterPhase(PhaseEvent event) {
    }

    /**
     * When a xhtml is visited, this function will be call before rendering.
     * @param event 
     */
    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();

        int userType = -1;
        SessionMap map = (SessionMap) fc.getExternalContext().getSessionMap();
        if (!map.isEmpty()) {
            Users user = (Users) map.get("user");
            if (user != null) 
                userType = user.getRoleId().getRoleId();
        }
        String viewid = fc.getViewRoot().getViewId();
        
        // limit anonymous only visit Login & LoginFailure
        if (userType < 0) {
            if (!viewid.equals("/users/Login.xhtml") && !viewid.equals("/users/LoginFailure.xhtml")) {
                try {
                    // redirect current page
                    fc.getExternalContext().redirect(getURI("/users/Login.xhtml"));
                } catch (IOException ex) {
                    Logger.getLogger(ShopPhaseEvent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }  
        
        // limit normal user can not visit product operation
        if (userType == 1) {
            if (viewid.contains("/product")) {
                try {
                    // redirect current page
                    fc.getExternalContext().redirect(getURI("/index.xhtml"));
                } catch (IOException ex) {
                    Logger.getLogger(ShopPhaseEvent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        // limit admin can not visit customer, shoppingcart and ordersuccess pages
        if (userType == 2) {
            if (viewid.contains("/customer")) {
                try {
                    // redirect current page
                    fc.getExternalContext().redirect(getURI("/index.xhtml"));
                } catch (IOException ex) {
                    Logger.getLogger(ShopPhaseEvent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (viewid.contains("shoppingcart") 
                    || viewid.contains("ordersuccess")) {
                try {
                    // redirect current page
                    fc.getExternalContext().redirect(getURI("/index.xhtml"));
                } catch (IOException ex) {
                    Logger.getLogger(ShopPhaseEvent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }     
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
    
    private String getURI(String viewId){
        return ROOTURI+viewId;
    }
}