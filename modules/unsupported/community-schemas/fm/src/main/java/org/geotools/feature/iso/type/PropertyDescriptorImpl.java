package org.geotools.feature.iso.type;

import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.type.PropertyDescriptor;

public abstract class PropertyDescriptorImpl implements PropertyDescriptor {

	Map properties = new HashMap();
	
	
	public void putUserData(Object key, Object value) {
		properties.put(key,value);

	}

	public Object getUserData(Object key) {
		return properties.get(key);
	}


}