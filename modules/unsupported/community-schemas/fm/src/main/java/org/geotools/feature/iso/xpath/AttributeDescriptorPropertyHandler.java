/**
 * 
 */
package org.geotools.feature.iso.xpath;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.geotools.feature.iso.Types;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;

/**
 * JXPath property handler that works on AttributeDescriptor/type
 * 
 * @author Gabriel Roldan
 */
public class AttributeDescriptorPropertyHandler implements DynamicPropertyHandler {

	public String[] getPropertyNames(java.lang.Object o) {
		AttributeType att = (AttributeType) o;
		String[] propNames = null;
		if (att instanceof ComplexType) {
			ComplexType complexType = (ComplexType) att;
			List/*<AttributeDescriptor>*/ childTypes = 
				new ArrayList(complexType.attributes());
			propNames = new String[childTypes.size()];
			for (int i = 0; i < propNames.length; i++) {
				propNames[i] = ((AttributeDescriptor)childTypes.get(i))
					.getName().getLocalPart();
			}
		}
		return propNames;
	}

	/**
	 * Returns the {@linkplain AttributeDescriptor} contained by the
	 * {@linkplain org.geotools.feature.iso.impl.ComplexAttributeImpl}
	 * <code>o</code>.
	 */
	public Object getProperty(Object o, String propName) {
		AttributeDescriptor value = null;
		AttributeDescriptor node = (AttributeDescriptor)o;
		if(node.getName().getLocalPart().equals(propName)){
			return node;
		}
		if(!(node.type() instanceof ComplexType)){
			throw new IllegalArgumentException("can't ask for property "
					+ propName + " of a non complex type: " + node.type());
		}
		ComplexType complex = (ComplexType) node.type();
        value = (AttributeDescriptor) Types.descriptor(complex, propName);
		//value = Descriptors.node(complex, propName);
		return value;
	}

	public void setProperty(Object feature, String propertyName, Object value) {
		throw new UnsupportedOperationException("not yet implemented");
		/*
		 * try { ((Feature) feature).setAttribute(propertyName, value); } catch
		 * (IllegalAttributeException e) { throw new JXPathException("Setting
		 * attribute " + propertyName + ": " + e.getMessage(), e); }
		 */
	}
}