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
		return createGranuleIndex(url,true,false);
	}
	public static GranuleIndex createGranuleIndex(final URL url, final boolean caching, final boolean create){
		//TODO @todo this is a temporary hack
		return caching?new STRTreeGranuleIndex(url):new GTDataStoreGranuleIndex(url,create);	
	}

}
