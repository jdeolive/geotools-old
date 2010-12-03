package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDOverlapBehaviorBinding;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:OverlapBehavior.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="OverlapBehavior"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          "OverlapBehavior" tells a system how to behave when multiple
 *          raster images in a layer overlap each other, for example with
 *          satellite-image scenes.
 *        &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:simpleType&gt;
 *          &lt;xsd:restriction base="xsd:string"&gt;
 *              &lt;xsd:enumeration value="LATEST_ON_TOP"/&gt;
 *              &lt;xsd:enumeration value="EARLIEST_ON_TOP"/&gt;
 *              &lt;xsd:enumeration value="AVERAGE"/&gt;
 *              &lt;xsd:enumeration value="RANDOM"/&gt;
 *          &lt;/xsd:restriction&gt;
 *      &lt;/xsd:simpleType&gt;
 *  &lt;/xsd:element&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class OverlapBehaviorBinding extends SLDOverlapBehaviorBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.OverlapBehavior;
    }
}