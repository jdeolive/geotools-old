package org.geotools.data.dir;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class DirectoryDataStoreFactoryTest extends TestCase {
    
    public void testStringListParam() throws IOException {
        DirectoryDataStoreFactory factory = new DirectoryDataStoreFactory();
        Map params = new HashMap();
        params.put(DirectoryDataStoreFactory.DIRECTORY.key, getClass().getResource("test-data/test1"));
        params.put(DirectoryDataStoreFactory.CREATE_SUFFIX_ORDER.key, "shp mif");
        factory.createDataStore(params);
    }
} 