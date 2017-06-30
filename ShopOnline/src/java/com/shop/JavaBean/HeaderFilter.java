/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.JavaBean;
import java.io.IOException; 
import javax.servlet.Filter; 
import javax.servlet.FilterChain; 
import javax.servlet.FilterConfig; 
import javax.servlet.ServletException; 
import javax.servlet.ServletRequest; 
import javax.servlet.ServletResponse; 
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author aries
 */
public class HeaderFilter implements Filter 
{ 
    @Override
    public void init(FilterConfig fc) throws ServletException {} 
    
    /**
     *  This function add the x-xss-protection to the header 
     *      that against xss attack in client's browser.
     * 
     * @param req request
     * @param res response
     * @param fc filter chain
     * @throws IOException
     * @throws ServletException 
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, 
            FilterChain fc) throws IOException, ServletException 
    { 
        HttpServletResponse response = (HttpServletResponse) res; 
        response.setHeader("X-XSS-Protection", "1; mode=block"); 
        fc.doFilter(req, res); 
    } 
 
    @Override
    public void destroy() {} 
}