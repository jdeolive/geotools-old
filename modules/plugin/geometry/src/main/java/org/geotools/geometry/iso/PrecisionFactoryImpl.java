package org.geotools.geometry.iso;

import org.opengis.geometry.Precision;
import org.opengis.geometry.PrecisionFactory;
import org.opengis.geometry.PrecisionType;

/**
 * Implementation set up to create PrecisionModel
 */
public class PrecisionFactoryImpl implements PrecisionFactory {

    public Precision createFixedPrecision( PrecisionType code, double scale ) {
        if( code == PrecisionType.FLOAT){
            return new PrecisionModel( scale  );    
        }
        return new PrecisionModel( code );
    }

}
