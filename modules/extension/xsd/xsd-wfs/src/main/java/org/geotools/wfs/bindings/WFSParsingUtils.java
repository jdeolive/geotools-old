/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.wfs.bindings;

import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.EObject;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.geotools.wfs.CompositeFeatureCollection;
import org.geotools.xml.EMFUtils;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.opengis.feature.simple.SimpleFeature;

public class WFSParsingUtils {

    public static EObject FeatureCollectionType_parse(EObject fct, ElementInstance instance, Node node) {
        
        SimpleFeatureCollection fc = null;
        
        //gml:featureMembers
        fc = (SimpleFeatureCollection) node.getChildValue(FeatureCollection.class);
        if (fc == null) {
            fc = new DefaultFeatureCollection(null, null);
        }
        
        //check for an array
        SimpleFeature[] featureMembers = (SimpleFeature[]) node.getChildValue(SimpleFeature[].class);
        if (featureMembers != null) {
            for (int i = 0; i < featureMembers.length; i++) {
                fc.add(featureMembers[i]);
            }
        }
        else {
            //gml:featureMember
            List<SimpleFeature> featureMember = node.getChildValues( SimpleFeature.class );
            for (SimpleFeature f : featureMember ) {
                fc.add( f );
            }
        }
        
        if ( !fc.isEmpty() ) {
            if (EMFUtils.has(fct, "feature")) {
                //wfs 1.0, 1.1
                EMFUtils.add(fct, "feature", fc);
            }
            else {
                //wfs 2.0
                EMFUtils.add(fct, "member", fc);
            }
        }
        
        return fct;
    }
    
    public static Object FeatureCollectionType_getProperty(EObject fc, QName name) {
        List<FeatureCollection> features = features(fc);
        FeatureCollection first = features.get( 0 );
        
        if( "boundedBy".equals( name.getLocalPart() ) ) {
            if ( features.size() == 1 ) {
                return first.getBounds();
            }
            else {
                //aggregate
                ReferencedEnvelope bounds = new ReferencedEnvelope(first.getBounds());
                for ( int i = 1; i < features.size(); i++ ) {
                    bounds.expandToInclude( features.get( i ).getBounds() );
                }
                return bounds;
            }
            
        }
        
        if ( "featureMember".equals( name.getLocalPart() ) || "member".equals(name.getLocalPart())) {
            if (features.size() > 1) {
                //wrap in a single
                return new CompositeFeatureCollection(features);
            }

            //just return the single
            return first;
        }
        
        return null;
    }
    
    public static List<FeatureCollection> features(EObject obj) {
        return (List) (EMFUtils.has(obj, "feature") 
            ? EMFUtils.get(obj, "feature") : EMFUtils.get(obj, "member"));
    }
}
