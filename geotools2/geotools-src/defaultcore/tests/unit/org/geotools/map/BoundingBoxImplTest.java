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
import org.geotools.ct.Adapters;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.Datum;
import org.geotools.cs.FactoryException;
import org.geotools.cs.HorizontalDatum;
import org.geotools.map.events.AreaOfInterestChangedListener;
import org.geotools.map.BoundingBoxImpl;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * Unit test for BoundingBox.
 *
 * @author Cameron Shorter
 */                                
public class BoundingBoxImplTest extends TestCase implements AreaOfInterestChangedListener {
    
    private static final Logger LOGGER = Logger.getLogger("org.geotools.map.BoundingBoxImplTest");
    
    /** flag to set and unset when an ChangeEvent is sent */
    private boolean changeEventSent=false;

    /** Test suite for this test case */
    private TestSuite suite = null;
    
    private BoundingBoxImpl boundingBox = null;
    private Envelope envelope = null;
    private CS_CoordinateSystem cs = null;
    private BoundingBoxImpl bbox;
    private Adapters adapters = Adapters.getDefault();

    /** 
     * Constructor with test name.
     */
    public BoundingBoxImplTest(String testName) {
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
        
        TestSuite suite = new TestSuite(BoundingBoxImplTest.class);
        return suite;
    }
    
    /** 
     * Create a Bounding Box.
     */
    protected void setUp() {
        envelope=new Envelope(10.0,10.0,15.0,15.0);
        try {
            cs = adapters.export(CoordinateSystemFactory.getDefault(
            ).createGeographicCoordinateSystem("WGS84",HorizontalDatum.WGS84));
             
            bbox=new BoundingBoxImpl(envelope,cs);
        } catch (org.geotools.cs.FactoryException e) {
            LOGGER.warning("FactoryException in setup");
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Cannot create bbox");
        }
    }

    /** Test normal constuctors. */
    public void testConstructor(){
        BoundingBoxImpl bbox1;
        try {
            bbox1= new BoundingBoxImpl(envelope,cs);
        } catch (IllegalArgumentException e) {
            this.fail("exception raised using default contructor");
        }
    }

    /** Test null constuctors.  Should raise an exception */
    public void testNullConstructor1(){
        try {
            BoundingBoxImpl bbox1=
                new BoundingBoxImpl(null,null);
            // If an exception has not been raised yet, then fail the test.
            this.fail("No exception when creating using a null contructor");
        } catch (IllegalArgumentException e) {
        }
    }
        
    /** Test null constuctors.  Should raise an exception */
    public void testNullConstructor2(){
        try {
            BoundingBoxImpl bbox1=
                new BoundingBoxImpl(envelope,null);
            // If an exception has not been raised yet, then fail the test.
            this.fail("No exception when creating using a null Bbox");
        } catch (IllegalArgumentException e) {
        }
    }
        
    /** Test null constuctors.  Should raise an exception */
    public void testNullConstructor3(){
        try {
            BoundingBoxImpl bbox1=
                new BoundingBoxImpl(null,cs);
            // If an exception has not been raised yet, then fail the test.
            this.fail("No exception when creating using a null Coord System");
        } catch (IllegalArgumentException e) {
        }
    }
    
     /** Test change of envelope triggers an event when register, and does
      * not trigger an event after deregistering */
    public void testChangeEvent(){
        changeEventSent=false;
        BoundingBoxImpl bbox1;
        try {
            bbox1= new BoundingBoxImpl(envelope,cs);

            bbox1.addAreaOfInterestChangedListener(this);
            bbox1.setAreaOfInterest(new Envelope(5.0, 5.0, 10.0,10.0));
            // areaOfInterest() should be called and changeEventSent set TRUE.
        
            Thread.sleep(1000);
            //delay 1 sec to allow a new thread to call
            //areaOfInterestChangedEvent.
            this.assertTrue("Event not sent after bbox change",changeEventSent);

            changeEventSent=false;
            bbox1.removeAreaOfInterestChangedListener(this);
            bbox1.setAreaOfInterest(new Envelope(5.0, 5.0, 11.0,11.0));
        
            Thread.sleep(1000);
            //delay 1 sec to allow a new thread to call
            //areaOfInterestChangedEvent.
            this.assertTrue("Event sent after de-registering for events",
                !changeEventSent);

        } catch (IllegalArgumentException e) {
            this.fail("exception raised using default contructor");
        } catch (java.lang.InterruptedException e){
            this.fail("exception in sleep: "+e);
        }
    }
    
    /** Test set/get Envelope */
    public void testSetGetEnvelope(){
        Envelope envelope1=new Envelope(10.0,10.0,20.0,20.0);
        bbox.setAreaOfInterest(envelope1);
        Envelope envelope2=bbox.getAreaOfInterest();
        this.assertTrue("set/getEnvelope not working",
            envelope1.equals(envelope2));
    }
    
    /** Test changing inserted envelope does not change envelope
     * internally
     */
    public void testImmutableEnvelope1(){
        Envelope envelope1=new Envelope(10.0,10.0,20.0,20.0);
        Envelope envelope2=new Envelope(envelope1);
        bbox.setAreaOfInterest(envelope1);
        envelope1.expandToInclude(1.0,2.0);
        Envelope envelope3=bbox.getAreaOfInterest();
        this.assertTrue("Changing extracted Envelope changes internal values",
            (envelope1!=envelope2)
            && envelope3.equals(envelope2));
    }

    /** Test changing extracted envelope does not change envelope
     * internally
     */
    public void testImmutableEnvelope2(){
        Envelope envelope1=new Envelope(10.0,10.0,20.0,20.0);
        bbox.setAreaOfInterest(envelope1);
        Envelope envelope3=bbox.getAreaOfInterest();
        Envelope envelope2=new Envelope(envelope3);
        envelope3.expandToInclude(1.0,1.0);
        this.assertTrue("2Changing external Envelope changes internal values",
            (envelope2!=envelope3)
            && envelope2.equals(bbox.getAreaOfInterest()));
    }

    /** Test Clonable */
    public void testClonable(){
    }
    
    /* TestImmutableCoordinateSystem() not required since CoordinateSystem
     * is not immutable.
     */
    
    /* Test change of coordinate system works, ensure there is no dependance
     * on transforms.
     */
    
    /** Process an AreaOfInterestChangedEvent.
     * @param areaOfInterestChangedEvent The new extent.
     *
     */
    public void areaOfInterestChanged(EventObject areaOfInterestChangedEvent) {
        changeEventSent=true;
    }
}
