/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.gce.geotiff.IIOMetadataAdpaters;

/**
 * Quoting the geotiff spec:
 * 
 * <pre>
 *      ModelTiepointTag:
 *      Tag = 33922 (8482.H) 
 *      Type = DOUBLE (IEEE Double precision)
 *      N = 6*K,  K = number of tiepoints
 *      Alias: GeoreferenceTag
 *      Owner: Intergraph
 * </pre>
 * 
 * This tag stores raster->model tiepoint pairs in the order
 * 
 * 
 * <pre>
 *   ModelTiepointTag = (...,I,J,K, X,Y,Z...),
 * </pre>
 * 
 * where (I,J,K) is the point at location (I,J) in raster space with pixel-value
 * K, and (X,Y,Z) is a vector in model space. In most cases the model space is
 * only two-dimensional, in which case both K and Z should be set to zero; this
 * third dimension is provided in anticipation of future support for 3D digital
 * elevation models and vertical coordinate systems.
 * 
 * <p>
 * A raster image may be georeferenced simply by specifying its location, size
 * and orientation in the model coordinate space M. This may be done by
 * specifying the location of three of the four bounding corner points. However,
 * tiepoints are only to be considered exact at the points specified; thus
 * defining such a set of bounding tiepoints does not imply that the model space
 * locations of the interior of the image may be exactly computed by a linear
 * interpolation of these tiepoints.
 * 
 * <p>
 * However, since the relationship between the Raster space and the model space
 * will often be an exact, affine transformation, this relationship can be
 * defined using one set of tiepoints and the "ModelPixelScaleTag", described
 * below, which gives the vertical and horizontal raster grid cell size,
 * specified in model units.
 * 
 * <p>
 * If possible, the first tiepoint placed in this tag shall be the one
 * establishing the location of the point (0,0) in raster space. However, if
 * this is not possible (for example, if (0,0) is goes to a part of model space
 * in which the projection is ill-defined), then there is no particular order in
 * which the tiepoints need be listed.
 * 
 * <p>
 * For orthorectification or mosaicking applications a large number of tiepoints
 * may be specified on a mesh over the raster image. However, the definition of
 * associated grid interpolation methods is not in the scope of the current
 * GeoTIFF spec.
 * 
 * 
 * @author Simone Giannecchini
 * @since 2.3
 */
public final class TiePoint {
	private double[] values = null;

	public TiePoint(double i, double j, double k, double x, double y, double z) {
		values = new double[6];
		set(i, j, k, x, y, z);
	}

	public void set(double i, double j, double k, double x, double y, double z) {
		values[0] = i;
		values[1] = j;
		values[2] = k;
		values[3] = x;
		values[4] = y;
		values[5] = z;
	}

	public double getValueAt(int index) {
		if (index < 0 || index > 5)
			throw new IllegalArgumentException(
					"Provided index should be between 0 and 5");
		return values[index];
	}

	public double[] getData() {
		return values;
	}
	
	public boolean isSet(){
		return values!=null;
	}

}