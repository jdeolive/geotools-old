/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.data.shapefile;

import com.vividsolutions.jts.geom.*;

import org.geotools.data.DataSourceMetaData;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceException;
import org.geotools.feature.*;
import org.geotools.filter.Filter;
import org.geotools.data.shapefile.dbf.*;
import org.geotools.data.shapefile.shp.*;
import org.geotools.data.Query;
import org.geotools.data.DefaultQuery;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.util.*;

import java.nio.ByteBuffer;
import java.nio.channels.*;

/** ShapefileDataSource is the datasource implementation for reading ESRIs 
 * shapefile format.
 * <b>Important assumptions which may cause you problems</b>
 * <ol>
 *   <li>All geometry attributes will be named "the_geom"</li>
 *   <li>The typeName of FeatureTypes produced will be the name of the file. For
 *       example, if the full path to the file is http://www.fubar.com/baz.shp,
 *       the typeName will be baz.</li>
 *   <li>Certain assumptions are made when writing data. If these assumptions 
 *       are violated, various unknown errors will occur. These assumptions 
 *       include, but are not limited to: collections must contain Features with
 *       the same FeatureType, compatable Geometry classes, etc.</li>
 * </ol>
 * 
 * @version $Id: ShapefileDataSource.java,v 1.19 2003/07/24 19:10:02 ianschneider Exp $
 * @author James Macgill, CCG
 * @author Ian Schneider
 * @author aaimee
 */

public class ShapefileDataSource extends AbstractDataSource {
  
  private URL shpURL;
  private URL dbfURL;
  private URL shxURL;
  
  private FeatureType schema = null;
  private IDFactory idFactory;
  
  public ShapefileDataSource(URL url) throws java.net.MalformedURLException {
    
    String filename = null;
    if (url == null) {
      throw new NullPointerException("Null URL for ShapefileDataSource");
    }
    try {
      filename = java.net.URLDecoder.decode(url.toString(),"US-ASCII");
    } catch (java.io.UnsupportedEncodingException use) {
      throw new java.net.MalformedURLException(
      "Unable to decode " + url + " cause " + use.getMessage()
      );
    }
    
    String shpext = ".shp";
    String dbfext = ".dbf";
    String shxext = ".shx";
    
    if(filename.endsWith(shpext) || filename.endsWith(dbfext) || filename.endsWith(shxext)) {
      filename = filename.substring(0, filename.length() - 4);
    } else if(filename.endsWith(".SHP") || filename.endsWith(".DBF") || filename.endsWith(".SHX")) {
      filename = filename.substring(0, filename.length() - 4);
      shpext = ".SHP";
      dbfext = ".DBF";
      shxext = ".SHX";
    }
    shpURL = new URL(filename + shpext);
    dbfURL = new URL(filename + dbfext);
    shxURL = new URL(filename + shxext);

  }


  
  public IDFactory getIDFactory() {
    if (idFactory == null) {
      idFactory = new DefaultIDFactory();
    }
    return idFactory;
  }
  
  public void setIDFactory(IDFactory f) {
    this.idFactory = f;
  }
  
  /** Stops this DatataSource from loading.
   */
  public void abortLoading() {
    // let em suffer...
  }
  
  /** Gets the bounding box of this datasource using the default speed of
   * this datasource as set by the implementer.
   *
   * @return The bounding box of the datasource or null if unknown and too
   * expensive for the method to calculate.
   */
  public Envelope getBounds() throws DataSourceException {
    // This is way quick!!!
    try {
      ByteBuffer buffer = ByteBuffer.allocateDirect(100);
      ReadableByteChannel in = getReadChannel(shpURL);
      in.read(buffer);
      buffer.flip();
      ShapefileHeader header = new ShapefileHeader();
      header.read(buffer, true);
      return new Envelope(header.minX(),header.maxX(),header.minY(),header.maxY() );
    } catch (IOException ioe) {
      // What now? This seems arbitrarily appropriate !
      throw new DataSourceException("Problem getting Bbox",ioe);
    }
  }
  
  
  /**
   * Loads features from the datasource into the passed collection, based
   * on the passed filter.  Note that all data sources must support this
   * method at a minimum.
   *
   * @param collection The collection to put the features into.
   * @param filter An OpenGIS filter; specifies which features to retrieve.
   * @throws DataSourceException For all data source errors.
   */
  public void getFeatures(FeatureCollection collection,final Query query) throws DataSourceException {
    try {
      
      Filter filter = null;
      if (query != null) {
        filter = query.getFilter();
      }
      // Open a channel for our URL
      ReadableByteChannel channel = getReadChannel(shpURL);
      if(channel == null) {
        throw new DataSourceException("Non existent file or problems opening file: " + shpURL);
      }
      ShapefileReader shp = new ShapefileReader(channel);
      
      // Start the DBaseFile, if it exists
      DbaseFileReader dbf = createDbaseReader();
      
      // create a selector set based upon the original fields (not including geometry)
      int[] mapping;
      if (query.retrieveAllProperties()) {
        mapping = new int[dbf.getHeader().getNumFields() + 1];
      } else {
        mapping = new int[query.getPropertyNames().length]; 
      }
      //BitSet selector = new BitSet(dbf.getHeader().getNumFields());
      
      // Create the FeatureType based on the dbf and shapefile
      //FeatureType type = getSchema( shp, dbf, query, selector );
      FeatureType type = getSchema( shp, dbf, query, mapping );

      // FeatureMaker is like an iterator
      //FeatureMaker features = new FeatureMaker(dbf,shp,type,selector);
      FeatureMaker features = new FeatureMaker(dbf,shp,type,mapping);
      
      // read until done
      while (features.hasNext()) {
        Feature f = features.next();
        // short circuit null filter!!!!
        // this wasn't done before
        if (filter == null || filter.contains(f)) {
          collection.add(f);
        }
      }
      shp.close();
      if (dbf != null) {
        dbf.close();
      }
    }
    catch (java.io.IOException ioe){
      throw new DataSourceException("IO Exception loading data",ioe);
    }
    catch (com.vividsolutions.jts.geom.TopologyException te){
      throw new DataSourceException("Topology Exception loading data", te);
    }
    catch (org.geotools.feature.IllegalAttributeException ife){
      throw new DataSourceException("Illegal Attribute Exception loading data",ife);
    }
    catch (org.geotools.data.shapefile.shp.ShapefileException ise){
      throw new DataSourceException("Illegal Feature Exception loading data",ise);
    }
  }
  
  /* Just a hook to allow various entry points and caching of schema
   *
   */
  private FeatureType getSchema(ShapefileReader shp,DbaseFileReader dbf,Query q,int[] sel)
  throws DataSourceException,IOException,ShapefileException {
      
    // Create the FeatureType based on the dbf and shapefile
    schema = getFeatureType( dbf == null ? createDbaseReader() : dbf,
                             shp == null ? new ShapefileReader(getReadChannel(shpURL)) : shp,
                             q,
                             sel );
    
    return schema;
  }
  
  
  /**
   * Retrieves the featureType that features extracted from this datasource
   * will be created with.
   */
  public FeatureType getSchema() throws DataSourceException{
    try {
      return getSchema(null,null,new DefaultQuery(),null);
    } catch (ShapefileException e) {
      throw new DataSourceException("Invalid Shapefile",e);
    } catch (IOException e) {
      throw new DataSourceException("IO problem reading shapefile",e);
    }
    
  }
  
  /**
   * Sets the schema that features extrated from this datasource will be
   * created with.  This allows the user to obtain the attributes he wants,
   * by calling getSchema and then creating a new schema using the
   * attributeTypes from the currently used schema.
   * @param schema the new schema to be used to create features.
   */
  public void setSchema(FeatureType schema) throws DataSourceException {
    this.schema = schema;
  }
  
  private DbaseFileReader createDbaseReader() throws IOException {
    DbaseFileReader reader = null;
    if (dbfURL != null) {
      ReadableByteChannel channel = getReadChannel(dbfURL);
      if (channel != null) {
        reader = new DbaseFileReader(channel);
      }
    }
    
    return reader;
  }
  
  private static ReadableByteChannel getReadChannel(URL url) throws IOException {
    ReadableByteChannel channel = null;
    if (url.getProtocol().equals("file")) {
      File file = new File(url.getFile());
      if (! file.exists() || !file.canRead()) {
        throw new IOException("File either doesn't exist or is unreadable : " + file);
      }
      FileInputStream in = new FileInputStream(file);
      channel = in.getChannel();
    } else {
      InputStream in = url.openConnection().getInputStream();
      channel = Channels.newChannel(in);
    }
    return channel;
  }
  
  private static WritableByteChannel getWriteChannel(URL url) throws IOException {
    WritableByteChannel channel;
    if (url.getProtocol().equals("file")) {
      File f = new File(url.getFile());
      f.delete();
      if (!f.exists() && !f.createNewFile()) {
        throw new IOException("Cannot create file " + f);
      }
      RandomAccessFile raf = new RandomAccessFile(f,"rw");
      channel = raf.getChannel();
    } else {
      OutputStream out = url.openConnection().getOutputStream();
      channel = Channels.newChannel(out);
    }
    return channel;
  }
  
  /* Use the AttributeType[] from the datasource (ds), those specified by the
   * query, and a bitset to record the selection.
   * The key piece of this is that the original types must be in the same
   * order they appear in the dbase header....
   * @todo fix me, this is based on old query stuff which I (IanS) misunderstood
   */
  protected AttributeType[] determineAttributeTypes(AttributeType[] ds,Query q,int[] sel) {
    AttributeType[] attTypes;
    // all properties, remember to flip all bits!!!!!
    if (q.retrieveAllProperties()) {
      for (int i = 0, ii = sel.length; i < ii; i++) {
        sel[i] = i;
      }
      attTypes = ds;
    } else {
      HashMap positions = new HashMap(ds.length);
      for (int i = 0, ii = ds.length; i < ii; i++) {
        positions.put(ds[i].getName(), new Object[] {new Integer(i),ds[i]});
      }

      String[] qat = q.getPropertyNames();
      ArrayList types = new ArrayList(qat.length);
      for (int i = 0, ii = qat.length; i < ii; i++) {
        Object[] entry = (Object[]) positions.get(qat[i]);
        sel[i] = entry == null ? -1 : ((Integer) entry[0]).intValue();
        if (sel[i] >= 0) {
          types.add(entry[1]);
        }
        
      }
      attTypes = (AttributeType[]) types.toArray(new AttributeType[types.size()]);
    }
    return attTypes;
  }
  
  
  private AttributeType[] getAttributeTypes(DbaseFileReader dbf,ShapeType type) {
    AttributeType geometryAttribute = AttributeTypeFactory.newAttributeType(
    "the_geom",
    JTSUtilities.findBestGeometryClass(type)
    );
    
    AttributeType[] atts;
    
    // take care of the case where no dbf and query wants all => geometry only
    if (dbf != null) {
      DbaseFileHeader header = dbf.getHeader();
      atts = new AttributeType[header.getNumFields() + 1];
      atts[0] = geometryAttribute;
      
      for (int i = 0, ii = header.getNumFields(); i < ii; i++) {
        Class clazz = header.getFieldClass(i);
        atts[i + 1] = AttributeTypeFactory.newAttributeType(header.getFieldName(i), clazz);
        
      }
    } else {
      atts = new AttributeType[] {geometryAttribute};
    }
    return atts;
  }
  
  /** Determine and create a feature type.
   */
  private FeatureType getFeatureType(DbaseFileReader dbf,ShapefileReader shp,Query q,int[] sel) throws IOException, DataSourceException {
    ShapeType t = shp.getHeader().getShapeType();
    AttributeType[] types = getAttributeTypes(dbf, t);
    if (sel == null) {
      sel = new int[types.length];
      for (int i = 0, ii = sel.length; i < ii; i++) {
        sel[i] = i; 
      }
    }
    types = determineAttributeTypes(types, q, sel);
    
    try{
      return FeatureTypeFactory.newFeatureType(types,getTypeName());
    }
    catch(org.geotools.feature.SchemaException se){
      throw new DataSourceException("Schema Error",se);
    }
  }
  
  
  protected final DataSourceMetaData createMetaData() {
    MetaDataSupport shpMeta = new MetaDataSupport();
    shpMeta.setSupportsGetBbox(true);
    shpMeta.setFastBbox(true);
    shpMeta.setSupportsSetFeatures(true);
    return shpMeta;
    
  }
  
  
  
  /** An iterator-like class to encapsulate the multi-plexing of the dbf and
   * shape file reading. Fields and class are final for optimization.
   *
   * This should be part of the general package for parsing shapefiles.
   */
  final class FeatureMaker {
    
    //final BitSet selector;
    final int[] mapping;
    final DbaseFileReader dbf;
    final ShapefileReader shp;
    final Object[] readStash;
    final Object[] writeStash;
    final FeatureType type;
    final IDFactory id;
    int cnt = 0;
    
    // if the dbf is null, we create a 1 length object array,
    // otherwise it is dbf.numFields + 1
    // the extra is for geometry!
    public FeatureMaker(DbaseFileReader dbf,ShapefileReader shp,FeatureType type,int[] mapping) {
      this.dbf = dbf;
      this.shp = shp;
      //this.selector = selector;
      this.mapping = mapping;
      this.type = type;
      // must be same size as header, should change dbasereader in future...
      readStash = new Object[dbf.getHeader().getNumFields()];
      // these go to the factory...
      writeStash = new Object[type.getAttributeTypes().length];
      id = getIDFactory();
      

    }
    
    public boolean hasNext() throws IOException {
      // ensure that the records are consistent
      int both = (shp.hasNext() ? 1 : 0) + (dbf.hasNext() ? 2 : 0);
      boolean more;
      if (both == 3) {
        more = true;
      } else if (both == 0) {
        more = false;
      } else {
        throw new IllegalStateException(
        (both == 1 ? "shape" : "dbf") + "file has extra record (count = " + cnt + ")"
        );
      }
      return more;
    }
    
    public Feature next() throws IOException, IllegalAttributeException {
      // read the geometry
      ShapefileReader.Record record = shp.nextRecord();
      DbaseFileReader.Row row = null;
      
      // dbf is not null, read the rest of the features
      if (dbf != null) {
        //dbf.readEntry(readStash);
        row = dbf.readRow();
      }
      
      // the selection routine...
      for (int i = 0, ii = writeStash.length; i < ii; i++) {
        int idx = mapping[i];
        if (idx == 0) {
          writeStash[i] = record.shape();
        } else if (idx == -1) {
          writeStash[i] = null;
        } else if (row != null) {
          writeStash[i] = row.read(idx - 1);//readStash[idx - 1];
        }
      }
      
      // becuase I know that FeatureFlat copies the array,
      // I've chosen to reuse it.
      // This could be changed.
      return type.create(writeStash,id.getFeatureID(++cnt));
    }
    
  }
  
  private String getTypeName() {
    String path = ShapefileDataSource.this.shpURL.getPath();
    int dot = path.lastIndexOf('.');
    if (dot < 0) {
      dot = path.length();
    }
    int slash = path.lastIndexOf('/') + 1;
    return path.substring(slash,dot);
  } 
  
  public static interface IDFactory {
    String getFeatureID(int record);
  }
  
  public class DefaultIDFactory implements IDFactory {
    final String file =  getTypeName();
    public String getFeatureID(int record) {
      return file + "." + record;
    }
    
  }
  


  
  

  /**
   * Overwrites the file writing the feature passed as parameters
   * @param collection - the collection to be written
   */
  public void setFeatures(org.geotools.feature.FeatureCollection collection)
  throws DataSourceException {
    
    try {
      // create a good geometry collection
      // this gc will be a collection of either multi-points, multi-polygons, or multi-linestrings
      // polygons will have the rings in the correct order
      GeometryCollection gc = makeShapeGeometryCollection(collection);
      
      // guess shape dimensions
      int shapeDims = 2;
      if(gc.getNumGeometries() > 0) {
        shapeDims = JTSUtilities.guessCoorinateDims(gc.getGeometryN(0).getCoordinates());
      }
      
      ShapefileWriter writer = new ShapefileWriter(getWriteChannel(shpURL),getWriteChannel(shxURL));
      
      writer.write(gc, JTSUtilities.getShapeType(gc.getGeometryN(0), shapeDims));
      writeDbf(collection);
    } catch (ShapefileException se) {
      throw new DataSourceException("Something went wrong during shapefile saving", se);
    } catch (DbaseFileException dfe) {
      throw new DataSourceException("Something went wrong during shapefile saving", dfe);
    } catch(IOException e) {
      throw new DataSourceException("IOException during shapefile saving", e);
    } catch(RuntimeException e) {
      throw new DataSourceException("Something went wrong during shapefile saving", e);
    }
  }
  
  
  /**
   * Write a dbf file with the information from the featureCollection.
   * @param featureCollection column data from collection
   * @param fname name of the dbf file to write to
   */
  private void writeDbf(FeatureCollection featureCollection) throws DbaseFileException,IOException {
    // welcome to the nastiest code in shapefile...
    
    // precondition: all features have the same schema
    // - currently ignoring this precondition
    FeatureIterator it = featureCollection.features();
    if (!it.hasNext()) {
      throw new IOException("Empty featureCollection");
    }
    
    AttributeType[] types = it.next().getFeatureType().getAttributeTypes();
    
    
    // compute how many supported attributes are there.
    // TODO: handle Calendar, BigDecimal and BigInteger as well
    
    // this will track whether the attribute at the given index is supported.
    // later down the line, we check these values, if > 0, supported
    int[] supported = new int[types.length];
    // tracks number supported
    int numAttributes = 0;
    for(int i = 0; i < types.length; i++) {
      Class currType = types[i].getType();
      
      if((currType == String.class) || (currType == Boolean.class) ||
      Number.class.isAssignableFrom(currType) ||
      Date.class.isAssignableFrom(currType)) {
        supported[i] = ++numAttributes; // mark supported
      } else if(Geometry.class.isAssignableFrom(currType)) {
        continue;
      } else {
        throw new DbaseFileException(
        "Shapefile: unsupported type found in feature schema : " +
        currType.getName()
        );
      }
    }
    
    // set up the header
    DbaseFileHeader header = new DbaseFileHeader();
    
    for(int i = 0; i < types.length; i++) {
      Class colType = types[i].getType();
      String colName = types[i].getName();
      
      if((colType == Integer.class) || (colType == Short.class) || (colType == Byte.class)) {
        header.addColumn(colName, 'N', 16, 0);
      } else if((colType == Double.class) || (colType == Float.class)) {
        header.addColumn(colName, 'N', 33, 16);
      } else if(Date.class.isAssignableFrom(colType)) {
        header.addColumn(colName, 'D', 8, 0);
      } else if (colType == Boolean.class) {
        header.addColumn(colName, 'L', 1, 0);
      } else if(colType == String.class) {
        int maxlength = findMaxStringLength(featureCollection, i);
        
        if(maxlength > 255) {
          throw new DbaseFileException(
          "Shapefile does not support strings longer than 255 characters");
        }
        
        header.addColumn(colName, 'C', maxlength, 0);
      } else if (Geometry.class.isAssignableFrom(colType)) {
        continue;
      } else {
        throw new DbaseFileException(
        "Unable to write : " + colType.getName());
      }
    }
    header.setNumRecords(featureCollection.size());
    
    // write header
    DbaseFileWriter dbf = new DbaseFileWriter(header,getWriteChannel(dbfURL));
    
    // write rows.
    Object[] dbrow = new Object[numAttributes];
    Object[] attVals = new Object[types.length];
    it = featureCollection.features();
    while (it.hasNext()) {
      it.next().getAttributes(attVals);
      int idx = 0;
      // make data for each column in this feature (row)
      for(int j = 0; j < types.length; j++) {
        // check for supported...
        if (supported[j] > 0) {
          dbrow[idx++] = forAttribute(attVals[j],types[j].getType());
        }
      }
      dbf.write(dbrow);
    }
    
    dbf.close();
  }
  
  /*
   * Just a place to do marshalling of data.
   */
  private Object forAttribute(final Object o,Class colType) {
    Object object;
    if(colType == Integer.class) {
      object = o;
    } else if((colType == Short.class) || (colType == Byte.class)) {
      object = new Integer(((Number) o).intValue());
    } else if(colType == Double.class) {
      object = o;
    } else if(colType == Float.class) {
      object = new Double(((Number) o).doubleValue());
    } else if(colType == String.class) {
      if (o == null) {
        object = o;
      } else {
        object = o.toString();
      }
    } else if (colType == Boolean.class) {
      object = o;
    } else if(Date.class.isAssignableFrom(colType)) {
      object = o;
    } else {
      // this is kinda bad
//      Logger l = Logger.getLogger("org.geotools.data.shapefile");
//      l.warning("cannot determine writeable class for " + colType);
      if (colType != null) {
        throw new RuntimeException("Cannot convert " + colType.getName());
      } else {
        throw new RuntimeException("Null Class for conversion");
      }
    }
    
    return object;
  }
  
  /**
   *look at all the data in the column of the featurecollection, and find the largest string!
   *@param fc features to look at
   *@param attributeNumber which of the column to test.
   */
  private int findMaxStringLength(FeatureCollection fc, int attributeNumber) {
    //Feature[] features = fc.getFeatures();
    Iterator i = fc.iterator();
    
    int maxlen = 0;
    
    while (i.hasNext()) {
      Feature f = (Feature) i.next();
      String s = (String) (f.getAttribute(attributeNumber));
      if (s == null) {
        continue;
      }
      int len = s.length();
      
      if(len > maxlen) {
        maxlen = len;
      }
    }
    
    return maxlen;
  }
  
  
  
  
  
  /**
   * return a single geometry collection <Br>
   *  result.GeometryN(i) = the i-th feature in the FeatureCollection<br>
   *   All the geometry types will be the same type (ie. all polygons) - or they will be set to<br>
   *     NULL geometries<br>
   *<br>
   * GeometryN(i) = {Multipoint,Multilinestring, or Multipolygon)<br>
   *
   *@param fc feature collection to make homogeneous
   */
  public GeometryCollection makeShapeGeometryCollection(FeatureCollection fc) throws DataSourceException {
    GeometryCollection result;

    Geometry[] allGeoms = new Geometry[fc.size()];
    
    Iterator i = fc.iterator();
    if (! i.hasNext()) {
      throw new DataSourceException("Feature Collection is empty");
    }
    
    Feature f1 = (Feature) i.next();
    if (f1.getFeatureType().getDefaultGeometry() == null) {
      throw new DataSourceException("Feature has no geometry");
    }
    
    final ShapeType type = JTSUtilities.findBestGeometryType(f1.getDefaultGeometry());
    
    if (type == ShapeType.NULL) {
      throw new DataSourceException(
      "Could not determine shapefile type - data is either all GeometryCollections or empty");
    }
    
    i = fc.iterator();
    int t = 0;
    while (i.hasNext()) {
      Geometry geom;
      geom = ((Feature)i.next()).getDefaultGeometry();
      
      if (type == ShapeType.POINT) {
        
        if((geom instanceof Point)) {
          allGeoms[t] = geom;
        } else {
          allGeoms[t] = new MultiPoint(null, new PrecisionModel(), 0);
        }
        
      } else if (type == ShapeType.ARC) {
        
        if((geom instanceof LineString)) {
          LineString[] l = new LineString[1];
          l[0] = (LineString) geom;
          
          allGeoms[t] = new MultiLineString(l, new PrecisionModel(), 0);
        } else if(geom instanceof MultiLineString) {
          allGeoms[t] = geom;
        } else {
          allGeoms[t] = new MultiLineString(null, new PrecisionModel(), 0);
        }
      } else if (type == ShapeType.POLYGON) {
        
        if(geom instanceof Polygon) {
          //good!
          Polygon[] p = new Polygon[1];
          p[0] = (Polygon) geom;
          
          allGeoms[t] = JTSUtilities.makeGoodShapeMultiPolygon(new MultiPolygon(p,
          geom.getPrecisionModel(),geom.getSRID()));
        } else if(geom instanceof MultiPolygon) {
          allGeoms[t] = JTSUtilities.makeGoodShapeMultiPolygon((MultiPolygon) geom);
        } else {
          allGeoms[t] = new MultiPolygon(null, geom.getPrecisionModel(),geom.getSRID());
        }
        
      }  else if (type == ShapeType.MULTIPOINT) {
        
        if((geom instanceof Point)) {
          Point[] p = new Point[1];
          p[0] = (Point) geom;
          
          allGeoms[t] = new MultiPoint(p, geom.getPrecisionModel(),geom.getSRID());
        } else if(geom instanceof MultiPoint) {
          allGeoms[t] = geom;
        } else {
          allGeoms[t] = new MultiPoint(null, geom.getPrecisionModel(),geom.getSRID());
        }
        
        
      }
      t++;
    } // end big crazy for loop
    
    result = new GeometryCollection(allGeoms, allGeoms[0].getPrecisionModel(),allGeoms[0].getSRID());
    
    return result;
  }
  
  
  // Just a Test
//  public static final void main(String[] args) throws Exception {
//    File src = new File(args[0]);
//    ShapefileDataSource ds = new ShapefileDataSource(src.toURL());
//    FeatureCollection features = ds.getFeatures(Filter.NONE);
//    Iterator i = features.iterator();
//    while (i.hasNext()) {
//      System.out.println(i.next());
//    }
//
//  }
}
