package org.geotools.xml;

import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.EFactory;
import org.geotools.xml.impl.InstanceBinding;

/**
 * A binding implementation which parses / encodes objects from an EMF model.
 * <p>
 * This binding implementation uses EMF reflection to implement all methods of 
 * the api. All that is needed is the 'target' of the binding. 
 * </p>
 * <p>
 * These bindings are "instance" bindings in that they are instantiated before
 * the parser is run (see {@link Configuration#registerBindings(java.util.Map)}) 
 * and not at runtime.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 * @see Configuration#registerBindings(java.util.Map)
 */
public class ComplexEMFBinding extends AbstractComplexEMFBinding
    implements InstanceBinding {

    /**
     * The name of the element or type of the binding.
     */
    QName target;
    
    /**
     * Creates the binding.
     * 
     * @param factory The factory for the emf model.
         * @param target The qualified name of the type in the emf model that this
         * binding works against. 
     */
    public ComplexEMFBinding( EFactory factory, QName target ) {
        super( factory );
        this.target = target;
    }
    
    public QName getTarget() {
        return target;
    }

}
