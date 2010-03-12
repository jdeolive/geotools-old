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
package org.geotools.gce.imagemosaic.properties.elevation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.gce.imagemosaic.properties.PropertiesCollectorSPI;
import org.geotools.gce.imagemosaic.properties.RegExPropertiesCollector;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;

class ElevationFileNameExtractor extends RegExPropertiesCollector {
	private final static Logger LOGGER= Logging.getLogger(ElevationFileNameExtractor.class);


	public ElevationFileNameExtractor(
			PropertiesCollectorSPI spi,
			List<String> propertyNames,
			String regex) {
		super(spi,  propertyNames,regex);

	}

	@Override
	public void setProperties(SimpleFeature feature) {
		
		// get all the matches and convert them in times
		final List<Double> values= new ArrayList<Double>();
		for(String match:getMatches()){
			// try to convert to date
			try {
				values.add(Double.valueOf(match));
			} catch (NumberFormatException e) {
				if(LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE,e.getLocalizedMessage(),e);
			}
			
		}
		
		// set the properties
		int index=0;
		for(String propertyName:getPropertyNames()){
			// set the property
			feature.setAttribute(propertyName, values.get(index++));
			
			// do we have more dates?
			if(index>=values.size())
				return;
		}
	}

}
