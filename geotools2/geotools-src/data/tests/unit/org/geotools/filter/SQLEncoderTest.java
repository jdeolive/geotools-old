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
package org.geotools.filter;

import junit.framework.*;
import org.w3c.dom.*;
import java.io.StringWriter;
import java.util.logging.Logger;
import javax.xml.parsers.*;


/**
 * Unit test for sql encoding of filters into where statements.
 *
 * @author Chris Holmes, TOPP
 *
 * @task REVISIT: validate these so we know if they break.
 */
public class SQLEncoderTest extends SQLFilterTestSupport {
    private FilterFactory filterFac = FilterFactory.createFilterFactory();
//
//    /** Test suite for this test case */
//    TestSuite suite = null;
//
//    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;
//
    public SQLEncoderTest(String testName) {
        super(testName);
        LOGGER.finer("running SQLEncoderTests");

        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder = "file:////" + dataFolder + "/tests/unit/testData";
            LOGGER.finer("data folder is " + dataFolder);
        }
    }
//
//    /**
//     * Main for test runner.
//     *
//     * @param args DOCUMENT ME!
//     */
//    public static void main(String[] args) {
//        junit.textui.TestRunner.run(suite());
//    }
//
//    /**
//     * Required suite builder.
//     *
//     * @return A test suite for this unit test.
//     */
//    public static Test suite() {
//        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
//        TestSuite suite = new TestSuite(SQLEncoderTest.class);
//        suite.addTestSuite(CapabilitiesTest.class);
//
//        return suite;
//    }
//
//    public void test1() throws Exception {
//        Filter test = testEncode(dataFolder + "/test1.xml");
//
//        //LOGGER.fine("parsed filter is: " + test);
//    }
//
//    public void test2() throws Exception {
//        Filter test = testEncode(dataFolder + "/test2.xml");
//
//        //LOGGER.fine("parsed filter is: " + test);
//    }
//
//    public void test3a() throws Exception {
//        try {
//            Filter test = testEncode(dataFolder + "/test3a.xml");
//
//            //LOGGER.fine("parsed filter is: " + test);
//        } catch (SQLEncoderException e) {
//            LOGGER.fine("successfully caught exception: " + e);
//
//            //contains geom, should not be supported
//            String expectMessage = "Filter type not supported";
//            assertTrue(expectMessage.equals(e.getMessage()));
//        }
//    }
//
//    public void test8() throws Exception {
//        Filter test = testEncode(dataFolder + "/test8.xml");
//
//        //LOGGER.fine("parsed filter is: " + test);
//    }
//
//    public void test9() throws Exception {
//        Filter test = testEncode(dataFolder + "/test9.xml");
//
//        //LOGGER.fine("parsed filter is: " + test);
//    }
//
//    /*public void test11() //like filters, uncomment when they are supported
//       throws Exception {
//       Filter test = testEncode(dataFolder+"/test11.xml");
//       } */
//    public void test13() throws Exception {
//        Filter test = testEncode(dataFolder + "/test13.xml");
//
//        //LOGGER.fine("parsed filter is: " + test);
//    }
//
    public void testConstructor() throws Exception {
        NullFilter tFilter = filterFac.createNullFilter();

        Integer testInt = new Integer(5);
        Expression testAtt = filterFac.createAttributeExpression(null, "test");
        tFilter.nullCheckValue(testAtt);

        //tFilter.addRightValue(testLiteral);
        LogicFilter notFilter = filterFac.createLogicFilter(AbstractFilter.LOGIC_NOT);
        notFilter.addFilter(tFilter);

        StringWriter output = new StringWriter();
        SQLEncoder encoder = new SQLEncoder(output, notFilter);
        LOGGER.fine("test filter is " + notFilter + "\n encoding result is "
            + output.getBuffer());
    }
//
//    /* public void test14() contains geom filter, not supported
//       throws Exception {
//       Filter test = testEncode(dataFolder+"/test14.xml");
//       } */
//
//    //TODO: changes to testEncode(String uri, String expected) so that 
//    //we actually assert things, so tests break if things mess up.
//    public Filter testEncode(String uri) throws Exception {
//        Filter filter = null;
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        DocumentBuilder db = dbf.newDocumentBuilder();
//        Document dom = db.parse(uri);
//        LOGGER.fine("exporting " + uri);
//
//        // first grab a filter node
//        NodeList nodes = dom.getElementsByTagName("Filter");
//
//        for (int j = 0; j < nodes.getLength(); j++) {
//            Element filterNode = (Element) nodes.item(j);
//            NodeList list = filterNode.getChildNodes();
//            Node child = null;
//
//            for (int i = 0; i < list.getLength(); i++) {
//                child = list.item(i);
//
//                //_log.getLoggerRepository().setThreshold(Level.INFO);
//                if ((child == null)
//                        || (child.getNodeType() != Node.ELEMENT_NODE)) {
//                    continue;
//                }
//
//                filter = FilterDOMParser.parseFilter(child);
//
//                //_log.getLoggerRepository().setThreshold(Level.DEBUG);
//                LOGGER.fine("filter: " + filter.toString());
//
//                //StringWriter output = new StringWriter();
//                SQLEncoder encoder = new SQLEncoder();
//                String out = encoder.encode((AbstractFilter) filter);
//
//                LOGGER.fine("Resulting SQL filter is \n" + out);
//            }
//        }
//
//        return filter;
//    }
}
