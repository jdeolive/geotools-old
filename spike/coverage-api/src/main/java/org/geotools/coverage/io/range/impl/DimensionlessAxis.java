package org.geotools.coverage.io.range.impl;

import java.awt.image.SampleModel;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.geotools.coverage.io.range.Axis;
import org.geotools.feature.NameImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.coverage.SampleDimension;
import org.opengis.feature.type.Name;
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
public class DimensionlessAxis extends Axis<Dimensionless> {
	

	/**
     * 
     */
    public DimensionlessAxis(final Name name,final InternationalString description) {
    	super(name,description,Unit.ONE );
    }
    
	/**
     * 
     */
    public DimensionlessAxis(final String name,final String description) {
    	super(new NameImpl(name),new SimpleInternationalString(description),Unit.ONE );
    }
    
	/**
     * 
     */
    public DimensionlessAxis(final String name) {
    	super(new NameImpl(name),new SimpleInternationalString(name),Unit.ONE );
    }

}