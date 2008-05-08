package org.geotools.feature.iso.attribute;

import org.geotools.feature.iso.AttributeImpl;
import org.opengis.feature.type.AttributeType;
import org.opengis.geometry.BoundingBox;

public class BoundingBoxAttribute extends AttributeImpl implements
		org.opengis.feature.simple.BoundingBoxAttribute {

	public BoundingBoxAttribute(BoundingBox content, AttributeType type) {
		super(content,type,null);
	}
}
