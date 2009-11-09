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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.idl.IDLBaseTestCase;
import org.geotools.process.idl.PrintingProgressListener;
import org.geotools.process.idl.impl.IDLFeatureExtractionProcessFactory;
import org.geotools.test.TestData;
import org.junit.Assert;

/**
 * Simple test class leveraging on the Feature Extraction function,
 * offered by means of the proper IDL wrapper.
 */
public class IDLProcessTest extends IDLBaseTestCase {

    /**
     * Launch 3 processes executing the Feature Extraction process.
     * Note that the pool is actually with size = 2, therefore
     * you should notice the third process starting after one 
     * of the previous two have finished. 
     * 
     * @throws InterruptedException
     */
    @org.junit.Test
    public void testProcesses() throws InterruptedException {
        if (!isIDLAvailable())
            return;
        final IDLFeatureExtractionProcessFactory factory = new IDLFeatureExtractionProcessFactory();
        ProcessThread p1 = new ProcessThread(factory);
        p1.start();
        p1.run();
    }
    
    static class ProcessThread extends Thread {
        IDLFeatureExtractionProcessFactory factory;

        public ProcessThread(IDLFeatureExtractionProcessFactory factory) {
            this.factory = factory;
        }

        public void run() {
            final Process process = factory.create();
            File testData = null;
			try {
				testData = TestData.file(this,"testin.tif");
			} catch (FileNotFoundException e1) {
				
			} catch (IOException e1) {
				
			}
            final Map<String, Object> values = new LinkedHashMap<String, Object>(2);
            values.put("input_data",testData.getAbsolutePath());  
            
//            values.put("input_data", "http://localhost:8081/geoserver/ows?service=WCS&request=GetCoverage&Format=GeoTIFF&version=1.0.0&" +
//            		"coverage=topp:asar22&crs=EPSG:32721&bbox=-22856.625,2412871.75,812143.375,3385771.75&width=800&height=523" 
//            		);
//            values.put(Utilities.GS_URL, "http://localhost:8081/geoserver");
//            values.put(Utilities.GS_UID, "admin");
//            values.put(Utilities.GS_PWD, "geoserver");
//            values.put(Utilities.OUTPUT_DIR, "D:\\gds\\jboss-4.2.3.GA\\workdir\\features");
            Map<String, Object> result = null;
			try {
				result = process.execute(values,
				        new PrintingProgressListener());
			} catch (ProcessException e) {
				
			}
            if (result != null && !result.isEmpty()) {
                final Iterator keysIt = result.keySet().iterator();
                final Object output = result.get(keysIt.next());
                Assert.assertNotNull(output);
            }
        }
    }
}
