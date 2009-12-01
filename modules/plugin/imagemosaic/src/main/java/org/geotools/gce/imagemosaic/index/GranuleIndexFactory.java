/**
 * 
 */
package org.geotools.gce.imagemosaic.index;

import java.io.Serializable;
import java.util.Map;

import org.geotools.data.DataStoreFactorySpi;

/**
 * Simple Factory class for creating {@link GranuleIndex} elements for this mosaic.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public abstract class GranuleIndexFactory {

	/**
	 * Default private constructo to enfoce singleton
	 */
	private GranuleIndexFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static GranuleIndex createGranuleIndex(final  Map<String, Serializable> params){
		return createGranuleIndex(params,true,false, null);
	}
	public static GranuleIndex createGranuleIndex(final  Map<String, Serializable> params, final boolean caching, final boolean create, final DataStoreFactorySpi spi){
		//TODO @todo this is a temporary hack
		return caching?new STRTreeGranuleIndex(params,spi):new GTDataStoreGranuleIndex(params,create,spi);	
	}

}
