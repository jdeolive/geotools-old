/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;

/**
 * Implementation of DefaultServiceInfo as a java bean.
 * 
 * @author Jody Garnett (Refractions Research)
 */
public class DefaultServiceInfo implements ServiceInfo, Serializable {    
    private static final long serialVersionUID = 7975308744804800859L;
    
    protected String description;
    protected Set<String> keywords;
    protected URI publisher;
    protected URI schema;
    protected String title;
    private Icon icon;
    private URI source;
    
    public DefaultServiceInfo(){                
    }
    
    public DefaultServiceInfo( ServiceInfo copy ){
        this.description = copy.getDescription();
        this.keywords = new HashSet<String>();
        if( copy.getKeywords() != null ){
            this.keywords.addAll( copy.getKeywords() );
        }
        this.publisher = copy.getPublisher();
        this.schema = copy.getSchema();
        this.title = copy.getTitle();
        this.source = copy.getSource();
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription( String description ) {
        this.description = description;
    }
    /**
     * @return the keywords
     */
    public Set<String> getKeywords() {
        return keywords;
    }
    /**
     * @param keywords the keywords to set
     */
    public void setKeywords( Set<String> keywords ) {
        this.keywords = keywords;
    }
    /**
     * @return the publisher
     */
    public URI getPublisher() {
        return publisher;
    }
    /**
     * @param publisher the publisher to set
     */
    public void setPublisher( URI publisher ) {
        this.publisher = publisher;
    }
    /**
     * @return the schema
     */
    public URI getSchema() {
        return schema;
    }
    /**
     * @param schema the schema to set
     */
    public void setSchema( URI schema ) {
        this.schema = schema;
    }
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * @return the source
     */
    public URI getSource() {
        return source;
    }
    /**
     * @param source the source to set
     */
    public void setSource( URI source ) {
        this.source = source;
    }

}
