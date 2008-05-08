package org.geotools.data.postgis;

import java.io.IOException;
import java.util.Map;

public class PostgisAlternateSchemaOnlineTest extends PostgisFeatureWriterOnlineTest {

    /**
     * Test auto increment fid mapper on alternate schema
     */
    public void testWrite() throws Exception {
        assertEquals(table7+".1",attemptWrite(table7));
    }
    
    public Map getParams() {
        Map map = super.getParams();
        map.put("schema", TEST_SCHEMA);
        return map;
    }
    
}
