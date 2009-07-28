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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geotools.data.DataAccess;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.complex.xml.XmlResponse;
import org.geotools.data.complex.xml.XmlXpathFilterData;
import org.geotools.data.view.DefaultView;
import org.geotools.data.ws.XmlDataStore;
import org.geotools.data.ws.protocol.ws.GetFeature;
import org.geotools.data.ws.protocol.ws.WSProtocol;
import org.geotools.data.ws.protocol.ws.WSResponse;
import org.geotools.data.ws.protocol.ws.GetFeature.ResultType;
import org.geotools.feature.NameImpl;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.XmlXpathUtilites;
import org.geotools.util.logging.Logging;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.xml.sax.helpers.NamespaceSupport;
import org.jdom.Document;

/**
 * An implementation of a DS that uses XML over HTTP.
 * <p>
 * Unlike normal DataStores that return features, this returns xml.
 * </p>
 * 
 * @author Russell Petty
 * @version $Id$ 
 */
public final class WS_DataStore implements XmlDataStore {
    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.ws");

    /**
     * Whether to use POST as default HTTP method is not explicitly set
     */
    private static final boolean DEFAULT_HTTP_METHOD = true;

    private static final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat()); 
    private static final SAXBuilder sax = new SAXBuilder();
    
    private final WSProtocol ws;

    private Integer maxFeaturesHardLimit;

    private boolean preferPostOverGet = false;

    private Name name;
    
    private NamespaceSupport namespaces;
    
    private String itemXpath;
    /**
     * The WFS capabilities document.
     * 
     * @param capabilities
     */
    public WS_DataStore(final WSProtocol ws) {
        if (ws == null) {
            throw new NullPointerException("ws protocol");
        }
        this.ws = ws; 
        maxFeaturesHardLimit = Integer.valueOf(0); // not set
    }

    /**
     * @see XmlDataStore#setMaxFeatures(Integer)
     */
    public void setMaxFeatures(Integer maxFeatures) {
        this.maxFeaturesHardLimit = Integer.valueOf(maxFeatures.intValue());
    }

    /**
     * @see XmlDataStore#getMaxFeatures()
     */
    public Integer getMaxFeatures() {
        return this.maxFeaturesHardLimit;
    }

    /**
     * @see XmlDataStore#isPreferPostOverGet()
     */
    public boolean isPreferPostOverGet() {
        return preferPostOverGet;
    }

    /**
     * @see XmlDataStore#setPreferPostOverGet(boolean)
     */
    public void setPreferPostOverGet(Boolean booleanValue) {
        this.preferPostOverGet = booleanValue == null ? DEFAULT_HTTP_METHOD : booleanValue
                .booleanValue();
    }

    /**
     * @see XmlDataStore#getInfo()
     */
    public ServiceInfo getInfo() {
        throw new UnsupportedOperationException("DS not supported!");
    }

    public SimpleFeatureType getSchema(final String prefixedTypeName) throws IOException {
        throw new UnsupportedOperationException("DS not supported!");
    }

    /**
     * @see DataAccess#getSchema(Name)
     * @see #getSchema(String)
     */
    public SimpleFeatureType getSchema(Name name) throws IOException {
        Set<QName> featureTypeNames = ws.getFeatureTypeNames();

        String namespaceURI;
        String localPart;
        for (QName qname : featureTypeNames) {
            namespaceURI = name.getNamespaceURI();
            localPart = name.getLocalPart();
            if (namespaceURI.equals(qname.getNamespaceURI())
                    && localPart.equals(qname.getLocalPart())) {
                String prefixedName = qname.getPrefix() + ":" + localPart;
                return getSchema(prefixedName);
            }
        }
        throw new SchemaNotFoundException(name.getURI());
    }

    /**
     * @see DataAccess#getNames()
     */
    public List<Name> getNames() throws IOException {
        Set<QName> featureTypeNames = ws.getFeatureTypeNames();
        List<Name> names = new ArrayList<Name>(featureTypeNames.size());
        String namespaceURI;
        String localPart;
        for (QName name : featureTypeNames) {
            namespaceURI = name.getNamespaceURI();
            localPart = name.getLocalPart();
            names.add(new NameImpl(namespaceURI, localPart));
        }
        return names;
    }

    /**
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() throws IOException {
        Set<QName> featureTypeNames = ws.getFeatureTypeNames();
        List<String> sorted = new ArrayList<String>(featureTypeNames.size());
        for (QName name : featureTypeNames) {
            sorted.add(name.getPrefix() + ":" + name.getLocalPart());
        }
        Collections.sort(sorted);
        return sorted.toArray(new String[sorted.size()]);
    }

    /**
     * @see org.geotools.data.DataStore#dispose()
     */
    public void dispose() {
        ws.dispose();
    }

    private WSResponse executeGetFeatures(final Query query, final Transaction transaction,
            final ResultType resultType) throws IOException {
        
        final String outputFormat = ws.getDefaultOutputFormat();

        GetFeature request = new GetFeatureQueryAdapter(query, outputFormat, resultType);

        final WSResponse response = ws.issueGetFeaturePOST(request);
        return response;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    public XmlResponse getXmlReader(Query query,
            final Transaction transaction) throws IOException {

        if (Filter.EXCLUDE.equals(query.getFilter())) {
            return null; //empty response--EmptyFeatureReader
        }

        query = new DefaultQuery(query);

        Filter[] filters = ws.splitFilters(query.getFilter());
        Filter supportedFilter = filters[0];
        Filter postFilter = filters[1];
        System.out.println("Supported filter:  " + supportedFilter);
        System.out.println("Unupported filter: " + postFilter);
        ((DefaultQuery) query).setFilter(supportedFilter);
        if (Filter.INCLUDE.equals(postFilter)) {
            ((DefaultQuery) query).setMaxFeatures(getMaxFeatures(query));
        }        

        WSResponse response = executeGetFeatures(query, transaction, ResultType.RESULTS);

        Document doc = null;
        try {
            doc = sax.build(response.getInputStream());           
            out.output(doc, System.out);

        } catch (JDOMException e1) {
            throw new RuntimeException("error reading xml from http", e1);
        }     
        
        int nodeCount = XmlXpathUtilites.countXPathNodes(namespaces, itemXpath, doc);
        List<Integer> l = new ArrayList<Integer>(); 

        for(int i = 1; i <= nodeCount; i++) {        
            XmlXpathFilterData peek = new XmlXpathFilterData(namespaces, doc, i, itemXpath);
            if (postFilter.evaluate(peek)) {
               l.add(i);
            }           
        }                                

        return new XmlResponse(doc, l);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query,
            final Transaction transaction) throws IOException {
          throw new UnsupportedOperationException("DS not supported!");  
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public WSFeatureSource getFeatureSource(final String typeName) throws IOException {
        return new WSFeatureSource(this, typeName, name);
    }

    /**
     * @return {@code null}, no lock support so far
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        throw new UnsupportedOperationException("DS not supported!");
    }

    /**
     * @see org.geotools.data.DataStore#getView(org.geotools.data.Query)
     * @see DefaultView
     */
    public FeatureSource<SimpleFeatureType, SimpleFeature> getView(final Query query)
            throws IOException, SchemaException {
        throw new UnsupportedOperationException("DS not supported!");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.opengis.filter.Filter, org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Filter filter, Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(String typeName,
            Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(Name typeName)
            throws IOException {
        // this is a hack as this datastore only returns one type of response.
        //set the name to what is passed in as it maybe needed later.        
        this.name = typeName;      
        return getFeatureSource(typeName.getLocalPart());
    }

    /**
     * @see DataAccess#updateSchema(Name, org.opengis.feature.type.FeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void updateSchema(Name typeName, SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WS does not support update schema");
    }

    /**
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.opengis.feature.simple.SimpleFeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WS does not support update schema");
    }

    /**
     * @see org.geotools.data.DataStore#createSchema(org.opengis.feature.simple.SimpleFeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WS DataStore does not support createSchema");
    }

    /**
     * @see XmlDataStore#getFeatureTypeName
     */
    public QName getFeatureTypeName(String typeName) {
        throw new UnsupportedOperationException("DS not supported!");
    }

    /**
     * @see XmlDataStore#getFeatureTypeTitle(String)
     */
    public String getFeatureTypeTitle(String typeName) {
        throw new UnsupportedOperationException("DS not supported!");
    }

    /**
     * @see XmlDataStore#getFeatureTypeAbstract(String)
     */
    public String getFeatureTypeAbstract(String typeName) {
        throw new UnsupportedOperationException("DS not supported!");
    }

    /**
     * @see XmlDataStore#getFeatureTypeWGS84Bounds(String)
     */
    public ReferencedEnvelope getFeatureTypeWGS84Bounds(String typeName) {
        throw new UnsupportedOperationException("DS not supported!");
    }

    /**
     * @see XmlDataStore#getFeatureTypeKeywords(String)
     */
    public Set<String> getFeatureTypeKeywords(String typeName) {
        throw new UnsupportedOperationException("DS not supported!");
    }

    /**
     * @see XmlDataStore#getCapabilitiesURL()
     */
    public URL getCapabilitiesURL() {
        URL capsUrl = ws.getOperationURL(false);
        if (capsUrl == null) {
            capsUrl = ws.getOperationURL(true);
        }
        return capsUrl;
    }

    /**
     * @see XmlDataStore#getServiceVersion()
     */
    public String getServiceVersion() {
        return ws.getServiceVersion().toString();
    }

    /**
     * If the query is fully supported, makes a {@code GetFeature} request with {@code
     * resultType=hits} and returns the counts returned by the server, otherwise returns {@code -1}
     * as the result is too expensive to calculate.
     * 
     * @param query
     * @return the number of features returned by a GetFeature?resultType=hits request, or {@code
     *         -1} if not supported
     */
    public int getCount(final Query query) throws IOException {
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WSDataStore[");
        sb.append("version=").append(getServiceVersion());
        URL capabilitiesUrl = getCapabilitiesURL();
        sb.append(", URL=").append(capabilitiesUrl);
        sb.append(", max features=").append(
                maxFeaturesHardLimit.intValue() == 0 ? "not set" : String
                        .valueOf(maxFeaturesHardLimit));
        sb.append(", prefer POST over GET=").append(preferPostOverGet);
        sb.append("]");
        return sb.toString();
    }

    protected int getMaxFeatures(Query query) {
        int maxFeaturesDataStoreLimit = getMaxFeatures().intValue();
        int queryMaxFeatures = query.getMaxFeatures();
        int maxFeatures = Query.DEFAULT_MAX;
        if (Query.DEFAULT_MAX != queryMaxFeatures) {
            maxFeatures = queryMaxFeatures;
        }
        if (maxFeaturesDataStoreLimit > 0) {
            maxFeatures = Math.min(maxFeaturesDataStoreLimit, maxFeatures);
        }
        return maxFeatures;
    }

    public Name getName() {
        return name;
    }
  
    public void setNamespaces(org.xml.sax.helpers.NamespaceSupport namespaces) {
        this.namespaces = namespaces;
    }

    public void setItemXpath(String itemXpath) {
        this.itemXpath = itemXpath;
    }
    
}
