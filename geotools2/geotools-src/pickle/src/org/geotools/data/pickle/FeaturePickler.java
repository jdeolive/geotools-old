/*
 * FeaturePickler.java
 *
 * Created on June 6, 2003, 3:50 PM
 */

package org.geotools.data.pickle;

import com.vividsolutions.jts.geom.*;
import java.io.*;
import org.geotools.feature.*;

/**
 *
 * @author  en
 */
public class FeaturePickler implements java.io.Serializable {
  
  private transient FeatureType schema;
  private transient FeatureFactory factory;
  private transient ClassLoader classLoader;
  private transient short handle;
  private transient Object[] cache;
  private transient byte[] types;
  
  private static final byte BOOLEAN = 0;
  private static final byte BYTE = 1;
  private static final byte SHORT = 2;
  private static final byte INT = 3;
  private static final byte FLOAT = 4;
  private static final byte DOUBLE = 5;
  private static final byte LONG = 6;
  private static final byte STRING = 7;
  private static final byte GEOMETRY = 8;
  private static final byte OBJECT = -100;
  
  /** Creates a new instance of FeaturePickler */
  public FeaturePickler(FeatureType schema,short handle) {
    this.schema = schema;
    this.handle = handle;
    initTypes(schema);
  }
  
  private void initTypes(FeatureType schema) {
    types = new byte[schema.attributeTotal()];
    for (int i = 0; i < types.length; i++) {
      types[i] = getType(schema.getAttributeType(i).getType());
    }
  }
  
  public void setClassLoader(ClassLoader loader) {
    classLoader = loader;
  }
  
  public ClassLoader getClassLoader() {
    if (classLoader == null) 
      return ClassLoader.getSystemClassLoader();
    return classLoader;
  }
  
  public FeatureType getSchema() {
    return schema;
  }
  
  private FeatureFactory getFactory() {
    if (factory == null)
      factory = new FlatFeatureFactory(schema);
    return factory;
  }
  
  private Object[] getCache() {
    if (cache == null)
      cache = new Object[types.length];
    return cache;
  }
  
  private byte getType(Class type) {
    if (type == Boolean.class)
      return BOOLEAN;
    if (type == Byte.class)
      return BYTE;
    if (type == Short.class)
      return SHORT;
    if (type == Integer.class)
      return INT;
    if (type == Float.class)
      return FLOAT;
    if (type == Double.class)
      return DOUBLE;
    if (type == Long.class)
      return LONG;
    if (type == String.class)
      return STRING;
    if (Geometry.class.isAssignableFrom(type))
      return GEOMETRY;
    
    return OBJECT;
  }
  
  private void writeObject(java.io.ObjectOutputStream stream)
  throws IOException {
    stream.writeShort(schema.attributeTotal());
    for (int i = 0, ii = types.length; i < ii; i++) {
      AttributeType att = schema.getAttributeType(i);
      stream.writeUTF(att.getName());
      stream.writeUTF(att.getType().getName());
    }
  }
  private void readObject(java.io.ObjectInputStream stream)
  throws IOException, ClassNotFoundException {
    final short len = stream.readShort();
    final ClassLoader loader = getClassLoader();
    AttributeType[] types = new AttributeType[len];
    for (int i = 0; i < len; i++) {
      types[i] = new AttributeTypeDefault(stream.readUTF(),loader.loadClass(stream.readUTF()));
    }
    try {
      schema = new FeatureTypeFlat(types);
    } catch (SchemaException se) {
      throw new IOException("unexpected schema error" + se.getMessage()); 
    }
    initTypes(schema);
  }
  
  public void writeFeature(Feature f,ObjectOutputStream output)
  throws IOException {
    Object[] atts = f.getAttributes();
    for (int i = 0, ii = atts.length; i < ii; i++) {
      if (atts[i] instanceof Geometry) {
        atts[i] = new GeometryPickler( (Geometry) atts[i] );
      }
      switch (types[i]) {
        case STRING:
          output.writeUTF( atts[i].toString() );
          break;
        case GEOMETRY:
        case OBJECT:
          output.writeObject( atts[i] );
          break;
          
        case INT:
          output.writeInt( ((Integer) atts[i]).intValue() );
          break;
        case DOUBLE:
          output.writeDouble( ((Double) atts[i]).doubleValue() );
          break;
        case LONG:
          output.writeLong( ((Long) atts[i]).longValue() );
          break;
        
        case FLOAT:
          output.writeFloat( ((Float) atts[i]).floatValue() );
          break;
        case BOOLEAN:
          output.writeBoolean( ((Boolean) atts[i]).booleanValue() );
          break;
        case BYTE:
          output.writeByte( ((Byte) atts[i]).byteValue() );
          break;
        case SHORT:
          output.writeShort( ((Short) atts[i]).shortValue() );
          break;

        
        default:
          throw new IllegalStateException( "type : " + types[i] );
      }
    }
  }
  
  
  public Feature readFeature(ObjectInputStream input)
  throws IOException,ClassNotFoundException,IllegalFeatureException {
    Object[] cache = getCache();
    for (int i = 0; i < types.length; i++) {
      switch (types[i]) {
        
        case INT:
          cache[i] = new Integer( input.readInt() );
          break;
        case DOUBLE:
          cache[i] = new Double( input.readDouble() );
          break;
        case STRING:
          cache[i] = input.readUTF();
          break;
        case GEOMETRY:
        case OBJECT:
          cache[i] = input.readObject();
          break;
          
        case FLOAT:
          cache[i] = new Float( input.readFloat() );
          break;  
        case BOOLEAN:
          cache[i] = new Boolean( input.readBoolean() );
          break;
        case LONG:
          cache[i] = new Long( input.readLong() );
          break;
        case BYTE:
          cache[i] = new Byte( input.readByte() );
          break;
        case SHORT:
          cache[i] = new Short( input.readShort() );
          break;
        default:
          throw new IllegalStateException( "type : " + types[i] );
      }
    }
    return getFactory().create(cache);
  }
  
}
