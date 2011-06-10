package org.geotools.jdbc;

import org.geotools.data.Join;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

public abstract class JDBCJoinTest extends JDBCTestSupport {

    
    public void testSelfJoin() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        Query q = new Query(tname("ft1"));
        q.getJoins().add(new Join("ft1", 
            ff.equal(ff.property("intProperty"), ff.property("intProperty"), true), Filter.INCLUDE));
        q.setFilter(ff.equal(ff.property("stringProperty"), ff.literal("two"), true));

        SimpleFeatureCollection features = dataStore.getFeatureSource(tname("ft1")).getFeatures(q);
        assertEquals(1, features.size());

        SimpleFeatureIterator it = features.features();
        it.hasNext();

        SimpleFeature f = it.next();
        System.out.println(f.getAttributeCount());
    }

}
