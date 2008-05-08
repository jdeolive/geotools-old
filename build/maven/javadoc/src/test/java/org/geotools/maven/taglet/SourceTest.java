/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.maven.taglet;

// J2SE dependencies
import java.util.regex.Matcher;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Source} taglet.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SourceTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(SourceTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public SourceTest(final String name) {
        super(name);
    }

    /**
     * Tests the regular expression validity using the tag for this source file.
     */
    public void testCurrentTag() {
        Source  s = new Source();
        Matcher m;
        String tag, url, group, category, module;
        tag = "$URL$";
        m = s.findURL.matcher(tag);
        assertTrue(m.matches());

        // Try to match the URL provided by SVN.
        url = m.group(1).trim();
        m = s.findModule.matcher(url);
        assertTrue(m.matches());
        group    = m.group(1);
        category = m.group(2);
        module   = m.group(3);
        assertEquals("build", group);
        assertEquals("maven", category);
        assertEquals("javadoc", module);

        // Try an other URL from a tag.
        url = "http://svn.geotools.org/geotools/tags/2.4-M0/modules/library/api/src/main/java/org/geotools/catalog/ResolveChangeListener.java";
        m = s.findModule.matcher(url);
        assertTrue(m.matches());
        group    = m.group(1);
        category = m.group(2);
        module   = m.group(3);
        assertEquals("modules", group);
        assertEquals("library", category);
        assertEquals("api", module);

        // Try an other URL from a tag.
        url = "http://svn.geotools.org/geotools/tags/2.2-RC4/modules/library/referencing/src/main/java/org/geotools/referencing/CRS.java";
        tag = Source.SVN_KEYWORD_DELIMITER + "URL: " + url + ' ' + Source.SVN_KEYWORD_DELIMITER;
        m = s.findURL.matcher(tag);
        assertTrue(m.matches());
        assertEquals(url, m.group(1).trim());
        m = s.findModule.matcher(url);
        assertTrue(m.matches());
        group    = m.group(1);
        category = m.group(2);
        module   = m.group(3);
        assertEquals("modules", group);
        assertEquals("library", category);
        assertEquals("referencing", module);
    }
}
