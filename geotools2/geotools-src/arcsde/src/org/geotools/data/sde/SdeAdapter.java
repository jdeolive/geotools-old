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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author not attributable
 * @version 0.1
 */
public class SdeAdapter
{
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(
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

    /** To create the sql where statement */
    private SQLEncoderSDE sqlEncoder = new SQLEncoderSDE();

    /** To create the array of sde spatial filters */
    private GeometryEncoderSDE geometryEncoder = new GeometryEncoderSDE();

    /** DOCUMENT ME!  */
    private SeSqlConstruct sdeSqlConstruct = null;

    /**
     * Creates a new SdeTypeAdapter object.
     */
    public SdeAdapter()
    {
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
    public static AttributeType[] mapSdeTypes(SeLayer sdeLayer,
        SeColumnDefinition[] colDefs) throws DataSourceException
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
                int seShapeType = sdeLayer.getShapeTypes();
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

    /**
     * DOCUMENT ME!
     *
     * @param seShapeType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
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

    /**
     * DOCUMENT ME!
     *
     * @param fSource DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SeQuery createSeQuery(SdeFeatureSource fSource, Query query)
        throws DataSourceException
    {
        String[] queryColumns = getQueryColumns(query, fSource.getSchema());

        return createSeQuery(fSource, queryColumns, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param fSource DOCUMENT ME!
     * @param queryColumns DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SeQuery createSeQuery(SdeFeatureSource fSource,
        String[] queryColumns, Query query) throws DataSourceException
    {
        SeQuery sdeQuery = null;
        SdeDataStore store = (SdeDataStore) fSource.getDataStore();
        SeLayer sdeLayer = fSource.getLayer();

        sdeSqlConstruct = new SeSqlConstruct();

        String[] tables = { sdeLayer.getTableName() };
        sdeSqlConstruct.setTables(tables);

        Filter filter = query.getFilter();
        SQLUnpacker unpacker = new SQLUnpacker(sqlEncoder.getCapabilities());
        unpacker.unPackAND(filter);

        Filter sqlFilter = unpacker.getSupported();
        Filter unsupportedFilter = unpacker.getUnSupported();
        unpacker = new SQLUnpacker(geometryEncoder.getCapabilities());
        unpacker.unPackAND(unsupportedFilter);

        Filter geometryFilter = unpacker.getSupported();
        unsupportedFilter = unpacker.getUnSupported();

        //figure out which of the filter we can use.
        if (sqlFilter != null)
        {
            try
            {
                String where = sqlEncoder.encode(sqlFilter);
                LOGGER.fine("applying where clause: '" + where + "'");
                sdeSqlConstruct.setWhere(where);
            }
            catch (SQLEncoderException sqle)
            {
                String message = "Encoder error" + sqle.getMessage();
                LOGGER.warning(message);
                throw new DataSourceException(message, sqle);
            }
        }

        SeConnection sdeConn = null;

        try
        {
            sdeConn = store.getConnectionPool().getConnection();
            sdeQuery = new SeQuery(sdeConn, queryColumns, sdeSqlConstruct);
        }
        catch (SeException ex)
        {
            throw new DataSourceException("Cannot create the SDE Query: "
                + ex.getMessage(), ex);
        }
        finally
        {
            store.getConnectionPool().release(sdeConn);
        }

        if (geometryFilter != null)
        {
            try
            {
                geometryEncoder.setLayer(sdeLayer);
                geometryEncoder.encode(geometryFilter);

                SeFilter[] sdeSpatialFilters = geometryEncoder
                    .getSpatialFilters();

                if ((sdeSpatialFilters != null)
                        && (sdeSpatialFilters.length > 0))
                {
                    LOGGER.fine("applying " + sdeSpatialFilters.length
                        + " spatial filters ");

                    try
                    {
                        sdeQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
                            false, sdeSpatialFilters);
                    }
                    catch (SeException ex)
                    {
                        throw new DataSourceException(
                            "Cannot apply spatial constraints: "
                            + ex.getMessage(), ex);
                    }
                }
            }

            catch (GeometryEncoderException ex)
            {
                String message = "Encoder error" + ex.getMessage();
                LOGGER.warning(message);
                throw new DataSourceException(message, ex);
            }
        }

        return sdeQuery;
    }

    /**
     * Builds a String array with the names of the attributes to query over an
     * ArcSDE FeatureClass. Usefull to construct <code>SeQuery</code>'s
     *
     * @param query the query from wich this method will obtain the list of
     *        attribute names
     * @param schema the full schema of the queried FeatureClass to construct
     *        the list of columns if query does not specifies a subset of
     *        attributes to query
     *
     * @return a String array with the attribute names of a <code>query</code>
     */
    public String[] getQueryColumns(Query query, FeatureType schema)
    {
        //attributes to retrieve
        String[] qcols = null;

        if (query.retrieveAllProperties()) //retrieve all properties
        {
            AttributeType[] atts = schema.getAttributeTypes();
            qcols = new String[atts.length];

            for (int i = 0; i < atts.length; i++)
                qcols[i] = atts[i].getName();
        }
        else
        {
            qcols = query.getPropertyNames();
        }

        return qcols;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     * @param schema DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected FeatureType getSchema(Query query, FeatureType schema)
        throws DataSourceException
    {
        if ((query == null) || query.retrieveAllProperties())
        {
            return schema;
        }

        String[] propertyNames = query.getPropertyNames();
        int countQueried = propertyNames.length;

        AttributeType[] attributes = new AttributeType[countQueried];
        AttributeType[] schemaAtts = schema.getAttributeTypes();
        FeatureType type = null;

        for (int i = 0; i < countQueried; i++)
        {
            for (int j = 0; j < schemaAtts.length; j++)
            {
                if (schemaAtts[j].getName().equalsIgnoreCase(propertyNames[i]))
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
            throw new DataSourceException("Cannot create subtype: "
                + ex.getMessage(), ex);
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
     * @return DOCUMENT ME!
     */
    public SeSqlConstruct getSdeSqlConstruct()
    {
        return sdeSqlConstruct;
    }
}
