/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wps.impl;

import java.util.Collection;

import net.opengis.wps.CRSsType;
import net.opengis.wps.WpsPackage;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>CR Ss Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wps.impl.CRSsTypeImpl#getCRS <em>CRS</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CRSsTypeImpl extends EObjectImpl implements CRSsType {
	/**
	 * The cached value of the '{@link #getCRS() <em>CRS</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCRS()
	 * @generated
	 * @ordered
	 */
	protected EList cRS;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CRSsTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WpsPackage.eINSTANCE.getCRSsType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList getCRS() {
		if (cRS == null) {
			cRS = new EDataTypeEList(String.class, this, WpsPackage.CR_SS_TYPE__CRS);
		}
		return cRS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case WpsPackage.CR_SS_TYPE__CRS:
				return getCRS();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case WpsPackage.CR_SS_TYPE__CRS:
				getCRS().clear();
				getCRS().addAll((Collection)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eUnset(int featureID) {
		switch (featureID) {
			case WpsPackage.CR_SS_TYPE__CRS:
				getCRS().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case WpsPackage.CR_SS_TYPE__CRS:
				return cRS != null && !cRS.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (cRS: ");
		result.append(cRS);
		result.append(')');
		return result.toString();
	}

} //CRSsTypeImpl
