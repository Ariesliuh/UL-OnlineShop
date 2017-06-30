/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.EJB;

import com.shop.EJB.util.JsfUtil;
import com.shop.Entities.Product;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author aries
 */
@Stateless
public class ProductFacade extends AbstractFacade<Product> {

    @PersistenceContext(unitName = "ShopOnlinePU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    /**
     * This method get product by productName.
     * 
     * @param productName the name of product
     * @return the product
     * @throws javax.jms.JMSException
     */
    public List<Product> findByProductName(String productName) throws JMSException {
        List<Product> products = new ArrayList<>();
        try{
            products = (List<Product>) this.em.createNamedQuery("Product.findByProductName")
                .setParameter("productName", productName).getResultList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "There is no product by "+ productName);
        }
        System.out.println("com.shop.EJB.ProductFacade.findByProductName()");
        return products;
    }
    
    /**
     * This method get product by id. 
     *  (This method could be replaced by super class find(id) function )
     * 
     * @param productId 
     * @return found product
     */
    public Product findByProductId(Integer productId) {
        Product products = new Product();
        try{
            products =  (Product)this.em.createNamedQuery("Product.findByProductId")
                .setParameter("productId", productId).getSingleResult();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "There is no product by "+ productId);
        }
        return products;
    }
    
    public ProductFacade() {
        super(Product.class);
    }
    
}
