package org.geotools.styling.builder;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.expression.ExpressionBuilder;
import org.geotools.styling.Fill;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.expression.Expression;

public class FillBuilder implements Builder<org.opengis.style.Fill>{
    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    ExpressionBuilder color = new ExpressionBuilder().unset();

    ExpressionBuilder opacity = new ExpressionBuilder().unset();

    GraphicBuilder graphic = new GraphicBuilder().unset();

    private boolean unset = false;

    public FillBuilder() {
        reset();
    }
    public FillBuilder( org.opengis.style.Fill fill ){
        reset( fill );
    }
    public FillBuilder color(Expression color) {
        this.color.reset( color );
        unset = false;
        return this;
    }

    public FillBuilder opacity(Expression opacity) {
        this.opacity.reset( opacity );
        unset = false;
        return this;
    }
    public ExpressionBuilder opacity(){
        unset = false;
        return opacity;   
    }
    public GraphicBuilder graphicFill() {
        unset = false;
        return graphic;
    }
    public ExpressionBuilder color(){
        this.unset = false;
        return color;
    }

    /**
     * Build Fill as defined; FillBuilder will be reset after this use.
     * 
     * @return Created Fill as defined
     */
    public Fill build() {
        if (unset) {
            return null;
        }
        Fill fill = sf.createFill(
           color.build(),
           null,
           opacity.build(),
           graphic.build() );

        reset();
        return fill;
    }

    public FillBuilder unset() {
        unset = true;
        return this;
    }

    /**
     * Reset to produce the default Fill.
     */
    public FillBuilder reset() {
        unset = false;
        color = new ExpressionBuilder( Fill.DEFAULT.getColor() );
        opacity = new ExpressionBuilder( Fill.DEFAULT.getOpacity() );
        graphic = new GraphicBuilder().unset();
        return this;
    }
    public FillBuilder reset(org.opengis.style.Fill original) {
        unset = false;
        color = new ExpressionBuilder( original.getColor() );
        opacity = new ExpressionBuilder( original.getOpacity() );
        graphic = new GraphicBuilder( original.getGraphicFill() );
        return this;
    }

}
