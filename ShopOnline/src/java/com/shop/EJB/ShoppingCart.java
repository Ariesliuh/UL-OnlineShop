/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.EJB;

import com.shop.Entities.Product;

/**
 *
 * @author Dongao
 */
public class ShoppingCart {
    private Product product;
    private int purchaseCount;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }

    
    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public ShoppingCart(Product product, int purchaseCount) {
        this.product = product;
        this.purchaseCount = purchaseCount;
    }
    
    public ShoppingCart() {
    }
    
   
    
}
