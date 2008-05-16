package org.geotools.renderer.style;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Makes sure the symbol factory lookup works as advertised
 * @author Andrea Aime - TOPP
 *
 */
public class DynamicSymbolFactoryFinderTest extends TestCase {

    public void testLookupMarkFactories() {
        List<MarkFactory> result = loadIterator(DynamicSymbolFactoryFinder.getMarkFactories());
        assertTrue(result.size() >= 2);
        assertContainsClassInstance(result, WellKnownMarkFactory.class);
        assertContainsClassInstance(result, TTFMarkFactory.class);
    }
    
    public void testLookupExternalGraphicFactories() {
        List<ExternalGraphicFactory> result = loadIterator(DynamicSymbolFactoryFinder.getExternalGraphicFactories());
        assertTrue(result.size() >= 2);
        assertContainsClassInstance(result, ImageGraphicFactory.class);
        assertContainsClassInstance(result, SVGGraphicFactory.class);
    }
    
    public void assertContainsClassInstance(List list, Class clazz) {
        for (Object item : list) {
            if(item != null && clazz.isAssignableFrom(item.getClass()))
                return;
        }
        fail("List does not contain any element of class " + clazz.getName());
    }
    
    public <T> List<T> loadIterator(Iterator<T> iterator) {
        List<T> result = new ArrayList<T>();
        while(iterator.hasNext())
            result.add(iterator.next());
        return result;
    }
}
