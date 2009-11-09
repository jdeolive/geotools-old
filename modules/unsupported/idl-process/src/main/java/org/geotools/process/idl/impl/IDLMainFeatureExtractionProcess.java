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
package org.geotools.process.idl.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.idl.IDLProcess;
import org.geotools.process.idl.IDLProcessFactory;
import org.geotools.process.impl.util.Utilities;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * Main IDL Feature Extraction Process implementation which, when invoked for the execution,
 * does the following operations:
 * 1) a getCoverage request.
 * 2) a feature extraction on the returned coverage.
 * 3) a geoserver ingestion of the produced feature.
 */
public class IDLMainFeatureExtractionProcess extends IDLProcess {

	private final static Logger LOGGER = Logging
	.getLogger("org.geotools.process.idl.impl");
	
	/**
	 * Base {@link IDLMainFeatureExtractionProcess} constructor.
	 */
    public IDLMainFeatureExtractionProcess(final IDLProcessFactory idlProcessFactory) {
        super(idlProcessFactory);
    }

    /**
     * Execute the whole Feature Extraction process.
     */
    public Map<String, Object> execute(final Map<String, Object> input,
            final ProgressListener monitor) {
        Map<String, Object> result;

        try {
        	// ////////////////////////////////////////////////////////////////
        	//
        	// 1) Execute a getCoverage request
        	// 
        	// ////////////////////////////////////////////////////////////////
	        final String filePath = getCoverage(input, monitor);
	        
	        // Check for the returned coverage 
	        if (filePath == null || filePath.trim().length() == 0)
	            return Collections.emptyMap();
	
	        // Retrieving the getCoverage request which is the input_data parameter 
	        final URL url = new URL((String)input.get("input_data"));
	        final String coverageRequest = url.toString().toLowerCase();
	        if (LOGGER.isLoggable(Level.INFO))
	        	LOGGER.info("GetCoverageRequest="+coverageRequest);
	        
	        // Actually, getCoverage request have parameters in this order:
	        // ...crs=EPSG:XXX&bbox=...
	        // Parsing the boundingBox and the coverage CRS
	        
	        final int bboxIndex = coverageRequest.indexOf("&bbox=");
	        final int crsIndex = coverageRequest.indexOf("&crs=");
	        final String epsgCode = coverageRequest.substring(crsIndex+5,bboxIndex);
	        final CoordinateReferenceSystem crs = CRS.decode(epsgCode,true);
	        final String wkt = crs.toWKT();
	        
	        // Updating the inputMap for the next execution step.
	        // The input of the real IDL featureExtraction operation is a file.
	        Map<String, Object> inputMap = new LinkedHashMap<String, Object>(1);
	        inputMap.put("input_data", filePath);
	
	        // ////////////////////////////////////////////////////////////////
        	//
        	// 2) Execute a featureExtraction on the returned coverage 
        	// 
        	// ////////////////////////////////////////////////////////////////
	        result = featureExtraction(inputMap, monitor);
	        if (result != null && !result.isEmpty() && result.containsKey("result")){
	        	
	        	// Get the produced feature from the result map.
		        final String fileToBeIngested = (String) result.get("result");
		        
		        // Preparing for the final step: the feature ingestion in geoserver
		        final Map<String, Object> ingestionInputMap = new LinkedHashMap<String, Object>(4);
		        ingestionInputMap.put("input_data", fileToBeIngested);
		        ingestionInputMap.put(Utilities.GS_URL, input.get(Utilities.GS_URL));
		        ingestionInputMap.put(Utilities.GS_UID, input.get(Utilities.GS_UID));
		        ingestionInputMap.put(Utilities.GS_PWD, input.get(Utilities.GS_PWD));
		        
		        // ////////////////////////////////////////////////////////////////
	        	//
	        	// 3) Ingest the returned feature in geoserver 
	        	// 
	        	// ////////////////////////////////////////////////////////////////
		        result = ingestToGeoserver(ingestionInputMap, monitor, wkt);
		
		        // Delete the feature after it has been ingested (and copied) to geoserver
		        final File fileToBeDeleted = new File(filePath);
		        if (fileToBeDeleted.exists())
		            fileToBeDeleted.delete();
	        }
	        else{
	        	if (LOGGER.isLoggable(Level.SEVERE))
	            	LOGGER.severe("Unable to execute IDL Processing: No results have been produced");
	            return Collections.emptyMap();
	        }
        }catch(Throwable t){
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.severe("Error in processing" + t );
        	return Collections.emptyMap();
        	
        }
        return result;
    }

//    /**
//     * TEST METHOD
//     * @return
//     */
//    private String createShapeFile() {
//
//        final SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyyMMdd'T'HHmmssSSS");
//		final String destinationName= new StringBuilder(dateFormat.format(new Date())).toString();
//    	
//		final String[] exts = new String[]{".shp", ".dbf", ".prj", ".shx"};
//		final String base = "D:/data/shapes/";
//		final String baseInput =  base + "shapes";
//		final String baseOutput = base + destinationName; 
//		
//		for (int i=0;i<4;i++){
//			final String ext = exts[i];
//			
//			File inputFile = new File(baseInput+ext);
//	        File outputFile = new File(baseOutput+ext);
//
//	        FileReader in=null;
//	        FileWriter out=null; 
//			try {
//				in = new FileReader(inputFile);
//				out = new FileWriter(outputFile);
//	        int c;
//
//	        while ((c = in.read()) != -1)
//	          out.write(c);
//
//	        	in.close();
//	        	out.close();
//			} catch (FileNotFoundException e) {
//			} catch (IOException e) {
//			}
//		}
//		return baseOutput + ".shp";
//	}

    /**
     * Execute the real {@code featureExtraction} process.
     * @param input 
	 * 			The input parameters map.
	 * @param monitor
	 * 			The {@link ProgressListener} monitor.	
	 * @return a map containing the result entry (the output feature).
     * @throws ProcessException 
     */
	private Map<String, Object> featureExtraction(Map<String, Object> inputMap,
            ProgressListener monitor) throws ProcessException {
        final Process featureExtractionProcess = IDLMainFeatureExtractionProcessFactory.featureExtractionFactory.create();
        return featureExtractionProcess.execute(inputMap, monitor);
    }

	/**
	 * Execute the {@code getCoverage} process.
	 * 
	 * @param input 
	 * 			The input parameters map also containing the getCoverage request URL.
	 * @param monitor
	 * 			The {@link ProgressListener} monitor.	
	 * @return the path of the file containing the requested coverage.
	 * @throws ProcessException 
	 */
    private String getCoverage(final Map<String, Object> input, final ProgressListener monitor) throws ProcessException {
        final Process getCoverageProcess = IDLMainFeatureExtractionProcessFactory.getCoverageFactory.create();
        final Map<String, Object> result = getCoverageProcess.execute(input, monitor);
        if (!result.isEmpty())
        	return (String) result.get("result");
        return "";
    }

    /**
     * Execute the {@code ingestToGeoserver} process.
     * 
     * @param input
     * 			The input parameters map, also containing the input_data entry representing 
     * 			the feature file to be ingested.
     * @param monitor
     * 			The {@link ProgressListener} monitor.	
     * @param wkt
     * 			The WKT of the feature to be ingested to create a proper PRJ file when missing.
     * @return a map containing the result entry (the ingested layer).
     * @throws ProcessException 
     */
    private Map<String, Object> ingestToGeoserver(
            final Map<String, Object> input, final ProgressListener monitor, final String wkt) throws ProcessException {
        final String fileToBeIngested = (String) input.get("input_data");
        final int extensionIndex = fileToBeIngested.lastIndexOf(".");
        final String extension = fileToBeIngested.substring(extensionIndex + 1);

        if (extension.equalsIgnoreCase("zip")) {
        	final File file = new File(fileToBeIngested);
        	if (file.exists())
        		 input.put("input_data", fileToBeIngested);
        } else {
        	// No zip file available. Create a proper zip archive containing the shape file
        	// as well as the prj with the CRS when missing.
            final String base = fileToBeIngested.substring(0, fileToBeIngested.lastIndexOf("."));
            final String prjPath = new StringBuilder(base).append(".prj").toString();
            final File prjFile = new File(prjPath);
            if(!prjFile.exists())
            	buildPrjFile(prjPath, wkt);
            
            String zipFile;
            try {
                zipFile = zipFiles(base);
                input.put("input_data", zipFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Ingest the zip archive (the extracted feature) in Geoserver.
        final Process geoserverIngestionProcess = IDLMainFeatureExtractionProcessFactory.geoserverIngestionFactory
                .create();
        return geoserverIngestionProcess.execute(input, monitor);
    }

    /**
     * Create a prj file from a provided WKT.
     * @param prjPath 
     * 			the path of the prj file to be written.  
     * @param wkt
     * 			the wkt CRS definition.
     */
    private void buildPrjFile(final String prjPath, final String wkt) {
		BufferedWriter bfw = null;
		try {
			bfw = new BufferedWriter(new FileWriter(new File(prjPath)));
			bfw.write(wkt);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, e.getLocalizedMessage());
		} finally{
			if (bfw!=null){
				try{
					bfw.close();
				}catch(Throwable t){
					if (LOGGER.isLoggable(Level.FINE))
						LOGGER.log(Level.FINE, t.getLocalizedMessage());
				}
			}
		}
	}

	/**
     * Zip a set of files starting with a common prefix.
     */
    private static String zipFiles(final String baseFile) throws IOException{

        final String prjFile = new StringBuilder(baseFile).append(".prj")
                .toString();
        final String shapeFile = new StringBuilder(baseFile).append(".shp")
                .toString();
        final String dbfFile = new StringBuilder(baseFile).append(".dbf")
                .toString();
        final String shxFile = new StringBuilder(baseFile).append(".shx")
                .toString();
        final String zipFile = new StringBuilder(baseFile).append(".zip")
                .toString();

        final String files[] = new String[] { shapeFile, dbfFile, shxFile,
                prjFile };

        // TODO: Add optional files management

        // ///////////////////////////////////
        // Check that the directory is a directory, and get its contents
        // ///////////////////////////////////

        final byte[] buffer = new byte[4096]; // Create a buffer for copying
        int bytesRead;

        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zipFile));
        try {
	        for (int i = 0; i < files.length; i++) {
	            final File f = new File(files[i]);
	            if (f.isDirectory())
	                continue; // Ignore directory
	            final FileInputStream in = new FileInputStream(f); // Stream to read file
	            final ZipEntry entry = new ZipEntry(f.getName()); // Make a ZipEntry
	            out.putNextEntry(entry); // Store entry
	            while ((bytesRead = in.read(buffer)) != -1)
	                out.write(buffer, 0, bytesRead);
	            in.close();
	        }
        } catch (IOException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, e.getLocalizedMessage());
		} finally{
			if (out!=null){
				try{
					out.close();
				}catch(Throwable t){
					if (LOGGER.isLoggable(Level.FINE))
						LOGGER.log(Level.FINE, t.getLocalizedMessage());
				}
			}
		}
        return zipFile;
    }
}
