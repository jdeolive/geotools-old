/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.styling2;

import java.util.ArrayList;
import java.util.List;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.PortrayalCatalog;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel
 */
public class MutablePortrayalCatalog implements PortrayalCatalog{

    private final List<FeatureTypeStyle> fts;
    private Symbolizer symbol;
    
    public MutablePortrayalCatalog(List<FeatureTypeStyle> fts, Symbolizer symbol){
        if(symbol == null){
            throw new NullPointerException("default symbolizer can't be null");
        }
        
        //TODO fix this list to listen to changes and fire events if necessary
        this.fts = new ArrayList<FeatureTypeStyle>();
        if(fts != null) this.fts.addAll(fts);
        this.symbol = symbol;
    }
    
    /**
     * @return live list
     */    
    public List<FeatureTypeStyle> getFeatureTypes() {
        return fts;
    }

    public Symbolizer getDefaultSpecification() {
        return symbol;
    }

    public void setDefaultSpecification(Symbolizer symbol){
        if(symbol == null){
            throw new NullPointerException("default symbolizer can't be null");
        }
        this.symbol = symbol;
        //TODO fire event
    }
    
}
