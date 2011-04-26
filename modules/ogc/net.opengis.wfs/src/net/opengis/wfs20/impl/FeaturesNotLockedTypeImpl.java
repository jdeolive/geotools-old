/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wfs20.impl;

import java.util.Collection;

import net.opengis.fes20.ResourceIdType;

import net.opengis.wfs20.FeaturesNotLockedType;
import net.opengis.wfs20.Wfs20Package;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Features Not Locked Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wfs20.impl.FeaturesNotLockedTypeImpl#getGroup <em>Group</em>}</li>
 *   <li>{@link net.opengis.wfs20.impl.FeaturesNotLockedTypeImpl#getResourceId <em>Resource Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FeaturesNotLockedTypeImpl extends EObjectImpl implements FeaturesNotLockedType {
    /**
     * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroup()
     * @generated
     * @ordered
     */
    protected FeatureMap group;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected FeaturesNotLockedTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return Wfs20Package.Literals.FEATURES_NOT_LOCKED_TYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getGroup() {
        if (group == null) {
            group = new BasicFeatureMap(this, Wfs20Package.FEATURES_NOT_LOCKED_TYPE__GROUP);
        }
        return group;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<ResourceIdType> getResourceId() {
        return getGroup().list(Wfs20Package.Literals.FEATURES_NOT_LOCKED_TYPE__RESOURCE_ID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__GROUP:
                return ((InternalEList<?>)getGroup()).basicRemove(otherEnd, msgs);
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__RESOURCE_ID:
                return ((InternalEList<?>)getResourceId()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__GROUP:
                if (coreType) return getGroup();
                return ((FeatureMap.Internal)getGroup()).getWrapper();
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__RESOURCE_ID:
                return getResourceId();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__GROUP:
                ((FeatureMap.Internal)getGroup()).set(newValue);
                return;
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__RESOURCE_ID:
                getResourceId().clear();
                getResourceId().addAll((Collection<? extends ResourceIdType>)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__GROUP:
                getGroup().clear();
                return;
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__RESOURCE_ID:
                getResourceId().clear();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__GROUP:
                return group != null && !group.isEmpty();
            case Wfs20Package.FEATURES_NOT_LOCKED_TYPE__RESOURCE_ID:
                return !getResourceId().isEmpty();
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (group: ");
        result.append(group);
        result.append(')');
        return result.toString();
    }

} //FeaturesNotLockedTypeImpl
