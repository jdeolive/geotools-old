package org.geotools.feature.iso.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.StructuralDescriptor;
import org.opengis.feature.type.TypeName;
import org.opengis.util.InternationalString;

/**
 * Base class for complex types.
 * 
 * @author gabriel
 */
public class ComplexTypeImpl extends AttributeTypeImpl implements ComplexType {

	protected Collection/*<StructuralDescriptor>*/ PROPERTIES = null;
	
	protected Collection/*<AttributeDescriptor>*/ SCHEMA = null;

	protected Collection/*<AssociationDescriptor>*/ ASSOCIATIONS = null;
	
	public ComplexTypeImpl(
		TypeName name, Collection schema, boolean identified, boolean isAbstract,
		Set/*<Filter>*/ restrictions, AttributeType superType, InternationalString description
	) {
		
		super(
			name, Collection.class, identified, isAbstract, restrictions, 
			superType, description
		);
		
		PROPERTIES = schema;
		
		SCHEMA = null;
		ASSOCIATIONS = null;
		
		if (schema != null) {
			//split out attributes and associatoins
			try {
				SCHEMA = (Collection) PROPERTIES.getClass().newInstance();
				ASSOCIATIONS = (Collection) PROPERTIES.getClass().newInstance();
			} 
			catch(Exception e) {
				//default to list
				SCHEMA = new ArrayList();
				ASSOCIATIONS = new ArrayList();
			}
			
			for (Iterator itr = PROPERTIES.iterator(); itr.hasNext();) {
				StructuralDescriptor sd = (StructuralDescriptor) itr.next();
				if (sd instanceof AttributeDescriptor) {
					SCHEMA.add(sd);
				}
				else if (sd instanceof AssociationDescriptor) {
					ASSOCIATIONS.add(sd);
				}
			}
		}
		else {
			PROPERTIES = Collections.EMPTY_LIST;
			SCHEMA = Collections.EMPTY_LIST;
			ASSOCIATIONS = Collections.EMPTY_LIST;
		}
	}

	public AttributeType getSuper() {
		return SUPER;
	}

	public Collection getProperties() {
		return PROPERTIES;
	}
	
	public Collection attributes() {
		return SCHEMA;
	}
	
	public Collection associations() {
		return ASSOCIATIONS;
	}

	//package visibility
	void setAttributes(Collection SCHEMA) {
		this.SCHEMA = SCHEMA;
	}
	
	//JD: not sure about this, ask JG
	public boolean isInline() {
		return false;
	}
	
	public boolean equals(Object o){
    	if(!(o instanceof ComplexType)){
    		return false;
    	}
    	if(!super.equals(o)){
    		return false;
    	}
    	if( o  instanceof ComplexTypeImpl )
    	{
    		Collection otherSchema = ((ComplexTypeImpl)o).SCHEMA;
    	   	if(!SCHEMA.equals(otherSchema)){
        		return false;
        	}
    	}
 
    	return true;
    }
    
	public Collection getStructuralProperties() {
		//TODO: associations
		return attributes();
	}
	

	
	
    public int hashCode(){
    	return super.hashCode() * SCHEMA.hashCode();
    }
    
	public String toString() {
		StringBuffer sb = new StringBuffer(Utilities.getShortClassName(this));
		sb.append("[name=").append(getName()).append(", binding=").append(BINDING)
				.append(", abstrsct= ").append(isAbstract()).append(", identified=")
				.append(IDENTIFIED).append(", restrictions=").append(getRestrictions())
				.append(", superType=").append(SUPER).append(", schema=").append(SCHEMA).append("]");

		return sb.toString();
	}
}
