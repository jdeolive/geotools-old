package org.geotools.data.store;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

public class ReTypingFeatureCollectionTest extends FeatureCollectionWrapperTestSupport {

    public void testSchema() throws Exception {
        // see http://jira.codehaus.org/browse/GEOT-1616
        SimpleFeatureType original = delegate.getSchema();
        String newName = original.getTypeName() + "xxx";
        SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder();
        stb.init(original);
        stb.setName(newName);
        SimpleFeatureType renamed = stb.buildFeatureType();

        ReTypingFeatureCollection rtc = new ReTypingFeatureCollection(delegate, renamed);
        assertEquals(renamed, rtc.getSchema());
    }
}
