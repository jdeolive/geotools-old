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
 * PickledFeatureProtocol encapsulates the ability to read and write a
 * FeatureCollection to java input/output streams. The writing of Features and
 * FeatureTypes is done in an implementation independent manner.<br>
 * The protocol uses two files, one to store FeatureTypes and associated 
 * ObjectStream Class related information, henceforth refered to as the 
   "schema file". The second file is used to store
 * Feature records themselves, henceforth refered to as the "feature file".<br>
 * This technique allows random access to Features by tricking the 
 * ObjectInputStream and allowing it to "share" Class definitions during the
 * reading process.<br>
 * 
 * The format for the schema file is:
 * <pre>
 *   magic "0xdeca"
 *   byte version
 *   Class FeaturePickler
 *   (byte 1, FeaturePickler)*
 *   byte 0
 *   short numberOfclasses
 *   Class*
 * </pre>
 * The format for the feature file is:
 * <pre>
 *   magic "0xdeca"
 *   byte version
 *   short numberOfFeatures
 *   (FeaturePicklerEntry)*
 * </pre>
 *
 * Where FeaturePickler is the actual object and a FeaturePicklerEntry is the 
 * record written by the FeaturePickler (representing a pickled Feature).
 *
 * <pre>
 * NOTES:
 *  - should be rewritten as subclass of ObjectOutputStream/InputStream.
 *    + two stream constructors (types + features)
 *    + override writeObject to intercept classes
 *    + add writeCollection method which follows the logic of this...
 * </pre>
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
    
    // for writing objects out - backed by ByteArrayOutputStream
    protected ObjectOutputStream output;
    // for reading objects in - backed by MultiInputStream
    protected ObjectInputStream input;
    // the underlying output for features
    protected OutputStream featureOut;
    // the underlying output for schema + classes
    protected OutputStream schemaOut;
    // a ObjectOutputStream independant way of writing data
    protected DataOutputStream dataOut;
    // a ObjectInputStream independant way of reading data
    protected DataInputStream dataIn;
    // the underlying feature input
    protected InputStream featureIn;
    // the underlying schema input
    protected InputStream schemaIn;
    // temporary buffer for writing, lies under output
    protected ByteArrayOutputStream bytesOut;
    // switchable input stream, lies under input
    protected MultiInputStream multi;
    
    // FeatureType -> Integer
    protected HashMap handles = new HashMap();
    // Set of classes encountered
    protected HashSet clazzes;
    // List of FeaturePicklers
    protected ArrayList typesList = new ArrayList();
    // holds on to handles
    protected short handleCnt = 0;
    // shortcut to list access
    protected short lastHandle = -1;
    // the most recently used pickler
    protected FeaturePickler pickler;
    
    protected void initRead(InputStream fin,InputStream sin) throws IOException {
      featureIn = fin;
      schemaIn = sin;
      
      multi = new MultiInputStream(featureIn); 
      // skip the ObjectStream header bytes 
      schemaIn.skip(4);  
      // this will read the header bytes from featureIn
      input = new ObjectInputStream(multi); 
      dataIn = new DataInputStream(fin); 
    }
    
    protected void initWrite(OutputStream fout,OutputStream sout) throws IOException {
      featureOut = fout;
      schemaOut = sout;
      
      // to store written classes of objects
      clazzes = new HashSet();
      
      bytesOut = new ByteArrayOutputStream();
      // this writes header bytes
      output = new ObjectOutputStream(bytesOut);
      output.flush();
      // write the buffered header bytes to each file
      bytesOut.writeTo(featureOut);
      bytesOut.writeTo(schemaOut);
      // reset the buffer
      bytesOut.reset();
      dataOut = new DataOutputStream(fout);
    }
    
    protected void flushToFeatures() throws IOException {
      // flush the objects
      output.flush();
      // write the block size
      dataOut.writeInt(bytesOut.size());
      // flush to underlying stream
      dataOut.flush();
      // now write the block
      bytesOut.writeTo(featureOut);
      // reset buffer
      bytesOut.reset();
    }
    
    protected void flushToSchema() throws IOException {
      // flush object
      output.flush();
      // write them out
      bytesOut.writeTo(schemaOut);
      // reset
      bytesOut.reset();
    }
    
    public int version() {
      return 1;
    }
    
    public void write(FeatureCollection fc) throws IOException {
      // write collection size and flush out
      output.writeInt(fc.size());
      output.flush();
      bytesOut.writeTo(featureOut);
      bytesOut.reset();
      
      // write the pickler class to the schema file and flush
      output.writeObject(FeaturePickler.class);
      flushToSchema();
      
      // get the collection iterator and write each one
      Iterator features = fc.iterator();
      int handleNum = 0;
      while (features.hasNext()) {
        writeFeature( (Feature) features.next() );
      }
      // mark last type in schema file and flush
      output.writeByte(0);
      output.writeShort(clazzes.size());
      Object[] clazzes = this.clazzes.toArray();
      for (int i = 0; i < clazzes.length; i++) {
        output.writeObject( clazzes[i] );
      }
      flushToSchema();
      
      // wrap things up
      output.close();
      schemaOut.close();
    }
    
    protected void writeFeature(Feature f) throws IOException {
      // look up handle and write
      short handle = getHandle(f);
      output.writeShort(handle);
      
      // get the pickler
      FeaturePickler pickler = (FeaturePickler) typesList.get(handle);
      
      // write and flush
      pickler.writeFeature(f, output,clazzes);
      flushToFeatures();
    }
    
    
    protected short getHandle(Feature f) throws IOException {
      FeatureType schema = f.getSchema();
      Short handle = (Short) handles.get(schema);
      
      // handle doesn't exist yet
      if (handle == null) {
        // next handle
        handle = new Short(handleCnt++);
        // map it
        handles.put(schema,handle);
        // write a next marker in schema
        output.writeByte(1);
        // create a pickler, write and flush
        FeaturePickler pickler = new FeaturePickler(schema, handle.shortValue());
        output.writeObject(pickler);
        flushToSchema();
        
        // store the pickler for lookup
        typesList.add(pickler);
      }
      
      return handle.shortValue();
    }
    
    public void read(FeatureCollection fc) throws IOException, ClassNotFoundException, SchemaException {
      // read number of features in file
      int numberOfFeatures = input.readInt();
      // read all the types first and switch input
      readTypes();
      multi.switchInput(featureIn);
      
      // read the features
      try {
        for (int i = 0; i < numberOfFeatures; i++) {
          fc.add(readFeature());
        }
      } catch (IllegalFeatureException ife) {
        throw new SchemaException("IllegalFeatures " + ife);
      }
    }
    
    protected void readTypes() throws IOException,ClassNotFoundException,SchemaException {
      // switch to schema file
      multi.switchInput(schemaIn);
      // read the FeaturePickler class
      input.readObject();
      // while markers exist
      while (input.readByte() == 1) {
        // read the FeaturePicklers
        typesList.add( input.readObject() );
      }
      // read in classes, "tricks" input stream
      final short classNum = input.readShort();
      for (int i = 0; i < classNum; i++) {
        input.readObject(); 
      }
    }
    
    protected Feature readFeature() throws IOException,ClassNotFoundException,SchemaException,IllegalFeatureException {
      // read length in bytes
      dataIn.readInt(); 
      // read handle
      final short handle = input.readShort();
      // short cut for handle lookup
      if (lastHandle != handle)
        pickler = (FeaturePickler) typesList.get(handle);
      
      // read the feature
      return pickler.readFeature(input);
    }
    
    public Feature read(int idx) throws IOException, ClassNotFoundException, SchemaException {
      int numberOfFeatures = input.readInt();
      readTypes();
      multi.switchInput(featureIn);
      // skip through entries
      for (int i = 0; i < idx; i++) {
        int l = dataIn.readInt();
        long s = 0;
        while ( ( s += dataIn.skip(l-s)) < s);
      }
      // now read!
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
