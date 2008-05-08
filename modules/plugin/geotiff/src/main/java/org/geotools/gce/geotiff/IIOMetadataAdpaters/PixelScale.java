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
 * This class is a placeholder for defining exact affine transformations between
 * raster and model space.
 * 
 * <p>
 * Quoting the geotiff spec:
 * 
 * <pre>
 *          ModelPixelScaleTag:
 *          Tag = 33550
 *          Type = DOUBLE (IEEE Double precision)
 *          N = 3
 *          Owner: SoftDesk
 * </pre>
 * 
 * This tag may be used to specify the size of raster pixel spacing in the model
 * space units, when the raster space can be embedded in the model space
 * coordinate system without rotation, and consists of the following 3 values:
 * 
 * <pre>
 *       ModelPixelScaleTag = (ScaleX, ScaleY, ScaleZ)
 * </pre>
 * 
 * where ScaleX and ScaleY give the horizontal and vertical spacing of raster
 * pixels. The ScaleZ is primarily used to map the pixel value of a digital
 * elevation model into the correct Z-scale, and so for most other purposes this
 * value should be zero (since most model spaces are 2-D, with Z=0).
 * 
 * <p>
 * A single tiepoint in the ModelTiepointTag, together with this tag, completely
 * determine the relationship between raster and model space
 * 
 * @author Simone Giannecchini
 * @since 2.3
 * 
 */
public final class PixelScale {

	private double scaleX;

	private double scaleY;

	private double scaleZ;

	public PixelScale(double scaleX, double scaleY, double scaleZ) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
	}

	public PixelScale() {
		this.scaleX = 0;
		this.scaleY = 0;
		this.scaleZ = 0;
	}

	public double getScaleX() {
		return scaleX;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	public double getScaleZ() {
		return scaleZ;
	}

	public void setScaleZ(double scaleZ) {
		this.scaleZ = scaleZ;
	}

	public double[] getValues() {
		return new double[] { scaleX, scaleY, scaleZ };
	}

	public boolean isSet() {
		return isComponentSet(scaleX) && isComponentSet(scaleY);
	}

	public boolean isSetExtended() {
		return isComponentSet(scaleX) && isComponentSet(scaleY)
				&& isComponentSet(scaleZ);
	}

	/**
	 * Tells me if a component of this {@link PixelScale} is set.
	 * 
	 * @param scale
	 * @return
	 */
	protected boolean isComponentSet(double scale) {
		return !Double.isInfinite(scale) && !Double.isNaN(scale)
				&& Math.abs(scale) > 1E-6;
	}

}
