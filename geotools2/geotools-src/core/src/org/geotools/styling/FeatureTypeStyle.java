/*
 * FeatureTypeStyle.java
 *
 * Created on March 27, 2002, 1:16 PM
 */

package org.geotools.styling;

/**
 * How to style a feature type.  This is introduced
 * as a convenient package that can be used independently for feature types,
 * for example in GML Default Styling.  The "layer" concept is discarded
 * inside of this element and all processing is relative to feature types.
 * The FeatureTypeName is allowed to be optional, but only one feature
 * type may be incontext and it must match the syntax and semantics of all
 * attribute references inside of the FeatureTypeStyle.
 *
 * @author  jamesm
 */
public interface FeatureTypeStyle {
    //public String getName();
    //public String getTitle();
    //public String getAbstract();
    
    /**
     * Only features with the type name returned by this method should
     * be styled by this feture type styler
     * @return the name of types that this styler applies to
     */
    public String getFeatureTypeName();
    
    /**
     * Set ths type name of the features that this styler should be
     * applied to.
     * TODO: should a set method be declared in this interface at all?
     * @param name The TypeName of the features to be syled by this instence.
     */
    public void setFeatureTypeName(String name);
    
    /**
     * The SemanticTypeIdentifier is experimental and is intended to be used to
     * identify using a community-controlled name(s) what the style is 
     * suitable to be used for.
     * For example, a single style may be suitable to use with many
     * different feature types.  The syntax of the SemanticTypeIdentifier
     * string is undefined, but the strings "generic:line_string",
     * "generic:polygon", "generic:point", "generic:text",
     * "generic:raster", and "generic:any" are reserved to indicate
     * that a FeatureTypeStyle may be used with any feature type
     * with the corresponding default geometry type (i.e., no feature 
     * properties are referenced in the feature-type style).
     *
     * @return An array of strings representing systemtic types which
     *         could be styled by this instance.
     **/
    public String[] getSymantecTypeIdentifiers();
    
    /**
     * Rules goven the appearence of any given feature to be styled by
     * this styler.  Each rule contains conditions based on scale and
     * feature attribute values, in addition rules contain the symbolizers
     * which should be applied when the rule holds true.
     *
     * @return The full set of rules contained in this styler.
     */
    public Rule[] getRules();
}

