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
import org.geotools.cs.AxisInfo;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.FactoryException;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.PrimeMeridian;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.gc.GridCoverage;
import org.geotools.gc.GridGeometry;
import org.geotools.gc.GridRange;
import org.geotools.pt.Envelope;
import org.geotools.units.Unit;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;


/**
 * A data source designed to read the GTOPO30 file format, a publicly available
 * world wide DEM. For more information, and to get the free data, visit <A
 * HREF="http://edcdaac.usgs.gov/gtopo30/gtopo30.html">GTOP030 web site</A>
 *
 * @author aaime
 */
public class GTopo30DataSource extends AbstractDataSource {
    /**
     * Let's say that, for the moment, I want to read approximately 128k at a
     * time
     */
    private static final int TILE_SIZE = 1024 * 128;

    /** Dem data URL */
    private URL demURL;

    /** Dem data header URL */
    private URL demHeaderURL;

    /** Dem source file URL */
    private URL srcURL;

    /** Dem source header URL */
    private URL srcHeaderURL;

    /** Dem statistics file URL */
    private URL statsURL;

    /** Cropping evenlope if the user doesn't want to get out the whole file */
    private com.vividsolutions.jts.geom.Envelope cropEnvelope;

    /** Preset colors used to generate an Image from the raw data */
    private Color[] demColors = new Color[] {
            new Color(5, 90, 5), new Color(150, 200, 150),
            new Color(190, 150, 20), new Color(100, 100, 50),
            new Color(200, 210, 220), Color.WHITE, Color.WHITE, Color.WHITE,
            Color.WHITE
        };

    /** Contains the file name, without extension */
    private String filename;

    /**
     * Creates a new instance of GTopo30DataSource
     *
     * @param url URL pointing to one of the GTopo30 files (.dem, .hdr, .src,
     *        .sch, .stx)
     *
     * @throws MalformedURLException if the URL does not correspond to one of
     *         the GTOPO30 files
     */
    public GTopo30DataSource(final URL url) throws MalformedURLException {
        try {
            filename = URLDecoder.decode(url.getFile(), "US-ASCII");
        } catch (UnsupportedEncodingException use) {
            throw new MalformedURLException(
                "Unable to decode " + url + " cause " + use.getMessage());
        }

        boolean recognized = false;
        String dmext = ".dem";
        String dhext = ".hdr";
        String srext = ".src";
        String shext = ".sch";
        String stext = ".stx";

        if (
            filename.endsWith(dmext) || filename.endsWith(dhext)
                || filename.endsWith(srext) || filename.endsWith(shext)
                || filename.endsWith(stext)) {
            recognized = true;
        } else {
            dmext = dmext.toUpperCase();
            dhext = dhext.toUpperCase();
            srext = srext.toUpperCase();
            shext = shext.toUpperCase();
            stext = stext.toUpperCase();

            if (
                filename.endsWith(dmext) || filename.endsWith(dhext)
                    || filename.endsWith(srext) || filename.endsWith(shext)
                    || filename.endsWith(stext)) {
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

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public com.vividsolutions.jts.geom.Envelope getBounds() {
        com.vividsolutions.jts.geom.Envelope env = null;

        try {
            GT30Header header = new GT30Header(demHeaderURL);
            double xmin = header.getULXMap() - (header.getXDim() / 2);
            double ymax = header.getULYMap() + (header.getYDim() / 2);
            double ymin = ymax - (header.getNRows() * header.getYDim());
            double xmax = xmin + (header.getNCols() * header.getXDim());

            env = new com.vividsolutions.jts.geom.Envelope(
                    xmin, xmax, ymin, ymax);
        } catch (Exception e) {
            // This should not happen!
            throw new RuntimeException("Unexpected error!" + e);
        }

        return env;
    }

    /**
     * Sets an envelope that will be used to crop the source data in order to
     * get fewer data from the file
     *
     * @param crop the rectangle that will be used to extract data from the
     *        file
     */
    public void setCropEnvelope(
        final com.vividsolutions.jts.geom.Envelope crop) {
        com.vividsolutions.jts.geom.Envelope bbox = getBounds();

        if (bbox.intersects(crop)) {
            cropEnvelope = crop;
        }
    }

    /**
     * Returns the current crop rectangle
     *
     * @return the current crop rectangle (null if not set)
     */
    public com.vividsolutions.jts.geom.Envelope getCropEnvelope() {
        return cropEnvelope;
    }

    /**
     * Loads the DEM from the file, wraps it in a feature that will be included
     * into the returned collection, provided that the DEM passes the filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter)
        throws DataSourceException {
        FeatureCollection fc = FeatureCollections.newCollection();
        getFeatures(fc, filter);

        return fc;
    }

    /**
     * Loads the DEM from the file, wraps it in a feature that will be included
     * into the returned collection, provided that it satisfies the passed
     * query.
     *
     * @param collection The collection to put the features into.
     * @param query a datasource query object.  It encapsulates requested
     *        information, such as tableName, maxFeatures and filter.
     *
     * @throws DataSourceException For all data source errors.
     */
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
        ColorModel cm = new ComponentColorModel(
                graycs, false, false, Color.TRANSLUCENT, DataBuffer.TYPE_SHORT);

        SampleModel sm = new BandedSampleModel(
                DataBuffer.TYPE_SHORT, ncols, nrows, 1);
        ImageTypeSpecifier its = new ImageTypeSpecifier(cm, sm);

        // Finally, build the image input stream
        RawImageInputStream raw;
        raw = new RawImageInputStream(
                iis, its, new long[] {0},
                new Dimension[] {new Dimension(ncols, nrows)});

        // if crop needed
        com.vividsolutions.jts.geom.Envelope env = getBounds();
        ImageReadParam irp = null;

        // Make some decision about tiling.
        int tileRows = (int) Math.ceil(TILE_SIZE / (ncols * 2));
        ImageLayout il = new ImageLayout(
                0, 0, ncols, nrows, 0, 0, ncols, tileRows, sm, cm);

        // First operator: read the image
        ParameterBlockJAI pbj = new ParameterBlockJAI("ImageRead");
        pbj.setParameter("Input", raw);
        pbj.setParameter("ReadParam", irp);

        // Do not cache these tiles: the file is memory mapped anyway by
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
            sourceCS = csFactory.createGeographicCoordinateSystem(
                    "Geographic - WGS84", Unit.DEGREE, datum, meridian,
                    AxisInfo.LATITUDE, AxisInfo.LONGITUDE);
        } catch (FactoryException fe) {
            throw new DataSourceException("Unexpected error", fe);
        }

        // Create the SampleDimension, with colors and byte transformation 
        // needed for visualization
        double offset = 0.0;
        double scale = max / 256.0;
        Category nullValue = new Category(
                "null", null, 0, 1, 1, header.getNoData());
        Category elevation = new Category(
                "elevation", demColors, 1, 256, scale, offset);
        SampleDimension sd = new SampleDimension(
                new Category[] {nullValue, elevation}, Unit.METRE);
        SampleDimension geoSd = sd.geophysics(true);
        SampleDimension[] bands = new SampleDimension[] {geoSd};

        // Create the transform from Grid to real coordinates (and flip NS axis)
        GridRange gr = new GridRange(image);
        GridGeometry gg = new GridGeometry(
                gr, convertEnvelope(env), new boolean[] {false, true});

        // Finally, create the gridcoverage!
        GridCoverage gc = new GridCoverage(
                "topo", image, sourceCS, gg.getGridToCoordinateSystem(), bands,
                null, null); //, bands, null, null);

        // last step, wrap, add the the feature collection and return
        try {
            collection.add(wrapGcInFeature(gc));
        } catch (Exception e) {
            throw new DataSourceException("Unexpected error", e);
        }
    }

    /**
     * Convenience method to wrap a GridCoverage in a feature for inclusion in
     * a FeatureCollection
     *
     * @param gc the GridCoverage to be passed
     *
     * @return a feature wrapping the grid coverage
     *
     * @throws RuntimeException Should never happens, if so, there's a bug in
     *         the feature building code
     */
    private Feature wrapGcInFeature(GridCoverage gc) {
        // create surrounding polygon
        PrecisionModel pm = new PrecisionModel();
        Rectangle2D rect = gc.getEnvelope().toRectangle2D();
        Coordinate[] coord = new Coordinate[5];
        coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
        coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
        coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
        coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
        coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());

        Feature feature = null;

        try {
            LinearRing ring = new LinearRing(coord, pm, 0);
            Polygon bounds = new Polygon(ring, pm, 0);

            // create the feature type
            AttributeType geom = AttributeTypeFactory.newAttributeType(
                    "geom", Polygon.class);
            AttributeType grid = AttributeTypeFactory.newAttributeType(
                    "grid", GridCoverage.class);
            FeatureType schema = null;
            AttributeType[] attTypes = {geom, grid};

            schema = FeatureTypeFactory.newFeatureType(attTypes, filename);

            // create the feature
            feature = schema.create(new Object[] {bounds, gc});
        } catch (SchemaException e) {
            throw new RuntimeException(
                "A schema exception occurred, that should not happen!", e);
        } catch (IllegalAttributeException e) {
            throw new RuntimeException(
                "An illegal attribute exception occurred, that "
                + "should not happen!", e);
        }

        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param source DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Envelope convertEnvelope(
        com.vividsolutions.jts.geom.Envelope source) {
        double[] min = new double[] {source.getMinX(), source.getMinY()};
        double[] max = new double[] {source.getMaxX(), source.getMaxY()};

        return new Envelope(min, max);
    }

    /**
     * DOCUMENT ME!
     *
     * @param a DOCUMENT ME!
     * @param b DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private com.vividsolutions.jts.geom.Envelope intersectEnvelope(
        com.vividsolutions.jts.geom.Envelope a,
        com.vividsolutions.jts.geom.Envelope b) {
        com.vividsolutions.jts.geom.Envelope env = null;

        if (a.intersects(b)) {
            env = new com.vividsolutions.jts.geom.Envelope(
                    Math.max(a.getMinX(), b.getMinX()),
                    Math.min(a.getMaxX(), b.getMaxX()),
                    Math.max(a.getMinY(), b.getMinY()),
                    Math.min(a.getMaxY(), b.getMaxY()));
        }

        return env;
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * @return the schema of features created by this datasource.
     *
     * @task REVISIT: Our current FeatureType model is not yet advanced enough
     *       to handle multiple featureTypes.  Should getSchema take a
     *       typeName now that  a query takes a typeName, and thus DataSources
     *       can now support multiple types? Or just wait until we can
     *       programmatically make powerful enough schemas?
     */
    public FeatureType getSchema() {
        return null;
    }

    /**
     * Returns the set of colors used to create the image contained in the
     * GridCoverage returned by getFeatures
     *
     * @return the set of colors used to depict the DEM
     */
    public Color[] getColors() {
        return demColors;
    }

    /**
     * Allows the user to set different colors to depict the DEM returned by
     * getFeatures
     *
     * @param colors the new color set
     */
    public void setColors(final Color[] colors) {
        if (colors != null) {
            demColors = colors;
        }
    }
}
