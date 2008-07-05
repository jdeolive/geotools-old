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
package org.geotools.wfs.v_1_1_0.data;

import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.CUBEWERX_GOVUNITCE_CRS;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.CUBEWERX_GOVUNITCE_DATA;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.CUBEWERX_GOVUNITCE_SCHEMA;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.CUBEWERX_GOVUNITCE_TYPENAME;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.CUBEWERX_ROADSEG_CRS;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.CUBEWERX_ROADSEG_DATA;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.CUBEWERX_ROADSEG_SCHEMA;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.CUBEWERX_ROADSEG_TYPENAME;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.GEOS_ARCHSITES_CRS;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.GEOS_ARCHSITES_DATA;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.GEOS_ARCHSITES_SCHEMA;
import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.GEOS_ARCHSITES_TYPENAME;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.referencing.CRS;
import org.geotools.test.TestData;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This abstract class comprises a sort of compliance tests for
 * {@link GetFeatureParser} implementations.
 * <p>
 * Subclasses shall just provide an implementation for
 * {@link #getParser(QName, SimpleFeatureType, String)}
 * </p>
 * <p>
 * Note this test depends on {@link EmfAppSchemaParser} to function correctly in
 * order to obtain the test FeatureTypes from the DescribeFeatureType response
 * samples under {@code test-data/}.
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id: StreamingParserFeatureReaderTest.java 28884 2008-01-22
 *          15:21:03Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/test/java/org/geotools/wfs/v_1_1_0/data/StreamingParserFeatureReaderTest.java $
 * @see XmlSimpleFeatureParserTest
 * @see StreamingParserFeatureReaderTest
 */
public abstract class AbstractGetFeatureParserTest extends TestCase {

    /**
     * Configuration object used to parse the sample schemas
     * 
     * @see #getTypeView(QName, String, String, String[])
     */
    private static final Configuration wfsConfiguration = new WFSConfiguration();

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * A feature visitor used to assert the parsed features
     * 
     * @author Gabriel Roldan (TOPP)
     * @version $Id: AbstractGetFeatureParserTest.java 29265 2008-02-13
     *          01:40:34Z groldan $
     * @since 2.5.x
     * @source $URL:
     *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/test/java/org/geotools/wfs/v_1_1_0/data/AbstractGetFeatureParserTest.java $
     */
    private static class FeatureAssertor implements FeatureVisitor {

        private SimpleFeatureType featureType;

        /**
         * A featuretype which might be a subset of the actual FeatureType whose
         * attributes will be used to assert the features.
         * 
         * @param featureType
         */
        public FeatureAssertor(SimpleFeatureType featureType) {
            this.featureType = featureType;
        }

        public void visit(final Feature feature) {
            assertNotNull(feature);
            assertNotNull(feature.getID());
            for (AttributeDescriptor descriptor : featureType.getAttributeDescriptors()) {
                final String name = descriptor.getLocalName();
                Property property = feature.getProperty(name);
                assertNotNull(name + " property was not parsed", property);
                assertNotNull("got null value for property " + name, property.getValue());
            }
        }
    }

    /**
     * Parses the featuretype from the test file referenced by
     * {@code schemaName} and returns a new, subset FeatureType comprised of
     * only the required {@code properties}
     * 
     * @param featureName
     *            the name of the Features produced for the target FeatureType
     *            (i.e. {@code topp:states} instead of {@code topp:states_Type})
     * @param schemaName
     *            the location of the schema file under {@code test-data/}
     * @param epsgCrsId
     *            the EPSG identifier for the feature type CRS (eg.
     *            {@code "EPSG:4326"})
     * @param properties
     *            the property names to include from the original schema in the
     *            one to be returned
     * @return a subset of the original featuretype containing only the required
     *         {@code properties}
     */
    private SimpleFeatureType getTypeView(final QName featureName, final String schemaName,
            final String epsgCrsId, final String[] properties) throws Exception {

        URL schemaLocation = TestData.getResource(this, schemaName);
        CoordinateReferenceSystem crs = CRS.decode(epsgCrsId);

        SimpleFeatureType originalType = EmfAppSchemaParser.parseSimpleFeatureType(
                wfsConfiguration, featureName, schemaLocation, crs);

        SimpleFeatureType subsetType = DataUtilities.createSubType(originalType, properties);
        return subsetType;
    }

    /**
     * Uses a {@link StreamingParserFeatureReader} to parse the features while
     * traversing the feature collection in a test {@code wfs:FeatureCollection}
     * document; {@code assertor} is a visitor provided by the actual unit test
     * calling this method, every feature fetched is passed to the visitor who
     * contains the specific assertions.
     * 
     * @param featureName
     *            the name of the features (not the feature type) expected
     * @param getFeatureResultTestFile
     *            the name of the test file name to load in order to simulate
     *            the response of a GetFeature request
     * @param assertor
     *            a FeatureVisitor to assert the contents or structure of the
     *            features
     * @param expectedFeatureCount
     *            the number of features there should be on the feature
     *            collection, an assertion is made at the end of the method.
     * @param schemaName
     * @throws Exception
     */
    private void testParseGetFeatures(final QName featureName,
            final SimpleFeatureType queryFeatureType, final GetFeatureParser parser,
            final FeatureVisitor assertor, final int expectedFeatureCount) throws Exception {

        int featureCount = 0;
        SimpleFeature feature;

        try {
            for (int i = 0; i < expectedFeatureCount; i++) {
                feature = parser.parse();
                featureCount++;
                assertor.visit(feature);
            }
            feature = parser.parse();
            assertNull(feature);
        } finally {
            parser.close();
        }

        assertEquals(expectedFeatureCount, featureCount);
    }

    /**
     * Subclasses need to implement in order to provide a specific
     * {@link GetFeatureParser} implementation settled up for the given
     * featureName and dataFile containing the test GetFeature request response.
     * 
     * @param featureName
     * @param schemaLocation
     * @param featureType
     * @param getFeaturesRequest
     *            the URL representing the GetFeature request. Opening its input
     *            stream shall suffice to get the GetFeature response.
     * @return
     * @throws IOException
     */
    protected abstract GetFeatureParser getParser(QName featureName, String schemaLocation,
            SimpleFeatureType featureType, URL getFeaturesRequest) throws IOException;

    /**
     * Verifies correctness on parsing a normal geoserver WFS 1.1.0 GetFeature
     * response.
     * 
     * Test method for {@link StreamingParserFeatureReader#parse()}.
     * 
     * @throws Exception
     */
    public void testParseGeoServer_ArchSites() throws Exception {
        final QName featureName = GEOS_ARCHSITES_TYPENAME;
        final int expectedCount = 3;
        final String schemaLocation = GEOS_ARCHSITES_SCHEMA;

        final String[] properties = { "cat", "str1", "the_geom" };
        final SimpleFeatureType featureType;
        featureType = getTypeView(featureName, schemaLocation, GEOS_ARCHSITES_CRS, properties);

        final FeatureVisitor assertor = new FeatureAssertor(featureType);

        URL url = TestData.getResource(this, GEOS_ARCHSITES_DATA);
        GetFeatureParser parser = getParser(featureName, schemaLocation, featureType, url);
        testParseGetFeatures(featureName, featureType, parser, assertor, expectedCount);
    }

    /**
     * Verifies correctness on parsing a sample CubeWerx WFS 1.1.0 GetFeature
     * response.
     * 
     * @throws Exception
     */
    public void testParseCubeWerx_GovernmentalUnitCE() throws Exception {
        final QName featureName = CUBEWERX_GOVUNITCE_TYPENAME;
        final String schemaLocation = CUBEWERX_GOVUNITCE_SCHEMA;
        final int expectedCount = 3;

        final String[] properties = { "geometry", "instanceName", "instanceCode",
                "governmentalUnitType" };

        final SimpleFeatureType featureType = getTypeView(featureName, schemaLocation,
                CUBEWERX_GOVUNITCE_CRS, properties);

        final FeatureVisitor assertor = new FeatureAssertor(featureType);

        URL url = TestData.getResource(this, CUBEWERX_GOVUNITCE_DATA);
        GetFeatureParser parser = getParser(featureName, schemaLocation, featureType, url);
        testParseGetFeatures(featureName, featureType, parser, assertor, expectedCount);
    }

    public void testParseCubeWerx_RoadSeg() throws Exception {
        final String[] properties = { "lastUpdateDate", "geometry", "status", "isAnchorSection" };
        final QName featureName = CUBEWERX_ROADSEG_TYPENAME;
        final String schemaLocation = CUBEWERX_ROADSEG_SCHEMA;
        final SimpleFeatureType featureType = getTypeView(featureName, schemaLocation,
                CUBEWERX_ROADSEG_CRS, properties);

        URL url = TestData.getResource(this, CUBEWERX_ROADSEG_DATA);
        final GetFeatureParser parser = getParser(featureName, schemaLocation, featureType, url);
        FeatureVisitor assertor = new FeatureAssertor(featureType);
        testParseGetFeatures(featureName, featureType, parser, assertor, 3);
    }

    protected void runGetFeaturesParsing() throws Exception {
        GetFeatureParser reader;
        {
            final String[] properties = { "geometry", "instanceName", "instanceCode",
                    "governmentalUnitType" };

            final URL getFeatures = new URL(
                    "http://frameworkwfs.usgs.gov/framework/wfs/wfs.cgi?DATASTORE=Framework&DATASTORE=Framework&"
                            + "SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=gubs:GovernmentalUnitCE&"
                            + "PROPERTYNAME=geometry,instanceName,instanceCode,governmentalUnitType&maxFeatures=100");

            // create a subtype with only the required properties
            final SimpleFeatureType featureType = getTypeView(CUBEWERX_GOVUNITCE_TYPENAME,
                    CUBEWERX_GOVUNITCE_SCHEMA, CUBEWERX_GOVUNITCE_CRS, properties);

            System.out.println("Getting parser for " + getFeatures.toExternalForm());
            reader = getParser(CUBEWERX_GOVUNITCE_TYPENAME, CUBEWERX_GOVUNITCE_SCHEMA, featureType,
                    getFeatures);

            System.out.println("Got " + reader.getClass().getSimpleName());
        }

        int count = 0;
        SimpleFeature feature;
        Object defaultGeometry;
        System.out.println("Parsing features...");

        Runtime runtime = Runtime.getRuntime();
        long initialMem = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.currentTimeMillis();

        while ((feature = reader.parse()) != null) {
            defaultGeometry = feature.getDefaultGeometry();
            count++;
            System.out.print('.');
            if (count % 100 == 0) {
                System.out.print('\n');
            }
        }
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        long endMem = runtime.totalMemory() - runtime.freeMemory();
        long memUsed = (endMem - initialMem) / (1024 * 1024);
        System.out.println("\nFetched " + count + " features " + " in " + totalTime + "ms. (avg. "
                + (totalTime / count) + "ms/feature) Mem. used: " + memUsed + "MB.");
    }
}
