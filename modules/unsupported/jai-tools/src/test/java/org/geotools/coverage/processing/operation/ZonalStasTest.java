/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.processing.operation;

import static java.lang.Math.round;
import jaitools.media.jai.zonalstats.Result;
import jaitools.media.jai.zonalstats.ZonalStats;
import jaitools.media.jai.zonalstats.ZonalStatsDescriptor;
import jaitools.numeric.Statistic;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.media.jai.ROI;
import javax.media.jai.ROIShape;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ZonalStasTest extends TestCase {
	
	final static double DELTA = 10E-4;
	
	private final static Logger LOGGER = Logger.getLogger(ZonalStasTest.class.toString());
	
	private class StatisticsTool {

	    /*
	     * external user params
	     */
	    private Set<Statistic> statisticsSet;
	    private Integer[] bands;
	    private GridCoverage2D gridCoverage2D;
	    private List<SimpleFeature> featureList;

	    /*
	     * results
	     */
	    private Map<String, Map<Statistic, List<Result>>> feature2StatisticsMap = new HashMap<String, Map<Statistic, List<Result>>>();

	    private StatisticsTool( Set<Statistic> statisticsSet, GridCoverage2D gridCoverage2D,
	            Integer[] bands, List<SimpleFeature> polygonsList ) {
	        this.statisticsSet = statisticsSet;
	        this.gridCoverage2D = gridCoverage2D;
	        this.bands = bands;
	        this.featureList = polygonsList;
	    }

	    /**
	     * Run the requested analysis.
	     * 
	     * <p>
	     * This is the moment in which the analysis takes place. This method
	     * is intended to give the user the possibility to choose the moment
	     * in which the workload is done.  
	     * @throws Exception 
	     */
	    public void run() throws Exception {
	    	processPolygonMode();
	    }

	    private void processPolygonMode() throws TransformException {
	        final AffineTransform gridToWorldTransformCorrected = new AffineTransform(
	                (AffineTransform) ((GridGeometry2D) gridCoverage2D.getGridGeometry())
	                        .getGridToCRS2D(PixelOrientation.UPPER_LEFT));
	        final MathTransform worldToGridTransform;
	        try {
	            worldToGridTransform = ProjectiveTransform.create(gridToWorldTransformCorrected.createInverse());
	        } catch (NoninvertibleTransformException e) {
	            throw new IllegalArgumentException(e.getLocalizedMessage());
	        }

	        for (SimpleFeature feature : featureList ) {
	            final String fid = feature.getID();
	            final Geometry geometry = (Geometry) feature.getDefaultGeometry();
	            if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
	                final BoundingBox bbox = feature.getBounds();
	                final ReferencedEnvelope rEnvelope = new ReferencedEnvelope(bbox);

	                /*
	                 * crop on region of interest
	                 */
	                final AbstractProcessor processor = AbstractProcessor.getInstance();
	                final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
	                param.parameter("Source").setValue(gridCoverage2D);
	                param.parameter("Envelope").setValue(rEnvelope);
	                final GridCoverage2D cropped = (GridCoverage2D) processor.doOperation(param);

	                ROI roi = null;
	                final int numGeometries = geometry.getNumGeometries();
	                for( int i = 0; i < numGeometries; i++ ) {
	                    Geometry geometryN = geometry.getGeometryN(i);
	                    java.awt.Polygon awtPolygon = toAWTPolygon((Polygon) geometryN,
	                            worldToGridTransform);
	                    if (roi == null) {
	                        roi = new ROIShape(awtPolygon);
	                    } else {
	                        ROI newRoi = new ROIShape(awtPolygon);
	                        roi.add(newRoi);
	                    }
	                }

	                final Statistic[] statistis = statisticsSet.toArray(new Statistic[statisticsSet.size()]);

	                final OperationJAI op = new OperationJAI("ZonalStats");
	                ParameterValueGroup params = op.getParameters();
	                params.parameter("dataImage").setValue(cropped);
	                params.parameter("stats").setValue(statistis);
	                params.parameter("bands").setValue(bands);
	                params.parameter("roi").setValue(roi);

	                final GridCoverage2D coverage = (GridCoverage2D) op.doOperation(params, null);
	                final ZonalStats stats = (ZonalStats) coverage.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
	                final Map<Statistic, List<Result>> statsMap = new HashMap<Statistic, List<Result>>();
	                for( Statistic statistic : statistis ) {
	                	List<Result> statsResult = stats.statistic(statistic).results();
	                    statsMap.put(statistic, statsResult);
	                }
	                feature2StatisticsMap.put(fid, statsMap);
	            }
	        }

	    }

	    private java.awt.Polygon toAWTPolygon( final Polygon roiInput,
	            MathTransform worldToGridTransform ) throws TransformException {
	        final boolean isIdentity = worldToGridTransform.isIdentity();
	        final java.awt.Polygon retValue = new java.awt.Polygon();
	        final double coords[] = new double[2];
	        final LineString exteriorRing = roiInput.getExteriorRing();
	        final CoordinateSequence exteriorRingCS = exteriorRing.getCoordinateSequence();
	        final int numCoords = exteriorRingCS.size();
	        for( int i = 0; i < numCoords; i++ ) {
	            coords[0] = exteriorRingCS.getX(i);
	            coords[1] = exteriorRingCS.getY(i);
	            if (!isIdentity)
	                worldToGridTransform.transform(coords, 0, coords, 0, 1);
	            retValue.addPoint((int) round(coords[0] + 0.5d), (int) round(coords[1] + 0.5d));
	        }
	        return retValue;
	    }

	    /**
	     * Gets the performed statistics.
	     *
	     * @param fId the id of the feature used as region for the analysis.
	     * @return the {@link List} of results of the analysis for all the 
	     *          requested {@link Statistic} for the requested bands. Note 
	     *          that the result contains for every {@link Statistic} a result
	     *          value for every band.
	     */
	    public Map<Statistic, List<Result>> getStatistics( String fId ) {
	        return feature2StatisticsMap.get(fId);
	    }
	}

	@Before
	public void setUp() throws Exception {
		TestData.unzipFile(this, "test.zip");
	}
	
	@Test
    public void testPolygonZone() throws Exception {
		final File file = TestData.file(this,"test.tif");
		final GeoTiffReader reader = new GeoTiffReader(file);
		final GridCoverage2D coverage2D = (GridCoverage2D) reader.read(null);
        
        final File fileshp = TestData.file(this,"testpolygon.shp");
        final DataStore store = FileDataStoreFinder.getDataStore(fileshp.toURL());
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = store.getFeatureSource(store.getNames().get(0));
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();
        List<SimpleFeature> polygonList = new ArrayList<SimpleFeature>();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            polygonList.add(feature);
        }
        featureCollection.close(featureIterator);

        // choose the stats
        Set<Statistic> statsSet = new LinkedHashSet<Statistic>();
        statsSet.add(Statistic.MIN);
        statsSet.add(Statistic.MAX);
        statsSet.add(Statistic.MEAN);
        statsSet.add(Statistic.MEDIAN);
        statsSet.add(Statistic.VARIANCE);
        statsSet.add(Statistic.SDEV);
        statsSet.add(Statistic.RANGE);
        statsSet.add(Statistic.APPROX_MEDIAN);
        // statsSet.add(Statistic.SUM);
        // statsSet.add(Statistic.ACTIVECELLS);

        // select the bands to work on
        Integer[] bands = new Integer[]{0};

        // create the proper instance
        StatisticsTool statisticsTool = new StatisticsTool(statsSet, coverage2D, bands, polygonList);

        // do analysis
        statisticsTool.run();

        // get the results
        String id = "testpolygon.1";
        Map<Statistic, List<Result>> statistics = statisticsTool.getStatistics(id);
        LOGGER.info(id + statistics.toString());
        assertEquals(statistics.get(Statistic.RANGE).get(0).getValue().doubleValue(), 363.0, DELTA);
        assertEquals(statistics.get(Statistic.MEDIAN).get(0).getValue().doubleValue(), 1349.0, DELTA);
        assertEquals(statistics.get(Statistic.SDEV).get(0).getValue().doubleValue(), 71.108, DELTA);
        assertEquals(statistics.get(Statistic.APPROX_MEDIAN).get(0).getValue().doubleValue(), 1351.0, DELTA);
        assertEquals(statistics.get(Statistic.MIN).get(0).getValue().doubleValue(), 1255.0, DELTA);
        assertEquals(statistics.get(Statistic.MEAN).get(0).getValue().doubleValue(), 1360.5278,DELTA);
        assertEquals(statistics.get(Statistic.VARIANCE).get(0).getValue().doubleValue(), 5056.3548, DELTA);
        assertEquals(statistics.get(Statistic.MAX).get(0).getValue().doubleValue(), 1618.0, DELTA);
        

        id = "testpolygon.2";
        statistics = statisticsTool.getStatistics(id);
        LOGGER.info(id + statistics.toString());
        assertEquals(statistics.get(Statistic.RANGE).get(0).getValue().doubleValue(), 216.0, DELTA);
        assertEquals(statistics.get(Statistic.MEDIAN).get(0).getValue().doubleValue(), 1251.0, DELTA);
        assertEquals(statistics.get(Statistic.SDEV).get(0).getValue().doubleValue(), 42.6214, DELTA);
        assertEquals(statistics.get(Statistic.APPROX_MEDIAN).get(0).getValue().doubleValue(), 1254.0, DELTA);
        assertEquals(statistics.get(Statistic.MIN).get(0).getValue().doubleValue(), 1192.0, DELTA);
        assertEquals(statistics.get(Statistic.MEAN).get(0).getValue().doubleValue(), 1256.8206,DELTA);
        assertEquals(statistics.get(Statistic.VARIANCE).get(0).getValue().doubleValue(), 1816.5803, DELTA);
        assertEquals(statistics.get(Statistic.MAX).get(0).getValue().doubleValue(), 1408.0, DELTA);

        id = "testpolygon.3";
        statistics = statisticsTool.getStatistics(id);
        LOGGER.info(id + statistics.toString());
        assertEquals(statistics.get(Statistic.RANGE).get(0).getValue().doubleValue(), 178.0000, DELTA);
        assertEquals(statistics.get(Statistic.MEDIAN).get(0).getValue().doubleValue(), 1289.0, DELTA);
        assertEquals(statistics.get(Statistic.SDEV).get(0).getValue().doubleValue(),  34.4797, DELTA);
        assertEquals(statistics.get(Statistic.APPROX_MEDIAN).get(0).getValue().doubleValue(), 1280.0, DELTA);
        assertEquals(statistics.get(Statistic.MIN).get(0).getValue().doubleValue(), 1173.0, DELTA);
        assertEquals(statistics.get(Statistic.MEAN).get(0).getValue().doubleValue(), 1280.4892,DELTA);
        assertEquals(statistics.get(Statistic.VARIANCE).get(0).getValue().doubleValue(), 1188.8471, DELTA);
        assertEquals(statistics.get(Statistic.MAX).get(0).getValue().doubleValue(), 1351.0, DELTA);

        reader.dispose();
    }

}

