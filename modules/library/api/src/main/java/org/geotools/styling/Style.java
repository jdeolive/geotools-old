/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.styling;



/**
 * Indicates how geographical content should be displayed (we call this a style for simplicity; in the spec it is called a UserStyle (user-defined style)).
 * <p>
 * The details of this object are taken from the
 * <a href="https://portal.opengeospatial.org/files/?artifact_id=1188">
 * OGC Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="UserStyle"&gt;
 *   &lt;xsd:annotation&gt;
 *     &lt;xsd:documentation&gt;
 *       A UserStyle allows user-defined styling and is semantically
 *       equivalent to a WMS named style.
 *     &lt;/xsd:documentation&gt;
 *   &lt;/xsd:annotation&gt;
 *   &lt;xsd:complexType&gt;
 *     &lt;xsd:sequence&gt;
 *       &lt;xsd:element ref="sld:Name" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Title" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Abstract" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:IsDefault" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:FeatureTypeStyle" maxOccurs="unbounded"/&gt;
 *     &lt;/xsd:sequence&gt;
 *   &lt;/xsd:complexType&gt;
 * &lt;/xsd:element&gt;
 * </code></pre>
 *
 * @source $URL$
 * @version $Id$
 * @author James Macgill
 */
public interface Style {
    /** Style name (machine readable, don't show to users) */
    String getName();

    void setName(String name);

    /**
     * Style Title (human readable name for user interfaces) 
     * 
     * @deprecated replaced by getDescription().getTitle in 2.6.x
     */
    @Deprecated
    String getTitle();

    @Deprecated
    void setTitle(String title);

    /** 
     * Description of this style 
     * 
     * @deprecated replaced by getDesciption().getAbstract() in 2.6.x
     */
    @Deprecated
    String getAbstract();

    @Deprecated
    void setAbstract(String abstractStr);

    /**
     * Indicates that this is the default style.
     */
    boolean isDefault();

    /**
     * Indicates that this is the default style.
     * <p>
     * Assume this is kept for GeoServer enabling a WMS to track
     * which style is considered the default. May consider providing a
     * clientProperties mechanism similar to Swing JComponent allowing
     * applications to mark up the Style content for custom uses.
     * </p>
     * @param isDefault
     */
    void setDefault(boolean isDefault);

    /**
     * Array of FeatureTypeStyles in portrayal order.
     * <p>
     * FeatureTypeStyle entries are rendered in order of appearance in this
     * list.
     * </p>
     * <p>
     * <i>Note: We are using a Array here to continue with Java 1.4 deployment.</i>
     * </p>
     * 
     * @deprecated replaced by a live list in 2.6.x
     */
    @Deprecated
    FeatureTypeStyle[] getFeatureTypeStyles();

    /**
     * @deprecated replaced by a live list in 2.6.x
     */
    @Deprecated
    void setFeatureTypeStyles(FeatureTypeStyle[] types);

    /**
     * @deprecated replaced by a live list in 2.6.x
     */
    @Deprecated
    void addFeatureTypeStyle(FeatureTypeStyle type);

    /**
     * Used to navigate Style information during portrayal.
     *
     * @param visitor
     */
    void accept(StyleVisitor visitor);
}
