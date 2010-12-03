package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:Trim.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Trim" substitutionGroup="se:Function" type="se:TrimType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *  Strips off chars from a string value
 *               &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *  &lt;/xsd:element&gt; 
 *		
 *	  </code>
 *	 </pre>
 *<pre>
 *       <code>
 *  &lt;xsd:complexType name="TrimType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="se:FunctionType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="se:StringValue"/&gt;
 *              &lt;/xsd:sequence&gt;
 *              &lt;xsd:attribute name="stripOffPosition" type="se:stripOffPositionType"/&gt;
 *              &lt;xsd:attribute name="stripOffChar" type="xsd:string"/&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt; 
 *              
 *        </code>
 *       </pre>
 * </p>
 *
 * @generated
 */
public class TrimBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return SE.Trim;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return null;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: implement and remove call to super
		return super.parse(instance,node,value);
	}

}