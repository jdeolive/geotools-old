/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.filter.v1_1.OGC;
import org.geotools.xml.Node;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.BinarySpatialOperator;

/**
 * Convenience class for filter parsing. 
 * <p>
 * The primary function of this class is to share code among the different versions 
 * of filter parsing.
 * </p>
 * @author Justin Deoliveira, OpenGEO
 *
 *
 *
 * @source $URL$
 */
public class FilterParsingUtils {

    public static Object Filter_getProperty(Object object, QName qName ) {
        Filter filter = (Filter) object;

        //use the local name to handle oth the OGC and FES namespaces
        String name = qName.getLocalPart();
        
        //&lt;xsd:element ref="ogc:spatialOps"/&gt;
        if ("spatialOps".equals(name) && filter instanceof BinarySpatialOperator) {
            return filter;
        }

        //&lt;xsd:element ref="ogc:comparisonOps"/&gt;
        if ("comparisonOps".equals(name)) {
            //JD: extra check here because many of our spatial implementations
            // extend both      
            if ( filter instanceof BinaryComparisonOperator
                && !(filter instanceof BinarySpatialOperator)) {
                return filter;    
            }
            else {
                //filters that don't extend BinaryComparisonOperator but are still 
                // comparisonOps
                if ( filter instanceof PropertyIsLike || filter instanceof PropertyIsNull 
                    || filter instanceof PropertyIsBetween ) {
                    return filter;
                }
            }
        }

        //&lt;xsd:element ref="ogc:logicOps"/&gt;
        if ("logicOps".equals(name) && filter instanceof BinaryLogicOperator) {
            return filter;
        }

        //&lt;xsd:element maxOccurs="unbounded" ref="ogc:_Id"/&gt;
        if ( filter instanceof Id && 
            ( "_Id".equals(name) /*1.1/2.0*/ || "FeatureId".equals(name) /*1.0*/ ) ) {
            //unwrap
            Id id = (Id) filter;

            return id.getIdentifiers();
        }

        return null;
    }
    
    public static List<Filter> BinaryLogicOperator_getChildFilters( Node node, org.opengis.filter.FilterFactory factory ) {
        List<Filter> filters = node.getChildValues(Filter.class);
        if ( filters.size() < 2 ) {
            //look for Id elements and turn them into fid filters
            //note: this is not spec compliant and is a bit lax
            List<Identifier> ids = node.getChildValues( Identifier.class );
            for ( Identifier id : ids ) {
                filters.add( factory.id( Collections.singleton( id ) ) ) ;
            }
        }
        
        return filters;
    }
}
