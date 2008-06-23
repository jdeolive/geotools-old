/*
 *    GeoTools - The Open Source Java GIS Toolkit
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
import org.opengis.style.Description;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.Style;
import org.opengis.style.StyleVisitor;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel
 */
public class MutableStyle implements Style{

    private final List<FeatureTypeStyle> fts;
    private Symbolizer symbol;
    private String name;
    private Description description;
    private boolean isDefault = false;


    public MutableStyle(String name, Description desc, List<FeatureTypeStyle> fts, Symbolizer symbol, boolean isDefault){
        if(symbol == null){
            throw new NullPointerException("default symbolizer can't be null");
        }

        //TODO fix this list to listen to changes and fire events if necessary
        this.fts = new ArrayList<FeatureTypeStyle>();
        if(fts != null) this.fts.addAll(fts);
        this.symbol = symbol;
        this.name = name;
        this.description = desc;
        this.isDefault = isDefault;
    }

    /**
     * @return live list
     */
    public List<FeatureTypeStyle> featureTypeStyles() {
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

    public String getName() {
        return name;
    }

    public Description getDescription() {
        return description;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

}
