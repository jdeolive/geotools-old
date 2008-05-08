/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.wfs.v_1_1_0.data;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import javax.swing.Icon;

import org.geotools.data.ResourceInfo;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

final class CapabilitiesResourceInfo implements ResourceInfo {
    private WFS110ProtocolHandler protocolHandler;

    private String typeName;

    public CapabilitiesResourceInfo(String typeName, WFS110ProtocolHandler protocolHandler) {
        this.typeName = typeName;
        this.protocolHandler = protocolHandler;
    }

    public String getTitle() {
        return protocolHandler.getFeatureTypeTitle(typeName);
    }

    public String getDescription() {
        return protocolHandler.getFeatureTypeAbstract(typeName);
    }

    public ReferencedEnvelope getBounds() {
        return protocolHandler.getFeatureTypeBounds(typeName);
    }

    public CoordinateReferenceSystem getCRS() {
        return protocolHandler.getFeatureTypeCRS(typeName);
    }

    public Set<String> getKeywords() {
        return protocolHandler.getKeywords(typeName);
    }

    public String getName() {
        return typeName;
    }

    public URI getSchema() {
        URL describeFeatureTypeURL;
        try {
            describeFeatureTypeURL = protocolHandler.getDescribeFeatureTypeURLGet(typeName);
        } catch (MalformedURLException e) {
            return null;
        }
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