package org.geotools.styling.builder;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.expression.ExpressionBuilder;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Halo;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.style.TextSymbolizer;

public class TextSymbolizerBuilder implements Builder<TextSymbolizer> {

    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    FillBuilder fill = new FillBuilder();

    HaloBuilder halo = new HaloBuilder().unset();

    ExpressionBuilder label = new ExpressionBuilder();

    String geometry;

    boolean unset = false;

    private LabelPlacement labelPlacement = null; // TODO: LabelPlacement builder

    public TextSymbolizerBuilder geometry(String geometry) {
        this.geometry = geometry;
        return this;
    }

    public HaloBuilder halo() {
        return halo;
    }

    public TextSymbolizer build() {
        if (unset) {
            return null;
        }
        Font[] fonts = null;
        TextSymbolizer ts = sf.createTextSymbolizer(fill.build(), fonts, halo.build(), label
                .build(), labelPlacement, geometry);
        reset();
        return ts;
    }

    public TextSymbolizerBuilder unset() {
        reset();
        unset = true;
        return this;
    }

    public TextSymbolizerBuilder reset() {
        fill.reset(); // TODO: default fill for text?
        halo.unset(); // no default halo
        label.unset();
        geometry = null;
        labelPlacement = null;
        unset = false;
        return this;
    }

    public TextSymbolizerBuilder reset(TextSymbolizer symbolizer) {
        fill.reset(symbolizer.getFill()); // TODO: default fill for text?
        halo.reset(symbolizer.getHalo()); // no default halo
        label.reset(symbolizer.getLabel());
        geometry = symbolizer.getGeometryPropertyName();
        labelPlacement = (LabelPlacement) symbolizer.getLabelPlacement();
        unset = false;
        return this;
    }

    public ExpressionBuilder label() {
        unset = false;
        return label;
    }

    /**
     * Expression used to label features.
     * <p>
     * The label expression often refers to an existing property. <br/>
     * <code>textBuilder.label().property("fullname")</code>
     * <p>
     * You may wish to concatenate several expressions together into a single label:<br/>
     * <code>textBuilder.label().function("strConcat").param().property("city").param().literal("!")</code>
     * 
     * @param expr
     * @return ExpressionBuilder for chaining
     */
    public ExpressionBuilder label(Expression expr) {
        label.reset(expr);
        unset = false;
        return label;
    }
}