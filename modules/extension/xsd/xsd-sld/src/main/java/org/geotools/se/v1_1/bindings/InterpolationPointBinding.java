package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:InterpolationPoint.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="InterpolationPoint"
 *      substitutionGroup="ogc:expression" type="se:InterpolationPointType"/&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * <pre>
 *       <code>
 *  &lt;xsd:complexType name="InterpolationPointType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:ExpressionType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="se:Data"/&gt;
 *                  &lt;xsd:element ref="se:Value"/&gt;
 *              &lt;/xsd:sequence&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt; 
 *              
 *        </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class InterpolationPointBinding extends AbstractComplexBinding {

    StyleFactory styleFactory;
    FilterFactory filterFactory;
    
    public InterpolationPointBinding(StyleFactory styleFactory, FilterFactory filterFactory) {
        this.styleFactory = styleFactory;
        this.filterFactory = filterFactory;
    }
    
    /**
     * @generated
     */
    public QName getTarget() {
        return SE.InterpolationPoint;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Class getType() {
        return ColorMapEntry.class;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {

        ColorMapEntry entry = styleFactory.createColorMapEntry();

        //&lt;xsd:element ref="se:Data"/&gt;
        entry.setQuantity(filterFactory.literal(node.getChildValue("Data")));
        
        //&lt;xsd:element ref="se:Value"/&gt;
        entry.setColor((Expression) node.getChildValue("Value"));
        
        return entry;
    }

}