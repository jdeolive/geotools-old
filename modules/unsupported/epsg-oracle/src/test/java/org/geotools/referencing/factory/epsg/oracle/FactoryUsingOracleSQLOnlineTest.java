package org.geotools.referencing.factory.epsg.oracle;

import java.sql.Connection;


import org.geotools.factory.Hints;
import org.geotools.referencing.factory.epsg.OracleDialectEpsgFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.GeodeticDatum;

/**
 * This class tests the Factory<b>Using</b>OracleSQL - ie the thing that does work!
 * <p>
 * No cache or buffer was harmed in the making of these tests.
 *  
 * @author Jody
 */
public class FactoryUsingOracleSQLOnlineTest extends OracleOnlineTestCase {

    public void testDatumCreation() throws Exception {
        Connection connection = datasource.getConnection();
        try{
            Hints hints = new Hints(Hints.EPSG_DATA_SOURCE, "jdbc/EPSG");
        
            OracleDialectEpsgFactory oracle = new OracleDialectEpsgFactory(hints, connection);
    
            GeodeticDatum datum = oracle.createGeodeticDatum("6326");
            assertNotNull(datum);
        }
        finally {
            connection.close();
        }
    }
    
    public void testCRSCreation() throws Exception {
        Connection connection = datasource.getConnection();
        try{
            Hints hints = new Hints(Hints.EPSG_DATA_SOURCE, "jdbc/EPSG");
            OracleDialectEpsgFactory oracle = new OracleDialectEpsgFactory(hints, connection );
            
            CoordinateReferenceSystem crs = oracle.createCoordinateReferenceSystem("4326");
            assertNotNull(crs);
        }
        finally {
            connection.close();
        }
    }

}
