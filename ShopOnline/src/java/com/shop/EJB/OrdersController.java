package com.shop.EJB;

import com.shop.Entities.Orders;
import com.shop.EJB.util.JsfUtil;
import com.shop.EJB.util.PaginationHelper;
import com.shop.Entities.OrderDetail;
import com.shop.Entities.OrderStatus;
import com.shop.Entities.Product;
import com.shop.Entities.Users;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.jms.JMSException;
import javax.servlet.http.HttpServletResponse;

@Named("ordersController")
@SessionScoped
public class OrdersController implements Serializable {

    private OrderList current;
    private DataModel items = null;
    private List<ShoppingCart> shoppingCart = new ArrayList<>();
    private List<OrderList> List_Order = new ArrayList<>(); 
    private List<OrderDetailList> List_OrderDetail = new ArrayList<>();
    @EJB
    private com.shop.EJB.OrdersFacade ejbFacade;
    @EJB
    private com.shop.EJB.OrderDetailFacade DetailejbFacade;
    @EJB
    private com.shop.EJB.ProductFacade proejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private int orderID;
    private Orders orderCopy;
    private int datamodelStatus;
    public OrdersController() {
    }
    
    public void setItems(DataModel model) {
        this.items = null;
        this.items = model;
   }

    public int getOrderID() {
        return orderID;
    }
    
   /**
     * This method add the item into shopping cart.
     * 
     * @param event
     * 
     */
    public void setShoppingCart(ActionEvent event) {
        Product attrvalue1 = (Product)event.getComponent().getAttributes().get("selectproductid");
        int index = searchListItem(shoppingCart,attrvalue1);
        //check whether the item exists in the shopping cart already, if have, add the count, if not, add items
        if (index >= 0){
            if (shoppingCart.get(index).getPurchaseCount()< attrvalue1.getCount()){
                int count = shoppingCart.get(index).getPurchaseCount() + 1;
                shoppingCart.get(index).setPurchaseCount(count);
                //System.out.println("Same add,  Count: "+count +" Index:  "+index);
            }else{
                JsfUtil.addErrorMessage("Can't add this item continue!");
            }
        }else{
            ShoppingCart e = new ShoppingCart();
            e.setProduct(attrvalue1);
            e.setPurchaseCount(1);
            shoppingCart.add(e);
            //System.out.println("New add,  indexOf: "+ shoppingC.indexOf(attrvalue1));
            System.out.println("Size:  " + shoppingCart.size());
        }
        System.out.println("SUCCESSFUL:   "+ attrvalue1.getProductName());
        DataModel dm = new ListDataModel(shoppingCart);
        setItems(dm);
    }
    
    /**
     * This method remove the shopping cart items
     * 
     * @param event
     * 
     */
    public void RemoveCartItem(ActionEvent event){
        ShoppingCart reItem = (ShoppingCart)event.getComponent().getAttributes().get("selectproductid");       
        shoppingCart.remove(reItem);
        DataModel dm = new ListDataModel(shoppingCart);
        setItems(dm);
    }
    public int getShoppingCartCount(){
        return shoppingCart.size();
    }
    
    public int getItemRow(){
        return List_Order.size();
    }
    
    /**
     * This method increase the shopping cart items count.
     * 
     * @param event
     * 
     */
    public void IncreaseCartCount(ActionEvent event){
       ShoppingCart InItem = (ShoppingCart)event.getComponent().getAttributes().get("selectproductid");
       if(InItem.getProduct().getCount()>InItem.getPurchaseCount()){
           InItem.setPurchaseCount(InItem.getPurchaseCount()+1);
       }else{
            JsfUtil.addErrorMessage("Can't increase the purchase count!");
       }
   }
    
    /**
     * This method decrease the shopping cart items count.
     * 
     * @param event
     * 
     */
    public void DecreaseCartCount(ActionEvent event){
       ShoppingCart DeItem = (ShoppingCart)event.getComponent().getAttributes().get("selectproductid");
        if(DeItem.getPurchaseCount()>1){
           DeItem.setPurchaseCount(DeItem.getPurchaseCount()-1);
       }else{          
            RemoveCartItem(event);
       }
   }
   
    /**
     * This method compare the item count that in the shopping cart with the purchase count in database.
     * 
     * @param item
     * 
     * @return boolean
     */
    public boolean CompareCount(ShoppingCart item){
       boolean ok=false;
       if (item.getProduct().getCount()>=item.getPurchaseCount()){
           ok=true;
       }
       return ok;
   }
    
    /**
     * This method refresh the item info that in the shopping cart.
     * 
     * @param event
     * 
     */
    public void RefreshCount(ActionEvent event){
        Product product=new Product();
        for(int i=0; i<shoppingCart.size();i++){
            product=(Product)this.ejbFacade.findByProductId(shoppingCart.get(i).getProduct().getProductId());
            shoppingCart.get(i).getProduct().setCount(product.getCount());
        }
        DataModel dm = new ListDataModel(shoppingCart);
        setItems(dm);
        System.out.println("Refresh successful!!!");
    }
    
    /**
     * This method provide a action for check out button to create an order.
     * 
     * @param event
     * 
     */
    public void ShoppingCartConfirm(ActionEvent event){
        FacesContext context = FacesContext.getCurrentInstance();
        if(context.getExternalContext().getSessionMap().get("user")!=null){
            if(shoppingCart.isEmpty()){
                JsfUtil.addErrorMessage("Shopping Cart is Empty! Create Order Failed!");
            }else{
                if (CompareDatabase()){
                    //check the purchase items count not more than database items count
                    CheckOutmodifyCount();
                    //if satisfy the above condition, create the order
                    CreateOrder();
                }else{
                    JsfUtil.addErrorMessage("Create Order Failed!");
                }
            }
        }else{
            context.responseComplete();      
            HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
            try {
                response.sendRedirect("users/Login.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(OrdersController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
     }
    
    /**
     * This method is to create an order. Method is called by ShoppingCartConfirm() function.
     * 
     */
    
    private void CreateOrder(){
        FacesContext context = FacesContext.getCurrentInstance();
        Orders order = new Orders();

        // create the OrderStatus
        OrderStatus orderstatus = new OrderStatus();
        orderstatus.setOrderStatusId(1);
        orderstatus.setDescription("Fresh");

        // create the Orders
        List<Orders> os = this.ejbFacade.findAll();
        //set the order Id
        if (!os.isEmpty()){
           int osId = os.get(os.size()-1).getOrderId() + 1; 
           order.setOrderId(osId);
        }else{
            order.setOrderId(1);
        }
        //set other attribute 
        Users user=(Users)context.getExternalContext().getSessionMap().get("user");
        order.setCustomerId(user.getCustomerId());
        order.setOrderStatusId(orderstatus);
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        order.setCreateTime(currentDate);
        this.ejbFacade.create(order);
        orderCopy=order;
        orderID=order.getOrderId();
        context.getExternalContext().getSessionMap().put("order", order);            

        //create the OrderDetail
        OrderDetail orderdetail = new OrderDetail();
        List<OrderDetail> odl = this.DetailejbFacade.findAll();
        int odlId = 1;
        if (!odl.isEmpty()){
            odlId = odl.get(odl.size()-1).getOrderDetailId() + 1;
        }
        // set order details attributes
        for (int i=0; i<shoppingCart.size(); i++ ){
            orderdetail.setOrderDetailId(odlId + i);
            orderdetail.setProductId(shoppingCart.get(i).getProduct());
            orderdetail.setCount(shoppingCart.get(i).getPurchaseCount());
            orderdetail.setOrderId(order);
            this.DetailejbFacade.create(orderdetail);

        }

        context.responseComplete();
        HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
        try {
            response.sendRedirect("order.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(OrdersController.class.getName()).log(Level.SEVERE, null, ex);
        }
                      
    }
    
   /**
     * This method provide a action for confirm the order. Change order status to confirmed.
     * 
     * @return String
     * 
     */
    public String OrderConfirm() throws JMSException{
        FacesContext context = FacesContext.getCurrentInstance();
        OrderStatus orderStatusUpdate= new OrderStatus();
        orderStatusUpdate.setOrderStatusId(2);
        //change the order status to confirmed
        orderStatusUpdate.setDescription("Confirmed");
        orderCopy.setOrderStatusId(orderStatusUpdate);
        this.ejbFacade.edit(orderCopy);
        shoppingCart.removeAll(shoppingCart);
        DataModel dm = new ListDataModel(shoppingCart);
        setItems(dm);
        Users user;
        //append the message to the log file
        user = (Users)context.getExternalContext().getSessionMap().get("user");
        JsfUtil.sendMessage(user.getUsername()+" confirm order:  "+getEntityInfo(), "jms/OrderMessageBeanFactory", "jms/OrderMessageBean");
        return "/orders/ordersuccess?faces-redirect=true";
    }
    
    /**
     * This method provide a action for cancel the order. Change order status to cancel.
     * 
     * @return String
     * 
     */
    public String OrderCancel() throws JMSException{
        FacesContext context = FacesContext.getCurrentInstance();
        CancelmodifyCount();
        OrderStatus orderStatusUpdate= new OrderStatus();
        orderStatusUpdate.setOrderStatusId(3);
        //change the order status to cancel
        orderStatusUpdate.setDescription("Cancel");
        orderCopy.setOrderStatusId(orderStatusUpdate);
        this.ejbFacade.edit(orderCopy);
        Users user;
        user = (Users)context.getExternalContext().getSessionMap().get("user");
        //append the message to the log file
        JsfUtil.sendMessage(user.getUsername()+"  cancel order:  " + getEntityInfo(), "jms/OrderMessageBeanFactory", "jms/OrderMessageBean");
        return "/orders/shoppingcart?faces-redirect=true";          
    }
    
    /**
     * This method provide modify the product count in the database when click the check out button.
     *
     */
    public void CheckOutmodifyCount(){
        Product product = new Product();
        List<ShoppingCart> shoppingCartCopy = shoppingCart;       
        
        for (ShoppingCart item : shoppingCartCopy) {
            product=(Product)this.ejbFacade.findByProductId(item.getProduct().getProductId());
            item.getProduct().setCount(product.getCount()-item.getPurchaseCount());
            proejbFacade.edit(item.getProduct());
        }
    }
    
    /**
     * This method provide modify the product count in the database 
     *   when customer click the cancel order button.
     *
     */
    public void CancelmodifyCount(){
        Product product = new Product();
        List<ShoppingCart> shoppingCartCopy = shoppingCart;       
        
        for (ShoppingCart item : shoppingCartCopy) {
            product=(Product)this.ejbFacade.findByProductId(item.getProduct().getProductId());
            item.getProduct().setCount(product.getCount()+item.getPurchaseCount());
            proejbFacade.edit(item.getProduct());
        }
    }
    
    /**
     * This method compare the product count between shopping cart and database.
     *
     * @return boolean 
     */
    private boolean CompareDatabase(){
        boolean ok=true;
        Product product=new Product();
        for(int i=0; i<shoppingCart.size();i++){
            product=(Product)this.ejbFacade.findByProductId(shoppingCart.get(i).getProduct().getProductId());
            if(shoppingCart.get(i).getPurchaseCount()>product.getCount()){
                JsfUtil.addErrorMessage(product.getProductName() + "purchase count more than product stored count, please modity !");
                ok=false;
                break;
            }
        }
        return ok;
    }
   
    public String goShoppingCartPage(){
        DataModel dm = new ListDataModel(shoppingCart);
        setItems(dm);
        return "/orders/shoppingcart?faces-redirect=true";
    }
    
    /**
     * This method create an order list when user click the order link.
     *
     * @return String
     */
    public String setOrderList(){        
        FacesContext context = FacesContext.getCurrentInstance();
        Users user = (Users) context.getExternalContext().getSessionMap().get("user");
        List<Orders> orderlist = new ArrayList<>(); 
       //if the user is admin, it will show all orders
        if (user.getRoleId().getRoleId()==2){
            orderlist = this.ejbFacade.findAll();
            setList(orderlist);
           
        }else {
            try {
                orderlist =  this.ejbFacade.findOrderByCustomerId(user.getCustomerId().getCustomerId());
                setList(orderlist);
            } catch (JMSException ex) {
                Logger.getLogger(ProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        //DataModel dm = new ListDataModel(List_Order);
        //setItems(dm);
        datamodelStatus=1;
        setItems(getPagination().createPageDataModel());
        return "/orders/List?faces-redirect=true";
    }
    
   /**
     * This method set List_Order and called by setOrderList() function.
     *
     */
    public void setList(List<Orders> orderlist){
        if (!List_Order.isEmpty()){
             List_Order.removeAll(List_Order);
        }
        //get the order list info
        for (int i=0; i< orderlist.size(); i++){
            OrderList o = new OrderList();
            o.setOrderId(orderlist.get(i).getOrderId());
            o.setCreateTime(orderlist.get(i).getCreateTime());
            o.setCustomerId(orderlist.get(i).getCustomerId().getCustomerId());
            o.setCustomerName(orderlist.get(i).getCustomerId().getName());
            o.setOrderStatus(orderlist.get(i).getOrderStatusId().getDescription());
            List_Order.add(o);
        }
    }
    
     /**
     * This method create an order detail list when user click the view detail link.
     *
     * @return String
     */
    public String setOrderDetial(){
        if (!List_OrderDetail.isEmpty()){
            List_OrderDetail.removeAll(List_OrderDetail);
        } 
        List<OrderDetail> orderdetail = new ArrayList<>();
        OrderList orderlist = (OrderList) getItems().getRowData();
        //according to order id to get the order details list
        try {
            orderdetail = this.ejbFacade.findDetailByOrderId(orderlist.getOrderId());
             for (int i =0; i<orderdetail.size();i++){
                OrderDetailList od = new OrderDetailList();
                od.setOrderDetailId(orderdetail.get(i).getOrderDetailId());
                od.setCount(orderdetail.get(i).getCount());
                od.setProductName(orderdetail.get(i).getProductId().getProductName());
                List_OrderDetail.add(od);
            }
        } catch (JMSException ex) {
            Logger.getLogger(OrderDetailController.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        DataModel dm = new ListDataModel(List_OrderDetail);
        setItems(dm);
        datamodelStatus=2;
        return "/orders/orderdetails?faces-redirect=true";
    }
    
    
    public int searchListItem(List<ShoppingCart> list, Product product){
        int exist= -1;
        for (int i =0; i< list.size(); i++){
            if(list.get(i).getProduct().equals(product)){
                exist= i;
            }
        }
        return exist;
    }
    
    public OrderList getSelected() {
        if (current == null) {
            current = new OrderList();
            selectedItemIndex = -1;
        }
        return current;
    }
    
    private OrdersFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    int count = 0;
                    if (datamodelStatus == 1 ){
                        count= getFacade().count();
                    }else{
                        count= List_OrderDetail.size();
                    }
                    System.out.println("itemcount:    " + count);
                    return count;
                }

                @Override
                public DataModel createPageDataModel() {                    
                    ListDataModel mode;
                    if(datamodelStatus==1){
                        List<OrderList> od= new ArrayList<>();
                        int size=0;
                        if (getPageFirstItem()+getPageSize()<List_Order.size()){
                            size = getPageFirstItem()+getPageSize();
                        }else{
                            size = List_Order.size();
                        }
                        for(int i=getPageFirstItem(); i<size;i++){                         
                            od.add(List_Order.get(i));
                        }
                       mode = new ListDataModel(od);
                    }else{
                        List<OrderDetailList> od= new ArrayList<>();
                        int size=0;
//                        if (getPageFirstItem()+getPageSize()<List_OrderDetail.size()){
//                            size = getPageFirstItem()+getPageSize();
//                        }else{
//                            size = List_OrderDetail.size();
//                        }
                        for(int i=getPageFirstItem(); i<List_OrderDetail.size();i++){                         
                            od.add(List_OrderDetail.get(i));
                        }
                       mode = new ListDataModel(od);
                    }
                    //setItems(mode);
                    return mode;
                    //return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (OrderList) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new OrderList();
        selectedItemIndex = -1;
        return "Create";
    }
    
    private String getEntityInfo(){
        FacesContext context = FacesContext.getCurrentInstance();
        Orders order;  
        order = (Orders)context.getExternalContext().getSessionMap().get("order");
        return  "CustomerId: "+ order.getCustomerId() + 
                ", OrderId:  " + order.getOrderId() +
                ", CreateTime: " + order.getCreateTime()+
                ", OrderStutasId: " + order.getOrderStatusId();
    }
    
    /**
     * This method delete an order from the database.
     *
     * @return String
     */
    public String destroy() throws JMSException {
        current = (OrderList) getItems().getRowData();
        Orders ord = this.ejbFacade.findOrderByORderId(current.getOrderId());
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        //according to order id to delete the order details first.
        List<OrderDetail> odl = this.ejbFacade.findDetailByOrderId(ord.getOrderId());
        for(int i=0; i<odl.size();i++){
            this.DetailejbFacade.remove(odl.get(i));
        }
        //delete the order
        performDestroy(ord);
        recreatePagination();
        //refresh the order list
        FacesContext context = FacesContext.getCurrentInstance();
        Users user = (Users) context.getExternalContext().getSessionMap().get("user");
        List<Orders> orderlist = new ArrayList<>(); 
        
        if (user.getRoleId().getRoleId()==2){
            orderlist = this.ejbFacade.findAll();
            setList(orderlist);
           
        }else {
            try {
                orderlist =  this.ejbFacade.findOrderByCustomerId(user.getCustomerId().getCustomerId());
                setList(orderlist);
            } catch (JMSException ex) {
                Logger.getLogger(ProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.out.println("OrderList size :     "  +  List_Order.size());
        DataModel dm = new ListDataModel(List_Order);
        setItems(dm);
        return "/orders/List?faces-redirect=true";
    }

    private void performDestroy(Orders order) {
        try {
            getFacade().remove(order);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("OrdersDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Orders getOrders(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Orders.class)
    public static class OrdersControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            OrdersController controller = (OrdersController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "ordersController");
            return controller.getOrders(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Orders) {
                Orders o = (Orders) object;
                return getStringKey(o.getOrderId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Orders.class.getName());
            }
        }

    }

}
