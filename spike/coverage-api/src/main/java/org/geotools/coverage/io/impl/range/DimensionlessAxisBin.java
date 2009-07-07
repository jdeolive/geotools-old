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
package org.geotools.coverage.io.impl.range;

import java.awt.image.SampleModel;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.geotools.coverage.io.range.Axis;
import org.geotools.coverage.io.range.AxisBin;
import org.opengis.coverage.SampleDimension;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.util.InternationalString;

/**
 * Implementation of {@link Axis} for multibands images.
 * 
 * <p>
 * This implementation of Axis can be seen as a stub implementation since in
 * this case we do not really have an {@link Axis} for this kind of data, or
 * rather we have an axis that just represents an ordinal or a certain set of .
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @todo add convenience constructor based on {@link SampleDimension} and or
 *       {@link SampleModel}
 */
public class DimensionlessAxisBin extends AxisBin<String,Dimensionless> {

    /**
     * 
     */
    public DimensionlessAxisBin(final Name name,final InternationalString description,final Axis<Dimensionless> axis, final String bandName) {
    	super(name,description,axis,bandName );
    }


    

	/**
     * @see org.geotools.coverage.io.range.Axis#getCoordinateReferenceSystem()
     */
    public SingleCRS getCoordinateReferenceSystem() {
        return null;
    }


    /**
     * @see org.geotools.coverage.io.range.Axis#getUnitOfMeasure()
     */
    public Unit<Dimensionless> getUnitOfMeasure() {
        return Unit.ONE;
    }

}
