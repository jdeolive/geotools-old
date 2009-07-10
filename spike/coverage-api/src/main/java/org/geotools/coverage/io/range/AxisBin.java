package org.geotools.coverage.io.range;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.geotools.feature.NameImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 *
 * @param <V>
 * @param <QA>
 */
public abstract class AxisBin<V, QA extends Quantity>{



	/**
	 * 
	 */
	private static final long serialVersionUID = 227920699316120413L;
	
	/**
	 * The {@link AxisBin} instance that is used as a reference by this {@link AxisBin}.
	 */
	private Axis<QA> axis;

	public AxisBin(
			final Name name, 
			final InternationalString description, 
			final Axis<QA> axis,
			final V value) {
		this.description = description;
		this.name = name;
		this.axis=axis;
		this.value=value;
	}
	
	public AxisBin(
			final Name name, 
			final Axis<QA> axis,
			final V value) {
		this(name, new SimpleInternationalString(name.getLocalPart()), axis, value);
	}
	
	public AxisBin(
			final String name, 
			final String description, 
			final Axis<QA> axis,
			final V value) {
		this(new NameImpl(name), new SimpleInternationalString(description), axis, value);
	}
	
	public AxisBin(
			final String name, 
			final Axis<QA> axis,
			final V value) {
		this(new NameImpl(name), new SimpleInternationalString(name), axis, value);
	}
	
	
	
	private V value;
	private InternationalString description;
	private Name name;
	public InternationalString getDescription(){
		return description;
	}

	public Name getName(){
		return name;
	}


	public Unit<QA> getUnit() {
		return axis.getUnitOfMeasure();
	}

	public V getValue() {
		return value;
	}
	
	public Axis<QA> getAxis(){
		return axis;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axis == null) ? 0 : axis.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		AxisBin other = (AxisBin) obj;
		if (axis == null) {
			if (other.axis != null)
				return false;
		} else if (!axis.equals(other.axis))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	
	@Override
	public String toString() {
		final StringBuilder builder= new StringBuilder();
		builder.append("Axis  bin description").append("\n");
		builder.append("Name:").append("\t\t\t\t\t").append(name.toString()).append("\n");
		builder.append("Description:").append("\t\t\t\t").append(description.toString()).append("\n");
		builder.append("Value:").append("\t\t\t\t\t").append(value.toString()).append("\n");
		builder.append("Axis:").append("\n").append(axis.toString()).append("\n");
		return builder.toString();
	}


	@SuppressWarnings("unchecked")
	public boolean belongsTo(final Axis axis){
		return axis.equals(this.axis);
	}
	
	@SuppressWarnings("unchecked")
	public boolean compatibleWith(final Axis axis){
		return axis.compatibleWith(this.axis);
	}
	
}
