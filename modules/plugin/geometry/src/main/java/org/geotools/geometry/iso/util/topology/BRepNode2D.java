/*
 * Created on 23.07.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.geotools.geometry.iso.util.topology;

import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BRepNode2D extends Point2D.Double {
	
	/**
	 * @param x
	 * @param y
	 */
	public BRepNode2D(double x, double y) {
		super(x,y);
		edges = null;
		value = null;
	}

	protected LinkedList edges;
	
	public Object value;

	protected void insertEdge(BRepEdge2D edge) {
		if (edges==null) {
			edges = new LinkedList();
		}
		edges.add(edge);
	}
}
