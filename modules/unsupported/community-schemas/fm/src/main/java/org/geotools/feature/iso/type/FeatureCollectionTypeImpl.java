package org.geotools.feature.iso.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.TypeName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

public class FeatureCollectionTypeImpl extends FeatureTypeImpl implements
		FeatureCollectionType {

	final protected Set MEMBERS;

	public FeatureCollectionTypeImpl(TypeName name, Collection schema,
			Collection members, AttributeDescriptor defaultGeom,
			CoordinateReferenceSystem crs, boolean isAbstract,
			Set/* <Filter> */restrictions, AttributeType superType,
			InternationalString description

	) {
		super(name, schema, defaultGeom, crs, isAbstract, restrictions,
				superType, description);
		this.MEMBERS = new HashSet(members);
	}

	public Set getMembers() {
		return MEMBERS;
	}

}
