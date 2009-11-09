package org.geotools.gml4wcs.bindings;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.Gml4wcsFactory;
import net.opengis.gml.GridEnvelopeType;
import net.opengis.gml.GridLimitsType;

import org.geotools.gml4wcs.GML;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Binding object for the type http://www.opengis.net/gml:GridLimitsType.
 * 
 * <p>
 * 
 * <pre>
 *	 <code>
 *  &lt;complexType name=&quot;GridLimitsType&quot;&gt;
 *      &lt;sequence&gt;
 *          &lt;element name=&quot;GridEnvelope&quot; type=&quot;gml:GridEnvelopeType&quot;/&gt;
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
public class GridLimitsTypeBinding extends AbstractComplexBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return GML.GridLimitsType;
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
        Envelope envelope = (Envelope) node.getChildValue("GridEnvelope");
        
        return envelope;
    }

}