/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.EJB;

import com.shop.EJB.util.JsfUtil;
import com.shop.Entities.OrderDetail;
import com.shop.Entities.Orders;
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
public class OrdersFacade extends AbstractFacade<Orders> {

    @PersistenceContext(unitName = "ShopOnlinePU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
     /**
     * This method gets the product from database and returns the product object
     * 
     * @param productId finds the product by product Id
     * @return the product.
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
    
    /**
     * This method gets the order list from database and returns the order list
     * 
     * @param CustomerId finds the orders by CustomerId
     * @return the  order list.
     */
     
    public List<Orders> findOrderByCustomerId(Integer CustomerId) throws JMSException {
        List<Orders> orders = new ArrayList<>();
        try{
            orders = (List<Orders>) this.em.createNamedQuery("Orders.findByCustomerId")
                .setParameter("customerId", CustomerId).getResultList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "There is no order by "+ CustomerId);
        }
        
        System.out.println(" Facade:  "+orders.size());
        return orders;
    }
      
     /**
     * This method gets the OrderDetail list from database and returns the OrderDetail list
     * 
     * @param orderId finds the orderdetail by orderId
     * @return the OrderDetail list.
     */
    public List<OrderDetail> findDetailByOrderId(Integer orderId) throws JMSException {
        List<OrderDetail> orderDetails = new ArrayList<>();
        try{
            orderDetails = (List<OrderDetail>) this.em.createNamedQuery("OrderDetail.findByOrderId")
                .setParameter("orderId", orderId).getResultList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "There is no order details by "+ orderId);
        }
       
       System.out.println("Order Details List:   "  +  orderDetails.size() +"  Order Id:   "+orderId);
       return orderDetails;
    }
    
    /**
     * This method gets the Orders from database and returns the Orders object
     * 
     * @param orderId finds the orders by orderId
     * @return the Orders objectt.
     */  
     public Orders findOrderByORderId (Integer orderId) {
        Orders order = new Orders();
        try{
            order =  (Orders)this.em.createNamedQuery("Orders.findByOrderId")
                .setParameter("orderId", orderId).getSingleResult();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "There is no product by "+ orderId);
        }
        return order;
    }
    
    
      
         
    public OrdersFacade() {
        super(Orders.class);
    }
    
}
