/*
 * StylingTest.java
 * JUnit based test
 *
 * Created on April 12, 2002, 1:18 PM
 */

package org.geotools.renderer.lite;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.MemoryDataSource;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;




/**
 *
 * @author jamesm
 */
public class Rendering2DTest extends TestCase {
    /** path for test data */
    // private java.net.URL base = getClass().getResource("/testData/");
    
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    
    public Rendering2DTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(Rendering2DTest.class);
        return suite;
    }

    private Style createTestStyle() throws IllegalFilterException
    {
        StyleFactory sFac = StyleFactory.createStyleFactory();
        //The following is complex, and should be built from
        //an SLD document and not by hand
        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());
        
        LineSymbolizer linesym = sFac.createLineSymbolizer();
        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(5)));
        LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);
        
        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        polysym.setStroke(sFac.getDefaultStroke());
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[]{rule});
        fts.setFeatureTypeName("polygonfeature");
        
        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
        fts2.setRules(new Rule[]{rule2});
        fts2.setFeatureTypeName("linefeature");
        
        Rule rule3 = sFac.createRule();
        rule3.setSymbolizers(new Symbolizer[]{pointsym});
        FeatureTypeStyle fts3 = sFac.createFeatureTypeStyle();
        fts3.setRules(new Rule[]{rule3});
        fts3.setFeatureTypeName("pointfeature");
        
        
        Rule rule4 = sFac.createRule();
        rule4.setSymbolizers(new Symbolizer[]{polysym, linesym});
        FeatureTypeStyle fts4 = sFac.createFeatureTypeStyle();
        fts4.setRules(new Rule[]{rule4});
        fts4.setFeatureTypeName("collFeature");
        
        Rule rule5 = sFac.createRule();
        rule5.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle fts5 = sFac.createFeatureTypeStyle();
        fts5.setRules(new Rule[]{rule5});
        fts5.setFeatureTypeName("ringFeature");
        
        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts, fts2, fts3, fts4, fts5});
        
        return style;
    }

    private FeatureCollection createTestFeatureCollection() throws Exception
    {
        // Request extent
        Envelope ex = new Envelope(5, 15, 5, 15);
        
        AttributeType[] types = new AttributeType[2];
        
        GeometryFactory geomFac = new GeometryFactory();
        
        LineString line = makeSampleLineString(geomFac);
        types[0] = AttributeTypeFactory.newAttributeType("centerline", line.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType lineType = FeatureTypeFactory.newFeatureType(types, "linefeature");
        Feature lineFeature = lineType.create(new Object[]{line, "centerline"});
        
        Polygon polygon = makeSamplePolygon(geomFac);
        
        types[0] = AttributeTypeFactory.newAttributeType("edge", polygon.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType polygonType = FeatureTypeFactory.newFeatureType(types,"polygonfeature");

        Feature polygonFeature = polygonType.create(new Object[]{polygon, "edge"});
        
        Point point = makeSamplePoint(geomFac);
        types[0] = AttributeTypeFactory.newAttributeType("centre", point.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType pointType = FeatureTypeFactory.newFeatureType(types,"pointfeature");
        
        Feature pointFeature = pointType.create(new Object[]{point, "centre"});
        
        LinearRing ring = makeSampleLinearRing(geomFac);
        types[0] = AttributeTypeFactory.newAttributeType("centerline", line.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType lrType = FeatureTypeFactory.newFeatureType(types,"ringfeature");
        Feature ringFeature = lrType.create(new Object[]{ring, "centerline"});
        
        GeometryCollection coll = makeSampleGeometryCollection(geomFac);
        types[0] = AttributeTypeFactory.newAttributeType("collection", coll.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType collType = FeatureTypeFactory.newFeatureType(types, "collfeature");
        Feature collFeature = collType.create(new Object[]{coll, "collection"});
                
        MemoryDataSource datasource = new MemoryDataSource();
        datasource.addFeature(lineFeature);
        datasource.addFeature(polygonFeature);
        datasource.addFeature(pointFeature);
        datasource.addFeature(ringFeature);
        datasource.addFeature(collFeature);
        
        FeatureCollection ft = datasource.getFeatures(Query.ALL);
        return ft;
    }
    
    public void testSimpleRender()throws Exception {
        //same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
    	
    	FeatureCollection ft = createTestFeatureCollection();
        Style style = createTestStyle();
        
		MapContext map = new DefaultMapContext();
        map.addLayer(ft,style);
        LiteRenderer renderer = new LiteRenderer(map);
        showRender(renderer, 1000);
    }
    
    /**
     * Tests the layer definition query behavior as implemented by LiteRenderer.
     * <p>
     * This method relies on the features created on createTestFeatureCollection()  
     * </p>
     * @throws Exception
     */
    public void testDefinitionQuery()throws Exception {
    	System.err.println("starting definition query test");
    	final FeatureCollection ft = createTestDefQueryFeatureCollection();
        final Style style = createDefQueryTestStyle();
        FeatureResults results;
        Envelope envelope = ft.getBounds();
        
        //we'll use this as the definition query for the layer
        Query layerQuery;

		MapLayer layer = new DefaultMapLayer(ft, style);
		MapContext map = new DefaultMapContext(new MapLayer[]{layer});
		map.setAreaOfInterest(envelope);
        LiteRenderer renderer = new LiteRenderer(map);
        renderer.setOptimizedDataLoadingEnabled(true);

        //this is the reader that LiteRenderer obtains after applying
        //the mixed filter to a given layer.
        FeatureReader reader;
        Filter filter = Filter.NONE;
        FilterFactory ffac = FilterFactory.createFilterFactory();
        
        //test maxFeatures, render just the first 2 features
        layerQuery = new DefaultQuery("querytest", filter, 2, null, "handle");
        layer.setQuery(layerQuery);

        results = renderer.queryLayer(layer, envelope);
        assertEquals(2, results.getCount());        
        //just the 3 geometric atts should get be loaded
        assertEquals(3, results.getSchema().getAttributeCount());

        showRender(renderer, 1000);

        //test attribute based filter
        FeatureType schema = ft.features().next().getFeatureType();
        filter = ffac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        ((CompareFilter)filter).addLeftValue(ffac.createAttributeExpression(schema, "id"));
        ((CompareFilter)filter).addRightValue(ffac.createLiteralExpression("ft1"));
        
        //note we include the "id" field in the layer query. Bad practice, since it goes against
        //the performance gain of renderer.setOptimizedDataLoadingEnabled(true), 
        //but we should test it anyway
        layerQuery = new DefaultQuery("querytest", filter, Integer.MAX_VALUE, new String[]{"id"}, "handle");
        layer.setQuery(layerQuery);

        results = renderer.queryLayer(layer, envelope);
        assertEquals(1, results.getCount());        
        //the 4 atts should be loaded since the definition query includes "id"
        assertEquals(4, results.getSchema().getAttributeCount());
        //we can check this since we explicitly requested the "id" attribute. If we not,
        //it would be not loaded
        String val = (String)results.reader().next().getAttribute("id");
        assertEquals("ft1", val);
        
        showRender(renderer, 1000);

        //try a bbox filter as definition query for the layer
        filter = null;
        GeometryFilter gfilter;
        //contains the first 2 features
        Envelope env = new Envelope(20, 130, 20, 130);
        gfilter = ffac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        gfilter.addLeftGeometry(ffac.createAttributeExpression(schema, "point"));
        gfilter.addRightGeometry(ffac.createBBoxExpression(env));
        filter = gfilter;

        gfilter = ffac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        gfilter.addLeftGeometry(ffac.createAttributeExpression(schema, "line"));
        gfilter.addRightGeometry(ffac.createBBoxExpression(env));
        filter = filter.or(gfilter);

        gfilter = ffac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        gfilter.addLeftGeometry(ffac.createAttributeExpression(schema, "polygon"));
        gfilter.addRightGeometry(ffac.createBBoxExpression(env));
        filter = filter.or(gfilter);
        
        System.err.println("trying with filter: " + filter);

        layerQuery = new DefaultQuery("querytest", filter, Integer.MAX_VALUE, null, "handle");
        layer.setQuery(layerQuery);

        results = renderer.queryLayer(layer, envelope);
        assertEquals(2, results.getCount());        
        //the 4 atts should be loaded since the definition query includes "id"
        assertEquals(3, results.getSchema().getAttributeCount());
        
        showRender(renderer, 1000);
    }
    
    private void showRender(LiteRenderer renderer, long timeOut)
    throws InterruptedException
    {
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        frame.setSize(300,300);
        frame.setVisible(true);
        renderer.paint((Graphics2D) p.getGraphics(),p.getBounds(), new AffineTransform());
        int w = 300, h = 600;
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0,0,w,h);
        renderer.paint((Graphics2D) g,new Rectangle(0,0,w,h), new AffineTransform());
        
        Thread.sleep(timeOut);
        frame.dispose();
    }
    
     public void testPixelToWorld()throws Exception {
        //same as the datasource test, load in some features into a table
        //System.err.println("starting rendering2DTest");
        // Request extent
        Envelope ex = new Envelope(0, 10, 0, 10);
        
        
        LiteRenderer renderer = new LiteRenderer();
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        frame.setSize(300,300);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(),p.getBounds());
       
        Coordinate c = renderer.pixelToWorld(150,150,ex);
        LOGGER.info("X Coordinate is " + c.x + " expected is 5 +/- 1.0" );
        LOGGER.info("Y Coordinate is " + c.y + " expected is 5 +/- 1.0" );
        assertEquals(5d, c.x, 1.0);
        assertEquals(5d, c.y, 1.0);
       
        frame.dispose();
        
    }
     
    private FeatureCollection createTestDefQueryFeatureCollection()
    throws Exception
    {
        MemoryDataSource datasource = new MemoryDataSource();
    	AttributeType []types = new AttributeType[4];
    	
    	types[0] = AttributeTypeFactory.newAttributeType("id", String.class);
    	types[1] = AttributeTypeFactory.newAttributeType("point", Point.class);
    	types[2] = AttributeTypeFactory.newAttributeType("line", LineString.class);
    	types[3] = AttributeTypeFactory.newAttributeType("polygon", Polygon.class);
    	
    	FeatureType type = FeatureTypeFactory.newFeatureType(types, "querytest");

    	GeometryFactory gf = new GeometryFactory();
    	Feature f;
    	LineString l;
    	Polygon p;
    	
    	l = line(gf, new int[] { 20, 20, 100, 20, 100, 100 });
    	p = (Polygon)l.convexHull();
        f = type.create(new Object[] {"ft1", point(gf, 20, 20), l, p},"test.1");
        datasource.addFeature(f);
        
        l = line(gf, new int[] { 130, 130, 110, 110, 110, 130, 30, 130 });
    	p = (Polygon)l.convexHull();
        f = type.create(new Object[] {"ft2", point(gf, 130, 130), l, p},"test.2");
        datasource.addFeature(f);

        l = line(gf, new int[] { 150, 150, 190, 140, 190, 190 });
    	p = (Polygon)l.convexHull();
        f = type.create(new Object[] {"ft3", point(gf, 150, 150), l, p},"test.3");
        datasource.addFeature(f);


        FeatureCollection col = datasource.getFeatures();
    	return col;
    }
    
    private Style createDefQueryTestStyle() throws IllegalFilterException
    {
        StyleFactory sFac = StyleFactory.createStyleFactory();

        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());        
        pointsym.setGeometryPropertyName("point");
        
        Rule rulep = sFac.createRule();
        rulep.setSymbolizers(new Symbolizer[]{pointsym});
        FeatureTypeStyle ftsP = sFac.createFeatureTypeStyle();
        ftsP.setRules(new Rule[]{rulep});
        ftsP.setFeatureTypeName("querytest");
        
        LineSymbolizer linesym = sFac.createLineSymbolizer();
        linesym.setGeometryPropertyName("line");

        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(3)));
        LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);
        
        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle ftsL = sFac.createFeatureTypeStyle();
        ftsL.setRules(new Rule[]{rule2});
        ftsL.setFeatureTypeName("querytest");
        
        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        polysym.setGeometryPropertyName("polygon");
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        polysym.setStroke(sFac.getDefaultStroke());
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        FeatureTypeStyle ftsPoly = sFac.createFeatureTypeStyle(new Rule[]{rule});
        //ftsPoly.setRules(new Rule[]{rule});
        ftsPoly.setFeatureTypeName("querytest");
        
        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{ftsPoly, ftsL, ftsP});
        
        return style;
    }

    public LineString line(final GeometryFactory gf, int[] xy) {
        Coordinate[] coords = new Coordinate[xy.length / 2];

        for (int i = 0; i < xy.length; i += 2) {
            coords[i / 2] = new Coordinate(xy[i], xy[i + 1]);
        }

        return gf.createLineString(coords);
    }

    public Point point(final GeometryFactory gf, int x, int y) {
        Coordinate coord = new Coordinate(x, y);
       return gf.createPoint(coord);
    }

    private Point makeSamplePoint(final GeometryFactory geomFac) {
        Coordinate c = new Coordinate(14.0d,14.0d);
        Point point = geomFac.createPoint(c);
        return point;
    }
    
    private LineString makeSampleLineString(final GeometryFactory geomFac) {
        Coordinate[] linestringCoordinates = new Coordinate[7];
        linestringCoordinates[0] = new Coordinate(5.0d,5.0d);
        linestringCoordinates[1] = new Coordinate(6.0d,5.0d);
        linestringCoordinates[2] = new Coordinate(6.0d,6.0d);
        linestringCoordinates[3] = new Coordinate(7.0d,6.0d);
        linestringCoordinates[4] = new Coordinate(7.0d,7.0d);
        linestringCoordinates[5] = new Coordinate(8.0d,7.0d);
        linestringCoordinates[6] = new Coordinate(8.0d,8.0d);
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
    
    private Polygon makeSamplePolygon(final GeometryFactory geomFac) {
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(7,7);
        polygonCoordinates[1] = new Coordinate(6,9);
        polygonCoordinates[2] = new Coordinate(6,11);
        polygonCoordinates[3] = new Coordinate(7,12);
        polygonCoordinates[4] = new Coordinate(9,11);
        polygonCoordinates[5] = new Coordinate(11,12);
        polygonCoordinates[6] = new Coordinate(13,11);
        polygonCoordinates[7] = new Coordinate(13,9);
        polygonCoordinates[8] = new Coordinate(11,7);
        polygonCoordinates[9] = new Coordinate(7,7);
        try{
            LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
            Polygon polyg = geomFac.createPolygon(ring,null);
            return polyg;
        }
        catch(TopologyException te){
            fail("Error creating sample polygon for testing "+te);
        }
        return null;
    }
    
    
    private GeometryCollection makeSampleGeometryCollection(final GeometryFactory geomFac) {
        try{
            Geometry polyg = buildShiftedGeometry(makeSamplePolygon(geomFac), 50, 50);
            Geometry lineString = buildShiftedGeometry(makeSampleLineString(geomFac), 50, 50);
            return geomFac.createGeometryCollection(new Geometry[] {polyg, lineString});
        }
        catch(TopologyException te){
            fail("Error creating sample polygon for testing "+te);
        }
        return null;
    }
    
    
    private LinearRing makeSampleLinearRing(final GeometryFactory geomFac) {
        try{
            Polygon polyg = (Polygon) buildShiftedGeometry(makeSamplePolygon(geomFac), 0, 100);
            return (LinearRing) polyg.getExteriorRing();
        }
        catch(TopologyException te){
            fail("Error creating sample polygon for testing "+te);
        }
        return null;
    }
    
    
    private Geometry buildShiftedGeometry(Geometry g, double shiftX, double shiftY) {
        Geometry clone = (Geometry) g.clone();
        Coordinate[] coords = clone.getCoordinates();
        for(int i = 0; i < coords.length; i++) {
            Coordinate coord = coords[i];
            coord.x += shiftX;
            coord.y += shiftY;
        }
        
        return clone;
    }
    
    
}
