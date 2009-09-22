/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.swing.data;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.swing.wizard.JPage;
import org.geotools.swing.wizard.JWizard;

/**
 * Wizard prompting the user to enter or review connection parameters.
 * <p>
 * GeoTools DataStores that work with files are asked to provide a FileDataStoreFactorySpi
 * documenting what file extensions they support; and any additional parameters that
 * may be interesting etc.
 */
public class JDataStoreWizard extends JWizard {
    private static final long serialVersionUID = -3788708439279424698L;
    DataStoreFactorySpi format;
    private JDataStorePage page1;
    private JDataStorePage page2;
    
    /**
     * Set up the wizard with a "default" set of parameters.
     * 
     * @param format
     */
    public JDataStoreWizard( DataStoreFactorySpi format ){
        this( format, new HashMap<String,Serializable>() );
    }
    
    public JDataStoreWizard( DataStoreFactorySpi format, Map params ){
        super( format == null ? "" : format.getDisplayName() );
        if( format == null){
            // GeoTools detects FileDataStoreFactorSpi's on the classpath
            // if you are getting this error for "shp" perhaps you do not have the
            // gt-shape jar on your classpath?
            throw new NullPointerException("Please indicate the data format to connect to");
        }
        this.format = format;
        page1 = new JDataStorePage(format, params );
        page1.setRequried(true);
        page1.setPageIdentifier("page1");
        page1.setNextPageIdentifier("page2");
        registerWizardPanel( page1 );
        
        page2 = new JDataStorePage(format, params );       
        page2.setPageIdentifier("page2");
        page2.setBackPageIdentifier("page1");
        page2.setRequried(false);
        registerWizardPanel( page2 );
        
        setCurrentPanel("page1");
    }
    
    public Map<String,Serializable> getConnectionParameters(){
        return page2.connectionParameters;
    }

}
