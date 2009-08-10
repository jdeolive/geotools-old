package org.geotools.styling.builder;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.expression.ExpressionBuilder;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.expression.Expression;

public class StrokeBuilder implements Builder<Stroke> {
    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    ExpressionBuilder color =  new ExpressionBuilder();

    ExpressionBuilder width =  new ExpressionBuilder();

    ExpressionBuilder opacity =  new ExpressionBuilder();

    ExpressionBuilder lineCap =  new ExpressionBuilder();

    ExpressionBuilder lineJoin =  new ExpressionBuilder();

    float[] dashArray = null;

    ExpressionBuilder dashOffset =  new ExpressionBuilder();

    GraphicBuilder graphicFill = new GraphicBuilder();

    GraphicBuilder graphicStroke = new GraphicBuilder();

    private boolean unset;

    public StrokeBuilder() {
        reset();
    }

    public StrokeBuilder unset() {
        reset();
        unset = true;
        return this;
    }
    public StrokeBuilder reset() {
        color.reset( Stroke.DEFAULT.getColor() );
        width.reset(Stroke.DEFAULT.getWidth() );
        opacity.reset(Stroke.DEFAULT.getOpacity() );
        lineCap.reset(Stroke.DEFAULT.getLineCap() );
        lineJoin.reset(Stroke.DEFAULT.getLineJoin() );
        dashArray = Stroke.DEFAULT.getDashArray();
        dashOffset.reset( Stroke.DEFAULT.getDashOffset() );
        graphicFill.unset();
        graphicStroke.reset();
        unset = false;
        return this;
    }
    public StrokeBuilder reset( Stroke original ) {
        color.reset( original.getColor() );
        width.reset( original.getWidth() );
        opacity.reset(original.getOpacity() );
        lineCap.reset(original.getLineCap() );
        lineJoin.reset(original.getLineJoin() );
        dashArray = original.getDashArray();
        dashOffset.reset( original.getDashOffset() );
        graphicFill.reset( original.getGraphicFill() );
        graphicStroke.reset( original.getGraphicStroke() );
        unset = false;
        return this;
    }

    public StrokeBuilder color(Expression color) {
        this.color.reset( color );
        unset = false;
        return this;
    }

    public StrokeBuilder width(Expression width) {
        this.width.reset( width );
        unset = false;
        return this;
    }

    public StrokeBuilder opacity(Expression opacity) {
        this.opacity.reset( opacity );
        unset = false;
        return this;
    }

    public StrokeBuilder lineCap(Expression lineCap) {
        this.lineCap.reset( lineCap );
        unset = false;
        return this;
    }

    public StrokeBuilder lineJoin(Expression lineJoin) {
        this.lineJoin.reset( lineJoin );
        unset = false;
        return this;
    }

    public StrokeBuilder dashArray(float[] dashArray) {
        this.dashArray = dashArray;
        unset = false;
        return this;
    }

    public StrokeBuilder dashOffet(Expression dashOffet) {
        this.dashOffset.reset( dashOffet );
        unset = false;
        return this;
    }

    public GraphicBuilder graphicStroke() {
        unset = false;
        return graphicStroke;
    }

    public GraphicBuilder fillBuilder() {
        unset = false;
        return graphicFill;
    }

    public Stroke build() {
        if( unset ){
            return null;
        }
        Stroke stroke = sf.createStroke(
                color.build(),
                width.build(), opacity.build(), lineJoin.build(), lineCap.build(), dashArray,
                dashOffset.build(), graphicFill.build(), this.graphicStroke.build());
        reset();
        return stroke;
    }
}
