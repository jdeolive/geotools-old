/*
 * MutextTest.java
 *
 * Created on July 25, 2003, 7:42 PM
 */

package org.geotools.feature;

import junit.framework.*;
import org.geotools.feature.FeatureTypeFactory;
/**
 *
 * @author  en
 */
public class MutextTest extends TestCase {
  
  public MutextTest(java.lang.String testName) {
    super(testName);
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(MutextTest.class);
    return suite;
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  private void assertThreadFinishes(Thread t) {
    assertThreadFinishes(t,10);
  }
  
  private void assertThreadFinishes(Thread t,int sleep) {
    int cnt = 0;
    while (cnt++ < 10) {
      try {
        System.out.println("waiting");
        t.join(sleep);
        if (!t.isAlive()) break;
      } catch (InterruptedException ie) {
        System.out.println("ok,ok,your done already");
        Thread.yield();
        break;
      }
    }
    assertTrue(!t.isAlive());
  }
  
  private void block(int cnt) {
    try {
      Thread.sleep(cnt);
    } catch (InterruptedException ie) {
      System.out.println("what the?");
    }
  }
  
  
  public void testReentry() {
    final Mutex mutex = new Mutex();
    final Thread launcher = Thread.currentThread();
    Thread one = new Thread() {
      public void run() {
        mutex.enterWrite();
        try {
          long time = System.currentTimeMillis();
          for (int i = 0; i < 1000; i++) {
            try {
              mutex.enterRead();
            } finally {
              mutex.exitRead();
            }
          }
          System.out.println("time for 1000 read locks " + (System.currentTimeMillis() - time));
        } finally {
          mutex.exitWrite();
        }
        launcher.interrupt();
      }
    };
    one.start();
    assertThreadFinishes(one,10);
  }
  
  public void testAtomicWrite() {
    final Mutex mutex = new Mutex();
    Thread writer = new Thread() {
      public void run() {
        mutex.enterWrite();
        try {
          block(1000);
          System.out.println("done");
        } finally {
          mutex.exitWrite();
        }
      }
    };
    
    while (! writer.isAlive()) {
      writer.start();
      Thread.yield(); 
    }
    try {
      mutex.enterRead();
      assertTrue(! writer.isAlive());
    } finally {
      mutex.exitRead();
    }
  }
  
  public void testReadThenWrite() {
    final Mutex mutex = new Mutex();
    mutex.enterRead();
    mutex.enterWrite();
    mutex.exitWrite();
    mutex.exitRead();
  }
  
}
