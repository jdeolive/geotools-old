/*
 * TestCaseSupportTest.java
 *
 * Created on November 26, 2003, 11:06 AM
 */

package org.geotools.data.shapefile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
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
    
    public void testUnzipping() throws UnsupportedEncodingException {
        TestCaseSupport.prepared = false;       
        URL r = getClass().getResource("/testData/");
        String decoded = URLDecoder.decode(r.getPath(),"UTF-8");
        assertNotNull(decoded);
        File dir = new File(decoded);
        assertTrue(dir.exists());
        File f = new File(decoded,folderWithSpaces);
        System.out.println("creating " + f);
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
        
        // IF YOU DON'T DO THIS, THINGS ARE BAD FOR THE REST OF THE TEST CASES
        TestCaseSupport.prepared = false;
    }
    
    protected void tearDown() throws Exception {
        // make sure TestCaseSupport is ready for the following suites!!!!
        assertTrue(! TestCaseSupport.prepared );
    }

    class Dumby extends TestCaseSupport {
        public Dumby() {
            super("",folderWithSpaces);
        }
    }
    
}
