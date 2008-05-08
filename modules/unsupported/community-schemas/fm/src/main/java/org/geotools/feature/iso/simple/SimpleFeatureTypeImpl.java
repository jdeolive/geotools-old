package org.geotools.feature.iso.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.feature.iso.Descriptors;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.type.FeatureTypeImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * Implementation fo SimpleFeatureType, subtypes must be atomic and are stored
 * in a list.
 * 
 * @author Justin
 */
public class SimpleFeatureTypeImpl extends FeatureTypeImpl implements
		SimpleFeatureType {

	// list of types
	List types = null;

	public SimpleFeatureTypeImpl(TypeName name, Collection schema,
			AttributeDescriptor defaultGeometry, CoordinateReferenceSystem crs,
			Set/* <Filter> */restrictions, InternationalString description) {
		super(name, schema, defaultGeometry, crs, false, restrictions, null,
				description);
	}
	
	public SimpleFeatureTypeImpl(TypeName name, List typeList, AttributeType geometryType, CoordinateReferenceSystem crs, Set restrictions, InternationalString description) {
		this( name, SimpleFeatureFactoryImpl.descriptors( typeList ), SimpleFeatureFactoryImpl.geometryName( geometryType), crs, restrictions, description );
		types = typeList;
	}
	
	private SimpleFeatureTypeImpl(TypeName name, List list, TypeName geomName, CoordinateReferenceSystem crs, Set restrictions, InternationalString description) {
		this( name, list, SimpleFeatureFactoryImpl.find( list, geomName ), crs, restrictions, description );
	}
	
	public AttributeType get(Name qname) {
		return Descriptors.type(SCHEMA, qname);
	}

	public AttributeType get(String name) {
		return get(Types.attributeName(name));
	}

	public AttributeType get(int index) {
		return (AttributeType) types().get(index);
	}

	public int indexOf(String arg0) {
		int index = 0;
		for (Iterator itr = SCHEMA.iterator(); itr.hasNext(); index++) {
			AttributeDescriptor descriptor = (AttributeDescriptor) itr.next();
			if (name.equals(descriptor.getName().getLocalPart())) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Number of available attributes
	 */
	public int getNumberOfAttribtues() {
		return SCHEMA.size();
	}

	/**
	 * Types are returned in the perscribed index order.
	 * 
	 * @return Types in prescribed order
	 */
	public List /* List<AttributeType> */types() {
		if (types == null) {
			synchronized (this) {
				if (types == null) {
					types = new ArrayList();
					for (Iterator itr = SCHEMA.iterator(); itr.hasNext();) {
						AttributeDescriptor ad = (AttributeDescriptor) itr
								.next();
						types.add(ad.getType());
					}
				}
			}
		}

		return types;
	}

	public GeometryType defaultGeometry() {
		AttributeDescriptor desc = getDefaultGeometry();
		if (desc != null)
			return (GeometryType) desc.getType();

		return null;
	}

}
