/*
 * PickledFeatureProtocol.java
 *
 * Created on June 4, 2003, 11:44 AM
 */

package org.geotools.data.pickle;

import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.util.*;
import org.geotools.feature.*;

/**
 *
 * @author  Ian Schneider
 */
public abstract class PickledFeatureProtocol {
  
  private static int VERSION = 1;
  protected ObjectOutputStream output;
  protected ObjectInputStream input;
  protected OutputStream featureOut;
  protected OutputStream schemaOut;
  protected DataOutputStream dataOut;
  protected DataInputStream dataIn;
  protected InputStream featureIn;
  protected InputStream schemaIn;
  protected ByteArrayOutputStream bytesOut;
  protected MultiInputStream multi;
  
  public static PickledFeatureProtocol defaultProtocol() {
    return new Version1();
  }
  
  public static PickledFeatureProtocol lookup(int protocol) {
    if (protocol == VERSION)
      return new Version1();
    return null;
  }
  
  public void setOutput(OutputStream fout,OutputStream sout) throws IOException {
    featureOut = fout;
    schemaOut = sout;
    bytesOut = new ByteArrayOutputStream();
    output = new ObjectOutputStream(bytesOut);
    output.flush();
    bytesOut.writeTo(featureOut);
    bytesOut.writeTo(schemaOut);
    bytesOut.reset();
    dataOut = new DataOutputStream(fout);
  }
  
  public void setInput(InputStream fin,InputStream sin) throws IOException {
    featureIn = fin;
    schemaIn = sin;
    multi = new MultiInputStream(featureIn);
    schemaIn.skip(4);
    input = new ObjectInputStream(multi);
    dataIn = new DataInputStream(fin);
  }
  
  public abstract int version();
  
  public abstract void read(FeatureCollection fc) throws IOException,ClassNotFoundException,SchemaException;
  
  public abstract Feature read(int idx) throws IOException,ClassNotFoundException,SchemaException;
  
  public abstract void write(FeatureCollection fc) throws IOException;
  
  static class Version1 extends PickledFeatureProtocol {
    
    HashMap handles = new LinkedHashMap();
    HashSet clazzes = new HashSet();
    ArrayList typesList = new ArrayList();
    int handleCnt = 0;
    
    protected void flushToFeatures() throws IOException {
      output.flush();
      dataOut.writeInt(bytesOut.size());
      dataOut.flush();
      bytesOut.writeTo(featureOut);
      bytesOut.reset();
    }
    
    protected void flushToSchema() throws IOException {
      output.flush();
      bytesOut.writeTo(schemaOut);
      bytesOut.reset();
    }
    
    public int version() {
      return 1;
    }
    
    public void write(FeatureCollection fc) throws IOException {
      output.writeInt(fc.size());
      output.flush();
      bytesOut.writeTo(featureOut);
      bytesOut.reset();
      
      output.writeObject(GeometryPickler.class);
      flushToSchema();
      
      Iterator i = fc.iterator();
      int handleNum = 0;
      while (i.hasNext()) {
        writeFeature( (Feature) i.next() );
      }
      // mark last type in schema file
      output.writeByte(0);
      flushToSchema();
    }
    
    protected void writeFeature(Feature f) throws IOException {
      Object[] atts = f.getAttributes();
      int handle = getHandle(f);
      output.writeShort(atts.length);
      output.writeShort(handle);
      for (int i = 0, ii = atts.length; i < ii; i++) {
        if (atts[i] instanceof Geometry) {
          atts[i] = new GeometryPickler( (Geometry) atts[i] );
        }
        clazzes.add(atts[i]);
        output.writeObject(atts[i]); 
      }
      
      flushToFeatures();
    }
    
    protected void writeSchema(FeatureType type) throws IOException {
      output.writeByte(1);
      output.writeShort(type.attributeTotal());
      for (int i = 0, ii = type.attributeTotal(); i < ii; i++) {
        AttributeType att = type.getAttributeType(i);
        output.writeObject(att.getName());
        output.writeObject(att.getType());
      }
      flushToSchema();
    }
    
    protected int getHandle(Feature f) throws IOException {
      FeatureType schema = f.getSchema();
      Integer handle = (Integer) handles.get(schema);
      if (handle == null) {
        handle = new Integer(handleCnt++);
        handles.put(schema,handle);
        writeSchema(schema);
      }
      return handle.intValue();
    }
    
    public void read(FeatureCollection fc) throws IOException, ClassNotFoundException, SchemaException {
      int numberOfFeatures = input.readInt();
      readTypes();
      multi.switchInput(featureIn);
      for (int i = 0; i < numberOfFeatures; i++) {
        fc.add(readFeature());
      }
    }
    
    protected void readTypes() throws IOException,ClassNotFoundException,SchemaException {
      multi.switchInput(schemaIn);
      input.readObject();
      while (input.readByte() == 1) {
        FeatureType type = readFeatureType();
        FeatureFactory factory = new FlatFeatureFactory(type);
        typesList.add(factory);
      }
    }
    
    protected Feature readFeature() throws IOException,ClassNotFoundException,SchemaException {
      dataIn.readInt(); // length in bytes
      final int len = input.readShort();
      final int handle = input.readShort();
      
      Object[] data = new Object[len];
      for (int i = 0; i < len; i++) {
        data[i] = input.readObject();
      }
      try {
        return createFeature(data,handle);
      } catch (IllegalFeatureException ife) {
        throw new SchemaException("BOGUS");
      }
    }
    
    protected FeatureType readFeatureType() throws IOException, ClassNotFoundException, SchemaException {
      int attCnt = input.readShort();
      String[] attNames = new String[attCnt];
      Class[] attClasses = new Class[attCnt];
      for (int i = 0; i < attCnt; i++) {
        attNames[i] = input.readObject().toString();
        attClasses[i] = (Class) input.readObject();
      }
      return createFeatureType(attNames,attClasses);
    }
    
    protected FeatureType createFeatureType(String[] attNames,Class[] attClasses) throws SchemaException {
      AttributeType[] atts = new AttributeType[attNames.length];
      for (int i = 0, ii = atts.length; i < ii; i++) {
        atts[i] = new AttributeTypeDefault(attNames[i],attClasses[i]);
      }
      return new FeatureTypeFlat(atts);
    }
    
    protected Feature createFeature(Object[] atts,int factoryIdx) throws IllegalFeatureException {
      return ((FeatureFactory) typesList.get(factoryIdx)).create(atts);
    }
    
    public Feature read(int idx) throws IOException, ClassNotFoundException, SchemaException {
      int numberOfFeatures = input.readInt();
      readTypes();
      multi.switchInput(featureIn);
      for (int i = 0; i < idx; i++) {
        int l = dataIn.readInt();
        long s = 0;
        while ( ( s += dataIn.skip(l-s)) < s);
      }
      return readFeature();
    }    
    
  }
  
 

  static final class MultiInputStream extends FilterInputStream {
    public MultiInputStream(InputStream in) {
      super(in);
    }
    public void switchInput(InputStream in) {
      this.in = in;
    }
  }

}
