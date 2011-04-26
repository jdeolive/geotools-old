/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wfs20;

import net.opengis.fes20.ResourceIdType;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Features Not Locked Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wfs20.FeaturesNotLockedType#getGroup <em>Group</em>}</li>
 *   <li>{@link net.opengis.wfs20.FeaturesNotLockedType#getResourceId <em>Resource Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wfs20.Wfs20Package#getFeaturesNotLockedType()
 * @model extendedMetaData="name='FeaturesNotLockedType' kind='elementOnly'"
 * @generated
 */
public interface FeaturesNotLockedType extends EObject {
    /**
     * Returns the value of the '<em><b>Group</b></em>' attribute list.
     * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Group</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Group</em>' attribute list.
     * @see net.opengis.wfs20.Wfs20Package#getFeaturesNotLockedType_Group()
     * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
     *        extendedMetaData="kind='group' name='group:0'"
     * @generated
     */
    FeatureMap getGroup();

    /**
     * Returns the value of the '<em><b>Resource Id</b></em>' containment reference list.
     * The list contents are of type {@link net.opengis.fes20.ResourceIdType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Id</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Id</em>' containment reference list.
     * @see net.opengis.wfs20.Wfs20Package#getFeaturesNotLockedType_ResourceId()
     * @model containment="true" required="true" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='ResourceId' namespace='http://www.opengis.net/fes/2.0' group='#group:0'"
     * @generated
     */
    EList<ResourceIdType> getResourceId();

} // FeaturesNotLockedType
