/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.ws.v1_1_0;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.geotools.data.ResourceInfo;
import org.geotools.data.ws.XmlDataStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A {@link ResourceInfo} adapter for the GetCapabilities information provided by the custom methods
 * in {@link XmlDataStore}.
 * 
 * @author Grpetty
 * @version $Id$
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/unsupported/app-schema/webservice/src/main/java/org/geotools/wfs/v_1_1_0
 *         /data/XmlSimpleFeatureParser.java $
 */
final class CapabilitiesResourceInfo implements ResourceInfo {
    private WS_DataStore wfs;

    private String typeName;

    public CapabilitiesResourceInfo(String typeName, WS_DataStore service) {
        this.typeName = typeName;
        this.wfs = service;
    }

    public String getTitle() {
        return wfs.getFeatureTypeTitle(typeName);
    }

    public String getDescription() {
        return wfs.getFeatureTypeAbstract(typeName);
    }

    /**
     * @see ResourceInfo#getBounds()
     */
    public ReferencedEnvelope getBounds() {
        return null;
    }

    public CoordinateReferenceSystem getCRS() {
        return null;
    }

    public Set<String> getKeywords() {
        return wfs.getFeatureTypeKeywords(typeName);
    }

    public String getName() {
        return typeName;
    }

    public URI getSchema() {
        URL describeFeatureTypeURL;
        describeFeatureTypeURL = null; //wfs.getDescribeFeatureTypeURL(typeName);
        if (describeFeatureTypeURL == null) {
            return null;
        }
        try {
            return describeFeatureTypeURL.toURI();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
