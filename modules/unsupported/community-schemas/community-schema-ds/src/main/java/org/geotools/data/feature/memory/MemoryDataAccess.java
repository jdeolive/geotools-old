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

package org.geotools.data.feature.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.catalog.ServiceInfo;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Source;
import org.geotools.data.Transaction;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.data.feature.adapter.GTFeatureAdapter;
import org.geotools.data.feature.adapter.GTFeatureTypeAdapter;
import org.geotools.data.feature.adapter.ISOFeatureAdapter;
import org.geotools.data.feature.adapter.ISOFeatureTypeAdapter;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.iso.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This is an example implementation of a DataStore used for testing.
 * 
 * <p>
 * It serves as an example implementation of:
 * </p>
 * 
 * <ul>
 * <li> FeatureListenerManager use: allows handling of FeatureEvents </li>
 * </ul>
 * 
 * <p>
 * This class will also illustrate the use of In-Process locking when the time
 * comes.
 * </p>
 * 
 * @author jgarnett
 * @source $URL:
 *         http://gtsvn.refractions.net/geotools/trunk/gt/modules/library/main/src/main/java/org/geotools/data/memory/MemoryDataStore.java $
 */
public class MemoryDataAccess extends AbstractDataStore implements FeatureAccess {
    /** Memory holds Map of Feature by fid by typeName. */
    protected Map memory = new HashMap();

    /** Schema holds FeatureType by typeName */
    protected Map schema = new HashMap();

    private SimpleFeatureFactory attributeFactory = new SimpleFeatureFactoryImpl();

    public MemoryDataAccess() {
        super(true);
    }

    public MemoryDataAccess(FeatureCollection collection) {
        addFeatures(collection);
    }

    public MemoryDataAccess(Feature[] array) {
        addFeatures(array);
    }

    public MemoryDataAccess(org.geotools.feature.FeatureCollection collection) {
        org.geotools.feature.Feature[] features = (org.geotools.feature.Feature[]) collection
                .toArray(new org.geotools.feature.Feature[0]);
        addFeatures(features);
    }

    public MemoryDataAccess(org.geotools.feature.Feature[] features) {
        addFeatures(features);
    }

    public MemoryDataAccess(FeatureReader reader) throws NoSuchElementException, IOException,
            IllegalAttributeException {
        try {
            ISOFeatureTypeAdapter isoType = null;
            while (reader.hasNext()) {
                org.geotools.feature.Feature feature = reader.next();
                if (isoType == null) {
                    isoType = new ISOFeatureTypeAdapter(feature.getFeatureType());
                }
                Feature isoFeature = new ISOFeatureAdapter(feature, isoType, attributeFactory);
                addFeatureInternal(isoFeature);
            }
        } finally {
            reader.close();
        }
    }

    public void addFeatures(FeatureCollection collection) {
        Iterator iterator = collection.iterator();
        List features = new LinkedList();
        Feature f;
        try{
            while(iterator.hasNext()){
                f = (Feature) iterator.next();
                features.add(f);
            }
        }finally{
            collection.close(iterator);
        }
        addFeatures(features);
    }
    
    /**
     * Configures MemoryDataStore with Collection.
     * 
     * <p>
     * You may use this to create a MemoryDataStore from a FeatureCollection.
     * </p>
     * 
     * @param collection
     *            Collection of features to add
     * 
     * @throws IllegalArgumentException
     *             If provided collection is empty
     */
    public void addFeatures(Collection collection) {
        if ((collection == null) || collection.isEmpty()) {
            throw new IllegalArgumentException("Provided FeatureCollection is empty");
        }

        synchronized (memory) {
            for (Iterator i = collection.iterator(); i.hasNext();) {
                addFeatureInternal((Feature) i.next());
            }
        }
    }

    /**
     * Configures MemoryDataStore with feature array.
     * 
     * @param features
     *            Array of features to add
     * 
     * @throws IllegalArgumentException
     *             If provided feature array is empty
     */
    public void addFeatures(Feature[] features) {
        if ((features == null) || (features.length == 0)) {
            throw new IllegalArgumentException("Provided features are empty");
        }

        synchronized (memory) {
            for (int i = 0; i < features.length; i++) {
                addFeatureInternal(features[i]);
            }
        }
    }

    public void addFeatures(org.geotools.feature.Feature[] features) {
        if ((features == null) || (features.length == 0)) {
            throw new IllegalArgumentException("Provided features are empty");
        }

        synchronized (memory) {
            ISOFeatureTypeAdapter isoType = new ISOFeatureTypeAdapter(features[0].getFeatureType());
            for (int i = 0; i < features.length; i++) {
                org.geotools.feature.Feature feature = features[i];
                Feature isoFeature = new ISOFeatureAdapter(feature, isoType, attributeFactory);
                addFeatureInternal(isoFeature);
            }
        }
    }

    /**
     * Adds a single Feature to the correct typeName entry.
     * 
     * <p>
     * This is an internal opperation used for setting up MemoryDataStore -
     * please use FeatureWriter for generatl use.
     * </p>
     * 
     * <p>
     * This method is willing to create new FeatureTypes for MemoryDataStore.
     * </p>
     * 
     * @param feature
     *            Individual feature to add
     */
    public void addFeature(org.geotools.feature.Feature feature) {
        synchronized (memory) {
            Feature f = new ISOFeatureAdapter(feature, null, attributeFactory);
            addFeatureInternal(f);
        }
    }

    public void addFeatureInternal(Feature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("Provided Feature is empty");
        }

        FeatureType featureType;
        featureType = (FeatureType) feature.getType();

        Name typeName = featureType.getName();

        Map featuresMap;

        if (!memory.containsKey(typeName)) {
            try {
                createSchemaInternal(featureType);
            } catch (IOException e) {
                // this should not of happened ?!?
                // only happens if typeNames is taken and
                // we just checked
            }
        }

        featuresMap = (Map) memory.get(typeName);
        featuresMap.put(feature.getID(), feature);
    }

    protected Map features(String typeName) throws IOException {
        Name name = typeName(typeName);
        return features(name);
    }

    /**
     * Access featureMap for typeName.
     * 
     * @param typeName
     * 
     * @return A Map of Features by FID
     * 
     * @throws IOException
     *             If typeName cannot be found
     */
    protected Map features(Name typeName) throws IOException {
        synchronized (memory) {
            if (memory.containsKey(typeName)) {
                return (Map) memory.get(typeName);
            }
        }

        throw new IOException("Type name " + typeName + " not found");
    }

    /**
     * List of available types provided by this DataStore.
     * 
     * @return Array of type names
     * 
     * @see org.geotools.data.AbstractDataStore#getFeatureTypes()
     */
    public String[] getTypeNames() {
        synchronized (memory) {
            String[] types = new String[schema.size()];
            int index = 0;

            for (Iterator i = schema.keySet().iterator(); i.hasNext(); index++) {
                Name next = (Name) i.next();
                types[index] = next.getLocalPart();
            }

            return types;
        }
    }

    /**
     * FeatureType access by <code>typeName</code>.
     * 
     * @param typeName
     * 
     * @return FeatureType for <code>typeName</code>
     * 
     * @throws IOException
     * @throws SchemaNotFoundException
     *             DOCUMENT ME!
     * 
     * @see org.geotools.data.AbstractDataStore#getSchema(java.lang.String)
     */
    public org.geotools.feature.FeatureType getSchema(String typeName) throws IOException {
        FeatureType isoType = getSchemaInternal(typeName);
        synchronized (memory) {
            if (!(isoType instanceof SimpleFeatureType)) {
                throw new IllegalArgumentException("Do not ask getSchema for non simple types: "
                        + typeName);
            }
            org.geotools.feature.FeatureType gtType;
            if (isoType instanceof ISOFeatureTypeAdapter) {
                gtType = ((ISOFeatureTypeAdapter) isoType).getAdaptee();
            } else {
                gtType = new GTFeatureTypeAdapter((SimpleFeatureType) isoType);
            }
            return gtType;
        }
    }

    public FeatureType getSchemaInternal(String typeName) throws SchemaNotFoundException {
        Name name = typeName(typeName);
        return getSchemaInternal(name);
    }

    public FeatureType getSchemaInternal(Name name) throws SchemaNotFoundException {
        synchronized (memory) {
            if (schema.containsKey(name)) {
                FeatureType isoType = (FeatureType) schema.get(name);
                return isoType;
            }
            throw new SchemaNotFoundException(String.valueOf(name));
        }
    }

    private Name typeName(String typeName) throws SchemaNotFoundException {
        for (Iterator it = schema.keySet().iterator(); it.hasNext();) {
            Name name = (Name) it.next();
            if (name.getLocalPart().equals(typeName)) {
                return name;
            }
        }
        throw new SchemaNotFoundException(typeName);
    }

    /**
     * Adds support for a new featureType to MemoryDataStore.
     * 
     * <p>
     * FeatureTypes are stored by typeName, an IOException will be thrown if the
     * requested typeName is already in use.
     * </p>
     * 
     * @param featureType
     *            FeatureType to be added
     * 
     * @throws IOException
     *             If featureType already exists
     * 
     * @see org.geotools.data.DataStore#createSchema(org.geotools.feature.FeatureType)
     */
    public void createSchema(org.geotools.feature.FeatureType featureType) throws IOException {

        SimpleFeatureType isoType = new ISOFeatureTypeAdapter(featureType);

        createSchemaInternal(isoType);
    }

    public void createSchemaInternal(FeatureType isoType) throws IOException {

        Name typeName = isoType.getName();

        if (memory.containsKey(typeName)) {
            // we have a conflict
            throw new IOException(typeName + " already exists");
        }
        Map featuresMap = new java.util.LinkedHashMap();// HashMap();
        schema.put(typeName, isoType);
        memory.put(typeName, featuresMap);
    }

    /**
     * Provides FeatureReader over the entire contents of <code>typeName</code>.
     * 
     * <p>
     * Implements getFeatureReader contract for AbstractDataStore.
     * </p>
     * 
     * @param typeName
     * 
     * 
     * @throws IOException
     *             If typeName could not be found
     * @throws DataSourceException
     *             See IOException
     * 
     * @see org.geotools.data.AbstractDataStore#getFeatureSource(java.lang.String)
     */
    public FeatureReader getFeatureReader(final String typeName) throws IOException {
        final FeatureType featureType = getSchemaInternal(typeName);

        if (!(featureType instanceof SimpleFeatureType)) {
            throw new IllegalArgumentException(
                    "do not use getFeatureReader for non simple Features");
        }

        final org.geotools.feature.FeatureType gtFType;
        if (featureType instanceof ISOFeatureTypeAdapter) {
            gtFType = ((ISOFeatureTypeAdapter) featureType).getAdaptee();
        } else {
            gtFType = new GTFeatureTypeAdapter((SimpleFeatureType) featureType);
        }

        return new FeatureReader() {

            Name name = typeName(typeName);

            Iterator iterator = features(name).values().iterator();

            public org.geotools.feature.FeatureType getFeatureType() {
                return gtFType;
            }

            public org.geotools.feature.Feature next() throws IOException,
                    IllegalAttributeException, NoSuchElementException {
                if (iterator == null) {
                    throw new IOException("Feature Reader has been closed");
                }

                try {
                    Object obj = iterator.next();

                    org.geotools.feature.Feature gtFeature;

                    if (obj instanceof org.geotools.feature.Feature) {
                        // it might be we're under a decorator reader
                        gtFeature = (org.geotools.feature.Feature) obj;
                    } else if (obj instanceof ISOFeatureAdapter) {
                        ISOFeatureAdapter featureAdapter = (ISOFeatureAdapter) obj;
                        gtFeature = (featureAdapter).getAdaptee();
                    } else {
                        SimpleFeature simpleFeature = (SimpleFeature) obj;
                        gtFeature = new GTFeatureAdapter(simpleFeature, gtFType);
                    }

                    return gtFType.duplicate(gtFeature);
                } catch (NoSuchElementException end) {
                    throw new DataSourceException("There are no more Features", end);
                }
            }

            public boolean hasNext() {
                return (iterator != null) && iterator.hasNext();
            }

            public void close() {
                if (iterator != null) {
                    iterator = null;
                }
            }
        };
    }

    /**
     * Provides FeatureWriter over the entire contents of <code>typeName</code>.
     * 
     * <p>
     * Implements getFeatureWriter contract for AbstractDataStore.
     * </p>
     * 
     * @param typeName
     *            name of FeatureType we wish to modify
     * 
     * @return FeatureWriter of entire contents of typeName
     * 
     * @throws IOException
     *             If writer cannot be obtained for typeName
     * @throws DataSourceException
     *             See IOException
     * 
     * @see org.geotools.data.AbstractDataStore#getFeatureSource(java.lang.String)
     */
    public FeatureWriter createFeatureWriter(final String typeName, final Transaction transaction)
            throws IOException {

        final FeatureType featureType = getSchemaInternal(typeName);

        if (!(featureType instanceof SimpleFeatureType)) {
            throw new IllegalArgumentException(
                    "do not use getFeatureWriter for non simple Features");
        }

        final org.geotools.feature.FeatureType gtFType;
        if (featureType instanceof ISOFeatureTypeAdapter) {
            gtFType = ((ISOFeatureTypeAdapter) featureType).getAdaptee();
        } else {
            gtFType = new GTFeatureTypeAdapter((SimpleFeatureType) featureType);
        }

        return new FeatureWriter() {

            Map contents = features(featureType.getName());

            Iterator iterator = contents.values().iterator();

            SimpleFeature live = null;

            org.geotools.feature.SimpleFeature gtLive = null;

            org.geotools.feature.SimpleFeature current = null; // current

            // Feature

            // returned to user

            public org.geotools.feature.FeatureType getFeatureType() {
                return gtFType;
            }

            public org.geotools.feature.Feature next() throws IOException, NoSuchElementException {
                if (hasNext()) {
                    // existing content
                    live = (SimpleFeature) iterator.next();
                    if (live instanceof ISOFeatureAdapter) {
                        gtLive = (org.geotools.feature.SimpleFeature) ((ISOFeatureAdapter) live)
                                .getAdaptee();
                    } else {
                        gtLive = new GTFeatureAdapter(live, gtFType);
                    }
                    try {
                        current = (org.geotools.feature.SimpleFeature) gtFType.duplicate(gtLive);
                    } catch (IllegalAttributeException e) {
                        throw new DataSourceException("Unable to edit " + live.getID() + " of "
                                + typeName);
                    }
                } else {
                    // new content
                    live = null;

                    try {
                        current = (org.geotools.feature.SimpleFeature) DataUtilities
                                .template(gtFType);
                    } catch (IllegalAttributeException e) {
                        throw new DataSourceException("Unable to add additional Features of "
                                + typeName);
                    }
                }

                return current;
            }

            public void remove() throws IOException {
                if (contents == null) {
                    throw new IOException("FeatureWriter has been closed");
                }

                if (current == null) {
                    throw new IOException("No feature available to remove");
                }

                if (live != null) {
                    // remove existing content
                    iterator.remove();
                    listenerManager.fireFeaturesRemoved(typeName, transaction, gtLive.getBounds(),
                            true);
                    live = null;
                    current = null;
                } else {
                    // cancel add new content
                    current = null;
                }
            }

            public void write() throws IOException {
                if (contents == null) {
                    throw new IOException("FeatureWriter has been closed");
                }

                if (current == null) {
                    throw new IOException("No feature available to write");
                }

                if (live != null) {
                    if (live.equals(current)) {
                        // no modifications made to current
                        //
                        live = null;
                        current = null;
                    } else {
                        // accept modifications
                        //
                        try {
                            gtLive.setAttributes(current.getAttributes(null));
                        } catch (IllegalAttributeException e) {
                            throw new DataSourceException("Unable to accept modifications to "
                                    + live.getID() + " on " + typeName);
                        }

                        Envelope bounds = new Envelope();
                        bounds.expandToInclude(gtLive.getBounds());
                        bounds.expandToInclude(current.getBounds());
                        listenerManager.fireFeaturesChanged(typeName, transaction, bounds, true);
                        live = null;
                        current = null;
                    }
                } else {
                    // add new content
                    //
                    contents.put(current.getID(), current);
                    listenerManager.fireFeaturesAdded(typeName, transaction, current.getBounds(),
                            true);
                    current = null;
                }
            }

            public boolean hasNext() throws IOException {
                if (contents == null) {
                    throw new IOException("FeatureWriter has been closed");
                }

                return (iterator != null) && iterator.hasNext();
            }

            public void close() {
                if (iterator != null) {
                    iterator = null;
                }
                contents = null;
                current = null;
                live = null;
            }
        };
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getBounds(java.lang.String,
     *      org.geotools.data.Query)
     */
    protected Envelope getBounds(Query query) throws IOException {
        String typeName = query.getTypeName();
        Name name = typeName(typeName);

        Map contents = features(name);

        Iterator iterator = contents.values().iterator();

        ReferencedEnvelope envelope = null;

        if (iterator.hasNext()) {
            int count = 1;
            Filter filter = query.getFilter();
            Feature first = (Feature) iterator.next();

            envelope = new ReferencedEnvelope(first.getBounds());
//            envelope.init(first.getBounds());

            while (iterator.hasNext() && (count < query.getMaxFeatures())) {
                Feature feature = (Feature) iterator.next();

                if (filter.evaluate(feature)) {
                    count++;
                    envelope.include(feature.getBounds());
                }
            }
        }

        return envelope;
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getCount(java.lang.String,
     *      org.geotools.data.Query)
     */
    protected int getCount(Query query) throws IOException {
        String typeName = query.getTypeName();
        Name name = typeName(typeName);
        Map contents = features(name);
        Iterator iterator = contents.values().iterator();

        int count = 0;

        Filter filter = query.getFilter();

        while (iterator.hasNext() && (count < query.getMaxFeatures())) {
            if (filter.evaluate(iterator.next())) {
                count++;
            }
        }

        return count;
    }

    // ////////// DataAcess implementation //////////////

    public Source access(Name typeName) {
        FeatureType type;
        try {
            type = getSchemaInternal(typeName);
        } catch (SchemaNotFoundException e) {
            throw new NoSuchElementException(e.getMessage());
        }
        Map map;
        try {
            map = features(typeName);
        } catch (IOException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        Collection collection = map.values();
        return new MemorySource(this, type, collection);
    }

    public Object describe(Name typeName) {
        FeatureType ftype;
        try {
            ftype = getSchemaInternal(typeName);
        } catch (SchemaNotFoundException e) {
            throw new NoSuchElementException(e.getMessage());
        }
        TypeFactory tf = new TypeFactoryImpl();
        AttributeDescriptor descriptor = tf.createAttributeDescriptor(ftype, ftype.getName(), 0,
                Integer.MAX_VALUE, true, null);
        return descriptor;
    }

    public void dispose() {
        // TODO Auto-generated method stub
    }

    public ServiceInfo getInfo() {
        return null;
    }

    public List/* <Name> */getNames() {
        List names = new ArrayList(schema.keySet());
        return names;
    }
}
