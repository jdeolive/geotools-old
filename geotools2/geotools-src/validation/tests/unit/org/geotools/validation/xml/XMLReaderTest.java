/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
/*
 * Created on Jan 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.xml;

import junit.framework.TestCase;
import org.geotools.validation.dto.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * XMLReaderTest purpose.
 * 
 * <p>
 * Description of XMLReaderTest ...
 * </p>
 * 
 * <p>
 * Capabilities:
 * </p>
 * 
 * <ul>
 * <li>
 * Feature: description
 * </li>
 * </ul>
 * 
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 * XMLReaderTest x = new XMLReaderTest(...);
 * </code></pre>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: sploreg $ (last modification)
 * @version $Id: XMLReaderTest.java,v 1.1 2004/04/29 21:57:32 sploreg Exp $
 */
public class XMLReaderTest extends TestCase {
    public XMLReaderTest() {
        super("XMLReaderTest");
    }

    public XMLReaderTest(String s) {
        super(s);
    }

    public void testReadPlugIn() {
        try {
            FileReader fr = new FileReader(
                    "C:/Java/workspace/geoserver/conf/plugins/Constraint.xml");
            PlugInDTO dto = XMLReader.readPlugIn(fr);
            assertNotNull("Error if null", dto);
            assertTrue("Name read", "Constraint".equals(dto.getName()));
            assertTrue("Description read",
                "All features must pass the provided filter".equals(
                    dto.getDescription()));
            assertTrue("ClassName read",
                "org.geoserver.validation.plugins.filter.OGCFilter".equals(
                    dto.getClassName()));
            assertNotNull("Should be one arg.", dto.getArgs());
            assertTrue("Should be one arg.", dto.getArgs().size() == 1);
            assertTrue("Arg. name", dto.getArgs().containsKey("tempDirectory"));
            assertTrue("Arg. value : " + dto.getArgs().get("tempDirectory"),
                dto.getArgs().containsValue(new URI("file:///c:/temp")));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testReadTestSuite() {
        try {
            //set-up
            FileReader fr = new FileReader(
                    "C:/Java/workspace/geoserver/conf/plugins/Constraint.xml");
            Map m = new HashMap();
            PlugInDTO dto = XMLReader.readPlugIn(fr);
            m.put(dto.getName(), dto);
            fr = new FileReader(
                    "C:/Java/workspace/geoserver/conf/plugins/NameInList.xml");
            dto = XMLReader.readPlugIn(fr);
            m.put(dto.getName(), dto);

            fr = new FileReader(
                    "C:/Java/workspace/geoserver/conf/validation/RoadTestSuite.xml");

            TestSuiteDTO testsuite = XMLReader.readTestSuite(fr, m);
            assertTrue("TestSuite Name read",
                "RoadTestSuite".equals(testsuite.getName()));

            // multi line so cannot effectively test

            /*assertTrue("TestSuite Description read",("This test suite checks each road name to see \n"+
               "that they are of appropriate length and checks to \n"+
               "see if they are on the list of possible road names.\n"+
               "It also checks to see if any roads are contained in\n"+
               "a specified box.").equals(testsuite.getDescription()));*/
            TestDTO test = (TestDTO) testsuite.getTests().get("NameLookup");

            assertNotNull("NameLookup does not exist as a test",test);

            // multi line so cannot effectively test
            // assertTrue("Test Description read","Checks to see if the road name is in the list of possible names.".equals(test.getDescription()));
            assertNotNull("Should not be null", test.getPlugIn());
            assertTrue("Test plugInName read",
                "NameInList".equals(test.getPlugIn().getName()));

            assertNotNull("Should be one arg.", test.getArgs());
            assertTrue("Should be one arg.", test.getArgs().size() == 2);
            assertTrue("Arg. name", test.getArgs().containsKey("LUTName"));

            // multi line so cannot effectively test
            //assertTrue("Arg. value : "+test.getArgs().get("LUTName"),test.getArgs().containsValue("RoadNameLUT.xls"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
