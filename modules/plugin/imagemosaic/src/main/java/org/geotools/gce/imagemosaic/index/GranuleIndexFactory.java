/**
 * 
 */
package org.geotools.gce.imagemosaic.index;

import java.net.URL;

/**
 * Simple Factory class for creating {@link GranuleIndex} elements for this mosaic.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public abstract class GranuleIndexFactory {

	/**
	 * 
	 */
	private GranuleIndexFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static GranuleIndex createGranuleIndex(final URL url){
		//TODO @todo this is a temporary hack
		return new JTSTRTreeGranuleIndex(url);	
	}

}
