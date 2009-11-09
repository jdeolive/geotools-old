/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wcs10.impl;

import net.opengis.wcs10.CapabilitiesSectionType;
import net.opengis.wcs10.GetCapabilitiesType;
import net.opengis.wcs10.Wcs10Package;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get Capabilities Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wcs10.impl.GetCapabilitiesTypeImpl#getSection <em>Section</em>}</li>
 *   <li>{@link net.opengis.wcs10.impl.GetCapabilitiesTypeImpl#getService <em>Service</em>}</li>
 *   <li>{@link net.opengis.wcs10.impl.GetCapabilitiesTypeImpl#getUpdateSequence <em>Update Sequence</em>}</li>
 *   <li>{@link net.opengis.wcs10.impl.GetCapabilitiesTypeImpl#getVersion <em>Version</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GetCapabilitiesTypeImpl extends EObjectImpl implements GetCapabilitiesType {
    /**
     * The default value of the '{@link #getSection() <em>Section</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSection()
     * @generated
     * @ordered
     */
    protected static final CapabilitiesSectionType SECTION_EDEFAULT = CapabilitiesSectionType.__LITERAL;

    /**
     * The cached value of the '{@link #getSection() <em>Section</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSection()
     * @generated
     * @ordered
     */
    protected CapabilitiesSectionType section = SECTION_EDEFAULT;

    /**
     * This is true if the Section attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean sectionESet;

    /**
     * The default value of the '{@link #getService() <em>Service</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getService()
     * @generated
     * @ordered
     */
    protected static final String SERVICE_EDEFAULT = "WCS";

    /**
     * The cached value of the '{@link #getService() <em>Service</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getService()
     * @generated
     * @ordered
     */
    protected String service = SERVICE_EDEFAULT;

    /**
     * This is true if the Service attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean serviceESet;

    /**
     * The default value of the '{@link #getUpdateSequence() <em>Update Sequence</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getUpdateSequence()
     * @generated
     * @ordered
     */
    protected static final String UPDATE_SEQUENCE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getUpdateSequence() <em>Update Sequence</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getUpdateSequence()
     * @generated
     * @ordered
     */
    protected String updateSequence = UPDATE_SEQUENCE_EDEFAULT;

    /**
     * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersion()
     * @generated
     * @ordered
     */
    protected static final String VERSION_EDEFAULT = "1.0.0";

    /**
     * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersion()
     * @generated
     * @ordered
     */
    protected String version = VERSION_EDEFAULT;

    /**
     * This is true if the Version attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean versionESet;

    private String baseUrl;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected GetCapabilitiesTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return Wcs10Package.Literals.GET_CAPABILITIES_TYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CapabilitiesSectionType getSection() {
        return section;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSection(CapabilitiesSectionType newSection) {
        CapabilitiesSectionType oldSection = section;
        section = newSection == null ? SECTION_EDEFAULT : newSection;
        boolean oldSectionESet = sectionESet;
        sectionESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wcs10Package.GET_CAPABILITIES_TYPE__SECTION, oldSection, section, !oldSectionESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetSection() {
        CapabilitiesSectionType oldSection = section;
        boolean oldSectionESet = sectionESet;
        section = SECTION_EDEFAULT;
        sectionESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, Wcs10Package.GET_CAPABILITIES_TYPE__SECTION, oldSection, SECTION_EDEFAULT, oldSectionESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetSection() {
        return sectionESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getService() {
        return service;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setService(String newService) {
        String oldService = service;
        service = newService;
        boolean oldServiceESet = serviceESet;
        serviceESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wcs10Package.GET_CAPABILITIES_TYPE__SERVICE, oldService, service, !oldServiceESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetService() {
        String oldService = service;
        boolean oldServiceESet = serviceESet;
        service = SERVICE_EDEFAULT;
        serviceESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, Wcs10Package.GET_CAPABILITIES_TYPE__SERVICE, oldService, SERVICE_EDEFAULT, oldServiceESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetService() {
        return serviceESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setUpdateSequence(String newUpdateSequence) {
        String oldUpdateSequence = updateSequence;
        updateSequence = newUpdateSequence;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wcs10Package.GET_CAPABILITIES_TYPE__UPDATE_SEQUENCE, oldUpdateSequence, updateSequence));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getVersion() {
        return version;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setVersion(String newVersion) {
        String oldVersion = version;
        version = newVersion;
        boolean oldVersionESet = versionESet;
        versionESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wcs10Package.GET_CAPABILITIES_TYPE__VERSION, oldVersion, version, !oldVersionESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetVersion() {
        String oldVersion = version;
        boolean oldVersionESet = versionESet;
        version = VERSION_EDEFAULT;
        versionESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, Wcs10Package.GET_CAPABILITIES_TYPE__VERSION, oldVersion, VERSION_EDEFAULT, oldVersionESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetVersion() {
        return versionESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case Wcs10Package.GET_CAPABILITIES_TYPE__SECTION:
                return getSection();
            case Wcs10Package.GET_CAPABILITIES_TYPE__SERVICE:
                return getService();
            case Wcs10Package.GET_CAPABILITIES_TYPE__UPDATE_SEQUENCE:
                return getUpdateSequence();
            case Wcs10Package.GET_CAPABILITIES_TYPE__VERSION:
                return getVersion();
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
            case Wcs10Package.GET_CAPABILITIES_TYPE__SECTION:
                setSection((CapabilitiesSectionType)newValue);
                return;
            case Wcs10Package.GET_CAPABILITIES_TYPE__SERVICE:
                setService((String)newValue);
                return;
            case Wcs10Package.GET_CAPABILITIES_TYPE__UPDATE_SEQUENCE:
                setUpdateSequence((String)newValue);
                return;
            case Wcs10Package.GET_CAPABILITIES_TYPE__VERSION:
                setVersion((String)newValue);
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
            case Wcs10Package.GET_CAPABILITIES_TYPE__SECTION:
                unsetSection();
                return;
            case Wcs10Package.GET_CAPABILITIES_TYPE__SERVICE:
                unsetService();
                return;
            case Wcs10Package.GET_CAPABILITIES_TYPE__UPDATE_SEQUENCE:
                setUpdateSequence(UPDATE_SEQUENCE_EDEFAULT);
                return;
            case Wcs10Package.GET_CAPABILITIES_TYPE__VERSION:
                unsetVersion();
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
            case Wcs10Package.GET_CAPABILITIES_TYPE__SECTION:
                return isSetSection();
            case Wcs10Package.GET_CAPABILITIES_TYPE__SERVICE:
                return isSetService();
            case Wcs10Package.GET_CAPABILITIES_TYPE__UPDATE_SEQUENCE:
                return UPDATE_SEQUENCE_EDEFAULT == null ? updateSequence != null : !UPDATE_SEQUENCE_EDEFAULT.equals(updateSequence);
            case Wcs10Package.GET_CAPABILITIES_TYPE__VERSION:
                return isSetVersion();
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
        result.append(" (section: ");
        if (sectionESet) result.append(section); else result.append("<unset>");
        result.append(", service: ");
        if (serviceESet) result.append(service); else result.append("<unset>");
        result.append(", updateSequence: ");
        result.append(updateSequence);
        result.append(", version: ");
        if (versionESet) result.append(version); else result.append("<unset>");
        result.append(')');
        return result.toString();
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

} //GetCapabilitiesTypeImpl
