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
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceMetaData;
import org.geotools.data.Query;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import java.util.*;
import java.util.logging.Logger;


/**
 * this is the old style, memory consuming, ArcSDE  DataSource for geotools. By
 * now, it supports attribute filtering, as well as bounding box and  geometry
 * intersection filtering too.
 *
 * <p>
 * The streaming version of this datasource is not included in this version for
 * to avoid current API conflicts such as throwing some
 * UnsupportedOperationExceptions. If you're interested in the streaming
 * version, look forward to the data-exp branch  in geotools2 CVS repository
 * (well, I think to start commiting stuff to this branch about monday
 * 2003-10-13 due to some vacation days from now on )
 * </p>
 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public class SdeDataSource extends AbstractDataSource
{
    /** package's logger */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.sde");

    /** mappings of SDE attribute's types to Java ones */
    private static final Map sdeTypes = new java.util.HashMap();

    static
    {
        sdeTypes.put(new Integer(SeColumnDefinition.TYPE_STRING), String.class);

        sdeTypes.put(new Integer(SeColumnDefinition.TYPE_SMALLINT), Short.class);

        sdeTypes.put(new Integer(SeColumnDefinition.TYPE_INTEGER), Integer.class);

        sdeTypes.put(new Integer(SeColumnDefinition.TYPE_FLOAT), Float.class);

        sdeTypes.put(new Integer(SeColumnDefinition.TYPE_DOUBLE), Double.class);

        sdeTypes.put(new Integer(SeColumnDefinition.TYPE_DATE), Date.class);

        sdeTypes.put(new Integer(SeColumnDefinition.TYPE_BLOB), byte[].class);

        //SeColumnDefinition.TYPE_RASTER is not supported...
    }

    /** where to get sde connections from */
    private SdeConnectionPool connectionPool;

    /** requested feature class name */
    private String tableName;

    /** SDE feature class object */
    private SeLayer sdeLayer;

    /** SDE table object */
    private SeTable sdeTable;

    /** cached datasource's schema */
    private FeatureType schema;

    /**
     * flag to stop loading features if abortLoad() is called while filling a
     * FeatureCollection
     */
    private boolean abortLoading = false;

    /**
     * Constructs a valid ArcSDE data source cheking for existence in the
     * backend SDE database. This class constructor is package protected to
     * ensure instantiation from an <code>SdeDataSourceFactory</code>, wich is
     * responsible of providing a valid <code>SdeConnectionPool</code> to get
     * sde connections from.
     *
     * <p></p>
     *
     * @param connPool
     * @param sdeTableName
     *
     * @throws DataSourceException if a DataSourceException is thrown while
     *         getting the SeLayer from the SDE database or if a layer named
     *         <code>sdeTableName</code> is not found.
     * @throws NullPointerException DOCUMENT ME!
     */
    protected SdeDataSource(SdeConnectionPool connPool, String sdeTableName)
        throws DataSourceException
    {
        if (connPool == null)
        {
            throw new NullPointerException(
                "a valid SDE connection pool mus be provided");
        }

        if (sdeTableName == null)
        {
            throw new NullPointerException("an SDE table name be provided");
        }

        this.connectionPool = connPool;

        this.tableName = sdeTableName;

        getSdeLayer();

        SeConnection sdeConn = null;

        try
        {
            sdeConn = getConnectionPool().getConnection();

            getSdeTable(sdeConn);
        }
        catch (DataSourceException ex)
        {
            throw ex;
        }
        finally
        {
            getConnectionPool().release(sdeConn);
        }
    }

    //

    /**
     * Creates the a metaData object.
     *
     * <p></p>
     *
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData()
    {
        MetaDataSupport md = new MetaDataSupport();

        md.setSupportsGetBbox(true);

        md.setFastBbox(true);

        md.setSupportsAbort(true);

        md.setSupportsAdd(false);

        md.setSupportsModify(false);

        md.setSupportsRemove(false);

        md.setSupportsRollbacks(false);

        md.setSupportsSetFeatures(false);

        return md;
    }

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws DataSourceException if bounds could not be calculated
     * @throws UnsupportedOperationException if the datasource can't get
     *         bounds.
     */
    public Envelope getBounds()
        throws DataSourceException, UnsupportedOperationException
    {
        SeLayer seLayer = getSdeLayer();
        Envelope bounds = null;
        SeExtent ext = seLayer.getExtent();

        bounds = new Envelope(ext.getMinX(), ext.getMaxX(), ext.getMinY(),
                ext.getMaxY());

        return bounds;
    }

    /**
     * queries the ArcSDE server to obtain the featureclass metadata and
     * construct its schema
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public FeatureType getSchema() throws DataSourceException
    {
        if (schema != null)
        {
            return schema;
        }

        SeConnection sdeConn = null;

        try
        {
            sdeConn = getConnectionPool().getConnection();

            return getSchema(sdeConn);
        }
        catch (DataSourceException ex)
        {
            throw ex;
        }
        finally
        {
            getConnectionPool().release(sdeConn);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param sdeConnection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected FeatureType getSchema(SeConnection sdeConnection)
        throws DataSourceException
    {
        if (schema != null)
        {
            return schema;
        }

        SeColumnDefinition[] colDefs = null;

        try
        {
            SeTable table = getSdeTable(sdeConnection);

            colDefs = table.describe();
        }
        catch (SeException ex)
        {
            throw new DataSourceException("Exception getting table def for "
                + tableName + ": " + ex.getMessage(), ex);
        }

        schema = getSchema(colDefs);

        return schema;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sdeConn DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeTable getSdeTable(SeConnection sdeConn)
        throws DataSourceException
    {
        if (this.sdeTable != null)
        {
            return sdeTable;
        }

        SeTable table = null;

        try
        {
            String qName = getSdeLayer().getQualifiedName();

            table = new SeTable(sdeConn, qName);

            this.sdeTable = table;
        }
        catch (SeException ex)
        {
            throw new DataSourceException("table " + tableName
                + " not found on catalog", ex);
        }

        return sdeTable;
    }

    /**

     *

     */
    protected FeatureType getSchema(Query query) throws DataSourceException
    {
        if ((query == null) || query.retrieveAllProperties())
        {
            return getSchema();
        }

        return getSchema(query.getPropertyNames());
    }

    /**

     *

     */
    protected FeatureType getSchema(String[] propertyNames)
        throws DataSourceException
    {
        int countQueried = propertyNames.length;

        FeatureType schema = getSchema();

        AttributeType[] attributes = new AttributeType[countQueried];

        AttributeType[] schemaAtts = schema.getAttributeTypes();

        FeatureType type = null;

        for (int i = 0; i < countQueried; i++)
        {
            for (int j = 0; j < schemaAtts.length; j++)
            {
                if (schemaAtts[j].getName().equals(propertyNames[i]))
                {
                    attributes[i] = schemaAtts[j];

                    break;
                }
            }
        }

        try
        {
            type = FeatureTypeFactory.newFeatureType(attributes,
                    schema.getTypeName());
        }
        catch (SchemaException ex)
        {
            throw new DataSourceException("creating FeatureType: "
                + ex.getMessage(), ex);
        }
        catch (FactoryConfigurationError ex)
        {
            throw new DataSourceException("can't create FeatureType: "
                + ex.getMessage(), ex);
        }

        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param colDefs DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected FeatureType getSchema(SeColumnDefinition[] colDefs)
        throws DataSourceException
    {
        AttributeType[] types = mapSdeTypes(colDefs);

        FeatureType type = null;

        try
        {
            type = FeatureTypeFactory.newFeatureType(types, this.tableName);
        }
        catch (SchemaException ex)
        {
        }
        catch (FactoryConfigurationError ex)
        {
        }

        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param colDefs DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private AttributeType[] mapSdeTypes(SeColumnDefinition[] colDefs)
        throws DataSourceException
    {
        int nCols = colDefs.length;

        AttributeType[] types = new AttributeType[nCols];

        AttributeType type = null;

        Class typeClass;

        boolean isNilable = true;

        for (int i = 0; i < nCols; i++)
        {
            Integer sdeType = new Integer(colDefs[i].getType());

            if (sdeType.intValue() == SeColumnDefinition.TYPE_SHAPE)
            {
                int seShapeType = getSdeLayer().getShapeTypes();

                typeClass = getGeometryType(seShapeType);

                isNilable = (seShapeType & SeLayer.SE_NIL_TYPE_MASK) == SeLayer.SE_NIL_TYPE_MASK;
            }
            else if (sdeType.intValue() == SeColumnDefinition.TYPE_RASTER)
            {
                throw new DataSourceException(
                    "Raster columns are not supported");
            }
            else
            {
                typeClass = (Class) sdeTypes.get(sdeType);
            }

            type = AttributeTypeFactory.newAttributeType(colDefs[i].getName(),
                    typeClass, isNilable);

            types[i] = type;
        }

        return types;
    }

    //
    public static Class getGeometryType(int seShapeType)
    {
        /*

                      public static final int SE_NIL_TYPE_MASK = 1;

                      public static final int SE_POINT_TYPE_MASK = 2;

                      public static final int SE_LINE_TYPE_MASK = 4;

                      public static final int SE_SIMPLE_LINE_TYPE_MASK = 8;

                      public static final int SE_AREA_TYPE_MASK = 16;

                      public static final int SE_MULTIPART_TYPE_MASK = 262144;

         */
        Class clazz = com.vividsolutions.jts.geom.Geometry.class;

        if ((seShapeType & SeLayer.SE_POINT_TYPE_MASK) == SeLayer.SE_POINT_TYPE_MASK)
        {
            if ((seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) == SeLayer.SE_MULTIPART_TYPE_MASK)
            {
                clazz = com.vividsolutions.jts.geom.MultiPoint.class;
            }
            else
            {
                clazz = com.vividsolutions.jts.geom.Point.class;
            }
        }
        else if ((seShapeType & SeLayer.SE_SIMPLE_LINE_TYPE_MASK) == SeLayer.SE_SIMPLE_LINE_TYPE_MASK)
        {
            if ((seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) == SeLayer.SE_MULTIPART_TYPE_MASK)
            {
                clazz = com.vividsolutions.jts.geom.MultiLineString.class;
            }
            else
            {
                clazz = com.vividsolutions.jts.geom.LineString.class;
            }
        }
        else if ((seShapeType & SeLayer.SE_LINE_TYPE_MASK) == SeLayer.SE_LINE_TYPE_MASK)
        {
            if ((seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) == SeLayer.SE_MULTIPART_TYPE_MASK)
            {
                clazz = com.vividsolutions.jts.geom.MultiLineString.class;
            }
            else
            {
                clazz = com.vividsolutions.jts.geom.LineString.class;
            }
        }
        else if ((seShapeType & SeLayer.SE_AREA_TYPE_MASK) == SeLayer.SE_AREA_TYPE_MASK)
        {
            if ((seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) == SeLayer.SE_MULTIPART_TYPE_MASK)
            {
                /**
                 * @task TODO: strongly test returning Polygon, it seems that
                 *       SDE polygons are OGC multipolygons...
                 */
                clazz = com.vividsolutions.jts.geom.MultiPolygon.class;
            }
            else
            {
                clazz = com.vividsolutions.jts.geom.MultiPolygon.class;
            }
        }

        return clazz;
    }

    //

    /**
     * finds the <code>SeLayer</code> object named <code>tableName</code>
     *
     * @return the sde schema of tableName or <code>null</code> if thoes not
     *         exists on the database or the user has no SELECT privileges
     *         over it
     *
     * @throws DataSourceException if an <code>SeException</code> is thrown
     *         while quering the connection for the list of SDE tables
     */
    protected SeLayer getSdeLayer() throws DataSourceException
    {
        if (this.sdeLayer != null)
        {
            return this.sdeLayer;
        }

        SeLayer layer = null;

        Vector layers = this.getConnectionPool().getAvailableSdeLayers();

        for (Iterator it = layers.iterator(); it.hasNext();)
        {
            layer = (SeLayer) it.next();

            try
            {
                if (layer.getName().equalsIgnoreCase(tableName)
                        || layer.getQualifiedName().equalsIgnoreCase(tableName))
                {
                    this.sdeLayer = layer;

                    LOGGER.finer(sdeLayer.getName() + "'s coordinate system: "
                        + sdeLayer.getCoordRef().getCoordSysDescription());

                    return sdeLayer;
                }
            }
            catch (SeException ex)
            {
                throw new DataSourceException(
                    "Error getting layer's qualified name: " + ex.getMessage(),
                    ex);
            }
        }

        throw new DataSourceException("Feature type " + tableName
            + " not found on SDE catalog");
    }

    //

    /**
     * DOCUMENT ME!
     *
     * @param features
     * @param query
     *
     * @throws DataSourceException
     */
    public void getFeatures(FeatureCollection features, Query query)
        throws DataSourceException
    {
        SeConnection sdeConn = null;

        try
        {
            //sdeConn = getConnectionPool().getConnection();
            SdeFeatureReader reader = new SdeFeatureReader(query, this);

            while (!abortLoading && reader.hasNext())
            {
                features.add(reader.next());
            }
        }

        catch (DataSourceException ex)
        {
            throw ex;
        }
        catch (RuntimeException re)
        {
            //be sure we release the connection by catching unckecked exceptions
            //forcing to pass through the finally section
            throw re;
        }
        finally
        {
            abortLoading = false; //reset abort state

            //getConnectionPool().release(sdeConn);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    SdeConnectionPool getConnectionPool()
    {
        return this.connectionPool;
    }
}
