/*
 * TestCaseSupportTest.java
 *
 * Created on November 26, 2003, 11:06 AM
 */

package org.geotools.data.shapefile;

import java.io.File;
import java.net.URL;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Ah, nothing like a test test.
 * @author  Ian Schneider
 */
public class TestCaseSupportTest extends TestCase {
    
    String folderWithSpaces = "folder with spaces";
    
    /** Creates a new instance of TestCaseSupportTest */
    public TestCaseSupportTest() {
        super("TestCaseSupportTest");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new TestSuite(TestCaseSupportTest.class));
    }
    
    public void testUnzipping() {
        URL r = getClass().getResource("/testData/");
        assertNotNull(r);
        File f = new File(r.getFile(),folderWithSpaces);
        f.mkdir();
        assertTrue(f.exists() && f.isDirectory());
        new Dumby();
        java.util.LinkedList deleteMe = new java.util.LinkedList();
        deleteMe.addAll(java.util.Arrays.asList(f.listFiles()));
        while (deleteMe.size() > 0) {
            File c = (File) deleteMe.removeFirst();
            if (c.isDirectory()) {
                deleteMe.addAll(java.util.Arrays.asList(c.listFiles()));
                deleteMe.addLast(c);
            } 
            c.delete();
        }
        f.delete();
    }

    class Dumby extends TestCaseSupport {
        public Dumby() {
            super("",folderWithSpaces);
        }
    }
    
}
