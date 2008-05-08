package org.geotools.geometry.jts;


import java.awt.geom.PathIterator;
import java.util.logging.Logger;

/**
 * Subclass that provides a convenient efficient currentSegment(float[] coords)
 * implementation that reuses always the same double array. This class and the
 * associated subclasses are not thread safe.
 * 
 * @author Andrea Aime
 * @source $URL:
 */
public abstract class AbstractLiteIterator implements PathIterator {

	/** The logger for the rendering module. */
	private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.rendering");

	protected double[] dcoords = new double[2];

	/**
	 * @see java.awt.geom.PathIterator#currentSegment(float[])
	 */
	public int currentSegment(float[] coords) {
		int result = currentSegment(dcoords);
		coords[0] = (float) dcoords[0];
		coords[1] = (float) dcoords[1];

		return result;
	}

}
