/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.complex.xml.XmlResponse;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jdom.Document;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * {@link DataStore} extension interface to provide WFS specific extra information.
 * 
 * @author rpetty
 * @version $Id$
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/app-schema/webservice/src/main/java/org/geotools
 *         /data/wfs/WFSDataStore.java $
 */
public interface XmlDataStore extends DataStore {

    public URL getCapabilitiesURL();

    public String getServiceVersion();

    public String getFeatureTypeTitle(String typeName);

    public QName getFeatureTypeName(String typeName);

    public String getFeatureTypeAbstract(String typeName);

    public ReferencedEnvelope getFeatureTypeWGS84Bounds(String typeName);

    public Set<String> getFeatureTypeKeywords(String typeName);

    public void setMaxFeatures(Integer maxFeatures);

    public Integer getMaxFeatures();

    public XmlResponse getXmlReader(Query query,
            final Transaction transaction) throws IOException;
    /**
     * 
     * @param booleanValue
     *            Boolean.TRUE to prefer POST over GET, Boolean.FALSE for the opposite, {@code null}
     *            for auto (let the implementation decide)
     */
    public void setPreferPostOverGet(Boolean booleanValue);

    public boolean isPreferPostOverGet();
}
