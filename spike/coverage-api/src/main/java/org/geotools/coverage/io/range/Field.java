package org.geotools.coverage.io.range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.geotools.coverage.io.metadata.MetadataNode;
import org.opengis.coverage.SampleDimension;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

/**
 * A {@link net.opengis.wcs11.FieldType} describes a
 * measure/observation/forecast of a certain quantity. A {@link Field} may
 * be a scalar (numeric or text) value, such as population density, or a vector
 * of many similar values, such as incomes by race, or radiance by wavelength.
 * 
 * <p>
 * A {@link Field} has an associated quantity from the JScience project
 * since the goal of a {@link Field} is to describe a {@link Quantity}.
 * Note that I am referring to quantity in the broader term here. As an instance
 * a {@link Field} could describe the bands of a synthetic RGB image. Now,
 * there might not be a real physical quantity (like Temperature or Pressure)
 * associated to such a quantity, but still we want to be able to capture
 * somehow the concept of digital number as the represented quantity as well as
 * the concept of the bands index or textual representation for the bands.
 * 
 * <p>
 * Note that in our proposal a {@link Field} shall always contain at least
 * one {@link Axis}.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public class Field  {
	/**
	 * The description for this {@link Field}.
	 */
    private InternationalString description;

    /**
     * The {@link Name} for this {@link Field}.
     */
    private Name name;

    /**
     * The {@link List} of {@link Axis} for this {@link Field}.
     */
    private List <? extends Axis<?,?>>axes; 

    /**
     * The list of {@link Name}s for the {@link Axis} instances of this {@link Field}.
     */
    private List<Name> axesNames;    
    
    /**
     * This {@link Map} holds the mapping between the Keys ( {@link Measure} instances) and the {@link SampleDimension}s 
     * for this field.
     * 
     */
    private Map<Measure<?,?>,SampleDimension> keysSampleDimensionsMap;
    
    
    /**
     * The {@link Unit} for this {@link Field}.
     */
    private Unit<? extends Quantity> uom;
	/**
	 * 
	 * @param name
	 * @param description
	 * @param unit
	 * @param axes
	 * @param samples
	 */
	public <V,QA extends Quantity> Field(final Name fieldName,
            final InternationalString fieldDescription,
            final Unit<? extends Quantity>  UoM,
            final List<? extends Axis<V,QA>> axes,
            final Map<? extends Measure<V,QA>,SampleDimension> dimensions) {
        this.name = fieldName;
        this.description = fieldDescription;
        this.axes = new ArrayList<Axis<V,QA>>(axes);
        this.keysSampleDimensionsMap = new HashMap<Measure<?,?>,SampleDimension>(dimensions);
        this.uom = UoM;
        axesNames = new ArrayList<Name>(axes.size());
        for (Axis<?,?> axis : axes) {
            axesNames.add(axis.getName());
        }
    }
    /**
     * {@link List} of all the axes of the {@link Field}
     * 
     * @return a {@link List} of all the {@link Axis} instances for this
     *         {@link Field}
     */
	public List<Axis<?,?>> getAxes() {
		return Collections.unmodifiableList(axes);
	}

    /**
     * {@link List} of all the {@link Axis} instances
     * {@link org.opengis.feature.type.Name}s.
     * 
     * @return a {@link List} of all the {@link Axis} instances
     *         {@link org.opengis.feature.type.Name}s.
     */
    public List<Name> getAxesNames() {
        return Collections.unmodifiableList(axesNames);
    }

    /**
     * Get the Axis by name
     * 
     * @param name
     *                name of the Axis
     * @return Axis instance or null if not found
     */
    public Axis<?,?> getAxis(Name name) {
        for (Axis<?,?> axis : axes) {
            if (axis.getName().toString().equalsIgnoreCase(name.toString()) ||
                    axis.getName().getLocalPart().equalsIgnoreCase(name.getLocalPart()))
                return  axis;
        }
        throw new IllegalArgumentException("Unable to find axis for the specified name.");
    }

    /**
     * Get the description of the {@link Field}
     * 
     * @return description of the {@link Field}
     */
	public InternationalString getDescription() {
		return description;
	}

    /**
     * Get the {@link Field} {@link org.opengis.feature.type.Name}
     * 
     * @return {@link org.opengis.feature.type.Name} of the {@link Field}
     */
	public Name getName() {
		return name;
	}

    /**
     * Look up the SampleDimension by key (as described by Axis)
     * 
     * @param key
     *                key of the SampleDimension
     * @return {@link SampleDimension} instance or null if not found
     */
    public SampleDimension getSampleDimension(Measure<?, ?>key) {
    	if(this.keysSampleDimensionsMap.containsKey(key))
    		return keysSampleDimensionsMap.get(key);
    	throw new IllegalArgumentException("Unable to find SampleDimension for the specified key.");
    }

    /**
     * List the SampleDimensions of the measure.
     * 
     * @return Set of {@link SampleDimension} instances
     */

    public Set<SampleDimension> getSampleDimensions() {
        return new HashSet<SampleDimension>(this.keysSampleDimensionsMap.values());
    }
	
    /**
     * Retrieves the Unit of measure for the values described by this field.
     *  
     * <p>
     * In case this {@link Field} is not made of measurable quantities we
     * return <code>null</code>
     * 
     * @return the Unit of measure for the values described by this field or
     *         <code>null</code> in case this {@link Field} is not made of
     *         measurable quantities
     */
	public Unit<? extends Quantity> getUnitOfMeasure() {
		return uom; // TODO Is this duplicated with sample dimensions ?
	}
	
	  /**
     * Simple Implementation of toString method for debugging purpose.
     */
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        final String lineSeparator = System.getProperty("line.separator", "\n");
        sb.append("Name:").append(name.toString()).append(lineSeparator);
        sb.append("Description:").append(description.toString()).append(lineSeparator);
        sb.append("UoM:").append(uom.toString()).append(lineSeparator);
        sb.append("Axes:").append(lineSeparator);
        for (Axis<?,?> axis : axes) {
            sb.append("   axisName:").append(axis.getName().toString());
            sb.append(" axisDescription:").append(axis.getDescription().toString());
            sb.append(" axisUoM:").append(axis.getUnitOfMeasure().toString());
            List<? extends Measure<?, ?>> axisKeys = axis.getKeys();
            for (Measure<?, ?> measure: axisKeys){
                sb.append(" key:").append(measure.toString());
            }
            sb.append(lineSeparator);
        }
        sb.append("SampleDimensions:").append(lineSeparator);
        for (SampleDimension sampleDimension :  keysSampleDimensionsMap.values()){
            sb.append("SampleDim: ").append(sampleDimension.toString());
        }
        return sb.toString();
    }
    
    /**
     * TODO
     * 
     * @param metadataDomain
     * @return
     */
	public MetadataNode getMetadata(String metadataDomain){
		return null;
		
	}
	
	/** TODO
	 * 
	 * @return
	 */
	public Set<Name> getMetadataDomains(){
		return null;
		
	}


}