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
 * The SemanticTypeIdentifier is experimental and is intended to be used to
 * identify using a community-controlled name(s) what the style is suitable to
 * be used for.  For example, a single style may be suitable to use with many
 * different feature types.  The syntax of the SemanticTypeIdentifier string
 * is undefined, but the strings "generic:line_string", "generic:polygon",
 * "generic:point", "generic:text", "generic:raster", and "generic:any"
 * are reserved to indicate that a FeatureTypeStyle may be used with any
 * feature type with the corresponding default geometry type (i.e., no
 * feature properties are referenced in the feature-type style). 
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
    public void setFeatureTypeName(String name);
    public String[] getSymantecTypeIdentifiers();
    public Rule[] getRules();
}

