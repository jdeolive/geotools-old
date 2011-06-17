package org.geotools.jdbc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.geotools.data.Join;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

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
            ff.equal(ff.property(aname("stringProperty")), ff.property(aname("name")), true)));

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
        
        it.close();
        ita.close();
        itb.close();
    }
    
    public void testSimpleJoinWithFilter() throws Exception {
        
        FilterFactory ff = dataStore.getFilterFactory();
        Query q = new Query(tname("ft1"));
        q.getJoins().add(new Join(tname("ftjoin"), 
            ff.equal(ff.property(aname("stringProperty")), ff.property(aname("name")), true)));
        q.setFilter(ff.equal(ff.property("stringProperty"), ff.literal("two"), true));
        
        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(1, features.size());

        SimpleFeatureIterator it = features.features();
        try {
            SimpleFeature f = it.next();
            assertEquals(5, f.getAttributeCount());
            assertEquals(new Integer(2), f.getAttribute(aname("intProperty")));
            assertEquals("two", f.getAttribute(aname("stringProperty")));
            
            SimpleFeature g = (SimpleFeature) f.getAttribute(aname("ftjoin"));
            assertEquals(3, g.getAttributeCount());
            assertEquals(new Integer(2), g.getAttribute(aname("id")));
            assertEquals("two", g.getAttribute(aname("name")));
        }
        finally {
            it.close();
        }
    }
    
    public void testSimpleJoinWithFilterCount() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        Query q = new Query(tname("ft1"));
        Join j = new Join(tname("ftjoin"), 
            ff.equal(ff.property(aname("stringProperty")), ff.property(aname("name")), true));
        j.filter(ff.greater(ff.property(aname("id")), ff.literal(1)));
        q.getJoins().add(j);
        q.setFilter(ff.less(ff.property("intProperty"), ff.literal(3)));
        
        assertEquals(1, dataStore.getFeatureSource(tname("ft1")).getCount(q));
    }

    public void testSimpleJoinWithPostFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        
        Filter j = ff.equal(ff.property(aname("stringProperty")), ff.property(aname("name")), true); 
        Query q = new Query(tname("ft1"));
        q.getJoins().add(new Join(tname("ftjoin"), j));
        q.setFilter(ff.equal(
            ff.function("__wktEquals", ff.property("geometry"), ff.literal("POINT (1 1)")), 
            ff.literal(true), true));

        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(1, features.size());
        
        //test with post filter on table being joined 
        q = new Query(tname("ft1"));
        Join join = new Join(tname("ftjoin"), j);
        join.filter(ff.equal(
            ff.function("__wktEquals", ff.property("geom"), 
                    ff.literal("POLYGON ((-1.1 -1.1, -1.1 1.1, 1.1 1.1, 1.1 -1.1, -1.1 -1.1))")), 
            ff.literal(true), true));
        q.getJoins().add(join);
        
        features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(1, features.size());
    }

    public void testSimpleJoinWithSort() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();

        Filter j = ff.equal(ff.property(aname("stringProperty")), ff.property(aname("name")), true); 
        Query q = new Query(tname("ft1"));
        q.getJoins().add(new Join(tname("ftjoin"), j));
        q.setSortBy(new SortBy[]{ff.sort(aname("intProperty"), SortOrder.DESCENDING)});

        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        SimpleFeatureIterator it = features.features();
        try {
            assertTrue(it.hasNext());
            assertEquals("two", it.next().getAttribute("stringProperty"));
            assertTrue(it.hasNext());
            assertEquals("one", it.next().getAttribute("stringProperty"));
            assertTrue(it.hasNext());
            assertEquals("zero", it.next().getAttribute("stringProperty"));
        }
        finally {
            it.close();
        }
    }

    public void testSimpleJoinWithLimitOffset() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        
        Filter j = ff.equal(ff.property(aname("stringProperty")), ff.property(aname("name")), true); 
        Query q = new Query(tname("ft1"));
        q.getJoins().add(new Join(tname("ftjoin"), j));
        q.setFilter(ff.greater(ff.property(aname("intProperty")), ff.literal(0)));
        q.setStartIndex(1);
        q.setSortBy(new SortBy[]{ff.sort(aname("intProperty"), SortOrder.ASCENDING)});
        
        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(1, features.size());

        SimpleFeatureIterator it = features.features();
        try {
            assertTrue(it.hasNext());
            
            SimpleFeature f = it.next();
            assertEquals("two", f.getAttribute("stringProperty"));
            
            SimpleFeature g = (SimpleFeature) f.getAttribute("ftjoin");
            assertEquals("two", g.getAttribute("name"));
        }
        finally {
            it.close();
        }
    }

    public void testSelfJoin() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        Query q = new Query(tname("ft1"));
        q.getJoins().add(new Join(tname("ft1"), 
            ff.equal(ff.property(aname("intProperty")), ff.property(aname("foo.intProperty")), true)).alias("foo"));
        q.setFilter(ff.equal(ff.property("stringProperty"), ff.literal("two"), true));

        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(1, features.size());

        SimpleFeatureIterator it = features.features();
        try {
            assertTrue(it.hasNext());
            
            SimpleFeature f = it.next();
            assertEquals(5, f.getAttributeCount());
            assertEquals(new Integer(2), f.getAttribute(aname("intProperty")));
            assertEquals("two", f.getAttribute(aname("stringProperty")));
            
            SimpleFeature g = (SimpleFeature) f.getAttribute(aname("foo"));
            assertEquals(4, g.getAttributeCount());
            assertEquals(new Integer(2), g.getAttribute(aname("intProperty")));
            assertEquals("two", g.getAttribute(aname("stringProperty")));
        }
        finally {
            it.close();
        }
    }

    public void testSpatialJoin() throws Exception {
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        Query q = new Query(tname("ft1"));
        q.setPropertyNames(Arrays.asList(aname("geometry"), aname("intProperty")));
        q.setSortBy(new SortBy[]{ff.sort(aname("intProperty"), SortOrder.ASCENDING)});
        q.getJoins().add(new Join(tname("ftjoin"),
            ff.contains(ff.property(aname("geom")), ff.property(aname("geometry")))));

        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(6, features.size());
        
        SimpleFeatureIterator it = features.features();
        SimpleFeature f;
        try {
            Set<String> s = new HashSet<String>(Arrays.asList("zero", "one", "two"));
            
            assertTrue(it.hasNext());
            f = it.next();
            assertEquals(0, f.getAttribute(aname("intProperty")));
            s.remove(((SimpleFeature)f.getAttribute(tname("ftjoin"))).getAttribute(aname("name")));
            
            assertTrue(it.hasNext());
            f = it.next();
            assertEquals(0, f.getAttribute(aname("intProperty")));
            s.remove(((SimpleFeature)f.getAttribute(tname("ftjoin"))).getAttribute(aname("name")));
            
            assertTrue(it.hasNext());
            f = it.next();
            assertEquals(0, f.getAttribute(aname("intProperty")));
            s.remove(((SimpleFeature)f.getAttribute(tname("ftjoin"))).getAttribute(aname("name")));
            
            assertEquals(0, s.size());
            
            s = new HashSet<String>(Arrays.asList("one", "two"));
            
            assertTrue(it.hasNext());
            f = it.next();
            assertEquals(1, f.getAttribute(aname("intProperty")));
            s.remove(((SimpleFeature)f.getAttribute(tname("ftjoin"))).getAttribute(aname("name")));
            
            assertTrue(it.hasNext());
            f = it.next();
            assertEquals(1, f.getAttribute(aname("intProperty")));
            s.remove(((SimpleFeature)f.getAttribute(tname("ftjoin"))).getAttribute(aname("name")));

            assertEquals(0, s.size());
            
            assertTrue(it.hasNext());
            f = it.next();
            assertEquals(2, f.getAttribute(aname("intProperty")));
            assertEquals("two", ((SimpleFeature)f.getAttribute(tname("ftjoin"))).getAttribute(aname("name")));
            
            assertFalse(it.hasNext());
        }
        finally {
            it.close();
        }
    }
}
