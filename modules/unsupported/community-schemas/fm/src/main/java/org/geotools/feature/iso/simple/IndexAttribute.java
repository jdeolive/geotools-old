/**
 * 
 */
package org.geotools.feature.iso.simple;

import java.util.List;

import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * Attribute that delegates to its parent array features based on index.
 * 
 * @author Jody
 */
class IndexAttribute implements Attribute {
	/**
	 * 
	 */
	ArraySimpleFeature feature;

	int index;

	IndexDescriptor descriptor;

	public IndexAttribute(ArraySimpleFeature feature, int index) {
		this.feature = feature;
		this.index = index;
		descriptor = new IndexDescriptor(this.feature, index);
	}

	public AttributeDescriptor getDescriptor() {
		return descriptor;
	}

	public boolean nillable() {
		return true;
	}

	public AttributeType getType() {
		return this.feature.type.get(index);
	}

	public String getID() {
		return null;
	}

	public Object get() {
		return this.feature.values[index];
	}

	public void set(Object value) throws IllegalArgumentException {
		this.feature.values[index] = value;
	}

	public PropertyDescriptor descriptor() {
		return descriptor;
	}

	public Name name() {
		return descriptor.getName();
	}

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }

}