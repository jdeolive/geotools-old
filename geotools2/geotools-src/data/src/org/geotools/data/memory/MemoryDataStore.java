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
package org.geotools.data.memory;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * This is an example implementation of a DataStore used for testing.
 * 
 * <p>
 * It serves as an example implementation of:
 * </p>
 * 
 * <ul>
 * <li>
 * FeatureListenerManager use: allows handling of FeatureEvents
 * </li>
 * </ul>
 * 
 * <p>
 * This class will also illustrate the use of In-Process locking when the time comes.
 * </p>
 *
 * @author jgarnett
 */
public class MemoryDataStore extends AbstractDataStore {
    /** Memory holds Map of Feature by fid by typeName. */
    protected Map memory = new HashMap();

    /** Schema holds FeatureType by typeName */
    protected Map schema = new HashMap();

    public MemoryDataStore() {
        super(true);
    }

    public MemoryDataStore(FeatureCollection collection) {
        addFeatures(collection);
    }

    public MemoryDataStore(Feature[] array) throws IOException {
        addFeatures(array);
    }

    public MemoryDataStore(FeatureReader reader) throws IOException {
        addFeatures(reader);
    }

    /**
     * Configures MemoryDataStore with FeatureReader.
     *
     * @param reader New contents to add
     *
     * @throws IOException If problems are encountered while adding
     * @throws DataSourceException See IOException
     */
    public void addFeatures(FeatureReader reader) throws IOException {
        try {
            FeatureType featureType;
            Map featureMap = new HashMap();
            String typeName;
            Feature feature;

            feature = reader.next();

            if (feature == null) {
                throw new IllegalArgumentException("Provided FeatureReader is closed");
            }

            featureType = feature.getFeatureType();
            typeName = featureType.getTypeName();

            featureMap.put(feature.getID(), feature);

            while (reader.hasNext()) {
                feature = reader.next();
                featureMap.put(feature.getID(), feature);
            }

            schema.put(typeName, featureType);
            memory.put(typeName, featureMap);
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Problem using reader", e);
        }
    }

    /**
     * Configures MemoryDataStore with Collection.
     * 
     * <p>
     * You may use this to create a MemoryDataStore from a FeatureCollection.
     * </p>
     *
     * @param collection Collection of features to add
     *
     * @throws IllegalArgumentException If provided collection is empty
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
     * @param features Array of features to add
     *
     * @throws IllegalArgumentException If provided feature array is empty
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

    /**
     * Adds a single Feature to the correct typeName entry.
     * 
     * <p>
     * This is an internal opperation used for setting up MemoryDataStore - please use
     * FeatureWriter for generatl use.
     * </p>
     * 
     * <p>
     * This method is willing to create new FeatureTypes for MemoryDataStore.
     * </p>
     *
     * @param feature Individual feature to add
     */
    public void addFeature(Feature feature) {
        synchronized (memory) {
            addFeatureInternal(feature);
        }
    }

    private void addFeatureInternal(Feature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("Provided Feature is empty");
        }

        FeatureType featureType;
        featureType = feature.getFeatureType();

        String typeName = featureType.getTypeName();

        Map featuresMap;

        if (!memory.containsKey(typeName)) {
            try {
                createSchema(featureType);
            } catch (IOException e) {
                // this should not of happened ?!?
                // only happens if typeNames is taken and
                // we just checked                    
            }
        }

        featuresMap = (Map) memory.get(typeName);
        featuresMap.put(feature.getID(), feature);
    }

    /**
     * Access featureMap for typeName.
     *
     * @param typeName
     *
     * @return A Map of Features by FID
     *
     * @throws IOException If typeName cannot be found
     */
    protected Map features(String typeName) throws IOException {
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
                types[index] = (String) i.next();
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
     * @throws SchemaNotFoundException DOCUMENT ME!
     *
     * @see org.geotools.data.AbstractDataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        synchronized (memory) {
            if (schema.containsKey(typeName)) {
                return (FeatureType) schema.get(typeName);
            } else {
                throw new SchemaNotFoundException(typeName);
            }
        }
    }

    /**
     * Adds support for a new featureType to MemoryDataStore.
     * 
     * <p>
     * FeatureTypes are stored by typeName, an IOException will be thrown if the requested typeName
     * is already in use.
     * </p>
     *
     * @param featureType FeatureType to be added
     *
     * @throws IOException If featureType already exists
     *
     * @see org.geotools.data.DataStore#createSchema(org.geotools.feature.FeatureType)
     */
    public void createSchema(FeatureType featureType) throws IOException {
        String typeName = featureType.getTypeName();

        if (memory.containsKey(typeName)) {
            // we have a conflict
            throw new IOException(typeName + " already exists");
        } else {
            Map featuresMap = new HashMap();
            schema.put(typeName, featureType);
            memory.put(typeName, featuresMap);
        }
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
     * @return
     *
     * @throws IOException If typeName could not be found
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.AbstractDataStore#getFeatureSource(java.lang.String)
     */
    public FeatureReader getFeatureReader(final String typeName)
        throws IOException {
        return new FeatureReader() {
                FeatureType featureType = getSchema(typeName);
                Iterator iterator = features(typeName).values().iterator();

                public FeatureType getFeatureType() {
                    return featureType;
                }

                public Feature next()
                    throws IOException, IllegalAttributeException, NoSuchElementException {
                    if (iterator == null) {
                        throw new IOException("Feature Reader has been closed");
                    }

                    try {
                        return featureType.duplicate((Feature) iterator.next());
                    } catch (NoSuchElementException end) {
                        throw new DataSourceException("There are no more Features", end);
                    }
                }

                public boolean hasNext() throws IOException {
                    return (iterator != null) && iterator.hasNext();
                }

                public void close() throws IOException {
                    if (iterator != null) {
                        iterator = null;
                    }

                    if (featureType != null) {
                        featureType = null;
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
     * @param typeName name of FeatureType we wish to modify
     *
     * @return FeatureWriter of entire contents of typeName
     *
     * @throws IOException If writer cannot be obtained for typeName
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.AbstractDataStore#getFeatureSource(java.lang.String)
     */
    public FeatureWriter getFeatureWriter(final String typeName)
        throws IOException {
        return new FeatureWriter() {
                FeatureType featureType = getSchema(typeName);
                Map contents = features(typeName);
                Iterator iterator = contents.values().iterator();
                Feature live = null;
                ; // current 

                Feature current = null; // current Feature returned to user        

                public FeatureType getFeatureType() {
                    return featureType;
                }

                public Feature next() throws IOException, NoSuchElementException {
                    if (hasNext()) {
                        // existing content
                        live = (Feature) iterator.next();

                        try {
                            current = featureType.duplicate(live);
                        } catch (IllegalAttributeException e) {
                            throw new DataSourceException("Unable to edit " + live.getID() + " of "
                                + typeName);
                        }
                    } else {
                        // new content
                        live = null;

                        try {
                            current = DataUtilities.template(featureType);
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
                        listenerManager.fireFeaturesRemoved(typeName, Transaction.AUTO_COMMIT,
                            live.getBounds());
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
                                live.setAttributes(current.getAttributes(null));
                            } catch (IllegalAttributeException e) {
                                throw new DataSourceException("Unable to accept modifications to "
                                    + live.getID() + " on " + typeName);
                            }

                            Envelope bounds = new Envelope();
                            bounds.expandToInclude(live.getBounds());
                            bounds.expandToInclude(current.getBounds());
                            listenerManager.fireFeaturesChanged(typeName, Transaction.AUTO_COMMIT,
                                bounds);
                            live = null;
                            current = null;
                        }
                    } else {
                        // add new content
                        //
                        contents.put(current.getID(), current);
                        listenerManager.fireFeaturesAdded(typeName, Transaction.AUTO_COMMIT,
                            current.getBounds());
                        current = null;
                    }
                }

                public boolean hasNext() throws IOException {
                    if (contents == null) {
                        throw new IOException("FeatureWriter has been closed");
                    }

                    return (iterator != null) && iterator.hasNext();
                }

                public void close() throws IOException {
                    if (iterator != null) {
                        iterator = null;
                    }

                    if (featureType != null) {
                        featureType = null;
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
    protected Envelope getBounds(String typeName, Query query)
        throws IOException {
        FeatureType featureType = getSchema(typeName);
        Map contents = features(typeName);
        Iterator iterator = contents.values().iterator();

        Envelope envelope = null;

        if (iterator.hasNext()) {
            int count = 1;
            Filter filter = query.getFilter();
            Feature first = (Feature) iterator.next();
            envelope = first.getDefaultGeometry().getEnvelopeInternal();

            while (iterator.hasNext() && (count < query.getMaxFeatures())) {
                Feature feature = (Feature) iterator.next();

                if (filter.contains(feature)) {
                    count++;
                    envelope.expandToInclude(feature.getDefaultGeometry().getEnvelopeInternal());
                }
            }
        }

        return envelope;
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getCount(java.lang.String, org.geotools.data.Query)
     */
    protected int getCount(String typeName, Query query)
        throws IOException {
        FeatureType featureType = getSchema(typeName);
        Map contents = features(typeName);
        Iterator iterator = contents.values().iterator();

        int count = 0;
        Envelope envelope = null;

        Filter filter = query.getFilter();

        while (iterator.hasNext() && (count < query.getMaxFeatures())) {
            if (filter.contains((Feature) iterator.next())) {
                count++;
            }
        }

        return count;
    }
}
