/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.display.primitive;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author sorel
 */
public abstract class FeatureGraphic extends GraphicPrimitive2D{

    
    public FeatureGraphic(CoordinateReferenceSystem crs){
        super(crs);
    }
    
    public boolean isVisible() {
        return true;
    }

    
    
    
    
    
}
