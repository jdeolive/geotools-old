// docs start source
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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Parameter;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JParameterListWizard;
import org.geotools.swing.wizard.JWizard;
import org.geotools.util.KVP;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.style.ContrastMethod;

public class ImageLab {

    public static void main(String[] args) throws Exception {
        ImageLab me = new ImageLab();
        me.getLayersAndDisplay();
    }

    // docs end main

    // docs start get layers
    /**
     * Prompts the user for a GeoTIFF file and a Shapefile and passes them to the displayLayers
     * method
     */
    private void getLayersAndDisplay() throws Exception {
        List<Parameter<?>> list = new ArrayList<Parameter<?>>();
        list.add(new Parameter<File>("image", File.class, "Image",
                "GeoTiff image to use as basemap", new KVP(Parameter.EXT, "tif")));
        list.add(new Parameter<File>("shape", File.class, "Shapefile",
                "Shapefile contents to display", new KVP(Parameter.EXT, "shp")));

        JParameterListWizard wizard = new JParameterListWizard("Image Lab",
                "Fill in the following layers", list);
        int finish = wizard.showModalDialog();

        if (finish != JWizard.FINISH) {
            System.exit(0);
        }
        File imageFile = (File) wizard.getConnectionParameters().get("image");
        File shapeFile = (File) wizard.getConnectionParameters().get("shape");
        displayLayers(imageFile, shapeFile);
    }

    // docs end get layers

    // docs start display layers
    /**
     * Displays a GeoTIFF file overlaid with a Shapefile
     * 
     * @param rasterFile
     *            the GeoTIFF file
     * @param shpFile
     *            the Shapefile
     */
    private void displayLayers(File rasterFile, File shpFile) throws Exception {
        /*
         * Connect to the GeoTIFF file using a GeoTiffReader
         */
        GeoTiffReader geotiffReader;
        try {
            geotiffReader = new GeoTiffReader(rasterFile);

        } catch (DataSourceException ex) {
            ex.printStackTrace();
            return;
        }

        /*
         * Create a Style that will be used to display the raster data
         */
        Style rasterStyle = createRGBStyle(geotiffReader);
        if (rasterStyle == null) {
            // input coverage didn't have bands labelled "red", "green", "blue"
            return;
        }

        /*
         * Connect to the shapefile
         */
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shpFile);
        FeatureSource<SimpleFeatureType, SimpleFeature> shapefileSource = dataStore
                .getFeatureSource();

        /*
         * Create a basic style with yellow lines and no fill
         */
        Style shpStyle = SLD.createPolygonStyle(Color.YELLOW, null, 0.0f);

        /*
         * Set up a MapContext with the two layers and display it
         */
        MapContext map = new DefaultMapContext();
        map.setTitle("ImageLab");
        map.addLayer(geotiffReader, rasterStyle);
        map.addLayer(shapefileSource, shpStyle);

        JMapFrame.showMap(map);
    }

    // docs end display layers

    // docs start create style
    /**
     * This method examines the names of the sample dimensions in the provided coverage looking for
     * "red...", "green..." and "blue..." (case insensitive match). It then sets up a raster
     * symbolizer and returns this wrapped in a Style.
     * 
     * @param coverage
     *            the coverage to be rendered
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

    // docs end create style

}
// docs end source