/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 */
package org.geotools.gui.swing;

// J2Se dependencies
import java.awt.*;
import javax.swing.*;
import java.util.Locale;

// JUnit dependencies
import junit.framework.*;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.util.ProgressListener;


/**
 * Test {@link ProgressWindow}.
 *
 * @version $Id: ProgressWindowTest.java,v 1.2 2003/05/13 11:01:40 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ProgressWindowTest extends TestCase {
    /** The description, if any.           */ private static String description;
    /** The source, if any.                */ private static String source;
    /** Text to put in the margin, if any. */ private static String margin;
    /** Warning to print, if any.          */ private static String warning;

    /**
     * Construct the test case.
     */
    public ProgressWindowTest(final String name) {
        super(name);
    }

    /**
     * Run the test case from the command line.
     */
    public static void main(final String[] args) throws Exception {
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        description = arguments.getOptionalString("-description");
        source      = arguments.getOptionalString("-source");
        margin      = arguments.getOptionalString("-margin");
        warning     = arguments.getOptionalString("-warning");
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the suite of tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ProgressWindowTest.class);
        return suite;
    }

    /**
     * Test the progress listener with a progress ranging from 0 to 100% in 10 seconds.
     */
    public void testProgress() throws InterruptedException {
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY-2);
        final ProgressListener progress = new ProgressWindow(null);
        progress.setDescription(description);
        progress.started();
        for (int i=0; i<=100; i++) {
            progress.progress(i);
            Thread.currentThread().sleep(100);
            if ((i==40 || i==80) && warning!=null) {
                progress.warningOccurred(source, margin, warning);
            }
        }
        progress.complete();
    }
}
