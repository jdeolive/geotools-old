package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/se:CoverageStyleType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="CoverageStyleType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element minOccurs="0" ref="se:Name"/&gt;
 *          &lt;xsd:element minOccurs="0" ref="se:Description"/&gt;
 *          &lt;xsd:element minOccurs="0" ref="se:CoverageName"/&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" ref="se:SemanticTypeIdentifier"/&gt;
 *          &lt;xsd:choice maxOccurs="unbounded"&gt;
 *              &lt;xsd:element ref="se:Rule"/&gt;
 *              &lt;xsd:element ref="se:OnlineResource"/&gt;
 *          &lt;/xsd:choice&gt;
 *      &lt;/xsd:sequence&gt;
 *      &lt;xsd:attribute name="version" type="se:VersionType"/&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class CoverageStyleTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return SE.CoverageStyleType;
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