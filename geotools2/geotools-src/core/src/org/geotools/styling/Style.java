/*
 * Style.java
 *
 * Created on March 27, 2002, 1:14 PM
 */

package org.geotools.styling;

/**
 *
 * @author  jamesm
 */
public interface Style {
    
    public String getName();
    public String getTitle();
    public String getAbstract();
    public boolean isDefault();
    public FeatureTypeStyle[] getFeatureTypeStyles();
   
}

