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
import com.vividsolutions.jts.geom.*;
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
 * Utility class to deal with SDE specifics such as creating SeQuery objects
 * from geotool's Query's, mapping SDE types to Java ones and JTS Geometries, etc.
 *
 * @author Gabriel Roldán
 * @version $Id: SdeAdapter.java,v 1.9 2003/11/19 17:50:11 groldan Exp $
 */
public class SdeAdapter
{
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.sde");

    /** DOCUMENT ME! */
    private static final String NO_SDE_TYPE_MATCH_MSG = "There are no an SDE type configured to match the type ";

    /** mappings of SDE attribute's types to Java ones */
    private static final Map sdeTypes = new HashMap();

    /** inverse of sdeTypes, maps Java types to SDE ones */
    private static final Map javaTypes = new HashMap();

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
        javaTypes.put(String.class,
            new SdeTypeDef(SeColumnDefinition.TYPE_STRING, 255, 0));
        javaTypes.put(Short.class,
            new SdeTypeDef(SeColumnDefinition.TYPE_SMALLINT, 4, 0));
        javaTypes.put(Integer.class,
            new SdeTypeDef(SeColumnDefinition.TYPE_INTEGER, 10, 0));
        javaTypes.put(Float.class,
            new SdeTypeDef(SeColumnDefinition.TYPE_FLOAT, 5, 2));
        javaTypes.put(Double.class,
            new SdeTypeDef(SeColumnDefinition.TYPE_DOUBLE, 15, 4));
        javaTypes.put(Date.class,
            new SdeTypeDef(SeColumnDefinition.TYPE_DATE, 1, 0));
        javaTypes.put(byte[].class,
            new SdeTypeDef(SeColumnDefinition.TYPE_BLOB, 1, 0));
        javaTypes.put(Number.class,
            new SdeTypeDef(SeColumnDefinition.TYPE_DOUBLE, 15, 4));
    }

    /** DOCUMENT ME! */
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
     * @param attribute DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public int guessShapeTypes(AttributeType attribute)
    {
        if (attribute == null)
            throw new IllegalArgumentException("null is not valid as argument");

        if (!attribute.isGeometry())
            throw new IllegalArgumentException(attribute.getName()
                + " is not a geometry attribute");

        Class geometryClass = attribute.getType();

        if (Geometry.class.isAssignableFrom(geometryClass))
            throw new IllegalArgumentException(geometryClass
                + " is not a valid Geometry class");

        int shapeTypes = 0;

        if (attribute.isNillable())
            shapeTypes |= SeLayer.SE_NIL_TYPE_MASK;

        if (GeometryCollection.class.isAssignableFrom(geometryClass))
        {
            shapeTypes |= SeLayer.SE_MULTIPART_TYPE_MASK;

            if (geometryClass == MultiPoint.class)
                shapeTypes |= SeLayer.SE_POINT_TYPE_MASK;
            else if (geometryClass == MultiLineString.class)
                shapeTypes |= SeLayer.SE_LINE_TYPE_MASK;
            else if (geometryClass == MultiPolygon.class)
                shapeTypes |= SeLayer.SE_AREA_TYPE_MASK;
            else
                throw new IllegalArgumentException(
                    "no SDE geometry mapping for " + geometryClass);
        }
        else
        {
            if (geometryClass == MultiPoint.class)
                shapeTypes |= SeLayer.SE_POINT_TYPE_MASK;
            else if (geometryClass == MultiLineString.class)
                shapeTypes |= SeLayer.SE_LINE_TYPE_MASK;
            else if (geometryClass == MultiPolygon.class)
                shapeTypes |= SeLayer.SE_AREA_TYPE_MASK;
            else
                throw new IllegalArgumentException(
                    "no SDE geometry mapping for " + geometryClass);
        }

        return shapeTypes;
    }

    /**
     *
     */
    public SeColumnDefinition[] createSeColDefs(FeatureType featureType)
        throws DataSourceException
    {
        int nCols = featureType.getAttributeCount();
        AttributeType[] atts = featureType.getAttributeTypes();
        SeColumnDefinition[] coldefs = new SeColumnDefinition[nCols];

        //new SeColumnDefinition( "Integer_Val", SeColumnDefinition.TYPE_INTEGER, 10, 0, isNullable);
        for (int i = 0; i < nCols; i++)
        {
            if (atts[i].isGeometry())
                continue;

            String attName = atts[i].getName();
            Class attClass = atts[i].getType();
            boolean nillable = atts[i].isNillable();

            SdeTypeDef sdeType = (SdeTypeDef) javaTypes.get(attClass);

            if (sdeType == null)
                throw new DataSourceException(NO_SDE_TYPE_MATCH_MSG
                    + attClass.getName());

            try
            {
                coldefs[i] = new SeColumnDefinition(attName,
                        sdeType.colDefType, sdeType.size, sdeType.scale,
                        nillable);
            }
            catch (SeException ex)
            {
                throw new DataSourceException(
                    "Cannot create the column definition named " + attName
                    + ": " + ex.getSeError().getSdeErrMsg(), ex);
            }
        }

        return coldefs;
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
    public static AttributeType[] createAttributeTypes(SeLayer sdeLayer,
        SeColumnDefinition[] wichCols) throws DataSourceException
    {
        boolean isNilable;
        int fieldLen;
        Object defValue;

        int nCols = wichCols.length;
        DefaultAttributeTypeFactory attFactory = new DefaultAttributeTypeFactory();
        AttributeType[] attTypes = new AttributeType[nCols];
        AttributeType attribute = null;
        Class typeClass;

        for (int i = 0; i < nCols; i++)
        {
            //well, once again, the "great" ArcSDE Java API seems to not provide
            //us as many information as we want. In fact, SeColumnDefinition
            //has a constructor with an argument to specify if an attribute
            //accepts null values, but DOES NOT HAVE a method to retrieve
            //such property... very very usefull, ESRI. (I really hope to
            //someone open my eyes and tell me how it can be obtained)
            isNilable = true;
            defValue = null;

            Integer sdeType = new Integer(wichCols[i].getType());
            fieldLen = wichCols[i].getSize();

            if (sdeType.intValue() == SeColumnDefinition.TYPE_SHAPE)
            {
                int seShapeType = sdeLayer.getShapeTypes();
                typeClass = getGeometryType(seShapeType);
                isNilable = (seShapeType & SeLayer.SE_NIL_TYPE_MASK) == SeLayer.SE_NIL_TYPE_MASK;
                defValue = GeometryBuilder.defaultValueFor(typeClass);
            }
            else if (sdeType.intValue() == SeColumnDefinition.TYPE_RASTER)
            {
                throw new DataSourceException(
                    "Raster columns are not supported yet");
            }
            else
            {
                typeClass = (Class) sdeTypes.get(sdeType);
            }

            attribute = attFactory.newAttributeType(wichCols[i].getName(),
                    typeClass, isNilable, fieldLen, defValue);

            attTypes[i] = attribute;
        }

        return attTypes;
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
    public SDEQuery createSeQuery(SdeFeatureStore fSource, Query query)
        throws DataSourceException
    {
        String[] queryColumns = getQueryColumns(query, fSource.getSchema());

        return createSeQuery(fSource, queryColumns, query);
    }

    /**
     * Runs a Query over the backend ArcSDE server and returns a
     * <code>SeQuery</code>
     *
     * @param fSource DOCUMENT ME!
     * @param queryColumns DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @return <code>null</code> if <code>query.getFilter() ==
     *         Filter.ALL</code> or the SeQuery object product of the
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SDEQuery createSeQuery(SdeFeatureStore fSource,
        String[] queryColumns, Query query) throws DataSourceException
    {
        Filter filter = query.getFilter();

        if (filter == Filter.ALL)
            return null;

        SDEQuery sdeQuery = null;
        SdeDataStore store = (SdeDataStore) fSource.getDataStore();
        SeLayer sdeLayer = fSource.getLayer();

        SQLEncoderSDE sqlEncoder = new SQLEncoderSDE(sdeLayer);
        GeometryEncoderSDE geometryEncoder = new GeometryEncoderSDE(sdeLayer);

        sdeSqlConstruct = new SeSqlConstruct();

        String[] tables = { sdeLayer.getTableName() };
        sdeSqlConstruct.setTables(tables);

        SQLUnpacker unpacker = new SQLUnpacker(sqlEncoder.getCapabilities());
        unpacker.unPackAND(filter);

        Filter sqlFilter = unpacker.getSupported();
        Filter unsupportedFilter = unpacker.getUnSupported();
        unpacker = new SQLUnpacker(GeometryEncoderSDE.getCapabilities());
        unpacker.unPackAND(unsupportedFilter);

        Filter geometryFilter = unpacker.getSupported();
        unsupportedFilter = unpacker.getUnSupported();

        //figure out which of the filter we can use.
        if ((sqlFilter != null) && (sqlFilter != Filter.NONE))
        {
            try
            {
                String where = sqlEncoder.encode(sqlFilter);
                LOGGER.fine("applying where clause: '" + where + "'");
                sdeSqlConstruct.setWhere(where);
            }
            catch (SQLEncoderException sqle)
            {
                String message = "Geometry encoder error: " + sqle.getMessage();
                LOGGER.warning(message);
                throw new DataSourceException(message, sqle);
            }
        }

        SdeConnectionPool pool = store.getConnectionPool();
        sdeQuery = new SDEQuery(pool, queryColumns, sdeSqlConstruct);

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
                        sdeQuery.setSpatialConstraints(sdeSpatialFilters);
                    }
                    catch (SeException ex)
                    {
                        throw new DataSourceException(
                            "Cannot apply spatial constraints: "
                            + ex.getMessage(), ex);
                    }
                }
            }catch (Throwable ex){
                if(sdeQuery != null)
                  sdeQuery.close();
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

    /**
     * DOCUMENT ME!
     *
     * @param stringFids DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     *
     */
    public static long[] getNumericFids(String[] stringFids)
        throws IllegalArgumentException
    {
        int nfids = stringFids.length;
        long[] fids = new long[nfids];

        for (int i = 0; i < nfids; i++)
        {
            fids[i] = getNumericFid(stringFids[i]);
        }

        return fids;
    }

    /**
     * Returns the numeric identifier of a FeatureId, given by the full
     * qualified name of the featureclass prepended to the ArcSDE feature id.
     * ej: SDE.SDE.SOME_LAYER.1
     *
     * @param fid a geotools FeatureID
     *
     * @return an ArcSDE feature ID
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static long getNumericFid(String fid) throws IllegalArgumentException
    {
        int dotIndex = fid.lastIndexOf('.');

        try
        {
            return Long.decode(fid.substring(++dotIndex)).longValue();
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException("FeatureID " + fid
                + " does not seems as a valid ArcSDE FID");
        }
    }
}


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
class SdeTypeDef
{
    /** DOCUMENT ME! */
    final int colDefType;

    /** DOCUMENT ME! */
    final int size;

    /** DOCUMENT ME! */
    final int scale;

    /**
     * Creates a new SdeTypeDef object.
     *
     * @param colDefType DOCUMENT ME!
     * @param size DOCUMENT ME!
     * @param scale DOCUMENT ME!
     */
    public SdeTypeDef(int colDefType, int size, int scale)
    {
        this.colDefType = colDefType;
        this.size = size;
        this.scale = scale;
    }
}
