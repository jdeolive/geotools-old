/*
 * UserLayer.java
 *
 * Created on November 3, 2003, 12:00 PM
 */

package org.geotools.styling;

import java.util.ArrayList;

/**
 *
 * @author  jamesm
 */
public class UserLayer extends StyledLayer {
    
    ArrayList styles = new ArrayList();
    
    public RemoteOWS getRemoteOWS(){
        return null;
    }
    public void setRemoteOWS(RemoteOWS service){
    }
    
    public FeatureTypeConstraint[] getLayerFeatureConstraints(){
        return null;
    }
    public void setLayerFeatureConstraints(FeatureTypeConstraint[] constraints){
    }
    
    public Style[] getUserStyles(){
       return (Style[])styles.toArray(new Style[0]);
    }
    public void setUserStyles(Style[] styles){
        
    }
    
    public void addUserStyle(Style style){
        styles.add(style);
    }
    
    
    
}
