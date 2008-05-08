/**
 * 
 */
package org.geotools.feature.iso.attribute;


import org.geotools.feature.iso.AttributeImpl;
import org.opengis.feature.type.AttributeDescriptor;

/**
 * @author Gabriel Roldan
 */
public class BooleanAttribute extends AttributeImpl 
	implements org.opengis.feature.simple.BooleanAttribute {

	public BooleanAttribute(Boolean content,AttributeDescriptor type) {
		super(content, type, null);
	}

	protected Object parse(Object value) throws IllegalArgumentException {
		Class type = getType().getBinding();
		
		// handle null values first
		if (value == null) {
			return value;
		}

		// no parse needed here if types are compatable
		if ((value.getClass() == type)
				|| type.isAssignableFrom(value.getClass())) {
			return value;
		}

		// if it is not 0 or 1, fails
		if (value instanceof Number) {
			Number num = (Number)value;
			return Boolean.valueOf(DateUtil.parseBoolean(String.valueOf(num.intValue())));
		}

		if (value instanceof CharSequence) {
			return Boolean.valueOf(DateUtil.parseBoolean(value.toString()));
		}
		
		// nothing else to do
		throw new IllegalArgumentException("Cannot parse " + value.getClass());
	}

}
