/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gui.swing.JMapFrame;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ImageLab {

    public static void main(String[] args) {

        File file = promptForFile();
        if (file == null) {
            return;
        }

        GeoTiffReader reader;
        try {
            reader = new GeoTiffReader(file);

        } catch (DataSourceException ex) {
            ex.printStackTrace();
            return;
        }

        GridCoverage2D coverage;
        try {
            coverage = (GridCoverage2D) reader.read(null);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem();
        if (crs == null) {
            crs = DefaultGeographicCRS.WGS84;
        }

        StyleBuilder builder = new StyleBuilder();
        RasterSymbolizer sym = builder.createRasterSymbolizer();
        
        Style style = builder.createStyle(sym);
        
        MapContext context = new DefaultMapContext(crs);
        MapLayer layer = null;

        try {
            layer = new DefaultMapLayer(coverage, style);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        
        context.addLayer(layer);
        context.setTitle("My coverage");

        JMapFrame.showMap(context);
    }

    private static File promptForFile() {
        File file = null;

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                int pos = f.getName().lastIndexOf('.');
                String ext = f.getName().substring(pos + 1);
                if (ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")) {
                    return true;
                }

                return false;
            }

            @Override
            public String getDescription() {
                return "TIFF files";
            }
        } );

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }

        return file;
    }
}
