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

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;
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
        this( format, new HashMap<String,Object>() );
    }
    
    public JDataStoreWizard( DataStoreFactorySpi format, Map params ){
        super( format == null ? "" : format.getDisplayName() );
        if( params == null ){
            params = new HashMap<String,Object>();
        }
        fillInDefaults( format, params );
        if( format == null){
            // GeoTools detects FileDataStoreFactorSpi's on the classpath
            // if you are getting this error for "shp" perhaps you do not have the
            // gt-shape jar on your classpath?
            throw new NullPointerException("Please indicate the data format to connect to");
        }
        
        this.format = format;
        page1 = new JDataStorePage(format, params );        
        page1.setLevel("user");
        page1.setPageIdentifier("page1");
        registerWizardPanel( page1 );

        if( countParamsAtLevel( format, "advanced") != 0 ){
            page2 = new JDataStorePage(format, params );       
            page2.setPageIdentifier("page2");
            page2.setBackPageIdentifier("page1");
            page2.setLevel("advanced");
            registerWizardPanel( page2 );
            
            // link from page 1
            page1.setNextPageIdentifier("page2");            
        }        
        setCurrentPanel("page1");
    }

    /**
     * Method used to fill in any required "programming" level defaults such as dbtype.
     * 
     * @param format2
     * @param params
     */
    private void fillInDefaults(DataStoreFactorySpi format, Map<String, Object> params) {
        if( format == null ) return;
        for( Param param : format.getParametersInfo() ){
            if( param.required && "program".equals(param.getLevel()) ){
                if( !params.containsKey( param.key )){
                    params.put( param.key, param.sample );
                }
            }
        }
    }

    private int countParamsAtLevel(DataStoreFactorySpi format, String level) {
        if( format == null ) return 0;
        int count = 0;
        Param[] parametersInfo = format.getParametersInfo();
        if( level == null ){
            return parametersInfo.length;
        }
        for( Param param : parametersInfo ){
            String check = param.getLevel();
            if( level.equals( check )){
                count ++;
            }
        }
        return count;
    }

    public Map<String,Object> getConnectionParameters(){
        if( page2 != null ){
            return page2.connectionParameters;
        }
        else {
            return page1.connectionParameters;
        }
    }

}
