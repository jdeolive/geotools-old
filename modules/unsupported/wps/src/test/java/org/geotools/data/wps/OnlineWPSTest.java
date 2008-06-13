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

import net.opengis.wps.ProcessBriefType;
import net.opengis.wps.ProcessOfferingsType;
import net.opengis.wps.WPSCapabilitiesType;

import org.eclipse.emf.common.util.EList;
import org.geotools.data.wps.request.DescribeProcessRequest;
import org.geotools.data.wps.request.ExecuteProcessRequest;
import org.geotools.data.wps.response.DescribeProcessResponse;
import org.geotools.data.wps.response.ExecuteProcessResponse;
import org.geotools.ows.ServiceException;

import junit.framework.TestCase;

public class OnlineWPSTest extends TestCase {
	
	private WebProcessingService wps;
	private URL url;
	
	public void setUp() throws ServiceException, IOException {
		
		//url = new URL("http://192.168.50.77:8080/geoserver/wps?service=WPS&request=GetCapabilities&language=ko");
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
	
	public void testDescribeProcess() throws ServiceException, IOException {
		
		WPSCapabilitiesType capabilities = wps.getCapabilities();
		
		// get the first process and describe it
		ProcessOfferingsType processOfferings = capabilities.getProcessOfferings();
		EList processes = processOfferings.getProcess();
		ProcessBriefType process = (ProcessBriefType) processes.get(0);
		
		DescribeProcessRequest request = wps.createDescribeProcessRequest();
		request.setIdentifier(process.getIdentifier().getValue());
		//System.out.println(request.getFinalURL());
		DescribeProcessResponse response = wps.issueRequest(request);
		//System.out.println(response);
		assertNotNull(response);
		
	}
	
//	public void testExecuteProcess() throws ServiceException, IOException {
//		
//		WPSCapabilitiesType capabilities = wps.getCapabilities();
//		
//		// get the first process and execute it
//		ProcessOfferingsType processOfferings = capabilities.getProcessOfferings();
//		EList processes = processOfferings.getProcess();
//		ProcessBriefType process = (ProcessBriefType) processes.get(0);
//		
//		ExecuteProcessRequest request = wps.createExecuteProcessRequest();
//		request.setIdentifier(process.getIdentifier().getValue());
//		ExecuteProcessResponse response = wps.issueRequest(request);
//		//System.out.println(response);
//		assertNotNull(response);
//		
//	}	
	

}
