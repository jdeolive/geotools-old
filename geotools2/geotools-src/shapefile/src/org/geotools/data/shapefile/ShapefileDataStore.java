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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.geotools.data.AbstractAttributeIO;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.AbstractFeatureLocking;
import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.AbstractFeatureStore;
import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultFIDReader;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.shapefile.dbf.DbaseFileException;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.data.shapefile.shp.ShapeHandler;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/** A DataStore implementation which allows reading and writing from Shapefiles.
 * @author Ian Schneider
 * @todo fix file creation bug
 */
public class ShapefileDataStore extends AbstractDataStore {
    
    private final URL shpURL;
    private final URL dbfURL;
    private final URL shxURL;
    
    private FeatureType schema;
    
    /** Creates a new instance of ShapefileDataStore.
     * @param url The URL of the shp file to use for this DataSource.
     * @throws MalformedURLException If computation of related URLs (dbf,shx) fails.
     */
    public ShapefileDataStore(URL url) throws java.net.MalformedURLException {
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
    
    /**
     * Determine if the location of this shapefile is local or remote.
     * @return true if local, false if remote
     */
    public boolean isLocal() {
        return shpURL.getProtocol().equals("file");
    }
    
    /**
     * Delete existing files.
     */ 
    private void clear() {
        if (isLocal()) {
            delete(shpURL);
            delete(dbfURL);
            delete(shxURL);
        }
    }
    
    /**
     * Delete a URL (file)
     */
    private void delete(URL u) {
        File f = new File(u.getFile());
        f.delete();
    }
    
    /**
     * Obtain a ReadableByteChannel from the given URL. If the url protocol is
     * file, a FileChannel will be returned. Otherwise a generic channel will
     * be obtained from the urls input stream.
     */
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
    
    /**
     * Obtain a WritableByteChannel from the given URL. If the url protocol is
     * file, a FileChannel will be returned. Currently, this method will return
     * a generic channel for remote urls, however both shape and dbf writing
     * can only occur with a local FileChannel channel.
     */
    private static WritableByteChannel getWriteChannel(URL url) throws IOException {
        WritableByteChannel channel;
        if (url.getProtocol().equals("file")) {
            File f = new File(url.getFile());
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
    
    /** Create a FeatureReader for the provided type name.
     * @param typeName The name of the FeatureType to create a reader for.
     * @throws IOException If an error occurs during creation
     * @return A new FeatureReader.
     */    
    protected FeatureReader getFeatureReader(String typeName) throws IOException {
        typeCheck(typeName);
        
        try {
            return createFeatureReader(typeName,getAttributesReader());
        } catch (SchemaException se) {
            throw new DataSourceException("Error creating schema",se);
        }
    }
    
    protected FeatureReader createFeatureReader(String typeName,Reader r) throws SchemaException,IOException {
        return new org.geotools.data.FIDFeatureReader(
            r,
            new DefaultFIDReader(typeName),
            schema
        );  
    }
    
    protected Reader getAttributesReader() throws IOException {
        AttributeType[] atts = schema == null ? 
                readAttributes() : schema.getAttributeTypes();
        return new Reader(atts,openShapeReader(),openDbfReader());
    }
    
    /** Convenience method for opening a ShapefileReader.
     * @throws IOException If an error occurs during creation.
     * @return A new ShapefileReader.
     */    
    protected ShapefileReader openShapeReader() throws IOException {
        ReadableByteChannel rbc = getReadChannel(shpURL);
        if (rbc == null)
            return null;
        try {
            return new ShapefileReader(rbc,true);
        } catch (ShapefileException se) {
            throw new DataSourceException("Error creating ShapefileReader",se);
        }
    }
    
    /** Convenience method for opening a DbaseFileReader.
     * @throws IOException If an error occurs during creation.
     * @return A new DbaseFileReader
     */    
    protected DbaseFileReader openDbfReader() throws IOException {
        ReadableByteChannel rbc = getReadChannel(dbfURL);
        if (rbc == null)
            return null;
        return new DbaseFileReader(rbc);
    }
    
    /** Get an array of type names this DataStore holds.<BR/>ShapefileDataStore will
     * always return a single name.
     * @return An array of length one containing the single type held.
     */    
    public String[] getTypeNames() {
        return new String[] { getCurrentTypeName() };
    }
    
    /** Create the type name of the single FeatureType this DataStore represents.<BR/>
     * For example, if the urls path is file:///home/billy/mytheme.shp, the type name
     * will be mytheme.
     * @return A name based upon the last path component of the url minus the extension.
     */    
    protected String createFeatureTypeName() {
        String path = shpURL.getPath();
        int slash = Math.max(0,path.lastIndexOf('/') + 1);
        int dot = path.indexOf('.',slash);
        if (dot < 0) {
            dot = path.length();
        }
        return path.substring(slash,dot);
    }
    
    protected String getCurrentTypeName() {
        return schema == null ? createFeatureTypeName() : schema.getTypeName();
    }
    
    /** A convenience method to check if a type name is correct.
     * @param requested The type name requested.
     * @throws IOException If the type name is not available
     */    
    protected void typeCheck(String requested) throws IOException {
        
        if (! getCurrentTypeName().equals(requested)) {
            throw new IOException("No such type : " + requested);
        }
    }
    
    /** Create a FeatureWriter for the given type name.
     * @param typeName The typeName of the FeatureType to write
     * @throws IOException If the typeName is not available or some other error occurs.
     * @return A new FeatureWriter.
     */    
    protected FeatureWriter getFeatureWriter(String typeName) throws IOException {
        typeCheck(typeName);

        return new Writer(typeName);
    }
    
    /** Obtain the FeatureType of the given name. ShapefileDataStore contains only one
     * FeatureType.
     * @param typeName The name of the FeatureType.
     * @throws IOException If a type by the requested name is not present.
     * @return The FeatureType that this DataStore contains.
     */    
    public FeatureType getSchema(String typeName) throws IOException {
        typeCheck(typeName);
        if (schema == null) {
            try {
                schema = FeatureTypeFactory.newFeatureType(readAttributes(),createFeatureTypeName());
            } catch (SchemaException se) {
                throw new DataSourceException("Error creating FeatureType",se);
            }
        }
        return schema;
    }
    
    /** Create the AttributeTypes contained within this DataStore.
     * @throws IOException If AttributeType reading fails
     * @return An array of new AttributeTypes
     */    
    protected AttributeType[] readAttributes() throws IOException {
        
        ShapefileReader shp = openShapeReader();
        DbaseFileReader dbf = openDbfReader();
        
        AttributeType geometryAttribute = AttributeTypeFactory.newAttributeType(
        "the_geom",
        JTSUtilities.findBestGeometryClass(shp.getHeader().getShapeType())
        );
        
        AttributeType[] atts;
        
        // take care of the case where no dbf and query wants all => geometry only
        if (dbf != null) {
            DbaseFileHeader header = dbf.getHeader();
            atts = new AttributeType[header.getNumFields() + 1];
            atts[0] = geometryAttribute;
            
            for (int i = 0, ii = header.getNumFields(); i < ii; i++) {
                Class clazz = header.getFieldClass(i);
                atts[i + 1] = AttributeTypeFactory.newAttributeType(header.getFieldName(i), clazz, true, header.getFieldLength(i));
                
            }
        } else {
            atts = new AttributeType[] {geometryAttribute};
        }
        return atts;
    }
    
    /** Set the FeatureType of this DataStore. This method will delete any existing
     * local resources or throw an IOException if the DataStore is remote.
     * @param featureType The desired FeatureType.
     * @throws IOException If the DataStore is remote.
     */    
    public void createSchema(FeatureType featureType) throws IOException {
        if (! isLocal() )
            throw new IOException("Cannot create FeatureType on remote shapefile");
        clear();
        schema = featureType;
    }
    
    
    /** An AttributeReader implementation for Shapefile. Pretty straightforward.
     *  <BR/>The default geometry is at position 0, and all dbf columns follow.
     */    
    protected static class Reader extends AbstractAttributeIO implements AttributeReader {

        protected ShapefileReader shp;
        protected DbaseFileReader dbf;
        protected DbaseFileReader.Row row;
        protected ShapefileReader.Record record;
        int cnt;
        
        public Reader(AttributeType[] atts,ShapefileReader shp,DbaseFileReader dbf) {
            super(atts);
            this.shp = shp;
            this.dbf = dbf;
        }
        
        public void close() throws IOException {
            try {
                shp.close();
                dbf.close();
            } finally {
            	row = null;
            	record = null;
            	shp = null;
                dbf = null;
            }
        }
        
        public boolean hasNext() throws IOException {
            int n = shp.hasNext() ? 1 : 0;
            n += dbf.hasNext() ? 2 : 0;
            if (n == 3)
                return true;
            if (n == 0)
                return false;

            throw new IOException( (n == 1 ? "Shp" : "Dbf") + " has extra record");
        }
        
        public void next() throws IOException {
            record = shp.nextRecord();
            row = dbf.readRow();
        }
        
        public Object read(int param) throws IOException, java.lang.ArrayIndexOutOfBoundsException {
            switch (param) {
                case 0:
                    return record.shape();
                default:
                    return row.read(param - 1);
            }
        }
        
    }
    
    /**
     * A FeatureWriter for ShapefileDataStore. Uses a write and annotate 
     * technique to avoid buffering attributes and geometries. Because the 
     * shapefile and dbf require header information which can only be obtained 
     * by reading the entire series of Features, the headers are updated after
     * the initial write completes.
     */
    protected class Writer implements FeatureWriter {
        
        // store current time here as flag for temporary write
        private long temp;
        // the FeatureReader to obtain the current Feature from
        protected FeatureReader featureReader;
        // the AttributeReader
        protected Reader attReader;
        
        // the current Feature
        private Feature currentFeature;
        // the FeatureType we are representing
        private FeatureType featureType;
        // an array for reuse in Feature creation
        private Object[] emptyAtts;
        // an array for reuse in writing to dbf.
        private Object[] transferCache;
        
        private ShapeType shapeType;
        private ShapeHandler handler;
        // keep track of shapefile length during write, starts at 100 bytes for
        // required header
        private int shapefileLength = 100;
        // hold the defaultGeometry index in the FeatureType
        private int defaultGeometryIdx;
        // keep track of the number of records written
        private int records = 0;
        // hold 1 if dbf should write the attribute at the index, 0 if not
        private byte[] writeFlags;
        private ShapefileWriter shpWriter;
        private DbaseFileWriter dbfWriter;
        private DbaseFileHeader dbfHeader;
        private FileChannel dbfChannel;
        // keep track of bounds during write
        private Envelope bounds = new Envelope();

        public Writer(String typeName) throws IOException {
            // set up reader
            try {
                attReader = getAttributesReader();
                featureReader = createFeatureReader(typeName,attReader);
                temp = System.currentTimeMillis();
                
            } catch (Exception e) {
                FeatureType schema = getSchema(typeName);
                if (schema == null)
                    throw new IOException("To create a shapefile, you must first call createSchema()");
                featureReader = new EmptyFeatureReader(schema);
                temp = 0;
            }
            
            this.featureType = featureReader.getFeatureType();
            
            // set up buffers and write flags
            emptyAtts = new Object[featureType.getAttributeCount()];
            writeFlags = new byte[featureType.getAttributeCount()];
            int cnt = 0;
            for (int i = 0, ii = featureType.getAttributeCount(); i < ii; i++) {
                // if its a geometry, we don't want to write it to the dbf...
                if (! featureType.getAttributeType(i).isGeometry()) {
                    cnt++;
                    writeFlags[i] = (byte)1;
                }
            }
            // dbf transfer buffer
            transferCache = new Object[cnt];
            
            // open underlying writers
            shpWriter = new ShapefileWriter(
                (FileChannel) getWriteChannel(getStorageURL(shpURL)),
                (FileChannel) getWriteChannel(getStorageURL(shxURL))
            );
            
            dbfChannel = (FileChannel) getWriteChannel(getStorageURL(dbfURL));
            dbfHeader = createDbaseHeader();
            dbfWriter = new DbaseFileWriter(dbfHeader, dbfChannel);
        }
        
        /**
         * Get a temporary URL for storage based on the one passed in
         */
        protected URL getStorageURL(URL url) throws java.net.MalformedURLException {
            return temp == 0 ? url : getStorageFile(url).toURL();
        }
        
        /**
         * Get a temproray File based on the URL passed in
         */
        protected File getStorageFile(URL url) {
            String f = url.getFile();
            f = f.substring(f.lastIndexOf("/") + 1) + temp;
            File tf = new File(System.getProperty("java.io.tmpdir"),f) ;
            return tf;
        }
        
        /**
         * Go back and update the headers with the required info.
         */
        protected void flush() throws IOException {
            shpWriter.writeHeaders(bounds, shapeType, records, shapefileLength);
            
            dbfHeader.setNumRecords(records);
            dbfChannel.position(0);
            dbfHeader.writeHeader(dbfChannel);
        }
        
        /**
         * Attempt to create a DbaseFileHeader for the FeatureType. Note, we 
         * cannot set the number of records until the write has completed.
         */
        protected DbaseFileHeader createDbaseHeader() throws IOException, DbaseFileException {
            DbaseFileHeader header = new DbaseFileHeader();
            for (int i = 0, ii = featureType.getAttributeCount(); i < ii; i++) {
                AttributeType type = featureType.getAttributeType(i);
                
                Class colType = type.getType();
                String colName = type.getName();
                int fieldLen = type.getFieldLength();
                if (fieldLen <= 0)
                  fieldLen = 255;
                
                // @todo respect field length
                if((colType == Integer.class) || (colType == Short.class) || (colType == Byte.class)) {
                    header.addColumn(colName, 'N', Math.min(fieldLen,16), 0);
                } else if((colType == Double.class) || (colType == Float.class) || colType == Number.class) {
                    int l = Math.min(fieldLen,33);
                    header.addColumn(colName, 'N', l, l / 2);
                } else if(java.util.Date.class.isAssignableFrom(colType)) {
                    header.addColumn(colName, 'D', fieldLen, 0);
                } else if (colType == Boolean.class) {
                    header.addColumn(colName, 'L', 1, 0);
                } else if(CharSequence.class.isAssignableFrom(colType)) {
                    // Possible fix for GEOT-42 : ArcExplorer doesn't like 0 length
                    // ensure that maxLength is at least 1
                    header.addColumn(colName, 'C', Math.min(254,fieldLen), 0);
                } else if (Geometry.class.isAssignableFrom(colType)) {
                    continue;
                } else {
                    throw new IOException("Unable to write : " + colType.getName());
                }
            }
            System.out.println("header " + header);
            return header;
        }
        
        /**
         * In case someone doesn't close me.
         */
        protected void finalize() throws Throwable {
            if (featureReader != null) {
                try {
                    close();
                } catch (Exception e) {
                    // oh well, we tried
                }
            }
        }
        
        /**
         * Clean up our temporary write if there was one
         */
        protected void clean() throws IOException {
            if (temp == 0)
                return;

            copyAndDelete(shpURL);
            copyAndDelete(shxURL);
            copyAndDelete(dbfURL);
        }
        
        /**
         * Copy the file at the given URL to the original
         */ 
        protected void copyAndDelete(URL src) throws IOException {
            File storage = getStorageFile(src);
            File dest = new File(src.getFile());
            FileChannel in = new FileInputStream(storage).getChannel();
            FileChannel out = new FileOutputStream(dest).getChannel();
            long len = in.size();
            long copied = out.transferFrom(in,0,in.size());
            if (len != copied)
                throw new IOException("unable to complete write");
            storage.delete();
        }
        
        /**
         * Release resources and flush the header information.
         */
        public void close() throws IOException {
            if (featureReader == null)
                throw new IOException("Writer closed");
            // make sure to write the last feature...
            if (currentFeature != null)
                write();
            
            // if the attribute reader is here, that means we may have some
            // additional tail-end file flushing to do if the Writer was closed
            // before the end of the file
            if (attReader != null) {
                shapeType = attReader.shp.getHeader().getShapeType();
                handler = shapeType.getShapeHandler();
                // handle the case where zero records have been written, but the
                // stream is closed and the headers
                if (records == 0) {
                    shpWriter.writeHeaders(bounds,shapeType, 0, 0);
                }
                
                // copy array for bounds
                double[] env = new double[4];
                while (attReader.hasNext()) {
                    // transfer bytes from shapefile
                    shapefileLength += attReader.shp.transferTo(
                        shpWriter, ++records, env
                    );
                    // bounds update
                    bounds.expandToInclude(env[0],env[1]);
                    bounds.expandToInclude(env[2],env[3]);
                    // transfer dbf bytes
                    attReader.dbf.transferTo(dbfWriter);
                }
            }

            // close reader, flush headers, and copy temp files, if any
            try {
                featureReader.close();
            } finally {
                try {
                    flush();
                } finally {
                    shpWriter.close();
                    dbfWriter.close();
                    dbfChannel.close();
                }
                featureReader = null;
                shpWriter = null;
                dbfWriter = null;
                dbfChannel = null;
                clean();
            }
        }
        
        public org.geotools.feature.FeatureType getFeatureType() {
            return featureType;
        }
        
        public boolean hasNext() throws IOException {
            if (featureReader == null)
                throw new IOException("Writer closed");
            return featureReader.hasNext();
        }
        
        public org.geotools.feature.Feature next() throws IOException {
            // closed already, error!
            if (featureReader == null)
                throw new IOException("Writer closed");
            
            // we have to write the current feature back into the stream
            if (currentFeature != null)
                write();
            
            // is there another? If so, return it
            if (featureReader.hasNext()) {
                try {
                    return currentFeature = featureReader.next();
                } catch (IllegalAttributeException iae) {
                    throw new DataSourceException("Error in reading",iae);
                }
            }
            
            // reader has no more (no were are adding to the file)
            // so return an empty feature
            try {
                return currentFeature = DataUtilities.template(
                    getFeatureType(),emptyAtts
                );
            } catch (IllegalAttributeException iae) {
                throw new DataSourceException("Error creating empty Feature",iae);
            }

        }
        
        public void remove() throws IOException {
            if (featureReader == null)
                throw new IOException("Writer closed");
            
            if (currentFeature == null)
                throw new IOException("Current feature is null");
            
            // mark the current feature as null, this will result in it not
            // being rewritten to the stream
            currentFeature = null;
        }
        
        public void write() throws IOException {
            if (currentFeature == null)
                throw new IOException("Current feature is null");
            
            if (featureReader == null)
                throw new IOException("Writer closed");
            
            // writing of Geometry
            Geometry g = currentFeature.getDefaultGeometry();
            
            // if this is the first Geometry, find the shapeType and handler
            if (shapeType == null) {
                int dims = JTSUtilities.guessCoorinateDims(g.getCoordinates());
                
                try {
                    shapeType = JTSUtilities.getShapeType(g,dims);
                    // we must go back and annotate this after writing
                    shpWriter.writeHeaders(new Envelope(),shapeType, 0, 0);
                    handler = shapeType.getShapeHandler();
                } catch (ShapefileException se) {
                    throw new RuntimeException("Unexpected Error",se);
                }
            }
            
            // convert geometry
            g = JTSUtilities.convertToCollection(g, shapeType);
            
            // bounds calculations
            Envelope b = g.getEnvelopeInternal();
            if (! b.isNull())
                bounds.expandToInclude(b);
            
            // file length update
            shapefileLength += handler.getLength(g) + 8;
            
            // write it
            shpWriter.writeGeometry(g);
    
            // writing of attributes
            int idx = 0;
            for (int i = 0, ii = featureType.getAttributeCount(); i < ii; i++) {
                // skip geometries
                if (writeFlags[i] > 0) {
                    transferCache[idx++] = currentFeature.getAttribute(i);
                }
            }
            dbfWriter.write(transferCache);
            
            // one more down...
            records++;
            
            // clear the currentFeature
            currentFeature = null;
        }
        
    }
    
    /** Gets the bounding box of the file represented by this data store
     *  as a whole (that is, off all of the features in the shapefile)
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    private Envelope getBounds() throws DataSourceException {
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
    
    protected Envelope getBounds(Query query) throws IOException {
    	if(query == Query.ALL) {
    		return getBounds();
    	} else {
    		return null; // too expensive
    	}
    }
    
	/**
	 * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
	 */
	public FeatureSource getFeatureSource(final String typeName)
	throws IOException {
		final FeatureType featureType = getSchema(typeName);

		if (isWriteable) {
			if (getLockingManager() != null) {
				return new AbstractFeatureLocking() {
					public DataStore getDataStore() {
						return ShapefileDataStore.this;
					}

					public void addFeatureListener(FeatureListener listener) {
						listenerManager.addFeatureListener(this, listener);
					}

					public void removeFeatureListener(
							FeatureListener listener) {
						listenerManager.removeFeatureListener(this, listener);
					}

					public FeatureType getSchema() {
						return featureType;
					}
					
					public Envelope getBounds(Query query) throws IOException {
						return ShapefileDataStore.this.getBounds(query);
					}
					
					
				};
			} else {
				return new AbstractFeatureStore() {
					public DataStore getDataStore() {
						return ShapefileDataStore.this;
					}

					public void addFeatureListener(FeatureListener listener) {
						listenerManager.addFeatureListener(this, listener);
					}

					public void removeFeatureListener(
							FeatureListener listener) {
						listenerManager.removeFeatureListener(this, listener);
					}

					public FeatureType getSchema() {
						return featureType;
					}
					
					public Envelope getBounds(Query query) throws IOException {
						return ShapefileDataStore.this.getBounds(query);
					}
				};
			}
		} else {
			return new AbstractFeatureSource() {
				public DataStore getDataStore() {
					return ShapefileDataStore.this;
				}

				public void addFeatureListener(FeatureListener listener) {
					listenerManager.addFeatureListener(this, listener);
				}

				public void removeFeatureListener(FeatureListener listener) {
					listenerManager.removeFeatureListener(this, listener);
				}

				public FeatureType getSchema() {
					return featureType;
				}
				
				public Envelope getBounds(Query query) throws IOException {
					return ShapefileDataStore.this.getBounds(query);
				}
			};
		}
	}
	

}
