package org.geotools.gml4wcs.bindings;

import javax.xml.namespace.QName;

import org.geotools.gml4wcs.GML;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Binding object for the type http://www.opengis.net/gml:GridEnvelopeType.
 * 
 * <p>
 * 
 * <pre>
 *	 <code>
 *  &lt;complexType name=&quot;GridEnvelopeType&quot;&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Provides grid coordinate values for the diametrically opposed corners of an envelope that bounds a section of grid. The value of a single coordinate is the number of offsets from the origin of the grid in the direction of a specific axis.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element name=&quot;low&quot; type=&quot;gml:integerList&quot;/&gt;
 *          &lt;element name=&quot;high&quot; type=&quot;gml:integerList&quot;/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt; 
 * 	
 * </code>
 *	 </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class GridEnvelopeTypeBinding extends AbstractComplexBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return GML.GridEnvelopeType;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Class getType() {
        return Envelope.class;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
            throws Exception {
        if (node.getChild("low") != null) {
            int[] l = (int[]) node.getChildValue("low");
            int[] h = (int[]) node.getChildValue("high");

            return new Envelope(l[0], h[0], l[1], h[1]);
        }

        return null;
    }

    public Element encode(Object object, Document document, Element value)
            throws Exception {
        Envelope envelope = (Envelope) object;

        if (envelope.isNull()) {
            value.appendChild(document.createElementNS(GML.NAMESPACE, org.geotools.gml3.GML.Null.getLocalPart()));
        }

        return null;
    }

    public Object getProperty(Object object, QName name) {
        Envelope envelope = (Envelope) object;

        if (envelope.isNull()) {
            return null;
        }

        if (name.getLocalPart().equals("low")) {
            return new int[] {(int) envelope.getMinX(), (int) envelope.getMinY()};
        }

        if (name.getLocalPart().equals("high")) {
            return new int[] {(int) envelope.getMaxX(), (int) envelope.getMaxY()};
        }

        return null;
    }
}