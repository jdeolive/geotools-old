package org.geotools.jdbc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.LineString;

public abstract class JDBCVirtualTableTest extends JDBCTestSupport {
    protected String dbSchemaName = null;

    @Override
    protected abstract JDBCDataStoreAPITestSetup createTestSetup();

    @Override
    protected void connect() throws Exception {
        super.connect();
    
        SQLDialect dialect = dataStore.getSQLDialect();
        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        dialect.encodeColumnName(aname("id"), sb);
        sb.append(", ");
        dialect.encodeColumnName(aname("geom"), sb);
        sb.append(", ");
        dialect.encodeColumnName(aname("river"), sb);
        sb.append(", ");
        dialect.encodeColumnName(aname("flow"), sb);
        sb.append(" * 2 as ");
        dialect.encodeColumnName(aname("doubleFlow"), sb);
        sb.append(" from ");
        if (dbSchemaName!=null) {
            dialect.encodeSchemaName(dbSchemaName, sb);
            sb.append(".");
        }
        dialect.encodeTableName(tname("river"), sb);
        sb.append(" where ");
        dialect.encodeColumnName(aname("flow"), sb);
        sb.append(" > 4");
        VirtualTable vt = new VirtualTable("riverReduced", sb.toString());
        vt.addGeometryMetadatata("geom", LineString.class, 4326);
        dataStore.addVirtualTable(vt);
        
        vt = new VirtualTable("riverReducedPk", sb.toString());
        vt.addGeometryMetadatata("geom", LineString.class, 4326);
        vt.setPrimaryKeyColumns(Arrays.asList(aname("id")));
        dataStore.addVirtualTable(vt);        
    }

    public void testRiverReducedSchema() throws Exception {
        SimpleFeatureType type = dataStore.getSchema("riverReduced");
        assertNotNull(type);
        
        assertEquals(4, type.getAttributeCount());
        AttributeDescriptor id = type.getDescriptor(aname("id"));
        assertTrue(Number.class.isAssignableFrom(id.getType().getBinding()));
        GeometryDescriptor geom = type.getGeometryDescriptor();
        assertEquals(aname("geom"), geom.getLocalName());
        AttributeDescriptor river = type.getDescriptor(aname("river"));
        assertEquals(String.class, river.getType().getBinding());
        AttributeDescriptor doubleFlow = type.getDescriptor(aname("doubleFlow"));
        assertTrue(Number.class.isAssignableFrom(doubleFlow.getType().getBinding()));
    }
    
    public void testListAll() throws Exception {
        FeatureSource fsView = dataStore.getFeatureSource("riverReduced");
        assertFalse(fsView instanceof FeatureStore);

        assertEquals(1, fsView.getCount(Query.ALL));
        
        FeatureIterator<SimpleFeature> it = null;
        try {
            it = fsView.getFeatures().features();
            
            assertTrue(it.hasNext());
            SimpleFeature sf = it.next();
            assertEquals("rv1", sf.getAttribute(aname("river")));
            assertEquals(9.0, ((Number) sf.getAttribute(aname("doubleFlow"))).doubleValue(), 0.1);
            assertFalse(it.hasNext());
        } finally {
            it.close();
        }
    }
    
    public void testBounds() throws Exception {
        FeatureSource fsView = dataStore.getFeatureSource("riverReduced");
        ReferencedEnvelope env = fsView.getBounds();
        assertNotNull(env);
    }
    
    public void testInvalidQuery() throws Exception {
        String sql = dataStore.getVirtualTables().get("riverReduced").getSql();
        
        VirtualTable vt = new VirtualTable("riverPolluted", "SOME EXTRA GARBAGE " + sql);
        vt.addGeometryMetadatata("geom", LineString.class, -1);
        try {
            dataStore.addVirtualTable(vt);
            fail("Should have failed with invalid sql definition");
        } catch(IOException e) {
            // ok, that's what we expected
        }
    }
    
    public void testGetFeatureId() throws Exception {
        FeatureSource fsView = dataStore.getFeatureSource("riverReducedPk");
        assertFalse(fsView instanceof FeatureStore);

        assertEquals(1, fsView.getCount(Query.ALL));
        
        FeatureIterator<SimpleFeature> it = null;
        try {
            it = fsView.getFeatures().features();
            
            assertTrue(it.hasNext());
            SimpleFeature sf = it.next();
            // check the primary key is build out of the fid attribute
            assertEquals("riverReducedPk.0", sf.getID());
        } finally {
            it.close();
        }
    }
}
