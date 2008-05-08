package org.geotools.feature.iso.type;

import java.util.Set;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.GeometryFactory;

public class GeometryTypeImpl extends AttributeTypeImpl implements GeometryType {

	protected GeometryFactory geometryFactory;

	/**
	 * CoordianteSystem used by this GeometryAttributeType NOT used yet, needs
	 * to incorporate the functionality from the old GeometricAttributeType
	 */
	private CoordinateReferenceSystem CRS;

	public GeometryTypeImpl(
		TypeName name, Class binding, CoordinateReferenceSystem crs, 
		boolean identified, boolean isAbstract, Set restrictions, 
		AttributeType superType, InternationalString description
	) {
		super(name, binding, identified, isAbstract, restrictions, superType, description);
		CRS = crs;
	}

	public CoordinateReferenceSystem getCRS() {
		return CRS;
	}

}
