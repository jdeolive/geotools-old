/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.feature.adapter;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.geotools.catalog.ServiceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Source;
import org.geotools.data.Transaction;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.feature.iso.Types;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class FeatureAccessAdapter implements FeatureAccess {

    private DataStore store;

    private SimpleFeatureFactory attributeFactory;

    public FeatureAccessAdapter(DataStore dataStore, SimpleFeatureFactory attributeFactory) {
        if (dataStore == null) {
            throw new NullPointerException("dataStore");
        }
        this.attributeFactory = attributeFactory;
        this.store = dataStore;
    }

    public void createSchema(FeatureType featureType) throws IOException {
        store.createSchema(featureType);
    }

    public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        return store.getFeatureReader(query, transaction);
    }

    public FeatureSource getFeatureSource(String typeName) throws IOException {
        return store.getFeatureSource(typeName);
    }

    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
            throws IOException {
        return store.getFeatureWriter(typeName, filter, transaction);
    }

    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
            throws IOException {
        return store.getFeatureWriter(typeName, transaction);
    }

    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
            throws IOException {
        return store.getFeatureWriterAppend(typeName, transaction);
    }

    public LockingManager getLockingManager() {
        return store.getLockingManager();
    }

    public FeatureType getSchema(String typeName) throws IOException {
        return store.getSchema(typeName);
    }

    public String[] getTypeNames() throws IOException {
        return store.getTypeNames();
    }

    public FeatureSource getView(Query query) throws IOException, SchemaException {
        return store.getView(query);
    }

    public void updateSchema(String typeName, FeatureType featureType) throws IOException {
        store.updateSchema(typeName, featureType);
    }

    // ///////////////////////////////////////////////////

    public Source access(Name typeName) {
        FeatureSource2 source;
        if (store instanceof FeatureAccess) {
            FeatureAccess faccess = (FeatureAccess) store;
            source = (FeatureSource2) faccess.access(typeName);
        } else {
            try {
                String localPart = typeName.getLocalPart();
                FeatureSource featureSource = store.getFeatureSource(localPart);
                source = new FeatureSource2Adapter(this, featureSource, attributeFactory);
            } catch (IOException e) {
                e.printStackTrace();
                throw (RuntimeException) new RuntimeException(e.getMessage()).initCause(e);
            }
        }
        return source;
    }

    public Object describe(Name typeName) {
        AttributeDescriptor descriptor;
        if (store instanceof FeatureAccess) {
            FeatureAccess faccess = (FeatureAccess) store;
            descriptor = (AttributeDescriptor) faccess.describe(typeName);
        } else {
            Source source = access(typeName);
            descriptor = (AttributeDescriptor) source.describe();
        }
        return descriptor;
    }

    public void dispose() {
        if (store instanceof FeatureAccess) {
            FeatureAccess faccess = (FeatureAccess) store;
            faccess.dispose();
        }
    }

    public ServiceInfo getInfo() {
        if (store instanceof FeatureAccess) {
            FeatureAccess faccess = (FeatureAccess) store;
            return faccess.getInfo();
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List getNames() {
        if (store instanceof FeatureAccess) {
            FeatureAccess faccess = (FeatureAccess) store;
            return faccess.getNames();
        }

        String[] typeNames;
        try {
            typeNames = getTypeNames();
        } catch (IOException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        int len = typeNames.length;
        List names = new ArrayList(len);
        try {
            for (int i = 0; i < i; i++) {
                String featureName = typeNames[i];
                FeatureType schema;
                schema = getSchema(featureName);
                URI namespace = schema.getNamespace();
                String nsUri = namespace == null ? null : namespace.toString();
                Name name = Types.attributeName(nsUri, featureName);
                names.add(name);
            }
        } catch (IOException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        return names;
    }

}
