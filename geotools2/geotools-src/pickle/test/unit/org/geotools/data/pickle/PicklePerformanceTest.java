package org.geotools.data.pickle;

import java.io.*;
import java.net.*;
import org.geotools.data.shapefile.ShapefileDataSource;
import org.geotools.feature.*;

public class PicklePerformanceTest {

  public static final void main(String[] args) throws Exception {
    String tmpDir = System.getProperty("java.io.tmpdir");
    java.net.URL url = new java.net.URL("file:" + args[0]);
    java.net.URL tmpShp = new java.net.URL("file:" + tmpDir + "/tmpshp_delete_me.shp");
    java.util.logging.Logger.getLogger("org.geotools.data.shapefile").setLevel(java.util.logging.Level.OFF);
    long[] times = new long[4];
    final int trials = 10;
    for (int i = 0; i < trials; i++) {
      long time = System.currentTimeMillis();
      org.geotools.data.shapefile.ShapefileDataSource sds = new org.geotools.data.shapefile.ShapefileDataSource(url);
      FeatureCollection fc = sds.getFeatures();
      System.out.println("read shapefile");
      times[0] += (System.currentTimeMillis() - time);
      
      time = System.currentTimeMillis();
      sds = new org.geotools.data.shapefile.ShapefileDataSource(tmpShp);
      sds.setFeatures(fc);
      System.out.println("wrote shapefile");
      times[1] += (System.currentTimeMillis() - time);
      
      time = System.currentTimeMillis();
      PickleDataSource pds = new PickleDataSource(new File(tmpDir), "pickletest_deleteme");
      pds.setFeatures(fc);
      System.out.println("pickled");
      times[2] += (System.currentTimeMillis() - time);
      
      time = System.currentTimeMillis();
      fc = pds.getFeatures();
      System.out.println("unpickled");
      times[3] += (System.currentTimeMillis() - time);
      
      // uncomment to test serial access
//      
//            long total = 0;
//            for (int j = 0, jj = fc.size(); j < jj; j++) {
//      
//              time = System.currentTimeMillis();
//              if (pds.getFeature(j) == null)
//                System.out.println("FAILURE");
//              total += System.currentTimeMillis() - time;
//            }
//            System.out.println("random access average (" + fc.size() + ") "  + (double)total / fc.size());
    }
    System.out.println("shapefile read  : " + (times[0] / (double)trials));
    System.out.println("shapefile write : " + (times[1] / (double)trials));
    System.out.println("pickle          : " + (times[2] / (double)trials));
    System.out.println("unpickle        : " + (times[3] / (double)trials));
  }
}
