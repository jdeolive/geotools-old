package org.geotools.styling.builder;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;

public class PolygonSymbolizerBuilder implements Builder<PolygonSymbolizer> {
	StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
	
	StrokeBuilder strokeBuilder;
	FillBuilder fillBuilder;
	String geometry;
	
	PolygonSymbolizerBuilder geometry(String geometry) {
		this.geometry = geometry;
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


	public PolygonSymbolizer build() {
		Stroke stroke = strokeBuilder == null ? strokeBuilder.build() : null;
		Fill fill = fillBuilder == null ? fillBuilder.build() : null;
		PolygonSymbolizer ps = sf.createPolygonSymbolizer(stroke, fill, geometry);
		reset();
		return ps;
	}

	public PolygonSymbolizerBuilder reset() {
		strokeBuilder = null;
		fillBuilder = null;
		return this;
	}
	
	
}
