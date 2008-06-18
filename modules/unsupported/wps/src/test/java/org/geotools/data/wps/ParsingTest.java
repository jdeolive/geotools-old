/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.data.wps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.measure.unit.Unit;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.ProcessDescriptionType;
import net.opengis.wps.ProcessDescriptionsType;
import net.opengis.wps.WpsPackage;
import net.opengis.wps.impl.UOMsTypeImpl;

import org.eclipse.emf.ecore.EObject;
import org.geotools.data.ows.ProcessDescription;
import org.geotools.wps.WPSConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.EMFUtils;
import org.geotools.xml.Parser;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class ParsingTest extends TestCase {

	public void testDescribeProcessParsing() throws IOException {
		
		Object object;
		BufferedReader in = null;
		
	    try {
	        Configuration config = new WPSConfiguration();
	    	Parser parser = new Parser(config);

			try {
				URL url = new URL("http://schemas.opengis.net/wps/1.0.0/examples/40_wpsDescribeProcess_response.xml");
				in = new BufferedReader(new InputStreamReader(url.openStream())); 
				object =  parser.parse(in);
			} catch (SAXException e) {
				throw (IOException) new IOException().initCause(e);
			} catch (ParserConfigurationException e) {
				throw (IOException) new IOException().initCause(e);
			}
			catch (MalformedURLException e ) {
				throw (MalformedURLException) new MalformedURLException().initCause(e);
			}
	        
			ProcessDescriptionsType processDesc = (ProcessDescriptionsType) object;
			assertNotNull(processDesc);
	    } finally {
	    	in.close();
	    }
	}
	
//	public void testUOMsList() {
//		UOMsTypeImpl uoMsType = new UOMsTypeImpl();
//		Unit newValue = Unit.valueOf("m");
//		EMFUtils.add(uoMsType, "UOM", newValue);
//		//uoMsType.eSet(WpsPackage.UO_MS_TYPE__UOM, newValue);
//	}
	
}
