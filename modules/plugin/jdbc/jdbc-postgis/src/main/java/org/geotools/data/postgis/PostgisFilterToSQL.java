/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.postgis;

import java.io.IOException;

import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.filter.FilterCapabilities;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BinarySpatialOperator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

public class PostgisFilterToSQL extends FilterToSQL {

    FilterToSqlHelper helper;

    Integer currentSRID;

    public PostgisFilterToSQL(PostGISDialect dialect) {
        helper = new FilterToSqlHelper(this);
    }

    public boolean isLooseBBOXEnabled() {
        return helper.looseBBOXEnabled;
    }

    public void setLooseBBOXEnabled(boolean looseBBOXEnabled) {
        helper.looseBBOXEnabled = looseBBOXEnabled;
    }

    @Override
    protected void visitLiteralGeometry(Literal expression) throws IOException {
        // evaluate the literal and store it for later
        Geometry geom  = (Geometry) evaluateLiteral(expression, Geometry.class);
        
        if ( geom instanceof LinearRing ) {
            //postgis does not handle linear rings, convert to just a line string
            geom = geom.getFactory().createLineString(((LinearRing) geom).getCoordinateSequence());
        }
        
        out.write("GeomFromText('");
        out.write(geom.toText());
        out.write("', " + currentSRID + ")");
    }

    @Override
    protected FilterCapabilities createFilterCapabilities() {
        return helper.createFilterCapabilities();
    }

    @Override
    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            Object extraData) {
        // basic checks
        if (filter == null)
            throw new NullPointerException(
                    "Filter to be encoded cannot be null");
        if (!(filter instanceof BinaryComparisonOperator))
            throw new IllegalArgumentException(
                    "This filter is not a binary comparison, "
                            + "can't do SDO relate against it: "
                            + filter.getClass());

        // extract the property name and the geometry literal
        PropertyName property;
        Literal geometry;
        BinaryComparisonOperator op = (BinaryComparisonOperator) filter;
        if (op.getExpression1() instanceof PropertyName
                && op.getExpression2() instanceof Literal) {
            property = (PropertyName) op.getExpression1();
            geometry = (Literal) op.getExpression2();
        } else if (op.getExpression2() instanceof PropertyName
                && op.getExpression1() instanceof Literal) {
            property = (PropertyName) op.getExpression2();
            geometry = (Literal) op.getExpression1();
        } else {
            throw new IllegalArgumentException(
                    "Can only encode spatial filters that do "
                            + "compare a property name and a geometry");
        }

        // handle native srid
        currentSRID = null;
        if (featureType != null) {
            // going thru evaluate ensures we get the proper result even if the
            // name has
            // not been specified (convention -> the default geometry)
            AttributeDescriptor descriptor = (AttributeDescriptor) property
                    .evaluate(featureType);
            if (descriptor instanceof GeometryDescriptor) {
                currentSRID = (Integer) descriptor.getUserData().get(
                        JDBCDataStore.JDBC_NATIVE_SRID);
            }
        }

        return visitBinarySpatialOperator(filter, property, geometry, filter
                .getExpression1() instanceof Literal, extraData);
    }

    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            PropertyName property, Literal geometry, boolean swapped,
            Object extraData) {
        helper.out = out;
        return helper.visitBinarySpatialOperator(filter, property, geometry,
                swapped, extraData);
    }

}
