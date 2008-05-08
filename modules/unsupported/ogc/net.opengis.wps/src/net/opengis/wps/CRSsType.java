/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>CR Ss Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Identifies a Coordinate Reference System (CRS) supported for this input or output.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.CRSsType#getCRS <em>CRS</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getCRSsType()
 * @model extendedMetaData="name='CRSsType' kind='elementOnly'"
 * @generated
 */
public interface CRSsType extends EObject {
	/**
	 * Returns the value of the '<em><b>CRS</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to a CRS supported for this Input/Output. 
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>CRS</em>' attribute list.
	 * @see net.opengis.wps.WpsPackage#getCRSsType_CRS()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.AnyURI" required="true"
	 *        extendedMetaData="kind='element' name='CRS'"
	 * @generated
	 */
	EList getCRS();

} // CRSsType
