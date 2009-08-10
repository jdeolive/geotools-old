package org.geotools.styling.builder;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;

public class PolygonSymbolizerBuilder implements Builder<PolygonSymbolizer> {
    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    StrokeBuilder stroke = new StrokeBuilder();

    FillBuilder fill = new FillBuilder();

    String geometry = null;

    boolean unset = false;

    PolygonSymbolizerBuilder geometry(String geometry) {
        this.geometry = geometry;
        unset = false;
        return this;
    }

    public StrokeBuilder stroke() {
        unset = false;
        return stroke;
    }

    public FillBuilder fill() {
        unset = false;
        return fill;
    }

    public PolygonSymbolizer build() {
        if( unset ){
            return null;
        }
        PolygonSymbolizer ps = sf.createPolygonSymbolizer(stroke.build(), fill.build(), geometry);
        reset();
        return ps;
    }

    public PolygonSymbolizerBuilder reset() {
        stroke.reset(); // TODO: check what default stroke is for Polygon
        fill.reset(); // TODO: check what default fill is for Polygon
        unset = false;
        return this;
    }
    public PolygonSymbolizerBuilder reset( PolygonSymbolizer symbolizer ) {
        stroke.reset( symbolizer.getStroke() );
        fill.reset( symbolizer.getFill() );
        unset = false;
        return this;
    }
    public PolygonSymbolizerBuilder unset() {
        stroke.unset();
        fill.unset();
        unset = true;
        return this;
    }
}
