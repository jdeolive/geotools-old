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
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.geotools.data.Parameter;
import org.geotools.process.Process;
import org.geotools.process.idl.IDLProcessFactory;
import org.geotools.process.idl.IDLWrapperPoolableFactory;
import org.geotools.text.Text;
import org.opengis.util.InternationalString;

/**
 * Factory definition of the Feature Extraction processing functionalities.
 */
public class IDLFeatureExtractionProcessFactory extends IDLProcessFactory {
    
	/** The definition file containing the IDL Feature Extraction algorithm */
    private final static String DEFINITION_FILE="psg_fx__define.pro";
    
    private final static String PROCESS_NAME_PREFIX = "IDL_FEATUREEXTRACT";

    public IDLFeatureExtractionProcessFactory() {
        final IDLFeatureExtractionWrapperPool pool = new IDLFeatureExtractionWrapperPool(
                this);
        pool.setMaxActive(2);
        pool.setMaxIdle(2);
        pool.setMaxWait(3000000);
        pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        setWrapperPool(pool);
    }

    // TODO: input could be a filename, an URI, a getCoverage request String,...
    private static final Parameter<String> INPUT_DATA = new Parameter<String>(
            "input_data", String.class, Text.text("Input data"), Text
                    .text("Input file to be processed"));
    
    private static final Parameter<String> RESULT = new Parameter<String>(
            "result", String.class, Text.text("Output feature"), Text
                    .text("Result of the operation, as the feature produced by the processing"));

    private static final InternationalString TITLE = Text.text("FeatureExt");

    private static final InternationalString DESCRIPTION = Text.text("Extract Feature from an input file");
    
    private static final String NAME = "FeatureExt";

    private static final Map<String, Parameter<?>> parameterInfo = new TreeMap<String, Parameter<?>>();

    private static final Map<String, Parameter<?>> resultInfo = new TreeMap<String, Parameter<?>>();
    
    static {
        parameterInfo.put(INPUT_DATA.key, INPUT_DATA);
        resultInfo.put(RESULT.key, RESULT);
    }

    @Override
    public boolean isAvailable() {
        boolean isAvailable = super.isAvailable();
        if (isAvailable) {
            final File file = new File(new StringBuilder(IDL_LIB_FOLDER).append(FILE_SEPARATOR).append(DEFINITION_FILE).toString());
            if (file.exists()){
                isAvailable = true;
            }
        }
        return isAvailable;
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

    /**
     * Pool of FeatureExtraction Wrappers 
     */
    class IDLFeatureExtractionWrapperPool extends GenericObjectPool {
        public IDLFeatureExtractionWrapperPool(
                final IDLFeatureExtractionProcessFactory factory) {
            super(new IDLFeatureExtractionWrapperPoolableFactory(factory));
        }
    }

    /**
     * Factory to handle pool of FeatureExtraction Wrappers 
     */
    class IDLFeatureExtractionWrapperPoolableFactory extends
            IDLWrapperPoolableFactory {

        public IDLFeatureExtractionWrapperPoolableFactory(
                final IDLFeatureExtractionProcessFactory factory) {
            super(factory, PROCESS_NAME_PREFIX);
        }

        @Override
        public synchronized void destroyObject(Object wrapper) throws Exception {
            ((BaseIDLObjectWrapper)wrapper).destroyObject();
        }

        @Override
        public synchronized Object makeObject() throws Exception {
            BaseIDLObjectWrapper wrapper = new IDLFeatureExtractionWrapper(getProcessNamePrefix() + getTimeStamp());
            wrapper.createObject();
            wrapper.addIDLNotifyListener(wrapper);
            return wrapper;
        }
    }

    /**
     * Create an {@link IDLFeatureExtractionProcess} process
     */
    @Override
    public Process create() {
        return new IDLFeatureExtractionProcess(this);
    }
}
