/*
 * XMLEncoderTest.java
 * JUnit based test
 *
 * Created on 01 August 2003, 16:46
 */

package org.geotools.styling;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;
import junit.framework.*;

/**
 *
 * @author iant
 */
public class XMLEncoderTest extends TestCase {
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.styling");
    public XMLEncoderTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XMLEncoderTest.class);
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    public void testSimpleEncoder() throws Exception{
        java.net.URL base = getClass().getResource("testData/");
        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "markTest.sld");
        LOGGER.info("reading "+surl);
        SLDStyle stylereader = new SLDStyle(factory,surl);
        Style[] style = stylereader.readXML();
        StringWriter output = new StringWriter();
//        Writer output = new PrintWriter(System.out); 
                XMLEncoder encode = new XMLEncoder(output, style[0]);
                LOGGER.info("Resulting SLD is \n" +
                    output.getBuffer().toString());
                String result = output.getBuffer().toString();
        
                result.replaceAll("\n","");
                result.replaceAll("\t","");
        assertTrue(result.indexOf("<FeatureTypeName>testPoint</FeatureTypeName>")>0);
        
    
    }
    public void testComplexEncoder() throws Exception{
        java.net.URL base = getClass().getResource("testData/");
        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "sample.sld");
        LOGGER.info("reading "+surl);
        SLDStyle stylereader = new SLDStyle(factory,surl);
        Style[] style = stylereader.readXML();
        StringWriter output = new StringWriter();
//        Writer output = new PrintWriter(System.out); 
                XMLEncoder encode = new XMLEncoder(output, style[0]);
                LOGGER.info("Resulting SLD is \n" +
                    output.getBuffer().toString());
    
    }
    public void testComplexTextEncoder() throws Exception{
        java.net.URL base = getClass().getResource("testData/");
        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "textTest.sld");
        LOGGER.info("reading "+surl);
        SLDStyle stylereader = new SLDStyle(factory,surl);
        Style[] style = stylereader.readXML();
        StringWriter output = new StringWriter();
//        Writer output = new PrintWriter(System.out); 
                XMLEncoder encode = new XMLEncoder(output, style[0]);
                LOGGER.info("Resulting SLD is \n" +
                    output.getBuffer().toString());
    
    }
     
}
