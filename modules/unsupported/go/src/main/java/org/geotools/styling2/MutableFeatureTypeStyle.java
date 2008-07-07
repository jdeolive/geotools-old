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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.opengis.feature.type.Name;
import org.opengis.filter.Id;
import org.opengis.style.Description;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.Rule;
import org.opengis.style.SemanticType;
import org.opengis.style.StyleVisitor;

/**
 *
 * @author Johann Sorel
 */
public class MutableFeatureTypeStyle implements FeatureTypeStyle{

    private final String name;
    private Description desc;
    private Id ids;
    private final Set<Name> names;
    private final Set<SemanticType> semantics;
    private final List<Rule> rules;
    
    
    public MutableFeatureTypeStyle(String name, Description desc, Id ids, Set<Name> names, Set<SemanticType> semantics, List<Rule> rules){
        if(name == null){
            throw new NullPointerException("name can't be null");
        }
        
        this.name = name;
        
        if(desc == null){
            this.desc = SymbolizerBuilder.DEFAULT_DESCRIPTION;
        }
        
        //TODO fix this list to listen to changes and fire events if necessary
        this.ids = null;
        this.names = new LinkedHashSet<Name>();
        this.semantics = new LinkedHashSet<SemanticType>();
        this.rules = new ArrayList<Rule>();
        if(ids != null) this.ids = ids;
        if(names != null) this.names.addAll(names);
        if(semantics != null) this.semantics.addAll(semantics);
        if(rules != null) this.rules.addAll(rules);
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

    /**
     * 
     * @return live list
     */
    public Id featureInstanceIDs() {
        return ids;
    }

    /**
     * 
     * @return live list
     */
    public Set<Name> featureTypeNames() {
        return names;
    }

    /**
     * 
     * @return live list
     */
    public Set<SemanticType> semanticTypeIdentifiers() {
        return semantics;
    }

    /**
     * 
     * @return live list
     */
    public List<Rule> rules() {
        return rules;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

}
