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
package org.geotools.data.gtopo30;

import com.sun.media.imageio.stream.FileChannelImageInputStream;
import com.sun.media.imageio.stream.RawImageInputStream;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.geotools.cs.*;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.data.DataSource;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.Query;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceMetaData;
import org.geotools.feature.*;
import org.geotools.filter.Filter;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.geotools.units.Unit;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.Set;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.TransposeDescriptor;
import org.geotools.gc.GridGeometry;
import org.geotools.gc.GridRange;


/**
 * DOCUMENT ME!
 *
 * @author aaime
 */
public class GTopo30DataSource extends AbstractDataSource 
    implements DataSource {
    private URL demURL;
    private URL demHeaderURL;
    private URL srcURL;
    private URL srcHeaderURL;
    private URL statsURL;
    private com.vividsolutions.jts.geom.Envelope cropEnvelope;
    private Color[] demColors = new Color[] {
            new Color(5, 90, 5), new Color(150, 200, 150), new Color(190, 150, 20),
            new Color(100, 100, 50), new Color(200, 210, 220), Color.WHITE, Color.WHITE, Color.WHITE,
            Color.WHITE
        };
    private String filename = null;
    
    /**
     * Creates a new instance of GTopo30DataSource
     *
     * @param url URL pointing to one of the GTopo30 files (.dem, .hdr, .src, .sch, .stx)
     *
     * @throws MalformedURLException
     */
    public GTopo30DataSource(URL url) throws MalformedURLException {
        try {
            filename = URLDecoder.decode(url.getFile(), "US-ASCII");
        } catch (UnsupportedEncodingException use) {
            throw new MalformedURLException("Unable to decode " + url + " cause " +
                use.getMessage());
        }

        boolean recognized = false;
        String dmext = ".dem";
        String dhext = ".hdr";
        String srext = ".src";
        String shext = ".sch";
        String stext = ".stx";

        if (filename.endsWith(dmext) || filename.endsWith(dhext) || filename.endsWith(srext) ||
            filename.endsWith(shext) || filename.endsWith(stext)) {
            recognized = true;
        } else {
            dmext = dmext.toUpperCase();
            dhext = dhext.toUpperCase();
            srext = srext.toUpperCase();
            shext = shext.toUpperCase();
            stext = stext.toUpperCase();

            if (filename.endsWith(dmext) || filename.endsWith(dhext) || filename.endsWith(srext) ||
                filename.endsWith(shext) || filename.endsWith(stext)) {
                recognized = true;
            }
        }

        filename = filename.substring(0, filename.length() - 4);

        demURL = new URL(url, filename + dmext);
        demHeaderURL = new URL(url, filename + dhext);
        srcURL = new URL(url, filename + srext);
        srcHeaderURL = new URL(url, filename + shext);
        statsURL = new URL(url, filename + stext);
    }

    public com.vividsolutions.jts.geom.Envelope getBbox() {
        com.vividsolutions.jts.geom.Envelope env = null;

        try {
            GT30Header header = new GT30Header(demHeaderURL);
            double xmin = header.getULXMap() - (header.getXDim() / 2);
            double ymax = header.getULYMap() + (header.getYDim() / 2);
            double ymin = ymax - (header.getNRows() * header.getYDim());
            double xmax = xmin + (header.getNCols() * header.getXDim());

            env = new com.vividsolutions.jts.geom.Envelope(xmin, xmax, ymin, ymax);
        } catch (Exception e) {
            // This should not happen!
            throw new IllegalArgumentException("Unexpected error!" + e);
        }

        return env;
    }

    public com.vividsolutions.jts.geom.Envelope getBbox(boolean speed) {
        return getBbox();
    }

    public void setCropEnvelope(com.vividsolutions.jts.geom.Envelope crop) {
        com.vividsolutions.jts.geom.Envelope bbox = getBbox();

        if (bbox.intersects(crop)) {
            cropEnvelope = crop;
        }
    }

    public com.vividsolutions.jts.geom.Envelope getCropEnvelope(
        com.vividsolutions.jts.geom.Envelope env) {
        return cropEnvelope;
    }

    public FeatureCollection getFeatures(Filter filter)
        throws DataSourceException {
        FeatureCollection fc = FeatureCollections.newCollection();
        getFeatures(fc, filter);

        return fc;
    }

    public void getFeatures(FeatureCollection collection, Query query)
        throws DataSourceException {
        // Read the header
        GT30Header header = null;

        try {
            header = new GT30Header(demHeaderURL);
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception", e);
        }

        int nrows = header.getNRows();
        int ncols = header.getNCols();
        int readCols = ncols; // columns effectively read from the datasource
        int readRows = nrows;
        double xdim = header.getXDim();
        double ydim = header.getYDim();
        double minx = header.getULXMap() - (xdim / 2);
        double miny = (header.getULYMap() + (ydim / 2)) - (ydim * nrows);

        // Read the statistics file
        GT30Stats stats = null;

        try {
            stats = new GT30Stats(statsURL);
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception", e);
        }

        int max = stats.getMax();

        // prepare NIO based ImageInputStream
        FileChannelImageInputStream iis = null;

        try {
            String filePath = URLDecoder.decode(demURL.getFile(), "US-ASCII");
            FileInputStream fis = new FileInputStream(filePath);
            FileChannel channel = fis.getChannel();
            iis = new FileChannelImageInputStream(channel);
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception", e);
        }

        // Prepare temporaray colorModel and sample model, needed to build the
        // RawImageInputStream
        ColorSpace graycs = ICC_ColorSpace.getInstance(ICC_ColorSpace.CS_GRAY);
        ColorModel cm = new ComponentColorModel(graycs, false, false, Color.TRANSLUCENT,
                DataBuffer.TYPE_SHORT);

        SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_SHORT, ncols, nrows, 1);
        ImageTypeSpecifier its = new ImageTypeSpecifier(cm, sm);

        // Finally, build the image input stream
        RawImageInputStream raw;
        raw = new RawImageInputStream(iis, its, new long[] { 0 },
                new Dimension[] { new Dimension(ncols, nrows) });

        // if crop needed
        com.vividsolutions.jts.geom.Envelope env = getBbox();
        ImageReadParam irp = null;
        
        // Make some decision about tiling. Let's say that, for the moment, I want
        // to read approximately 128k at a time
        int tileRows = (int) Math.ceil((1024 * 128) / (ncols * 2));
        ImageLayout il = new ImageLayout(0, 0, ncols, nrows, 0, 0, ncols, tileRows, sm, cm);

        // First operator: read the image
        ParameterBlockJAI pbj = new ParameterBlockJAI("ImageRead");
        pbj.setParameter("Input", raw);
        pbj.setParameter("ReadParam", irp);

        // Do not cache these tiles: the file is memory mapped anyway by virtue of
        // using NIO and these tiles are very big and fill up rapidly the cache:
        // better use it to avoid operations down the rendering chaing
        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);
        hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, null));

        RenderedOp image = JAI.create("ImageRead", pbj, hints);

        

        if (cropEnvelope != null) {
            env = intersectEnvelope(env, cropEnvelope);

            float cxmin = (float) Math.round((env.getMinX() - minx) / xdim);
            float cymin = (float) Math.round((env.getMinY() - miny) / ydim);
            float cwidth = (float) Math.round(env.getWidth() / xdim);
            float cheight = (float) Math.round(env.getHeight() / ydim);
            cymin = nrows - cymin - cheight;

            ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(cxmin);
            pb.add(cymin);
            pb.add(cwidth);
            pb.add(cheight);
            hints = new RenderingHints(JAI.KEY_TILE_CACHE, null);
            image = JAI.create("Crop", pb, hints);

            pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(-cxmin);
            pb.add(-cymin);
            image = JAI.create("Translate", pb, hints);
        }

        // Build the coordinate system
        CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
        HorizontalDatum datum = HorizontalDatum.WGS84;
        PrimeMeridian meridian = PrimeMeridian.GREENWICH;
        GeographicCoordinateSystem sourceCS = null;

        try {
            sourceCS = csFactory.createGeographicCoordinateSystem("Geographic - WGS84",
                    Unit.DEGREE, datum, meridian, AxisInfo.LATITUDE, AxisInfo.LONGITUDE);
        } catch (FactoryException fe) {
            throw new DataSourceException("Unexpected error", fe);
        }

        // Create the SampleDimension, with colors and byte transformation needed for visualization
        double offset = 0.0;
        double scale = max / 256.0;
        Category nullValue = new Category("null", null, 0, 1, 1, header.getNoData());
        Category elevation = new Category("elevation", demColors, 1, 256, scale, offset);
        SampleDimension sd = new SampleDimension(new Category[] { nullValue, elevation }, Unit.METRE);
        SampleDimension geoSd = sd.geophysics(true);
        SampleDimension[] bands = new SampleDimension[] { geoSd };
        
        // Create the transform from Grid to real coordinates (and flip NS axis)
        GridRange gr = new GridRange(image);
        GridGeometry gg = new GridGeometry(gr, convertEnvelope(env), new boolean[] {false, true});

        // Finally, create the gridcoverage!
        GridCoverage gc = new GridCoverage("topo", image, sourceCS, gg.getGridToCoordinateSystem(), bands,
                null, null); //, bands, null, null);

        // last step, wrap, add the the feature collection and return
        try {
            collection.add(wrapGcInFeature(gc));
        } catch (Exception e) {
            throw new DataSourceException("Unexpected error", e);
        }
    }

    private Feature wrapGcInFeature(GridCoverage gc)
        throws IllegalAttributeException, SchemaException {
        // create surrounding polygon
        PrecisionModel pm = new PrecisionModel();
        Rectangle2D rect = gc.getEnvelope().toRectangle2D();
        Coordinate[] coord = new Coordinate[5];
        coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
        coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
        coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
        coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
        coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());

        LinearRing ring = new LinearRing(coord, pm, 0);
        Polygon bounds = new Polygon(ring, pm, 0);

        // create the feature type
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Polygon.class);
        AttributeType grid = AttributeTypeFactory.newAttributeType("grid", GridCoverage.class);
        FeatureType schema = null;
	AttributeType[] attTypes =  { geom, grid };
	//HACK - the name should not be gtopo, but instead the name of the file.
        schema = FeatureTypeFactory.newFeatureType(attTypes, "gtopo");

        // create the feature
        Feature feature = schema.create(new Object[] { bounds, gc });

        return feature;
    }
    
    private Envelope convertEnvelope(com.vividsolutions.jts.geom.Envelope source) {
        double[] min = new double[] { source.getMinX(), source.getMinY() };
        double[] max = new double[] { source.getMaxX(), source.getMaxY() };

        return new Envelope(min, max);
    }

    private com.vividsolutions.jts.geom.Envelope intersectEnvelope(
        com.vividsolutions.jts.geom.Envelope a, com.vividsolutions.jts.geom.Envelope b) {
        com.vividsolutions.jts.geom.Envelope env = null;

        if (a.intersects(b)) {
            env = new com.vividsolutions.jts.geom.Envelope(Math.max(a.getMinX(), b.getMinX()),
                    Math.min(a.getMaxX(), b.getMaxX()), Math.max(a.getMinY(), b.getMinY()),
                    Math.min(a.getMaxY(), b.getMaxY()));
        }

        return env;
    }


    public FeatureType getSchema() {
        return null;
    }

    public Color[] getColors() {
        return demColors;
    }

    public void setColors(Color[] colors) {
        if (colors != null) {
            demColors = colors;
        }
    }


}
