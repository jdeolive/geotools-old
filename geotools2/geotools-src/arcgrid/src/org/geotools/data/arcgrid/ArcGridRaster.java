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
package org.geotools.data.arcgrid;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import javax.media.jai.RasterFactory;

import org.geotools.io.NIOBufferUtils;


/**
 * Class user for parsing an ArcGrid header (.arc, .asc) file
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 * @author <a href="mailto:aaime@users.sf.net">Andrea Aime</a>
 */
public class ArcGridRaster {
    /** Column number tag in the header file */
    public static final String NCOLS = "NCOLS";
    
    /** Row number tag in the header file */
    public static final String NROWS = "NROWS";
    
    /** x corner coordinate tag in the header file */
    public static final String XLLCORNER = "XLLCORNER";
    
    /** y corner coordinate tag in the header file */
    public static final String YLLCORNER = "YLLCORNER";
    
    /** cell size tag in the header file */
    public static final String CELLSIZE = "CELLSIZE";
    
    /** no data tag in the header file */
    public static final String NODATA_VALUE = "NODATA_VALUE";
    
    /** header or data file url */
    private URL srcURL;
    
    /** max value found in the file */
    protected double maxValue = Float.MIN_VALUE;
    
    /** min value found in the file */
    protected double minValue = Float.MAX_VALUE;
    
	protected double xllCorner = Double.NaN;
    
	protected double yllCorner = Double.NaN;
    
	protected double cellSize = Double.NaN;
    
	protected double noData = Double.NaN;
    
	protected int nCols = -1;
    
	protected int nRows = -1;
    
    /**
     * Creates a new instance of ArcGridRaster
     *
     * @param srcURL URL of a ArcGridRaster
     *
     */
    public ArcGridRaster(URL srcURL) throws IOException {
        this.srcURL = srcURL;
    }
    
    /**
     * Max value
     *
     * @return the max value contained in the data file
     */
    public double getMaxValue() {
        return maxValue;
    }
    
    /**
     * Min value
     *
     * @return the min value contained in the data file
     */
    public double getMinValue() {
        return minValue;
    }
    
    /**
     * Returns the number of rows contained in the file
     *
     * @return number of rows
     */
    public int getNRows() {
        return nRows;
    }
    
    /**
     * Returns the number of columns contained in the file
     *
     * @return number of columns
     */
    public int getNCols() {
        return nCols;
    }
    
    /**
     * Returns the x cordinate of the ... corner
     *
     * @return x cordinate of the ... corner
     */
    public double getXlCorner() {
        return xllCorner;
    }
    
    /**
     * Returns the y cordinate of the ... corner
     *
     * @return y cordinate of the ... corner
     */
    public double getYlCorner() {
        return yllCorner;
    }
    
    /**
     * Returns the cell size
     *
     * @return cell size
     */
    public double getCellSize() {
        return cellSize;
    }
    
    /**
     * Returns the no data (null) value
     *
     * @return no data (null) value
     */
    public double getNoData() {
        return noData;
    }
    
    
    /**
     * Parses the reader for the known properties
     *
     * @param properties the map to be filled in
     * @param reader the source data
     *
     * @throws IOException for reading errors
     * @throws DataSourceException for unrecoverable data format violations
     */
    public void parseHeader() throws IOException {
        parseHeader(new StreamTokenizer(openReader()));
    }
    
    
    protected void parseHeader(StreamTokenizer st) throws IOException {
        // make sure tokenizer is set up right
        st.resetSyntax();
        st.eolIsSignificant(true);
        st.whitespaceChars('\t', '\t');
        st.whitespaceChars(' ',' ');
        st.wordChars('a','z');
        st.wordChars('A','Z');
        st.wordChars('_','_');
        st.parseNumbers();

        // read lines while the next token is not a number
        while (st.nextToken() !=  StreamTokenizer.TT_NUMBER) {
            
            if (st.ttype == StreamTokenizer.TT_WORD) {
                String key = st.sval;
                if (st.nextToken() != StreamTokenizer.TT_NUMBER)
                    throw new IOException("Expected number after " + key);
                double val = st.nval;
                
                if (NCOLS.equalsIgnoreCase(key)) {
                    nCols = (int) val;
                } else if (NROWS.equalsIgnoreCase(key)) {
                    nRows = (int) val;
                } else if (XLLCORNER.equalsIgnoreCase(key)) {
                    xllCorner = readHeaderDouble(st);
                } else if (YLLCORNER.equalsIgnoreCase(key)) {
                    yllCorner = readHeaderDouble(st);
                } else if (CELLSIZE.equalsIgnoreCase(key)) {
                    cellSize = readHeaderDouble(st);
                } else if (NODATA_VALUE.equalsIgnoreCase(key)) {
                    noData = readHeaderDouble(st);
                } else {
                    // ignore extra fields for now
                    // are there ever any?
                }
                
                if (st.nextToken() != StreamTokenizer.TT_EOL) {
                    throw new IOException("Expected new line, not " + st.sval);
                }
            } else {
                throw new IOException("Exected word token");
            }
            
            
        }
        st.pushBack();
    }
    
    protected double readHeaderDouble(StreamTokenizer st) throws IOException {
        double val = st.nval;
        if (st.nextToken() == StreamTokenizer.TT_WORD && st.sval.startsWith("E")) {
            val = val * Math.pow(10,Integer.parseInt(st.sval.substring(1)));
        } else {
            st.pushBack();
        }
        return val;
    }
    
    
    /**
     * Obtain the best reader for the situation
     */
    protected Reader openReader() throws IOException {
        // gzipped source, may be remote URL
        if (srcURL.getFile().endsWith(".gz")) {
            InputStream in = new java.util.zip.GZIPInputStream(
                srcURL.openStream()
            );
            return new InputStreamReader(new java.io.BufferedInputStream(in));
        } 
        
        // file based, non zipped - lets use memory-mapped reader
        if (srcURL.getProtocol().equals("file")) {
            return new MemoryMappedReader(new File(java.net.URLDecoder.decode(srcURL.getPath(),"UTF-8"))); 
        } 
        
        // default URL
        return new InputStreamReader(srcURL.openStream());
    }
    
    /**
     * Open the best writer for the situation.
     */
    protected PrintWriter openWriter(boolean compress) throws IOException {
        java.io.OutputStream out;
        if (srcURL.getProtocol().equals("file")) {
            out = new java.io.BufferedOutputStream(
                new java.io.FileOutputStream(new File(java.net.URLDecoder.decode(srcURL.getPath(),"UTF-8")))
            ); 
        } else {
            out = srcURL.openConnection().getOutputStream();
        }
        if (compress)
            out = new java.util.zip.GZIPOutputStream(out);
        return new PrintWriter(out);
    }

    /**
     * Returns the WritableRaster of the raster
     *
     * @return RenderedImage
     */
    public WritableRaster readRaster() throws IOException {
        
        // open reader and make tokenizer
        Reader reader = openReader();
        StreamTokenizer st = new StreamTokenizer(reader);
        
        // parse header
        parseHeader(st);
        
        // reconfigure tokenizer
        st.resetSyntax();
        st.parseNumbers();
        st.whitespaceChars(' ',' ');
        st.whitespaceChars(' ','\t');
        st.whitespaceChars('\n','\n');
        st.eolIsSignificant(false);        
        st.ordinaryChars('E', 'E');
        
        // allocate raster, for now this is done with floating point data,
        // though eventually it should be configurable
        WritableRaster raster = RasterFactory.createBandedRaster(
            java.awt.image.DataBuffer.TYPE_FLOAT,
            getNCols(),
            getNRows(),
            1,
            null
        );
        
        // Read values from grid and put into raster.
        // Values must be numbers, which may be simple <num>, or expressed
        // in scientific notation <num>E<exp>. 
        // The following loop can read both, even if mixed.
        
        // The loop expects a token to be read already
        st.nextToken();
        for (int y = 0; y < getNRows(); y++) {
            for (int x = 0; x < getNCols(); x++) {
                
                // this call always reads the next token
                double d = readCell(st,x,y);
                
                // mask no data values with NaN
                if (d == getNoData()) {
                    d = Double.NaN;
                } else {
                    minValue = Math.min(minValue,d);
                    maxValue = Math.max(maxValue,d);
                }
                
                // set the value at x,y in band 0 to the parsed value
                raster.setSample(x,y,0,d);
                
            }
        }
        
        reader.close();

        return raster;
    }
    
    /**
     * Parse a number.
     */
    private double readCell(StreamTokenizer st,int x,int y) throws IOException {
        double d = 0;
        
        // read a token, expected: a number
        switch (st.ttype) {
            case StreamTokenizer.TT_NUMBER:
                d = (float) st.nval;
                break;
            case StreamTokenizer.TT_EOF:
                throw new IOException("Unexpected EOF at " + x + "," + y);
            default:
                throw new IOException("Unknown token " + st.ttype);
        }
        
        // read another. May be an exponent of this number.
        // If its not an exponent, its the next number. Fall through
        // and token is prefetched for next loop...
        switch (st.nextToken()) {
            case 'e':case 'E':
                // now read the exponent
                st.nextToken();
                if (st.ttype != StreamTokenizer.TT_NUMBER)
                    throw new IOException("Expected exponent at " + x + "," + y);
                // calculate
                d = d * Math.pow(10.0,st.nval);
                
                // prefetch for next loop
                st.nextToken();
                
                break;
            case StreamTokenizer.TT_NUMBER: case StreamTokenizer.TT_EOF:
                break;
            default:
                throw new IOException("Expected Number or EOF");
        }
        
        return d;
    }
    
    /**
     * Print n spaces to the PrintWriter
     */
    protected void spaces(PrintWriter p,int n) {
        for (int i = 0; i < n; i++) {
            p.print(' ');
        }
    }
    
    /**
     * Write out the given raster.
     */
    public void writeRaster(Raster raster,
                            double xl,double yl,double cellsize,
                            boolean compress) throws IOException {
        // open writer
        PrintWriter out = openWriter(compress);
        
        // output header and assign header fields
        out.print(NCOLS); spaces(out,9); out.println(nCols = raster.getWidth());
        out.print(NROWS); spaces(out,9); out.println(nRows = raster.getHeight());
        out.print(XLLCORNER); spaces(out,5); out.println(xllCorner = xl);
        out.print(YLLCORNER); spaces(out,5); out.println(yllCorner = yl);
        out.print(CELLSIZE); spaces(out,6); out.println(cellSize = cellsize);
        out.print(NODATA_VALUE); spaces(out, 2); out.println(noData = -9999);
        
        // reset min and max
        minValue = Double.MAX_VALUE;
        maxValue = Double.MIN_VALUE;
        
        // a buffer to flush each line to
        // this technique makes things a bit quicker because buffer.append()
        // internally calls new FloatingDecimal(double).appendTo(StringBuffer)
        // instead of creating a new String each time (as would be done with
        // PrintWriter, ie. print(new FloatingDecimal(double).toString())
        StringBuffer buffer = new StringBuffer(raster.getWidth() * 4);
        for (int i = 0, ii = raster.getHeight(); i < ii; i++) {
            // clear buffer
            buffer.delete(0, buffer.length());
            // write row to buffer
            for (int j = 0, jj = raster.getWidth(); j < jj; j++) {
                
                double v = raster.getSampleDouble(j,i,0);
                
                // no data masking
                if (Double.isNaN(v)) {
                    v = -9999;
                } 
                
                // append value and possible spacer
                buffer.append(v);
                if (j + 1 < jj) buffer.append(' ');
            }
            
            // flush out row
            out.write(buffer.toString());
            out.println();
        }
        out.flush();
        out.close();
    }
    
    /**
     * This is a slight optimization over using a BufferedReader. 
     * StreamTokenizer makes single character read calls.
     */
    static class MemoryMappedReader extends java.io.Reader {
        ByteBuffer map;
        CharBuffer chars;
        CharsetDecoder decoder = Charset.forName("US-ASCII").newDecoder();
        FileChannel channel;
        public MemoryMappedReader(File f) throws IOException {
            channel= new FileInputStream(f).getChannel();
            map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            chars = CharBuffer.allocate(16 * 1028);
            fill();
        }
        
        public void close() throws IOException {
            if(channel != null)
                channel.close();
            NIOBufferUtils.clean(map);
            channel = null;
            map = null;
        }
        
        void fill() throws IOException {
            decoder.decode(map,chars, false);
            chars.flip();
        }
        
        public int read() throws IOException {
            if (chars.remaining() == 0) {
                chars.flip();
                fill();
                if (chars.remaining() == 0)
                    return -1;
            }
            return chars.get();
        }
        
        public int read(char[] cbuf, int off, int len) throws IOException {
            throw new RuntimeException("Expected single character read");
        }
        
    }
    
    public String toString() {
        java.lang.reflect.Field[] f = getClass().getDeclaredFields();
        String s = "";
        for (int i = 0, ii = f.length; i < ii; i++) {
            if (! java.lang.reflect.Modifier.isStatic(f[i].getModifiers()))
                try {
                    s += (f[i].getName() + " : " + f[i].get(this));
                    if (i + 1 < f.length)
                        s += " ";
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return s;
    }
}
