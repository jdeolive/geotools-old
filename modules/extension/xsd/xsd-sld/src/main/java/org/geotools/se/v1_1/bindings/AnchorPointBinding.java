package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDAnchorPointBinding;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:AnchorPoint.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="AnchorPoint" type="se:AnchorPointType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          An "AnchorPoint" identifies the location inside of a text label to
 *          use an an 'anchor' for positioning it relative to a point geometry.
 *        &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *  &lt;/xsd:element&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class AnchorPointBinding extends SLDAnchorPointBinding {

    public AnchorPointBinding(StyleFactory styleFactory) {
        super(styleFactory);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.AnchorPoint;
    }
}