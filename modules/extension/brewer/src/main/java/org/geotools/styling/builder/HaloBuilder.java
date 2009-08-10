package org.geotools.styling.builder;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.expression.ExpressionBuilder;
import org.geotools.styling.Halo;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.expression.Expression;

public class HaloBuilder implements Builder<org.opengis.style.Halo> {
    boolean unset;

    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    ExpressionBuilder radius;

    FillBuilder fill;

    public HaloBuilder() {
        reset();
    }

    /**
     * Set the HaloBuilder to produce <code>node</code>
     * @return current HaloBuilder for chaining operations
     */
    public HaloBuilder unset() {
        unset = true;
        return this;
    }

    /**
     * Set the HaloBuilder to produce a default Halo.
     * 
     * @return current HaloBuilder for chaining operations
     */
    public HaloBuilder reset() {
        unset = false; // 
        radius = new ExpressionBuilder();
        fill = new FillBuilder();

        return this;
    }

    /**
     * Set the HaloBuilder to produce the provided Halo.
     * 
     * @param halo Halo under construction; if null HaloBuilder will be unset()
     * @return current HaloBuilder for chaining operations
     */
    public HaloBuilder reset(org.opengis.style.Halo halo) {
        if( halo == null ){
            return unset();
        }
        fill = new FillBuilder( halo.getFill() );
        radius = new ExpressionBuilder( halo.getRadius() );
        
        return this;
    }

    public HaloBuilder radius(Expression radius) {
        this.radius.reset( radius );
        return this;
    }

    public FillBuilder fillBuilder() {
        return fill;
    }

    public Halo build() {
        Halo halo = sf.createHalo(fill.build(), radius.build());
        reset();
        return halo;
    }
}