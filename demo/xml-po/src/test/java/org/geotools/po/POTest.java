package org.geotools.po;

import java.io.InputStream;

import junit.framework.TestCase;

import org.geotools.po.bindings.POConfiguration;
import org.geotools.xml.Parser;

/**
 * Test case which tests the parsing of a sample instance document from 
 * the purchase order schema, po.xsd.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class POTest extends TestCase {

	public void test() throws Exception {
		
		//load the xml file
		InputStream input = getClass().getResourceAsStream( "po.xml" );
		
		//create the configuration
		POConfiguration configuration = new POConfiguration();
		
		//parse the instance document
		Parser parser = new Parser( configuration );
		PurchaseOrderType po = (PurchaseOrderType) parser.parse( input );
		
		assertNotNull( po );
	}
}
