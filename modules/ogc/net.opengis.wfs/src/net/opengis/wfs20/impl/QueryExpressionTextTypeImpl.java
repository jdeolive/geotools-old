/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wfs20.impl;

import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wfs20.QueryExpressionTextType;
import net.opengis.wfs20.Wfs20Package;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Query Expression Text Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wfs20.impl.QueryExpressionTextTypeImpl#getMixed <em>Mixed</em>}</li>
 *   <li>{@link net.opengis.wfs20.impl.QueryExpressionTextTypeImpl#getAny <em>Any</em>}</li>
 *   <li>{@link net.opengis.wfs20.impl.QueryExpressionTextTypeImpl#getAny1 <em>Any1</em>}</li>
 *   <li>{@link net.opengis.wfs20.impl.QueryExpressionTextTypeImpl#isIsPrivate <em>Is Private</em>}</li>
 *   <li>{@link net.opengis.wfs20.impl.QueryExpressionTextTypeImpl#getLanguage <em>Language</em>}</li>
 *   <li>{@link net.opengis.wfs20.impl.QueryExpressionTextTypeImpl#getReturnFeatureTypes <em>Return Feature Types</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class QueryExpressionTextTypeImpl extends EObjectImpl implements QueryExpressionTextType {
    /**
     * The cached value of the '{@link #getMixed() <em>Mixed</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMixed()
     * @generated
     * @ordered
     */
    protected FeatureMap mixed;

    /**
     * The default value of the '{@link #isIsPrivate() <em>Is Private</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isIsPrivate()
     * @generated
     * @ordered
     */
    protected static final boolean IS_PRIVATE_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isIsPrivate() <em>Is Private</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isIsPrivate()
     * @generated
     * @ordered
     */
    protected boolean isPrivate = IS_PRIVATE_EDEFAULT;

    /**
     * This is true if the Is Private attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean isPrivateESet;

    /**
     * The default value of the '{@link #getLanguage() <em>Language</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLanguage()
     * @generated
     * @ordered
     */
    protected static final String LANGUAGE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getLanguage() <em>Language</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLanguage()
     * @generated
     * @ordered
     */
    protected String language = LANGUAGE_EDEFAULT;

    /**
     * The default value of the '{@link #getReturnFeatureTypes() <em>Return Feature Types</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getReturnFeatureTypes()
     * @generated
     * @ordered
     */
    protected static final List<QName> RETURN_FEATURE_TYPES_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getReturnFeatureTypes() <em>Return Feature Types</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getReturnFeatureTypes()
     * @generated
     * @ordered
     */
    protected List<QName> returnFeatureTypes = RETURN_FEATURE_TYPES_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected QueryExpressionTextTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return Wfs20Package.Literals.QUERY_EXPRESSION_TEXT_TYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getMixed() {
        if (mixed == null) {
            mixed = new BasicFeatureMap(this, Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__MIXED);
        }
        return mixed;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getAny() {
        return (FeatureMap)getMixed().<FeatureMap.Entry>list(Wfs20Package.Literals.QUERY_EXPRESSION_TEXT_TYPE__ANY);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getAny1() {
        return (FeatureMap)getMixed().<FeatureMap.Entry>list(Wfs20Package.Literals.QUERY_EXPRESSION_TEXT_TYPE__ANY1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isIsPrivate() {
        return isPrivate;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setIsPrivate(boolean newIsPrivate) {
        boolean oldIsPrivate = isPrivate;
        isPrivate = newIsPrivate;
        boolean oldIsPrivateESet = isPrivateESet;
        isPrivateESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__IS_PRIVATE, oldIsPrivate, isPrivate, !oldIsPrivateESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetIsPrivate() {
        boolean oldIsPrivate = isPrivate;
        boolean oldIsPrivateESet = isPrivateESet;
        isPrivate = IS_PRIVATE_EDEFAULT;
        isPrivateESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__IS_PRIVATE, oldIsPrivate, IS_PRIVATE_EDEFAULT, oldIsPrivateESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetIsPrivate() {
        return isPrivateESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getLanguage() {
        return language;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setLanguage(String newLanguage) {
        String oldLanguage = language;
        language = newLanguage;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__LANGUAGE, oldLanguage, language));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public List<QName> getReturnFeatureTypes() {
        return returnFeatureTypes;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setReturnFeatureTypes(List<QName> newReturnFeatureTypes) {
        List<QName> oldReturnFeatureTypes = returnFeatureTypes;
        returnFeatureTypes = newReturnFeatureTypes;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__RETURN_FEATURE_TYPES, oldReturnFeatureTypes, returnFeatureTypes));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__MIXED:
                return ((InternalEList<?>)getMixed()).basicRemove(otherEnd, msgs);
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY:
                return ((InternalEList<?>)getAny()).basicRemove(otherEnd, msgs);
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY1:
                return ((InternalEList<?>)getAny1()).basicRemove(otherEnd, msgs);
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
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__MIXED:
                if (coreType) return getMixed();
                return ((FeatureMap.Internal)getMixed()).getWrapper();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY:
                if (coreType) return getAny();
                return ((FeatureMap.Internal)getAny()).getWrapper();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY1:
                if (coreType) return getAny1();
                return ((FeatureMap.Internal)getAny1()).getWrapper();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__IS_PRIVATE:
                return isIsPrivate();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__LANGUAGE:
                return getLanguage();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__RETURN_FEATURE_TYPES:
                return getReturnFeatureTypes();
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
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__MIXED:
                ((FeatureMap.Internal)getMixed()).set(newValue);
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY:
                ((FeatureMap.Internal)getAny()).set(newValue);
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY1:
                ((FeatureMap.Internal)getAny1()).set(newValue);
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__IS_PRIVATE:
                setIsPrivate((Boolean)newValue);
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__LANGUAGE:
                setLanguage((String)newValue);
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__RETURN_FEATURE_TYPES:
                setReturnFeatureTypes((List<QName>)newValue);
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
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__MIXED:
                getMixed().clear();
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY:
                getAny().clear();
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY1:
                getAny1().clear();
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__IS_PRIVATE:
                unsetIsPrivate();
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__LANGUAGE:
                setLanguage(LANGUAGE_EDEFAULT);
                return;
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__RETURN_FEATURE_TYPES:
                setReturnFeatureTypes(RETURN_FEATURE_TYPES_EDEFAULT);
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
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__MIXED:
                return mixed != null && !mixed.isEmpty();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY:
                return !getAny().isEmpty();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__ANY1:
                return !getAny1().isEmpty();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__IS_PRIVATE:
                return isSetIsPrivate();
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__LANGUAGE:
                return LANGUAGE_EDEFAULT == null ? language != null : !LANGUAGE_EDEFAULT.equals(language);
            case Wfs20Package.QUERY_EXPRESSION_TEXT_TYPE__RETURN_FEATURE_TYPES:
                return RETURN_FEATURE_TYPES_EDEFAULT == null ? returnFeatureTypes != null : !RETURN_FEATURE_TYPES_EDEFAULT.equals(returnFeatureTypes);
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
        result.append(" (mixed: ");
        result.append(mixed);
        result.append(", isPrivate: ");
        if (isPrivateESet) result.append(isPrivate); else result.append("<unset>");
        result.append(", language: ");
        result.append(language);
        result.append(", returnFeatureTypes: ");
        result.append(returnFeatureTypes);
        result.append(')');
        return result.toString();
    }

} //QueryExpressionTextTypeImpl
