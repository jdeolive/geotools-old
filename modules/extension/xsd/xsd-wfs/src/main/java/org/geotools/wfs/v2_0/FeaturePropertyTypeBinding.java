package org.geotools.wfs.v2_0;

import javax.xml.namespace.QName;

import org.geotools.gml3.XSDIdRegistry;
import org.geotools.gml3.v3_2.GML;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

/**
 * Binding object for the type http://www.opengis.net/wfs/2.0:FeaturePropertyType.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:complexType name="FeaturePropertyType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="gml:AbstractFeatureMemberType"&gt;
 *              &lt;xsd:sequence minOccurs="0"&gt;
 *                  &lt;xsd:element ref="gml:AbstractFeature"/&gt;
 *              &lt;/xsd:sequence&gt;
 *              &lt;xsd:attribute name="state" type="wfs:StateValueType"/&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class FeaturePropertyTypeBinding extends
        org.geotools.gml3.bindings.FeaturePropertyTypeBinding {

    public FeaturePropertyTypeBinding(XSDIdRegistry idSet) {
        super(idSet);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return WFS.FeaturePropertyType;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {

        // TODO: implement and remove call to super
        return super.parse(instance, node, value);
    }
    
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        if (GML.AbstractFeature.equals(name)) {
            return super.getProperty(object, org.geotools.gml3.GML._Feature);
        }
        return super.getProperty(object, name);
    }

}