package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDTextSymbolizerBinding;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:TextSymbolizer.
 * 
 * <p>
 * 
 * <pre>
 *  <code>
 *  &lt;xsd:element name="TextSymbolizer" substitutionGroup="se:Symbolizer" type="se:TextSymbolizerType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          A "TextSymbolizer" is used to render text labels according to
 *          various graphical parameters.
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
public class TextSymbolizerBinding extends SLDTextSymbolizerBinding {

    public TextSymbolizerBinding(StyleFactory styleFactory) {
        super(styleFactory);
    }
    
    @Override
    public int getExecutionMode() {
        return BEFORE;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SE.TextSymbolizer;
    }

}