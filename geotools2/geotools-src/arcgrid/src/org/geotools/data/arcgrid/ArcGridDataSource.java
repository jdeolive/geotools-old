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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
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
import org.geotools.pt.Envelope;
import org.geotools.units.Unit;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;


/**
 * Data source that read the ARC Grid ASCII format. Returns as usual a feature collection with a
 * single feature, the GridCoverage being contained in the "grid" attribute.
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class ArcGridDataSource extends AbstractDataSource {
    /** URL pointing to the arc grid header or data file */
    private URL srcURL;

    /** The raster read from the data file */
    private ArcGridRaster arcGridRaster = null;

    /** The coordinate system associated to the returned GridCoverage */
    private CoordinateSystem coordinateSystem = GeographicCoordinateSystem.WGS84;

    /** Default color ramp */
    private Color[] demColors = new Color[] { Color.BLUE, Color.WHITE, Color.RED };

    /** The grid coverage read from the data file */
    private GridCoverage gridCoverage = null;

    /**
     * Creates a new instance of ArcGridDataSource
     *
     * @param url URL pointing to an ArcGrid files (.arx, .asc)
     *
     * @throws MalformedURLException when the url is malformed
     * @throws DataSourceException for any other problem that may occur reading the file
     */
    public ArcGridDataSource(URL url) throws MalformedURLException, DataSourceException {
        String filename = null;

        try {
            filename = URLDecoder.decode(url.getFile(), "US-ASCII");
        } catch (UnsupportedEncodingException use) {
            throw new MalformedURLException("Unable to decode " + url + " cause "
                + use.getMessage());
        }

        String arcext = ".arc";
        String ascext = ".asc";

        if (!filename.toLowerCase().endsWith(arcext) && !filename.toLowerCase().endsWith(ascext)) {
            throw new MalformedURLException("file extension not recognized: " + filename);
        }

        srcURL = new URL(url, filename);

        try {
            arcGridRaster = new ArcGridRaster(srcURL);
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception", e);
        }
    }

    /**
     * Returns the ArcGridRaster read by the datasource. Use it only for specific needs, it's not a
     * datasource independent method.
     *
     * @return the ArcGridRaster read by the datasource
     */
    public ArcGridRaster getArcGridRaster() {
        return arcGridRaster;
    }

    /**
     * Returns the GridCoverage read by the datasource. Use it if you want to avoid unpacking the
     * getFeatures return
     *
     * @return the GridCoverage read by the datasource
     */
    public GridCoverage getGridCoverage() {
        return gridCoverage;
    }

    /**
     * Sets the coordinate system that will be associated to the GridCoverage
     *
     * @param coordinateSystem the new default coordinate system
     */
    public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * Gets the coordinate system that will be associated to the GridCoverage. The WGS84 coordinate
     * system is used by default
     *
     * @return the coordinate system for GridCoverage creation
     */
    public CoordinateSystem getCoordinateSystem() {
        return this.coordinateSystem;
    }

    /**
     * Gets the bounding box of this datasource using the default speed of this datasource as set
     * by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too expensive for the
     *         method to calculate.
     *
     * @throws IllegalArgumentException if bounds could not be calculated
     */
    public com.vividsolutions.jts.geom.Envelope getBounds() {
        com.vividsolutions.jts.geom.Envelope env = null;

        try {
            double xmin = arcGridRaster.getXlCorner();
            double ymin = arcGridRaster.getYlCorner();
            double xmax = xmin + (arcGridRaster.getNCols() * arcGridRaster.getCellSize());
            double ymax = ymin + (arcGridRaster.getNRows() * arcGridRaster.getCellSize());

            env = new com.vividsolutions.jts.geom.Envelope(xmin, xmax, ymin, ymax);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unexpected error!" + e);
        }

        return env;
    }

    /**
     * Loads features from the datasource into the passed collection, based on the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve. To get all features
     *        use {@link Filter.ALL}
     *
     * @return A feature collection with a single feature, tha contains the GridCoverate in the
     *         "grid" attribute
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
     * Loads features from the datasource into the passed collection, based on the passed query.
     * The FeatureCollection will contain a new Feature, the GridCoverage will be contained in the
     * "grid" attribute.
     *
     * @param collection The collection to put the features into.
     * @param query a datasource query object.  It encapsulates requested information, such as
     *        tableName, maxFeatures and filter.
     *
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Query query)
        throws DataSourceException {
        final double SCALE = 1; // Scale factor for pixel transcoding.
        final double OFFSET = 0; // Offset factor for pixel transcoding.

        // Create the SampleDimension, with colors and byte transformation needed for visualization
        Category nullValue = new Category("null", null, 0, 1, 1, arcGridRaster.getNoData());
        Category elevation = new Category("elevation", demColors, 1, 256, SCALE, OFFSET);

        SampleDimension sd = new SampleDimension(new Category[] { nullValue, elevation }, Unit.METRE);
        SampleDimension geoSd = sd.geophysics(true);
        SampleDimension[] bands = new SampleDimension[] { geoSd };

        RenderedImage image = arcGridRaster.getImage();

        gridCoverage = new GridCoverage("ArcGrid Coverage", image, coordinateSystem,
                convertEnvelope(getBounds()), bands, null, null);

        // last step, wrap, add the the feature collection and return
        try {
            collection.add(wrapGcInFeature(gridCoverage));
        } catch (Exception e) {
            throw new DataSourceException("Unexpected error", e);
        }
    }

    /**
     * Wraps a grid coverage into a Feature
     *
     * @param gc the grid coverage
     *
     * @return a feature with the grid coverage envelope as the geometry and the grid coverage
     *         itself in the "grid" attribute
     *
     * @throws IllegalAttributeException Should never be thrown
     * @throws SchemaException Should never be thrown
     */
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
        AttributeType[] attTypes = { geom, grid };

        //HACK - the name should not be arcgrid, but instead the name of the file.
        schema = FeatureTypeFactory.newFeatureType(attTypes, "arcgrid");

        // create the feature
        Feature feature = schema.create(new Object[] { bounds, gc });

        return feature;
    }

    /**
     * Converts a JTS Envelope into an org.geotools.pt.Envelope
     *
     * @param source the jts envelope
     *
     * @return the equivalent geotools envelope
     */
    private Envelope convertEnvelope(com.vividsolutions.jts.geom.Envelope source) {
        double[] min = new double[] { source.getMinX(), source.getMinY() };
        double[] max = new double[] { source.getMaxX(), source.getMaxY() };

        return new Envelope(min, max);
    }

    /**
     * Retrieves the featureType that features extracted from this datasource will be created with.
     *
     * @return the schema of features created by this datasource.
     */
    public FeatureType getSchema() {
        return null;
    }

    /**
     * Gets the default color ramp used to depict the GridCoverage
     *
     * @return the color ramp
     */
    public Color[] getColors() {
        return demColors;
    }

    /**
     * Sets the default color ramp used to depict the GridCoverage. The GridCoverage will be build
     * with this color ramp, this method must be called before getFeatures to have any effect
     *
     * @param colors the new color ramp
     */
    public void setColors(Color[] colors) {
        if (colors != null) {
            demColors = colors;
        }
    }
}
