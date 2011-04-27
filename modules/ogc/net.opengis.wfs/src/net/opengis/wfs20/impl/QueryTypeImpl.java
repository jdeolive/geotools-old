/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wfs20.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl;

import net.opengis.wfs20.QueryType;
import net.opengis.wfs20.Wfs20Package;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Query Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wfs20.impl.QueryTypeImpl#getFeatureVersion <em>Feature Version</em>}</li>
 *   <li>{@link net.opengis.wfs20.impl.QueryTypeImpl#getSrsName <em>Srs Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class QueryTypeImpl extends AbstractAdhocQueryExpressionTypeImpl implements QueryType {
    /**
     * The default value of the '{@link #getFeatureVersion() <em>Feature Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFeatureVersion()
     * @generated
     * @ordered
     */
    protected static final String FEATURE_VERSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getFeatureVersion() <em>Feature Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFeatureVersion()
     * @generated
     * @ordered
     */
    protected String featureVersion = FEATURE_VERSION_EDEFAULT;

    /**
     * The default value of the '{@link #getSrsName() <em>Srs Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSrsName()
     * @generated
     * @ordered
     */
    protected static final String SRS_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getSrsName() <em>Srs Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSrsName()
     * @generated
     * @ordered
     */
    protected String srsName = SRS_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected QueryTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return Wfs20Package.Literals.QUERY_TYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getFeatureVersion() {
        return featureVersion;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setFeatureVersion(String newFeatureVersion) {
        String oldFeatureVersion = featureVersion;
        featureVersion = newFeatureVersion;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wfs20Package.QUERY_TYPE__FEATURE_VERSION, oldFeatureVersion, featureVersion));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSrsName(String newSrsName) {
        String oldSrsName = srsName;
        srsName = newSrsName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wfs20Package.QUERY_TYPE__SRS_NAME, oldSrsName, srsName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case Wfs20Package.QUERY_TYPE__FEATURE_VERSION:
                return getFeatureVersion();
            case Wfs20Package.QUERY_TYPE__SRS_NAME:
                return getSrsName();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case Wfs20Package.QUERY_TYPE__FEATURE_VERSION:
                setFeatureVersion((String)newValue);
                return;
            case Wfs20Package.QUERY_TYPE__SRS_NAME:
                setSrsName((String)newValue);
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
            case Wfs20Package.QUERY_TYPE__FEATURE_VERSION:
                setFeatureVersion(FEATURE_VERSION_EDEFAULT);
                return;
            case Wfs20Package.QUERY_TYPE__SRS_NAME:
                setSrsName(SRS_NAME_EDEFAULT);
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
            case Wfs20Package.QUERY_TYPE__FEATURE_VERSION:
                return FEATURE_VERSION_EDEFAULT == null ? featureVersion != null : !FEATURE_VERSION_EDEFAULT.equals(featureVersion);
            case Wfs20Package.QUERY_TYPE__SRS_NAME:
                return SRS_NAME_EDEFAULT == null ? srsName != null : !SRS_NAME_EDEFAULT.equals(srsName);
        }
        return super.eIsSet(featureID);
    }

    /**
     * @generated NOT
     */
    public void setFilter(Filter filter) {
        setAbstractSelectionClause(filter);
    }

    /**
     * @generated NOT
     */
    public Filter getFilter() {
        return (Filter) getAbstractSelectionClause();
    }
    
    /**
     * @generated NOT
     */
    public EList<QName> getPropertyNames() {
        return (EList) getAbstractProjectionClause();
    }

    /**
     * @generated NOT
     */
    public List<SortBy> getSortBy() {
        if (abstractSortingClause == null) {
            abstractSortingClause = new ArrayList();
        }
        return (List<SortBy>) abstractSortingClause;
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
        result.append(" (featureVersion: ");
        result.append(featureVersion);
        result.append(", srsName: ");
        result.append(srsName);
        result.append(')');
        return result.toString();
    }

} //QueryTypeImpl
