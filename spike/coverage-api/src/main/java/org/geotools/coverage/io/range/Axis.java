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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.geotools.feature.NameImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.util.InternationalString;

/**
 * Definition of one axis in a field for which we have some
 * measurements/observations/forecasts. The {@link Axis} data structure
 * describes the nature of each control variable for a certain {@link Field},
 * moreover it indicates as {@link Measurable}s the keys used to control each
 * Field subset.
 * 
 * <p>
 * Note that in order to comply with the WCS spec we need to have the
 * possibility to encode {@link Measurable}s as {@link String}s.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @param V Value being used to define this Axis
 * @param QA Quantity being represented by this Axis
 */
public class Axis<V,Q extends Quantity>{
	private SingleCRS crs;
	private InternationalString description;
	private List<Measure<V,Q>> keys;
	private Name name;
	private Unit<Q> unit;
	
	public Axis( String name, Measure<V,Q> key, Unit<Q> unit){
		this( new NameImpl( name ), new SimpleInternationalString( name ), Collections.singletonList(key), unit, null );
	}
	
	public Axis( String name, List<Measure<V,Q>> keys, Unit<Q> unit){
		this( new NameImpl( name ), new SimpleInternationalString( name ), keys, unit, null );
	}
	
	public Axis( Name name, InternationalString description, List<? extends Measure<V,Q>> keys, Unit<Q> unit){
		this( name, description, keys, unit, null );
	}
	
	public Axis( Name name, InternationalString description, List<? extends Measure<V,Q>> keys, Unit<Q> unit, SingleCRS crs ){
		this.name = name;
		this.unit = unit;
		this.description = description;
		this.keys = new ArrayList<Measure<V,Q>>( keys );
		this.crs = crs;
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
     * Retrieves a specific key for this {@link Axis}.
     * 
     * @return Retrieves a specific key for this {@link Axis}.
     */
	public Measure<V, Q> getKey(int keyIndex) {
		return keys.get( keyIndex );
	}

    /**
     * Retrieves the list of keys for this {@link Axis}.
     * 
     * @return Retrieves the list of keys for this {@link Axis}.
     */
	public List<? extends Measure<V, Q>> getKeys() {
		return Collections.unmodifiableList(keys);
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
     * Retrieves the number of keys for this {@link Axis}.
     * 
     * @return Retrieves the number of keys for this {@link Axis}.
     */
	public int getNumKeys() {
		return keys.size();
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
	
}
