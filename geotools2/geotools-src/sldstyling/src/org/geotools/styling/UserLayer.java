/*
 * UserLayer.java
 *
 * Created on November 3, 2003, 12:00 PM
 */

package org.geotools.styling;

/**
 *
 * @author  jamesm
 */
public class UserLayer extends StyledLayer {
    
    
    public RemoteOWS getRemoteOWS(){
        return null;
    }
    public void setRemoteOWS(RemoteOWS service){
    }
    
    public FeatureTypeConstraint[] getLayerFeatureConstraints(){
        return null;
    }
    public void setLayerFeatureConstraints(){
    }
    
    public Style[] getUserStyles(){
        return null;
    }
    public void setUserStyles(Style[] styles){
    }
    
    public void addUserStyle(Style style){
    }
    
    
    
}
