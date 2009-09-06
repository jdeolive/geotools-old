/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.gui.swing.data;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.FileDataStoreFactorySpi;

public class JFileDataStoreChooser extends JFileChooser {
    
    public JFileDataStoreChooser( final String extension ){
        setFileFilter( new FileFilter(){
            public boolean accept( File f ) {
                return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
            }
            public String getDescription() {
                return "Shapefiles";
            }               
        });
        //int returnVal = showOpenDialog( null );
    }
    public JFileDataStoreChooser( final FileDataStoreFactorySpi format ){
        setFileFilter( new FileFilter(){
            public boolean accept( File f ) {
                if( f.isDirectory() ){
                    return true;
                }
                for( String ext : format.getFileExtensions()){
                    if( f.getPath().endsWith(ext) ){
                        return true;
                    }
                    if( f.getPath().endsWith(ext.toUpperCase()) ){
                        return true;
                    }
                }
                return false;
            }
            public String getDescription() {
                return "Shapefiles";
            }               
        });
        //int returnVal = showOpenDialog( null );
    }
    
    
    public static File showOpenFile( String extension, Component parent ) throws HeadlessException {
        JFileDataStoreChooser dialog = new JFileDataStoreChooser( extension );
        int returnVal = dialog.showOpenDialog(parent);
        if(returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = dialog.getSelectedFile();
        return file;
    }
    public static File showOpenFile( FileDataStoreFactorySpi format, Component parent ) throws HeadlessException {
        JFileDataStoreChooser dialog = new JFileDataStoreChooser( format );
        int returnVal = dialog.showOpenDialog(parent);
        if(returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = dialog.getSelectedFile();
        return file;
    }
    
    public static void main( String arg[] ){
        File file = JFileDataStoreChooser.showOpenFile("shp", null );
    }
}
