package org.geotools.styling.builder;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Graphic;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.expression.Expression;

public class StrokeBuilder {
	StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
	Expression color;
	Expression width;
	Expression opacity;
	Expression lineCap;
	Expression lineJoin;
	float[] dashArray;
	Expression dashOffset;
	GraphicBuilder fillBuilder;
	GraphicBuilder strokeBuilder;
	
	public StrokeBuilder() {
		reset();
	}
	
	public StrokeBuilder reset() {
		color = Stroke.DEFAULT.getColor();
		width = Stroke.DEFAULT.getWidth();
		opacity = Stroke.DEFAULT.getOpacity();
		lineCap = Stroke.DEFAULT.getLineCap();
		lineJoin = Stroke.DEFAULT.getLineJoin();
		dashArray = Stroke.DEFAULT.getDashArray();
		dashOffset = Stroke.DEFAULT.getDashOffset();
		fillBuilder = null;
		strokeBuilder = null;
		
		return this;
	}

	public StrokeBuilder color(Expression color) {
		this.color = color;
		return this;
	}

	public StrokeBuilder width(Expression width) {
		this.width = width;
		return this;
	}

	public StrokeBuilder opacity(Expression opacity) {
		this.opacity = opacity;
		return this;
	}

	public StrokeBuilder lineCap(Expression lineCap) {
		this.lineCap = lineCap;
		return this;
	}

	public StrokeBuilder lineJoin(Expression lineJoin) {
		this.lineJoin = lineJoin;
		return this;
	}

	public StrokeBuilder dashArray(float[] dashArray) {
		this.dashArray = dashArray;
		return this;
	}

	public StrokeBuilder dashOffet(Expression dashOffet) {
		this.dashOffset = dashOffet;
		return this;
	}
	
	public GraphicBuilder graphicStroke() {
		if(strokeBuilder == null)
			strokeBuilder = new GraphicBuilder();
		return strokeBuilder;
	}
	
	public GraphicBuilder fillBuilder() {
		if(fillBuilder == null)
			fillBuilder = new GraphicBuilder();
		return fillBuilder;
	}
	
	Stroke build() {
		Graphic graphicFill = fillBuilder != null ? fillBuilder.build() : null;
		Graphic graphicStroke = strokeBuilder != null ? strokeBuilder.build() : null;
		Stroke stroke = sf.createStroke(color, width, opacity, lineJoin, lineCap, dashArray, dashOffset, graphicFill, graphicStroke);
		reset();
		
		return stroke;	
	}
}
