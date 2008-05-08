package org.geotools.po.bindings;


import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;

/**
 * 
 * @generated
 */
public class POSchemaLocationResolver implements XSDSchemaLocationResolver {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 *	@generated modifiable
	 */
	public String resolveSchemaLocation(XSDSchema xsdSchema, String namespaceURI,  String schemaLocationURI) {
		if (schemaLocationURI == null)
			return null;
			
		//if no namespace given, assume default for the current schema
		if ((namespaceURI == null || "".equals(namespaceURI)) && xsdSchema != null) {
			namespaceURI = xsdSchema.getTargetNamespace();
		}
			
		if ("http://www.geotools.org/po".equals(namespaceURI)) {
			if (schemaLocationURI.endsWith("po.xsd")) {
				return getClass().getResource("po.xsd").toString();
			}
		}
		
		return null;
	}

}