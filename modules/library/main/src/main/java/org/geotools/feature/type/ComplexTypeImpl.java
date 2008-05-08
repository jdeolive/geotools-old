package org.geotools.feature.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.FeatureImplUtils;
import org.geotools.feature.NameImpl;
import org.geotools.resources.Utilities;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

/**
 * Base class for complex types.
 * 
 * @author gabriel
 */
public class ComplexTypeImpl extends AttributeTypeImpl implements ComplexType {

	protected Collection<PropertyDescriptor> properties = null;
	
	public ComplexTypeImpl(
		Name name, Collection<PropertyDescriptor> properties, boolean identified, 
		boolean isAbstract, List<Filter> restrictions, AttributeType superType, 
		InternationalString description
	) {
		
		super(
			name, Collection.class, identified, isAbstract, restrictions, 
			superType, description
		);
		
		this.properties = properties != null ? properties : Collections.EMPTY_LIST;
		
	}

	public Class<Collection<Property>> getBinding() {
	    return (Class<Collection<Property>>) super.getBinding();
	}
	
	public Collection<PropertyDescriptor> getProperties() {
		return FeatureImplUtils.unmodifiable(properties);
	}
	
	public PropertyDescriptor getProperty(Name name) {
	    for ( Iterator<PropertyDescriptor> p = properties.iterator(); p.hasNext(); ) {
	        PropertyDescriptor property = p.next();
	        if ( property.getName().equals( name ) ) {
	            return property;
	        }
	    }
	    
	    return null;
	}
	
	public PropertyDescriptor getProperty(String name) {
	    return getProperty(new NameImpl( name ) );
	}
	
	public boolean isInline() {
	    //JD: at this point "inlining" is unused... we might want to kill it 
	    // from the interface
	    return false;
	}
	
	public boolean equals(Object o){
    	if(!(o instanceof ComplexTypeImpl)){
    		return false;
    	}
    	if(!super.equals(o)){
    		return false;
    	}
    	
    	ComplexTypeImpl other = (ComplexTypeImpl)o;
    	if ( !Utilities.equals( properties, other.properties ) ) {
    		return false;
    	}
    	
    	return true;
    }
    
	public int hashCode(){
    	return super.hashCode() * properties.hashCode();
    }
    
	public String toString() {
		StringBuffer sb = new StringBuffer(Utilities.getShortClassName(this));
		sb.append("[name=").append(getName()).append(", binding=").append(binding)
				.append(", abstrsct= ").append(isAbstract()).append(", identified=")
				.append(identified).append(", restrictions=").append(getRestrictions())
				.append(", superType=").append(superType).append(", schema=").append(properties).append("]");

		return sb.toString();
	}
}
