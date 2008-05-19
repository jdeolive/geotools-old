/*
 * Created on 23.07.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.geotools.geometry.iso.util.topology;

import java.util.LinkedList;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BdryRing2D {

	// LinkedList<Edge2D>
	protected LinkedList edges;

	// orientation of the first edge in order be a clockwise oriented ring
	protected boolean orientation;

	/**
	 * @return Returns the boundaryEdges ArrayList<Edge2D>
	 */
	public LinkedList getEdges() {
		return edges;
	}

	/**
	 * @param maxLength
	 */
	public void split(double maxLength) { 
		if (maxLength<=0.0) return;
		LinkedList newEdges = new LinkedList();
		for (int i = 0; i<edges.size();++i) {
			BdryEdge2D edge = (BdryEdge2D)edges.get(i); 
			newEdges.addAll(edge.split(maxLength));
		}
		this.edges = newEdges;
	}


}
