package org.geotools.data.dir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.TestData;

public class DirectoryDataStoreTest extends TestCase {

    public void testFeatureTypes() throws IOException {
        DirectoryDataStoreFactory factory = new DirectoryDataStoreFactory();
        Map params = new HashMap();
        copyShapefiles("shapes/archsites.shp");
        File f = copyShapefiles("shapes/bugsites.shp");
        params.put(DirectoryDataStoreFactory.DIRECTORY.key, f.getParentFile().toURL());
        params.put(DirectoryDataStoreFactory.CREATE_SUFFIX_ORDER.key, new String[] {"shp"});
        DirectoryDataStore store =  (DirectoryDataStore) factory.createDataStore(params);
        assertTrue(store.getTypeNames().length > 0);
        assertTrue(Arrays.asList(store.getTypeNames()).contains("archsites"));
        assertTrue(Arrays.asList(store.getTypeNames()).contains("bugsites"));
    }
    
    /**
    * Copies the specified shape file into the {@code test-data} directory, together with its
    * sibling ({@code .dbf}, {@code .shp}, {@code .shx} and {@code .prj} files).
    */
   protected File copyShapefiles(final String name) throws IOException {
       assertTrue(TestData.copy(this, sibling(name, "dbf")).canRead());
       assertTrue(TestData.copy(this, sibling(name, "shp")).canRead());
       try {
           assertTrue(TestData.copy(this, sibling(name, "shx")).canRead());
       } catch (FileNotFoundException e) {
           // Ignore: this file is optional.
       }
       try {
           assertTrue(TestData.copy(this, sibling(name, "prj")).canRead());
       } catch (FileNotFoundException e) {
           // Ignore: this file is optional.
       }
       return TestData.copy(this, name);
   }
  
   /**
    * Helper method for {@link #copyShapefiles}.
    */
   private static String sibling(String name, final String ext) {
       final int s = name.lastIndexOf('.');
       if (s >= 0) {
           name = name.substring(0, s);
       }
       return name + '.' + ext;
   }
} 
