/* $Id: SQLEncoderOracleTest.java,v 1.5 2003/11/26 22:57:45 seangeo Exp $
 *
 * Created on 31/07/2003
 */
package org.geotools.filter;

import java.util.HashMap;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * DOCUMENT ME!
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: SQLEncoderOracleTest.java,v 1.5 2003/11/26 22:57:45 seangeo Exp $ Last Modified: $Date: 2003/11/26 22:57:45 $
 */
public class SQLEncoderOracleTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private SQLEncoder encoder;

    /**
     * Constructor for SQLEncoderOracleTest.
     *
     * @param arg0
     */
    public SQLEncoderOracleTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGeometryFilterEncoder() throws Exception {
        encoder = new SQLEncoderOracle("FID",new HashMap());

        GeometryFilter filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        filter.addLeftGeometry(filterFactory.createAttributeExpression(null, "GEOM"));
        filter.addRightGeometry(filterFactory.createBBoxExpression(
                new Envelope(-180.0, 180.0, -90.0, 90.0)));
        String value = encoder.encode(filter);
        assertEquals("WHERE SDO_GEOM.RELATE(\"GEOM\",'disjoint'," +
            "MDSYS.SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1)," +
            "MDSYS.SDO_ORDINATE_ARRAY(-180.0,-90.0,-180.0,90.0,180.0,90.0,180.0,-90.0,-180.0,-90.0)),0.001)" +
            " = 'FALSE' ", value);

        filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_CONTAINS);
        filter.addLeftGeometry(filterFactory.createAttributeExpression(null, "SHAPE"));
        filter.addRightGeometry(filterFactory.createLiteralExpression(geometryFactory.createPoint(
                    new Coordinate(10.0, -10.0))));
        value = encoder.encode(filter);
        LOGGER.fine(value);
        assertEquals("WHERE SDO_RELATE(\"SHAPE\",MDSYS.SDO_GEOMETRY(0001,NULL," +
            "MDSYS.SDO_POINT_TYPE(10.0,-10.0,NULL),NULL,NULL),'mask=contains querytype=WINDOW') = 'TRUE' ",
            value);

        filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_CROSSES);
        filter.addLeftGeometry(filterFactory.createAttributeExpression(null, "GEOM"));
        filter.addRightGeometry(filterFactory.createLiteralExpression(
                geometryFactory.createLineString(
                    new Coordinate[] { new Coordinate(-10.0d, -10.0d), new Coordinate(10d, 10d) })));
        value = encoder.encode(filter);
        assertEquals("WHERE SDO_RELATE(\"GEOM\",MDSYS.SDO_GEOMETRY(1002,NULL," +
            "NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(" +
            "-10.0,-10.0,10.0,10.0)),'mask=overlapbydisjoint querytype=WINDOW') = 'TRUE' ", value);
    }
    
    public void testFIDEncoding() throws Exception {
        encoder = new SQLEncoderOracle("FID",new HashMap());
        
        Filter filter = filterFactory.createFidFilter("FID.1");
        String value = encoder.encode(filter);
        assertEquals("WHERE FID = '1'",value);
        
        FidFilter fidFilter = filterFactory.createFidFilter();
        fidFilter.addFid("FID.1");
        fidFilter.addFid("FID.3");
        value = encoder.encode(fidFilter);
        assertEquals("WHERE FID = '3' OR FID = '1'",value);
    }
}
