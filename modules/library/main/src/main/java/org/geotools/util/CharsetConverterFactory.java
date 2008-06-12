package org.geotools.util;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.geotools.factory.Hints;

/**
 * Converter for going from a String to a {@link Charset} and vice versa.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * @since 2.5
 */
public class CharsetConverterFactory implements ConverterFactory {

    public Converter createConverter(Class<?> source, Class<?> target,
            Hints hints) {
        
        if ( CharSequence.class.isAssignableFrom( source ) && 
                Charset.class.isAssignableFrom( target ) ) {
            return new Converter() {
                public <T> T convert(Object source, Class<T> target) throws Exception {
                    try {
                        return(T) Charset.forName( (String) source );
                    }
                    catch( UnsupportedCharsetException e ) {
                        //TODO: log this
                        return null;
                    }
                }
            };
        }
        if ( Charset.class.isAssignableFrom( source ) && 
                CharSequence.class.isAssignableFrom( target ) ) {
            return new Converter() {
                public <T> T convert(Object source, Class<T> target) throws Exception {
                    return (T) ((Charset)source).toString();
                }
                
            };
        }
                
        return null;
    }

}
