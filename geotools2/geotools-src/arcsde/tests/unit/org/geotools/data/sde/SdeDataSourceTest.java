/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.sde;

import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.xml.parsers.*;

import org.geotools.data.*;
import org.geotools.data.sde.old.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryFilter;
import org.geotools.gml.*;
import org.xml.sax.helpers.*;
import com.vividsolutions.jts.geom.*;
import junit.framework.*;

/**
 * SdeDatasource's test cases
 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public class SdeDataSourceTest extends TestCase
{
    /** DOCUMENT ME! */
    private static Logger LOGGER = Logger.getLogger("org.geotools.data.sde");

    /** DOCUMENT ME! */
    private String dataFolder = "/testData/";

    /** DOCUMENT ME! */
    SdeDataSourceFactory sdeDsFactory = null;

    /** DOCUMENT ME! */
    private Properties conProps = null;

    /** DOCUMENT ME! */
    private String point_table;

    /** DOCUMENT ME! */
    private String line_table;

    /** DOCUMENT ME! */
    private String polygon_table;

    /** DOCUMENT ME! */
    FilterFactory ff = FilterFactory.createFilterFactory();

    /**
     * Creates a new SdeDataSourceTest object.
     */
    public SdeDataSourceTest()
    {
        this("ArcSDE DataSource unit tests");
    }

    /**
     * Creates a new SdeDataSourceTest object.
     *
     * @param name DOCUMENT ME!
     */
    public SdeDataSourceTest(String name)
    {
        super(name);
        URL folderUrl = getClass().getResource("/testData");
        dataFolder = folderUrl.toExternalForm() + "/";
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        String failMsg = null;
        conProps = new Properties();
        String propsFile = "/testData/testparams.properties";
        conProps.load(getClass().getResourceAsStream(propsFile));
        point_table = conProps.getProperty("point_table");
        line_table = conProps.getProperty("line_table");
        polygon_table = conProps.getProperty("polygon_table");
        assertNotNull(point_table);
        assertNotNull(line_table);
        assertNotNull(polygon_table);
        sdeDsFactory = new SdeDataSourceFactory();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception
    {
        conProps = null;
        sdeDsFactory = null;
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     */
    public void testConnect()
    {
        LOGGER.info("testing connection to the sde database");
        SdeConnectionPoolFactory pf = SdeConnectionPoolFactory.getInstance();
        SdeConnectionConfig congfig = null;
        try
        {
            congfig = new SdeConnectionConfig(conProps);
        }
        catch (Exception ex)
        {
            pf.clear(); //close and remove all pools

            fail(ex.getMessage());
        }

        try
        {
            SdeConnectionPool pool = pf.getPoolFor(congfig);

            LOGGER.info("connection succeed");
        }
        catch (DataSourceException ex)
        {
            fail(ex.getMessage());
        }
        finally
        {
            pf.clear(); //close and remove all pools
        }
    }

    public void testFinder()
    {
      DataSource sdeDs = null;
      try {
        Map dsProps = new HashMap(conProps);
        dsProps.put("table", point_table);
        sdeDs = DataSourceFinder.getDataSource(dsProps);
        String failMsg = sdeDs +  " is not an SdeDataSource";
        assertTrue(failMsg, (sdeDs instanceof SdeDataSource));
      }
      catch (DataSourceException ex) {
        ex.printStackTrace();
        fail("can't find the SdeDataSource:" + ex.getMessage());
      }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void testGetFeaturesPoint() throws DataSourceException
    {
        testGetFeatures("points", point_table);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void testGetFeaturesLine() throws DataSourceException
    {
        testGetFeatures("lines", line_table);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void testGetFeaturesPolygon() throws DataSourceException
    {
        testGetFeatures("polygons", polygon_table);
    }

    /**
     * DOCUMENT ME!
     *
     * @param wich DOCUMENT ME!
     * @param table DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private void testGetFeatures(String wich, String table)
        throws DataSourceException
    {
        LOGGER.info("getting all features from " + table);
        SdeDataSource sdeDataSource = getDataSource(table);
        int expectedCount = getExpectedCount("getfeatures." + wich
                + ".expectedCount");

        FeatureCollection features = sdeDataSource.getFeatures();
        int fCount = features.size();
        String failMsg = "Expected and returned result count does not match";
        assertEquals(failMsg, expectedCount, fCount);
        LOGGER.info("fetched " + fCount + " features for " + wich
            + " layer, OK");
    }

    /**
     * DOCUMENT ME!
     */
    public void testSQLFilterPoints()
    {
        String uri = getFilterUri("filters.sql.points.filter");
        int expected = getExpectedCount("filters.sql.points.expectedCount");
        testFilter(uri, point_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testSQLFilterLines()
    {
        String uri = getFilterUri("filters.sql.lines.filter");
        int expected = getExpectedCount("filters.sql.lines.expectedCount");
        testFilter(uri, line_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testSQLFilterPolygons()
    {
        String uri = getFilterUri("filters.sql.polygons.filter");
        int expected = getExpectedCount("filters.sql.polygons.expectedCount");
        testFilter(uri, polygon_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterPoints()
    {
        //String uri = getFilterUri("filters.bbox.points.filter");
        //int expected = getExpectedCount("filters.bbox.points.expectedCount");
        int expected = 6;

        testBBox(point_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterLines()
    {
        //String uri = getFilterUri("filters.bbox.lines.filter");
        //int expected = getExpectedCount("filters.bbox.lines.expectedCount");
        int expected = 22;

        testBBox(line_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterPolygons()
    {
        //String uri = getFilterUri("filters.bbox.polygons.filter");
        //int expected = getExpectedCount("filters.bbox.polygons.expectedCount");
        int expected = 8;

        testBBox(polygon_table, expected);
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param expected DOCUMENT ME!
     */
    private void testBBox(String table, int expected)
    {
        try
        {
            DataSource ds = getDataSource(table);
            Filter bboxFilter = getBBoxfilter(ds);
            testFilter(bboxFilter, ds, expected);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            super.fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param ds DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Filter getBBoxfilter(DataSource ds) throws Exception
    {
        Envelope env = new Envelope(-60, -40, -55, -20);
        BBoxExpression bbe = ff.createBBoxExpression(env);
        GeometryFilter gf = ff.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);

        FeatureType schema = ds.getSchema();
        Expression attExp = ff.createAttributeExpression(schema,
                schema.getDefaultGeometry().getName());

        gf.addLeftGeometry(attExp);
        gf.addRightGeometry(bbe);

        return gf;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filterKey DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String getFilterUri(String filterKey)
    {
        String filterFileName = conProps.getProperty(filterKey);

        if (filterFileName == null)
        {
            super.fail(filterKey
                + " param not found in tests configurarion properties file");
        }

        String uri = dataFolder + filterFileName;

        return uri;
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private int getExpectedCount(String key)
    {
        try
        {
            return Integer.parseInt(conProps.getProperty(key));
        }
        catch (NumberFormatException ex)
        {
            super.fail(key
                + " parameter not found or not an integer in testParams.properties");
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filterUri DOCUMENT ME!
     * @param table DOCUMENT ME!
     * @param expected DOCUMENT ME!
     */
    private void testFilter(String filterUri, String table, int expected)
    {
        try
        {
            DataSource ds = getDataSource(table);
            Filter filter = parseDocument(filterUri);
            testFilter(filter, ds, expected);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     * @param ds DOCUMENT ME!
     * @param expected DOCUMENT ME!
     */
    private void testFilter(Filter filter, DataSource ds, int expected)
    {
        try
        {
            FeatureCollection fc = ds.getFeatures(filter);
            int fCount = fc.size();
            String failMsg = "Expected and returned result count does not match";
            assertEquals(failMsg, expected, fCount);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testGeometryIntersectsFilters()
    {
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private SdeDataSource getDataSource(String table)
        throws DataSourceException
    {
        conProps.setProperty("table", table);
        SdeDataSource ds = (SdeDataSource) sdeDsFactory.createDataSource(conProps);
        return ds;
    }

    /**
     * stolen from filter module tests
     *
     * @param uri DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public Filter parseDocument(String uri) throws Exception
    {
        LOGGER.finest("about to create parser");
        SAXParserFactory factory = SAXParserFactory.newInstance();

        // chains all the appropriate filters together (in correct order)
        //  and initiates parsing
        TestFilterHandler filterHandler = new TestFilterHandler();
        FilterFilter filterFilter = new FilterFilter(filterHandler, null);
        GMLFilterGeometry geometryFilter = new GMLFilterGeometry(filterFilter);
        GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
        SAXParserFactory fac = SAXParserFactory.newInstance();
        SAXParser parser = fac.newSAXParser();

        ParserAdapter p = new ParserAdapter(parser.getParser());
        p.setContentHandler(documentFilter);

        LOGGER.fine("just made parser, " + uri);
        p.parse(uri);
        LOGGER.finest("just parsed: " + uri);
        Filter filter = filterHandler.getFilter();
        return filter;
    }
}
