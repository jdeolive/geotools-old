package org.geotools.data.pickle;

import java.io.*;
import java.net.*;
import org.geotools.data.shapefile.ShapefileDataSource;
import org.geotools.data.pickle.PickleDataSource;
import org.geotools.feature.*;

public class PicklePerformanceTest {

  public static final void main(String[] args) throws Exception {
    String tmpDir = System.getProperty("java.io.tmpdir");
    URL url = new java.net.URL("file:" + args[0]);
    URL tmpShp = new java.net.URL("file:" + tmpDir + "/tmpshp_delete_me.shp");
    java.util.logging.Logger.getLogger("org.geotools.data.shapefile").setLevel(java.util.logging.Level.OFF);
    for (int i = 0, ii = 3; i < ii; i++) {
      long time = System.currentTimeMillis();
      org.geotools.data.shapefile.ShapefileDataSource sds = new org.geotools.data.shapefile.ShapefileDataSource(url);
      FeatureCollection fc = sds.getFeatures();
      System.out.println("read shapefile in " + (System.currentTimeMillis() - time));
      
      time = System.currentTimeMillis();
      sds = new org.geotools.data.shapefile.ShapefileDataSource(tmpShp);
      sds.setFeatures(fc);
      System.out.println("wrote shapefile in " + (System.currentTimeMillis() - time));
      
      time = System.currentTimeMillis();
      PickleDataSource pds = new PickleDataSource(new File(tmpDir), "junkyCrap");
      pds.setFeatures(fc);
      System.out.println("pickled in " + (System.currentTimeMillis() - time));
  
      time = System.currentTimeMillis();
      fc = pds.getFeatures();
      System.out.println("unpickled in " + (System.currentTimeMillis() - time));
      
      // uncomment to test serial access
      
//      long total = 0;
//      pds = new PickleDataSource(new File("/tmp"), "junkyCrap");
//      for (int j = 0, jj = fc.size(); j < jj; j++) {
//        
//        time = System.currentTimeMillis();
//        if (pds.getFeature(j) == null)
//          System.out.println("FAILURE");
//        total += System.currentTimeMillis() - time;
//      }
//      System.out.println("random access average (" + fc.size() + ") "  + (double)total / fc.size());
    }
  }
}
