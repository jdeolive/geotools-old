/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Center for Computational Geography
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */
package org.geotools.styling;

import org.opengis.filter.expression.Expression;


/**
 * A PointPlacement specifies how a text label is positioned relative to a
 * geometric point.
 *
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="PointPlacement"&gt;
 *   &lt;xsd:annotation&gt;
 *     &lt;xsd:documentation&gt;
 *       A "PointPlacement" specifies how a text label should be rendered
 *       relative to a geometric point.
 *     &lt;/xsd:documentation&gt;
 *   &lt;/xsd:annotation&gt;
 *   &lt;xsd:complexType&gt;
 *     &lt;xsd:sequence&gt;
 *       &lt;xsd:element ref="sld:AnchorPoint" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Displacement" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Rotation" minOccurs="0"/&gt;
 *     &lt;/xsd:sequence&gt;
 *   &lt;/xsd:complexType&gt;
 * &lt;/xsd:element&gt;
 * </code></pre>
 * </p>
 *
 * <p>
 * $Id$
 * </p>
 *
 * @author Ian Turton
 * @source $URL$
 */
public interface PointPlacement extends LabelPlacement {
    /**
     * Returns the AnchorPoint which identifies the location inside a textlabel
     * to use as an "anchor" for positioning it relative to a point geometry.
     *
     * @return DOCUMENT ME!
     */
    AnchorPoint getAnchorPoint();

    /**
     * sets the AnchorPoint which identifies the location inside a textlabel to
     * use as an "anchor" for positioning it relative to a point geometry.
     *
     * @param anchorPoint DOCUMENT ME!
     */
    void setAnchorPoint(AnchorPoint anchorPoint);

    /**
     * Returns the Displacement which gives X and Y offset displacements to use
     * for rendering a text label near a point.
     *
     * @return DOCUMENT ME!
     */
    Displacement getDisplacement();

    /**
     * sets the Displacement which gives X and Y offset displacements to use
     * for rendering a text label near a point.
     *
     * @param displacement DOCUMENT ME!
     */
    void setDisplacement(Displacement displacement);

    /**
     * Returns the rotation of the label.
     *
     * @return DOCUMENT ME!
     */
    Expression getRotation();

    /**
     * sets the rotation of the label.
     *
     * @param rotation DOCUMENT ME!
     */
    void setRotation(Expression rotation);
}
