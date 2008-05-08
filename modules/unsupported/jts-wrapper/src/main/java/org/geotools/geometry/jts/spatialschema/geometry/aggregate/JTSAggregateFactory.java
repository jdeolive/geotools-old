package org.geotools.geometry.jts.spatialschema.geometry.aggregate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.geotools.factory.Factory;
import org.geotools.factory.Hints;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.aggregate.AggregateFactory;
import org.opengis.geometry.aggregate.MultiCurve;
import org.opengis.geometry.aggregate.MultiPoint;
import org.opengis.geometry.aggregate.MultiPrimitive;
import org.opengis.geometry.aggregate.MultiSurface;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Implementation of AggregateFactory able to make MultiPointImpl but
 * little else.
 * 
 * @author Jody Garnett
 */
public class JTSAggregateFactory implements Factory,  AggregateFactory {
    private CoordinateReferenceSystem crs;
    private final Map usedHints = new LinkedHashMap();
    /**
     * No argument constructor for FactorySPI
     */
    public JTSAggregateFactory(){
        this( DefaultGeographicCRS.WGS84);
    }
    /**
     * Hints constructor for FactoryRegistry
     */
    public JTSAggregateFactory( Hints hints ){
        this( (CoordinateReferenceSystem) hints.get( Hints.CRS ) );
    }
    /**
     * Direct constructor for test cases
     */
    public JTSAggregateFactory( CoordinateReferenceSystem crs ) {
        this.crs = crs;
        usedHints.put( Hints.CRS, crs );
    }
    public Map getImplementationHints() {
        return usedHints;
    }
    public MultiCurve createMultiCurve( Set arg0 ) {
        throw new UnsupportedOperationException("MultiCurve not implemented");
    }
    public MultiPoint createMultiPoint( Set arg0 ) {
        return new MultiPointImpl( crs );
    }
    public MultiPrimitive createMultiPrimitive( Set arg0 ) {
        throw new UnsupportedOperationException("MultiPrimitive not implemented");
    }
    public MultiSurface createMultiSurface( Set arg0 ) {
        throw new UnsupportedOperationException("MultiSurface not implemented");
    }
}
