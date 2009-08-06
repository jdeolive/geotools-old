package org.geotools.styling.builder;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;

public class LineSymbolizerBuilder implements Builder<LineSymbolizer> {

	StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

	StrokeBuilder strokeBuilder;
	String geometry;

	public LineSymbolizerBuilder geometry(String geometry) {
		this.geometry = geometry;
		return this;
	}

	public StrokeBuilder stroke() {
		if (strokeBuilder == null)
			strokeBuilder = new StrokeBuilder();
		return strokeBuilder;
	}

	public LineSymbolizer build() {
		Stroke stroke = strokeBuilder == null ? strokeBuilder.build() : Stroke.DEFAULT;
		LineSymbolizer ls = sf.createLineSymbolizer(stroke, geometry);
		reset();
		return ls;
	}

	public LineSymbolizerBuilder reset() {
		strokeBuilder = null;
		return this;
	}
}
