/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.map;

import com.vividsolutions.jts.geom.Envelope;
import java.util.EventObject;
import java.util.logging.Logger;
import junit.framework.*;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.Datum;
import org.geotools.cs.FactoryException;
import org.geotools.cs.HorizontalDatum;
import org.geotools.map.events.AreaOfInterestChangedListener;
import org.geotools.map.DefaultAreaOfInterestModel;
//import org.opengis.cs.CS_CoordinateSystem;


/**
 * Unit test for BoundingBox.
 *
 * @author Cameron Shorter
 */                                
public class DefaultAreaOfInterestModelTest extends TestCase implements AreaOfInterestChangedListener {
    
    private static final Logger LOGGER = Logger.getLogger("org.geotools.map");
    
    /** flag to set and unset when an ChangeEvent is sent */
    private boolean changeEventSent=false;

    /** Test suite for this test case */
    private TestSuite suite = null;
    
    private DefaultAreaOfInterestModel boundingBox = null;
    private Envelope envelope = null;
    private CoordinateSystem cs = null;
    private DefaultAreaOfInterestModel bbox;



    /** 
     * Constructor with test name.
     */
    public DefaultAreaOfInterestModelTest(String testName) {
        super(testName);
    }        
    
    /** 
     * Main for test runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** 
     * Required suite builder.
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite(DefaultAreaOfInterestModelTest.class);
        return suite;
    }
    
    /** 
     * Sets up.
     */
    protected void setUp() {
        envelope=new Envelope(10.0,10.0,15.0,15.0);
        try {
        cs=CoordinateSystemFactory.getDefault(
            ).createGeographicCoordinateSystem("WGS84",HorizontalDatum.WGS84);
        } catch (org.geotools.cs.FactoryException e) {
            LOGGER.warning("FactoryException in setup");
        }
    }


    /** Test null constuctors.  Should raise an exception */
    public void testNullConstructor(){
        try {
            DefaultAreaOfInterestModel bbox1=
                new DefaultAreaOfInterestModel(null,null);
            // If an exception has not been raised yet, then fail the test.
            this.fail("No exception when creating using a null contructor");
        } catch (IllegalArgumentException e) {
        }
        
        try {
            DefaultAreaOfInterestModel bbox1=
                new DefaultAreaOfInterestModel(envelope,null);
            // If an exception has not been raised yet, then fail the test.
            this.fail("No exception when creating using a null Bbox");
        } catch (IllegalArgumentException e) {
        }
        
        try {
            DefaultAreaOfInterestModel bbox1=
                new DefaultAreaOfInterestModel(null,cs);
            // If an exception has not been raised yet, then fail the test.
            this.fail("No exception when creating using a null Coord System");
        } catch (IllegalArgumentException e) {
        }
    }
    
    /** Test normal constuctors. */
    public void testConstructor(){
        try {
            bbox=
                new DefaultAreaOfInterestModel(envelope,cs);
        } catch (IllegalArgumentException e) {
            this.fail("exception raised using default contructor");
        }
    }
    
     /** Test change of envelope triggers an event. */
//    public void testSetEnvelope(){
//        changeEventSent=false;
//        bbox.setAreaOfInterest(new Envelope(5.0, 5.0, 10.0,10.0));
//        //delay 0.5 secs to allow a new thread to call
//        //areaOfInterestChangedEvent.
//        //assert("Event not sent after bbox change",changeEventSent);
//        assert(changeEventSent);
//    }
    
    /** Test set/get Envelope */
    
    /* Test change of coordinate system works, ensure there is no dependance
     * on transforms.
     */
    
    /* Test Clonable */
    
    /** Process an AreaOfInterestChangedEvent.
     * @param areaOfInterestChangedEvent The new extent.
     *
     */
    public void areaOfInterestChanged(EventObject areaOfInterestChangedEvent) {
        changeEventSent=true;
    }
    
}
