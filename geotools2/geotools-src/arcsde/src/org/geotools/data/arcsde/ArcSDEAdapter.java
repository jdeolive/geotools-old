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
package org.geotools.data.arcsde;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.*;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultAttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryEncoderSDE;
import org.geotools.filter.GeometryEncoderException;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderSDE;
import org.geotools.filter.SQLUnpacker;

import com.esri.sde.sdk.client.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import java.util.*;

/**
 * Utility class to deal with SDE specifics such as creating SeQuery objects
 * from geotool's Query's, mapping SDE types to Java ones and JTS Geometries, etc.
 *
 * @author Gabriel Roldán
 * @version $Id: ArcSDEAdapter.java,v 1.1 2004/03/11 00:17:09 groldan Exp $
 */
public class ArcSDEAdapter
{
  /** Logger for ths class' package */
  private static final Logger LOGGER = Logger.getLogger(ArcSDEAdapter.class.
      getPackage().getName());

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

  /**
   * DOCUMENT ME!
   *
   * @param attribute DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws IllegalArgumentException DOCUMENT ME!
   */
  public static int guessShapeTypes(AttributeType attribute)
  {
    if (attribute == null) {
      throw new IllegalArgumentException("null is not valid as argument");
    }

    if (!attribute.isGeometry()) {
      throw new IllegalArgumentException(attribute.getName()
                                         + " is not a geometry attribute");
    }

    Class geometryClass = attribute.getType();

    if (Geometry.class.isAssignableFrom(geometryClass)) {
      throw new IllegalArgumentException(geometryClass
                                         + " is not a valid Geometry class");
    }

    int shapeTypes = 0;

    if (attribute.isNillable()) {
      shapeTypes |= SeLayer.SE_NIL_TYPE_MASK;

    }
    if (GeometryCollection.class.isAssignableFrom(geometryClass)) {
      shapeTypes |= SeLayer.SE_MULTIPART_TYPE_MASK;

      if (geometryClass == MultiPoint.class) {
        shapeTypes |= SeLayer.SE_POINT_TYPE_MASK;
      }
      else if (geometryClass == MultiLineString.class) {
        shapeTypes |= SeLayer.SE_LINE_TYPE_MASK;
      }
      else if (geometryClass == MultiPolygon.class) {
        shapeTypes |= SeLayer.SE_AREA_TYPE_MASK;
      }
      else {
        throw new IllegalArgumentException(
            "no SDE geometry mapping for " + geometryClass);
      }
    }
    else {
      if (geometryClass == MultiPoint.class) {
        shapeTypes |= SeLayer.SE_POINT_TYPE_MASK;
      }
      else if (geometryClass == MultiLineString.class) {
        shapeTypes |= SeLayer.SE_LINE_TYPE_MASK;
      }
      else if (geometryClass == MultiPolygon.class) {
        shapeTypes |= SeLayer.SE_AREA_TYPE_MASK;
      }
      else {
        throw new IllegalArgumentException(
            "no SDE geometry mapping for " + geometryClass);
      }
    }

    return shapeTypes;
  }

  /**
   *
   */
  public static SeColumnDefinition[] createSeColDefs(FeatureType featureType)
      throws DataSourceException
  {
    int nCols = featureType.getAttributeCount();
    AttributeType[] atts = featureType.getAttributeTypes();
    SeColumnDefinition[] coldefs = new SeColumnDefinition[nCols];

    //new SeColumnDefinition( "Integer_Val", SeColumnDefinition.TYPE_INTEGER, 10, 0, isNullable);
    for (int i = 0; i < nCols; i++) {
      if (atts[i].isGeometry()) {
        continue;
      }

      String attName = atts[i].getName();
      Class attClass = atts[i].getType();
      boolean nillable = atts[i].isNillable();

      SdeTypeDef sdeType = (SdeTypeDef) javaTypes.get(attClass);

      if (sdeType == null) {
        throw new DataSourceException("No ArcSDE equivalent type found for: "
                                      + attClass.getName());
      }

      try {
        coldefs[i] = new SeColumnDefinition(attName,
                                            sdeType.colDefType, sdeType.size,
                                            sdeType.scale,
                                            nillable);
      }
      catch (SeException ex) {
        throw new DataSourceException(
            "Cannot create the column definition named " + attName
            + ": " + ex.getSeError().getSdeErrMsg(), ex);
      }
    }

    return coldefs;
  }

  /**
   * creates the schema of a given ArcSDE featureclass
   */
  public static FeatureType createSchema(ArcSDEConnectionPool connPool,
                                         String typeName)
      throws IOException
  {
    SeLayer sdeLayer = connPool.getSdeLayer(typeName);
    SeTable sdeTable = connPool.getSdeTable(typeName);
    AttributeType[] types = createAttributeTypes(sdeLayer, sdeTable);
    FeatureType type = null;

    try {
      type = FeatureTypeFactory.newFeatureType(types,
                                               sdeLayer.getQualifiedName());
    }
    catch (SeException ex) {
      throw new DataSourceException(ex.getMessage(), ex);
    }
    catch (SchemaException ex) {
      throw new DataSourceException(ex.getMessage(), ex);
    }
    catch (FactoryConfigurationError ex) {
      throw new DataSourceException(ex.getMessage(), ex);
    }

    return type;
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
  private static AttributeType[] createAttributeTypes(SeLayer sdeLayer, SeTable table)
      throws DataSourceException
  {
    boolean isNilable;
    int fieldLen;
    Object defValue;

    SeColumnDefinition[] wichCols = null;
    try {
      wichCols = table.describe();
    }
    catch (SeException ex) {
      throw new DataSourceException("Error obtaining table schema from " +
                                    table.getQualifiedName());
    }

    int nCols = wichCols.length;
    AttributeType[] attTypes = new AttributeType[nCols];
    AttributeType attribute = null;
    Class typeClass;

    for (int i = 0; i < nCols; i++) {
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

      if (sdeType.intValue() == SeColumnDefinition.TYPE_SHAPE) {
        int seShapeType = sdeLayer.getShapeTypes();
        typeClass = getGeometryType(seShapeType);
        isNilable = (seShapeType & SeLayer.SE_NIL_TYPE_MASK) ==
            SeLayer.SE_NIL_TYPE_MASK;
        defValue = GeometryBuilder.defaultValueFor(typeClass);
      }
      else if (sdeType.intValue() == SeColumnDefinition.TYPE_RASTER) {
        throw new DataSourceException(
            "Raster columns are not supported yet");
      }
      else {
        typeClass = (Class) sdeTypes.get(sdeType);
      }

      attribute = DefaultAttributeTypeFactory.newAttributeType(wichCols[i].
          getName(),
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

    if ( (seShapeType & SeLayer.SE_POINT_TYPE_MASK) ==
        SeLayer.SE_POINT_TYPE_MASK) {
      if ( (seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) ==
          SeLayer.SE_MULTIPART_TYPE_MASK) {
        clazz = com.vividsolutions.jts.geom.MultiPoint.class;
      }
      else {
        clazz = com.vividsolutions.jts.geom.Point.class;
      }
    }
    else if ( (seShapeType & SeLayer.SE_SIMPLE_LINE_TYPE_MASK) ==
             SeLayer.SE_SIMPLE_LINE_TYPE_MASK) {
      if ( (seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) ==
          SeLayer.SE_MULTIPART_TYPE_MASK) {
        clazz = com.vividsolutions.jts.geom.MultiLineString.class;
      }
      else {
        clazz = com.vividsolutions.jts.geom.LineString.class;
      }
    }
    else if ( (seShapeType & SeLayer.SE_LINE_TYPE_MASK) ==
             SeLayer.SE_LINE_TYPE_MASK) {
      if ( (seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) ==
          SeLayer.SE_MULTIPART_TYPE_MASK) {
        clazz = com.vividsolutions.jts.geom.MultiLineString.class;
      }
      else {
        clazz = com.vividsolutions.jts.geom.LineString.class;
      }
    }
    else if ( (seShapeType & SeLayer.SE_AREA_TYPE_MASK) ==
             SeLayer.SE_AREA_TYPE_MASK) {
      if ( (seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) ==
          SeLayer.SE_MULTIPART_TYPE_MASK) {
        /**
         * @task TODO: strongly test returning Polygon, it seems that
         *       SDE polygons are OGC multipolygons...
         */
        clazz = com.vividsolutions.jts.geom.MultiPolygon.class;
      }
      else {
        clazz = com.vividsolutions.jts.geom.MultiPolygon.class;
      }
    }

    return clazz;
  }

  public static ArcSDEQuery createSeQuery(ArcSDEDataStore store,
                                          Query query)
      throws IOException
  {
    return createSeQuery(store, store.getSchema(query.getTypeName()), query);
  }

  public static ArcSDEQuery createSeQuery(ArcSDEDataStore store,
                                          FeatureType schema,
                                          Query query)
      throws IOException
  {
    Filter filter = query.getFilter();

    if (filter == Filter.ALL) {
      return null;
    }

    ArcSDEQuery sdeQuery = null;
    String typeName = query.getTypeName();

    SeSqlConstruct sdeSqlConstruct = new SeSqlConstruct();
    String[] tables = {typeName};
    sdeSqlConstruct.setTables(tables);

    ArcSDEConnectionPool pool = store.getConnectionPool();

    String []queryColumns = query.getPropertyNames();
    queryColumns = getQueryColumns(store, typeName, queryColumns);
    FeatureType querySchema = null;
    try {
      querySchema = DataUtilities.createSubType(schema, queryColumns);
    }
    catch (SchemaException ex) {
      throw new DataSourceException(
          "Some requested attributes do not match the table schema: " +
          ex.getMessage(), ex);
    }

    FilterSet filters = computeFilters(store, typeName, filter);

    sdeQuery = new ArcSDEQuery(pool, querySchema, sdeSqlConstruct);

    if(!query.equals(Query.ALL))
    {
      String where = filters.createSqlWhereClause();

      if (where != null) {
        LOGGER.fine("applying where clause: '" + where + "'");
        sdeSqlConstruct.setWhere(where);
      }

      sdeQuery.setFilterSet(filters);

      if (filters.getGeometryFilter() != Filter.NONE) {
        try {
          SeFilter[] sdeSpatialFilters = filters.createSpatialFilters();
          if ( (sdeSpatialFilters != null)
              && (sdeSpatialFilters.length > 0)) {
            LOGGER.fine("applying " + sdeSpatialFilters.length
                        + " spatial filters ");
            try {
              sdeQuery.setSpatialConstraints(sdeSpatialFilters);
            }
            catch (SeException ex) {
              throw new DataSourceException(
                  "Cannot apply spatial constraints: "
                  + ex.getMessage(), ex);
            }
          }
        }
        catch (Throwable ex) {
          if (sdeQuery != null) {
            sdeQuery.close();
          }
          String message = "Encoder error " + ex.getMessage();
          LOGGER.warning(message);
          throw new DataSourceException(message, ex);
        }
      }
    }

    return sdeQuery;
  }

  public static FilterSet computeFilters(ArcSDEDataStore store,
                                          String typeName, Filter filter)
      throws NoSuchElementException, IOException {
    SeLayer sdeLayer = store.getConnectionPool().getSdeLayer(typeName);
    FilterSet filters = new FilterSet(sdeLayer, filter);
    return filters;
  }

  private static String[] getQueryColumns(ArcSDEDataStore store,
                                          String typeName,
                                          String[] queryColumns)
      throws DataSourceException {
    if (queryColumns == null || queryColumns.length == 0) {
      SeTable table = store.getConnectionPool().getSdeTable(typeName);
      SeColumnDefinition[] sdeCols = null;
      try {
        sdeCols = table.describe();
      }
      catch (SeException ex) {
        throw new DataSourceException(ex.getMessage(), ex);
      }
      queryColumns = new String[sdeCols.length];
      for (int i = 0; i < sdeCols.length; i++) {
        queryColumns[i] = sdeCols[i].getName();
      }
    }
    return queryColumns;
  }

}

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
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

class FilterSet
{
  private Filter sourceFilter;

  private Filter sqlFilter;
  private Filter geometryFilter;
  private Filter unsupportedFilter;

  SQLEncoderSDE sqlEncoder;
  GeometryEncoderSDE geometryEncoder;

  public FilterSet(SeLayer sdeLayer, Filter sourceFilter)
  {
    sqlEncoder = new SQLEncoderSDE(sdeLayer);
    geometryEncoder = new GeometryEncoderSDE(sdeLayer);
    setFilter(sourceFilter);
  }

  public void setFilter(Filter sourceFilter)
  {
    this.sourceFilter = sourceFilter;
    createFilters();
  }

  private void createFilters()
  {
    SQLUnpacker unpacker = new SQLUnpacker(sqlEncoder.getCapabilities());
    unpacker.unPackAND(sourceFilter);

    this.sqlFilter = unpacker.getSupported();
    Filter remainingFilter = unpacker.getUnSupported();

    unpacker = new SQLUnpacker(GeometryEncoderSDE.getCapabilities());
    unpacker.unPackAND(remainingFilter);

    this.geometryFilter = unpacker.getSupported();
    this.unsupportedFilter = unpacker.getUnSupported();
  }

  public String createSqlWhereClause()
      throws DataSourceException
  {
    String where = null;
    Filter sqlFilter = getSqlFilter();
    if (sqlFilter != Filter.NONE) {
      try {
        where = sqlEncoder.encode(sqlFilter);
      }
      catch (SQLEncoderException sqle) {
        String message = "Geometry encoder error: " + sqle.getMessage();
        throw new DataSourceException(message, sqle);
      }
    }
    return where;
  }

  public SeFilter[] createSpatialFilters()
      throws GeometryEncoderException
  {
    geometryEncoder.encode(geometryFilter);
    SeFilter[] sdeSpatialFilters = geometryEncoder.getSpatialFilters();
    return sdeSpatialFilters;
  }

  public Filter getSqlFilter()
  {
    return sqlFilter == null ? Filter.NONE : sqlFilter;
  }

  public Filter getGeometryFilter()
  {
    return geometryFilter == null ? Filter.NONE : geometryFilter;
  }

  public Filter getUnsupportedFilter()
  {
    return unsupportedFilter == null ? Filter.NONE : unsupportedFilter;
  }
}

