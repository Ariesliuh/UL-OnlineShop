/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shop.JavaBean;

import java.util.Arrays;
import java.util.Comparator;
import javax.faces.model.DataModel;

/**
 *
 * @author aries
 */
public class SortTableDataModel<E> extends DataModel<E>{

    DataModel<E> model;
    private Integer[] rows;
  
    public SortTableDataModel(DataModel<E> model){
        this.model = model;
        initRows();
    }
    
    /**
     * initial the rows for data model
     */
    public void initRows(){
        int rowCount = model.getRowCount();
        if(rowCount != -1){
            this.rows = new Integer[rowCount];
            for(int i = 0; i < rowCount; ++i){
              rows[i] = i;
            }
        }
    }
    
    /**
     * This method exec sort feature.
     * 
     * @param comparator is a type of compare
     */
    public void sortBy(final Comparator<E> comparator){
        Comparator<Integer> rowComp = (Integer i1, Integer i2) -> {
            E o1 = getData(i1);
            E o2 = getData(i2);
            System.out.println(".compare():"+i1);
            return comparator.compare(o1, o2);
        } /**
         * This method compares 2 int data
         * @param i1
         * @param i2
         * @return the result of compare.
         */ ;
        Arrays.sort(rows, rowComp);

    }
    
    /**
     * This method gets the data.
     * 
     * @param row according to the row find the data
     * @return the data
     */
    private E getData(int row){
        int originalRowIndex = model.getRowIndex();
        
        model.setRowIndex(row);
        E newRowData = model.getRowData();
        model.setRowIndex(originalRowIndex);

        return newRowData;
    }
  
    @Override
    public void setRowIndex(int rowIndex) {

        if(0 <= rowIndex && rowIndex < rows.length){
            model.setRowIndex(rows[rowIndex]);
        }else{
            model.setRowIndex(rowIndex);
        }
    }
  
    @Override
    public boolean isRowAvailable() {
        return model.isRowAvailable();
    }

    @Override
    public int getRowCount() {
        return model.getRowCount();
    }

    @Override
    public E getRowData() {
        return model.getRowData();
    }

    @Override
    public int getRowIndex() {
        return model.getRowIndex();
    }

    @Override
    public Object getWrappedData() {
        return model.getWrappedData();
    }

    @Override
    public void setWrappedData(Object data) {
        model.setWrappedData(data);
        initRows();

    }

}
