/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.io.range;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.geotools.feature.NameImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

/**
 * Default implementation of {@link RangeManager}
 * 
 * @author Simone Giannecchini, GeoSolutions.
 */
public class RangeManager  {
	private InternationalString description;

	private Name name;

	private Set<Field> fields;

	private Set<Name> fieldTypesNames;
	
	public RangeManager(final String name,
			final String description,
			final Field field) {
		this(new NameImpl(name), new SimpleInternationalString(description), Collections.singleton(field));
	}	
	
	public RangeManager(final String name,
			final String description,
			final Set<Field> fields) {
		this(new NameImpl(name), new SimpleInternationalString(description), fields);
	}	
	public RangeManager(final Name name,
			final InternationalString description,
			final Set<Field> fields) {
		this.name = name;
		this.description = description;
		this.fields = new LinkedHashSet<Field>(fields);
		fieldTypesNames = new LinkedHashSet<Name>(fields.size());
		for (Field field : fields) {
			fieldTypesNames.add(field.getName());
		}
	}

	/**
	 * Retrieves this {@link RangeManager} description as {@link InternationalString}.
	 * 
	 * @return this {@link RangeManager} description as {@link InternationalString}.
	 */
	public InternationalString getDescription() {
		return description;
	}

	/**
	 * Get the Field by name
	 * 
	 * @param name
	 *            name of the Field in the form "nameSpace:localPart".
	 *            In case of no nameSpace, the simple "localPart" section
	 *            need to be provided
	 * @return Field instance or null if not found
	 */
	public Field getFieldType(String name) {
		for (Field field : fields) {
		    final Name ftName = field.getName();
		    String localPart=name;
		    String nameSpace = "";
		    if (name.contains(":")){
		        final int indexOf = name.lastIndexOf(":");
		        localPart = name.substring(indexOf+1,localPart.length());
		        nameSpace = name.substring(0,indexOf);
		    }
		    final String ftLocalPart = ftName.getLocalPart().toString();
		    final String ftNameSpace = ftName.getNamespaceURI();
		    if (ftLocalPart.equals(localPart)){
		        if (ftNameSpace!=null){
		            if (ftNameSpace.equals(nameSpace))
		                return field;
		        }
		        return field;
		    }
		    
		}
		return null;
	}

	/**
	 * Get the {@link Field} {@link org.opengis.feature.type.Name}s.
	 * 
	 * @return List of {@link Field} names
	 */
	public Set<Name> getFieldTypeNames() {
		return Collections.unmodifiableSet(fieldTypesNames);
	}
	
	/**
	 * Get all the measure types of this Coverage type
	 * 
	 * @return Set of Field instances
	 */
	public Set<Field> getFieldTypes() {
		return Collections.unmodifiableSet(fields);
	}

	/**
	 * Retrieves this {@link RangeManager} {@link org.opengis.feature.type.Name}.
	 * 
	 * @return this {@link RangeManager} {@link org.opengis.feature.type.Name}.
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Get the Number of FieldTypes in the range
	 * 
	 * @return number of measure types
	 */
	public int getNumFieldTypes() {
		return fields.size();
	}
	
	/**
	 * Simple Implementation of toString method for debugging purpose.
	 */
	public String toString(){
	    final StringBuilder sb = new StringBuilder();
	        final String lineSeparator = System.getProperty("line.separator", "\n");
	        sb.append("Name:").append(name.toString()).append(lineSeparator);
	        sb.append("Description:").append(description.toString()).append(lineSeparator);
	        sb.append("FieldTypes:").append(lineSeparator);
	        for (Field field: fields) {
	            sb.append("fieldType:").append(field.toString());
	            sb.append(lineSeparator).append(lineSeparator);
	        }
	        return sb.toString();
	}
}
