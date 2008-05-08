package org.geotools.demo.xml;

import org.geotools.filter.FilterTransformer;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.gml.producer.GeometryTransformer;
import org.opengis.filter.Filter;

public class TransformExample {

	public static void main( String args[]) throws Exception {
		Filter filter = CQL.toFilter("name = 'fred'");
		System.out.println( filter );
		
		FilterTransformer transform = new FilterTransformer();
		transform.setIndentation(2);
		
		String xml = transform.transform( filter );
		System.out.println( xml );
	}
}

