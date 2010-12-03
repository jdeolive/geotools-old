package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDSelectedChannelTypeBinding;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/se:SelectedChannelType.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:complexType name="SelectedChannelType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element ref="se:SourceChannelName"/&gt;
 *          &lt;xsd:element minOccurs="0" ref="se:ContrastEnhancement"/&gt;
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
public class SelectedChannelTypeBinding extends SLDSelectedChannelTypeBinding {

    public SelectedChannelTypeBinding(StyleFactory styleFactory) {
        super(styleFactory);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.SelectedChannelType;
    }

}