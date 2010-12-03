package org.geotools.se.v1_1.bindings;

import org.geotools.filter.visitor.ThreshholdsBelongTo;
import org.geotools.se.v1_1.SE;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/se:ThreshholdsBelongToType.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:simpleType name="ThreshholdsBelongToType"&gt;
 *      &lt;xsd:restriction base="xsd:token"&gt;
 *          &lt;xsd:enumeration value="succeeding"/&gt;
 *          &lt;xsd:enumeration value="preceding"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class ThreshholdsBelongToTypeBinding extends AbstractSimpleBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.ThreshholdsBelongToType;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Class getType() {
        return ThreshholdsBelongTo.class;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Object parse(InstanceComponent instance, Object value) throws Exception {
        if ("succeeding".equals(value)) {
            return ThreshholdsBelongTo.SUCCEEDING;
        }
        if ("preceding".equals(value)) {
            return ThreshholdsBelongTo.PRECEDING;
        }

        return null;
    }

}