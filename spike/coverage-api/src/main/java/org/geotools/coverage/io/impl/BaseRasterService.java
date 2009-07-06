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
package org.geotools.coverage.io.impl;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.geotools.coverage.io.service.RasterService;
import org.geotools.data.Parameter;
import org.geotools.factory.Hints;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

/**
 * Base Implementation for the {@link RasterService} interface.
 */
public abstract class BaseRasterService<T> implements RasterService<T> {

    private String name;

    private InternationalString description;

    private InternationalString title;

    private Map<Key, ?> implementationHints;

    private Map<String, Parameter<?>> connectParameterInfo;
	private Map<String, Parameter<?>> createParameterInfo;

    protected BaseRasterService(final String name, final String description,
            final String title, final Hints implementationHints) {
        this.name = name;
        this.description = new SimpleInternationalString(description);
        this.title = new SimpleInternationalString(title);
    }

    public String getName() {
        return this.name;
    }

    public InternationalString getTitle() {
        return this.title;
    }

    /**
     * Implementation hints provided during construction.
     * <p>
     * Often these hints are configuration and factory settings
     * used to intergrate the driver with application services.
     */
    public Map<Key, ?> getImplementationHints() {
        return this.implementationHints;
    }
    public InternationalString getDescription() {
        return this.description;
    }


    public synchronized Map<String, Parameter<?>> getParameterInfo() {
		if( connectParameterInfo == null ){
			connectParameterInfo = defineParameterInfo();
			if( connectParameterInfo == null ){
				connectParameterInfo = Collections.emptyMap();
			}
		}
		return connectParameterInfo;
	}

	/**
	 * Called to define the value returned by getConnectionParameterInfo().
	 * <p>
	 * Subclasses should provide an implementation of this method
	 * indicating the parameters they require.
	 * </p>
	 */
    protected abstract Map<String, Parameter<?>> defineParameterInfo();

    public synchronized Map<String, Parameter<?>> getCreateParameterInfo() {
		if( createParameterInfo == null ){
			createParameterInfo = defineParameterInfo();
			if( createParameterInfo == null ){
				createParameterInfo = Collections.emptyMap();
			}
		}
		return createParameterInfo;
	}

	/** Subclass can override to support create operations */
    public T createInstance(final Map<String, Serializable>  parameters, Hints hints,
    		ProgressListener listener)throws IOException{
    	throw new UnsupportedOperationException( getTitle()+" does not support create operation");
    	
    }
  
}
