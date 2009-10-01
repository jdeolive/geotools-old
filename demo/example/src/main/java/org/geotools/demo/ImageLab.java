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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SelectedChannelType;
import org.geotools.swing.JMapFrame;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.wizard.JPage;
import org.geotools.swing.wizard.JWizard;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.style.ContrastMethod;

public class ImageLab {

    public static void main(String[] args) throws Exception {
        ImageLab me = new ImageLab();
        me.getLayersAndDisplay();
    }

    private void getLayersAndDisplay() throws Exception {
        DataWizard wizard = new DataWizard("ImageLab");
        int rtnVal = wizard.showModalDialog();
        if (rtnVal != DataWizard.FINISH) {
            System.exit(0);
        }

        displayLayers(wizard.getRasterFile(), wizard.getShapefile());
    }
    
    private void displayLayers(File rasterFile, File shpFile) throws Exception {
        GeoTiffReader geotiffReader;
        try {
            geotiffReader = new GeoTiffReader(rasterFile);

        } catch (DataSourceException ex) {
            ex.printStackTrace();
            return;
        }

        Style rasterStyle = createRGBStyle(geotiffReader);
        if (rasterStyle == null) {
            // input coverage didn't have bands labelled "red", "green", "blue"
            return;
        }

        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shpFile);
        FeatureSource<SimpleFeatureType, SimpleFeature> shapefileSource = dataStore.getFeatureSource();

        Style shpStyle = SLD.createPolygonStyle(Color.YELLOW, null, 0.0f);

        MapContext map = new DefaultMapContext();
        map.setTitle("My coverage");

        map.addLayer(geotiffReader, rasterStyle);
        map.addLayer(shapefileSource, shpStyle);

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


    class DataWizard extends JWizard {

        DataPage page;

        public DataWizard(String title) {
            super(title);
            
            page = new DataPage();
            this.registerWizardPanel(page);
        }

        File getRasterFile() { return page.rasterFile; }

        File getShapefile() { return page.shapeFile; }
    }

    class DataPage extends JPage {

        JTextField rasterTxt;
        JTextField shapefileTxt;

        File rasterFile;
        File shapeFile;

        @Override
        public JPanel createPanel() {
            JPanel page = new JPanel(new MigLayout());

            JLabel label = new JLabel("Raster file");
            page.add(label);

            rasterTxt = new JTextField(30);
            page.add(rasterTxt, "growx");

            JButton btn = new JButton("Browse");
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileDataStoreChooser chooser = new JFileDataStoreChooser(new String[]{"tif", "tiff"});
                    chooser.setDialogTitle("Choose a GeoTIFF file");
                    if (chooser.showOpenDialog(null) == JFileDataStoreChooser.APPROVE_OPTION) {
                        rasterTxt.setText(chooser.getSelectedFile().getAbsolutePath());
                        DataPage.this.getJWizard().getController().syncButtonsToPage();
                    }
                }
            });
            page.add(btn, "wrap");


            label = new JLabel("Shapefile");
            page.add(label);

            shapefileTxt = new JTextField(30);
            page.add(shapefileTxt, "growx");

            btn = new JButton("Browse");
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
                    chooser.setDialogTitle("Choose a shapefile");
                    if (chooser.showOpenDialog(null) == JFileDataStoreChooser.APPROVE_OPTION) {
                        shapefileTxt.setText(chooser.getSelectedFile().getAbsolutePath());
                        DataPage.this.getJWizard().getController().syncButtonsToPage();
                    }
                }
            });
            page.add(btn, "wrap");

            return page;
        }

        @Override
        public boolean isValid() {
            rasterFile = new File(rasterTxt.getText());
            if (!rasterFile.exists()) {
                return false;
            }

            shapeFile = new File(shapefileTxt.getText());
            if (!shapeFile.exists()) {
                return false;
            }

            return true;
        }

    }
}
