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
package org.geotools.data;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * This is a starting point for providing your own FeatureStore implementation.
 *
 * @author Jody Garnett, Refractions Research
 */
public abstract class AbstractFeatureStore extends AbstractFeatureSource
    implements FeatureStore {
    /** Current Transaction this FeatureSource is opperating against */
    protected Transaction transaction = Transaction.AUTO_COMMIT;

    /**
     * Retrieve the Transaction this FeatureSource is opperating against.
     *
     * @return Transaction FeatureSource is operating against
     */
    public Transaction getTransaction() {
        return transaction;
    }

    // 
    // FeatureStore implementation against DataStore API
    // 

    /**
     * Modifies features matching <code>filter</code>.
     * 
     * <p>
     * Equivelent to:
     * </p>
     * <pre><code>
     * FeatureWriter writer = dataStore.getFeatureWriter( typeName, filter, transaction );
     * while( writer.hasNext() ){
     *    feature = writer.next();
     *    feature.setAttribute( type.getName(), value );
     *    writer.write();
     * }
     * writer.close();
     * </code>
     * </pre>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param type Attribute to modify
     * @param value Modification being made to type
     * @param filter Identifies features to modify
     *
     * @throws IOException If modification could not be made
     *
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType,
     *      java.lang.Object, org.geotools.filter.Filter)
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws IOException {
        modifyFeatures(new AttributeType[] { type, }, new Object[] { value, },
            filter);
    }

    /**
     * Modifies features matching <code>filter</code>.
     * 
     * <p>
     * Equivelent to:
     * </p>
     * <pre><code>
     * FeatureWriter writer = dataStore.getFeatureWriter( typeName, filter, transaction );
     * Feature feature;
     * while( writer.hasNext() ){
     *    feature = writer.next();
     *    feature.setAttribute( type[0].getName(), value[0] );
     *    feature.setAttribute( type[1].getName(), value[1] );
     *    ...
     *    feature.setAttribute( type[N].getName(), value[N] ); 
     *    writer.write();
     * }
     * writer.close();
     * </code>
     * </pre>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param type Attributes to modify
     * @param value Modifications being made to type
     * @param filter Identifies features to modify
     *
     * @throws IOException If we could not modify Feature
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType,
     *      java.lang.Object, org.geotools.filter.Filter)
     */
    public void modifyFeatures(AttributeType[] type, Object[] value,
        Filter filter) throws IOException {
        String typeName = getSchema().getTypeName();
        FeatureWriter writer = getDataStore().getFeatureWriter(typeName,
                filter, getTransaction());
        Feature feature;

        try {
            while (writer.hasNext()) {
                feature = writer.next();

                for (int i = 0; i < type.length; i++) {
                    try {
                        feature.setAttribute(type[i].getName(), value[i]);
                    } catch (IllegalAttributeException e) {
                        throw new DataSourceException(
                            "Could not update feature " + feature.getID()
                            + " with " + type[i].getName() + "=" + value[i], e);
                    }
                }

                writer.write();
            }
        } finally {
            writer.close();
        }
    }

    /**
     * Add Features from reader to this FeatureStore.
     * 
     * <p>
     * Equivelent to:
     * </p>
     * <pre><code>
     * Set set = new HashSet();
     * FeatureWriter writer = dataStore.getFeatureWriter( typeName, true, transaction );
     * Featrue feature, newFeature;
     * while( reader.hasNext() ){
     *    feature = reader.next();
     *    newFeature = writer.next();
     *    newFeature.setAttributes( feature.getAttribtues( null ) );
     *    writer.write();
     *    set.add( newfeature.getID() );
     * }
     * reader.close();
     * writer.close();
     * 
     * return set;
     * </code>
     * </pre>
     * 
     * <p>
     * (If you don't have a FeatureReader handy DataUtilities.reader() may be
     * able to help out)
     * </p>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param reader
     *
     * @return The Set of FeatureIDs added
     *
     * @throws IOException If we encounter a problem encounter writing content
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.FeatureStore#addFeatures(org.geotools.data.FeatureReader)
     */
    public Set addFeatures(FeatureReader reader) throws IOException {
        Set addedFids = new HashSet();
        String typeName = getSchema().getTypeName();
        Feature feature = null;
        Feature newFeature;
        FeatureWriter writer = getDataStore().getFeatureWriterAppend(typeName,
                getTransaction());

        try {
            while (reader.hasNext()) {
                try {
                    feature = reader.next();
                } catch (Exception e) {
                    throw new DataSourceException("Could not add Features, problem with provided reader",
                        e);
                }

                newFeature = writer.next();

                try {
                    newFeature.setAttributes(feature.getAttributes(null));
                } catch (IllegalAttributeException writeProblem) {
                    throw new DataSourceException("Could not create "
                        + typeName + " out of provided feature: "
                        + feature.getID(), writeProblem);
                }

                writer.write();
                addedFids.add(newFeature.getID());
            }
        } finally {
            reader.close();
            writer.close();
        }

        return addedFids;
    }

    /**
     * Removes features indicated by provided filter.
     * 
     * <p>
     * Equivelent to:
     * </p>
     * <pre><code>
     * FeatureWriter writer = dataStore.getFeatureWriter( typeName, filter, transaction );
     * Feature feature;
     * while( writer.hasNext() ){
     *    feature = writer.next();
     *    writer.remove();
     * }
     * writer.close();
     * </code>
     * </pre>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param filter Identifies features to remove
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType,
     *      java.lang.Object, org.geotools.filter.Filter)
     */
    public void removeFeatures(Filter filter) throws IOException {
        String typeName = getSchema().getTypeName();
        FeatureWriter writer = getDataStore().getFeatureWriter(typeName,
                filter, getTransaction());
        Feature feature;

        try {
            while (writer.hasNext()) {
                feature = writer.next();
                writer.remove();
            }
        } finally {
            writer.close();
        }
    }

    /**
     * Replace with contents of reader.
     * 
     * <p>
     * Equivelent to:
     * </p>
     * <pre><code>
     * FeatureWriter writer = dataStore.getFeatureWriter( typeName, false, transaction );
     * Feature feature, newFeature;
     * while( writer.hasNext() ){
     *    feature = writer.next();
     *    writer.remove();
     * }
     * while( reader.hasNext() ){
     *    newFeature = reader.next();
     *    feature = writer.next();
     *    newFeature.setAttributes( feature.getAttributes( null ) );
     *    writer.write();
     * }
     * reader.close();
     * writer.close();
     * </code>
     * </pre>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param reader Contents to replace with
     *
     * @throws IOException if anything goes wrong during replacement
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType,
     *      java.lang.Object, org.geotools.filter.Filter)
     */
    public void setFeatures(FeatureReader reader) throws IOException {
        String typeName = getSchema().getTypeName();
        FeatureWriter writer = getDataStore().getFeatureWriter(typeName,
                getTransaction());
        Feature feature;
        Feature newFeature;

        try {
            while (writer.hasNext()) {
                feature = writer.next();
                writer.remove();
            }

            while (reader.hasNext()) {
                try {
                    feature = reader.next();
                } catch (Exception readProblem) {
                    throw new DataSourceException("Could not add Features, problem with provided reader",
                        readProblem);
                }

                newFeature = writer.next();

                try {
                    newFeature.setAttributes(feature.getAttributes(null));
                } catch (IllegalAttributeException writeProblem) {
                    throw new DataSourceException("Could not create "
                        + typeName + " out of provided feature: "
                        + feature.getID(), writeProblem);
                }

                writer.write();
            }
        } finally {
            reader.close();
            writer.close();
        }
    }

    public void setTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException(
                "Transaction cannot be null, did you mean Transaction.AUTO_COMMIT?");
        }

        this.transaction = transaction;
    }
}
