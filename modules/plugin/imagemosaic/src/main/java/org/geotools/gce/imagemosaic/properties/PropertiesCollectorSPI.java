package org.geotools.gce.imagemosaic.properties;

import java.util.List;

import org.geotools.factory.OptionalFactory;

public interface PropertiesCollectorSPI extends OptionalFactory {
	
	public PropertiesCollector create(
			final Object o,
			final List<String> propertyNames);
	
	public String getName();
}
