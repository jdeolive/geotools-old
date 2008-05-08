/**
 * 
 */
package org.geotools.feature.iso.simple;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;

class IndexDescriptor implements AttributeDescriptor {
	/**
	 * 
	 */
	ArraySimpleFeature feature;
	int index;

	protected IndexDescriptor(ArraySimpleFeature feature, int index) {
		this.feature = feature;
		this.index = index;
	}

	public boolean isNillable() {
		return true;
	}

	public AttributeType getType() {
		return this.feature.type.get(index);
	}

	public int getMinOccurs() {
		return 1;
	}

	public int getMaxOccurs() {
		return 1;
	}

	public void putUserData(Object arg0, Object arg1) {
	}

	public Object getUserData(Object arg0) {
		return null;
	}

	public Name getName() {
		return this.feature.type.get(index).getName();
	}

	public PropertyType type() {
		return this.feature.type.get(index);
	}
}