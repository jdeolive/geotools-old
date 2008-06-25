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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import net.opengis.wps.ComplexDataCombinationsType;
import net.opengis.wps.ComplexDataDescriptionType;
import net.opengis.wps.DataType;
import net.opengis.wps.InputDescriptionType;
import net.opengis.wps.ProcessBriefType;
import net.opengis.wps.ProcessDescriptionType;
import net.opengis.wps.ProcessDescriptionsType;
import net.opengis.wps.ProcessOfferingsType;
import net.opengis.wps.SupportedComplexDataInputType;
import net.opengis.wps.WPSCapabilitiesType;

import org.eclipse.emf.common.util.EList;
import org.geotools.data.wps.request.DescribeProcessRequest;
import org.geotools.data.wps.request.ExecuteProcessRequest;
import org.geotools.data.wps.response.DescribeProcessResponse;
import org.geotools.data.wps.response.ExecuteProcessResponse;
import org.geotools.ows.ServiceException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;

public class OnlineWPSTest extends TestCase {
	
	private WebProcessingService wps;
	private URL url;
	
	public void setUp() throws ServiceException, IOException {
		
		//url = new URL("http://192.168.50.77:8080/geoserver/ows?service=WPS&request=GetCapabilities&language=ko");
		// try the 52 North test server
		url = new URL("http://geoserver.itc.nl:8080/wps100/WebProcessingService?service=WPS&request=GetCapabilities&language=ko");
		wps = new WebProcessingService(url);
	}
	
	public void testGetCaps() throws ServiceException, IOException {

		WPSCapabilitiesType capabilities = wps.getCapabilities();
		assertNotNull("capabilities shouldn't be null", capabilities);
		
		ProcessOfferingsType processOfferings = capabilities.getProcessOfferings();
		assertNotNull("process offerings shouldn't be null", processOfferings);
		EList processes = processOfferings.getProcess();
		for (int i=0; i<processes.size(); i++) {
			ProcessBriefType process = (ProcessBriefType) processes.get(i);
			//System.out.println(process.getTitle());
			assertNotNull("process ["+ process + " shouldn't be null", process);
		}

	}
	
//	public void testDescribeProcess() throws ServiceException, IOException {
//		
//		WPSCapabilitiesType capabilities = wps.getCapabilities();
//		
//		// get the first process and describe it
//		ProcessOfferingsType processOfferings = capabilities.getProcessOfferings();
//		EList processes = processOfferings.getProcess();
//		ProcessBriefType process = (ProcessBriefType) processes.get(0);
//		
//		DescribeProcessRequest request = wps.createDescribeProcessRequest();
//		request.setIdentifier(process.getIdentifier().getValue());
//		//System.out.println(request.getFinalURL());
//		DescribeProcessResponse response = wps.issueRequest(request);
//		//System.out.println(response);
//		assertNotNull(response);
//		
//	}
	
	public void testExecuteProcess() throws ServiceException, IOException, ParseException {
		
		WPSCapabilitiesType capabilities = wps.getCapabilities();
		
		// get the first process and execute it
		ProcessOfferingsType processOfferings = capabilities.getProcessOfferings();
		EList processes = processOfferings.getProcess();
		//ProcessBriefType process = (ProcessBriefType) processes.get(0);

		// does the server contain the specific process I want
		String iden = "org.n52.wps.server.algorithm.collapse.SimplePolygon2PointCollapse";
		boolean found = false;
		Iterator iterator = processes.iterator();
		while (iterator.hasNext()) {
			ProcessBriefType process = (ProcessBriefType) iterator.next();
			if (process.getIdentifier().getValue().equalsIgnoreCase(iden)) {
				found =true;
				break;
			}
		}
		
		// exit test if my process doesn't exist on server
		if (!found) {
			return;
		}
		
		// do a full describeprocess on my process
		// http://geoserver.itc.nl:8080/wps100/WebProcessingService?REQUEST=DescribeProcess&IDENTIFIER=org.n52.wps.server.algorithm.collapse.SimplePolygon2PointCollapse&VERSION=1.0.0&SERVICE=WPS
		DescribeProcessRequest descRequest = wps.createDescribeProcessRequest();
		descRequest.setIdentifier(iden);
		DescribeProcessResponse descResponse = wps.issueRequest(descRequest);
		
		// based on the describeprocess, setup the execute
		ProcessDescriptionsType processDesc = descResponse.getProcessDesc();
		ExecuteProcessRequest exeRequest = wps.createExecuteProcessRequest();
		exeRequest.setIdentifier(iden);
		
		// this process takes 1 input, a building polygon to collapse.
		ProcessDescriptionType pdt = (ProcessDescriptionType) processDesc.getProcessDescription().get(0);
		InputDescriptionType idt = (InputDescriptionType) pdt.getDataInputs().getInput().get(0);
		
		// create a polygon for the input
        WKTReader reader = new WKTReader( new GeometryFactory() );
        Geometry geom1 = (Polygon) reader.read("POLYGON((20 10, 30 0, 40 10, 30 20, 20 10))");

        // create and set the input on the exe request
		DataType input = WPSUtils.createInput(geom1, idt);
		exeRequest.addInput(idt.getIdentifier().getValue(), input);
		
		// send the request
		ExecuteProcessResponse response = wps.issueRequest(exeRequest);
		//System.out.println(response);
		//System.out.println(response.getExecuteResponse());
		assertNotNull(response);
		
	}	
	

}
