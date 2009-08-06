package org.geotools.styling.builder;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.ConstantExpression;
import org.geotools.styling.Fill;
import org.geotools.styling.Mark;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.expression.Expression;

public class MarkBuilder {
	StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

	Expression name;
	StrokeBuilder strokeBuilder;
	FillBuilder fillBuilder;
	
	public MarkBuilder() {
		reset();
	}
	
	
	public MarkBuilder wellKnownName(Expression name) {
		this.name = name;
		return this;
	}
	
	public StrokeBuilder stroke() {
		if(strokeBuilder == null)
			strokeBuilder = new StrokeBuilder();
		return strokeBuilder;
	}
	
	public FillBuilder fill() {
		if(fillBuilder == null)
			fillBuilder = new FillBuilder();
		return fillBuilder;
	}
	
	public MarkBuilder reset() {
		// TODO: where is the default mark?
		this.name = CommonFactoryFinder.getFilterFactory(null).literal("square");
		this.strokeBuilder = null;
		this.fillBuilder = null;
		
		return this;
	}
	
	public Mark build() {
		Stroke stroke = strokeBuilder != null ? strokeBuilder.build() : Stroke.DEFAULT;
		Fill fill = fillBuilder != null ? fillBuilder.build() : Fill.DEFAULT;
		// why in the world does the factory accept a size and a rotation? they are part of the Graphics
		Mark mark = sf.createMark(name, stroke, fill, ConstantExpression.constant(12), ConstantExpression.constant(0));
		reset();
		return mark;
	}
}
