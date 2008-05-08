package org.geotools.feature.type;

import java.util.List;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * AttributeType for hold geometry implementations, maintains CRS information.
 */
public class GeometryTypeImpl extends AttributeTypeImpl implements GeometryType {

	protected CoordinateReferenceSystem CRS;

	public GeometryTypeImpl(
		Name name, Class binding, CoordinateReferenceSystem crs, 
		boolean identified, boolean isAbstract, List<Filter> restrictions, 
		AttributeType superType, InternationalString description
	) {
		super(name, binding, identified, isAbstract, restrictions, superType, description);
		CRS = crs;
	}

	public CoordinateReferenceSystem getCRS() {
		return CRS;
	}

}
