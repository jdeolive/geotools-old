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
package org.geotools.data.sde;

import com.esri.sde.sdk.client.*;
import org.geotools.data.*;
import org.geotools.factory.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public class SdeDataStore implements DataStore
{
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.sde");
    /** where to get sde connections from */
    private SdeConnectionPool connectionPool;

    /**
     * Creates a new SdeDataStore object wich will obtain connections to an
     * ArcSDE database through <code>connPool</code>
     *
     * @param connPool the ArcSDE connection pool from wich this DataStore will
     *        obtain connections to the ArcSDE database gateway
     *
     * @throws NullPointerException if an <code>SdeConnectionPool</code> is not
     *         passed as argument.
     */
    public SdeDataStore(SdeConnectionPool connPool)
    {
        if (connPool == null)
            throw new NullPointerException("null argument is not valid");

        this.connectionPool = connPool;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SdeConnectionPool getConnectionPool()
    {
        return connectionPool;
    }

    /**
    * Creates storage for a new <code>featureType</code>.
    *
    * <p>
    * The provided <code>featureType</code> we be accessable by the typeName
    * provided by featureType.getTypeName().
    * </p>
    *
    * <p>
    * Due to the current lack of stronger AttributeType's definitions,
    * some assumptions are made to the database table schema creation:
    * </p>
    *
    * @param featureType FetureType to add to DataStore
    *
    * @throws IOException If featureType cannot be created
    */
    public void createSchema(FeatureType featureType) throws IOException
    {
      SdeAdapter adapter = new SdeAdapter();
      SeColumnDefinition []sdeColumns = null;
      SeConnection sdeConn = null;
      String typeName = featureType.getTypeName();

      try {
        sdeConn = getConnectionPool().getConnection();

        SeLayer sdeLayer = new SeLayer(sdeConn);

        AttributeType geometryAtt = featureType.getDefaultGeometry();
        if(geometryAtt == null)
          throw new IllegalArgumentException("No geometry attribute has been defined for the new FeatureType");

        /*By creating a qualified table name with current user's name and
         *the name of the table to be created, "EXAMPLE", we establish the user
         *as the table owner in the backend database
         */
        LOGGER.info("creating new sde table " + typeName);
        String tableName = (sdeConn.getUser() + "." + typeName);
        SeTable table = new SeTable(sdeConn, tableName);
        sdeColumns = adapter.createSeColDefs(featureType);

        /*Create the table using the DBMS default configuration keyword.
         *Valid config keywords are found in the $SDEHOME\etc\dbtune.sde file.
         */
        table.create(sdeColumns, "DEFAULTS");

        /*Define the attributes of the spatial column*/
        sdeLayer.setSpatialColumnName(geometryAtt.getName());
        sdeLayer.setTableName(typeName);

        //get the or'ed mask of the shape types this layer will accept,
        //based on the geometry type of de default geometry
        int sdeShapeTypes = adapter.guessShapeTypes(geometryAtt);
        sdeLayer.setShapeTypes(sdeShapeTypes);

        //spatially enable the new table, allocating space for 100 initial
        //features and setting the estimated average number of points per feature
        LOGGER.finer("about to spatially enable the new table");
        sdeLayer.create(100, 4);
        LOGGER.finer("new table has been succesfully spatially enabled");
      }catch (SeException ex) {
        throw new DataSourceException(ex.getMessage(), ex);
      }finally{
        getConnectionPool().release(sdeConn);
      }
    }

    /**
     * DOCUMENT ME!
     *
     * @param parm1 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    protected FeatureWriter getFeatureWriter(String parm1)
        throws java.io.IOException
    {
        /**
         * @todo Implement this org.geotools.data.AbstractDataStore abstract
         *       method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method getFeatureWriter() not yet implemented.");
    }

    public FeatureWriter getFeatureWriter(String typeName, Transaction trans){
	  throw new java.lang.UnsupportedOperationException(
            "Method getFeatureWriter() not yet implemented.");
    }

     public FeatureWriter getFeatureWriterAppend(String typeName, Transaction trans){
	  throw new java.lang.UnsupportedOperationException(
            "Method getFeatureWriter() not yet implemented.");
    }

    /**
     * DOCUMENT ME!
     *
     * @return the list of full qualified feature class names on the ArcSDE
     *         database this DataStore works on. An ArcSDE full qualified
     *         class name is composed of three dot separated strings:
     *         "DATABASE.USER.CLASSNAME", wich is usefull enough to use it as
     *         namespace
     *
     * @throws RuntimeException if an exception occurs while retrieving the
     *         list of registeres feature classes on the backend, or while
     *         obtaining the full qualified name of one of them
     */
    public String[] getTypeNames()
    {
        String[] featureTypesNames = null;

        try
        {
            Vector sdeLayers = connectionPool.getAvailableSdeLayers();
            featureTypesNames = new String[sdeLayers.size()];

            String typeName;
            int i = 0;

            for (Iterator it = sdeLayers.iterator(); it.hasNext(); i++)
            {
                typeName = ((SeLayer) it.next()).getQualifiedName();
                featureTypesNames[i] = typeName;
            }
        }
        catch (SeException ex)
        {
            throw new RuntimeException("Exception while fetching layer name: "
                + ex.getMessage(), ex);
        }
        catch (DataSourceException ex)
        {
            throw new RuntimeException("Exception while getting layers list: "
                + ex.getMessage(), ex);
        }

        return featureTypesNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     */
    public FeatureType getSchema(String typeName) throws java.io.IOException
    {
        return getSchema(connectionPool.getSdeLayer(typeName));
    }

    /**
     * DOCUMENT ME!
     *
     * @param sdeLayer DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    protected FeatureType getSchema(SeLayer sdeLayer) throws IOException
    {
        SeColumnDefinition[] colDefs = null;
        String typeName = null;

        try
        {
            typeName = sdeLayer.getQualifiedName();

            SeTable table = connectionPool.getSdeTable(typeName);
            colDefs = table.describe();
        }
        catch (SeException ex)
        {
            IOException ioe = new IOException(
                    "Exception getting table def for " + typeName + ": "
                    + ex.getMessage());
            ioe.setStackTrace(ex.getStackTrace());
            throw ioe;
        }

        return getSchema(sdeLayer, colDefs);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sdeLayer DOCUMENT ME!
     * @param colDefs DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected FeatureType getSchema(SeLayer sdeLayer,
        SeColumnDefinition[] colDefs) throws DataSourceException
    {
        AttributeType[] types = SdeAdapter.createAttributeTypes(sdeLayer, colDefs);

        FeatureType type = null;

        try
        {
            type = FeatureTypeFactory.newFeatureType(types,
                    sdeLayer.getQualifiedName());
        }
        catch (SeException ex)
        {
            throw new DataSourceException(ex.getMessage(), ex);
        }
        catch (SchemaException ex)
        {
            throw new DataSourceException(ex.getMessage(), ex);
        }
        catch (FactoryConfigurationError ex)
        {
            throw new DataSourceException(ex.getMessage(), ex);
        }

        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    protected org.geotools.data.FeatureReader getFeatureReader(String typeName)
        throws IOException
    {
        return getFeatureReader(getSchema(typeName), Filter.ALL,
            Transaction.AUTO_COMMIT);
    }

    /**
     * Provides FeatureReader access.
     *
     * <p>
     * <b>Filter</b> is used as a Low level indication of constraints.
     * Implementations may resort to using a FilteredFeatureReader, or provide
     * their own optimizations.
     * </p>
     *
     * <p>
     * <b>FeatureType</b> provides a template for the returned FeatureReader
     * </p>
     *
     * <ul>
     * <li>
     * featureType.getTypeName(): used by JDBC as the table reference to query
     * against. Shapefile reader may need to store a lookup to required
     * filename.
     * </li>
     * <li>
     * featureType.getAttributeTypes(): describes the requested content. This
     * may be a subset of the complete FeatureType defined by the DataStore.
     * The actual query may need to retrieve additional attributes in order to
     * correctly apply the filter, although these should be hidden from the
     * returned FeatureReader.
     * </li>
     * </ul>
     *
     * <p>
     * <b>Transaction</b> to externalize DataStore state on a per Transaction
     * basis. The most common example is a JDBC datastore saving a Connection
     * for use across several FeatureReader requests. Similarly a Shapefile
     * reader may wish to redirect FeatureReader requests to a alternate
     * filename over the course of a Transaction.
     * </p>
     *
     * <p>
     * Open Questions:
     * </p>
     *
     * <ul>
     * <li>
     * Using FeatureType may be overkill, we may wish to simply use typeName
     * </li>
     * </ul>
     *
     *
     * @param featureType Describes the form of the returned Features
     * @param filter Describes constraints used to limit the query
     * @param transaction Transaction this query opperates against
     *
     * @return FeatureReader Allows Sequential Processing of featureType
     *
     * @throws IOException DOCUMENT ME!
     */
    public org.geotools.data.FeatureReader getFeatureReader(FeatureType featureType,
        Filter filter, Transaction transaction) throws IOException
    {
        assertGetReaderParams(featureType, filter, transaction);

        String typeName = featureType.getTypeName();
        SdeFeatureSource source = new SdeFeatureSource(this, typeName, featureType);
        FeatureResults results = source.getFeatures(filter);
        org.geotools.data.FeatureReader sdeReader = results.reader();

        return sdeReader;
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureSource getFeatureSource(String featureType)
        throws IOException
    {
        return new SdeFeatureSource(this, featureType);
    }

    /**
     * Access FeatureWriter for modification of the DataStore typeName.
     *
     * <p>
     * FeatureWriter will need to be limited to the FeatureTypes defined by the
     * DataStore, the easiest way to express this limitation is to the
     * FeatureType by a provided typeName.
     * </p>
     *
     * <p>
     * (If we did decide to support FeatureType this could be an entry point
     * for new table/shapefile creation.)
     * </p>
     *
     * <p>
     * The parameter <code>append</code> can be used to quickly add new content
     * to <code>typeName</code>.
     * </p>
     *
     * <p>
     * The returned FeatureWriter will return <code>false</code> for getNext()
     * when it reaches the end of the Query. You may use append() rather than
     * next() to allow additional modifications.
     * </p>
     *
     * @param typeName Indicates featureType to be modified
     * @param append <code>true</code> if FeatureWriter should advance to "end"
     * @param transaction Transaction this query opperates against
     *
     * @return FeatureReader Allows Sequential Processing of featureType
     *
     * @throws IOException DOCUMENT ME!
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public FeatureWriter getFeatureWriter(String typeName, boolean append,
        Transaction transaction) throws IOException
    {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Access FeatureWriter for modification of the DataStore contents.
     *
     * <p>
     * The constructed FeatureWriter will be placed at the start of the
     * provided <code>store</code>.
     * </p>
     *
     * <p>
     * FeatureWriter will need to be limited to the FeatureTypes defined by the
     * DataStore, the easiest way to express this limitation is to the
     * FeatureType by a provided typeName.
     * </p>
     *
     * <p>
     * If we did decide to support FeatureType this could be an entry point for
     * new table/shapefile creation.
     * </p>
     *
     * <p>
     * The returned FeatureWriter does not support the addition on new Features
     * to FeatureType (it would need to police your modifications to agree
     * with <code>filer</code>).  As such it will return <code>false</code>
     * for getNext() when it reaches the end of the Query.
     * </p>
     *
     * @param typeName Indicates featureType to be modified
     * @param filter constraints used to limit the modification
     * @param transaction Transaction this query opperates against
     *
     * @return FeatureWriter Allows Sequential Modification of featureType
     *
     * @throws IOException DOCUMENT ME!
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
        Transaction transaction) throws IOException
    {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * isolated assertion of getFeatureReader parameters
     *
     * @param featureType DOCUMENT ME!
     * @param filter DOCUMENT ME!
     * @param transaction DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    private void assertGetReaderParams(FeatureType featureType, Filter filter,
        Transaction transaction) throws IOException
    {
        if (filter == null)
        {
            throw new NullPointerException("getFeatureReader requires Filter: "
                + "did you mean Filter.NONE?");
        }

        if (featureType == null)
        {
            throw new NullPointerException(
                "getFeatureReader requires FeatureType: "
                + "use getSchema( typeName ) to aquire a FeatureType");
        }

        if (transaction == null)
        {
            throw new NullPointerException(
                "getFeatureReader requires Transaction: "
                + "did you mean to use Transaction.AUTO_COMMIT?");
        }
    }

    /**
     * by now just returs null. I'll implement locking in short
     */
    public LockingManager getLockingManager()
    {
      return null;
    }
    /**
     * DOCUMENT ME!
     *
     * @param msg DOCUMENT ME!
     */
    private static void log(String msg)
    {
        System.out.println(msg);
    }
}
