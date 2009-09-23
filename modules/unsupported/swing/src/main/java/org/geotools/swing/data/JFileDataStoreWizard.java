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

import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.swing.wizard.JWizard;

/**
 * Wizard allowing a user to open a file with a GeoTools DataStore.
 * <p>
 * GeoTools DataStores that work with files are asked to provide a FileDataStoreFactorySpi
 * documenting what file extensions they support; and any additional parameters that
 * may be interesting etc.
 * <p>
 * This wizard can be started with a specific FileDataStoreFactorySpi, or a file extension
 * which will be used to locate one.
 */
public class JFileDataStoreWizard extends JWizard {
    private static final long serialVersionUID = -3788708439279424698L;
    FileDataStoreFactorySpi format;
    private JFileDataStorePage page;
    
    public JFileDataStoreWizard( String extension ){
        this( null, FileDataStoreFinder.getDataStoreFactory( extension ));
    }
    public JFileDataStoreWizard( File file, String extension ){
        this( file, FileDataStoreFinder.getDataStoreFactory( extension ));
    }
    
    public JFileDataStoreWizard( File file, FileDataStoreFactorySpi format ){
        super( format == null ? "" : format.getDisplayName() );
        if( format == null){
            // GeoTools detects FileDataStoreFactorSpi's on the classpath
            // if you are getting this error for "shp" perhaps you do not have the
            // gt-shape jar on your classpath?
            throw new NullPointerException("Please provide a supported file format");
        }
        this.format = format;
        page = new JFileDataStorePage( file, format);        
        registerWizardPanel( page );
    }
    public File getFile(){
        return page.file;
    }

    public static void main( String args[]){
        File file = null;
        if( args.length > 0 ){
            file = new File( args[0]);
        }
        JFileDataStoreWizard wizard = new JFileDataStoreWizard(".shp");
        int result = wizard.showModalDialog();
        System.out.print("Wizard completed with:");
        switch (result) {
        case JWizard.CANCEL:
            System.out.println("CANCEL");
            break;
        case JWizard.FINISH:
            System.out.println("FINISH "+wizard.getFile());
            break;
        case JWizard.ERROR:
            System.out.println("ERROR");
            break;
        default:
            System.out.println("unexpected " + result);
        }
    }
}
