package org.geotools.gml;

/*
 * ProducerTest.java
 * JUnit based test
 *
 */
import org.geotools.data.*;
import org.geotools.feature.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.Point;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.geotools.gml.producer.*;

import junit.framework.*;

/**
 *
 * @author Chris Holmes, TOPP
 */
public class ProducerTest extends TestCase {
    
        
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gml");
    private Feature testFeature;

    private FeatureType schema;

       private FeatureFactory featureFactory;


    static int NTests = 7;
    
    FeatureCollection table = null;
    
    public ProducerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ProducerTest.class);
        return suite;
    }

    /**
     * This needs to be redone, for now it is just a demo
     * that will print some sample features.
     */
    public void testProducer() throws Exception {
	
    System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");

	LOGGER.info("testing producer" );
	    String dataFolder = System.getProperty("dataFolder");
            if(dataFolder==null){
                //then we are being run by maven
                dataFolder = System.getProperty("basedir");
                dataFolder+="/tests/unit/testData";
            }
            URL url = new URL("file:///"+dataFolder+"/testGML7Features.gml");
            System.out.println("Testing blorg to load "+url+" as Feature datasource");
            DataSource ds = new GMLDataSource(url);
            

            AttributeType[] atts = {  
	    AttributeTypeFactory.newAttributeType("geoid", Integer.class),
	    AttributeTypeFactory.newAttributeType("geom", Geometry.class),
	    AttributeTypeFactory.newAttributeType("name", String.class)};
	try {
	schema = FeatureTypeFactory.newFeatureType(atts,"rail");
	//schema = ((FeatureTypeFlat)schema).setNamespace("http://www.openplans.org/ch");
	} catch (SchemaException e) {
	    LOGGER.finer("problem with creating schema");
	}
	LOGGER.info("namespace is " + schema.getNamespace());
	featureFactory = schema;

 
	Coordinate[] points = { new Coordinate(15, 15),
				new Coordinate(15, 25),
				new Coordinate(25, 25),
				new Coordinate(25, 15),
				 new Coordinate(15, 15) };
	PrecisionModel precModel = new PrecisionModel();
	int srid = 2035;
	LinearRing shell = new LinearRing(points, precModel, srid);
	Polygon the_geom = new Polygon(shell, precModel, srid);

	Point point = new Point(new Coordinate(3, 35), precModel, srid);
	LineString line = new LineString(points, precModel, srid);

	Integer featureId = new Integer(32);
	String name = "inse<rt polygon";
	Object[] attributes = { featureId, point, name };
	Feature polygonFeature = null;
	Feature lineFeature = null;
	try{
	    
	testFeature = featureFactory.create(attributes, null);
	attributes[1] = line;
	lineFeature =  featureFactory.create(attributes, null);
	attributes[1] = the_geom;
	polygonFeature = featureFactory.create(attributes, null);
	    
	} catch (IllegalAttributeException ife) {
	    LOGGER.warning("problem in setup " + ife);
	}

	//table = ds.getFeatures();//new FeatureCollectionDefault();
	table = FeatureCollections.newCollection();
	table.add(testFeature);
	table.add(lineFeature);
	table.add(polygonFeature);
	    System.out.println("the feature collection is " + table + ", and "
			       + "the first feat is " + table.features().next());
	    
	    FeatureTransformer fr = new FeatureTransformer();
	    fr.setPrettyPrint(true);
	    fr.setDefaultNamespace("http://www.openplans.org/ch");
	    fr.transform(table, System.out);
    }
}
