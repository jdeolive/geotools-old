package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDOnlineResourceBinding;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:OnlineResource.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="OnlineResource" type="se:OnlineResourceType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          An "OnlineResource" is typically used to refer to an HTTP URL.
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
public class OnlineResourceBinding extends SLDOnlineResourceBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.OnlineResource;
    }
}