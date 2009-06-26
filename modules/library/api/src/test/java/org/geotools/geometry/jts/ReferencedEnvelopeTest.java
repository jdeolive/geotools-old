package org.geotools.geometry.jts;

import org.junit.*;

import com.vividsolutions.jts.geom.Coordinate;

import static org.junit.Assert.*;

public class ReferencedEnvelopeTest {

    @Test
    public void testEverything() {
        ReferencedEnvelope everything = ReferencedEnvelope.EVERYTHING;
        ReferencedEnvelope world = new ReferencedEnvelope( ReferencedEnvelope.EVERYTHING );
        
        assertSame( everything, ReferencedEnvelope.EVERYTHING );
        assertNotSame( everything, world );
        assertEquals( everything, world );
        assertEquals( world, everything );
        
        assertFalse( "This is not an empty envelope", everything.isEmpty() );
        assertTrue( "This is a null envelope", everything.isNull() );        
        
        Coordinate center = everything.centre();
        assertNotNull( center );
        
        double area = everything.getArea();
        assertTrue( "area="+area, Double.isInfinite( area ) );
        
        area = world.getArea();
        assertTrue( "area="+area, Double.isInfinite( area ) );
        
        try {
            everything.setBounds( new ReferencedEnvelope() );
            fail("Expected IllegalStateException");
        }
        catch( IllegalStateException expected ){
            // ignore
        }
        everything.setToNull();
        everything.translate(1.0, 1.0);
        
        assertEquals( everything, world );
        assertEquals( world, everything );     
        
        assertEquals( world.getMaximum(0), everything.getMaximum(0),0.0);
        assertEquals( world.getMaximum(1), everything.getMaximum(1),0.0);
        
        assertEquals( world.getMinimum(0), everything.getMinimum(0),0.0);
        assertEquals( world.getMinimum(1), everything.getMinimum(1),0.0);
        
        assertEquals( world.getMedian(0), everything.getMedian(0),0.0);
        assertEquals( world.getMedian(1), everything.getMedian(0),0.0);

    }
    
}
