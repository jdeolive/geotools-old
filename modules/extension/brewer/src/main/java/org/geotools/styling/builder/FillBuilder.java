package org.geotools.styling.builder;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.expression.Expression;

public class FillBuilder {
	StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
	
	Expression color;
	Expression opacity;
	GraphicBuilder graphicBuilder;
	
	public FillBuilder color(Expression color) {
		this.color = color;
		return this;
	}
	
	public FillBuilder opacity(Expression opacity) {
		this.opacity = opacity;
		return this;
	}
	
	public GraphicBuilder graphicFill() {
		if(graphicBuilder == null)
			graphicBuilder = new GraphicBuilder();
		return graphicBuilder;
	}
	
	public Fill build() {
		Graphic graphicFill = graphicBuilder != null ? graphicBuilder.build() : null;
		Fill fill = sf.createFill(color, null, opacity, graphicFill);
		reset();
		return fill;
	}

	public void reset() {
		color = Fill.DEFAULT.getColor();
		opacity = Fill.DEFAULT.getOpacity();
		graphicBuilder = null;
	}
	
}
