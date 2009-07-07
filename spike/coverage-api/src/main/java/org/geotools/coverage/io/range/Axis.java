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

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.util.InternationalString;

/**
 * Definition of one axis in a field for which we have some
 * measurements/observations/forecasts. The {@link Axis} data structure
 * describes the nature of each control variable for a certain {@link FieldType}
 * 
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class Axis<Q extends Quantity>{



	private SingleCRS crs;
	private InternationalString description;
	private Name name;
	private Unit<Q> unit;
	
	public Axis( String name, Unit<Q> unit){
		this( new NameImpl( name ), new SimpleInternationalString( name ),unit );
	}
	
	public Axis( Name name, InternationalString description, Unit<Q> unit){
		this.name = name;
		this.unit = unit;
		this.description = description;
	}
	
	@SuppressWarnings("unchecked")
	public Axis( Name name, InternationalString description, SingleCRS crs ){
		this(name, description,  getUoM(crs));
		this.crs = crs;
	}
	
	public Axis( String name, String description, SingleCRS crs ){
		this(new NameImpl(name), new SimpleInternationalString(description),  crs);
	}
	
	public Axis( String name, SingleCRS crs ){
		this(new NameImpl(name), new SimpleInternationalString(name),  crs);
	}
	
	@SuppressWarnings("unchecked")
    private static Unit getUoM(SingleCRS crs) {
		return crs.getCoordinateSystem().getAxis(0).getUnit();
	}

	/**
     * Retrieves the coordinate reference system for this {@link Axis}.
     * 
     * <p>
     * In case the coordinate reference system is present the Unit of measure
     * for its single coordinate axis should conform to the global {@link Unit}
     * for this {@link Axis}.
     * 
     * @return the coordinate reference system for this {@link Axis} or
     *         <code>null</code>, if no coordinate reference system is know
     *         or applicable.
     */
	public SingleCRS getCoordinateReferenceSystem() {
		return crs;
	}

	/**
     * Retrieves the description of the {@link Axis}
     * 
     * @return description of the {@link Axis}
     */
	public InternationalString getDescription() {
		return description;
	}

    /**
     * Retrieves the {@link Axis} name
     * 
     * @return {@link org.opengis.feature.type.Name} of the {@link Axis}s
     */
	public Name getName() {
		return name;
	}

    /**
     * Retrieves the Unit of measure for the various keys of this axis.
     * 
     * In case this {@link Axis} is not made of measurable quantities
     * 
     * @return the Unit of measure for the various keys of this axis.
     */
	public Unit<Q> getUnitOfMeasure() {
		return unit;
	}
	
	public <V> boolean isBinCompatible(final AxisBin<V, Q> bin){
		return bin.getAxis().equals(this);
	}

	@Override
	public String toString() {
		final StringBuilder builder= new StringBuilder();
		builder.append("Axis description").append("\n");
		builder.append("Name:").append("\t\t\t\t\t").append(name.toString()).append("\n");
		builder.append("Description:").append("\t\t\t\t").append(description.toString()).append("\n");
		builder.append("Unit:").append("\t\t\t\t\t").append(unit!=null?unit.toString():"null uom").append("\n");
		builder.append("crs:").append("\t\t\t\t\t").append(crs!=null?crs.toString():"null crs").append("\n");
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crs == null) ? 0 : crs.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Axis that = (Axis) obj;
		
		if (crs == null) {
			if (that.crs != null)
				return false;
		} else if (!CRS.equalsIgnoreMetadata(crs,that.crs))
			return false;
		
		if (description == null) {
			if (that.description != null)
				return false;
		} else if (!description.toString().equalsIgnoreCase(that.description.toString()))
			return false;
		
		if (name == null) {
			if (that.name != null)
				return false;
		} else if (!name.toString().equalsIgnoreCase(that.name.toString()))
			return false;
		
		if (unit == null) {
			if (that.unit != null)
				return false;
		} else if (!unit.equals(that.unit))
			return false;
		return true;
	}
}
