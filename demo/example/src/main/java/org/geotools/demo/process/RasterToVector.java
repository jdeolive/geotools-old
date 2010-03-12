/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.demo.process;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Collections;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.Envelope2D;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.process.raster.RasterToVectorProcess;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author Michael Bedward
 */
public class RasterToVector {
    
    public static void main(String[] args) throws Exception {
        new RasterToVector().demo();
    }

    private void demo() throws Exception {
        Envelope2D env = new Envelope2D(DefaultGeographicCRS.WGS84, 0.0, 0.0, 8.0, 8.0);
        GridCoverage2D cov = createChessboardCoverage(256, 256, 32, env);
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = RasterToVectorProcess.process(cov, 0, env, Collections.singletonList(0.0d), null);

        MapContext map = new DefaultMapContext();
        map.setTitle("raster to vector conversion");
        Style style = SLD.createPolygonStyle(Color.BLUE, Color.CYAN, 1.0f);
        map.addLayer(fc, style);
        JMapFrame.showMap(map);
    }

    private GridCoverage2D createChessboardCoverage(int imgWidth, int imgHeight, int squareWidth, Envelope2D env) {
        GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);
        GridCoverage2D cov = factory.create("chessboard", createChessboardImage(imgWidth, imgHeight, squareWidth), env);
        return cov;
    }

    private RenderedImage createChessboardImage(int imgWidth, int imgHeight, int squareWidth) {
        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_BINARY);
        WritableRaster raster = img.getRaster();

        for (int y = 0; y < imgHeight; y++) {
            boolean oddRow = (y / squareWidth) % 2 == 1;
            for (int x = 0; x < imgWidth; x++) {
                boolean oddCol = (x / squareWidth) % 2 == 1;
                raster.setSample(x, y, 0, (oddCol == oddRow ? 1 : 0));
            }
        }

        return img;
    }
}
