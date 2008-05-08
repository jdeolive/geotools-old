package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.geotools.wfs.protocol.ConnectionFactory;
import org.geotools.wfs.protocol.DefaultConnectionFactory;

public abstract class DataTestSupport extends TestCase {

    /**
     * Location of a test data capabilities from geoserver
     */
    public static final String GEOS_CAPABILITIES = "geoserver/geoserver_capabilities_1_1_0.xml";

    /**
     * Location of a test DescribeFeatureType response from a geoserver
     * {@code topp:states} FeatureType
     */
    protected static final String GEOS_STATES_SCHEMA = "geoserver/DescribeFeatureType_States.xsd";

    /**
     * Type name for the sample geoserver states featuretype
     */
    public static final QName GEOS_STATES_TYPENAME = new QName("http://www.openplans.org/topp",
            "states");

    /**
     * Prefixed type name for the sample geoserver states featuretype as used in
     * the capabilities document (and thus as presented by the
     * WFSDataStore.getTypeNames() method)
     */
    public static final String GEOS_STATES_FEATURETYPENAME = "topp:states";

    /**
     * Type name for the sample geoserver archsites featuretype
     */
    public static final QName GEOS_ARCHSITES_TYPENAME = new QName(
            "http://www.openplans.org/spearfish", "archsites");

    /**
     * Prefixed type name for the sample geoserver archsites featuretype as used
     * in the capabilities document (and thus as presented by the
     * WFSDataStore.getTypeNames() method)
     */
    public static final String GEOS_ARCHSITES_FEATURETYPENAME = "sf:archsites";

    /**
     * The CRS id for the geoserver archsites test data
     */
    public static final String GEOS_ARCHSITES_CRS = "EPSG:26713";

    /**
     * Location of the file containing a sample GeoServer GetFeature response
     * for the {@code archsites} feature type
     */
    public static final String GEOS_ARCHSITES_DATA = "geoserver/geoserver_archsites_features.xml";

    /**
     * Location of a test DescribeFeatureType response from a geoserver
     * {@code sf:archsites} FeatureType
     */
    public static final String GEOS_ARCHSITES_SCHEMA = "geoserver/geoserver_archsites_describeFeatureType.xsd";

    /**
     * Type name for the sample CubeWerx GovernmentalUnitCE featuretype
     */
    public static final QName CUBEWERX_GOVUNITCE_TYPENAME = new QName(
            "http://www.fgdc.gov/framework/073004/gubs", "GovernmentalUnitCE");

    /**
     * Prefixed type name for the sample CubeWerx GovernmentalUnitCE featuretype
     * as used in capabilities
     */
    public static final String CUBEWERX_GOVUNITCE_FEATURETYPENAME = "gubs:GovernmentalUnitCE";

    /**
     * Location of the sample GetFeature response from a CubeWerx server for the
     * GovernmentalUnitCE feature type
     */
    public static final String CUBEWERX_GOVUNITCE_DATA = "CubeWerx_nsdi/CubeWerx_nsdi_GovernmentalUnitCE.xml";

    /**
     * EPSG id for the sample GovUnitCE data
     */
    public static final String CUBEWERX_GOVUNITCE_CRS = "EPSG:4269";

    /**
     * Location of a test DescribeFeatureType response from a CubeWerx
     * {@code GovernmentalUnitCE} FeatureType
     */
    public static final String CUBEWERX_GOVUNITCE_SCHEMA = "CubeWerx_nsdi/CubeWerx_nsdi_GovernmentalUnitCE_DescribeFeatureType.xsd";

    /**
     * Location of a test data capabilities from CubeWerx
     */
    public static final String CUBEWERX_CAPABILITIES = "CubeWerx_nsdi/CubeWerx_nsdi_GetCapabilities.xml";

    public static final QName CUBEWERX_ROADSEG_TYPENAME = new QName(
            "http://www.fgdc.gov/framework/073004/transportation", "RoadSeg");

    public static final String CUBEWERX_ROADSEG_FEATURETYPENAME = "trans:RoadSeg";

    public static final String CUBEWERX_ROADSEG_SCHEMA = "CubeWerx_nsdi/CubeWerx_nsdi_RoadSeg_DescribeFeatureType.xsd";

    public static final String CUBEWERX_ROADSEG_CRS = "EPSG:4269";

    public static final String CUBEWERX_ROADSEG_DATA = "CubeWerx_nsdi/CubeWerx_nsdi_RoadSeg.xml";

    protected WFS110ProtocolHandler protocolHandler;

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        protocolHandler = null;
    }

    /**
     * Creates the test {@link #protocolHandler} with a default connection
     * factory that parses the capabilities object from the test xml file
     * pointed out by {@code capabilitiesFileName}
     * <p>
     * Tests methods call this one to set up a protocolHandler to test
     * </p>
     * 
     * @param capabilitiesFileName
     *            the relative path under {@code test-data} for the file
     *            containing the WFS_Capabilities document.
     * @throws IOException
     */
    protected void createProtocolHandler(String capabilitiesFileName) throws IOException {
        ConnectionFactory connFac = new DefaultConnectionFactory();
        createProtocolHandler(capabilitiesFileName, connFac);
    }

    /**
     * Creates the test {@link #protocolHandler} with the provided connection
     * factory that parses the capabilities object from the test xml file
     * pointed out by {@code capabilitiesFileName}
     * <p>
     * Tests methods call this one to set up a protocolHandler to test
     * </p>
     * 
     * @param capabilitiesFileName
     *            the relative path under {@code test-data} for the file
     *            containing the WFS_Capabilities document.
     * @throws IOException
     */
    protected void createProtocolHandler(String capabilitiesFileName, ConnectionFactory connFac)
            throws IOException {
        InputStream stream = TestData.openStream(this, capabilitiesFileName);
        protocolHandler = new WFS110ProtocolHandler(stream, connFac, Integer.valueOf(0));
    }

}
