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

package org.geotools.gui.swing;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;

import org.geotools.gui.swing.sldeditor.style.StyleEditorChooser;
import org.geotools.styling.SLDStyle;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class LegendEditorTest extends TestCase {
    /**
     * The context which contains this maps data
     *
     * @param testName DOCUMENT ME!
     */
    public LegendEditorTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testLegend() {
        URL base = getClass().getResource("testdata/");
        
        
        SLDStyle sld = null;
        
        try {
            File sldFile = new File(URLDecoder.decode(base.getPath(),"UTF-8") + "/color.sld");
            sld = new SLDStyle(StyleFactory.createStyleFactory(), sldFile);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        }
         catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            fail();
        }
        
        Style[] styles = sld.readXML();
        System.out.println("Style loaded");
        
        long start = System.currentTimeMillis();
        StyleEditorChooser sec = new StyleEditorChooser(null, styles[0]);
        
        System.out.println("Style editor created in " + (System.currentTimeMillis() - start));
        
        // Create frame
        JFrame frame = new JFrame();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                System.exit(0);
            }
        });
        frame.setContentPane(sec);
        
        frame.pack();
        frame.show();
        frame.dispose();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new junit.framework.TestSuite(LegendEditorTest.class);
    }
}
