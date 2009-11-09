/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.process.ProcessFactory;
import org.geotools.process.impl.util.Utilities;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

/**
 * A GetCoverage process functionality which allow to execute a getCoverage request
 * and store the returned coverage on an output file.
 *
 */
public class GetCoverageProcess extends AbstractProcess{

	private final static Logger LOGGER = Logging
	.getLogger("org.geotools.process.impl");
	
    protected GetCoverageProcess(ProcessFactory factory) {
        super(factory);
    }

    /**
     * Execute the getCoverage process.
     */
    public Map<String, Object> execute(Map<String, Object> input,
            ProgressListener monitor) {
        
        try {
            final URL url = new URL((String)input.get("input_data"));
            final String uid = (String)input.get(Utilities.GS_UID);
            final String pwd = (String)input.get(Utilities.GS_PWD);
            final String outFolder = (String)input.get(Utilities.OUTPUT_DIR);
            final String outputFile = getCoverage(url, outFolder, uid, pwd);
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("result",outputFile);
            return result;
        } catch (MalformedURLException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Perform a GeoServer's getCoverage request and return the path of an output file 
     * (usually a GeoTIFF, depending on the coverage request) containing the requested coverage. 
     * 
     * @param geoserverURL 
     * 			The whole geoserver getCoverage request, as an instance:
     * 			{@code http://localhost:8080/geoserver/ows?service=WCS&request=GetCoverage&Format=GeoTIFF&version=1.0.0&" +
     *       		"coverage=it.geosolutions:testlayer&crs=EPSG:32721&bbox=-22856.625,2412871.75,812143.375,3385771.75&width=800&height=523}
     * @param outFolder
     * 			The folder where to store the output file.
     * @param gsUser
     * 			The geoserver user authentication.
     * @param gsPassword
     * 			The geoserver password authentication.
     * @return the path of the output file containing the requested coverage. 
     */
    private String getCoverage(final URL geoserverURL, final String outFolder, final String gsUser, final String gsPassword) {
        
        HttpURLConnection con=null;
        String res = "";
    try {
        con = (HttpURLConnection) geoserverURL.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);                   
        con.setRequestMethod("GET");

        //Prepare for authentication
	    final String login = gsUser;
	    final String password = gsPassword;
	
	    if ((login != null) && (login.trim().length() > 0)) {
	        Authenticator.setDefault(new Authenticator() {
	                protected PasswordAuthentication getPasswordAuthentication() {
	                    return new PasswordAuthentication(login, password.toCharArray());
	                }
	            });
	    }
    
        final int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
        	final String gsUrl = geoserverURL.toString();
        	final int coverageIndex = gsUrl.indexOf("&coverage=");
        	final int endOfCoverageIndex = gsUrl.indexOf("&",coverageIndex+1);
        	
        	//Throw away the namespace
        	String coverageName = gsUrl.substring(coverageIndex+10, endOfCoverageIndex);
        	coverageName = coverageName.substring(coverageName.indexOf(":")+1);
        	
            final InputStream inputStream = con.getInputStream();
            
            //Write the output file
            final SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyyMMdd'T'HHmmssSSS");
			final StringBuilder builderDestinationName= new StringBuilder(coverageName);
			builderDestinationName.append("_").append(dateFormat.format(new Date())).append(".tif");
            
            final String fileName = builderDestinationName.toString();
            final File outFile = new File(new StringBuilder(outFolder).append(Utilities.FILE_SEPARATOR)
            		.append(fileName).toString());
            final FileOutputStream outputStream = new FileOutputStream(outFile);
            Utilities.copyStream(inputStream, outputStream, true, true);
            res =  outFile.getAbsolutePath();
            
        } else {
        	if (LOGGER.isLoggable(Level.SEVERE))
            	LOGGER.severe("Unsuccessfull getCoverage request. The server responded with " + responseCode + " code");
        }
    	} catch (IOException e) {
//      if (LOGGER.isLoggable(Level.SEVERE))
//              LOGGER.severe("HTTP ERROR: " + e.getLocalizedMessage());
     
        }
    	return res;
    }
}
