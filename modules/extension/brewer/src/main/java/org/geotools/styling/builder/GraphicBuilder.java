package org.geotools.styling.builder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Graphic;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbol;
import org.opengis.filter.expression.Expression;

public class GraphicBuilder implements Builder<org.opengis.style.Graphic> {
    boolean unset = false;

    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    List<Symbol> symbols = new ArrayList<Symbol>();

    MarkBuilder markBuilder;

    Expression opacity;

    Expression size;

    Expression rotation;
    public GraphicBuilder(){
        reset();
    }
    public GraphicBuilder( org.opengis.style.Graphic graphic ){
        reset( graphic );
    }
    public GraphicBuilder opacity(Expression opacity) {
        this.opacity = opacity;
        return this;
    }

    public GraphicBuilder size(Expression size) {
        this.size = size;
        return this;
    }

    public GraphicBuilder rotation(Expression rotation) {
        this.rotation = rotation;
        return this;
    }

    public GraphicBuilder externalGraphic(URL onlineResource, String format) {
        symbols.add(sf.createExternalGraphic(onlineResource, format));
        return this;
    }

    public MarkBuilder newMark() {
        if (markBuilder != null)
            symbols.add(markBuilder.build());
        else
            markBuilder = new MarkBuilder();
        return markBuilder;
    }

    public Graphic build() {
        if (markBuilder != null)
            symbols.add(markBuilder.build());
        if (symbols.size() == 0) {
            MarkBuilder builder = new MarkBuilder();
            symbols.add(builder.build());
        }

        Symbol[] symbolsArray = (Symbol[]) symbols.toArray(new Symbol[symbols.size()]);
        Graphic g = sf.createGraphic(null, null, symbolsArray, opacity, size, rotation);

        reset();
        return g;
    }
    public GraphicBuilder unset() {
        unset = true;
        return this;
    }
    public GraphicBuilder reset() {
        unset = false;
        symbols.clear();
        opacity = null;
        size = null;
        rotation = null;

        return this;
    }
    public GraphicBuilder reset( org.opengis.style.Graphic graphic ){
        if( graphic == null ){
            return unset();
        }
        return this;
    }
}
