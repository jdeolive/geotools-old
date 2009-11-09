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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.geotools.data.Parameter;
import org.geotools.process.Process;
import org.geotools.process.idl.IDLProcessFactory;
import org.geotools.process.impl.GeoserverIngestionProcessFactory;
import org.geotools.process.impl.GetCoverageProcessFactory;
import org.geotools.process.impl.util.Utilities;
import org.geotools.text.Text;
import org.opengis.util.InternationalString;

/**
 * Factory definition of the Feature Extraction processing functionalities.
 */
public class IDLMainFeatureExtractionProcessFactory extends IDLProcessFactory {
    
	/** The GeoserverIngestion process factory */
    final static GeoserverIngestionProcessFactory geoserverIngestionFactory = new GeoserverIngestionProcessFactory(); 
    
    /** The GetCoverage process factory */
    final static GetCoverageProcessFactory getCoverageFactory = new GetCoverageProcessFactory();
    
    /** The FeatureExtraction process factory */
    final static IDLFeatureExtractionProcessFactory featureExtractionFactory = new IDLFeatureExtractionProcessFactory();

    /** Base constructor */
    public IDLMainFeatureExtractionProcessFactory() {
    	
    }

    // TODO: input could be a filename, an URI, a getCoverage request String,...
    private static final Parameter<String> INPUT_DATA = new Parameter<String>(
            "input_data", String.class, Text.text("Input data"), Text
                    .text("URI referring the input data to be processed"));
    
    private static final Parameter<String> GEOSERVER_BASE_URL = new Parameter<String>(
            Utilities.GS_URL, String.class, Text.text("Geoserver url"), Text
                    .text("Geoserver url"));
    
    private static final Parameter<String> GEOSERVER_UID = new Parameter<String>(
            Utilities.GS_UID, String.class, Text.text("Geoserver uid"), Text
                    .text("Geoserver User id"));
    
    private static final Parameter<String> GEOSERVER_PWD = new Parameter<String>(
            Utilities.GS_PWD, String.class, Text.text("Geoserver pwd"), Text
                    .text("Geoserver user password"));
    
    private static final Parameter<String> OUTPUT_DIR = new Parameter<String>(
            Utilities.OUTPUT_DIR, String.class, Text.text("output dir"), Text
                    .text("The output dir where to save exported geotiff"));
    
    private static final Parameter<String> RESULT = new Parameter<String>(
            "result", String.class, Text.text("Ingested layer"), Text
                    .text("Result of the operation, as the feature ingested in geoserver"));

    private static final InternationalString TITLE = Text
            .text("FeatureExtraction");

    private static final InternationalString DESCRIPTION = Text
            .text("Extract Features and ingest them on geoserver");
    
    private static final String NAME = "FeatureExtraction";

    private static final Map<String, Parameter<?>> parameterInfo = new TreeMap<String, Parameter<?>>();

    private static final Map<String, Parameter<?>> resultInfo = new TreeMap<String, Parameter<?>>();
    
    static {
        parameterInfo.put(INPUT_DATA.key, INPUT_DATA);
        parameterInfo.put(GEOSERVER_BASE_URL.key, GEOSERVER_BASE_URL);
        parameterInfo.put(GEOSERVER_UID.key, GEOSERVER_UID);
        parameterInfo.put(GEOSERVER_PWD.key, GEOSERVER_PWD);
        parameterInfo.put(OUTPUT_DIR.key, OUTPUT_DIR);
        resultInfo.put(RESULT.key, RESULT);
    }

    public InternationalString getDescription() {
        return DESCRIPTION;
    }

    public Map<String, Parameter<?>> getParameterInfo() {
        return Collections.unmodifiableMap(parameterInfo);
    }

    public Map<String, Parameter<?>> getResultInfo(
            Map<String, Object> parameters) throws IllegalArgumentException {
        return Collections.unmodifiableMap(resultInfo);
    }

    public InternationalString getTitle() {
        return TITLE;
    }

    public String getVersion() {
        return "1.0.0";
    }

    public boolean supportsProgress() {
        return true;
    }

    public String getName() {
        return NAME;
    }

    @Override
    public Process create() {
        return new IDLMainFeatureExtractionProcess(this);
    }

}
