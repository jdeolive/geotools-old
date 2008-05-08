package org.geotools.util;

import java.awt.Color;

import junit.framework.TestCase;

public class ColorConverterFactoryTest extends TestCase {

    ColorConverterFactory factory;
    
    protected void setUp() throws Exception {
        factory = new ColorConverterFactory();
    }
    
    public void testFromString() throws Exception {
        assertEquals( Color.RED, convert( "#FF0000" ) );
    }
    
    public void testFromInteger() throws Exception {
        assertEquals( Color.RED, convert(0xFF0000) );
        assertEquals( "no alpha", new Color( 0,0,255,255), convert(0x000000FF) );
        
        assertEquals( "255 alpha", new Color( 0,0,255,255), convert(0xFF0000FF) );
        
        assertEquals( "1 alpha", new Color( 0,0,255,1), convert(0x010000FF) );
    }
    
    Color convert( Object value ) throws Exception {
        Converter converter = factory.createConverter( value.getClass(), Color.class, null );
        return (Color) converter.convert( value, Color.class );
    }
}