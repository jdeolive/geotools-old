package org.geotools.styling.builder;

import java.awt.Color;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.expression.ExpressionBuilder;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.expression.Expression;

public class StrokeBuilder implements Builder<Stroke> {
    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    ExpressionBuilder color = new ExpressionBuilder();

    ExpressionBuilder width = new ExpressionBuilder();

    ExpressionBuilder opacity = new ExpressionBuilder();

    ExpressionBuilder lineCap = new ExpressionBuilder();

    ExpressionBuilder lineJoin = new ExpressionBuilder();

    float[] dashArray = null;

    ExpressionBuilder dashOffset = new ExpressionBuilder();

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

    /**
     * Reset stroke to default values.
     */
    public StrokeBuilder reset() {
        color.reset(Stroke.DEFAULT.getColor());
        width.reset(Stroke.DEFAULT.getWidth());
        opacity.reset(Stroke.DEFAULT.getOpacity());
        lineCap.reset(Stroke.DEFAULT.getLineCap());
        lineJoin.reset(Stroke.DEFAULT.getLineJoin());
        dashArray = Stroke.DEFAULT.getDashArray();
        dashOffset.reset(Stroke.DEFAULT.getDashOffset());
        graphicFill.unset();
        graphicStroke.reset();
        unset = false;
        return this;
    }

    /**
     * Reset builder to provided original stroke.
     * 
     * @param stroke
     */
    public StrokeBuilder reset(Stroke stroke) {
        color.reset(stroke.getColor());
        width.reset(stroke.getWidth());
        opacity.reset(stroke.getOpacity());
        lineCap.reset(stroke.getLineCap());
        lineJoin.reset(stroke.getLineJoin());
        dashArray = stroke.getDashArray();
        dashOffset.reset(stroke.getDashOffset());
        graphicFill.reset(stroke.getGraphicFill());
        graphicStroke.reset(stroke.getGraphicStroke());
        unset = false;
        return this;
    }

    public StrokeBuilder color(Expression color) {
        this.color.reset(color);
        unset = false;
        return this;
    }
    public StrokeBuilder color(Color color) {
        this.color.literal(color);
        unset = false;
        return this;
    }
    public StrokeBuilder color(String color) {
        this.color.literal(color);
        unset = false;
        return this;
    }

    public ExpressionBuilder color() {
        return color;
    }

    public StrokeBuilder width(Expression width) {
        this.width.reset(width);
        unset = false;
        return this;
    }
    
    public StrokeBuilder width(int width) {
        this.width.literal( width );
        unset = false;
        return this;
    }
    
    public StrokeBuilder width(double width) {
        this.width.literal( width );
        unset = false;
        return this;
    }

    public ExpressionBuilder width() {
        return width;
    }

    public StrokeBuilder opacity(Expression opacity) {
        this.opacity.reset(opacity);
        unset = false;
        return this;
    }
    public StrokeBuilder opacity(double opacity) {
        this.opacity.literal(opacity);
        unset = false;
        return this;
    }
    public ExpressionBuilder opacity() {
        return opacity;
    }

    public StrokeBuilder lineCap(Expression lineCap) {
        this.lineCap.reset(lineCap);
        unset = false;
        return this;
    }

    public ExpressionBuilder lineCap() {
        return lineCap;
    }

    public StrokeBuilder lineJoin(Expression lineJoin) {
        this.lineJoin.reset(lineJoin);
        unset = false;
        return this;
    }

    public ExpressionBuilder lineJoin() {
        return lineJoin;
    }

    public StrokeBuilder dashArray(float[] dashArray) {
        this.dashArray = dashArray;
        unset = false;
        return this;
    }

    public float[] dashArray() {
        return dashArray;
    }

    public StrokeBuilder dashOffet(Expression dashOffet) {
        this.dashOffset.reset(dashOffet);
        unset = false;
        return this;
    }

    public StrokeBuilder dashOffet(int offset) {
        this.dashOffset.literal( offset );
        unset = false;
        return this;
    }
    
    public StrokeBuilder dashOffet(double offset) {
        this.dashOffset.literal( offset );
        unset = false;
        return this;
    }
    
    public ExpressionBuilder dashOffset() {
        return dashOffset;
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
        if (unset) {
            return null;
        }
        Stroke stroke = sf.createStroke(color.build(), width.build(), opacity.build(), lineJoin
                .build(), lineCap.build(), dashArray, dashOffset.build(), graphicFill.build(),
                this.graphicStroke.build());
        reset();
        return stroke;
    }
}
