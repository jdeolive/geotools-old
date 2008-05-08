/*
 * Created on 23.07.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.geotools.geometry.iso.util.topology;

import java.util.ArrayList;
import java.util.List;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BdryFace2D extends BRepFace2D {

	protected BdryRing2D extRing;
	protected ArrayList intRings = null;
	
	public BdryFace2D(BdryRing2D extRing, List intRings) {
		this.extRing = extRing;
		if (intRings!=null) {
			this.intRings = new ArrayList(intRings);
		}
	}
	
	
	/**
	 * @return Returns the extRing.
	 */
	public BdryRing2D getExtRing() {
		return extRing;
	}
	/**
	 * @return Returns the intRings.
	 */
	public ArrayList getIntRings() {
		return intRings;
	}

	public void split(double maxLength) {

		extRing.split(maxLength);
	}

}
