/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process.raster;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.ProcessException;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.util.ProgressListener;
import static org.junit.Assert.*;

/**
 * Unit tests for {@code JMapPaneModel}.
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public class RasterToVectorProcessTest {

    private static final GridCoverageFactory covFactory = CoverageFactoryFinder.getGridCoverageFactory(null);

    /**
     * Test conversion with a tiny coverage
     */
    @Test
    public void testConvert() throws Exception {
        System.out.println("   convert simple raster");

        // small raster with 3 regions, two which touch at a corner
        final float[][] DATA = {
            {1, 1, 0, 1},
            {0, 1, 0, 0},
            {0, 1, 1, 1},
            {1, 0, 0, 1}
        };

        final int DATA_WIDTH = 4;
        final int CELL_WIDTH = 100;
        final int PERIMETER = 2400;
        final int AREA = 90000;

        GridCoverage2D cov = covFactory.create(
                "coverage",
                DATA,
                new ReferencedEnvelope(0, DATA_WIDTH * CELL_WIDTH, 0, DATA_WIDTH * CELL_WIDTH, null));

        int band = 0;
        Set<Double> outsideValues = new HashSet<Double>();
        outsideValues.add(0d);

        ProgressListener progress = null;
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc =
                RasterToVectorProcess.process(cov, band, null, outsideValues, progress);

        double perimeter = 0;
        double area = 0;
        FeatureIterator iter = fc.features();
        try {
            while (iter.hasNext()) {
                SimpleFeature feature = (SimpleFeature) iter.next();
                Polygon poly = (Polygon) feature.getDefaultGeometry();
                perimeter += poly.getLength();
                area += poly.getArea();
            }
        } finally {
            iter.close();
        }

        assertEquals(AREA, (int) Math.round(area));
        assertEquals(PERIMETER, (int) Math.round(perimeter));
    }

    /**
     * Test conversion with an image that gave problems at one stage
     */
    @Test
    public void testProblemTiff() throws IOException, ProcessException {
        System.out.println("   convert problem image");

        final double ROUND_OFF_TOLERANCE = 1.0e-4D;

        URL url = getClass().getResource("data/viewshed.tif");
        BufferedImage img = ImageIO.read(url);

        ReferencedEnvelope env = new ReferencedEnvelope(
                new Rectangle2D.Double(img.getMinX(), img.getMinY(), img.getWidth(), img.getHeight()),
                null);

        GridCoverage2D cov = covFactory.create("coverage", img, env);

        int band = 0;
        int outside = -1;
        Set<Double> outsideValues = Collections.singleton(Double.valueOf(outside));

        ProgressListener progress = null;
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc =
                RasterToVectorProcess.process(cov, band, null, outsideValues, progress);

        // validate geometries and sum areas
        FeatureIterator<SimpleFeature> iter = fc.features();
        Map<Integer, Double> areas = new HashMap<Integer, Double>();
        try {
            while (iter.hasNext()) {
                SimpleFeature feature = iter.next();
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                assertTrue(geom.isValid());

                int code = ((Number) feature.getAttribute("code")).intValue();
                if (code != outside) {
                    Double sum = areas.get(code);
                    if (sum == null) {
                        sum = 0.0d;
                    }
                    sum += geom.getArea();
                    areas.put(code, sum);
                }
            }
        } finally {
            iter.close();
        }

        // compare summed areas to image data
        Map<Integer, Double> imgAreas = new HashMap<Integer, Double>();
        Raster tile = img.getTile(0, 0);
        for (int y = img.getMinY(), ny = 0; ny < img.getHeight(); y++, ny++) {
            for (int x = img.getMinX(), nx = 0; nx < img.getWidth(); x++, nx++) {
                int code = tile.getSample(x, y, 0);
                if (code != outside) {
                    Double sum = areas.get(code);
                    if (sum == null) {
                        sum = 1.0D;
                    } else {
                        sum += 1.0D;
                    }
                    areas.put(code, sum);
                }
            }
        }

        for (Integer code : imgAreas.keySet()) {
            double ratio = areas.get(code) / imgAreas.get(code);
            assertTrue(Math.abs(1.0D - ratio) < ROUND_OFF_TOLERANCE);
        }
    }
}
