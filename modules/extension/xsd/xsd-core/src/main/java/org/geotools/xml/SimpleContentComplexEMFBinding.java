/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xml;

import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;

/**
 * A binding implementation which handles the case of a complex type with 
 * simple content.
 * <p>
 * Model objects typically have a single "value" getter/setter. This binding
 * calls that method reflectively on the class created by the super type. 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @see ComplexEMFBinding
 */
public class SimpleContentComplexEMFBinding extends ComplexEMFBinding {

    public SimpleContentComplexEMFBinding(EFactory factory, QName target) {
        super(factory, target);
    }
    
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {
    
        EObject object = createEObject(value);
        if ( object != null ) {
            EMFUtils.set( object, "value", value );
            return object;
        }
        
        return value;
    }

}
