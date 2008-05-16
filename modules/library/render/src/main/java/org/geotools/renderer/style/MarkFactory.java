package org.geotools.renderer.style;

import java.awt.Graphics2D;
import java.awt.Shape;

import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;

/**
 * Symbol handler for a Mark.
 */
public interface MarkFactory {
    /**
     * Turns the specified URL into an Shape, eventually using the Feature
     * attributes to evaluate the expression, or returns <code>null</code> if
     * the factory cannot evaluate this symbolUrl.
     * 
     * @param symbolUrl
     *            the expression that will return the symbol name. Once
     *            evaluated the expression should return something like
     *            <code>plainName</code> or like <code>protocol://path</code>.
     *            See the actual implementations for details on the kind of
     *            supported name.
     * @param feature
     *            The feature that will be used to evaluate the symbolURL
     *            expression (or to extract data from it, think complex attributes, in that
     *            case a visit to the expression and some direct attribute value extraction 
     *            might be needed instead)
     * 
     */
    public Shape getShape(Graphics2D graphics, Expression symbolUrl, Feature feature) throws Exception;
}
