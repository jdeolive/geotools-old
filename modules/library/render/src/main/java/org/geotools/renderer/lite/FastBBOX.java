/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.renderer.lite;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.BinarySpatialOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * A fast envelope vs envelope bbox used in rendering operations.
 * To be removed one we have an official concept of "loose bbox" in the API
 * @author aaime
 *
 */
class FastBBOX implements BBOX, BinarySpatialOperator, BinaryComparisonOperator {
    
    String property;
    Envelope envelope; 
    
    public FastBBOX(String propertyName, Envelope env) {
        this.property = propertyName;
        this.envelope = env;
    }

    public double getMaxX() {
        return envelope.getMaxX();
    }

    public double getMaxY() {
        return envelope.getMaxY();
    }

    public double getMinX() {
        return envelope.getMinX();
    }

    public double getMinY() {
        return envelope.getMinY();
    }

    public String getPropertyName() {
        return property;
    }

    public String getSRS() {
        return null;
    }

    public Expression getExpression1() {
        return CommonFactoryFinder.getFilterFactory(null).property(property);
    }

    public Expression getExpression2() {
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(envelope.getMinX(), envelope.getMinY());
        coords[1] = new Coordinate(envelope.getMinX(), envelope.getMaxY());
        coords[2] = new Coordinate(envelope.getMaxX(), envelope.getMaxY());
        coords[3] = new Coordinate(envelope.getMaxX(), envelope.getMinY());
        coords[4] = new Coordinate(envelope.getMinX(), envelope.getMinY());

        LinearRing ring = null;

        GeometryFactory gfac = new GeometryFactory();
        try {
            ring = gfac.createLinearRing(coords);
        } catch (TopologyException tex) {
            throw new IllegalFilterException(tex.toString());
        }

        Polygon polygon = gfac.createPolygon(ring, null);
        if (envelope instanceof ReferencedEnvelope) {
            ReferencedEnvelope refEnv = (ReferencedEnvelope) envelope;
            polygon.setUserData(refEnv.getCoordinateReferenceSystem());
        }
        
        return CommonFactoryFinder.getFilterFactory(null).literal(polygon);
    }

    public Object accept(FilterVisitor visitor, Object context) {
        Object result = visitor.visit(this, context);
        if(!(result instanceof BBOX))
            return result;
        
        BBOX clone = (BBOX) result;
        if(clone.getExpression1().equals(getExpression1()) && clone.getExpression2().equals(getExpression2())) 
            return new FastBBOX(property, envelope);
        
        return result;
    }

    public boolean evaluate(Object feature) {
        SimpleFeature sf = (SimpleFeature) feature;
        if(feature == null)
            return false;
        
        Geometry other = (Geometry) sf.getAttribute(property);
        if(other == null)
            return false;
        
        return other.getEnvelopeInternal().intersects(envelope); 
    }

    public boolean isMatchingCase() {
        return false;
    }

}
