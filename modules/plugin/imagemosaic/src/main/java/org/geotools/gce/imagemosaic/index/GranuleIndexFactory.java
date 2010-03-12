/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
	 * Default private constructor to enforce singleton
	 */
	private GranuleIndexFactory() {
	}
	
	public static GranuleIndex createGranuleIndex(final  Map<String, Serializable> params){
		return createGranuleIndex(params,true,false, null);
	}
	public static GranuleIndex createGranuleIndex(final  Map<String, Serializable> params, final boolean caching, final boolean create, final DataStoreFactorySpi spi){
		//TODO @todo this is a temporary hack before we have an even stupid SPI mechanism here
		return caching?new STRTreeGranuleIndex(params,spi):new GTDataStoreGranuleIndex(params,create,spi);	
	}

}
