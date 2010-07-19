/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gml2;

import java.util.Iterator;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDPackage;
import org.geotools.util.Utilities;

/**
 * Adapter for preventing memory leaks created by substitution group affiliations.
 * <p>
 * When an application schema contains an element in the gml:_Feature substitution group a link 
 * from gml:_Feature back to the app schema element is created. Since the gml schema (and thus the
 * gml:_Feature) element is a singleton this creates a memory leak. This adapter watches the 
 * {@link XSDElementDeclaration#getSubstitutionGroup()} of the gml:_Feature element and prevents it
 * from growing in size by making it a unique list of {@link XSDElementDeclaration} based on 
 * qualified name.
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class SubstitutionGroupLeakPreventer implements Adapter {

    XSDElementDeclaration target;
    
    public Notifier getTarget() {
        return target;
    }
    
    public void setTarget(Notifier newTarget) {
        target = (XSDElementDeclaration) newTarget;
    }

    public boolean isAdapterForType(Object type) {
        return type instanceof XSDElementDeclaration;
    }

    public void notifyChanged(Notification notification) {
        int featureId = notification.getFeatureID(target.getClass());
        if (featureId != XSDPackage.XSD_ELEMENT_DECLARATION__SUBSTITUTION_GROUP) {
            return;
        }
           
        if (notification.getEventType() != Notification.ADD) {
            return;
        }
        if (!(notification.getNewValue() instanceof XSDElementDeclaration)) {
                return;
        }
            
        XSDElementDeclaration el = (XSDElementDeclaration) notification.getNewValue();
        XSDElementDeclaration e = target;
            
        while(e != null) {
            synchronized(e) {
                //TODO: iterate in reverse order to keep the last one added
                Iterator<XSDElementDeclaration> i = e.getSubstitutionGroup().iterator();

                boolean exists = false;
                while(i.hasNext()) {
                    XSDElementDeclaration se = i.next();
                    if (Utilities.equals(el.getTargetNamespace(), se.getTargetNamespace()) &&  
                        Utilities.equals(el.getName(), se.getName())) {
                        
                        if (!exists) {
                            exists = true;
                        }
                        else {
                            i.remove();
                            
                            if (target.equals(el.getSubstitutionGroupAffiliation())) {
                                XSDElementDeclaration clone = (XSDElementDeclaration) 
                                    target.cloneConcreteComponent(false, false);
                                clone.setTargetNamespace(GML.NAMESPACE);
                                
                                el.setSubstitutionGroupAffiliation(clone);
                            }
                            
                            //break?
                        }
                    }
                }
            }
            e = e.getSubstitutionGroupAffiliation();
        }
        
    }


}
