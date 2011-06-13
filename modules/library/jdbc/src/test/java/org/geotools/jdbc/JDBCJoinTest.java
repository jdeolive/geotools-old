package org.geotools.jdbc;

import java.util.Arrays;

import org.geotools.data.Join;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;

public abstract class JDBCJoinTest extends JDBCTestSupport {

    @Override
    protected abstract JDBCJoinTestSetup createTestSetup();

    public void testSimpleJoin() throws Exception {
        SimpleFeatureIterator ita = 
            dataStore.getFeatureSource(tname("ft1")).getFeatures().features();
        SimpleFeatureIterator itb = 
            dataStore.getFeatureSource(tname("ftjoin")).getFeatures().features();

        FilterFactory ff = dataStore.getFilterFactory();
        Query q = new Query(tname("ft1"));
        q.getJoins().add(new Join(tname("ftjoin"), 
            ff.equal(ff.property(aname("stringProperty")), ff.property(aname("name")), true), Filter.INCLUDE));

        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(dataStore.getFeatureSource(tname("ft1")).getFeatures(q).size(), features.size());

        SimpleFeatureIterator it = features.features();
        assertTrue(it.hasNext() && ita.hasNext() && itb.hasNext());
        
        while(it.hasNext()) {
            SimpleFeature f = it.next();
            assertEquals(5, f.getAttributeCount());
            
            SimpleFeature g = (SimpleFeature) f.getAttribute(tname("ftjoin"));
            
            SimpleFeature a = ita.next();
            SimpleFeature b = itb.next();
            
            for (int i = 0; i < a.getAttributeCount(); i++) {
                assertAttributeValuesEqual(a.getAttribute(i), f.getAttribute(i));
            }
            for (int i = 0; i < b.getAttributeCount(); i++) {
                assertAttributeValuesEqual(b.getAttribute(i), g.getAttribute(i));
            }
        }
    }

    public void testSelfJoin() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        Query q = new Query(tname("ft1"));
        q.getJoins().add(new Join(tname("ft1"), 
            ff.equal(ff.property(aname("intProperty")), ff.property(aname("intProperty")), true), Filter.INCLUDE));
        q.setFilter(ff.equal(ff.property("stringProperty"), ff.literal("two"), true));

        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(1, features.size());

        SimpleFeatureIterator it = features.features();
        assertTrue(it.hasNext());
        
        SimpleFeature f = it.next();
        assertEquals(5, f.getAttributeCount());
        assertEquals(new Integer(2), f.getAttribute(aname("intProperty")));
        assertEquals("two", f.getAttribute(aname("stringProperty")));
        
        SimpleFeature g = (SimpleFeature) f.getAttribute(tname("ft1"));
        assertEquals(4, g.getAttributeCount());
        assertEquals(new Integer(2), g.getAttribute(aname("intProperty")));
        assertEquals("two", g.getAttribute(aname("stringProperty")));
    }

    public void testSpatialJoin() throws Exception {
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        Query q = new Query(tname("ft1"));
        q.setPropertyNames(Arrays.asList(aname("geometry")));

        q.getJoins().add(new Join(tname("ftjoin"),
            ff.intersects(ff.property(aname("geometry")), ff.property(aname("geom"))), Filter.INCLUDE));

        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        SimpleFeatureIterator it = features.features();
        while(it.hasNext()) {
            SimpleFeature f = it.next();
            System.out.println(f.getAttributes());
        }
    }
}
