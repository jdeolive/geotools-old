/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.factory;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.*;

/** This code was mostly spiked from SAXParserFactory, which I know runs in
 * applets. The former way of doing things was to use System.getProperty,
 * which cause security problems. Because EVERYTHING in geotools uses factories,
 * I thought, "hey, we should make a class for this, or something". The other
 * problem was using the ServiceProvider thing from sun, BUT THIS COMPLETELY
 * VIOLATES ALL THE PRINCIPLES OF MAKING OUR CODE JVM INDEPENDENT!!!!!!!!!!!
 * This package works the same way.<br>
 *
 * The mechanism for finding factories (which must implement Factory) is as
 * follows:<br>
 * <ol>
 * <li>Look up key in System properties. If key doesn't exist or a
 * SecurityException is thrown, fall through. Otherwise attempt to instantiate
 * the given class.</li>
 * <li>Search the resource paths for the key in META-INF/services. If the resource
 * is found, the file is read and the class is instantiated. If the resource does
 * not exist, fall through</li>
 * <li>Finally, each call to findFactory must provide a default implementation
 * class name. If this class is found, it is instantiated and used. If not, a
 * grave error is thrown (FactoryConfigurationError).</li>
 * <OL>
 * The ClassLoader used to search for resources can be specfied, and if not
 * provided, the search mechanism uses first Thread.getContextClassLoader and then
 * falls back on the system ClassLoader.<br>
 * @author Ian Schneider
 * @version $Id: FactoryFinder.java,v 1.2 2003/07/17 07:09:52 ianschneider Exp $
 */
public final class FactoryFinder {
  
  private static Object newInstance(String clazz,ClassLoader loader) {
  
    Logger logger = Logger.getLogger("org.geotools.factory");
    logger.finest("Creating instance from '" + clazz + "'");
    
    try {
      Class spiClass;
      if (loader == null) {
        spiClass = Class.forName(clazz);
      } else {
        spiClass = loader.loadClass(clazz);
      }
      return spiClass.newInstance();
    } catch (ClassNotFoundException x) {
      throw new FactoryConfigurationError(
      "Provider " + clazz + " not found - " +
      "please check your classpath", x);
    } catch (ExceptionInInitializerError eiie) {
      throw new FactoryConfigurationError(
      "Provider " + clazz + " not initialized - " +
      "error in constructor", eiie);
    } catch (InstantiationException ie) {
      throw new FactoryConfigurationError(
      "Provider " + clazz + " could not be instantiated - " +
      "is it abstract, an interface, an array, or does it not have an " +
      "empty constructor?",ie);
    } catch (IllegalAccessException iae) {
      throw new FactoryConfigurationError(
      "Provider " + clazz + " could not be accessed - " +
      "is it private or is the empty constructor private?",iae);
    }
  }
  
  private static ClassLoader findLoader() {
    
    Logger logger = Logger.getLogger("org.geotools.factory");
    logger.finest("Finding ClassLoader");
    
    // lets get a class loader. By using the Thread's class loader, we allow
    // for more flexability.
    ClassLoader classLoader = null;
    try {
      classLoader = Thread.currentThread().getContextClassLoader();
      logger.finest("Using context ClassLoader " + classLoader);
    } catch (SecurityException se) {
      classLoader = FactoryFinder.class.getClassLoader();
      logger.finest("Using system ClassLoader " + classLoader);
    }
    return classLoader;
  }
  
  private static Factory getFactory(URL resource,ClassLoader loader) throws IOException {
    Logger.getLogger("org.geotools.factory").finest("Searching resource " + resource);
    if (resource == null){
        return null;
    }
    InputStream in = resource.openStream();
    return getFactory(in,loader);
  }
  
  private static Factory getFactory(InputStream in,ClassLoader loader) throws IOException {
    Logger log = Logger.getLogger("org.geotools.factory");
    
    log.finest("Reading Factory , has stream : " + (in != null));
    
    if( in !=null ) {
      
      BufferedReader rd =new BufferedReader(
      new InputStreamReader(in, "UTF-8")
      );
      
      String factoryClassName = rd.readLine().trim();
      rd.close();
      
      if (factoryClassName != null && factoryClassName.length() != 0) {
        return (Factory) newInstance(factoryClassName, loader);
      }
      
      log.finest("Factory name unacceptable : '" + factoryClassName + "'");
    }
    
    return null;
  }
  
  /** Find a Factory using the given key, the given default backup implementation, and
   * the specified ClassLoader. If the ClassLoader is null, the ClassLoader will be
   * assigned using the search mechanism discussed above.
   * @param factoryKey The key to search for an implementation with.
   * @param defaultImpl The name of a default implementation to use.
   * @param classLoader The ClassLoader to search in. May be null.
   * @throws FactoryConfigurationError If the given arguments do not yield a findable, instantiable Factory.
   * @throws ClassCastException If the given arguments yield an Object which is not a Factory
   * @return A Factory object.
   */  
  public static Factory findFactory(
  String factoryKey,
  String defaultImpl,
  ClassLoader classLoader) throws FactoryConfigurationError,ClassCastException {
    Logger logger = Logger.getLogger("org.geotools.factory");
    logger.finest(
      "findFactory(" + factoryKey + "," + defaultImpl + "," + classLoader + ")"
    );
    if (classLoader == null)
      classLoader = findLoader();
    
    logger.finest("ClassLoader : " + classLoader);
    
    // Use the system property first
    
    try {
      logger.finest("Searching system properties");
      String systemProp = System.getProperty( factoryKey );
      if ( systemProp!=null ) {
        logger.finest(factoryKey + "=" + systemProp);
        return (Factory) newInstance(systemProp, classLoader);
      }
      logger.finest("Key '" + factoryKey + "' not found");
    } catch (SecurityException se) {
      logger.finest("SecurityException " + se.getMessage());
    }
    
    
    
    String serviceId = "META-INF/services/" + factoryKey;
    
    logger.finest("Searching for service: " + serviceId);
    
    // try to find services in CLASSPATH
    try {
      InputStream is = classLoader.getResourceAsStream( serviceId );
      Factory f = getFactory(is, classLoader);
      if (f != null) {
        return f;
      }
      logger.finest("Service '" + serviceId + "' not found");
    } catch( Exception ex ) {
      logger.finest("Exception " + ex.getMessage());
    }
    
    logger.finest("Resorting to default");
    
    if (defaultImpl == null) {
      logger.finest("No default");
      throw new FactoryConfigurationError(
      "Provider for " + factoryKey + " cannot be found", null);
    }
    
    return (Factory) newInstance(defaultImpl, classLoader);
  }
  
  /** A convienience method for calling findFactory(factoryKey,defaultImpl,null).
   * @param factoryKey The key to search for an implementation with.
   * @param defaultImpl The name of a default implementation to use.
   * @throws FactoryConfigurationError If the given arguments do not yield a findable, instantiable Factory.
   * @throws ClassCastException If the given arguments yield an Object which is not a Factory
   * @return A Factory object.
   */  
  public static Factory findFactory(String factoryKey,String defaultImpl)
  throws FactoryConfigurationError,ClassCastException {
    return findFactory(factoryKey,defaultImpl,findLoader());
  }
  
  /** A convenience method for factories(key,null).
   * @param key The key to search with. The actual key to search with will be key.getName().
   * @return An Iterator of Factory objects. May have no entries.
   */  
  public static Iterator factories(Class key) {
    return factories(key, findLoader()); 
  }
  
  /** Obtain an Iterator of Factory Objects which are found using the given key. This
   * is a replacement for the sun.misc.Service class, and can be used to implement
   * a service provider architecture.
   * @param key The key to search with. The actual key to search with will be key.getName().
   * @param loader The ClassLoader to search for resources with.
   * @return An Iterator of Factory objects. May have no entries.
   */  
  public static Iterator factories(Class key,final ClassLoader loader) {
    String clazz = key.getName();
    String serviceId = "META-INF/services/" + clazz;
    try {
      final Enumeration resources = loader.getResources(serviceId);
      return new Iterator() {
        public boolean hasNext() {
          return resources.hasMoreElements();
        }
        
        public Object next() {
          URL url = (URL) resources.nextElement();
          try {
            return getFactory( url, loader);
          } catch (IOException ioe) {
            return null; 
          }
        }
        
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    } catch (IOException ioe) {
      throw new RuntimeException("Unexpected IOException " + ioe.getMessage(),ioe);
    }
  }
}
