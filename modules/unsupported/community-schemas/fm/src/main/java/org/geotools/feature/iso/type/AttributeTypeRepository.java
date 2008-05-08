package org.geotools.feature.iso.type;

import java.util.Map;
import java.util.NoSuchElementException;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeName;

/**
 * What is this for? It looks like a primitive Schema?
 * @deprecated Is this needed?
 * @author Justin
 */
interface AttributeTypeRepository {
	AttributeType getType(TypeName typeName)throws NoSuchElementException;
	Map/*<Name, AttributeType>*/ getTypes();
	void registerType(AttributeType type);
}
