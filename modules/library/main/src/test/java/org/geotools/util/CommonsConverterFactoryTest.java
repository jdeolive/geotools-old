package org.geotools.util;

import junit.framework.TestCase;

public class CommonsConverterFactoryTest extends TestCase {
    
    CommonsConverterFactory factory;
    
    protected void setUp() throws Exception {
        factory = new CommonsConverterFactory();
    }
    
    public void testStringNumberConversion() throws Exception {
        // test with integers
        assertEquals(12, convert("12", Integer.class));
        assertEquals(null, convert("12.0", Integer.class));
        assertEquals(null, convert("12.5", Integer.class));
        assertEquals(null, convert(Long.MAX_VALUE + "", Integer.class));
        
        // test with longs
        assertEquals(Long.MAX_VALUE, convert(Long.MAX_VALUE + "", Long.class));
        assertEquals(null, convert("1e100", Long.class));
        assertEquals(null, convert("12.5", Long.class));
        
        // test with doubles
        assertEquals((double) Long.MAX_VALUE, convert(Long.MAX_VALUE + "", Double.class));
        assertEquals(1e100, convert("1e100", Double.class));
        assertEquals(12.5, convert("12.5", Double.class));
    }
    
    Object convert( Object source, Class target ) throws Exception {
        return factory.createConverter( source.getClass(), target, null ).convert( source, target );
    }

}
