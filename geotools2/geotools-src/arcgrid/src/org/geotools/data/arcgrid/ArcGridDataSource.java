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
import java.awt.image.RenderedImage;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSource;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * ArcGridDataSource datasource
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a> 
 */
public class ArcGridDataSource extends AbstractDataSource implements DataSource {

	private URL srcURL;
	private ArcGridRaster arcGridRaster = null;

	private CoordinateSystem coordinateSystem = GeographicCoordinateSystem.WGS84;

	private Color[] demColors = new Color[] { Color.BLUE, Color.WHITE, Color.RED };
	private String filename = null;

	private GridCoverage gridCoverage = null;

	/**
	 * @return
	 */
	public ArcGridRaster getArcGridRaster() {
		return arcGridRaster;
	}

	/**
	 * @return
	 */
	public GridCoverage getGridCoverage() {
		return gridCoverage;
	}

	/**
	 * @param coordinateSystem
	 */
	public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
		this.coordinateSystem = coordinateSystem;
	}

	/**
	 * Creates a new instance of ArcGridDataSource
	 *
	 * @param url URL pointing to an ArcGrid files (.arx, .asc)
	 *
	 * @throws MalformedURLException
	 */
	public ArcGridDataSource(URL url) throws Exception {
		try {
			filename = URLDecoder.decode(url.getFile(), "US-ASCII");
		} catch (UnsupportedEncodingException use) {
			throw new MalformedURLException(
				"Unable to decode " + url + " cause " + use.getMessage());
		}

		boolean recognized = false;
		String arcext = ".arc";
		String ascext = ".asc";

		if (!filename.toLowerCase().endsWith(arcext) && !filename.toLowerCase().endsWith(ascext))
			throw new MalformedURLException("file extension not recognized: " + filename);

		srcURL = new URL(url, filename);

		try {
			arcGridRaster = new ArcGridRaster(srcURL);
		} catch (Exception e) {
			throw new Exception("Unexpected exception", e);
		}
	}

	public com.vividsolutions.jts.geom.Envelope getBbox() {
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

	public com.vividsolutions.jts.geom.Envelope getBbox(boolean speed) {
		return getBbox();
	}

	public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
		FeatureCollection fc = FeatureCollections.newCollection();
		getFeatures(fc, filter);

		return fc;
	}

	public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {

		final double SCALE = 1; // Scale factor for pixel transcoding.
		final double OFFSET = 0; // Offset factor for pixel transcoding.

		// Create the SampleDimension, with colors and byte transformation needed for visualization
		Category nullValue = new Category("null", null, 0, 1, 1, arcGridRaster.getNoData());
		Category elevation = new Category("elevation", demColors, 1, 256, SCALE, OFFSET);

		SampleDimension sd =
			new SampleDimension(new Category[] { nullValue, elevation }, Unit.METRE);
		SampleDimension geoSd = sd.geophysics(true);
		SampleDimension[] bands = new SampleDimension[] { geoSd };

		RenderedImage image = arcGridRaster.getImage();

		gridCoverage =
			new GridCoverage(
				"ArcGrid Coverage",
				image,
				coordinateSystem,
				convertEnvelope(getBbox()),
				bands,
				null,
				null);

		// last step, wrap, add the the feature collection and return
		try {
			collection.add(wrapGcInFeature(gridCoverage));
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
		AttributeType[] attTypes = { geom, grid };
		//HACK - the name should not be arcgrid, but instead the name of the file.
		schema = FeatureTypeFactory.newFeatureType(attTypes, "arcgrid");

		// create the feature
		Feature feature = schema.create(new Object[] { bounds, gc });

		return feature;
	}

	private Envelope convertEnvelope(com.vividsolutions.jts.geom.Envelope source) {
		double[] min = new double[] { source.getMinX(), source.getMinY()};
		double[] max = new double[] { source.getMaxX(), source.getMaxY()};

		return new Envelope(min, max);
	}

	private com.vividsolutions.jts.geom.Envelope intersectEnvelope(
		com.vividsolutions.jts.geom.Envelope a,
		com.vividsolutions.jts.geom.Envelope b) {
		com.vividsolutions.jts.geom.Envelope env = null;

		if (a.intersects(b)) {
			env =
				new com.vividsolutions.jts.geom.Envelope(
					Math.max(a.getMinX(), b.getMinX()),
					Math.min(a.getMaxX(), b.getMaxX()),
					Math.max(a.getMinY(), b.getMinY()),
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

	public com.vividsolutions.jts.geom.Envelope getBounds() throws DataSourceException {
		return getBbox();
	}

}
