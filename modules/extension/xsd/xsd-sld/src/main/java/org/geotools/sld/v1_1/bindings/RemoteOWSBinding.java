package org.geotools.sld.v1_1.bindings;

import org.geotools.sld.bindings.SLDRemoteOWSBinding;
import org.geotools.sld.v1_1.SLD;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/sld:RemoteOWS.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="RemoteOWS"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          A RemoteOWS gives a reference to a remote WFS/WCS/other-OWS server. 
 *        &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:complexType&gt;
 *          &lt;xsd:sequence&gt;
 *              &lt;xsd:element ref="sld:Service"/&gt;
 *              &lt;xsd:element ref="se:OnlineResource"/&gt;
 *          &lt;/xsd:sequence&gt;
 *      &lt;/xsd:complexType&gt;
 *  &lt;/xsd:element&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class RemoteOWSBinding extends SLDRemoteOWSBinding {

    public RemoteOWSBinding(StyleFactory styleFactory) {
        super(styleFactory);
    }

}