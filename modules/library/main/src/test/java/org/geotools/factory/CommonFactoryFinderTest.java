package org.geotools.factory;

import junit.framework.TestCase;

public class CommonFactoryFinderTest extends TestCase {

    public void testGetStyleFactory() {
        assertNotNull( CommonFactoryFinder.getStyleFactories( GeoTools.getDefaultHints() ));
    }

    public void testGetFilterFactory() {
        assertNotNull( CommonFactoryFinder.getFilterFactory( null ));
    }

}
