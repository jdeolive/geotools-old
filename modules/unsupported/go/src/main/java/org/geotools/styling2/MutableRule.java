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
import org.opengis.filter.Filter;
import org.opengis.style.Description;
import org.opengis.style.LegendGraphic;
import org.opengis.style.Rule;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel
 */
public class MutableRule implements Rule{

    private final String name;
    private Description desc;
    private LegendGraphic legend;
    private Filter filter;
    private boolean isElse;
    private double minscale;
    private double maxScale;
    private final List<Symbolizer> symbols;
    
    public MutableRule(String name, Description desc, LegendGraphic legend, Filter filter, boolean isElse, double minScale, double maxScale, List<Symbolizer> symbols){
        
        if(name == null){
            throw new NullPointerException("name can't be null");
        }
        
        this.name = name;
        
        if(desc == null){
            this.desc = SymbolizerBuilder.DEFAULT_DESCRIPTION;
        }
        
        this.legend = legend;
        this.filter = filter;
        this.isElse = isElse;
        this.minscale = minScale;
        this.maxScale = maxScale;
        //TODO fix this list to listen to changes and fire events if necessary
        this.symbols = new ArrayList<Symbolizer>();
        if(symbols != null) this.symbols.addAll(symbols);
        
    }
    
    public String getName() {
        return name;
    }

    public Description getDescription() {
        return desc;
    }

    public void setDescription(Description desc){
        if(desc == null){
            throw new NullPointerException("description can't be null");
        }
        this.desc = desc;
        //TODO fire event
    }
    
    public LegendGraphic getLegendGraphic() {
        return legend;
    }

    public void setLegendGraphic(LegendGraphic legend){
        this.legend = legend;
        //TODO fire event
    }
    
    public Filter getFilter() {
        return filter;
    }
    
    public void setFilter(Filter filter){
        this.filter = filter;
        //TODO fire event
    }

    public boolean isElseFilter() {
        return isElse;
    }
    
    public void setElseFilter(boolean isElse){
        this.isElse = isElse;
        //TODO fire event
    }

    public double getMinScaleDenominator() {
        return minscale;
    }
    
    public void setMinScaleDenominator(double minScale){
        this.minscale = minScale;
        //TODO fire event
    }

    public double getMaxScaleDenominator() {
        return maxScale;
    }
    
    public void setMaxScaleDenominator(double maxScale){
        this.maxScale = maxScale;
        //TODO fire event
    }

    /**
     * 
     * @return live list
     */
    public List<Symbolizer> getSymbolizers() {
        return symbols;
    }

}
