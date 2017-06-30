package com.shop.EJB;

import com.shop.Entities.Product;
import com.shop.EJB.util.JsfUtil;
import com.shop.EJB.util.PaginationHelper;
import com.shop.Entities.Users;
import com.shop.JavaBean.SortTableDataModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
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
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.jms.JMSException;

@Named("productController")
@SessionScoped
public class ProductController implements Serializable {

    private Product current;
    private DataModel items = null;
    @EJB
    private com.shop.EJB.ProductFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private String searchText;
    
    private boolean sortAscending = false;

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    public ProductController() {
    }
    
    /**
     * This method get items by different condition.
     * 
     * 1. null or "", it will get all items.
     * 2. searching number, it will get the product by the number as product id.
     * 3. searching String, it will get products by the String as product's name.
     * @return page's URI - the page will be redirected.
     */
    public String productSearch(){
        List<Product> list = new ArrayList<>();
        // When search text is "" return all products.
        if (this.searchText == null || this.searchText.equals("")){
            list = this.ejbFacade.findAll();
        // When search text is a number, search the product by number(id).
        }else if (JsfUtil.isInteger(this.searchText)){
            try {
                Product p = getProduct(Integer.parseInt(this.searchText));
                if (p != null)
                    list.add(p);
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e.getMessage());
            }
        // When search text is a String, search products by name.  
        } else {
            try {
                list =  this.ejbFacade.findByProductName(this.searchText);
            } catch (JMSException ex) {
                Logger.getLogger(ProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        DataModel dm = new ListDataModel(list);
        setItems(dm);
        return "/index?faces-redirect=true";
    }
    
    
    
    /**
     * This method sort by quantity of product in product list.
     * 
     */
    public void sortByQuantity() {
        SortTableDataModel sortDM;
        sortDM = new SortTableDataModel(getItems());
        if(sortAscending){
            sortDM.sortBy(new Comparator<Product>() {
                @Override
                public int compare(Product o1, Product o2) {
                    return (int) (o1.getCount()- o2.getCount());
                }
            });
            sortAscending = false;
        }else{
            //descending book
            sortDM.sortBy(new Comparator<Product>() {
                @Override
                public int compare(Product o1, Product o2) {
                    return (int) (o2.getCount()- o1.getCount());
                }
            });
          sortAscending = true;
        }
        // reset datamodel to datatable
        setItems(sortDM);
    }
    
    /**
     * This method gets the info of an entity.
     * 
     * @return all info with a String.
     */
    private String getEntityInfo(){
        return "ID: " + current.getProductId() +
                ", Name:" + current.getProductName() +
                ", Picture: " + current.getPicture() +
                ", Price: " + current.getPrice() + 
                ", Quantity: " + current.getCount();
    }
    
    /**
     * This function creates a product
     * 
     * @return page's URI
     */
    public String create() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            Users user;
            // get current user.
            user = (Users)context.getExternalContext().getSessionMap().get("user");
            current.setProductId(AutogetProductId());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProductCreated"));
            
            String msg; 
            // generate the message which will be send to JMS
            msg = user.getUsername() + " adds product(" + getEntityInfo() + ")";
            JsfUtil.sendMessage(msg, "jms/ProductMessageBeanFactory", "jms/ProductMessageBean");
            
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }
    
    /**
     * This method destroys the current product
     */
    private void performDestroy() {
        try {
            // delete the product
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProductDeleted"));
            FacesContext context = FacesContext.getCurrentInstance();
            Users user;
            // get current user.
            user = (Users)context.getExternalContext().getSessionMap().get("user");

            String msg; 
            // generate the message which will be send to JMS
            msg = user.getUsername() + " removes product(" + getEntityInfo() + ")";
            JsfUtil.sendMessage(msg, "jms/ProductMessageBeanFactory", "jms/ProductMessageBean");
            
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }
    
    /**
     * This method set the Items to display in the page.
     * 
     * @param model is a list of entity.
     */
    public void setItems(DataModel model) {
        this.recreateModel();
        items = model;
        // update the data source of datatable
        this.updateCurrentItem();
    }
    
    public Product getSelected() {
        if (current == null) {
            current = new Product();
            selectedItemIndex = -1;
        }
        return current;
    }

    private ProductFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(50) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
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
        current = (Product) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Product();
        selectedItemIndex = -1;
        return "/product/Create?faces-redirect=true";
    }
    
    public int AutogetProductId(){
         List<Product> pro =this.ejbFacade.findAll();
         int productid = 1;
         if (!pro.isEmpty()){
            productid = pro.get(pro.size()-1).getProductId() + 1;
         }
         
        return productid;
    }

    public String prepareEdit() {
        current = (Product) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProductUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Product) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
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
        return "/index";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "/index";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Product getProduct(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Product.class)
    public static class ProductControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ProductController controller = (ProductController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "productController");
            return controller.getProduct(getKey(value));
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
            if (object instanceof Product) {
                Product o = (Product) object;
                return getStringKey(o.getProductId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Product.class.getName());
            }
        }

    }

}


