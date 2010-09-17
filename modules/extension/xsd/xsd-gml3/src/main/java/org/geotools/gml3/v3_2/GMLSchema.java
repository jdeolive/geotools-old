package org.geotools.gml3.v3_2;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.ComplexTypeImpl;
import org.geotools.feature.type.SchemaImpl;

import org.geotools.xs.XSSchema;
import org.geotools.xlink.XLINKSchema;

public class GMLSchema extends SchemaImpl {

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractCurveSegmentType"&gt;
     *      &lt;attribute default="0" name="numDerivativesAtStart" type="integer"/&gt;
     *      &lt;attribute default="0" name="numDerivativesAtEnd" type="integer"/&gt;
     *      &lt;attribute default="0" name="numDerivativeInterior" type="integer"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTCURVESEGMENTTYPE_TYPE = build_ABSTRACTCURVESEGMENTTYPE_TYPE();
    
    private static ComplexType build_ABSTRACTCURVESEGMENTTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.INTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","numDerivativesAtStart"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.INTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","numDerivativesAtEnd"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.INTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","numDerivativeInterior"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractCurveSegmentType"), schema, false,
            true, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CurveSegmentArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:CurveSegmentArrayPropertyType is a container for an array of curve segments.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
     *          &lt;element ref="gml:AbstractCurveSegment"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CURVESEGMENTARRAYPROPERTYTYPE_TYPE = build_CURVESEGMENTARRAYPROPERTYTYPE_TYPE();
    
    private static ComplexType build_CURVESEGMENTARRAYPROPERTYTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ABSTRACTCURVESEGMENTTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","AbstractCurveSegment"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CurveSegmentArrayPropertyType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractMemberType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;To create a collection of GML Objects that are not all features, a property type shall be derived by extension from gml:AbstractMemberType.
     *  This abstract property type is intended to be used only in object types where software shall be able to identify that an instance of such an object type is to be interpreted as a collection of objects.
     *  By default, this abstract property type does not imply any ownership of the objects in the collection. The owns attribute of gml:OwnershipAttributeGroup may be used on a property element instance to assert ownership of an object in the collection. A collection shall not own an object already owned by another object.
     *  &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *      &lt;attributeGroup ref="gml:OwnershipAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTMEMBERTYPE_TYPE = build_ABSTRACTMEMBERTYPE_TYPE();
    
    private static ComplexType build_ABSTRACTMEMBERTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.BOOLEAN_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","owns"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractMemberType"), schema, false,
            true, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SuccessionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="substitution"/&gt;
     *          &lt;enumeration value="division"/&gt;
     *          &lt;enumeration value="fusion"/&gt;
     *          &lt;enumeration value="initiation"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SUCCESSIONTYPE_TYPE = build_SUCCESSIONTYPE_TYPE();
     
    private static AttributeType build_SUCCESSIONTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","SuccessionType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="UomIdentifier"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The simple type gml:UomIdentifer defines the syntax and value space of the unit of measure identifier.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:UomSymbol gml:UomURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType UOMIDENTIFIER_TYPE = build_UOMIDENTIFIER_TYPE();
     
    private static AttributeType build_UOMIDENTIFIER_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","UomIdentifier"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MeasureType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:MeasureType supports recording an amount encoded as a value of XML Schema double, together with a units of measure indicated by an attribute uom, short for "units Of measure". The value of the uom attribute identifies a reference system for the amount, usually a ratio or interval scale.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="double"&gt;
     *              &lt;attribute name="uom" type="gml:UomIdentifier" use="required"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MEASURETYPE_TYPE = build_MEASURETYPE_TYPE();
    
    private static ComplexType build_MEASURETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                UOMIDENTIFIER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","uom"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","MeasureType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.DOUBLE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridLengthType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDLENGTHTYPE_TYPE = build_GRIDLENGTHTYPE_TYPE();
    
    private static ComplexType build_GRIDLENGTHTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","GridLengthType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SignType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:SignType is a convenience type with values "+" (plus) and "-" (minus).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="-"/&gt;
     *          &lt;enumeration value="+"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SIGNTYPE_TYPE = build_SIGNTYPE_TYPE();
     
    private static AttributeType build_SIGNTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","SignType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractSurfacePatchType"/&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTSURFACEPATCHTYPE_TYPE = build_ABSTRACTSURFACEPATCHTYPE_TYPE();
    
    private static ComplexType build_ABSTRACTSURFACEPATCHTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractSurfacePatchType"), Collections.<PropertyDescriptor>emptyList(), false,
            true, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType final="#all" name="AggregationType"&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="set"/&gt;
     *          &lt;enumeration value="bag"/&gt;
     *          &lt;enumeration value="sequence"/&gt;
     *          &lt;enumeration value="array"/&gt;
     *          &lt;enumeration value="record"/&gt;
     *          &lt;enumeration value="table"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType AGGREGATIONTYPE_TYPE = build_AGGREGATIONTYPE_TYPE();
     
    private static AttributeType build_AGGREGATIONTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AggregationType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractParametricCurveSurfaceType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfacePatchType"&gt;
     *              &lt;attributeGroup ref="gml:AggregationAttributeGroup"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTPARAMETRICCURVESURFACETYPE_TYPE = build_ABSTRACTPARAMETRICCURVESURFACETYPE_TYPE();
    
    private static ComplexType build_ABSTRACTPARAMETRICCURVESURFACETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                AGGREGATIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","aggregationType"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractParametricCurveSurfaceType"), schema, false,
            true, Collections.<Filter>emptyList(), ABSTRACTSURFACEPATCHTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="AxisDirection"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The value of a gml:AxisDirection indicates the incrementation order to be used on an axis of the grid.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;pattern value="[\+\-][1-9][0-9]*"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType AXISDIRECTION_TYPE = build_AXISDIRECTION_TYPE();
     
    private static AttributeType build_AXISDIRECTION_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AxisDirection"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="CalDate"&gt;
     *      &lt;union memberTypes="date gYearMonth gYear"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType CALDATE_TYPE = build_CALDATE_TYPE();
     
    private static AttributeType build_CALDATE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CalDate"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NameList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="Name"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NAMELIST_TYPE = build_NAMELIST_TYPE();
     
    private static AttributeType build_NAMELIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","NameList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CodeListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:CodeListType provides for lists of terms. The values in an instance element shall all be valid according to the rules of the dictionary, classification scheme, or authority identified by the value of its codeSpace attribute.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:NameList"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CODELISTTYPE_TYPE = build_CODELISTTYPE_TYPE();
    
    private static ComplexType build_CODELISTTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","codeSpace"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CodeListType"), schema, false,
            false, Collections.<Filter>emptyList(), NAMELIST_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" mixed="true" name="AbstractMetaDataType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *      &lt;attribute ref="gml:id"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTMETADATATYPE_TYPE = build_ABSTRACTMETADATATYPE_TYPE();
    
    private static ComplexType build_ABSTRACTMETADATATYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ID_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","id"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractMetaDataType"), schema, false,
            true, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType mixed="true" name="GenericMetaDataType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent mixed="true"&gt;
     *          &lt;extension base="gml:AbstractMetaDataType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;any maxOccurs="unbounded" minOccurs="0" processContents="lax"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GENERICMETADATATYPE_TYPE = build_GENERICMETADATATYPE_TYPE();
    
    private static ComplexType build_GENERICMETADATATYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","GenericMetaDataType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), ABSTRACTMETADATATYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SpeedType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SPEEDTYPE_TYPE = build_SPEEDTYPE_TYPE();
    
    private static ComplexType build_SPEEDTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","SpeedType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="integerOrNilReason"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Extension to the respective XML Schema built-in simple type to allow a choice of either a value of the built-in simple type or a reason for a nil value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NilReasonEnumeration integer anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INTEGERORNILREASON_TYPE = build_INTEGERORNILREASON_TYPE();
     
    private static AttributeType build_INTEGERORNILREASON_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","integerOrNilReason"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NilReasonType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:NilReasonType defines a content model that allows recording of an explanation for a void value or other exception.
     *  gml:NilReasonType is a union of the following enumerated values:
     *  -	inapplicable there is no value
     *  -	missing the correct value is not readily available to the sender of this data. Furthermore, a correct value may not exist
     *  -	template the value will be available later
     *  -	unknown the correct value is not known to, and not computable by, the sender of this data. However, a correct value probably exists
     *  -	withheld the value is not divulged
     *  -	other:text other brief explanation, where text is a string of two or more characters with no included spaces
     *  and
     *  -	anyURI which should refer to a resource which describes the reason for the exception
     *  A particular community may choose to assign more detailed semantics to the standard values provided. Alternatively, the URI method enables a specific or more complete explanation for the absence of a value to be provided elsewhere and indicated by-reference in an instance document.
     *  gml:NilReasonType is used as a member of a union in a number of simple content types where it is necessary to permit a value from the NilReasonType union as an alternative to the primary type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NilReasonEnumeration anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NILREASONTYPE_TYPE = build_NILREASONTYPE_TYPE();
     
    private static AttributeType build_NILREASONTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","NilReasonType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="_Quantity"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"&gt;
     *              &lt;attribute name="nilReason" type="gml:NilReasonType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType _QUANTITY_TYPE = build__QUANTITY_TYPE();
    
    private static ComplexType build__QUANTITY_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                NILREASONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","nilReason"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","_Quantity"), schema, false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TimeType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMETYPE_TYPE = build_TIMETYPE_TYPE();
    
    private static ComplexType build_TIMETYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","TimeType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="booleanList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="boolean"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BOOLEANLIST_TYPE = build_BOOLEANLIST_TYPE();
     
    private static AttributeType build_BOOLEANLIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","booleanList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="QNameList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="QName"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType QNAMELIST_TYPE = build_QNAMELIST_TYPE();
     
    private static AttributeType build_QNAMELIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","QNameList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="KnotTypesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This enumeration type specifies values for the knots' type (see ISO 19107:2003, 6.4.25).&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="uniform"/&gt;
     *          &lt;enumeration value="quasiUniform"/&gt;
     *          &lt;enumeration value="piecewiseBezier"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType KNOTTYPESTYPE_TYPE = build_KNOTTYPESTYPE_TYPE();
     
    private static AttributeType build_KNOTTYPESTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","KnotTypesType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="_Boolean"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="boolean"&gt;
     *              &lt;attribute name="nilReason" type="gml:NilReasonType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType _BOOLEAN_TYPE = build__BOOLEAN_TYPE();
    
    private static ComplexType build__BOOLEAN_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                NILREASONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","nilReason"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","_Boolean"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.BOOLEAN_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SurfacePatchArrayPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:SurfacePatchArrayPropertyType is a container for a sequence of surface patches.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
     *          &lt;element ref="gml:AbstractSurfacePatch"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SURFACEPATCHARRAYPROPERTYTYPE_TYPE = build_SURFACEPATCHARRAYPROPERTYTYPE_TYPE();
    
    private static ComplexType build_SURFACEPATCHARRAYPROPERTYTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ABSTRACTSURFACEPATCHTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","AbstractSurfacePatch"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","SurfacePatchArrayPropertyType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="doubleList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="double"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DOUBLELIST_TYPE = build_DOUBLELIST_TYPE();
     
    private static AttributeType build_DOUBLELIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","doubleList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NCNameList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="NCName"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NCNAMELIST_TYPE = build_NCNAMELIST_TYPE();
     
    private static AttributeType build_NCNAMELIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","NCNameList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectPositionListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;posList instances (and other instances with the content model specified by DirectPositionListType) hold the coordinates for a sequence of direct positions within the same coordinate reference system (CRS).
     *  if no srsName attribute is given, the CRS shall be specified as part of the larger context this geometry element is part of, typically a geometric object like a point, curve, etc. 
     *  The optional attribute count specifies the number of direct positions in the list. If the attribute count is present then the attribute srsDimension shall be present, too.
     *  The number of entries in the list is equal to the product of the dimensionality of the coordinate reference system (i.e. it is a derived value of the coordinate reference system definition) and the number of direct positions.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleList"&gt;
     *              &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
     *              &lt;attribute name="count" type="positiveInteger"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTPOSITIONLISTTYPE_TYPE = build_DIRECTPOSITIONLISTTYPE_TYPE();
    
    private static ComplexType build_DIRECTPOSITIONLISTTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                NCNAMELIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","axisLabels"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.POSITIVEINTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","srsDimension"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","srsName"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                NCNAMELIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","uomLabels"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.POSITIVEINTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","count"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","DirectPositionListType"), schema, false,
            false, Collections.<Filter>emptyList(), DOUBLELIST_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ScaleType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SCALETYPE_TYPE = build_SCALETYPE_TYPE();
    
    private static ComplexType build_SCALETYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","ScaleType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AreaType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType AREATYPE_TYPE = build_AREATYPE_TYPE();
    
    private static ComplexType build_AREATYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AreaType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NameOrNilReasonList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:NameOrNilReason"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NAMEORNILREASONLIST_TYPE = build_NAMEORNILREASONLIST_TYPE();
     
    private static AttributeType build_NAMEORNILREASONLIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","NameOrNilReasonList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CodeOrNilReasonListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:CodeOrNilReasonListType provides for lists of terms. The values in an instance element shall all be valid according to the rules of the dictionary, classification scheme, or authority identified by the value of its codeSpace attribute. An instance element may also include embedded values from NilReasonType. It is intended to be used in situations where a term or classification is expected, but the value may be absent for some reason.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:NameOrNilReasonList"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CODEORNILREASONLISTTYPE_TYPE = build_CODEORNILREASONLISTTYPE_TYPE();
    
    private static ComplexType build_CODEORNILREASONLISTTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","codeSpace"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CodeOrNilReasonListType"), schema, false,
            false, Collections.<Filter>emptyList(), NAMEORNILREASONLIST_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CategoryExtentType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:CodeOrNilReasonListType"&gt;
     *              &lt;length value="2"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CATEGORYEXTENTTYPE_TYPE = build_CATEGORYEXTENTTYPE_TYPE();
    
    private static ComplexType build_CATEGORYEXTENTTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CategoryExtentType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), CODEORNILREASONLISTTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="_Count"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="integer"&gt;
     *              &lt;attribute name="nilReason" type="gml:NilReasonType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType _COUNT_TYPE = build__COUNT_TYPE();
    
    private static ComplexType build__COUNT_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                NILREASONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","nilReason"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","_Count"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.INTEGER_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="booleanOrNilReasonList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:booleanOrNilReason"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BOOLEANORNILREASONLIST_TYPE = build_BOOLEANORNILREASONLIST_TYPE();
     
    private static AttributeType build_BOOLEANORNILREASONLIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","booleanOrNilReasonList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="UomSymbol"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type specifies a character string of length at least one, and restricted such that it must not contain any of the following characters: ":" (colon), " " (space), (newline), (carriage return), (tab). This allows values corresponding to familiar abbreviations, such as "kg", "m/s", etc. 
     *  It is recommended that the symbol be an identifier for a unit of measure as specified in the "Unified Code of Units of Measure" (UCUM) (http://aurora.regenstrief.org/UCUM). This provides a set of symbols and a grammar for constructing identifiers for units of measure that are unique, and may be easily entered with a keyboard supporting the limited character set known as 7-bit ASCII. ISO 2955 formerly provided a specification with this scope, but was withdrawn in 2001. UCUM largely follows ISO 2955 with modifications to remove ambiguities and other problems.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;pattern value="[^: \n\r\t]+"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType UOMSYMBOL_TYPE = build_UOMSYMBOL_TYPE();
     
    private static AttributeType build_UOMSYMBOL_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","UomSymbol"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractFeatureMemberType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;To create a collection of GML features, a property type shall be derived by extension from gml:AbstractFeatureMemberType.
     *  By default, this abstract property type does not imply any ownership of the features in the collection. The owns attribute of gml:OwnershipAttributeGroup may be used on a property element instance to assert ownership of a feature in the collection. A collection shall not own a feature already owned by another object.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *      &lt;attributeGroup ref="gml:OwnershipAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTFEATUREMEMBERTYPE_TYPE = build_ABSTRACTFEATUREMEMBERTYPE_TYPE();
    
    private static ComplexType build_ABSTRACTFEATUREMEMBERTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.BOOLEAN_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","owns"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractFeatureMemberType"), schema, false,
            true, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="UnitOfMeasureType"&gt;
     *      &lt;sequence/&gt;
     *      &lt;attribute name="uom" type="gml:UomIdentifier" use="required"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType UNITOFMEASURETYPE_TYPE = build_UNITOFMEASURETYPE_TYPE();
    
    private static ComplexType build_UNITOFMEASURETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                UOMIDENTIFIER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","uom"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","UnitOfMeasureType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="FormulaType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" name="a" type="double"/&gt;
     *          &lt;element name="b" type="double"/&gt;
     *          &lt;element name="c" type="double"/&gt;
     *          &lt;element minOccurs="0" name="d" type="double"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType FORMULATYPE_TYPE = build_FORMULATYPE_TYPE();
    
    private static ComplexType build_FORMULATYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","a"), 0, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","b"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","c"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","d"), 0, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","FormulaType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ConversionToPreferredUnitType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The inherited attribute uom references the preferred unit that this conversion applies to. The conversion of a unit to the preferred unit for this physical quantity type is specified by an arithmetic conversion (scaling and/or offset). The content model extends gml:UnitOfMeasureType, which has a mandatory attribute uom which identifies the preferred unit for the physical quantity type that this conversion applies to. The conversion is specified by a choice of 
     *  -	gml:factor, which defines the scale factor, or
     *  -	gml:formula, which defines a formula 
     *  by which a value using the conventional unit of measure can be converted to obtain the corresponding value using the preferred unit of measure.  
     *  The formula defines the parameters of a simple formula by which a value using the conventional unit of measure can be converted to the corresponding value using the preferred unit of measure. The formula element contains elements a, b, c and d, whose values use the XML Schema type double. These values are used in the formula y = (a + bx) / (c + dx), where x is a value using this unit, and y is the corresponding value using the base unit. The elements a and d are optional, and if values are not provided, those parameters are considered to be zero. If values are not provided for both a and d, the formula is equivalent to a fraction with numerator and denominator parameters.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:UnitOfMeasureType"&gt;
     *              &lt;choice&gt;
     *                  &lt;element name="factor" type="double"/&gt;
     *                  &lt;element name="formula" type="gml:FormulaType"/&gt;
     *              &lt;/choice&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CONVERSIONTOPREFERREDUNITTYPE_TYPE = build_CONVERSIONTOPREFERREDUNITTYPE_TYPE();
    
    private static ComplexType build_CONVERSIONTOPREFERREDUNITTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","factor"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                FORMULATYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","formula"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","ConversionToPreferredUnitType"), schema, false,
            false, Collections.<Filter>emptyList(), UNITOFMEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CodeType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:CodeType is a generalized type to be used for a term, keyword or name.
     *  It adds a XML attribute codeSpace to a term, where the value of the codeSpace attribute (if present) shall indicate a dictionary, thesaurus, classification scheme, authority, or pattern for the term.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="string"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CODETYPE_TYPE = build_CODETYPE_TYPE();
    
    private static ComplexType build_CODETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","codeSpace"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CodeType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="_Category"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:CodeType"&gt;
     *              &lt;attribute name="nilReason" type="gml:NilReasonType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType _CATEGORY_TYPE = build__CATEGORY_TYPE();
    
    private static ComplexType build__CATEGORY_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                NILREASONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","nilReason"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","_Category"), schema, false,
            false, Collections.<Filter>emptyList(), CODETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectPositionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Direct position instances hold the coordinates for a position within some coordinate reference system (CRS). Since direct positions, as data types, will often be included in larger objects (such as geometry elements) that have references to CRS, the srsName attribute will in general be missing, if this particular direct position is included in a larger element with such a reference to a CRS. In this case, the CRS is implicitly assumed to take on the value of the containing object's CRS.
     *  if no srsName attribute is given, the CRS shall be specified as part of the larger context this geometry element is part of, typically a geometric object like a point, curve, etc.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleList"&gt;
     *              &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTPOSITIONTYPE_TYPE = build_DIRECTPOSITIONTYPE_TYPE();
    
    private static ComplexType build_DIRECTPOSITIONTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                NCNAMELIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","axisLabels"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.POSITIVEINTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","srsDimension"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","srsName"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                NCNAMELIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","uomLabels"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","DirectPositionType"), schema, false,
            false, Collections.<Filter>emptyList(), DOUBLELIST_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VectorType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;For some applications the components of the position may be adjusted to yield a unit vector.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:DirectPositionType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VECTORTYPE_TYPE = build_VECTORTYPE_TYPE();
    
    private static ComplexType build_VECTORTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","VectorType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), DIRECTPOSITIONTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AngleType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ANGLETYPE_TYPE = build_ANGLETYPE_TYPE();
    
    private static ComplexType build_ANGLETYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AngleType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DirectionVectorType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Direction vectors are specified by providing components of a vector.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:vector"/&gt;
     *          &lt;sequence&gt;
     *              &lt;annotation&gt;
     *                  &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *              &lt;/annotation&gt;
     *              &lt;element name="horizontalAngle" type="gml:AngleType"/&gt;
     *              &lt;element name="verticalAngle" type="gml:AngleType"/&gt;
     *          &lt;/sequence&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DIRECTIONVECTORTYPE_TYPE = build_DIRECTIONVECTORTYPE_TYPE();
    
    private static ComplexType build_DIRECTIONVECTORTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                VECTORTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","vector"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                ANGLETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","horizontalAngle"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                ANGLETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","verticalAngle"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","DirectionVectorType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="CompassPointEnumeration"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;These directions are necessarily approximate, giving direction with a precision of 22.5¡. It is thus generally unnecessary to specify the reference frame, though this may be detailed in the definition of a GML application language.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="N"/&gt;
     *          &lt;enumeration value="NNE"/&gt;
     *          &lt;enumeration value="NE"/&gt;
     *          &lt;enumeration value="ENE"/&gt;
     *          &lt;enumeration value="E"/&gt;
     *          &lt;enumeration value="ESE"/&gt;
     *          &lt;enumeration value="SE"/&gt;
     *          &lt;enumeration value="SSE"/&gt;
     *          &lt;enumeration value="S"/&gt;
     *          &lt;enumeration value="SSW"/&gt;
     *          &lt;enumeration value="SW"/&gt;
     *          &lt;enumeration value="WSW"/&gt;
     *          &lt;enumeration value="W"/&gt;
     *          &lt;enumeration value="WNW"/&gt;
     *          &lt;enumeration value="NW"/&gt;
     *          &lt;enumeration value="NNW"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType COMPASSPOINTENUMERATION_TYPE = build_COMPASSPOINTENUMERATION_TYPE();
     
    private static AttributeType build_COMPASSPOINTENUMERATION_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CompassPointEnumeration"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MeasureListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:MeasureListType provides for a list of quantities.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleList"&gt;
     *              &lt;attribute name="uom" type="gml:UomIdentifier" use="required"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MEASURELISTTYPE_TYPE = build_MEASURELISTTYPE_TYPE();
    
    private static ComplexType build_MEASURELISTTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                UOMIDENTIFIER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","uom"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","MeasureListType"), schema, false,
            false, Collections.<Filter>emptyList(), DOUBLELIST_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="DegreeValueType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="nonNegativeInteger"&gt;
     *          &lt;maxInclusive value="359"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DEGREEVALUETYPE_TYPE = build_DEGREEVALUETYPE_TYPE();
     
    private static AttributeType build_DEGREEVALUETYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","DegreeValueType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.NONNEGATIVEINTEGER_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DegreesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:DegreeValueType"&gt;
     *              &lt;attribute name="direction"&gt;
     *                  &lt;simpleType&gt;
     *                      &lt;restriction base="string"&gt;
     *                          &lt;enumeration value="N"/&gt;
     *                          &lt;enumeration value="E"/&gt;
     *                          &lt;enumeration value="S"/&gt;
     *                          &lt;enumeration value="W"/&gt;
     *                          &lt;enumeration value="+"/&gt;
     *                          &lt;enumeration value="-"/&gt;
     *                      &lt;/restriction&gt;
     *                  &lt;/simpleType&gt;
     *              &lt;/attribute&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DEGREESTYPE_TYPE = build_DEGREESTYPE_TYPE();
    
    private static ComplexType build_DEGREESTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","DegreesType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), DEGREEVALUETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="DecimalMinutesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="decimal"&gt;
     *          &lt;minInclusive value="0.00"/&gt;
     *          &lt;maxExclusive value="60.00"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DECIMALMINUTESTYPE_TYPE = build_DECIMALMINUTESTYPE_TYPE();
     
    private static AttributeType build_DECIMALMINUTESTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","DecimalMinutesType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.DECIMAL_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="ArcMinutesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="nonNegativeInteger"&gt;
     *          &lt;maxInclusive value="59"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ARCMINUTESTYPE_TYPE = build_ARCMINUTESTYPE_TYPE();
     
    private static AttributeType build_ARCMINUTESTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","ArcMinutesType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.NONNEGATIVEINTEGER_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="ArcSecondsType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="decimal"&gt;
     *          &lt;minInclusive value="0.00"/&gt;
     *          &lt;maxExclusive value="60.00"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType ARCSECONDSTYPE_TYPE = build_ARCSECONDSTYPE_TYPE();
     
    private static AttributeType build_ARCSECONDSTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","ArcSecondsType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.DECIMAL_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DMSAngleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:degrees"/&gt;
     *          &lt;choice minOccurs="0"&gt;
     *              &lt;element ref="gml:decimalMinutes"/&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:minutes"/&gt;
     *                  &lt;element minOccurs="0" ref="gml:seconds"/&gt;
     *              &lt;/sequence&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DMSANGLETYPE_TYPE = build_DMSANGLETYPE_TYPE();
    
    private static ComplexType build_DMSANGLETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                DEGREESTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","degrees"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                DECIMALMINUTESTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","decimalMinutes"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                ARCMINUTESTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","minutes"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                ARCSECONDSTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","seconds"), 0, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","DMSAngleType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AngleChoiceType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;choice&gt;
     *          &lt;element ref="gml:angle"/&gt;
     *          &lt;element ref="gml:dmsAngle"/&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ANGLECHOICETYPE_TYPE = build_ANGLECHOICETYPE_TYPE();
    
    private static ComplexType build_ANGLECHOICETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ANGLETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","angle"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                DMSANGLETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","dmsAngle"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AngleChoiceType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CodeWithAuthorityType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:CodeWithAuthorityType requires that the codeSpace attribute is provided in an instance.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:CodeType"&gt;
     *              &lt;attribute name="codeSpace" type="anyURI" use="required"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CODEWITHAUTHORITYTYPE_TYPE = build_CODEWITHAUTHORITYTYPE_TYPE();
    
    private static ComplexType build_CODEWITHAUTHORITYTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","codeSpace"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CodeWithAuthorityType"), schema, false,
            false, Collections.<Filter>emptyList(), CODETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="booleanOrNilReason"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Extension to the respective XML Schema built-in simple type to allow a choice of either a value of the built-in simple type or a reason for a nil value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NilReasonEnumeration boolean anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType BOOLEANORNILREASON_TYPE = build_BOOLEANORNILREASON_TYPE();
     
    private static AttributeType build_BOOLEANORNILREASON_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","booleanOrNilReason"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="doubleOrNilReason"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Extension to the respective XML Schema built-in simple type to allow a choice of either a value of the built-in simple type or a reason for a nil value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NilReasonEnumeration double anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DOUBLEORNILREASON_TYPE = build_DOUBLEORNILREASON_TYPE();
     
    private static AttributeType build_DOUBLEORNILREASON_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","doubleOrNilReason"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NameOrNilReason"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Extension to the respective XML Schema built-in simple type to allow a choice of either a value of the built-in simple type or a reason for a nil value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NilReasonEnumeration Name anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NAMEORNILREASON_TYPE = build_NAMEORNILREASON_TYPE();
     
    private static AttributeType build_NAMEORNILREASON_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","NameOrNilReason"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="UomURI"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type specifies a URI, restricted such that it must start with one of the following sequences: "#", "./", "../", or a string of characters followed by a ":". These patterns ensure that the most common URI forms are supported, including absolute and relative URIs and URIs that are simple fragment identifiers, but prohibits certain forms of relative URI that could be mistaken for unit of measure symbol . 
     *  NOTE	It is possible to re-write such a relative URI to conform to the restriction (e.g. "./m/s").
     *  In an instance document, on elements of type gml:MeasureType the mandatory uom attribute shall carry a value corresponding to either 
     *  -	a conventional unit of measure symbol,
     *  -	a link to a definition of a unit of measure that does not have a conventional symbol, or when it is desired to indicate a precise or variant definition.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="anyURI"&gt;
     *          &lt;pattern value="([a-zA-Z][a-zA-Z0-9\-\+\.]*:|\.\./|\./|#).*"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType UOMURI_TYPE = build_UOMURI_TYPE();
     
    private static AttributeType build_UOMURI_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","UomURI"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYURI_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="CoordinatesType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This type is deprecated for tuples with ordinate values that are numbers.
     *  CoordinatesType is a text string, intended to be used to record an array of tuples or coordinates. 
     *  While it is not possible to enforce the internal structure of the string through schema validation, some optional attributes have been provided in previous versions of GML to support a description of the internal structure. These attributes are deprecated. The attributes were intended to be used as follows:
     *  Decimal	symbol used for a decimal point (default="." a stop or period)
     *  cs        	symbol used to separate components within a tuple or coordinate string (default="," a comma)
     *  ts        	symbol used to separate tuples or coordinate strings (default=" " a space)
     *  Since it is based on the XML Schema string type, CoordinatesType may be used in the construction of tables of tuples or arrays of tuples, including ones that contain mixed text and numeric values.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="string"&gt;
     *              &lt;attribute default="." name="decimal" type="string"/&gt;
     *              &lt;attribute default="," name="cs" type="string"/&gt;
     *              &lt;attribute default=" " name="ts" type="string"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType COORDINATESTYPE_TYPE = build_COORDINATESTYPE_TYPE();
    
    private static ComplexType build_COORDINATESTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.STRING_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","decimal"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.STRING_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","cs"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.STRING_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","ts"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CoordinatesType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EnvelopeType"&gt;
     *      &lt;choice&gt;
     *          &lt;sequence&gt;
     *              &lt;element name="lowerCorner" type="gml:DirectPositionType"/&gt;
     *              &lt;element name="upperCorner" type="gml:DirectPositionType"/&gt;
     *          &lt;/sequence&gt;
     *          &lt;element maxOccurs="2" minOccurs="2" ref="gml:pos"&gt;
     *              &lt;annotation&gt;
     *                  &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *          &lt;element ref="gml:coordinates"/&gt;
     *      &lt;/choice&gt;
     *      &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ENVELOPETYPE_TYPE = build_ENVELOPETYPE_TYPE();
    
    private static ComplexType build_ENVELOPETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                DIRECTPOSITIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","lowerCorner"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                DIRECTPOSITIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","upperCorner"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                DIRECTPOSITIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","pos"), 2, 2, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                COORDINATESTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","coordinates"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                NCNAMELIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","axisLabels"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.POSITIVEINTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","srsDimension"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","srsName"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                NCNAMELIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","uomLabels"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","EnvelopeType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="TimePositionUnion"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The simple type gml:TimePositionUnion is a union of XML Schema simple types which instantiate the subtypes for temporal position described in ISO 19108.
     *   An ordinal era may be referenced via URI.  A decimal value may be used to indicate the distance from the scale origin .  time is used for a position that recurs daily (see ISO 19108:2002 5.4.4.2).
     *   Finally, calendar and clock forms that support the representation of time in systems based on years, months, days, hours, minutes and seconds, in a notation following ISO 8601, are assembled by gml:CalDate&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:CalDate time dateTime anyURI decimal"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType TIMEPOSITIONUNION_TYPE = build_TIMEPOSITIONUNION_TYPE();
     
    private static AttributeType build_TIMEPOSITIONUNION_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","TimePositionUnion"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="TimeIndeterminateValueType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;These values are interpreted as follows: 
     *  -	"unknown" indicates that no specific value for temporal position is provided.
     *  -	"now" indicates that the specified value shall be replaced with the current temporal position whenever the value is accessed.
     *  -	"before" indicates that the actual temporal position is unknown, but it is known to be before the specified value.
     *  -	"after" indicates that the actual temporal position is unknown, but it is known to be after the specified value.
     *  A value for indeterminatePosition may 
     *  -	be used either alone, or 
     *  -	qualify a specific value for temporal position.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="after"/&gt;
     *          &lt;enumeration value="before"/&gt;
     *          &lt;enumeration value="now"/&gt;
     *          &lt;enumeration value="unknown"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType TIMEINDETERMINATEVALUETYPE_TYPE = build_TIMEINDETERMINATEVALUETYPE_TYPE();
     
    private static AttributeType build_TIMEINDETERMINATEVALUETYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","TimeIndeterminateValueType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType final="#all" name="TimePositionType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The method for identifying a temporal position is specific to each temporal reference system.  gml:TimePositionType supports the description of temporal position according to the subtypes described in ISO 19108.
     *  Values based on calendars and clocks use lexical formats that are based on ISO 8601, as described in XML Schema Part 2:2001. A decimal value may be used with coordinate systems such as GPS time or UNIX time. A URI may be used to provide a reference to some era in an ordinal reference system . 
     *  In common with many of the components modelled as data types in the ISO 19100 series of International Standards, the corresponding GML component has simple content. However, the content model gml:TimePositionType is defined in several steps.
     *  Three XML attributes appear on gml:TimePositionType:
     *  A time value shall be associated with a temporal reference system through the frame attribute that provides a URI reference that identifies a description of the reference system. Following ISO 19108, the Gregorian calendar with UTC is the default reference system, but others may also be used. Components for describing temporal reference systems are described in 14.4, but it is not required that the reference system be described in this, as the reference may refer to anything that may be indentified with a URI.  
     *  For time values using a calendar containing more than one era, the (optional) calendarEraName attribute provides the name of the calendar era.  
     *  Inexact temporal positions may be expressed using the optional indeterminatePosition attribute.  This takes a value from an enumeration.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:TimePositionUnion"&gt;
     *              &lt;attribute default="#ISO-8601" name="frame" type="anyURI"/&gt;
     *              &lt;attribute name="calendarEraName" type="string"/&gt;
     *              &lt;attribute name="indeterminatePosition" type="gml:TimeIndeterminateValueType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEPOSITIONTYPE_TYPE = build_TIMEPOSITIONTYPE_TYPE();
    
    private static ComplexType build_TIMEPOSITIONTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","frame"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.STRING_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","calendarEraName"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                TIMEINDETERMINATEVALUETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","indeterminatePosition"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","TimePositionType"), schema, false,
            false, Collections.<Filter>emptyList(), TIMEPOSITIONUNION_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="EnvelopeWithTimePeriodType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:EnvelopeType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="beginPosition" type="gml:TimePositionType"/&gt;
     *                  &lt;element name="endPosition" type="gml:TimePositionType"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute default="#ISO-8601" name="frame" type="anyURI"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ENVELOPEWITHTIMEPERIODTYPE_TYPE = build_ENVELOPEWITHTIMEPERIODTYPE_TYPE();
    
    private static ComplexType build_ENVELOPEWITHTIMEPERIODTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                TIMEPOSITIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","beginPosition"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                TIMEPOSITIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","endPosition"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYURI_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","frame"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","EnvelopeWithTimePeriodType"), schema, false,
            false, Collections.<Filter>emptyList(), ENVELOPETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="NilReasonEnumeration"&gt;
     *      &lt;union&gt;
     *          &lt;simpleType&gt;
     *              &lt;restriction base="string"&gt;
     *                  &lt;enumeration value="inapplicable"/&gt;
     *                  &lt;enumeration value="missing"/&gt;
     *                  &lt;enumeration value="template"/&gt;
     *                  &lt;enumeration value="unknown"/&gt;
     *                  &lt;enumeration value="withheld"/&gt;
     *              &lt;/restriction&gt;
     *          &lt;/simpleType&gt;
     *          &lt;simpleType&gt;
     *              &lt;restriction base="string"&gt;
     *                  &lt;pattern value="other:\w{2,}"/&gt;
     *              &lt;/restriction&gt;
     *          &lt;/simpleType&gt;
     *      &lt;/union&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType NILREASONENUMERATION_TYPE = build_NILREASONENUMERATION_TYPE();
     
    private static AttributeType build_NILREASONENUMERATION_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","NilReasonEnumeration"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ArrayAssociationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:AbstractObject"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:OwnershipAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ARRAYASSOCIATIONTYPE_TYPE = build_ARRAYASSOCIATIONTYPE_TYPE();
    
    private static ComplexType build_ARRAYASSOCIATIONTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.ANYTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","AbstractObject"), 0, 2147483647, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.BOOLEAN_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","owns"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","ArrayAssociationType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractRingType"&gt;
     *      &lt;sequence/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTRINGTYPE_TYPE = build_ABSTRACTRINGTYPE_TYPE();
    
    private static ComplexType build_ABSTRACTRINGTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractRingType"), Collections.<PropertyDescriptor>emptyList(), false,
            true, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractRingPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A property with the content model of gml:AbstractRingPropertyType encapsulates a ring to represent the surface boundary property of a surface.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:AbstractRing"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTRINGPROPERTYTYPE_TYPE = build_ABSTRACTRINGPROPERTYTYPE_TYPE();
    
    private static ComplexType build_ABSTRACTRINGPROPERTYTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ABSTRACTRINGTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","AbstractRing"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractRingPropertyType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SurfaceInterpolationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:SurfaceInterpolationType is a list of codes that may be used to identify the interpolation mechanisms specified by an application schema.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="none"/&gt;
     *          &lt;enumeration value="planar"/&gt;
     *          &lt;enumeration value="spherical"/&gt;
     *          &lt;enumeration value="elliptical"/&gt;
     *          &lt;enumeration value="conic"/&gt;
     *          &lt;enumeration value="tin"/&gt;
     *          &lt;enumeration value="parametricCurve"/&gt;
     *          &lt;enumeration value="polynomialSpline"/&gt;
     *          &lt;enumeration value="rationalSpline"/&gt;
     *          &lt;enumeration value="triangulatedSpline"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SURFACEINTERPOLATIONTYPE_TYPE = build_SURFACEINTERPOLATIONTYPE_TYPE();
     
    private static AttributeType build_SURFACEINTERPOLATIONTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","SurfaceInterpolationType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="PolygonPatchType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfacePatchType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element minOccurs="0" ref="gml:exterior"/&gt;
     *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:interior"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="planar" name="interpolation" type="gml:SurfaceInterpolationType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType POLYGONPATCHTYPE_TYPE = build_POLYGONPATCHTYPE_TYPE();
    
    private static ComplexType build_POLYGONPATCHTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ABSTRACTRINGPROPERTYTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","exterior"), 0, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                ABSTRACTRINGPROPERTYTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","interior"), 0, 2147483647, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                SURFACEINTERPOLATIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","interpolation"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","PolygonPatchType"), schema, false,
            false, Collections.<Filter>emptyList(), ABSTRACTSURFACEPATCHTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="stringOrNilReason"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;Extension to the respective XML Schema built-in simple type to allow a choice of either a value of the built-in simple type or a reason for a nil value.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;union memberTypes="gml:NilReasonEnumeration string anyURI"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType STRINGORNILREASON_TYPE = build_STRINGORNILREASON_TYPE();
     
    private static AttributeType build_STRINGORNILREASON_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","stringOrNilReason"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="LengthType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;This is a prototypical definition for a specific measure type defined as a vacuous extension (i.e. aliases) of gml:MeasureType. In this case, the content model supports the description of a length (or distance) quantity, with its units. The unit of measure referenced by uom shall be suitable for a length, such as metres or feet.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType LENGTHTYPE_TYPE = build_LENGTHTYPE_TYPE();
    
    private static ComplexType build_LENGTHTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","LengthType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="_SecondDefiningParameter"&gt;
     *      &lt;choice&gt;
     *          &lt;element name="inverseFlattening" type="gml:MeasureType"/&gt;
     *          &lt;element name="semiMinorAxis" type="gml:LengthType"/&gt;
     *          &lt;element default="true" name="isSphere" type="boolean"/&gt;
     *      &lt;/choice&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType _SECONDDEFININGPARAMETER_TYPE = build__SECONDDEFININGPARAMETER_TYPE();
    
    private static ComplexType build__SECONDDEFININGPARAMETER_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                MEASURETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","inverseFlattening"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                LENGTHTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","semiMinorAxis"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.BOOLEAN_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","isSphere"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","_SecondDefiningParameter"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="_secondDefiningParameter"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:SecondDefiningParameter"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType _sECONDDEFININGPARAMETER_TYPE = build__sECONDDEFININGPARAMETER_TYPE();
    
    private static ComplexType build__sECONDDEFININGPARAMETER_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                _SECONDDEFININGPARAMETER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","SecondDefiningParameter"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","_secondDefiningParameter"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="VolumeType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:MeasureType"/&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType VOLUMETYPE_TYPE = build_VOLUMETYPE_TYPE();
    
    private static ComplexType build_VOLUMETYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","VolumeType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="SequenceRuleEnumeration"&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="Linear"/&gt;
     *          &lt;enumeration value="Boustrophedonic"/&gt;
     *          &lt;enumeration value="Cantor-diagonal"/&gt;
     *          &lt;enumeration value="Spiral"/&gt;
     *          &lt;enumeration value="Morton"/&gt;
     *          &lt;enumeration value="Hilbert"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType SEQUENCERULEENUMERATION_TYPE = build_SEQUENCERULEENUMERATION_TYPE();
     
    private static AttributeType build_SEQUENCERULEENUMERATION_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","SequenceRuleEnumeration"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="IncrementOrder"&gt;
     *      &lt;annotation&gt;
     *          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="+x+y"/&gt;
     *          &lt;enumeration value="+y+x"/&gt;
     *          &lt;enumeration value="+x-y"/&gt;
     *          &lt;enumeration value="-x-y"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INCREMENTORDER_TYPE = build_INCREMENTORDER_TYPE();
     
    private static AttributeType build_INCREMENTORDER_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","IncrementOrder"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="AxisDirectionList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The different values in a gml:AxisDirectionList indicate the incrementation order to be used on all axes of the grid. Each axis shall be mentioned once and only once.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:AxisDirection"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType AXISDIRECTIONLIST_TYPE = build_AXISDIRECTIONLIST_TYPE();
     
    private static AttributeType build_AXISDIRECTIONLIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AxisDirectionList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="SequenceRuleType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;The gml:SequenceRuleType is derived from the gml:SequenceRuleEnumeration through the addition of an axisOrder attribute.  The gml:SequenceRuleEnumeration is an enumerated type. The rule names are defined in ISO 19123. If no rule name is specified the default is "Linear".&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:SequenceRuleEnumeration"&gt;
     *              &lt;attribute name="order" type="gml:IncrementOrder"&gt;
     *                  &lt;annotation&gt;
     *                      &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
     *                  &lt;/annotation&gt;
     *              &lt;/attribute&gt;
     *              &lt;attribute name="axisOrder" type="gml:AxisDirectionList"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType SEQUENCERULETYPE_TYPE = build_SEQUENCERULETYPE_TYPE();
    
    private static ComplexType build_SEQUENCERULETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                INCREMENTORDER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","order"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                AXISDIRECTIONLIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","axisOrder"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","SequenceRuleType"), schema, false,
            false, Collections.<Filter>emptyList(), SEQUENCERULEENUMERATION_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="integerList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="integer"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INTEGERLIST_TYPE = build_INTEGERLIST_TYPE();
     
    private static AttributeType build_INTEGERLIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","integerList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridFunctionType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element minOccurs="0" name="sequenceRule" type="gml:SequenceRuleType"/&gt;
     *          &lt;element minOccurs="0" name="startPoint" type="gml:integerList"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDFUNCTIONTYPE_TYPE = build_GRIDFUNCTIONTYPE_TYPE();
    
    private static ComplexType build_GRIDFUNCTIONTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                SEQUENCERULETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","sequenceRule"), 0, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                INTEGERLIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","startPoint"), 0, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","GridFunctionType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="doubleOrNilReasonList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:doubleOrNilReason"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType DOUBLEORNILREASONLIST_TYPE = build_DOUBLEORNILREASONLIST_TYPE();
     
    private static AttributeType build_DOUBLEORNILREASONLIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","doubleOrNilReasonList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="MeasureOrNilReasonListType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:MeasureOrNilReasonListType provides for a list of quantities. An instance element may also include embedded values from NilReasonType. It is intended to be used in situations where a value is expected, but the value may be absent for some reason.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="gml:doubleOrNilReasonList"&gt;
     *              &lt;attribute name="uom" type="gml:UomIdentifier" use="required"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType MEASUREORNILREASONLISTTYPE_TYPE = build_MEASUREORNILREASONLISTTYPE_TYPE();
    
    private static ComplexType build_MEASUREORNILREASONLISTTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                UOMIDENTIFIER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","uom"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","MeasureOrNilReasonListType"), schema, false,
            false, Collections.<Filter>emptyList(), DOUBLEORNILREASONLIST_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="QuantityExtentType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;restriction base="gml:MeasureOrNilReasonListType"&gt;
     *              &lt;length value="2"/&gt;
     *          &lt;/restriction&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType QUANTITYEXTENTTYPE_TYPE = build_QUANTITYEXTENTTYPE_TYPE();
    
    private static ComplexType build_QUANTITYEXTENTTYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","QuantityExtentType"), Collections.<PropertyDescriptor>emptyList(), false,
            false, Collections.<Filter>emptyList(), MEASUREORNILREASONLISTTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractGeneralParameterValueType"&gt;
     *      &lt;sequence/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGENERALPARAMETERVALUETYPE_TYPE = build_ABSTRACTGENERALPARAMETERVALUETYPE_TYPE();
    
    private static ComplexType build_ABSTRACTGENERALPARAMETERVALUETYPE_TYPE() {
        ComplexType builtType;
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractGeneralParameterValueType"), Collections.<PropertyDescriptor>emptyList(), false,
            true, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AbstractGeneralParameterValuePropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:AbstractGeneralParameterValuePropertyType is a  property type for inline association roles to a parameter value or group of parameter values, always containing the values.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:AbstractGeneralParameterValue"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTGENERALPARAMETERVALUEPROPERTYTYPE_TYPE = build_ABSTRACTGENERALPARAMETERVALUEPROPERTYTYPE_TYPE();
    
    private static ComplexType build_ABSTRACTGENERALPARAMETERVALUEPROPERTYTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ABSTRACTGENERALPARAMETERVALUETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","AbstractGeneralParameterValue"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractGeneralParameterValuePropertyType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="TriangleType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfacePatchType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:exterior"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="planar" name="interpolation" type="gml:SurfaceInterpolationType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TRIANGLETYPE_TYPE = build_TRIANGLETYPE_TYPE();
    
    private static ComplexType build_TRIANGLETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ABSTRACTRINGPROPERTYTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","exterior"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                SURFACEINTERPOLATIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","interpolation"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","TriangleType"), schema, false,
            false, Collections.<Filter>emptyList(), ABSTRACTSURFACEPATCHTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="integerOrNilReasonList"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;A type for a list of values of the respective simple type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;list itemType="gml:integerOrNilReason"/&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType INTEGERORNILREASONLIST_TYPE = build_INTEGERORNILREASONLIST_TYPE();
     
    private static AttributeType build_INTEGERORNILREASONLIST_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","integerOrNilReasonList"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="CountExtentType"&gt;
     *      &lt;restriction base="gml:integerOrNilReasonList"&gt;
     *          &lt;length value="2"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType COUNTEXTENTTYPE_TYPE = build_COUNTEXTENTTYPE_TYPE();
     
    private static AttributeType build_COUNTEXTENTTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CountExtentType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), INTEGERORNILREASONLIST_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="AffinePlacementType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="location" type="gml:DirectPositionType"/&gt;
     *          &lt;element maxOccurs="unbounded" name="refDirection" type="gml:VectorType"/&gt;
     *          &lt;element name="inDimension" type="positiveInteger"/&gt;
     *          &lt;element name="outDimension" type="positiveInteger"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType AFFINEPLACEMENTTYPE_TYPE = build_AFFINEPLACEMENTTYPE_TYPE();
    
    private static ComplexType build_AFFINEPLACEMENTTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                DIRECTPOSITIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","location"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                VECTORTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","refDirection"), 1, 2147483647, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.POSITIVEINTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","inDimension"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.POSITIVEINTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","outDimension"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AffinePlacementType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ClothoidType_refLocation"&gt;
     *      &lt;sequence&gt;
     *          &lt;element ref="gml:AffinePlacement"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CLOTHOIDTYPE_REFLOCATION_TYPE = build_CLOTHOIDTYPE_REFLOCATION_TYPE();
    
    private static ComplexType build_CLOTHOIDTYPE_REFLOCATION_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                AFFINEPLACEMENTTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","AffinePlacement"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","ClothoidType_refLocation"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="CurveInterpolationType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:CurveInterpolationType is a list of codes that may be used to identify the interpolation mechanisms specified by an application schema.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;restriction base="string"&gt;
     *          &lt;enumeration value="linear"/&gt;
     *          &lt;enumeration value="geodesic"/&gt;
     *          &lt;enumeration value="circularArc3Points"/&gt;
     *          &lt;enumeration value="circularArc2PointWithBulge"/&gt;
     *          &lt;enumeration value="circularArcCenterPointWithRadius"/&gt;
     *          &lt;enumeration value="elliptical"/&gt;
     *          &lt;enumeration value="clothoid"/&gt;
     *          &lt;enumeration value="conic"/&gt;
     *          &lt;enumeration value="polynomialSpline"/&gt;
     *          &lt;enumeration value="cubicSpline"/&gt;
     *          &lt;enumeration value="rationalSpline"/&gt;
     *      &lt;/restriction&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType CURVEINTERPOLATIONTYPE_TYPE = build_CURVEINTERPOLATIONTYPE_TYPE();
     
    private static AttributeType build_CURVEINTERPOLATIONTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","CurveInterpolationType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.STRING_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="ClothoidType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element name="refLocation"&gt;
     *                      &lt;complexType name="ClothoidType_refLocation"&gt;
     *                          &lt;sequence&gt;
     *                              &lt;element ref="gml:AffinePlacement"/&gt;
     *                          &lt;/sequence&gt;
     *                      &lt;/complexType&gt;
     *                  &lt;/element&gt;
     *                  &lt;element name="scaleFactor" type="decimal"/&gt;
     *                  &lt;element name="startParameter" type="double"/&gt;
     *                  &lt;element name="endParameter" type="double"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="clothoid" name="interpolation" type="gml:CurveInterpolationType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType CLOTHOIDTYPE_TYPE = build_CLOTHOIDTYPE_TYPE();
    
    private static ComplexType build_CLOTHOIDTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                CLOTHOIDTYPE_REFLOCATION_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","refLocation"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DECIMAL_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","scaleFactor"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","startParameter"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","endParameter"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                CURVEINTERPOLATIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","interpolation"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","ClothoidType"), schema, false,
            false, Collections.<Filter>emptyList(), ABSTRACTCURVESEGMENTTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="RectangleType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:AbstractSurfacePatchType"&gt;
     *              &lt;sequence&gt;
     *                  &lt;element ref="gml:exterior"/&gt;
     *              &lt;/sequence&gt;
     *              &lt;attribute fixed="planar" name="interpolation" type="gml:SurfaceInterpolationType"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType RECTANGLETYPE_TYPE = build_RECTANGLETYPE_TYPE();
    
    private static ComplexType build_RECTANGLETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ABSTRACTRINGPROPERTYTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","exterior"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                SURFACEINTERPOLATIONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","interpolation"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","RectangleType"), schema, false,
            false, Collections.<Filter>emptyList(), ABSTRACTSURFACEPATCHTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridEnvelopeType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="low" type="gml:integerList"/&gt;
     *          &lt;element name="high" type="gml:integerList"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDENVELOPETYPE_TYPE = build_GRIDENVELOPETYPE_TYPE();
    
    private static ComplexType build_GRIDENVELOPETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                INTEGERLIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","low"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                INTEGERLIST_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","high"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","GridEnvelopeType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="GridLimitsType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="GridEnvelope" type="gml:GridEnvelopeType"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType GRIDLIMITSTYPE_TYPE = build_GRIDLIMITSTYPE_TYPE();
    
    private static ComplexType build_GRIDLIMITSTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                GRIDENVELOPETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","GridEnvelope"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","GridLimitsType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="KnotType"&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="value" type="double"/&gt;
     *          &lt;element name="multiplicity" type="nonNegativeInteger"/&gt;
     *          &lt;element name="weight" type="double"/&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType KNOTTYPE_TYPE = build_KNOTTYPE_TYPE();
    
    private static ComplexType build_KNOTTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","value"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.NONNEGATIVEINTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","multiplicity"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.DOUBLE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","weight"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","KnotType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="KnotPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;gml:KnotPropertyType encapsulates a knot to use it in a geometric type.&lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence&gt;
     *          &lt;element name="Knot" type="gml:KnotType"&gt;
     *              &lt;annotation&gt;
     *                  &lt;documentation&gt;A knot is a breakpoint on a piecewise spline curve.
     *  value is the value of the parameter at the knot of the spline (see ISO 19107:2003, 6.4.24.2).
     *  multiplicity is the multiplicity of this knot used in the definition of the spline (with the same weight).
     *  weight is the value of the averaging weight used for this knot of the spline.&lt;/documentation&gt;
     *              &lt;/annotation&gt;
     *          &lt;/element&gt;
     *      &lt;/sequence&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType KNOTPROPERTYTYPE_TYPE = build_KNOTPROPERTYTYPE_TYPE();
    
    private static ComplexType build_KNOTPROPERTYTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                KNOTTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","Knot"), 1, 1, false, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","KnotPropertyType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;simpleType name="TimeUnitType"&gt;
     *      &lt;union&gt;
     *          &lt;simpleType&gt;
     *              &lt;restriction base="string"&gt;
     *                  &lt;enumeration value="year"/&gt;
     *                  &lt;enumeration value="month"/&gt;
     *                  &lt;enumeration value="day"/&gt;
     *                  &lt;enumeration value="hour"/&gt;
     *                  &lt;enumeration value="minute"/&gt;
     *                  &lt;enumeration value="second"/&gt;
     *              &lt;/restriction&gt;
     *          &lt;/simpleType&gt;
     *          &lt;simpleType&gt;
     *              &lt;restriction base="string"&gt;
     *                  &lt;pattern value="other:\w{2,}"/&gt;
     *              &lt;/restriction&gt;
     *          &lt;/simpleType&gt;
     *      &lt;/union&gt;
     *  &lt;/simpleType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final AttributeType TIMEUNITTYPE_TYPE = build_TIMEUNITTYPE_TYPE();
     
    private static AttributeType build_TIMEUNITTYPE_TYPE() {
        AttributeType builtType;
        builtType = new AttributeTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","TimeUnitType"), java.lang.Object.class, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYSIMPLETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType final="#all" name="TimeIntervalLengthType"&gt;
     *      &lt;simpleContent&gt;
     *          &lt;extension base="decimal"&gt;
     *              &lt;attribute name="unit" type="gml:TimeUnitType" use="required"/&gt;
     *              &lt;attribute name="radix" type="positiveInteger"/&gt;
     *              &lt;attribute name="factor" type="integer"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/simpleContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType TIMEINTERVALLENGTHTYPE_TYPE = build_TIMEINTERVALLENGTHTYPE_TYPE();
    
    private static ComplexType build_TIMEINTERVALLENGTHTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                TIMEUNITTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","unit"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.POSITIVEINTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","radix"), 0, 1, true, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.INTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","factor"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","TimeIntervalLengthType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.DECIMAL_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="InlinePropertyType"&gt;
     *      &lt;sequence&gt;
     *          &lt;any namespace="##any"/&gt;
     *      &lt;/sequence&gt;
     *      &lt;attributeGroup ref="gml:OwnershipAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType INLINEPROPERTYTYPE_TYPE = build_INLINEPROPERTYTYPE_TYPE();
    
    private static ComplexType build_INLINEPROPERTYTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.BOOLEAN_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","owns"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","InlinePropertyType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="DerivationUnitTermType"&gt;
     *      &lt;complexContent&gt;
     *          &lt;extension base="gml:UnitOfMeasureType"&gt;
     *              &lt;attribute name="exponent" type="integer"/&gt;
     *          &lt;/extension&gt;
     *      &lt;/complexContent&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType DERIVATIONUNITTERMTYPE_TYPE = build_DERIVATIONUNITTERMTYPE_TYPE();
    
    private static ComplexType build_DERIVATIONUNITTERMTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.INTEGER_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","exponent"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","DerivationUnitTermType"), schema, false,
            false, Collections.<Filter>emptyList(), UNITOFMEASURETYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType name="BoundingShapeType"&gt;
     *      &lt;sequence&gt;
     *          &lt;choice&gt;
     *              &lt;element ref="gml:Envelope"/&gt;
     *              &lt;element ref="gml:Null"/&gt;
     *          &lt;/choice&gt;
     *      &lt;/sequence&gt;
     *      &lt;attribute name="nilReason" type="gml:NilReasonType"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType BOUNDINGSHAPETYPE_TYPE = build_BOUNDINGSHAPETYPE_TYPE();
    
    private static ComplexType build_BOUNDINGSHAPETYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                ENVELOPETYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","Envelope"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                NILREASONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","Null"), 1, 1, false, null
            )
        );
        schema.add(
            new AttributeDescriptorImpl(
                NILREASONTYPE_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","nilReason"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","BoundingShapeType"), schema, false,
            false, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }

    /**
     * <p>
     *  <pre>
     *   <code>
     *  &lt;complexType abstract="true" name="AbstractMetadataPropertyType"&gt;
     *      &lt;annotation&gt;
     *          &lt;documentation&gt;To associate metadata described by any XML Schema with a GML object, a property element shall be defined whose content model is derived by extension from gml:AbstractMetadataPropertyType. 
     *  The value of such a property shall be metadata. The content model of such a property type, i.e. the metadata application schema shall be specified by the GML Application Schema.
     *  By default, this abstract property type does not imply any ownership of the metadata. The owns attribute of gml:OwnershipAttributeGroup may be used on a metadata property element instance to assert ownership of the metadata. 
     *  If metadata following the conceptual model of ISO 19115 is to be encoded in a GML document, the corresponding Implementation Specification specified in ISO/TS 19139 shall be used to encode the metadata information.
     *  &lt;/documentation&gt;
     *      &lt;/annotation&gt;
     *      &lt;sequence/&gt;
     *      &lt;attributeGroup ref="gml:OwnershipAttributeGroup"/&gt;
     *  &lt;/complexType&gt;
     *
     *    </code>
     *   </pre>
     * </p>
     *
     * @generated
     */
    public static final ComplexType ABSTRACTMETADATAPROPERTYTYPE_TYPE = build_ABSTRACTMETADATAPROPERTYTYPE_TYPE();
    
    private static ComplexType build_ABSTRACTMETADATAPROPERTYTYPE_TYPE() {
        ComplexType builtType;
        List<PropertyDescriptor> schema = new ArrayList<PropertyDescriptor>();
        schema.add(
            new AttributeDescriptorImpl(
                XSSchema.BOOLEAN_TYPE, new NameImpl("http://www.opengis.net/gml/3.2","owns"), 0, 1, true, null
            )
        );
        builtType = new ComplexTypeImpl(
            new NameImpl("http://www.opengis.net/gml/3.2","AbstractMetadataPropertyType"), schema, false,
            true, Collections.<Filter>emptyList(), XSSchema.ANYTYPE_TYPE, null
        );
        return builtType;
    }


    public GMLSchema() {
        super("http://www.opengis.net/gml/3.2");

        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractCurveSegmentType"),ABSTRACTCURVESEGMENTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CurveSegmentArrayPropertyType"),CURVESEGMENTARRAYPROPERTYTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractMemberType"),ABSTRACTMEMBERTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","SuccessionType"),SUCCESSIONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","UomIdentifier"),UOMIDENTIFIER_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","MeasureType"),MEASURETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","GridLengthType"),GRIDLENGTHTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","SignType"),SIGNTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractSurfacePatchType"),ABSTRACTSURFACEPATCHTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AggregationType"),AGGREGATIONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractParametricCurveSurfaceType"),ABSTRACTPARAMETRICCURVESURFACETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AxisDirection"),AXISDIRECTION_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CalDate"),CALDATE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","NameList"),NAMELIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CodeListType"),CODELISTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractMetaDataType"),ABSTRACTMETADATATYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","GenericMetaDataType"),GENERICMETADATATYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","SpeedType"),SPEEDTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","integerOrNilReason"),INTEGERORNILREASON_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","NilReasonType"),NILREASONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","_Quantity"),_QUANTITY_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","TimeType"),TIMETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","booleanList"),BOOLEANLIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","QNameList"),QNAMELIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","KnotTypesType"),KNOTTYPESTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","_Boolean"),_BOOLEAN_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","SurfacePatchArrayPropertyType"),SURFACEPATCHARRAYPROPERTYTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","doubleList"),DOUBLELIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","NCNameList"),NCNAMELIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","DirectPositionListType"),DIRECTPOSITIONLISTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","ScaleType"),SCALETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AreaType"),AREATYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","NameOrNilReasonList"),NAMEORNILREASONLIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CodeOrNilReasonListType"),CODEORNILREASONLISTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CategoryExtentType"),CATEGORYEXTENTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","_Count"),_COUNT_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","booleanOrNilReasonList"),BOOLEANORNILREASONLIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","UomSymbol"),UOMSYMBOL_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractFeatureMemberType"),ABSTRACTFEATUREMEMBERTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","UnitOfMeasureType"),UNITOFMEASURETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","FormulaType"),FORMULATYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","ConversionToPreferredUnitType"),CONVERSIONTOPREFERREDUNITTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CodeType"),CODETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","_Category"),_CATEGORY_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","DirectPositionType"),DIRECTPOSITIONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","VectorType"),VECTORTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AngleType"),ANGLETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","DirectionVectorType"),DIRECTIONVECTORTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CompassPointEnumeration"),COMPASSPOINTENUMERATION_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","MeasureListType"),MEASURELISTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","DegreeValueType"),DEGREEVALUETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","DegreesType"),DEGREESTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","DecimalMinutesType"),DECIMALMINUTESTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","ArcMinutesType"),ARCMINUTESTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","ArcSecondsType"),ARCSECONDSTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","DMSAngleType"),DMSANGLETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AngleChoiceType"),ANGLECHOICETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CodeWithAuthorityType"),CODEWITHAUTHORITYTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","booleanOrNilReason"),BOOLEANORNILREASON_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","doubleOrNilReason"),DOUBLEORNILREASON_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","NameOrNilReason"),NAMEORNILREASON_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","UomURI"),UOMURI_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CoordinatesType"),COORDINATESTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","EnvelopeType"),ENVELOPETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","TimePositionUnion"),TIMEPOSITIONUNION_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","TimeIndeterminateValueType"),TIMEINDETERMINATEVALUETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","TimePositionType"),TIMEPOSITIONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","EnvelopeWithTimePeriodType"),ENVELOPEWITHTIMEPERIODTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","NilReasonEnumeration"),NILREASONENUMERATION_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","ArrayAssociationType"),ARRAYASSOCIATIONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractRingType"),ABSTRACTRINGTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractRingPropertyType"),ABSTRACTRINGPROPERTYTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","SurfaceInterpolationType"),SURFACEINTERPOLATIONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","PolygonPatchType"),POLYGONPATCHTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","stringOrNilReason"),STRINGORNILREASON_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","LengthType"),LENGTHTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","_SecondDefiningParameter"),_SECONDDEFININGPARAMETER_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","_secondDefiningParameter"),_SECONDDEFININGPARAMETER_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","VolumeType"),VOLUMETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","SequenceRuleEnumeration"),SEQUENCERULEENUMERATION_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","IncrementOrder"),INCREMENTORDER_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AxisDirectionList"),AXISDIRECTIONLIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","SequenceRuleType"),SEQUENCERULETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","integerList"),INTEGERLIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","GridFunctionType"),GRIDFUNCTIONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","doubleOrNilReasonList"),DOUBLEORNILREASONLIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","MeasureOrNilReasonListType"),MEASUREORNILREASONLISTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","QuantityExtentType"),QUANTITYEXTENTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractGeneralParameterValueType"),ABSTRACTGENERALPARAMETERVALUETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractGeneralParameterValuePropertyType"),ABSTRACTGENERALPARAMETERVALUEPROPERTYTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","TriangleType"),TRIANGLETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","integerOrNilReasonList"),INTEGERORNILREASONLIST_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CountExtentType"),COUNTEXTENTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AffinePlacementType"),AFFINEPLACEMENTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","ClothoidType_refLocation"),CLOTHOIDTYPE_REFLOCATION_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","CurveInterpolationType"),CURVEINTERPOLATIONTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","ClothoidType"),CLOTHOIDTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","RectangleType"),RECTANGLETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","GridEnvelopeType"),GRIDENVELOPETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","GridLimitsType"),GRIDLIMITSTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","KnotType"),KNOTTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","KnotPropertyType"),KNOTPROPERTYTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","TimeUnitType"),TIMEUNITTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","TimeIntervalLengthType"),TIMEINTERVALLENGTHTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","InlinePropertyType"),INLINEPROPERTYTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","DerivationUnitTermType"),DERIVATIONUNITTERMTYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","BoundingShapeType"),BOUNDINGSHAPETYPE_TYPE);
        put(new NameImpl("http://www.opengis.net/gml/3.2","AbstractMetadataPropertyType"),ABSTRACTMETADATAPROPERTYTYPE_TYPE);
    }
    
}