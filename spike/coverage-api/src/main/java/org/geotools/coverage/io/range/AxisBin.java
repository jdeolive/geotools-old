package org.geotools.coverage.io.range;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

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
	public String toString() {
		final StringBuilder builder= new StringBuilder();
		builder.append("Axis  bin description").append("\n");
		builder.append("Name:").append("\t\t\t\t\t").append(name.toString()).append("\n");
		builder.append("Description:").append("\t\t\t\t").append(description.toString()).append("\n");
		builder.append("Value:").append("\t\t\t\t\t").append(value.toString()).append("\n");
		builder.append("Axis:").append("\n").append(axis.toString()).append("\n");
		return builder.toString();
	}

	
}
