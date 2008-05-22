package org.geotools.wps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class ExecuteTest extends TestCase {

	/*
	 * Try doing an execute request from the example xsd and parse it
	 */
	public void testExecute() throws IOException, SAXException, ParserConfigurationException {
		   URL url = new URL("http://schemas.opengis.net/wps/1.0.0/examples/51_wpsExecute_request_ResponseDocument.xml");
		   BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		   org.geotools.xml.Parser parser = new org.geotools.xml.Parser(new WPSConfiguration());
		   Object obj = parser.parse(in);
		   assertNotNull(obj);
	}
}
