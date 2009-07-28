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
package org.geotools.data.ws.protocol.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.data.Query;
import org.geotools.data.ws.protocol.http.HttpMethod;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

/**
 * 
 * @author rpetty
 * @version $Id$
 * @since 2.6
 * @source $URL:
 *         http://gtsvn.refractions.net/trunk/modules/unsupported/app-schema/webservice/src/main/java/org/geotools/data
 *         /wfs/protocol/wfs/WFSProtocol.java $
 */
public interface WSProtocol {

    /**
     * Returns the WFS protocol version this facade talks to the WFS instance.
     * 
     * @return the protocol version in use by this facade
     */
    public Version getServiceVersion();

    /**
     * Returns the set of type names as extracted from the capabilities document, including the
     * namespace and prefix.
     * 
     * @return the set of feature type names as extracted from the capabilities document
     */
    public Set<QName> getFeatureTypeNames();

    public FilterCapabilities getFilterCapabilities();
    
    /**
     * Returns the URL for the given operation name and HTTP protocol as stated in the WFS
     * capabilities.
     * 
     * @param operation
     *            the name of the WFS operation
     * @param method
     *            the HTTP method
     * @return The URL access point for the given operation and method or {@code null} if the
     *         capabilities does not declare an access point for the operation/method combination
     * @see #supportsOperation(WSOperationType, HttpMethod)
     */
    public URL getOperationURL(final boolean post);

    /**
     * Issues a GetFeature request for the given request, using POST HTTP method
     * <p>
     * The query to WFS request parameter translation is the same than for
     * {@link #issueGetFeatureGET(GetFeature)}
     * </p>
     */
    public WSResponse issueGetFeaturePOST(GetFeature request) throws IOException,
            UnsupportedOperationException;

    /**
     * Allows to free any resource held.
     * <p>
     * Successive calls to this method should not result in any exception, but the instance is meant
     * to not be usable after the first invocation.
     * </p>
     */
    public void dispose();

    public String getDefaultOutputFormat();
    
    public Filter[] splitFilters(Filter filter);
}
