package org.geotools.referencing.factory.epsg.oracle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.geotools.factory.GeoTools;
import org.geotools.referencing.CRS;
import org.geotools.referencing.factory.epsg.ThreadedOracleEpsgFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.GeodeticDatum;

/**
 * This one tests Factory<b>On</b>OracleSQL - ie it has a buffer and delegates to a
 * OracleDialectEpsgFactory when the buffer needs to be fed.
 * 
 * @author Jody
 */
public class ThreadedOracleEpsgAuthorityOnlineTest extends OracleOnlineTestCase {

    public void testWSG84() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        assertNotNull(crs);
    }
    public void testJNDIConfiguredProperlyForTest() throws Exception {
        InitialContext context = GeoTools.getInitialContext(null);
        String name = "jdbc/EPSG";
        //name = GeoTools.fixName(context,"jdbc/EPSG");        
        DataSource source = (DataSource) context.lookup( name);
        assertNotNull(source);
        assertSame(source, this.datasource);
    }
    /**
     * It is a little hard to test this thing, the DefaultAuthorityFactory holds a field "buffered"
     * that is an AbstractAuthorityFactory which in turn is an FactoryUsing
     * 
     * @throws Exception
     */
    public void testCRSCreation() throws Exception {
        ThreadedOracleEpsgFactory oracle = new ThreadedOracleEpsgFactory();

        
        CoordinateReferenceSystem crs = oracle.createCoordinateReferenceSystem("4326");
        assertNotNull(crs);
    }
    
    public void testDatumCreation() throws Exception {
        ThreadedOracleEpsgFactory oracle = new ThreadedOracleEpsgFactory();
                
        GeodeticDatum datum = oracle.createGeodeticDatum("6326");
        assertNotNull( datum );
    }
}
