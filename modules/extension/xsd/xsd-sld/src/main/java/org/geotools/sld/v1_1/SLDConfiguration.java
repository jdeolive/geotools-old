package org.geotools.sld.v1_1;

import org.geotools.se.v1_1.SEConfiguration;
import org.geotools.sld.v1_1.bindings.NamedLayerBinding;
import org.geotools.sld.v1_1.bindings.NamedStyleBinding;
import org.geotools.sld.v1_1.bindings.RemoteOWSBinding;
import org.geotools.sld.v1_1.bindings.StyledLayerDescriptorBinding;
import org.geotools.sld.v1_1.bindings.UserLayerBinding;
import org.geotools.sld.v1_1.bindings.UserStyleBinding;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

/**
 * Parser configuration for the http://www.opengis.net/sld schema.
 *
 * @generated
 */
public class SLDConfiguration extends Configuration {

    /**
     * Creates a new configuration.
     * 
     * @generated
     */     
    public SLDConfiguration() {
       super(SLD.getInstance());
       
       addDependency(new SEConfiguration());
    }
    
    /**
     * Registers the bindings for the configuration.
     *
     * @generated
     */
    protected final void registerBindings( MutablePicoContainer container ) {
        //Elements
        container.registerComponentImplementation(SLD.NamedLayer,NamedLayerBinding.class);
        container.registerComponentImplementation(SLD.NamedStyle,NamedStyleBinding.class);
        container.registerComponentImplementation(SLD.RemoteOWS,RemoteOWSBinding.class);
        container.registerComponentImplementation(SLD.StyledLayerDescriptor,StyledLayerDescriptorBinding.class);
        container.registerComponentImplementation(SLD.UserLayer,UserLayerBinding.class);
        container.registerComponentImplementation(SLD.UserStyle,UserStyleBinding.class);
    }
} 