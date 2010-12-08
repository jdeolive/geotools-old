package org.geotools.se.v1_1.bindings;

import java.util.List;

import org.geotools.se.v1_1.SE;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:Interpolate.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="Interpolate" substitutionGroup="se:Function" type="se:InterpolateType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *         The transformation of continuous values to a number of values.
 *               &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *  &lt;/xsd:element&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * <pre>
 *       <code>
 *  &lt;xsd:complexType name="InterpolateType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="se:FunctionType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="se:LookupValue"/&gt;
 *                  &lt;xsd:element maxOccurs="unbounded" ref="se:InterpolationPoint"/&gt;
 *              &lt;/xsd:sequence&gt;
 *              &lt;xsd:attribute name="mode" type="se:ModeType"/&gt;
 *              &lt;xsd:attribute name="method" type="se:MethodType"/&gt;
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
public class InterpolateBinding extends AbstractComplexBinding {

    StyleFactory styleFactory;
    FilterFactory filterFactory;
    
    public InterpolateBinding(StyleFactory styleFactory, FilterFactory filterFactory) {
        this.styleFactory = styleFactory;
        this.filterFactory = filterFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.Interpolate;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Class getType() {
        return ColorMap.class;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {

        ColorMap map = styleFactory.createColorMap();
        
        for (ColorMapEntry e : (List<ColorMapEntry>) node.getChildValues(ColorMapEntry.class)) {
            map.addColorMapEntry(e);
        }
        
        return map;
    }

}