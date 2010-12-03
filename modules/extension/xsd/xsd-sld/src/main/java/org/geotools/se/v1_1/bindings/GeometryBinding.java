package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDGeometryBinding;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:Geometry.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="Geometry" type="se:GeometryType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          A Geometry gives reference to a (the) geometry property of a
 *          feature to be used for rendering.
 *        &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *  &lt;/xsd:element&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:complexType name="GeometryType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element ref="ogc:PropertyName"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt; 
 *      
 *   </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class GeometryBinding extends SLDGeometryBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.Geometry;
    }
}