/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.gml.impl;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.opengis.gml.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.emf.ecore.util.Diagnostician;

import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class Gml4wcsFactoryImpl extends EFactoryImpl implements Gml4wcsFactory {
    /**
     * Creates the default factory implementation.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static Gml4wcsFactory init() {
        try {
            Gml4wcsFactory theGml4wcsFactory = (Gml4wcsFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.opengis.net/gml"); 
            if (theGml4wcsFactory != null) {
                return theGml4wcsFactory;
            }
        }
        catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new Gml4wcsFactoryImpl();
    }

    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Gml4wcsFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case Gml4wcsPackage.ABSTRACT_RING_PROPERTY_TYPE: return createAbstractRingPropertyType();
            case Gml4wcsPackage.ABSTRACT_SURFACE_TYPE: return createAbstractSurfaceType();
            case Gml4wcsPackage.BOUNDING_SHAPE_TYPE: return createBoundingShapeType();
            case Gml4wcsPackage.CODE_LIST_TYPE: return createCodeListType();
            case Gml4wcsPackage.CODE_TYPE: return createCodeType();
            case Gml4wcsPackage.DIRECT_POSITION_TYPE: return createDirectPositionType();
            case Gml4wcsPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case Gml4wcsPackage.ENVELOPE_TYPE: return createEnvelopeType();
            case Gml4wcsPackage.ENVELOPE_WITH_TIME_PERIOD_TYPE: return createEnvelopeWithTimePeriodType();
            case Gml4wcsPackage.GRID_ENVELOPE_TYPE: return createGridEnvelopeType();
            case Gml4wcsPackage.GRID_LIMITS_TYPE: return createGridLimitsType();
            case Gml4wcsPackage.GRID_TYPE: return createGridType();
            case Gml4wcsPackage.LINEAR_RING_TYPE: return createLinearRingType();
            case Gml4wcsPackage.META_DATA_PROPERTY_TYPE: return createMetaDataPropertyType();
            case Gml4wcsPackage.POINT_TYPE: return createPointType();
            case Gml4wcsPackage.POLYGON_TYPE: return createPolygonType();
            case Gml4wcsPackage.RECTIFIED_GRID_TYPE: return createRectifiedGridType();
            case Gml4wcsPackage.REFERENCE_TYPE: return createReferenceType();
            case Gml4wcsPackage.STRING_OR_REF_TYPE: return createStringOrRefType();
            case Gml4wcsPackage.TIME_POSITION_TYPE: return createTimePositionType();
            case Gml4wcsPackage.VECTOR_TYPE: return createVectorType();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object createFromString(EDataType eDataType, String initialValue) {
        switch (eDataType.getClassifierID()) {
            case Gml4wcsPackage.TIME_INDETERMINATE_VALUE_TYPE:
                return createTimeIndeterminateValueTypeFromString(eDataType, initialValue);
            case Gml4wcsPackage.DOUBLE_LIST:
                return createDoubleListFromString(eDataType, initialValue);
            case Gml4wcsPackage.INTEGER_LIST:
                return createIntegerListFromString(eDataType, initialValue);
            case Gml4wcsPackage.NAME_LIST:
                return createNameListFromString(eDataType, initialValue);
            case Gml4wcsPackage.TEMPORAL_POSITION_TYPE:
                return createTemporalPositionTypeFromString(eDataType, initialValue);
            case Gml4wcsPackage.TIME_DURATION_TYPE:
                return createTimeDurationTypeFromString(eDataType, initialValue);
            case Gml4wcsPackage.TIME_INDETERMINATE_VALUE_TYPE_OBJECT:
                return createTimeIndeterminateValueTypeObjectFromString(eDataType, initialValue);
            case Gml4wcsPackage.VECTOR_TYPE_BASE:
                return createVectorTypeBaseFromString(eDataType, initialValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertToString(EDataType eDataType, Object instanceValue) {
        switch (eDataType.getClassifierID()) {
            case Gml4wcsPackage.TIME_INDETERMINATE_VALUE_TYPE:
                return convertTimeIndeterminateValueTypeToString(eDataType, instanceValue);
            case Gml4wcsPackage.DOUBLE_LIST:
                return convertDoubleListToString(eDataType, instanceValue);
            case Gml4wcsPackage.INTEGER_LIST:
                return convertIntegerListToString(eDataType, instanceValue);
            case Gml4wcsPackage.NAME_LIST:
                return convertNameListToString(eDataType, instanceValue);
            case Gml4wcsPackage.TEMPORAL_POSITION_TYPE:
                return convertTemporalPositionTypeToString(eDataType, instanceValue);
            case Gml4wcsPackage.TIME_DURATION_TYPE:
                return convertTimeDurationTypeToString(eDataType, instanceValue);
            case Gml4wcsPackage.TIME_INDETERMINATE_VALUE_TYPE_OBJECT:
                return convertTimeIndeterminateValueTypeObjectToString(eDataType, instanceValue);
            case Gml4wcsPackage.VECTOR_TYPE_BASE:
                return convertVectorTypeBaseToString(eDataType, instanceValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public AbstractRingPropertyType createAbstractRingPropertyType() {
        AbstractRingPropertyTypeImpl abstractRingPropertyType = new AbstractRingPropertyTypeImpl();
        return abstractRingPropertyType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public AbstractSurfaceType createAbstractSurfaceType() {
        AbstractSurfaceTypeImpl abstractSurfaceType = new AbstractSurfaceTypeImpl();
        return abstractSurfaceType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public BoundingShapeType createBoundingShapeType() {
        BoundingShapeTypeImpl boundingShapeType = new BoundingShapeTypeImpl();
        return boundingShapeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CodeListType createCodeListType() {
        CodeListTypeImpl codeListType = new CodeListTypeImpl();
        return codeListType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CodeType createCodeType() {
        CodeTypeImpl codeType = new CodeTypeImpl();
        return codeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DirectPositionType createDirectPositionType() {
        DirectPositionTypeImpl directPositionType = new DirectPositionTypeImpl();
        return directPositionType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DocumentRoot createDocumentRoot() {
        DocumentRootImpl documentRoot = new DocumentRootImpl();
        return documentRoot;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EnvelopeType createEnvelopeType() {
        EnvelopeTypeImpl envelopeType = new EnvelopeTypeImpl();
        return envelopeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EnvelopeWithTimePeriodType createEnvelopeWithTimePeriodType() {
        EnvelopeWithTimePeriodTypeImpl envelopeWithTimePeriodType = new EnvelopeWithTimePeriodTypeImpl();
        return envelopeWithTimePeriodType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GridEnvelopeType createGridEnvelopeType() {
        GridEnvelopeTypeImpl gridEnvelopeType = new GridEnvelopeTypeImpl();
        return gridEnvelopeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GridLimitsType createGridLimitsType() {
        GridLimitsTypeImpl gridLimitsType = new GridLimitsTypeImpl();
        return gridLimitsType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GridType createGridType() {
        GridTypeImpl gridType = new GridTypeImpl();
        return gridType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public LinearRingType createLinearRingType() {
        LinearRingTypeImpl linearRingType = new LinearRingTypeImpl();
        return linearRingType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MetaDataPropertyType createMetaDataPropertyType() {
        MetaDataPropertyTypeImpl metaDataPropertyType = new MetaDataPropertyTypeImpl();
        return metaDataPropertyType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PointType createPointType() {
        PointTypeImpl pointType = new PointTypeImpl();
        return pointType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PolygonType createPolygonType() {
        PolygonTypeImpl polygonType = new PolygonTypeImpl();
        return polygonType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RectifiedGridType createRectifiedGridType() {
        RectifiedGridTypeImpl rectifiedGridType = new RectifiedGridTypeImpl();
        return rectifiedGridType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ReferenceType createReferenceType() {
        ReferenceTypeImpl referenceType = new ReferenceTypeImpl();
        return referenceType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public StringOrRefType createStringOrRefType() {
        StringOrRefTypeImpl stringOrRefType = new StringOrRefTypeImpl();
        return stringOrRefType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public TimePositionType createTimePositionType() {
        TimePositionTypeImpl timePositionType = new TimePositionTypeImpl();
        return timePositionType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public VectorType createVectorType() {
        VectorTypeImpl vectorType = new VectorTypeImpl();
        return vectorType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public TimeIndeterminateValueType createTimeIndeterminateValueTypeFromString(EDataType eDataType, String initialValue) {
        TimeIndeterminateValueType result = TimeIndeterminateValueType.get(initialValue);
        if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
        return result;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertTimeIndeterminateValueTypeToString(EDataType eDataType, Object instanceValue) {
        return instanceValue == null ? null : instanceValue.toString();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public List createDoubleListFromString(EDataType eDataType, String initialValue) {
        if (initialValue == null) return null;
        List result = new ArrayList();
        for (StringTokenizer stringTokenizer = new StringTokenizer(initialValue); stringTokenizer.hasMoreTokens(); ) {
            String item = stringTokenizer.nextToken();
            result.add((Double)XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.DOUBLE, item));
        }
        return result;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertDoubleListToString(EDataType eDataType, Object instanceValue) {
        if (instanceValue == null) return null;
        List list = (List)instanceValue;
        if (list.isEmpty()) return "";
        StringBuffer result = new StringBuffer();
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            result.append(XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.DOUBLE, i.next()));
            result.append(' ');
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public List createIntegerListFromString(EDataType eDataType, String initialValue) {
        if (initialValue == null) return null;
        List result = new ArrayList();
        for (StringTokenizer stringTokenizer = new StringTokenizer(initialValue); stringTokenizer.hasMoreTokens(); ) {
            String item = stringTokenizer.nextToken();
            result.add((BigInteger)XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.INTEGER, item));
        }
        return result;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertIntegerListToString(EDataType eDataType, Object instanceValue) {
        if (instanceValue == null) return null;
        List list = (List)instanceValue;
        if (list.isEmpty()) return "";
        StringBuffer result = new StringBuffer();
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            result.append(XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.INTEGER, i.next()));
            result.append(' ');
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public List createNameListFromString(EDataType eDataType, String initialValue) {
        if (initialValue == null) return null;
        List result = new ArrayList();
        for (StringTokenizer stringTokenizer = new StringTokenizer(initialValue); stringTokenizer.hasMoreTokens(); ) {
            String item = stringTokenizer.nextToken();
            result.add((String)XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.NAME, item));
        }
        return result;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertNameListToString(EDataType eDataType, Object instanceValue) {
        if (instanceValue == null) return null;
        List list = (List)instanceValue;
        if (list.isEmpty()) return "";
        StringBuffer result = new StringBuffer();
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            result.append(XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.NAME, i.next()));
            result.append(' ');
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object createTemporalPositionTypeFromString(EDataType eDataType, String initialValue) {
        if (initialValue == null) return null;
        Object result = null;
        RuntimeException exception = null;
        try {
            result = XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.DATE_TIME, initialValue);
            if (result != null && Diagnostician.INSTANCE.validate(eDataType, result, null, null)) {
                return result;
            }
        }
        catch (RuntimeException e) {
            exception = e;
        }
        try {
            result = XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.DATE, initialValue);
            if (result != null && Diagnostician.INSTANCE.validate(eDataType, result, null, null)) {
                return result;
            }
        }
        catch (RuntimeException e) {
            exception = e;
        }
        try {
            result = XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.GYEAR_MONTH, initialValue);
            if (result != null && Diagnostician.INSTANCE.validate(eDataType, result, null, null)) {
                return result;
            }
        }
        catch (RuntimeException e) {
            exception = e;
        }
        try {
            result = XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.GYEAR, initialValue);
            if (result != null && Diagnostician.INSTANCE.validate(eDataType, result, null, null)) {
                return result;
            }
        }
        catch (RuntimeException e) {
            exception = e;
        }
        try {
            result = XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.ANY_URI, initialValue);
            if (result != null && Diagnostician.INSTANCE.validate(eDataType, result, null, null)) {
                return result;
            }
        }
        catch (RuntimeException e) {
            exception = e;
        }
        try {
            result = XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.DECIMAL, initialValue);
            if (result != null && Diagnostician.INSTANCE.validate(eDataType, result, null, null)) {
                return result;
            }
        }
        catch (RuntimeException e) {
            exception = e;
        }
        if (result != null || exception == null) return result;
    
        throw exception;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertTemporalPositionTypeToString(EDataType eDataType, Object instanceValue) {
        if (instanceValue == null) return null;
        if (XMLTypePackage.Literals.DATE_TIME.isInstance(instanceValue)) {
            try {
                String value = XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.DATE_TIME, instanceValue);
                if (value != null) return value;
            }
            catch (Exception e) {
                // Keep trying other member types until all have failed.
            }
        }
        if (XMLTypePackage.Literals.DATE.isInstance(instanceValue)) {
            try {
                String value = XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.DATE, instanceValue);
                if (value != null) return value;
            }
            catch (Exception e) {
                // Keep trying other member types until all have failed.
            }
        }
        if (XMLTypePackage.Literals.GYEAR_MONTH.isInstance(instanceValue)) {
            try {
                String value = XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.GYEAR_MONTH, instanceValue);
                if (value != null) return value;
            }
            catch (Exception e) {
                // Keep trying other member types until all have failed.
            }
        }
        if (XMLTypePackage.Literals.GYEAR.isInstance(instanceValue)) {
            try {
                String value = XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.GYEAR, instanceValue);
                if (value != null) return value;
            }
            catch (Exception e) {
                // Keep trying other member types until all have failed.
            }
        }
        if (XMLTypePackage.Literals.ANY_URI.isInstance(instanceValue)) {
            try {
                String value = XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.ANY_URI, instanceValue);
                if (value != null) return value;
            }
            catch (Exception e) {
                // Keep trying other member types until all have failed.
            }
        }
        if (XMLTypePackage.Literals.DECIMAL.isInstance(instanceValue)) {
            try {
                String value = XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.DECIMAL, instanceValue);
                if (value != null) return value;
            }
            catch (Exception e) {
                // Keep trying other member types until all have failed.
            }
        }
        throw new IllegalArgumentException("Invalid value: '"+instanceValue+"' for datatype :"+eDataType.getName());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object createTimeDurationTypeFromString(EDataType eDataType, String initialValue) {
        if (initialValue == null) return null;
        Object result = null;
        RuntimeException exception = null;
        try {
            result = XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.DURATION, initialValue);
            if (result != null && Diagnostician.INSTANCE.validate(eDataType, result, null, null)) {
                return result;
            }
        }
        catch (RuntimeException e) {
            exception = e;
        }
        try {
            result = XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.DECIMAL, initialValue);
            if (result != null && Diagnostician.INSTANCE.validate(eDataType, result, null, null)) {
                return result;
            }
        }
        catch (RuntimeException e) {
            exception = e;
        }
        if (result != null || exception == null) return result;
    
        throw exception;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertTimeDurationTypeToString(EDataType eDataType, Object instanceValue) {
        if (instanceValue == null) return null;
        if (XMLTypePackage.Literals.DURATION.isInstance(instanceValue)) {
            try {
                String value = XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.DURATION, instanceValue);
                if (value != null) return value;
            }
            catch (Exception e) {
                // Keep trying other member types until all have failed.
            }
        }
        if (XMLTypePackage.Literals.DECIMAL.isInstance(instanceValue)) {
            try {
                String value = XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.DECIMAL, instanceValue);
                if (value != null) return value;
            }
            catch (Exception e) {
                // Keep trying other member types until all have failed.
            }
        }
        throw new IllegalArgumentException("Invalid value: '"+instanceValue+"' for datatype :"+eDataType.getName());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public TimeIndeterminateValueType createTimeIndeterminateValueTypeObjectFromString(EDataType eDataType, String initialValue) {
        return createTimeIndeterminateValueTypeFromString(Gml4wcsPackage.Literals.TIME_INDETERMINATE_VALUE_TYPE, initialValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertTimeIndeterminateValueTypeObjectToString(EDataType eDataType, Object instanceValue) {
        return convertTimeIndeterminateValueTypeToString(Gml4wcsPackage.Literals.TIME_INDETERMINATE_VALUE_TYPE, instanceValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public List createVectorTypeBaseFromString(EDataType eDataType, String initialValue) {
        return createDoubleListFromString(Gml4wcsPackage.Literals.DOUBLE_LIST, initialValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertVectorTypeBaseToString(EDataType eDataType, Object instanceValue) {
        return convertDoubleListToString(Gml4wcsPackage.Literals.DOUBLE_LIST, instanceValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Gml4wcsPackage getGml4wcsPackage() {
        return (Gml4wcsPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    public static Gml4wcsPackage getPackage() {
        return Gml4wcsPackage.eINSTANCE;
    }

} //Gml4wcsFactoryImpl
