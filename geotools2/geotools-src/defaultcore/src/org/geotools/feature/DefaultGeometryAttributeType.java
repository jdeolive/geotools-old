/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.feature;

import org.geotools.cs.AxisInfo;
import org.geotools.cs.AxisOrientation;
import org.geotools.cs.CoordinateSystem;
import org.geotools.units.Unit;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Simple extension of AttributeType to allow for CoordinateSystem use.
 * <p>
 * I am limiting this class to only work with CoordinateSystem even though
 * the interface supports any CS_CoordinateSystem. Life is too short.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class DefaultGeometryAttributeType extends DefaultAttributeType implements GeometryAttributeType {
    
    /** Used as a placeholder for the default Coordinate System */
    public static final CoordinateSystem DEFAULT_CS = new DefaultCoordinateSystem();
             
    /** CoordianteSystem used by this GeometryAttributeType */
    protected CoordinateSystem coordinateSystem;
    
    /**
     * 
     * @param name
     * @param type Restricted to instance of instanceof Geometry
     * @param nillable
     * @param fieldLength
     * @param defaultValue
     * @param cs
     */
    public DefaultGeometryAttributeType(String name, Class type, boolean nillable, int fieldLength, Object defaultValue, CoordinateSystem cs) {
        super(name, type, nillable, fieldLength, defaultValue);
        coordinateSystem = cs != null ? cs : DEFAULT_CS;        
    }
    public DefaultGeometryAttributeType(GeometryAttributeType copy, CoordinateSystem override) {
        super( copy );
        coordinateSystem = (CoordinateSystem) copy.getCoordinateSystem();
        if( override != null){
            coordinateSystem = override;                      
        }
        if( coordinateSystem == null){
            coordinateSystem = DEFAULT_CS;            
        }        
    }
    public Object getCoordinateSystem() {
        return coordinateSystem;
    }
    public GeometryFactory getGeometryFactory() {
        return coordinateSystem.getGeometryFactory();
    }    
}
/**
 * DefaultCoordinateSystem used by DefaultGeometryAttributeType.
 * 
 * @author Jody Garnett, Refractions Research, Inc
 */
class DefaultCoordinateSystem extends CoordinateSystem {
    static final AxisInfo X = new AxisInfo("X",AxisOrientation.EAST );
    static final AxisInfo Y = new AxisInfo("Y",AxisOrientation.NORTH );
    static final AxisInfo Z = new AxisInfo("Z",AxisOrientation.UP );            
        
    public DefaultCoordinateSystem(){
        super( "Default" );
    }
            
    protected GeometryFactory createGeometryFactory() {
        PrecisionModel pm = new PrecisionModel();
        GeometryFactory factory = new GeometryFactory( pm, 0 );
        return factory;
    }
    public int getDimension() {
        return 3;
    }

    public AxisInfo getAxis(int dimension) {
        switch( dimension ){
        case 0: return X;
        case 1: return Y;
        case 2: return Z;
        default:
            return null; 
        }
    }

    public Unit getUnits(int dimension) {
        return Unit.DIMENSIONLESS;
    }
        
};
