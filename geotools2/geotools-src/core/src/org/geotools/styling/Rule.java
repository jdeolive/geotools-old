/*
 * Rule.java
 *
 * Created on March 27, 2002, 2:25 PM
 */

package org.geotools.styling;

/**
 * A rule is used to attach a condition to and group the individual
 * symbolizers used for rendering.  The Title and Abstract describe the rule
 * and may be used to generate a legend, as may the LegendGraphic.
 *
 * The Filter, ElseFilter, MinScale, and MaxScale elements allow the selection
 * of features and rendering scales for a rule.  The scale selection works
 * as follows.  When a map is to be rendered, the scale denominator is
 * computed and all rules in all UserStyles that have a scale outside
 * of the request range are dropped.  (This also includes Rules that
 * have an ElseFilter.)  An ElseFilter is simply an ELSE condition to
 * the conditions (Filters) of all other rules in the same UserStyle.
 * The exact meaning of the ElseFilter is determined after Rules have been
 * eliminated for not fitting the rendering scale.  This definition of the
 * behaviour of ElseFilters may seem a little strange, but it allows for
 * scale-dependent and scale-independent ELSE conditions.  For the Filter,
 * only SqlExpression is available for specification, but this is a hack
 * and should be replaced with Filter as defined in WFS.

 * A missing Filter element means "always true".  If a set of Rules has no
 * ElseFilters, then some features may not be rendered (which is presumably
 * the desired behavior).  The Scales are actually scale denominators
 * (as double floats), so "10e6" would be interpreted as 1:10M.  A missing
 * MinScale means there is no lower bound to the scale-denominator range
 * (lim[x->0+](x)), and a missing MaxScale means there is no upper bound
 * (infinity).  0.28mm 
 *
 * TODO: add getFitlers method to interface
 * TODO: add hasElseFilter method to interface
 *
 * @author James Macgill
 */
public interface Rule {
   
    /**
     * The smallest value for scale denominator at which symbolizers contained
     * by this rule should be applied.
     *
     * @return The smallest(inclusive) denomiator value that this rule will be
     *         active for.
     **/
    public double getMinScaleDenominator();
    
    /**
     * The largest value for scale denominator at which symbolizers contained
     * by this rule should be applied.
     *
     * @return The largest(exclusive) denomiator value that this rule will be
     *         active for.
     **/
    public double getMaxScaleDenominator();
    //public Filter[] getFilters();
    //public boolean hasElseFilter();
    
    /**
     * A set of equivlent Graphics in different formats which can be used
     * as a legend against features stylalized by the symbolizers in this
     * rule.
     *
     * @return An array of Graphic objects, any of which can be used as
     *         the legend.
     **/
    public Graphic[] getLegendGraphic();
    
    /**
     * The symbolizers contain the actual styling information for different
     * geometry types.  A single feature may be rendered by more than one
     * of the symbolizers returned by this method.  It is important that the
     * symbolizers be applied in the order in which they are returned if the
     * end result is to be as inteded.
     * All symbolizers should be applied to all features which make it through
     * the filters in this rule regardless of the features geometry.
     * For example, a polygon symbolizer should be applied to line geometries
     * and even points.  If this is not the desired beavior ensure that ether
     * the filters block inapprorpriate features or that the FeatureTypeStyler
     * which contains this rull has its FeatureTypeName or 
     * SymanticTypeIdentifier set appropriatly.
     *
     * @return An array of symbolizers to be applied, in sequence, to all of
     *         the features addressed by the FeatureTypeStyler which contains
     *         this rule.
     */
    public Symbolizer[] getSymbolizers();

}

