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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.WindowConstants;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * Data source that read the ARC Grid ASCII format. Returns as usual a feature collection with a
 * single feature, the GridCoverage being contained in the "grid" attribute. Loads the raster file
 * but cannot georeference it, default georeferencing is WGS84 lat/long.
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 * @author <a href="mailto:aaime@users.sf.net">Andrea Aime</a>
 */
public class ArcGridDataSource extends AbstractDataSource {
    /** URL pointing to the arc grid header or data file */
    private URL srcURL;
    
    /** The raster read from the data file */
    private ArcGridRaster arcGridRaster = null;
    
    /** The coordinate system associated to the returned GridCoverage */
    private CoordinateSystem coordinateSystem = GeographicCoordinateSystem.WGS84;
    
    /** Default color ramp */
    private Color[] demColors = new Color[] {Color.BLUE, Color.WHITE, Color.RED};
    
    /** The grid coverage read from the data file */
    private java.lang.ref.SoftReference gridCoverage = null;
    
    /** The name of the file, used as the schema name */
    private String name = null;
    
    private boolean compress = false;
    
    private boolean GRASSFormatEnabled = false;
    
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
        
        name = filename.substring(0, filename.length() - 4);
        srcURL = new URL(url, filename);
        
        
    }
    
    public boolean isGRASSFormatEnabled() {
    	return GRASSFormatEnabled;
    }
    
    public void setGRASSFormatEnabled(boolean enabled) {
    	GRASSFormatEnabled = enabled;
    }
    
    /**
     * Use Gzip compression for writing file.
     */
    public void setUseGzipCompression(boolean compress) {
        this.compress = true;
    }
    
    /**
     * Returns the ArcGridRaster read by the datasource. Use it only for specific needs, it's not a
     * datasource independent method.
     *
     * @return the ArcGridRaster read by the datasource
     */
    public ArcGridRaster openArcGridRaster() throws java.io.IOException {
        if (arcGridRaster == null) {
            try {
            	if(GRASSFormatEnabled) {
					arcGridRaster = new GRASSArcGridRaster(srcURL);
            	} else {
					arcGridRaster = new ArcGridRaster(srcURL);
            	}
                
            } catch (Exception e) {
                throw new DataSourceException("Unexpected exception", e);
            }
        }
        return arcGridRaster;
    }
    
    /**
     * Returns the GridCoverage read by the datasource. Use it if you want to avoid unpacking the
     * getFeatures returned feature collection. Use only for specific needs, it's not a datasource
     * independent method.
     *
     * @return the GridCoverage read by the datasource
     */
    public GridCoverage getGridCoverage() throws java.io.IOException {
        if (gridCoverage == null || gridCoverage.get() == null) {
            gridCoverage = new java.lang.ref.SoftReference(createCoverage());
        }
        return (GridCoverage) gridCoverage.get();
    }
    
    protected GridCoverage createCoverage() throws java.io.IOException {
        java.awt.image.WritableRaster raster = null;
        
        raster = openArcGridRaster().readRaster();
        
        double[] min = new double[] {arcGridRaster.getMinValue()};
        double[] max = new double[] {arcGridRaster.getMaxValue()};
        
        CoordinateSystem coordinateSystem = getCoordinateSystem();
        if (coordinateSystem == null)
            coordinateSystem = GeographicCoordinateSystem.WGS84;
        
        return new GridCoverage(
            name,
            raster,
            coordinateSystem,
            convertEnvelope(getBounds()),
            min,max,
            null,
            new Color[][] {getColors()},
            null
        );
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
    public Envelope getBounds() {
        com.vividsolutions.jts.geom.Envelope env = null;
        
        double xmin = arcGridRaster.getXlCorner();
        double ymin = arcGridRaster.getYlCorner();
        double xmax = xmin + (arcGridRaster.getNCols() * arcGridRaster.getCellSize());
        double ymax = ymin + (arcGridRaster.getNRows() * arcGridRaster.getCellSize());
        
        env = new com.vividsolutions.jts.geom.Envelope(xmin, xmax, ymin, ymax);
        
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
    
    public void setFeatures(FeatureCollection fc) throws DataSourceException {
        if (fc.size() > 0) {
            Feature f = fc.features().next();
            GridCoverage gc = (GridCoverage) f.getAttribute("grid");
            if (gc == null) {
                AttributeType[] t = f.getFeatureType().getAttributeTypes();
                for (int i = 0, ii = t.length; i < ii; i++) {
                    if (GridCoverage.class.isAssignableFrom(t[i].getType()))
                        gc = (GridCoverage) f.getAttribute(i);
                }
            }
            if (gc != null) {
                java.awt.image.Raster data = gc.getRenderedImage().getData();
                org.geotools.pt.Envelope bounds = gc.getGridGeometry().getEnvelope();
                double xl = bounds.getMinimum(0);
                double yl = bounds.getMinimum(1);
                double cellsize = Math.max(
                    (bounds.getMaximum(0) - xl) / data.getWidth(),
                    (bounds.getMaximum(1) - yl) / data.getHeight()
                );
                try {
                    if(GRASSFormatEnabled) {
						arcGridRaster = new GRASSArcGridRaster(srcURL);
                    } else {
						arcGridRaster = new ArcGridRaster(srcURL);
                    }
                    
                    arcGridRaster.writeRaster(data, xl, yl, cellsize,compress);
                } catch (java.io.IOException ioe) {
                    throw new DataSourceException("IOError writing",ioe);
                }
            }
        }
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
        
        // last step, wrap, add the the feature collection and return
        try {
            GridCoverage gc = getGridCoverage();
            collection.add(wrapGcInFeature(gc));
        } catch (Exception e) {
            throw new DataSourceException("IO error", e);
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
        CoordinateSequenceFactory csf = DefaultCoordinateSequenceFactory.instance();
        GeometryFactory gf = new GeometryFactory(pm, 0);
        Coordinate[] coord = new Coordinate[5];
        Rectangle2D rect = gc.getEnvelope().toRectangle2D();
        coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
        coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
        coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
        coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
        coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
        
        LinearRing ring = new LinearRing(csf.create(coord), gf);
        Polygon bounds = new Polygon(ring, null, gf);
        
        // create the feature type
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Polygon.class);
        AttributeType grid = AttributeTypeFactory.newAttributeType("grid", GridCoverage.class);
        
        FeatureType schema = null;
        AttributeType[] attTypes = {geom, grid};
        
        schema = FeatureTypeFactory.newFeatureType(attTypes, name);
        
        // create the feature
        Feature feature = schema.create(new Object[] {bounds, gc});
        
        return feature;
    }
    
    /**
     * Converts a JTS Envelope into an org.geotools.pt.Envelope
     *
     * @param source the jts envelope
     *
     * @return the equivalent geotools envelope
     */
    private org.geotools.pt.Envelope convertEnvelope(com.vividsolutions.jts.geom.Envelope source) {
        double[] min = new double[] {source.getMinX(), source.getMinY()};
        double[] max = new double[] {source.getMaxX(), source.getMaxY()};
        
        return new org.geotools.pt.Envelope(min, max);
        
    }
    
    /**
     * Retrieves the featureType that features extracted from this datasource will be created with.
     *
     * @return the schema of features created by this datasource.
     */
    public FeatureType getSchema() {
        try {
            AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Polygon.class);
            AttributeType grid = AttributeTypeFactory.newAttributeType("grid", GridCoverage.class);
            
            FeatureType schema = null;
            AttributeType[] attTypes = {geom, grid};
            
            return FeatureTypeFactory.newFeatureType(attTypes, name);
        } catch (SchemaException e) {
            // in fact it never happens unless there is a bug in the code
            throw new RuntimeException("Hey, someone broke the ArcGridDataSource.getSchema() code!");
        }
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
    
    public static final void main(String[] args) throws Exception {
        ArcGridDataSource grid = new ArcGridDataSource(new java.io.File(args[0]).toURL());
        org.geotools.feature.Feature g = grid.getFeatures().features().next();
        org.geotools.gc.GridCoverage gc = (org.geotools.gc.GridCoverage) g.getAttribute("grid");
        final java.awt.image.RenderedImage i = gc.getRenderedImage();
        final java.awt.geom.AffineTransform a = new java.awt.geom.AffineTransform();
        javax.swing.JPanel p = new javax.swing.JPanel() {
            public void paint(java.awt.Graphics g) {
                ((java.awt.Graphics2D)g).drawRenderedImage(i, a);
            }
        };
        javax.swing.JFrame f = new javax.swing.JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.getContentPane().add(p);
        f.setSize(i.getWidth(),i.getHeight());
        f.setLocationRelativeTo(null);
        f.show();
    }
}
