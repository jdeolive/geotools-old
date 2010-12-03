package org.geotools.se.v1_1.bindings;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDGraphicFillBinding;
import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/se:GraphicFill.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="GraphicFill" type="se:GraphicFillType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          A "GraphicFill" defines repeated-graphic filling (stippling)
 *          pattern for an area geometry.
 *        &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *  &lt;/xsd:element&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class GraphicFillBinding extends SLDGraphicFillBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return SE.GraphicFill;
	}
}