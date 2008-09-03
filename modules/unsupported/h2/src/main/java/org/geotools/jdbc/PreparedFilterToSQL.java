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
package org.geotools.jdbc;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.jdbc.FilterToSQL;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BinarySpatialOperator;

/**
 * Extension of FilterToSQL intended for use with prepared statements.
 * <p>
 * Each time a {@link Literal} is visited, a '?' is encoded, and the 
 * value and type of the literal are stored, available after the fact 
 * via {@link #getLiteralValues()} and {@link #getLiteralTypes()}. 
 * 
 * </p>
 * @author Justin Deoliveira, OpenGEO
 * @author Andrea Aime, OpenGEO
 *
 */
public class PreparedFilterToSQL extends FilterToSQL {
    
    Integer currentSRID;

    /**
     * ordered list of literal values encountered and their types
     */
    List<Object> literalValues = new ArrayList<Object>();
    List<Class> literalTypes = new ArrayList<Class>();
    List<Integer> SRIDs = new ArrayList<Integer>();
    boolean prepareEnabled = true;
    
    public PreparedFilterToSQL() {
        super();
    }

    /**
     * If true (default) a sql statement with literal placemarks is created, otherwise
     * a normal statement is created
     * @return
     */
    public boolean isPrepareEnabled() {
        return prepareEnabled;
    }

    public void setPrepareEnabled(boolean prepareEnabled) {
        this.prepareEnabled = prepareEnabled;
    }

    public PreparedFilterToSQL(Writer out) {
        super(out);
    }

    public Object visit(Literal expression, Object context)
            throws RuntimeException {
        if(!prepareEnabled)
            return super.visit(expression, context);
        
        //evaluate the literal and store it for later
        Object literalValue = evaluateLiteral( expression, (Class) context );
        literalValues.add(literalValue);
        SRIDs.add(currentSRID);
        if(literalValue != null)
            literalTypes.add(literalValue.getClass());
        else if(context instanceof Class)
            literalTypes.add((Class) context);
        
        try {
            out.write( "?" );
        } 
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        
        return context;
    }
    
    public List<Object> getLiteralValues() {
        return literalValues;
    }
    
    public List<Class> getLiteralTypes() {
        return literalTypes;
    }
    
    /**
     * Returns the list of native SRID for each literal that happens to be a geometry, or null otherwise
     * @return
     */
    public List<Integer> getSRIDs() {
        return SRIDs;
    }
    
    @Override
    protected final Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            Object extraData) {
        // basic checks
        if(filter == null)
            throw new NullPointerException("Filter to be encoded cannot be null");
        if(!(filter instanceof BinaryComparisonOperator))
            throw new IllegalArgumentException("This filter is not a binary comparison, " +
                    "can't do SDO relate against it: " + filter.getClass());
        
        // extract the property name and the geometry literal
        PropertyName property;
        Literal geometry;
        BinaryComparisonOperator op = (BinaryComparisonOperator) filter;
        if(op.getExpression1() instanceof PropertyName && op.getExpression2() instanceof Literal) {
            property = (PropertyName) op.getExpression1();
            geometry = (Literal) op.getExpression2();
        } else if(op.getExpression2() instanceof PropertyName && op.getExpression1() instanceof Literal) {
            property = (PropertyName) op.getExpression2();
            geometry = (Literal) op.getExpression1();
        } else {
            throw new IllegalArgumentException("Can only encode spatial filters that do " +
                    "compare a property name and a geometry");
        }
        
        // handle native srid
        currentSRID = null;
        if(featureType != null) {
            AttributeDescriptor descriptor = featureType.getDescriptor(property.getPropertyName());
            if(descriptor instanceof GeometryDescriptor) {
                currentSRID = (Integer) descriptor.getUserData().get(JDBCDataStore.JDBC_NATIVE_SRID);
            }
        }
        
        return visitBinarySpatialOperator(filter, property, geometry, extraData);
    }

    /**
     * Subclasses should override this, the property and the geometry have already been separated out
     * @param filter
     * @param property
     * @param geometry
     * @param extraData
     */
    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter, PropertyName property,
            Literal geometry, Object extraData) {
        return super.visitBinarySpatialOperator(filter, extraData);
    }
}
