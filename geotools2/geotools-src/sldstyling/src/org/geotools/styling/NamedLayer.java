/*
 * NamedLayer.java
 *
 * Created on November 3, 2003, 10:10 AM
 */

package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  jamesm
 */
public class NamedLayer extends StyledLayer{
    
    List styles = new ArrayList();
    
    public FeatureTypeConstraint[] getLayerFeatureConstrains(){
        return null;
    }
    
    public StyledLayer[] getStyles(){
        return (StyledLayer[])styles.toArray(new StyledLayer[0]);
    }
    
    public void addStyledLayer(StyledLayer sl){
        styles.add(sl);
    }

}
