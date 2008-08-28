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
import java.util.HashMap;
import java.util.Map;

import org.geotools.coverage.io.Driver;
import org.geotools.data.Parameter;
import org.geotools.factory.Hints;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

/**
 * Base Implementation for the {@link Driver} interface.
 */
public abstract class BaseDriver implements Driver {

    private String name;

    private InternationalString description;

    private InternationalString title;

    private Map<Key, ?> implementationHints;

    private Map<String, Parameter<?>> connectParameterInfo;

    protected BaseDriver(final String name, final String description,
            final String title, final Hints implementationHints,
            final Map<String, Parameter<?>> connectParameterInfo) {
        this.name = name;
        this.description = new SimpleInternationalString(description);
        this.title = new SimpleInternationalString(title);
        this.connectParameterInfo = connectParameterInfo != null ? new HashMap<String, Parameter<?>>(
                connectParameterInfo)
                : connectParameterInfo;
    }

    public Map<String, Parameter<?>> getConnectParameterInfo() {
        return this.connectParameterInfo;
    }

    public InternationalString getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public InternationalString getTitle() {
        return this.title;
    }

    public Map<Key, ?> getImplementationHints() {
        return this.implementationHints;
    }

}
