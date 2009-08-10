package org.geotools.styling.builder;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;

public class LineSymbolizerBuilder implements Builder<LineSymbolizer> {

    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
    StrokeBuilder strokeBuilder = new StrokeBuilder();
    String geometry = null;
    private boolean unset = false;

    public LineSymbolizerBuilder geometry(String geometry) {
        this.geometry = geometry;
        unset = false;
        return this;
    }

    public StrokeBuilder stroke() {
        unset = false;
        return strokeBuilder;
    }

    public LineSymbolizer build() {
        if( unset ){
            return null; // builder was constructed but never used
        }
        Stroke stroke = strokeBuilder == null ? strokeBuilder.build() : Stroke.DEFAULT;
        LineSymbolizer ls = sf.createLineSymbolizer(stroke, geometry);
        reset();
        return ls;
    }

    public LineSymbolizerBuilder reset() {
        strokeBuilder.reset();
        geometry = null;
        unset = false;
        return this;
    }
    
    public LineSymbolizerBuilder reset( LineSymbolizer origional ){
        return this;   
    }
    
    public Builder<LineSymbolizer> unset() {
        strokeBuilder.reset();
        geometry = null;
        unset = true;
        return this;
    }
}
