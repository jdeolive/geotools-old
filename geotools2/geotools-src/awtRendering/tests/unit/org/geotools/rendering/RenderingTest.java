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

    public void testSimpleRender()throws java.io.IOException {
        //same as the datasource test, load in some features into a table

        // Request extent
        EnvelopeExtent ex = new EnvelopeExtent(0, 360, 0, 180.0);
        Feature feature = new DefaultFeature();
        GeometryFactory geomFac = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[7];
        coordinates[0] = new Coordinate(0.0d,0.0d);
        coordinates[1] = new Coordinate(1.0d,0.0d);
        coordinates[2] = new Coordinate(1.0d,1.0d);
        coordinates[3] = new Coordinate(2.0d,1.0d);
        coordinates[4] = new Coordinate(2.0d,2.0d);
        coordinates[5] = new Coordinate(3.0d,2.0d);
        coordinates[6] = new Coordinate(3.0d,3.0d);
        LineString line = geomFac.createLineString(coordinates);
        
        feature.setAttributes(new Object[]{line});
        MemoryDataSource datasource = new MemoryDataSource();
        datasource.addFeature(feature);
        
        FeatureTable ft = new DefaultFeatureTable(datasource);
        
        org.geotools.map.Map map = new DefaultMap();
        
        //The following is complex, and should be built from
        //an SLD document and not by hand
        LineSymbolizer linesym = new DefaultLineSymbolizer();
        DefaultRule rule = new DefaultRule();
        rule.setSymbolizers(new Symbolizer[]{linesym});
        DefaultFeatureTypeStyle fts = new DefaultFeatureTypeStyle();
        fts.setRules(new Rule[]{rule});
        
        DefaultStyle style = new DefaultStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
        
        map.addFeatureTable(ft,style);
        Renderer renderer = new org.geotools.renderer.AWTRenderer();
        map.render(renderer,ex);//and finaly try and draw it!
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
