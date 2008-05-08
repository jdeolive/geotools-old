package org.geotools.filter.pojo;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;

public class JDOQLTest extends TestCase {

	
	private FilterFactory ff;

	protected void setUp() throws Exception {
		ff = CommonFactoryFinder.getFilterFactory( null );
		
		super.setUp();
	}
	
	public void testSimpleQuery(){
		FilterVisitor munch = new JDOQLEncoder(3005);

		Filter filter = ff.and(ff.equals(ff.property("foo"), ff.literal(12)), ff.equals(ff.property("bar"), ff.literal(42)));
		StringBuffer query = new StringBuffer();
		filter.accept(munch, query);
		assertEquals("foo==12&&bar==42", query.toString());
		
		filter = ff.bbox("geom", 0.0, 0.0, 10.0, 10.0, "EPSG:3005");
		query = new StringBuffer();
		filter.accept(munch, query);
		assertEquals("Spatial.bboxIntersects(geom, Spatial.geomFromText('POLYGON((0.0 10.0, 10.0 10.0, 10.0 0.0, 0.0 0.0, 0.0 10.0))', 3005))", query.toString());		
	}
}
