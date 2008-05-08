package org.geotools.data.ogr;

import java.io.IOException;
import java.net.URL;

import org.geotools.TestData;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.referencing.CRS;

public class OGRPeformanceTest extends TestCaseSupport {
	final static String STATE_POP = "shapes/statepop.shp";

	public OGRPeformanceTest(String name) throws IOException {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		AttributeTypeFactory.newAttributeType("Gimbo", String.class, true, 200, "AAA");
		CRS.decode("EPSG:4326");
	}

	public void testShapefilePerformance() throws Exception {
		URL url = TestData.url(STATE_POP);
		ShapefileDataStore sds = new ShapefileDataStore(url);
		long start = System.currentTimeMillis();
		sds.getSchema();
		long end = System.currentTimeMillis();
		System.out.println("SDS schema: " + (end - start) / 1000.0);
		

		DefaultQuery query = new DefaultQuery(sds.getTypeNames()[0]);
		start = System.currentTimeMillis();
		FeatureReader sfr = sds.getFeatureReader(query, Transaction.AUTO_COMMIT);
		while (sfr.hasNext())
			sfr.next();
		sfr.close();
		end = System.currentTimeMillis();
		System.out.println("SDS: " + (end - start) / 1000.0);

		System.out.println("Attribute count: " + sds.getSchema().getAttributeCount());
		System.out.println("Feature count: "
				+ sds.getFeatureSource(sds.getSchema().getTypeName()).getCount(Query.ALL));
	}
	

	public void testOGRShapePerformance() throws Exception {
		OGRDataStore ods = new OGRDataStore(getAbsolutePath(STATE_POP), null, null);
		long start = System.currentTimeMillis();
		ods.getSchema(ods.getTypeNames()[0]);
		long end = System.currentTimeMillis();
		System.out.println("OGR schema: " + (end - start) / 1000.0);
		DefaultQuery query = new DefaultQuery(ods.getTypeNames()[0]);
		start = System.currentTimeMillis();
		FeatureReader ofr = ods.getFeatureReader(query, Transaction.AUTO_COMMIT);
		while (ofr.hasNext())
			ofr.next();
		ofr.close();
		end = System.currentTimeMillis();
		System.out.println("OGR: " + (end - start) / 1000.0);
	}
	
	
}
