/*
 * EqualClasses.java
 *
 * Created on 02 December 2003, 12:30
 */

package org.geotools.algorithms.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author  iant
 */
public class EqualClasses {
    int numberClasses;
    double[] breaks;
    double[] collection;
    /** Creates a new instance of EqualClasses */
    public EqualClasses(int numberClasses, double[] fc) {
        
        breaks = new double[numberClasses-1];
        setCollection(fc);
        setNumberClasses(numberClasses);
        
    }
    
    /** Getter for property numberClasses.
     * @return Value of property numberClasses.
     *
     */
    public int getNumberClasses() {
        return numberClasses;
    }
    
    /** Setter for property numberClasses.
     * @param numberClasses New value of property numberClasses.
     *
     */
    public void setNumberClasses(int numberClasses) {
        this.numberClasses = numberClasses;
        if(breaks == null){
            breaks = new double[numberClasses-1];
        }
        
        Arrays.sort(collection);
        
        int step = collection.length/numberClasses;
        for(int i=step,j=0;j<breaks.length;j++,i+=step){
            breaks[j] = collection[i];
        }
    }
    
    
    /** 
     * returns the the break points between the classes
     * <b>Note</b> You get one less breaks than number of classes.
     * @return Value of property breaks.
     *
     */
    public double[] getBreaks() {
        return this.breaks;
    }
    

    
    /** Setter for property collection.
     * @param collection New value of property collection.
     *
     */
    public void setCollection(double[] collection) {
        this.collection = collection;
    }
    
}
