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
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SelectedChannelType;
import org.geotools.swing.JMapFrame;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.ContrastMethod;

public class ImageLab {

    public static void main(String[] args) {

        /*
         * We want to prompt the user for a GeoTIFF file which may
         * have a tif or tiff extension. We can use GeoTools data
         * format system to help us out
         */
        File file = JFileDataStoreChooser.showOpenFile(new String[] {"tiff", "tif"}, null);
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

        CoordinateReferenceSystem crs = reader.getCrs();
        if (crs == null) {
            crs = DefaultGeographicCRS.WGS84;
        }

        Style style = createRGBStyle(reader);
        if (style == null) {
            // input coverage didn't have bands labelled "red", "green", "blue"
            return;
        }

        MapContext map = new DefaultMapContext(crs);
        MapLayer layer = null;

        try {
            layer = new DefaultMapLayer(reader, style);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        
        map.addLayer(layer);
        map.setTitle("My coverage");

        JMapFrame.showMap(map);
    }


    /**
     * This method examines the names of the sample dimensions in the provided coverage
     * looking for "red...", "green..." and "blue..." (case insensitive match). It then
     * sets up a raster symbolizer and returns this wrapped in a Style.
     *
     * @param coverage the coverage to be rendered
     *
     * @return a new Style object containing a raster symbolizer set up for RGB image
     */
    private static Style createRGBStyle(GeoTiffReader reader) {
        StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        GridCoverage2D cov = null;
        try {
            cov = (GridCoverage2D) reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }

        SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        final int RED = 0, GREEN = 1, BLUE = 2;
        int k = 1;
        for (GridSampleDimension dim : cov.getSampleDimensions()) {
            String name = dim.getDescription().toString().toLowerCase();
            if (name.matches("red.*")) {
                sct[RED] = sf.createSelectedChannelType(String.valueOf(k++), ce);

            } else if (name.matches("green.*")) {
                sct[GREEN] = sf.createSelectedChannelType(String.valueOf(k++), ce);

            } else if (name.matches("blue.*")) {
                sct[BLUE] = sf.createSelectedChannelType(String.valueOf(k++), ce);
            }
        }

        if (sct[0] == null || sct[1] == null || sct[2] == null) {
            return null;
        }

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }
}
