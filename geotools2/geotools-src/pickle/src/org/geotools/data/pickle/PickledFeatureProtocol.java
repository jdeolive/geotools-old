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
  
  public static final int CURRENT_VERSION = 1;
  
  public static PickledFeatureProtocol getReader(InputStream fin,InputStream sin) throws IOException {
    checkMagic(fin);
    checkMagic(sin);
    int fv = fin.read();
    int sv = sin.read();
    PickledFeatureProtocol protocol = null;
    if (fv == sv && fv == CURRENT_VERSION)
      protocol = new Version1();
    else
      throw new IOException("No reader for " + fv + "," + sv);
    
    if (protocol != null)
      protocol.initRead(fin,sin);
    return protocol;
  }
  
  public static PickledFeatureProtocol getWriter(OutputStream fout,OutputStream sout) throws IOException {
    return getWriter(CURRENT_VERSION,fout,sout); 
  }
  
  public static PickledFeatureProtocol getWriter(int version,OutputStream fout,OutputStream sout) throws IOException {
    PickledFeatureProtocol protocol = null;
    if (version == CURRENT_VERSION)
      protocol = new Version1();
    
    if (protocol != null) {
      writeMagicAndVersion(fout,version);
      writeMagicAndVersion(sout,version);
      protocol.initWrite(fout,sout);
    }
    return protocol;
  }
  
  private static void writeMagicAndVersion(OutputStream out,int version) throws IOException {
    out.write(0xde);
    out.write(0xca);
    out.write(version);
  }
  
  private static void checkMagic(InputStream in) throws IOException {
    if (in.read() != 0xde || in.read() != 0xca)
      throw new IOException("Not Pickled File");
  }
  
  protected abstract void initRead(InputStream fin,InputStream sin) throws IOException;
  
  protected abstract void initWrite(OutputStream fout,OutputStream sout) throws IOException;
  
  protected abstract int version();
  
  public abstract void read(FeatureCollection fc) throws IOException,ClassNotFoundException,SchemaException;
  
  public abstract Feature read(int idx) throws IOException,ClassNotFoundException,SchemaException;
  
  public abstract void write(FeatureCollection fc) throws IOException;
  
  static class Version1 extends PickledFeatureProtocol {
    
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
    
    HashMap handles = new LinkedHashMap();
    HashSet clazzes = new HashSet();
    ArrayList typesList = new ArrayList();
    short handleCnt = 0;
    short lastHandle = -1;
    FeaturePickler pickler;
    
    protected void initRead(InputStream fin,InputStream sin) throws IOException {
      featureIn = fin;
      schemaIn = sin;
      multi = new MultiInputStream(featureIn);
      schemaIn.skip(4);
      input = new ObjectInputStream(multi);
      dataIn = new DataInputStream(fin);
    }
    
    protected void initWrite(OutputStream fout,OutputStream sout) throws IOException {
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
      output.writeObject(FeaturePickler.class);
      flushToSchema();
      
      Iterator i = fc.iterator();
      int handleNum = 0;
      while (i.hasNext()) {
        writeFeature( (Feature) i.next() );
      }
      // mark last type in schema file
      output.writeByte(0);
      flushToSchema();
      
      output.close();
      schemaOut.close();
    }
    
    protected void writeFeature(Feature f) throws IOException {
      short handle = getHandle(f);
      FeaturePickler pickler = (FeaturePickler) typesList.get(handle);
      output.writeShort(handle);
      pickler.writeFeature(f, output);
      flushToFeatures();
    }
    
    
    protected short getHandle(Feature f) throws IOException {
      FeatureType schema = f.getSchema();
      Short handle = (Short) handles.get(schema);
      if (handle == null) {
        handle = new Short(handleCnt++);
        handles.put(schema,handle);
        output.writeByte(1);
        FeaturePickler pickler = new FeaturePickler(schema, handle.shortValue());
        output.writeObject(pickler);
        typesList.add(pickler);
        flushToSchema();
      }
      return handle.shortValue();
    }
    
    public void read(FeatureCollection fc) throws IOException, ClassNotFoundException, SchemaException {
      int numberOfFeatures = input.readInt();
      readTypes();
      multi.switchInput(featureIn);
      
      try {
        for (int i = 0; i < numberOfFeatures; i++) {
          fc.add(readFeature());
        }
      } catch (IllegalFeatureException ife) {
        throw new SchemaException("IllegalFeatures " + ife);
      }
    }
    
    protected void readTypes() throws IOException,ClassNotFoundException,SchemaException {
      multi.switchInput(schemaIn);
      input.readObject();
      input.readObject();
      while (input.readByte() == 1) {
        typesList.add( input.readObject() );
      }
    }
    
    protected Feature readFeature() throws IOException,ClassNotFoundException,SchemaException,IllegalFeatureException {
      dataIn.readInt(); // length in bytes
      final short handle = input.readShort();
      if (lastHandle != handle)
        pickler = (FeaturePickler) typesList.get(handle);
      
      return pickler.readFeature(input);
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
      try {
        return readFeature();
      } catch (IllegalFeatureException ife) {
        throw new SchemaException("IllegalFeatures " + ife.getMessage());
      }
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
