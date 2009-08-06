package org.geotools.styling.builder;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.StyleFactory;

public class PointSymbolizerBuilder implements Builder<PointSymbolizer> {
	StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
	String geometry;
	GraphicBuilder graphicBuilder;
	
	PointSymbolizerBuilder geometry(String geometry) {
		this.geometry = geometry;
		return this;
	}
	
	GraphicBuilder graphic() {
		if(graphicBuilder == null)
			graphicBuilder = new GraphicBuilder();
		return graphicBuilder;
	}
	
	public PointSymbolizer build() {
		// TODO: see what the actual default is!
		Graphic graphic;
		if(graphicBuilder != null) {
			graphic = graphicBuilder.build();
		} else {
			graphicBuilder = new GraphicBuilder();
			graphic = graphicBuilder.build();
		}
		PointSymbolizer ps = sf.createPointSymbolizer(graphic, geometry);
		reset();
		
		return ps;
	}

	public PointSymbolizerBuilder reset() {
		this.geometry = null;
		this.graphicBuilder = null;
		
		return this;
	}
	
}
