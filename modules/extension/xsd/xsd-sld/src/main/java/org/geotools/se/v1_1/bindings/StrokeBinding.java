package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDStrokeBinding;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:Stroke.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="Stroke" type="se:StrokeType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          A "Stroke" specifies the appearance of a linear geometry.  It is
 *          defined in parallel with SVG strokes.  The following SvgParameters
 *          may be used: "stroke" (color), "stroke-opacity", "stroke-width",
 *          "stroke-linejoin", "stroke-linecap", "stroke-dasharray", and
 *          "stroke-dashoffset".  Others are not officially supported.
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
public class StrokeBinding extends SLDStrokeBinding {

    public StrokeBinding(StyleFactory styleFactory) {
        super(styleFactory);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.Stroke;
    }
}