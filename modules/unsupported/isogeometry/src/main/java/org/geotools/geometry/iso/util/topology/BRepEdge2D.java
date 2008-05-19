/*
 * Created on 23.07.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.geotools.geometry.iso.util.topology;

import java.awt.geom.Line2D;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class BRepEdge2D extends Line2D {

	protected BRepFace2D surfaceRight;
	
	protected BRepFace2D surfaceLeft;
	
	public Object value;

	protected BRepEdge2D(BRepFace2D surfaceRight, BRepFace2D surfaceLeft) {
		this.surfaceRight = surfaceRight;
		this.surfaceLeft = surfaceLeft;
		this.value = null;
	}
	/**
	 * @return Returns the surfaceRight.
	 */
	public BRepFace2D getSurfaceRight() {
		return surfaceRight;
	}
	/**
	 * @return Returns the surfaceLeft.
	 */
	public BRepFace2D getSurfaceLeft() {
		return surfaceLeft;
	}


}
