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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.io.*;
import java.net.URL;
import java.nio.channels.*;
import org.geotools.data.AbstractAttributeIO;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultFeatureReader;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.dbf.DbaseFileException;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.data.shapefile.shp.ShapeHandler;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

/**
 *
 * @author  Ian Schneider
 */
public class ShapefileDataStore extends AbstractDataStore {
    
    private URL shpURL;
    private URL dbfURL;
    private URL shxURL;
    
    FeatureType schema;
    
    /** Creates a new instance of ShapefileDataStore */
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
    
    protected FeatureReader getFeatureReader(String typeName) throws IOException {
        typeCheck(typeName);
        
        try {
            return new DefaultFeatureReader(new Reader(readAttributes(),openShapeReader(),openDbfReader()));
        } catch (SchemaException se) {
            throw new DataSourceException("Error creating schema",se);
        }
    }
    
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
    
    protected DbaseFileReader openDbfReader() throws IOException {
        ReadableByteChannel rbc = getReadChannel(dbfURL);
        if (rbc == null)
            return null;
        return new DbaseFileReader(rbc);
    }
    
    public String[] getTypeNames() {
        return new String[] { createFeatureTypeName() };
    }
    
    protected String createFeatureTypeName() {
        String path = shpURL.getPath();
        int slash = Math.max(0,path.lastIndexOf('/') + 1);
        int dot = path.indexOf('.',slash);
        if (dot < 0) {
            dot = path.length();
        }
        return path.substring(slash,dot);
    }
    
    protected void typeCheck(String requested) throws IOException {
        if (! createFeatureTypeName().equals(requested)) {
            throw new IOException("No such type : " + requested);
        }
    }
    
    protected FeatureWriter getFeatureWriter(String typeName) throws IOException {
        typeCheck(typeName);
        
        FeatureReader r = null;
        try {
            r = getFeatureReader(typeName);
        } catch (Exception e) {
            FeatureType schema = getSchema(typeName);
            if (schema == null)
                throw new IOException("To create a shapefile, you must first call createSchema()");
            r = new EmptyFeatureReader(schema);
        }
        return new Writer(r,dbfURL,shpURL,shxURL);
    }
    
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
                atts[i + 1] = AttributeTypeFactory.newAttributeType(header.getFieldName(i), clazz);
                
            }
        } else {
            atts = new AttributeType[] {geometryAttribute};
        }
        return atts;
    }
    
    public void createSchema(org.geotools.feature.FeatureType featureType) throws IOException {
        schema = featureType;
    }
    
    
    protected static class Reader extends AbstractAttributeIO implements AttributeReader {
        protected ShapefileReader shp;
        protected DbaseFileReader dbf;
        protected DbaseFileReader.Row row;
        protected ShapefileReader.Record record;
        
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
                shp = null;
                dbf = null;
                row = null;
                record = null;
            }
        }
        
        public boolean hasNext() throws IOException {
            int n = shp.hasNext() ? 1 : 0;
            n += dbf.hasNext() ? 2 : 0;
            if (n == 3)
                return true;
            if (n == 0)
                return false;
            throw new IOException( (n == 1 ? "Dbf" : "Shp") + " has extra record");
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
    
    
    
    protected static class Writer implements FeatureWriter {
        
        protected FeatureReader reader;
        private ObjectOutputStream buffer;
        private File bufferFile;
        private Feature currentFeature;
        private FeatureType featureType;
        private Object[] emptyAtts;
        private int[] stringLength;
        private int[] stringFields;
        private ShapeType shapeType;
        private ShapeHandler handler;
        private int shapefileLength = 100;
        private int defaultGeometryIdx;
        private int records = 0;
        private URL dbfURL;
        private URL shpURL;
        private URL shxURL;
        private Envelope bounds = new Envelope();
        
        public Writer(FeatureReader reader,URL dbfURL,URL shpURL,URL shxURL) throws IOException {
            this.reader = reader;
            this.featureType = reader.getFeatureType();
            this.dbfURL = dbfURL;
            this.shpURL = shpURL;
            this.shxURL = shxURL;
            
            bufferFile = File.createTempFile("shapefile","ser");
            bufferFile.deleteOnExit();
            buffer = new ObjectOutputStream(new FileOutputStream(bufferFile));
            
            emptyAtts = new Object[featureType.getAttributeCount()];
            stringFields = new int[featureType.getAttributeCount()];
            
            int cnt = 0;
            for (int i = 0, ii = featureType.getAttributeCount(); i < ii; i++) {
                if (CharSequence.class.isAssignableFrom(featureType.getAttributeType(i).getType()))
                    stringFields[i] = cnt++;
                else
                    stringFields[i] = -1;
            }
            stringLength = new int[cnt];
            
        }
        
        protected void flush() throws IOException,ClassNotFoundException,DbaseFileException {
            buffer.close();
            if (records == 0) return;
            ObjectInputStream bufferRead = new ObjectInputStream(new FileInputStream(bufferFile));
            DbaseFileHeader dbaseHeader = createDbaseHeader();
            DbaseFileWriter dbf = new DbaseFileWriter(dbaseHeader, getWriteChannel(dbfURL));
            ShapefileWriter shp = new ShapefileWriter(getWriteChannel(shpURL), getWriteChannel(shxURL));
            shp.writeHeaders(bounds, shapeType, records, shapefileLength);
            final int fc = featureType.getAttributeCount() - 1;
            Object[] cache = new Object[fc];
            for (int i = 0; i < records; i++) {
                Geometry g = (Geometry) bufferRead.readObject();
                shp.writeGeometry(g);
                for (int j = 0; j < fc; j++) {
                    cache[j] = bufferRead.readObject();
                }
                dbf.write(cache);
            }
            shp.close();
            dbf.close();
        }
        
        protected DbaseFileHeader createDbaseHeader() throws IOException, DbaseFileException {
            DbaseFileHeader header = new DbaseFileHeader();
            for (int i = 0, ii = featureType.getAttributeCount(); i < ii; i++) {
                AttributeType type = featureType.getAttributeType(i);
                
                Class colType = type.getType();
                String colName = type.getName();
                
                if((colType == Integer.class) || (colType == Short.class) || (colType == Byte.class)) {
                    header.addColumn(colName, 'N', 16, 0);
                } else if((colType == Double.class) || (colType == Float.class) || colType == Number.class) {
                    header.addColumn(colName, 'N', 33, 16);
                } else if(java.util.Date.class.isAssignableFrom(colType)) {
                    header.addColumn(colName, 'D', 8, 0);
                } else if (colType == Boolean.class) {
                    header.addColumn(colName, 'L', 1, 0);
                } else if(CharSequence.class.isAssignableFrom(colType)) {
                    int len = stringLength[stringFields[i]];
                    // Possible fix for GEOT-42 : ArcExplorer doesn't like 0 length
                    // ensure that maxLength is at least 1
                    header.addColumn(colName, 'C', Math.max(1,Math.min(255,len)), 0);
                } else if (Geometry.class.isAssignableFrom(colType)) {
                    continue;
                } else {
                    throw new IOException("Unable to write : " + colType.getName());
                }
            }
            header.setNumRecords(records);
            return header;
        }
        
        public void close() throws IOException {
            if (reader == null)
                throw new IOException("Writer closed");
            try {
                reader.close();
            } finally {
                reader = null;
            }
            try {
                flush();
            } catch (ClassNotFoundException cnfe) {
                throw new DataSourceException("Classpath error",cnfe);
            } catch (DbaseFileException dfe) {
                throw new DataSourceException("Dbf error",dfe);
            } finally {
                
            }
        }
        
        public org.geotools.feature.FeatureType getFeatureType() {
            return featureType;
        }
        
        public boolean hasNext() throws IOException {
            if (reader == null)
                throw new IOException("Writer closed");
            return reader.hasNext();
        }
        
        public org.geotools.feature.Feature next() throws IOException {
            if (reader == null)
                throw new IOException("Writer closed");
            if (reader.hasNext()) {
                try {
                    currentFeature = reader.next();
                } catch (IllegalAttributeException iae) {
                    throw new DataSourceException("Error in reading",iae);
                }
            }
            try {
                currentFeature = DataUtilities.template(getFeatureType(),emptyAtts);
            } catch (IllegalAttributeException iae) {
                throw new DataSourceException("Error creating empty Feature",iae);
            }
            return currentFeature;
        }
        
        public void remove() throws IOException {
            if (reader == null)
                throw new IOException("Writer closed");
            // do nothing
        }
        
        public void write() throws IOException {
            if (reader == null)
                throw new IOException("Writer closed");
            
            Geometry g = currentFeature.getDefaultGeometry();
            if (shapeType == null) {
                int dims = JTSUtilities.guessCoorinateDims(g.getCoordinates());
                
                try {
                    shapeType = JTSUtilities.getShapeType(g,dims);
                    handler = shapeType.getShapeHandler();
                } catch (ShapefileException se) {
                    throw new RuntimeException("Unexpected Error",se);
                }
            }
            g = JTSUtilities.convertToCollection(g, shapeType);
            Envelope b = g.getEnvelopeInternal();
            if (! b.isNull())
                bounds.expandToInclude(b);
            shapefileLength += handler.getLength(g) + 8;
            buffer.writeObject(g);
            
            for (int i = 0, ii = featureType.getAttributeCount(); i < ii; i++) {
                
                Object o = currentFeature.getAttribute(i);
                if (Geometry.class.isAssignableFrom(o.getClass()))
                    continue;
                
                int sidx = stringFields[i];
                if (sidx >= 0) {
                    CharSequence c = (CharSequence) currentFeature.getAttribute(i);
                    int len = c == null ? 0 : c.length();
                    stringLength[sidx] = Math.max(1,Math.max(stringLength[sidx],len));
                }
                
                try {
                    buffer.writeObject(o);
                } catch (IOException ioe) {
                    throw new DataSourceException("Error writing buffer object of class " +
                    currentFeature.getAttribute(i).getClass().getName(),ioe
                    );
                }
            }
            records++;
        }
        
    }
}
