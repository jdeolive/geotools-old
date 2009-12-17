package org.geotools.gml4wcs.bindings;

import javax.xml.namespace.QName;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.gml4wcs.GML;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
        return GeneralEnvelope.class;
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

            GeneralEnvelope envelope = new GeneralEnvelope(l.length);
            if (l.length == 2)
                envelope.setEnvelope(l[0], l[1], h[0], h[1]);
            else if (l.length == 3)
                envelope.setEnvelope(l[0], l[1], l[2], h[0], h[1], h[2]);
            return envelope;
        }

        return null;
    }

    public Element encode(Object object, Document document, Element value)
            throws Exception {
        GeneralEnvelope envelope = (GeneralEnvelope) object;

        if (envelope.isNull()) {
            value.appendChild(document.createElementNS(GML.NAMESPACE, org.geotools.gml3.GML.Null.getLocalPart()));
        }

        return null;
    }

    public Object getProperty(Object object, QName name) {
        GeneralEnvelope envelope = (GeneralEnvelope) object;

        if (envelope.isNull()) {
            return null;
        }

        if (name.getLocalPart().equals("low")) {
            return envelope.getLowerCorner().getCoordinate();
        }

        if (name.getLocalPart().equals("high")) {
            return envelope.getUpperCorner().getCoordinate();
        }

        return null;
    }
}