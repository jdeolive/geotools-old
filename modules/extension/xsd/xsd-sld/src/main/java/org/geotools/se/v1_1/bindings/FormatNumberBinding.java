package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:FormatNumber.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="FormatNumber" substitutionGroup="se:Function" type="se:FormatNumberType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *  Function for formatting numbers to make them human readable.
 *               &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *  &lt;/xsd:element&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * <pre>
 *       <code>
 *  &lt;xsd:complexType name="FormatNumberType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="se:FunctionType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="se:NumericValue"/&gt;
 *                  &lt;xsd:element ref="se:Pattern"/&gt;
 *                  &lt;xsd:element minOccurs="0" ref="se:NegativePattern"/&gt;
 *              &lt;/xsd:sequence&gt;
 *              &lt;xsd:attribute default="." name="decimalPoint"
 *                  type="xsd:string" use="optional"/&gt;
 *              &lt;xsd:attribute default="," name="groupingSeparator"
 *                  type="xsd:string" use="optional"/&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt; 
 *              
 *        </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class FormatNumberBinding extends AbstractComplexBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.FormatNumber;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Class getType() {
        return null;
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

}