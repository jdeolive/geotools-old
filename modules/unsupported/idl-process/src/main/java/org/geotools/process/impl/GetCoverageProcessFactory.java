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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.geotools.data.Parameter;
import org.geotools.process.Process;
import org.geotools.process.impl.util.Utilities;
import org.geotools.text.Text;
import org.opengis.util.InternationalString;

/**
 * Factory definition of the GetCoverage processing functionalities.
 */
public class GetCoverageProcessFactory extends SingleProcessFactory {

    public GetCoverageProcessFactory() {
       
    }

    // TODO: input could be a filename, an URI, a getCoverage request String,...
    // Temp for testings
    private static final Parameter<String> INPUT_URL = new Parameter<String>(
            "input_uri", String.class, Text.text("GetCoverage request URL"), Text
                    .text("URL containing the getCoverageRequest"));
    
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
            "result", String.class, Text.text("Result location"), Text
                    .text("The output GeoTiff file resulting from the request"));

    private static final InternationalString TITLE = Text
            .text("GetCoverage");

    private static final InternationalString DESCRIPTION = Text
            .text("Compute a GetCoverage request and returns a GeoTIFF");
    
    private static final String NAME = "GetCoverage";

    private static final Map<String, Parameter<?>> parameterInfo = new TreeMap<String, Parameter<?>>();

    private static final Map<String, Parameter<?>> resultInfo = new TreeMap<String, Parameter<?>>();
    
    static {
        parameterInfo.put(INPUT_URL.key, INPUT_URL);
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

    public Process create() {
        return new GetCoverageProcess(this);
    }

}
