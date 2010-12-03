package org.geotools.se.v1_1.bindings;

import javax.xml.namespace.QName;

import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDLegendGraphicBinding;

/**
 * Binding object for the element http://www.opengis.net/se:LegendGraphic.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="LegendGraphic" type="se:LegendGraphicType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class LegendGraphicBinding extends SLDLegendGraphicBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return SE.LegendGraphic;
	}
}