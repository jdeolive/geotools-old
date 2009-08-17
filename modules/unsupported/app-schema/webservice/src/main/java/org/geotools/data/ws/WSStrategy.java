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
package org.geotools.data.ws;

import java.io.IOException;
import java.util.Map;

import net.opengis.wfs.GetFeatureType;

import org.geotools.data.ws.protocol.ws.GetFeature;
import org.geotools.data.ws.protocol.ws.WSProtocol;
import org.geotools.filter.Capabilities;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.opengis.filter.Filter;

import freemarker.template.Template;

/**
 * An interface to allow plugging different strategy objects into a {@link XmlDataStore} to take
 * care of specific WFS implementations limitations or deviations from the spec.
 * 
 * @author rpetty
 * @version $Id$
 * @since 2.6
 * @source $URL:
 *         http://gtsvn.refractions.net/trunk/modules/unsupported/app-schema/webservice/src/main/java/org/geotools/data
 *         /wfs/v1_1_0/WSStrategy.java $
 * @see WSDataStoreFactory
 * @see DefaultWSStrategy
 * @see CubeWerxStrategy
 */
public interface WSStrategy {

    /**
     * A simple means to specify whether POST requests are supported between the server and the
     * client. Regardless of the server supporting the method or not there might be other sort of
     * inconvenients (technical?) that prevents us from using a given method at all, but generally
     * this should just return {@code true}
     * 
     * @return whether we can send POST requests to the server
     */
    public boolean supportsPost();

    /**
     * A simple means to specify whether GET requests are supported between the server and the
     * client. Regardless of the server supporting the method or not there might be other sort of
     * inconvenients (technical?) that prevents us from using a given method at all, but generally
     * this should just return {@code true}
     * 
     * @return whether we can send GET requests to the server
     */
    public boolean supportsGet();

    /**
     * Returns an xml configuration suitable to parse/encode wfs documents appropriate for the
     * server.
     * <p>
     * Note: most of the time it will just be {@link WFSConfiguration}, but it may be possible, for
     * example, an strategy needs to override some bindings.
     * </p>
     * 
     * @return a WFS xml {@link Configuration}
     */
    public Configuration getWsConfiguration();
    
    /**
     * Returns the protocol default output format name for the WFS version the implementation talks.
     * 
     * @param operation
     * 
     * @return the default output format name for the given operation for the protocol version
     */
    public String getDefaultOutputFormat(WSProtocol wfs);

    public Filter[] splitFilters(Capabilities filterCaps, Filter filter);

    /**
     * Creates a GetFeature request that the server implementation this strategy works upon can deal
     * with, and returns both the appropriate request to send to the server as well as the
     * {@link Filter} that should be post processed at runtime once the server response is obtained,
     * in order to match the actual {@code query}.
     * 
     * @param ws
     *            the WFS protocol handler from which the strategy may need to grab some feature
     *            type metadata not available through the datastore interface, or even perform some
     *            test request.
     * @param request
     *            the GetFeature query to create the server request and post-processing filter for
     * @param outputFormat
     *            the output format indentifier that the request needs to be sent for. Shall be
     *            supported by the server for the requested feature type.
     * @return a handle to the request and post-processing filter appropriate to attend the given
     *         {@code query}
     * @throws IOException
     */
 //   public RequestComponents createGetFeatureRequest(WSProtocol wfs, GetFeature request)
   //         throws IOException;

    public Template getTemplate();
    
    public Map createDataModel(GetFeature query) throws IOException;
    
    /**
     * Holds the components needed by the data store to issue and post process a GetFeature request.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @since 2.6
     */
    public class RequestComponents {

        /**
         * The GetFeature request to issue to the WS
         */
        private GetFeatureType serverRequest;

        private Map<String, String> kvpParameters;

        public GetFeatureType getServerRequest() {
            return serverRequest;
        }

        public void setServerRequest(GetFeatureType serverRequest) {
            this.serverRequest = serverRequest;
        }
    }
}
