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

import static net.opengis.wfs.ResultTypeType.HITS_LITERAL;
import static net.opengis.wfs.ResultTypeType.RESULTS_LITERAL;
import static org.geotools.data.ws.protocol.ws.GetFeature.ResultType.RESULTS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.WfsFactory;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;

import org.geotools.data.ws.protocol.ws.GetFeature;
import org.geotools.data.ws.protocol.ws.WSProtocol;
import org.geotools.data.ws.protocol.ws.GetFeature.ResultType;
import org.geotools.factory.GeoTools;
import org.geotools.filter.Capabilities;
import org.geotools.filter.v1_1.OGC;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.filter.visitor.CapabilitiesFilterSplitter;
import org.geotools.util.logging.Logging;

import org.geotools.wfs.v1_1.WFSConfiguration;
import org.geotools.xml.Encoder;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.sort.SortBy;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * A default strategy for a WFS 1.1.0 implementation that assumes the server sticks to the standard.
 * 
 * @author rpetty
 * @version $Id$
 * @since 2.6
 * @source $URL:
 *         http://gtsvn.refractions.net/trunk/modules/unsupported/app-schema/webservice/src/main/java/org/geotools/data
 *         /ws/v1_1_0/DefaultWSStrategy.java $
 */
@SuppressWarnings("nls")
public class DefaultWSStrategy implements WSStrategy {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    protected static final String DEFAULT_OUTPUT_FORMAT = "text/xml; subtype=gml/3.1.1";

    private static Configuration cfg;
    
    private static Template requestTemplate;
    
    private static final String GSMLNS = "http://www.cgi-iugs.org/xml/GeoSciML/2";
    
    private static final org.geotools.xml.Configuration wfs_1_1_0_Configuration = new WFSConfiguration();


    public DefaultWSStrategy(String templateDirectory,
            String templateName) {
        LOGGER.log(Level.WARNING,"template directory is: " + templateDirectory);
        initialiseFreeMarkerConfiguration(templateDirectory);
        try {
            requestTemplate = cfg.getTemplate(templateName);
//            Query q = namedQuery(Filter.INCLUDE,
//                    new Integer(5));
//            
//            Map root = new HashMap();
//            root.put("unitId", q);
//            Writer out = new OutputStreamWriter(System.out);
//            requestTemplate.process(root, out);
//            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();}
//        } catch (TemplateException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
    
    private DefaultQuery namedQuery(Filter filter, int count) {
        DefaultQuery namedQuery = null;
        try {
            namedQuery = new DefaultQuery("MappedFeature", new URI(GSMLNS), filter, count,
                new String[] {}, "tom");
        } catch (Exception e) {
            System.out.println(e);
        }
        
        return namedQuery;
    }
    
    private void initialiseFreeMarkerConfiguration(String templateDirectory) {
        cfg = new Configuration();
        // Specify the data source where the template files come from.
        // Here I set a file directory for it:
        try {
          //  File f = new File(".");
         //   File f1 = f.getCanonicalFile();
           cfg.setDirectoryForTemplateLoading(
                    new File(templateDirectory));
       } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
        // Specify how templates will see the data-model. This is an advanced topic...
        // but just use this:
        cfg.setObjectWrapper(new DefaultObjectWrapper()); 
    }
    
    public Template getTemplate() {
        return requestTemplate;
    }
    /**
     * @see WSStrategy#supportsGet()
     */
    public boolean supportsGet() {
        return false;
    }

    /**
     * @see WSStrategy#supportsPost()
     */
    public boolean supportsPost() {
        return true;
    }

    /**
     * @return {@code "text/xml; subtype=gml/3.1.1"}
     * @see WSProtocol#getDefaultOutputFormat()
     */
    public String getDefaultOutputFormat(WSProtocol wfs) {
       return DEFAULT_OUTPUT_FORMAT;
    }

    /**
     * Creates the mapping {@link GetFeatureType GetFeature} request for the given {@link Query} and
     * {@code outputFormat}, and post-processing filter based on the server's stated filter
     * capabilities.
     * 
     * @see WSStrategy#createGetFeatureRequest(WFS_1_1_0_DataStore, WSProtocol, Query, String)
     */
    @SuppressWarnings("unchecked")
    public Map createDataModel(GetFeature query)
            throws IOException {
        final WfsFactory factory = WfsFactory.eINSTANCE;

        GetFeatureType getFeature = factory.createGetFeatureType();
        getFeature.setService("WS");        
        getFeature.setOutputFormat(query.getOutputFormat());

        getFeature.setHandle("GeoTools " + GeoTools.getVersion() + " WS DataStore");
        Integer maxFeatures = query.getMaxFeatures();
        if (maxFeatures != null) {
            getFeature.setMaxFeatures(BigInteger.valueOf(maxFeatures.intValue()));
        }

        ResultType resultType = query.getResultType();
        getFeature.setResultType(RESULTS == resultType ? RESULTS_LITERAL : HITS_LITERAL);

        QueryType wsQuery = factory.createQueryType();
        wsQuery.setTypeName(Collections.singletonList(query.getTypeName()));

        Filter serverFilter = query.getFilter();
        if (!Filter.INCLUDE.equals(serverFilter)) {
            wsQuery.setFilter(serverFilter);
        }

        String[] propertyNames = query.getPropertyNames();
        boolean retrieveAllProperties = propertyNames == null;
        if (!retrieveAllProperties) {
            List propertyName = wsQuery.getPropertyName();
            for (String propName : propertyNames) {
                propertyName.add(propName);
            }
        }
        SortBy[] sortByList = query.getSortBy();
        if (sortByList != null) {
            for (SortBy sortBy : sortByList) {
                wsQuery.getSortBy().add(sortBy);
            }
        }

        getFeature.getQuery().add(wsQuery);

   //     RequestComponents reqParts = new RequestComponents();
     //   reqParts.setServerRequest(getFeature);
  
   //     reqParts.setKvpParameters(parametersForGet);
        Map root = new HashMap();
        Filter f = query.getFilter();
        Integer maxfeatures = query.getMaxFeatures();
        if(maxfeatures == null) {
            maxfeatures = new Integer(0);
        }
        String filterString = f.toString();
        LOGGER.log(Level.WARNING,"Filter to search on: " + filterString);
        LOGGER.log(Level.WARNING,"MaxFeatures: " + maxfeatures);
        root.put("filterString", filterString);
        root.put("maxfeatures", maxfeatures);
        
        return root;
    }

    /**
     * @see WFSStrategy#getWfsConfiguration()
     */
    public org.geotools.xml.Configuration getWsConfiguration() {
        return wfs_1_1_0_Configuration;
    }

    /**
     * Splits the filter provided by the geotools query into the server supported and unsupported
     * ones.
     * 
     * @param caps
     *            the server filter capabilities description
     * @param queryFilter
     * @return a two-element array where the first element is the supported filter and the second
     *         the one to post-process
     * @see WSStrategy#splitFilters(WFS_1_1_0_Protocol, Filter)
     */
    public Filter[] splitFilters(Capabilities caps, Filter queryFilter) {
        CapabilitiesFilterSplitter splitter = new CapabilitiesFilterSplitter(
                caps, null, null);

        queryFilter.accept(splitter, null);

        Filter server = splitter.getFilterPre();
        Filter post = splitter.getFilterPost();

        return new Filter[] { server, post };
    }

}
