/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;



/**
 * This is a support class which creates test features for use in testing.
 *
 * @author jamesm
 */
public class LockingDataSourceFixture {
    protected PrecisionModel precisionModel;
    protected FeatureType featureType;
    // private Feature testFeature = null;

    /**
     * Creates a new instance of SampleFeatureFixtures
     */
    public LockingDataSourceFixture() throws Exception{
        precisionModel = new PrecisionModel();
        featureType = createTestType();
    }
        
    public Feature createFeature( String name, int x, int y ) {
        Object[] attributes = createAttributes( name, x, y );
        try{
            return featureType.create(attributes);
        } catch (Exception e) {
            Error ae = new AssertionError(
                    "Sample Feature for tests has been misscoded");
            ae.initCause(e);
            throw ae;
        }
    }

    /** generated fid */
    int fid=0;
    /**
     * Creates an attributes.
     * 
     * Where:
     * <ul>
     * <li>0: generated integer representing fid</li>
     * <li>1: generated Point(x,y)</li>
     * <li>2: provided name</li> 
     * </ul>
     * @return
     */
    public Object[] createAttributes( String name, int x, int y ) {
        Object[] attributes = new Object[3];
        
        attributes[0] = new Integer(fid++);
        attributes[1] = new Point(new Coordinate(x, y), precisionModel, 1);
        attributes[2] = name;

        return attributes;
    }

    /**
     * Creates FeatureType 'test'.
     * Where:
     * <ul>
     * <li>fid: integer</li>
     * <li>geometry: Point</li>
     * <li>name: String</li> 
     * </ul>
     *
     *
     * @throws SchemaException
     */
    private static FeatureType createTestType() throws SchemaException {
      FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance("test");
      
      typeFactory.addType(AttributeTypeFactory.newAttributeType("fid",
                Integer.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("geometry",
                Point.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("name",
                String.class));
      typeFactory.setDefaultGeometry( (GeometryAttributeType) typeFactory.get(1) );
      
      return typeFactory.getFeatureType();    
    }
    
    
}
