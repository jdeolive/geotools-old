package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDFillBinding;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;
import org.opengis.filter.FilterFactory;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:Fill.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="Fill" type="se:FillType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          A "Fill" specifies the pattern for filling an area geometry.
 *          The allowed SvgParameters are: "fill" (color) and "fill-opacity".
 *        &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *  &lt;/xsd:element&gt; 
 * 	
 *   </code>
 * </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class FillBinding extends SLDFillBinding {

    public FillBinding(StyleFactory styleFactory, FilterFactory filterFactory) {
        super(styleFactory, filterFactory);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.Fill;
    }

}