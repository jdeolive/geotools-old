/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gc;

// J2SE dependencies
import java.util.Random;
import java.util.Arrays;
import java.awt.geom.*;

// Geotools dependencies
import org.geotools.gc.*;
import org.geotools.cs.*;
import org.geotools.ct.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link GridLocalization} implementation.
 *
 * @version $Id: LocalizationGridTest.java,v 1.4 2002/08/08 18:36:07 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class LocalizationGridTest extends TestCase {
    /**
     * Random number generator for this test.
     */
    private Random random;

    /**
     * The grid of localization to test.
     */
    protected LocalizationGrid grid;
    
    /**
     * The "real world" coordinates of the image.
     */
    private static final double[][] REAL_WORLD_VALUE =  
                                                {{74.27344,   -37.882812,    /* First line. */
                                                  72.69531,   -38.375,    
                                                  71.38281,   -38.765625, 
                                                  70.25,      -39.085938, 
                                                  69.25781,   -39.359375, 
                                                  68.375,    -39.585938}, 
                                                 {74.27344,   -37.875,       /* Second line. */
                                                  72.69531,   -38.367188, 
                                                  71.375,     -38.757812, 
                                                  70.25,      -39.078125, 
                                                  69.25781,   -39.34375,  
                                                  68.36719,  -39.578125},   
                                                 {74.265625,  -37.867188,    /* third line. */
                                                  72.6875,    -38.359375,  
                                                  71.375,     -38.75,      
                                                  70.24219,   -39.070312,  
                                                  69.25,      -39.335938,  
                                                  68.36719,  -39.570312},  
                                                 {74.25781,   -37.859375,    /* fourth line. */
                                                  72.6875,    -38.351562,  
                                                  71.36719,   -38.742188,  
                                                  70.234375,  -39.0625,    
                                                  69.24219,   -39.328125,  
                                                  68.359375, -39.5625},   
                                                 {74.25781,   -37.851562,    /* fifth line. */
                                                  72.67969,   -38.34375,   
                                                  71.359375,  -38.734375,  
                                                  70.234375,  -39.054688,  
                                                  69.24219,   -39.320312,  
                                                  68.359375, -39.546875},  
                                                 {74.25,      -37.84375,     /* sixth line. */
                                                  72.671875,  -38.335938,  
                                                  71.359375,  -38.726562,  
                                                  70.22656,   -39.039062,  
                                                  69.234375,  -39.3125,    
                                                  68.35156,   -39.539062}};                                                  
                                                  
    /**
     * Offset of x and y coordinates in an "real world" entry.
     */                                                  
    private static final int X_OFFSET = LocalizationGridTransform2D.X_OFFSET,
                             Y_OFFSET = LocalizationGridTransform2D.Y_OFFSET;
    
    /**
     * Length of an "real world" entry in the grid.
     */
    private static final int REAL_WORLD_LENGTH = LocalizationGridTransform2D.CP_LENGTH;
       
    /**
     * Epsilon between expected "real world" and compute "real world"
     * coordinates when direct transformation is used.
     */
    private static final double epsDirectTransform = 0.001;
    
    /**
     * Epsilon between expected "real world" and compute "real world"
     * coordinates when affine transformation is used.
     */
    private static final double epsAffineTransform = 0.4;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(LocalizationGridTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public LocalizationGridTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        random = new Random();
        
        // Construct a grid with real word associates. 
        grid = new LocalizationGrid(REAL_WORLD_VALUE[0].length/REAL_WORLD_LENGTH,
                                    REAL_WORLD_VALUE.length);
        for (int indiceLine = 0; indiceLine < REAL_WORLD_VALUE.length; indiceLine++) {
            for (int indicePoint = 0 ; indicePoint < REAL_WORLD_VALUE[0].length/REAL_WORLD_LENGTH ; indicePoint ++) {
                grid.setLocalizationPoint(indicePoint,
                                          indiceLine, 
                                          REAL_WORLD_VALUE[indiceLine][indicePoint*REAL_WORLD_LENGTH + X_OFFSET],
                                          REAL_WORLD_VALUE[indiceLine][indicePoint*REAL_WORLD_LENGTH + Y_OFFSET]);
            }
        }
    }
    
    /**
     * Test direct transformation from pixel coordinate system to "real world" 
     * coordinate system by using the localization grid.
     */
    public void testDirectTransform() throws TransformException {
        // Construct and set up an array containing index of points to transform in "real 
        // world" coordinates.
        final double[] src = new double[REAL_WORLD_VALUE.length * REAL_WORLD_VALUE[0].length],
                       dst = new double[src.length];
        
        for (int indiceLine = 0; indiceLine < REAL_WORLD_VALUE.length ; indiceLine++) {
            for (int indicePoint = 0 ; indicePoint < REAL_WORLD_VALUE[0].length/REAL_WORLD_LENGTH ; indicePoint ++) {
                src[indiceLine*REAL_WORLD_VALUE[0].length + indicePoint*REAL_WORLD_LENGTH + X_OFFSET] = indicePoint;
                src[indiceLine*REAL_WORLD_VALUE[0].length +indicePoint*REAL_WORLD_LENGTH + Y_OFFSET] = indiceLine;
            }
        }
        
        // Transform point localized by their index in point localized by a "real world"
        // coordinates.
        grid.getMathTransform().transform(src, 0, dst, 0, src.length / REAL_WORLD_LENGTH);
        
        // Test and compare the "real world" compute with the tranformation and the 
        // "real world" expected.
        for (int indiceLine = 0; indiceLine < REAL_WORLD_VALUE.length ; indiceLine++) {
            for (int indicePoint = 0 ; indicePoint < REAL_WORLD_VALUE[0].length/REAL_WORLD_LENGTH ; indicePoint ++) {                
                assertEquals(REAL_WORLD_VALUE[indiceLine][indicePoint*REAL_WORLD_LENGTH + X_OFFSET], 
                             dst[indiceLine*REAL_WORLD_VALUE[0].length + indicePoint*REAL_WORLD_LENGTH + X_OFFSET], 
                             epsDirectTransform);
                assertEquals(REAL_WORLD_VALUE[indiceLine][indicePoint*REAL_WORLD_LENGTH + Y_OFFSET], 
                             dst[indiceLine*REAL_WORLD_VALUE[0].length + indicePoint*REAL_WORLD_LENGTH + Y_OFFSET], 
                             epsDirectTransform);
            }
        }
    }
    
    
    /**
     * Test affine tranformation for the whole grid by comparing the "real world"
     * coordinates expected to the "real world" coordinates expected.
     */
    public void testAffineTransform() {
        // Construct and set up an array containing index of points to transform in "real 
        // world" coordinates.
        final double[] src = new double[REAL_WORLD_VALUE.length * REAL_WORLD_VALUE[0].length],
                       dst = new double[src.length];

        for (int indiceLine = 0; indiceLine < REAL_WORLD_VALUE.length ; indiceLine++) {
            for (int indicePoint = 0 ; indicePoint < REAL_WORLD_VALUE[0].length/REAL_WORLD_LENGTH ; indicePoint ++) {
                src[indiceLine*REAL_WORLD_VALUE[0].length +indicePoint*REAL_WORLD_LENGTH + X_OFFSET] = indicePoint;
                src[indiceLine*REAL_WORLD_VALUE[0].length +indicePoint*REAL_WORLD_LENGTH + Y_OFFSET] = indiceLine;
            }
        }
        
        // Transform point localized by their index in point localized by a "real world"
        // coordinates.
        grid.getAffineTransform().transform(src, 0, dst, 0, src.length / REAL_WORLD_LENGTH);
        
        // Test and compare the "real world" compute with the affine transformation and the 
        // "real world" expected.
        for (int indiceLine = 0; indiceLine < REAL_WORLD_VALUE.length ; indiceLine++) {
            for (int indicePoint = 0 ; indicePoint < REAL_WORLD_VALUE[0].length/REAL_WORLD_LENGTH ; indicePoint ++) {                
                assertEquals(REAL_WORLD_VALUE[indiceLine][indicePoint*REAL_WORLD_LENGTH + X_OFFSET], 
                             dst[indiceLine*REAL_WORLD_VALUE[0].length + indicePoint*REAL_WORLD_LENGTH + X_OFFSET], 
                             epsAffineTransform);
                assertEquals(REAL_WORLD_VALUE[indiceLine][indicePoint*REAL_WORLD_LENGTH + Y_OFFSET], 
                             dst[indiceLine*REAL_WORLD_VALUE[0].length + indicePoint*REAL_WORLD_LENGTH + Y_OFFSET], 
                             epsAffineTransform);
            }
        }
    }

    /**
     * Test some mathematical identities used if {@link LocalizationGrid#fitPlane}.
     */
    public void testMathematicalIdentities() {
        int sum_x  = 0;
        int sum_y  = 0;
        int sum_xx = 0;
        int sum_yy = 0;
        int sum_xy = 0;

        final int width  = random.nextInt(100)+5;
        final int height = random.nextInt(100)+5;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                sum_x  += x;
                sum_y  += y;
                sum_xx += x*x;
                sum_yy += y*y;
                sum_xy += x*y;
            }
        }
        final int n = width*height;
        assertEquals("sum_x" , (n * (width -1))/2,              sum_x );
        assertEquals("sum_y" , (n * (height-1))/2,              sum_y );
        assertEquals("sum_xy", (n * (width-1)*(height-1))/4,    sum_xy);
        assertEquals("sum_xx", (n * (width -0.5)*(width -1))/3, sum_xx, 1E-6);
        assertEquals("sum_yy", (n * (height-0.5)*(height-1))/3, sum_yy, 1E-6);
    }
}
