package org.geotools.wfs.v_1_1_0.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.namespace.QName;

import org.opengis.feature.simple.SimpleFeatureType;

public class XmlSimpleFeatureParserTest extends AbstractGetFeatureParserTest {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected GetFeatureParser getParser(final QName featureName, final String schemaLocation,
            final SimpleFeatureType featureType, final URL getFeaturesRequest) throws IOException {

        InputStream inputStream = new BufferedInputStream(getFeaturesRequest.openStream());
        GetFeatureParser parser = new XmlSimpleFeatureParser(inputStream, featureName, featureType);
        return parser;
    }

    /**
     * This is to be run as a normal java application in order to reproduce a
     * GetFeature request to the nsdi server and thus being able to
     * assess/profile the OutOfMemory errors I'm getting in uDig
     * 
     * @param argv
     */
    public static void main(String argv[]) {
        XmlSimpleFeatureParserTest test;
        test = new XmlSimpleFeatureParserTest();
        try {
            test.runGetFeaturesParsing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
