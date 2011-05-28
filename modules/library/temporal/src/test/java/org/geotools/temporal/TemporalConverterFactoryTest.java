package org.geotools.temporal;

import java.util.Date;

import org.geotools.util.Converter;
import org.opengis.temporal.Instant;

import junit.framework.TestCase;

public class TemporalConverterFactoryTest extends TestCase {

    TemporalConverterFactory factory;
    
    @Override
    protected void setUp() throws Exception {
        factory = new TemporalConverterFactory();
    }
    
    public void testDateToInstant() throws Exception {
        Converter c = factory.createConverter(Date.class, Instant.class, null);
        assertNotNull(c);
        
        Date d = new Date();
        Instant i = c.convert(d, Instant.class);
        assertNotNull(i);
        
        assertEquals(d, i.getPosition().getDate());
    }
}
