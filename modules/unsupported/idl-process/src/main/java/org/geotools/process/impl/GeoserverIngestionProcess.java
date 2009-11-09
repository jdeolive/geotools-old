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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.process.ProcessFactory;
import org.geotools.process.impl.util.Utilities;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

/**
 * A GeoserverIngestion Process functionality which allow to ingest a dataset 
 * in geoserver using REST.  
 */
public class GeoserverIngestionProcess extends AbstractProcess {

	private final static Logger LOGGER = Logging
	.getLogger("org.geotools.process.impl");
	
    protected GeoserverIngestionProcess(ProcessFactory factory) {
        super(factory);
    }

    /**
     * Execute the geoserverIngestion process.
     */
    public Map<String, Object> execute(Map<String, Object> input,
            ProgressListener monitor) {
        final Map<String, Object> result = new HashMap<String, Object>(1);

        final String namespace = "it.geosolutions";
        final String fileName = (String) input.get("input_data");

        final File file = new File(fileName);
        String dataName = file.getName();
        final int idx = dataName.lastIndexOf(".");
        dataName = (idx > 0 ? dataName.substring(0, idx) : dataName);

        final String geoserverBaseUrl = (String) input.get(Utilities.GS_URL);
        final String geoserverUid = (String) input.get(Utilities.GS_UID);
        final String geoserverPwd = (String) input.get(Utilities.GS_PWD);

        URL geoserverREST_URL;
        try {
            geoserverREST_URL = new URL(new StringBuilder(geoserverBaseUrl)
                    .append("/rest/workspaces/").append(namespace).append(
                            "/datastores/").append(dataName).append("/file.")
                    .append("shp").append("?").append("style=polygon").toString());
        } catch (MalformedURLException e) {
        	if (LOGGER.isLoggable(Level.SEVERE))
            	LOGGER.severe("No more wrappers available to execute the process");
        	return Collections.emptyMap();
        }
        final String response = putBinaryFileTo(geoserverREST_URL, file,
                geoserverUid, geoserverPwd);
        if (response!=null && response.trim().length()>0){
        	String layerName = extractLayerNameFromResponse(response);
        	result.put("result", layerName);
        }
        else{
        	if (LOGGER.isLoggable(Level.SEVERE))
            	LOGGER.severe("The rest POST request has not produced a valid response");
        	return Collections.emptyMap();
        }
        	
        	
        return result;
    }

    /**
     * Extract the layerName from the response.
     * @param response A String response returned by a geoserver REST put request. 
     * @return the layer name found in the response.
     */
    private String extractLayerNameFromResponse(final String response) {
    	//TODO: 
    	//Improve this using better XML objects parsing
        final String nameTag = "<name>";
        final String nameEndTag = "</name>";
        final String nameSpaceTag = "<namespace>";
        final int firstIndexOfName = response.indexOf(nameTag);
        final int lastIndexOfName = response.indexOf(nameEndTag,
                firstIndexOfName);
        final String name = response.substring(firstIndexOfName
                + nameTag.length(), lastIndexOfName);
        int firstIndexOfNameSpace = response.indexOf(nameSpaceTag);
        firstIndexOfNameSpace = response
                .indexOf(nameTag, firstIndexOfNameSpace);

        int lastIndexOfNameSpace = response.indexOf(nameEndTag,
                firstIndexOfNameSpace);
        final String nameSpace = response.substring(firstIndexOfNameSpace
                + nameTag.length(), lastIndexOfNameSpace);
        return new StringBuilder(nameSpace).append(":").append(name).toString();
    }

    /**
     * 
     * @param geoserverREST_URL
     * @param file
     * @return
     */
    public static String putBinaryFileTo(final URL geoserverREST_URL,
            final File file, final String gsUser, final String gsPassword) {
        HttpURLConnection con = null;
        String res = "";
        try {
            con = (HttpURLConnection) geoserverREST_URL.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");
            if (file.getPath().endsWith(".zip"))
                con.setRequestProperty("CONTENT-TYPE", "application/zip");
        } catch (IOException e) {
             if (LOGGER.isLoggable(Level.SEVERE))
             LOGGER.severe("HTTP ERROR: " + e.getLocalizedMessage());
            return res;
        }

        final String login = gsUser;
        final String password = gsPassword;

        if ((login != null) && (login.trim().length() > 0)) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(login, password
                            .toCharArray());
                }
            });
        }

        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = con.getOutputStream();
            inputStream = new FileInputStream(file);
            Utilities.copyStream(inputStream, outputStream, true, true);
        } catch (IOException e) {
             if (LOGGER.isLoggable(Level.SEVERE))
             LOGGER.severe("HTTP ERROR: " + e.getLocalizedMessage());
            return res;
        } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (Exception e) {
                }

            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
        }

        try {
            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(con
                        .getInputStream());
                String response = Utilities.readIs(is);
                is.close();
                 if (LOGGER.isLoggable(Level.FINE))
                 LOGGER.fine("HTTP OK: " + response);
                res = response;
                return res;
            } else if (responseCode == HttpURLConnection.HTTP_CREATED) {
                InputStreamReader is = new InputStreamReader(con
                        .getInputStream());
                String response = Utilities.readIs(is);
                is.close();
                if (LOGGER.isLoggable(Level.FINE))
                	LOGGER.log(Level.FINE,"HTTP CREATED: " + response);
                res = response;
            } else {
                 if (LOGGER.isLoggable(Level.INFO))
                 LOGGER.info("HTTP ERROR: " + con.getResponseMessage());
            }
        } catch (IOException e) {
             if (LOGGER.isLoggable(Level.SEVERE))
            	 LOGGER.severe("HTTP ERROR: " + e.getLocalizedMessage());
        }
        return res;
    }
}
