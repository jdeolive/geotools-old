/*
 * StylingTest.java
 * JUnit based test
 *
 * Created on April 12, 2002, 1:18 PM
 */

package org.geotools.rendering;
import org.geotools.renderer.*;
import org.geotools.datasource.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.datasource.extents.*;
import org.geotools.featuretable.*;
import org.geotools.styling.*;
import org.geotools.map.*;
import java.util.*;
import junit.framework.*;
import java.awt.Frame;
import java.awt.Panel;


/**
 *
 * @author jamesm
 */
public class RenderingTest extends TestCase {
    
    public RenderingTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RenderingTest.class);
        return suite;
    }
    
    public void testSimpleRender()throws Exception {
        //same as the datasource test, load in some features into a table
        
        // Request extent
        EnvelopeExtent ex = new EnvelopeExtent(0, 15, 0, 15);
        Feature lineFeature = new DefaultFeature();
        
        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac);
        lineFeature.setAttributes(new Object[]{line});
        Feature polygonFeature = new DefaultFeature();
        
        polygonFeature.setAttributes(new Object[]{makeSamplePolygon(geomFac)});
        MemoryDataSource datasource = new MemoryDataSource();
        datasource.addFeature(lineFeature);
        datasource.addFeature(polygonFeature);
        
        FeatureTable ft = new DefaultFeatureTable(datasource);
        
        org.geotools.map.Map map = new DefaultMap();
        
        //The following is complex, and should be built from
        //an SLD document and not by hand
        LineSymbolizer linesym = new DefaultLineSymbolizer();
        DefaultPolygonSymbolizer polysym = new DefaultPolygonSymbolizer();
        DefaultFill myFill = new DefaultFill();
        myFill.setColor("#ff0000");
        polysym.setFill(myFill);
        DefaultRule rule = new DefaultRule();
        rule.setSymbolizers(new Symbolizer[]{linesym,polysym});
        DefaultFeatureTypeStyle fts = new DefaultFeatureTypeStyle();
        fts.setRules(new Rule[]{rule});
        
        DefaultStyle style = new DefaultStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
        
        map.addFeatureTable(ft,style);
        AWTRenderer renderer = new org.geotools.renderer.AWTRenderer();
        Frame frame = new Frame();
        Panel p = new Panel();
        frame.add(p);
        frame.setSize(300,300);
        frame.setVisible(true);
        renderer.setOutput(p);
        map.render(renderer,ex.getBounds());//and finaly try and draw it!
        Thread.sleep(5000);
    }
    
    private LineString makeSampleLineString(final GeometryFactory geomFac) {
        Coordinate[] linestringCoordinates = new Coordinate[7];
        linestringCoordinates[0] = new Coordinate(0.0d,0.0d);
        linestringCoordinates[1] = new Coordinate(1.0d,0.0d);
        linestringCoordinates[2] = new Coordinate(1.0d,1.0d);
        linestringCoordinates[3] = new Coordinate(2.0d,1.0d);
        linestringCoordinates[4] = new Coordinate(2.0d,2.0d);
        linestringCoordinates[5] = new Coordinate(3.0d,2.0d);
        linestringCoordinates[6] = new Coordinate(3.0d,3.0d);
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
    
    private com.vividsolutions.jts.geom.Polygon makeSamplePolygon(final GeometryFactory geomFac) {
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(2,2);
        polygonCoordinates[1] = new Coordinate(1,4);
        polygonCoordinates[2] = new Coordinate(1,6);
        polygonCoordinates[3] = new Coordinate(2,7);
        polygonCoordinates[4] = new Coordinate(4,6);
        polygonCoordinates[5] = new Coordinate(6,7);
        polygonCoordinates[6] = new Coordinate(8,6);
        polygonCoordinates[7] = new Coordinate(8,4);
        polygonCoordinates[8] = new Coordinate(6,2);
        polygonCoordinates[9] = new Coordinate(2,2);
        try{
        LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
        com.vividsolutions.jts.geom.Polygon polyg = geomFac.createPolygon(ring,null);
        return polyg;
        }
        catch(TopologyException te){
           fail("Error creating sample polygon for testing "+te);
        }
        return null;
    }
    
    class MemoryDataSource implements DataSource{
        
        private Vector features = new Vector();
        
        /** Stops this DataSource from loading
         */
        public void stopLoading() {
            //do nothing
        }
        
        /** Loads Feature rows for the given Extent from the datasource
         * @param ft featureTable to load features into
         * @param ex an extent defining which features to load - null means all features
         * @throws DataSourceException if anything goes wrong
         */
        public void importFeatures(FeatureTable ft, Extent ex) throws DataSourceException {
            for(int i=0;i<features.size();i++){
                Feature f = (Feature)features.elementAt(i);
                //if(ex.containsFeature(f)){
                ft.addFeature(f);
                //}
            }
        }
        
        /** Saves the given features to the datasource
         * @param ft feature table to get features from
         * @param ex extent to define which features to write - null means all
         * @throws DataSourceException if anything goes wrong or if exporting is not supported
         */
        public void exportFeatures(FeatureTable ft, Extent ex) throws DataSourceException {
            //do nothing
        }
        
        public void addFeature(Feature f){
            features.addElement(f);
        }
        
    }
    
    
}
