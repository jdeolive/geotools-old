package org.geotools.data.arcgrid;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URL;
import javax.media.jai.RasterFactory;


/**
 * ArcGridRaster sub-instance that handles the "ArcGrid" format that GRASS
 * outputs (a variation of the original format...)
 *
 * @author aaime
 */
public class GRASSArcGridRaster extends ArcGridRaster {
    /** Column number tag in the header file */
    public static final String COLS = "COLS";

    /** Row number tag in the header file */
    public static final String ROWS = "ROWS";

    /** x corner coordinate tag in the header file */
    public static final String NORTH = "NORTH";

    /** y corner coordinate tag in the header file */
    public static final String SOUTH = "SOUTH";

    /** y corner coordinate tag in the header file */
    public static final String EAST = "EAST";

    /** y corner coordinate tag in the header file */
    public static final String WEST = "WEST";
    public static final String NO_DATA_MARKER = "*";

    /**
     * Creates a new instance of ArcGridRaster
     *
     * @param srcURL URL of a ArcGridRaster
     *
     * @throws IOException DOCUMENT ME!
     */
    public GRASSArcGridRaster(URL srcURL) throws IOException {
        super(srcURL);
    }

    protected void parseHeader(StreamTokenizer st) throws IOException {
        // make sure tokenizer is set up right
        st.resetSyntax();
        st.eolIsSignificant(true);
        st.whitespaceChars('\t', '\t');
        st.whitespaceChars(' ', ' ');
		st.whitespaceChars(':', ':');
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars('_', '_');
		st.wordChars('*', '*');
        st.parseNumbers();

        double north = 0;
        double south = 0;
        double east = 0;
        double west = 0;

        // read lines while the next token is not a number
        while (st.nextToken() != StreamTokenizer.TT_NUMBER) {
            if (st.ttype == StreamTokenizer.TT_WORD) {
                String key = st.sval;
                
				if (NO_DATA_MARKER.equalsIgnoreCase(key))
					break;

                if (st.nextToken() != StreamTokenizer.TT_NUMBER) {
                    throw new IOException("Expected number after " + key);
                }

                double val = st.nval;

                if (COLS.equalsIgnoreCase(key)) {
                    nCols = (int) val;
                } else if (ROWS.equalsIgnoreCase(key)) {
                    nRows = (int) val;
                } else if (NORTH.equalsIgnoreCase(key)) {
                    north = readHeaderDouble(st);
                } else if (SOUTH.equalsIgnoreCase(key)) {
                    south = readHeaderDouble(st);
                } else if (EAST.equalsIgnoreCase(key)) {
                    east = readHeaderDouble(st);
                } else if (WEST.equalsIgnoreCase(key)) {
                    west = readHeaderDouble(st);
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

        xllCorner = west;
        yllCorner = south;
        cellSize = (north - south) / nRows;
    }

    /**
     * Returns the WritableRaster of the raster
     *
     * @return RenderedImage
     *
     * @throws IOException DOCUMENT ME!
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
        st.whitespaceChars(' ', ' ');
        st.whitespaceChars(' ', '\t');
        st.whitespaceChars('\n', '\n');
        st.eolIsSignificant(false);
        st.ordinaryChars('E', 'E');
        st.ordinaryChars('*', '*');

        // allocate raster, for now this is done with floating point data,
        // though eventually it should be configurable
        WritableRaster raster = RasterFactory.createBandedRaster(java.awt.image.DataBuffer.TYPE_FLOAT,
                getNCols(), getNRows(), 1, null);

        // Read values from grid and put into raster.
        // Values must be numbers, which may be simple <num>, or expressed
        // in scientific notation <num>E<exp>. 
        // The following loop can read both, even if mixed.
        // The loop expects a token to be read already
        st.nextToken();

        for (int y = 0; y < getNRows(); y++) {
            for (int x = 0; x < getNCols(); x++) {
                // this call always reads the next token
                double d = readCell(st, x, y);

                // mask no data values with NaN
                if (!Double.isNaN(d)) {
                    minValue = Math.min(minValue, d);
                    maxValue = Math.max(maxValue, d);
                }

                // set the value at x,y in band 0 to the parsed value
                raster.setSample(x, y, 0, d);
            }
        }

        reader.close();

        return raster;
    }

    /**
     * Parse a number.
     *
     * @param st DOCUMENT ME!
     * @param x DOCUMENT ME!
     * @param y DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private double readCell(StreamTokenizer st, int x, int y)
        throws IOException {
        double d = 0;

        // read a token, expected: a number of the null marker
        switch (st.ttype) {
        case StreamTokenizer.TT_NUMBER:
            d = (float) st.nval;

            break;

        case StreamTokenizer.TT_EOF:
            throw new IOException("Unexpected EOF at " + x + "," + y);

        default:

            if (st.ttype == '*' || st.sval.equalsIgnoreCase(NO_DATA_MARKER)) {
            	st.nextToken();
                return Double.NaN;
            } else {
                throw new IOException("Unknown token " + st.ttype);
            }
        }

        // read another. May be an exponent of this number.
        // If its not an exponent, its the next number. Fall through
        // and token is prefetched for next loop...
        switch (st.nextToken()) {
        case 'e':
        case 'E':

            // now read the exponent
            st.nextToken();

            if (st.ttype != StreamTokenizer.TT_NUMBER) {
                throw new IOException("Expected exponent at " + x + "," + y);
            }

            // calculate
            d = d * Math.pow(10.0, st.nval);

            // prefetch for next loop
            st.nextToken();

            break;

		case '*':
        case StreamTokenizer.TT_NUMBER:
        case StreamTokenizer.TT_EOF:
            break;

        default:
            throw new IOException("Expected Number or EOF");
        }

        return d;
    }

    /**
     * Write out the given raster.
     *
     * @param raster DOCUMENT ME!
     * @param xl DOCUMENT ME!
     * @param yl DOCUMENT ME!
     * @param cellsize DOCUMENT ME!
     * @param compress DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void writeRaster(Raster raster, double xl, double yl,
        double cellsize, boolean compress) throws IOException {
        // open writer
        PrintWriter out = openWriter(compress);

        nCols = raster.getWidth();
        nRows = raster.getHeight();
        xllCorner = xl;
        yllCorner = yl;
        cellSize = cellsize;

        double north = yllCorner + (nRows * cellSize);
        double east = xllCorner + (nCols * cellSize);

        // output header and assign header fields
        out.println(NORTH + ": " + north);
        out.println(SOUTH + ": " + yllCorner);
        out.println(EAST + ": " + east);
        out.println(WEST + ": " + xllCorner);
        out.println(ROWS + ": " + nRows);
        out.println(COLS + ": " + nCols);

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
                double v = raster.getSampleDouble(j, i, 0);

                // no data masking
                if (Double.isNaN(v)) {
                    buffer.append(NO_DATA_MARKER);
                } else {
                    buffer.append(v);
                }

                // append value and possible spacer
                if ((j + 1) < jj) {
                    buffer.append(' ');
                }
            }

            // flush out row
            out.write(buffer.toString());
            out.println();
        }

        out.flush();
        out.close();
    }
}
