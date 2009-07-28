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

import static org.geotools.data.ws.protocol.http.HttpMethod.GET;
import static org.geotools.data.ws.protocol.http.HttpMethod.POST;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows10.DCPType;
import net.opengis.ows10.DomainType;
import net.opengis.ows10.KeywordsType;
import net.opengis.ows10.OnlineResourceType;
import net.opengis.ows10.OperationType;
import net.opengis.ows10.OperationsMetadataType;
import net.opengis.ows10.Ows10Factory;
import net.opengis.ows10.RequestMethodType;
import net.opengis.ows10.ServiceIdentificationType;
import net.opengis.ows10.ServiceProviderType;
import net.opengis.ows10.WGS84BoundingBoxType;
import net.opengis.wfs.DescribeFeatureTypeType;
import net.opengis.wfs.FeatureTypeType;
import net.opengis.wfs.GetCapabilitiesType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.GetGmlObjectType;
import net.opengis.wfs.LockFeatureType;
import net.opengis.wfs.OutputFormatListType;
import net.opengis.wfs.TransactionType;
import net.opengis.wfs.WFSCapabilitiesType;

import org.eclipse.emf.ecore.EObject;
import org.geotools.data.DataSourceException;
import org.geotools.data.Query;

import org.geotools.data.ws.protocol.http.HTTPProtocol;
import org.geotools.data.ws.protocol.http.HTTPResponse;
import org.geotools.data.ws.protocol.http.HttpMethod;
import org.geotools.data.ws.protocol.http.HTTPProtocol.POSTCallBack;
import org.geotools.data.ws.protocol.ws.GetFeature;
import org.geotools.data.ws.protocol.ws.Version;
import org.geotools.data.ws.protocol.ws.WSProtocol;
import org.geotools.data.ws.protocol.ws.WSResponse;
import org.geotools.data.ws.v1_1_0.WSStrategy.RequestComponents;
import org.geotools.filter.Capabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.xml.sax.SAXException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * {@link WSProtocol} implementation to talk to a WFS 1.1.0 server leveraging the GeoTools {@code
 * xml-xsd} subsystem for schema assisted parsing and encoding of WFS requests and responses.
 * 
 * @author rpetty
 * @version $Id$
 * @since 2.6
 * @source $URL:
 *         http://gtsvn.refractions.net/trunk/modules/unsupported/app-schema/webservice/src/main/java/org/geotools/data
 *         /wfs/v1_1_0/WFS_1_1_0_Protocol.java $
 */
@SuppressWarnings( { "unchecked", "nls" })
public class WS_Protocol implements WSProtocol {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.ws");

    private WSStrategy strategy;

    /**
     * Per featuretype name Map of capabilities feature type information. Not to be used directly
     * but through {@link #getFeatureTypeInfo(String)}
     */
    private final Map<String, FeatureTypeType> typeInfos;

    private HTTPProtocol http;

    private URL url;

    final WFSCapabilitiesType capabilities;

    public WS_Protocol(InputStream capabilitiesReader, WSStrategy strategy, URL query,
            HTTPProtocol http) throws IOException {
        this.strategy = strategy;
        this.capabilities = parseCapabilities(capabilitiesReader);
        this.http = http;
        this.url = query;
        this.typeInfos = new HashMap<String, FeatureTypeType>();
    }

    public void setStrategy(WSStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * @return {@link Version#v1_1_0}
     * @see WSProtocol#getServiceVersion()
     */
    public Version getServiceVersion() {
        return Version.v1_0_0;
    }

    public URL getOperationURL(final boolean post) {
        return this.url;
    }

    /**
     * @see WSProtocol#getFeatureTypeNames()
     */
    public Set<QName> getFeatureTypeNames() {
        Set<QName> typeNames = new HashSet<QName>();
        for (FeatureTypeType typeInfo : typeInfos.values()) {
            QName name = typeInfo.getName();
            typeNames.add(name);
        }
        return typeNames;
    }

    /**
     * @see WSProtocol#getFeaturePOST(Query, String)
     */
    public WSResponse issueGetFeaturePOST(final GetFeature request) throws IOException {

        URL url = getOperationURL(true);
        Map reqParts = strategy.createDataModel(request);
        WSResponse response = issuePostRequest(reqParts, url);
        return response;
    }

    /**
     * @see WSProtocol#dispose()
     */
    public void dispose() {
        // do nothing
    }

    private WSResponse issuePostRequest(final Map request, final URL url) throws IOException {

        final POSTCallBack requestBodyCallback = new POSTCallBack() {
            public long getContentLength() {
                // don't know
                return -1;
            }

            public String getContentType() {
                return "text/xml";
            }

            public void writeBody(final OutputStream out) throws IOException {                
                WS_Protocol.encode(request, strategy, out);
            }
        };

        HTTPResponse httpResponse = http.issuePost(url, requestBodyCallback);

        String responseCharset = httpResponse.getResponseCharset();
        Charset charset = responseCharset == null ? null : Charset.forName(responseCharset);
        String contentType = httpResponse.getContentType();
        InputStream responseStream = httpResponse.getResponseStream();
        String target = httpResponse.getTargetUrl();
        WSResponse response = new WSResponse(target, request, charset, contentType, responseStream);
        return response;
    }

    /**
     * Encodes a WFS request into {@code out}
     * 
     * @param request
     *            one of {@link GetCapabilitiesType}, {@link GetFeatureType}, etc
     * @param configuration
     *            the wfs configuration to use for encoding the request into the output stream
     * @param out
     *            the output stream where to encode the request into
     * @param charset
     *            the charset to use to encode the request in
     * @throws IOException
     */
    public static void encode(final EObject request, final Configuration configuration,
            final OutputStream out, final Charset charset) throws IOException {
        Encoder encoder = new Encoder(configuration);
        encoder.setEncoding(charset);        
    }

    private static void encode(Map request, WSStrategy strategy, OutputStream out)
            throws IOException {
        Template template = strategy.getTemplate();
        Writer wr = new OutputStreamWriter(out);
        try {
            template.process(request, wr);
        } catch (TemplateException e) {
            throw new RuntimeException("error creating request template", e);
        }
    }

    public String getDefaultOutputFormat() {
        return strategy.getDefaultOutputFormat(this);
    }

    private WFSCapabilitiesType parseCapabilities(InputStream capabilitiesReader)
            throws IOException {

        final Configuration wsConfig = strategy.getWsConfiguration();
        final Parser parser = new Parser(wsConfig);
        final Object parsed;
        try {
            parsed = parser.parse(capabilitiesReader);
        } catch (SAXException e) {
            throw new DataSourceException("Exception parsing WFS 1.1.0 capabilities", e);
        } catch (ParserConfigurationException e) {
            throw new DataSourceException("WFS 1.1.0 parsing configuration error", e);
        }
        if (parsed == null) {
            throw new DataSourceException("WFS 1.1.0 capabilities was not parsed");
        }
        if (!(parsed instanceof WFSCapabilitiesType)) {
            throw new DataSourceException("Expected WFS Capabilities, got " + parsed);
        }
        return (WFSCapabilitiesType) parsed;
    }
    
    /**
     * @see WFSProtocol#getFilterCapabilities()
     */
    public FilterCapabilities getFilterCapabilities() {        
        return capabilities.getFilterCapabilities();
    }
    
    public Filter[] splitFilters(Filter filter) {
        FilterCapabilities filterCapabilities = getFilterCapabilities();
        Capabilities filterCaps = new Capabilities();
        if (filterCapabilities != null) {
            filterCaps.addAll(filterCapabilities);
        }
        return strategy.splitFilters(filterCaps, filter);
    }
}
