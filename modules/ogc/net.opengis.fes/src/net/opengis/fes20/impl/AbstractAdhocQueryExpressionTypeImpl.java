/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.fes20.impl;

import java.util.List;

import net.opengis.fes20.AbstractAdhocQueryExpressionType;
import net.opengis.fes20.Fes20Package;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Abstract Adhoc Query Expression Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl#getAbstractProjectionClauseGroup <em>Abstract Projection Clause Group</em>}</li>
 *   <li>{@link net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl#getAbstractProjectionClause <em>Abstract Projection Clause</em>}</li>
 *   <li>{@link net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl#getAbstractSelectionClauseGroup <em>Abstract Selection Clause Group</em>}</li>
 *   <li>{@link net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl#getAbstractSelectionClause <em>Abstract Selection Clause</em>}</li>
 *   <li>{@link net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl#getAbstractSortingClauseGroup <em>Abstract Sorting Clause Group</em>}</li>
 *   <li>{@link net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl#getAbstractSortingClause <em>Abstract Sorting Clause</em>}</li>
 *   <li>{@link net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl#getAliases <em>Aliases</em>}</li>
 *   <li>{@link net.opengis.fes20.impl.AbstractAdhocQueryExpressionTypeImpl#getTypeNames <em>Type Names</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class AbstractAdhocQueryExpressionTypeImpl extends AbstractQueryExpressionTypeImpl implements AbstractAdhocQueryExpressionType {
    /**
     * The cached value of the '{@link #getAbstractProjectionClauseGroup() <em>Abstract Projection Clause Group</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAbstractProjectionClauseGroup()
     * @generated
     * @ordered
     */
    protected FeatureMap abstractProjectionClauseGroup;

    /**
     * The cached value of the '{@link #getAbstractSelectionClauseGroup() <em>Abstract Selection Clause Group</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAbstractSelectionClauseGroup()
     * @generated
     * @ordered
     */
    protected FeatureMap abstractSelectionClauseGroup;

    /**
     * The cached value of the '{@link #getAbstractSortingClauseGroup() <em>Abstract Sorting Clause Group</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAbstractSortingClauseGroup()
     * @generated
     * @ordered
     */
    protected FeatureMap abstractSortingClauseGroup;

    /**
     * The default value of the '{@link #getAliases() <em>Aliases</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAliases()
     * @generated
     * @ordered
     */
    protected static final List<String> ALIASES_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getAliases() <em>Aliases</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAliases()
     * @generated
     * @ordered
     */
    protected List<String> aliases = ALIASES_EDEFAULT;

    /**
     * The default value of the '{@link #getTypeNames() <em>Type Names</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTypeNames()
     * @generated
     * @ordered
     */
    protected static final List<Object> TYPE_NAMES_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTypeNames() <em>Type Names</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTypeNames()
     * @generated
     * @ordered
     */
    protected List<Object> typeNames = TYPE_NAMES_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected AbstractAdhocQueryExpressionTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return Fes20Package.Literals.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getAbstractProjectionClauseGroup() {
        if (abstractProjectionClauseGroup == null) {
            abstractProjectionClauseGroup = new BasicFeatureMap(this, Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE_GROUP);
        }
        return abstractProjectionClauseGroup;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<EObject> getAbstractProjectionClause() {
        return getAbstractProjectionClauseGroup().list(Fes20Package.Literals.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getAbstractSelectionClauseGroup() {
        if (abstractSelectionClauseGroup == null) {
            abstractSelectionClauseGroup = new BasicFeatureMap(this, Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE_GROUP);
        }
        return abstractSelectionClauseGroup;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject getAbstractSelectionClause() {
        return (EObject)getAbstractSelectionClauseGroup().get(Fes20Package.Literals.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE, true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetAbstractSelectionClause(EObject newAbstractSelectionClause, NotificationChain msgs) {
        return ((FeatureMap.Internal)getAbstractSelectionClauseGroup()).basicAdd(Fes20Package.Literals.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE, newAbstractSelectionClause, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getAbstractSortingClauseGroup() {
        if (abstractSortingClauseGroup == null) {
            abstractSortingClauseGroup = new BasicFeatureMap(this, Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE_GROUP);
        }
        return abstractSortingClauseGroup;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject getAbstractSortingClause() {
        return (EObject)getAbstractSortingClauseGroup().get(Fes20Package.Literals.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE, true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetAbstractSortingClause(EObject newAbstractSortingClause, NotificationChain msgs) {
        return ((FeatureMap.Internal)getAbstractSortingClauseGroup()).basicAdd(Fes20Package.Literals.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE, newAbstractSortingClause, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setAliases(List<String> newAliases) {
        List<String> oldAliases = aliases;
        aliases = newAliases;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ALIASES, oldAliases, aliases));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public List<Object> getTypeNames() {
        return typeNames;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTypeNames(List<Object> newTypeNames) {
        List<Object> oldTypeNames = typeNames;
        typeNames = newTypeNames;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__TYPE_NAMES, oldTypeNames, typeNames));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE_GROUP:
                return ((InternalEList<?>)getAbstractProjectionClauseGroup()).basicRemove(otherEnd, msgs);
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE:
                return ((InternalEList<?>)getAbstractProjectionClause()).basicRemove(otherEnd, msgs);
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE_GROUP:
                return ((InternalEList<?>)getAbstractSelectionClauseGroup()).basicRemove(otherEnd, msgs);
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE:
                return basicSetAbstractSelectionClause(null, msgs);
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE_GROUP:
                return ((InternalEList<?>)getAbstractSortingClauseGroup()).basicRemove(otherEnd, msgs);
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE:
                return basicSetAbstractSortingClause(null, msgs);
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
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE_GROUP:
                if (coreType) return getAbstractProjectionClauseGroup();
                return ((FeatureMap.Internal)getAbstractProjectionClauseGroup()).getWrapper();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE:
                return getAbstractProjectionClause();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE_GROUP:
                if (coreType) return getAbstractSelectionClauseGroup();
                return ((FeatureMap.Internal)getAbstractSelectionClauseGroup()).getWrapper();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE:
                return getAbstractSelectionClause();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE_GROUP:
                if (coreType) return getAbstractSortingClauseGroup();
                return ((FeatureMap.Internal)getAbstractSortingClauseGroup()).getWrapper();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE:
                return getAbstractSortingClause();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ALIASES:
                return getAliases();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__TYPE_NAMES:
                return getTypeNames();
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
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE_GROUP:
                ((FeatureMap.Internal)getAbstractProjectionClauseGroup()).set(newValue);
                return;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE_GROUP:
                ((FeatureMap.Internal)getAbstractSelectionClauseGroup()).set(newValue);
                return;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE_GROUP:
                ((FeatureMap.Internal)getAbstractSortingClauseGroup()).set(newValue);
                return;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ALIASES:
                setAliases((List<String>)newValue);
                return;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__TYPE_NAMES:
                setTypeNames((List<Object>)newValue);
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
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE_GROUP:
                getAbstractProjectionClauseGroup().clear();
                return;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE_GROUP:
                getAbstractSelectionClauseGroup().clear();
                return;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE_GROUP:
                getAbstractSortingClauseGroup().clear();
                return;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ALIASES:
                setAliases(ALIASES_EDEFAULT);
                return;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__TYPE_NAMES:
                setTypeNames(TYPE_NAMES_EDEFAULT);
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
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE_GROUP:
                return abstractProjectionClauseGroup != null && !abstractProjectionClauseGroup.isEmpty();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_PROJECTION_CLAUSE:
                return !getAbstractProjectionClause().isEmpty();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE_GROUP:
                return abstractSelectionClauseGroup != null && !abstractSelectionClauseGroup.isEmpty();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SELECTION_CLAUSE:
                return getAbstractSelectionClause() != null;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE_GROUP:
                return abstractSortingClauseGroup != null && !abstractSortingClauseGroup.isEmpty();
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ABSTRACT_SORTING_CLAUSE:
                return getAbstractSortingClause() != null;
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__ALIASES:
                return ALIASES_EDEFAULT == null ? aliases != null : !ALIASES_EDEFAULT.equals(aliases);
            case Fes20Package.ABSTRACT_ADHOC_QUERY_EXPRESSION_TYPE__TYPE_NAMES:
                return TYPE_NAMES_EDEFAULT == null ? typeNames != null : !TYPE_NAMES_EDEFAULT.equals(typeNames);
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
        result.append(" (abstractProjectionClauseGroup: ");
        result.append(abstractProjectionClauseGroup);
        result.append(", abstractSelectionClauseGroup: ");
        result.append(abstractSelectionClauseGroup);
        result.append(", abstractSortingClauseGroup: ");
        result.append(abstractSortingClauseGroup);
        result.append(", aliases: ");
        result.append(aliases);
        result.append(", typeNames: ");
        result.append(typeNames);
        result.append(')');
        return result.toString();
    }

} //AbstractAdhocQueryExpressionTypeImpl
