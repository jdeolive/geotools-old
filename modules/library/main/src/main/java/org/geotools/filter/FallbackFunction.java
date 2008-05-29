package org.geotools.filter;

import java.util.List;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * A placeholder class used to track a function the user requested
 * that is not supported by our java implementation.
 * <p>
 * This can be used to construct expressions that are to be executed
 * by another systems (say as SQL or as a WFS request).
 * 
 * @author Jody Garnett
 */
public class FallbackFunction extends FunctionExpressionImpl {

    protected FallbackFunction(String name, List params, Literal fallback) {
        super(name, fallback);
        this.setParameters(params);
    }
    public int getArgCount() {
        return 0;
    }
    @Override
    public Object evaluate(Object object) {
        return fallback.evaluate(object);
    }
    @SuppressWarnings("unchecked")
    @Override
    public Object evaluate(Object object, Class context) {
        return fallback.evaluate( object, context );
    }
}
