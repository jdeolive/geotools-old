/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.pool.Command;
import org.geotools.arcsde.pool.Session;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.jdbc.MutableFIDFeature;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.BoundingBox;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeStreamOp;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeUpdate;
import com.esri.sde.sdk.client.SeTable.SeTableIdRange;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

abstract class ArcSdeFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

    protected static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    /**
     * Fid prefix used for just created and not yet committed features
     */
    private static final String NEW_FID_PREFIX = "@NEW_";

    /**
     * Complete feature type this writer acts upon
     */
    protected final SimpleFeatureType featureType;

    /**
     * Connection to hold while this feature writer is alive.
     */
    protected Session session;

    /**
     * Reader for streamed access to filtered content this writer acts upon.
     */
    protected FeatureReader<SimpleFeatureType, SimpleFeature> filteredContent;

    /**
     * Builder for new Features this writer creates when next() is called and hasNext() == false
     */
    protected final SimpleFeatureBuilder featureBuilder;

    /**
     * Map of {row index/mutable column names} in the SeTable structure. Not to be accessed
     * directly, but through {@link #getMutableColumnNames(Session)}
     */
    private LinkedHashMap<Integer, String> mutableColumnNames;

    private LinkedHashMap<Integer, String> insertableColumnNames;

    /**
     * Not to be accessed directly, but through {@link #getLayer()}
     */
    private SeLayer cachedLayer;

    /**
     * Not to be accessed directly, but through {@link #getTable()}
     */
    private SeTable cachedTable;

    /**
     * The feature at the current index. No need to maintain any sort of collection of features as
     * this writer works a feature at a time.
     */
    protected SimpleFeature feature;

    /**
     * Provides row_id column index
     */
    protected final FIDReader fidReader;

    protected final FeatureListenerManager listenerManager;

    /**
     * version handler to delegate setting up and handling database version states
     */
    private final ArcSdeVersionHandler versionHandler;

    public ArcSdeFeatureWriter(final FIDReader fidReader,
                               final SimpleFeatureType featureType,
                               final FeatureReader<SimpleFeatureType, SimpleFeature> filteredContent,
                               final Session session,
                               final FeatureListenerManager listenerManager,
                               final ArcSdeVersionHandler versionHandler) throws IOException {

        assert fidReader != null;
        assert featureType != null;
        assert filteredContent != null;
        assert session != null;
        assert listenerManager != null;
        assert versionHandler != null;

        this.fidReader = fidReader;
        this.featureType = featureType;
        this.filteredContent = filteredContent;
        this.session = session;
        this.listenerManager = listenerManager;
        this.featureBuilder = new SimpleFeatureBuilder(featureType);
        this.versionHandler = versionHandler;
    }

    /**
     * Creates the type of arcsde stream operation specified by the {@code streamType} class and,if
     * the working layer is of a versioned table, sets up the stream to being editing the default
     * database version.
     * 
     * @param streamType
     * @return
     * @throws IOException
     */
    private SeStreamOp createStream(Class<? extends SeStreamOp> streamType) throws IOException {
        SeStreamOp streamOp;

        if (SeInsert.class == streamType) {
            streamOp = Session.issueCreateSeInsert(session);
        } else if (SeUpdate.class == streamType) {
            streamOp = Session.issueCreateSeUpdate(session);
        } else if (SeDelete.class == streamType) {
            streamOp = Session.issueCreateSeDelete(session);
        } else {
            throw new IllegalArgumentException("Unrecognized stream type: " + streamType);
        }

        versionHandler.setUpStream(session, streamOp);

        return streamOp;
    }

    /**
     * @see FeatureWriter#close()
     */
    public void close() throws IOException {

        if (filteredContent != null) {
            filteredContent.close();
            filteredContent = null;
        }

        // let repeatedly calling close() be inoffensive
        if (session != null) {
            session.close();
            session = null;
        }
    }

    /**
     * @see FeatureWriter#getFeatureType()
     */
    public final SimpleFeatureType getFeatureType() {
        return featureType;
    }

    /**
     * @see FeatureWriter#hasNext()
     */
    public final boolean hasNext() throws IOException {
        // filteredContent may be null because we
        // took the precaution of closing it in a previous call
        // to this method
        final boolean hasNext = filteredContent != null && filteredContent.hasNext();
        // be cautious of badly coded clients
        if (!hasNext && filteredContent != null) {
            filteredContent.close();
            filteredContent = null;
        }
        return hasNext;
    }

    /**
     * @see FeatureWriter#next()
     */
    public final SimpleFeature next() throws IOException {
        if (hasNext()) {
            feature = filteredContent.next();
        } else {
            final String newFid = newFid();
            final SimpleFeature newFeature = featureBuilder.buildFeature(newFid);
            final List<Property> properties = (List<Property>) newFeature.getProperties();
            feature = new MutableFIDFeature(properties, featureType, newFid);
        }
        return feature;
    }

    /**
     * @see FeatureWriter#remove()
     */
    public void remove() throws IOException {
        if (isNewlyCreated(feature)) {
            // we're in auto commit, no need to remove anything
            return;
        }
        // deletes are executed immediately. We set up a transaction
        // if in autocommit mode to be committed or rolled back on this same
        // method if something happens bellow.
        final boolean handleTransaction = !session.isTransactionActive();
        if (handleTransaction) {
            Session.issueStartTransaction(session);
        }

        final String id = feature.getID();
        final long featureId = ArcSDEAdapter.getNumericFid(id);
        final SeObjectId objectID = new SeObjectId(featureId);
        final String qualifiedName = featureType.getTypeName();

        final SeDelete seDelete = (SeDelete) createStream(SeDelete.class);

        final Command<Void> deleteCmd = new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                try {
                    // A call to SeDelete.byId immediately deletes the row from the
                    // database. The application does not need to call execute()
                    seDelete.byId(qualifiedName, objectID);
                    if (handleTransaction) {
                        session.commitTransaction();
                    }
                    fireRemoved(feature);
                } catch (IOException e) {
                    if (handleTransaction) {
                        try {
                            session.rollbackTransaction();
                        } catch (IOException e1) {
                            LOGGER.log(Level.SEVERE, "Unrecoverable error rolling "
                                    + "back delete transaction", e);
                        }
                    }
                    throw new DataSourceException("Error deleting feature with id:" + featureId, e);
                } finally {
                    if (seDelete != null) {
                        try {
                            seDelete.close();
                        } catch (SeException e) {
                            LOGGER.log(Level.SEVERE,
                                    "Unrecoverable error rolling back delete transaction", e);
                        }
                    }
                }
                return null;
            }
        };

        try {
            session.issue(deleteCmd);
            versionHandler.editOperationWritten(seDelete);
        } catch (IOException e) {
            versionHandler.editOperationFailed(seDelete);
            throw e;
        }

    }

    private void fireAdded(final SimpleFeature addedFeature) {
        final String typeName = featureType.getTypeName();
        final BoundingBox bounds = addedFeature.getBounds();
        final ReferencedEnvelope referencedEnvelope;
        if (bounds instanceof ReferencedEnvelope) {
            referencedEnvelope = (ReferencedEnvelope) bounds;
        } else {
            referencedEnvelope = new ReferencedEnvelope(bounds);
        }
        doFireFeaturesAdded(typeName, referencedEnvelope);
    }

    private void fireChanged(final SimpleFeature changedFeature) {
        final String typeName = featureType.getTypeName();
        final BoundingBox bounds = changedFeature.getBounds();
        final ReferencedEnvelope referencedEnvelope;
        if (bounds instanceof ReferencedEnvelope) {
            referencedEnvelope = (ReferencedEnvelope) bounds;
        } else {
            referencedEnvelope = new ReferencedEnvelope(bounds);
        }
        doFireFeaturesChanged(typeName, referencedEnvelope);
    }

    private void fireRemoved(final SimpleFeature removedFeature) {
        final String typeName = featureType.getTypeName();
        final BoundingBox bounds = removedFeature.getBounds();
        final ReferencedEnvelope referencedEnvelope;
        if (bounds instanceof ReferencedEnvelope) {
            referencedEnvelope = (ReferencedEnvelope) bounds;
        } else {
            referencedEnvelope = new ReferencedEnvelope(bounds);
        }
        doFireFeaturesRemoved(typeName, referencedEnvelope);
    }

    protected abstract void doFireFeaturesAdded(String typeName, ReferencedEnvelope bounds);

    protected abstract void doFireFeaturesChanged(String typeName, ReferencedEnvelope bounds);

    protected abstract void doFireFeaturesRemoved(String typeName, ReferencedEnvelope bounds);

    /**
     * @see FeatureWriter#write()
     */
    public void write() throws IOException {
        // final ArcSDEPooledConnection connection = getConnection();
        final SeLayer layer = getLayer();
        if (isNewlyCreated(feature)) {
            Number newId;
            try {
                newId = insertSeRow(feature, layer);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error inserting " + feature + ": " + e.getMessage(), e);
                throw e;
            }
            MutableFIDFeature mutableFidFeature = (MutableFIDFeature) feature;
            String id = featureType.getTypeName() + "." + newId.longValue();
            mutableFidFeature.setID(id);
            fireAdded(mutableFidFeature);
        } else {
            try {
                updateRow(feature, layer);
                fireChanged(feature);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error updating " + feature + ": " + e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * Updates the contents of a Feature in the database.
     * <p>
     * The db row to modify is obtained from the feature id.
     * </p>
     * 
     * @param modifiedFeature the newly create Feature to insert.
     * @param layer the layer where to insert the feature.
     * @param session the connection to use for the insert operation. Its auto commit mode
     *            determines whether the operation takes effect immediately or not.
     * @throws IOException
     * @throws SeException if thrown by any sde stream method
     * @throws IOException
     */
    private void updateRow(final SimpleFeature modifiedFeature, final SeLayer layer)
            throws IOException {

        final SeUpdate updateStream = (SeUpdate) createStream(SeUpdate.class);
        // updateStream.setWriteMode(true);

        final SeCoordinateReference seCoordRef = layer.getCoordRef();

        final LinkedHashMap<Integer, String> mutableColumns = getUpdatableColumnNames();
        final String[] rowColumnNames = new ArrayList<String>(mutableColumns.values())
                .toArray(new String[0]);
        final String typeName = featureType.getTypeName();
        final String fid = modifiedFeature.getID();
        final long numericFid = ArcSDEAdapter.getNumericFid(fid);
        final SeObjectId seObjectId = new SeObjectId(numericFid);

        final Command<Void> updateCmd = new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                try {
                    final SeRow row = updateStream.singleRow(seObjectId, typeName, rowColumnNames);

                    setRowProperties(modifiedFeature, seCoordRef, mutableColumns, row);
                    updateStream.execute();
                    // updateStream.flushBufferedWrites();
                } finally {
                    updateStream.close();
                }
                return null;
            }
        };

        try {
            session.issue(updateCmd);
            versionHandler.editOperationWritten(updateStream);
        } catch (NoSuchElementException e) {
            versionHandler.editOperationFailed(updateStream);
            throw e;
        } catch (IOException e) {
            versionHandler.editOperationFailed(updateStream);
            throw e;
        }
    }

    /**
     * Inserts a feature into an SeLayer.
     * 
     * @param newFeature the newly create Feature to insert.
     * @param layer the layer where to insert the feature.
     * @param session the connection to use for the insert operation. Its auto commit mode
     *            determines whether the operation takes effect immediately or not.
     * @throws IOException
     */
    private Number insertSeRow(final SimpleFeature newFeature, final SeLayer layer)
            throws IOException {

        final SeCoordinateReference seCoordRef = layer.getCoordRef();

        // this returns only the mutable attributes
        final LinkedHashMap<Integer, String> insertColumns = getInsertableColumnNames();
        final SeInsert insertStream = (SeInsert) createStream(SeInsert.class);

        final Command<Number> insertCmd = new Command<Number>() {

            @Override
            public Number execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                final SeRow row;
                Number newId;

                // ensure we get the next sequence id when the fid is user managed
                // and include it in the attributes to set
                if (fidReader instanceof FIDReader.UserManagedFidReader) {
                    newId = getNextAvailableUserManagedId();
                    final int rowIdIndex = fidReader.getColumnIndex();
                    newFeature.setAttribute(rowIdIndex, newId);
                }
                String[] rowColumnNames = new ArrayList<String>(insertColumns.values())
                        .toArray(new String[0]);
                String typeName = featureType.getTypeName();
                insertStream.intoTable(typeName, rowColumnNames);
                insertStream.setWriteMode(true);
                row = insertStream.getRowToSet();

                setRowProperties(newFeature, seCoordRef, insertColumns, row);
                insertStream.execute();

                if (fidReader instanceof FIDReader.SdeManagedFidReader) {
                    SeObjectId newRowId = insertStream.lastInsertedRowId();
                    newId = Long.valueOf(newRowId.longValue());
                } else {
                    throw new DataSourceException("fid reader is not user nor sde managed");
                }

                insertStream.flushBufferedWrites(); // jg: my customer wanted this uncommented
                insertStream.close();
                return newId;
            }
        };

        final Number newId;

        try {
            newId = session.issue(insertCmd);
            versionHandler.editOperationWritten(insertStream);
        } catch (IOException e) {
            versionHandler.editOperationFailed(insertStream);
            throw e;
        }

        // TODO: handle SHAPE fid strategy (actually such a table shouldn't be
        // editable)
        return newId;
    }

    /**
     * Sets the SeRow property values by index, taking the index from the mutableColumns keys and
     * the values from <code>feature</code>, using the mutableColumns values to get the feature
     * properties by name.
     * <p>
     * This method is intended to be called from inside a
     * {@link Command#execute(Session, SeConnection)} method
     * </p>
     * 
     * @param feature the Feature where to get the property values from
     * @param seCoordRef
     * @param mutableColumns
     * @param row
     * @throws SeException
     * @throws IOException
     */
    private static void setRowProperties(final SimpleFeature feature,
            final SeCoordinateReference seCoordRef,
            Map<Integer, String> mutableColumns,
            final SeRow row) throws SeException, IOException {

        // Now set the values for the new row here...
        int seRowIndex;
        String attName;
        Object value;
        for (Map.Entry<Integer, String> entry : mutableColumns.entrySet()) {
            seRowIndex = entry.getKey().intValue();
            attName = entry.getValue();
            value = feature.getAttribute(attName);
            setRowValue(row, seRowIndex, value, seCoordRef, attName);
        }
    }

    /**
     * Called when the layer row id is user managed to ask ArcSDE for the next available ID.
     * 
     * @return
     * @throws IOException
     * @throws SeException
     */
    private Number getNextAvailableUserManagedId() throws IOException, SeException {

        // TODO: refactor, this is expensive to do for each row to insert
        // TODO: refactor to some sort of strategy object like done for
        // FIDReader
        final SeTable table = getTable();
        // ArcSDE JavaDoc only says: "Returns a range of row id values"
        // http://edndoc.esri.com/arcsde/9.1/java_api/docs/com/esri/sde/sdk/client/setable.html#getIds(int)
        // I've checked empirically it is to return a range of available ids
        final SeTableIdRange ids = table.getIds(1);
        final SeObjectId startId = ids.getStartId();
        final long id = startId.longValue();
        final Long newId = Long.valueOf(id);

        final AttributeDescriptor rowIdAtt = featureType.getAttribute(fidReader.getFidColumn());
        final Class<?> binding = rowIdAtt.getType().getBinding();
        final Number userFidValue;
        if (Long.class == binding) {
            userFidValue = newId;
        } else if (Integer.class == binding) {
            userFidValue = Integer.valueOf(newId.intValue());
        } else if (Double.class == binding) {
            userFidValue = new Double(newId.doubleValue());
        } else if (Float.class == binding) {
            userFidValue = new Float(newId.floatValue());
        } else {
            throw new IllegalArgumentException("Can't handle a user managed row id of type "
                    + binding);
        }

        return userFidValue;
    }

    /**
     * Used to set a value on an SeRow object. The values is converted to the appropriate type based
     * on an inspection of the SeColumnDefintion object.
     * <p>
     * This method is intended to be called from inside a
     * {@link Command#execute(Session, SeConnection)} method
     * </p>
     * 
     * @param row
     * @param index
     * @param convertedValue
     * @param coordRef
     * @param attName for feedback purposes only in case of failure
     * @throws IOException if failed to set the row value
     */
    private static void setRowValue(final SeRow row,
            final int index,
            final Object value,
            final SeCoordinateReference coordRef,
            final String attName) throws IOException {

        try {
            final SeColumnDefinition seColumnDefinition = row.getColumnDef(index);

            final int colType = seColumnDefinition.getType();

            // the actual value to be set, converted to the appropriate type where
            // needed
            Object convertedValue = value;
            if (colType == SeColumnDefinition.TYPE_INT16) {
                convertedValue = Converters.convert(convertedValue, Short.class);
                row.setShort(index, (Short) convertedValue);
            } else if (colType == SeColumnDefinition.TYPE_INT32) {
                convertedValue = Converters.convert(convertedValue, Integer.class);
                row.setInteger(index, (Integer) convertedValue);
            } else if (colType == SeColumnDefinition.TYPE_INT64) {
                convertedValue = Converters.convert(convertedValue, Long.class);
                row.setLong(index, (Long) convertedValue);
            } else if (colType == SeColumnDefinition.TYPE_FLOAT32) {
                convertedValue = Converters.convert(convertedValue, Float.class);
                row.setFloat(index, (Float) convertedValue);
            } else if (colType == SeColumnDefinition.TYPE_FLOAT64) {
                convertedValue = Converters.convert(convertedValue, Double.class);
                row.setDouble(index, (Double) convertedValue);
            } else if (colType == SeColumnDefinition.TYPE_STRING
                    || colType == SeColumnDefinition.TYPE_NSTRING
                    || colType == SeColumnDefinition.TYPE_CLOB
                    || colType == SeColumnDefinition.TYPE_NCLOB) {
                convertedValue = Converters.convert(convertedValue, String.class);
                row.setString(index, (String) convertedValue);
            } else if (colType == SeColumnDefinition.TYPE_DATE) {
                // @todo REVISIT: is converters already ready for date->calendar?
                if (convertedValue != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime((Date) convertedValue);
                    row.setTime(index, calendar);
                } else {
                    row.setTime(index, null);
                }
            } else if (colType == SeColumnDefinition.TYPE_SHAPE) {
                if (convertedValue != null) {
                    final Geometry geom = (Geometry) convertedValue;
                    IsValidOp validator = new IsValidOp(geom);
                    if (!validator.isValid()) {
                        TopologyValidationError validationError = validator.getValidationError();
                        String validationErrorMessage = validationError.getMessage();
                        Coordinate coordinate = validationError.getCoordinate();
                        String errorMessage = "Topology validation error at or near point "
                                + coordinate + ": " + validationErrorMessage;
                        throw new DataSourceException("Invalid geometry passed for " + attName
                                + "\n Geomerty: " + geom + "\n" + errorMessage);
                    }
                    ArcSDEGeometryBuilder geometryBuilder;
                    geometryBuilder = ArcSDEGeometryBuilder.builderFor(geom.getClass());
                    SeShape shape = geometryBuilder.constructShape(geom, coordRef);
                    row.setShape(index, shape);
                } else {
                    row.setShape(index, null);
                }
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    /**
     * Returns the row index and column names for all the mutable properties in the sde layer. That
     * is, those properties whose type is not
     * {@link SeRegistration#SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE}, which are used as row id
     * columns managed by arcsde.
     * 
     * @return a map keyed by mutable column name and valued by the index of the mutable column name
     *         in the SeTable structure
     * @throws IOException
     * @throws NoSuchElementException
     */
    private LinkedHashMap<Integer, String> getUpdatableColumnNames() throws NoSuchElementException,
            IOException {
        if (mutableColumnNames == null) {
            // We are going to inspect the column defintions in order to
            // determine which attributes are actually mutable...
            final String typeName = this.featureType.getTypeName();
            final SeColumnDefinition[] columnDefinitions = Session.issueDescribe(session, typeName);
            final String shapeAttributeName;
            final SeLayer layer = getLayer();
            try {
                shapeAttributeName = layer.getShapeAttributeName(SeLayer.SE_SHAPE_ATTRIBUTE_FID);
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }

            // use LinkedHashMap to respect column order
            LinkedHashMap<Integer, String> columnList = new LinkedHashMap<Integer, String>();

            SeColumnDefinition columnDefinition;
            String columnName;
            int usedIndex = 0;
            for (int actualIndex = 0; actualIndex < columnDefinitions.length; actualIndex++) {
                columnDefinition = columnDefinitions[actualIndex];
                columnName = columnDefinition.getName();
                // this is an attribute added to the featuretype
                // solely to support FIDs. It isn't an actual attribute
                // on the underlying SDE table, and as such it can't
                // be written to. Skip it!
                if (columnName.equals(shapeAttributeName)) {
                    continue;
                }

                // ignore SeColumns for which we don't have a known mapping
                final int sdeType = columnDefinition.getType();
                if (SeColumnDefinition.TYPE_SHAPE != sdeType
                        && null == ArcSDEAdapter.getJavaBinding(new Integer(sdeType))) {
                    continue;
                }

                // We need to exclude read only types from the set of "mutable"
                // column names.
                final short rowIdType = columnDefinition.getRowIdType();
                if (SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE == rowIdType) {
                    continue;
                }

                columnList.put(Integer.valueOf(usedIndex), columnName);
                // only increment usedIndex if we added a mutable column to
                // the list
                usedIndex++;
            }
            this.mutableColumnNames = columnList;
        }

        return this.mutableColumnNames;
    }

    private LinkedHashMap<Integer, String> getInsertableColumnNames()
            throws NoSuchElementException, IOException {
        if (insertableColumnNames == null) {
            // We are going to inspect the column defintions in order to
            // determine which attributes are actually mutable...
            String typeName = this.featureType.getTypeName();
            final SeColumnDefinition[] columnDefinitions = Session.issueDescribe(session, typeName);

            // use LinkedHashMap to respect column order
            LinkedHashMap<Integer, String> columnList = new LinkedHashMap<Integer, String>();

            SeColumnDefinition columnDefinition;
            String columnName;
            int usedIndex = 0;
            for (int actualIndex = 0; actualIndex < columnDefinitions.length; actualIndex++) {
                columnDefinition = columnDefinitions[actualIndex];
                columnName = columnDefinition.getName();

                if (fidReader instanceof FIDReader.SdeManagedFidReader) {
                    if (columnName.equals(fidReader.getFidColumn()))
                        continue;
                }

                // ignore SeColumns for which we don't have a known mapping
                final int sdeType = columnDefinition.getType();
                if (SeColumnDefinition.TYPE_SHAPE != sdeType
                        && null == ArcSDEAdapter.getJavaBinding(Integer.valueOf(sdeType))) {
                    continue;
                }

                columnList.put(Integer.valueOf(usedIndex), columnName);
                usedIndex++;
            }
            this.insertableColumnNames = columnList;
        }

        return this.insertableColumnNames;
    }

    private SeTable getTable() throws IOException {
        if (this.cachedTable == null) {
            // final ArcSDEPooledConnection connection = getConnection();
            final String typeName = this.featureType.getTypeName();
            final SeTable table = Session.issueGetTable(session, typeName);
            this.cachedTable = table;
        }
        return this.cachedTable;
    }

    private SeLayer getLayer() throws IOException {
        if (this.cachedLayer == null) {
            // final ArcSDEPooledConnection connection = getConnection();
            final String typeName = this.featureType.getTypeName();
            final SeLayer layer = Session.issueGetLayer(session, typeName);
            this.cachedLayer = layer;
        }
        return this.cachedLayer;
    }

    /**
     * Creates a feature id for a new feature; the feature id is compound of the
     * {@value #NEW_FID_PREFIX} plus a UUID.
     * 
     * @return
     */
    private String newFid() {
        return NEW_FID_PREFIX + UUID.randomUUID();
    }

    /**
     * Checks if <code>feature</code> has been created by this writer
     * <p>
     * A Feature is created but not yet inserted if its id starts with {@link #NEW_FID_PREFIX}
     * </p>
     * 
     * @param aFeature
     * @return
     */
    private final boolean isNewlyCreated(SimpleFeature aFeature) {
        final String id = aFeature.getID();
        return id.startsWith(NEW_FID_PREFIX);
    }

    public Session getSession() {
        return session;
    }
}