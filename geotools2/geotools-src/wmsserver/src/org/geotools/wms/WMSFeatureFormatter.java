/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.wms;

import org.geotools.feature.Feature;
import java.io.OutputStream;
import org.geotools.feature.FeatureCollection;


/**
 * An interface for formatting a list of geotools Feature objects into a
 * stream. IE, application/vnd.ogc.gml requests that the feature information
 * be formatted in Geography Markup Language (GML) FeatureFormatters are
 * attached to the WMSServlet on initialization, and an arbitrary number can
 * be attached
 */
public interface WMSFeatureFormatter {
    /**
     * Gets the mime-type of the stream written to by formatFeatures()
     *
     * @return DOCUMENT ME!
     */
    public String getMimeType();

    /**
     * Formats the given array of Features as this Formatter's mime-type and
     * writes it to the given OutputStream
     */
    public void formatFeatures(FeatureCollection features, OutputStream out);
}
