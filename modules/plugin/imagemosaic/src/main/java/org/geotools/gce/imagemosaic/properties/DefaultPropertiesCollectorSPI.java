package org.geotools.gce.imagemosaic.properties;

import java.awt.RenderingHints.Key;
import java.util.Collections;
import java.util.Map;

public abstract class DefaultPropertiesCollectorSPI implements PropertiesCollectorSPI {

	private final String name;
	
	public String getName() {
		return name;
	}

	public DefaultPropertiesCollectorSPI(String name) {
		this.name = name;
	}

	public boolean isAvailable() {
		return true;
	}

	public Map<Key, ?> getImplementationHints() {
		return Collections.emptyMap();
	}

}
