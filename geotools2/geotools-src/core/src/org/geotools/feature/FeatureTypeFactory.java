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
package org.geotools.feature;

import java.io.*;
import java.util.*;
import org.geotools.factory.*;

/**
 * A factory patterned object for the creation of FeatureTypes. Because
 * FeatureTypes are meant to be immutable, this object is mutable.<p>
 * 
 * The basic idea for usage is that you configure the factory to whatever state 
 * is desired, setting properties and adding AttributeTypes. When the desired 
 * state is acheived, the expected FeatureType can be retrieved by calling<br>
 * <code>getFeatureType()</code><br>
 * Repeated calls to getFeatureType will return the <i>same</i> FeatureType 
 * given that no calls which modify the state of the factory are made.<p>
 *
 * Here's an example of how to use this:
 * <code>
 * <pre>
 * FeatureTypeFactory factory = FeatureTypeFactory.newInstance();
 * factory.addType(...);
 * factory.setName(...);
 * factory.setNamespace(...);
 * FeatureType type = factory.getFeatureType();
 * </pre>
 * </code>
 *
 * <b>Remember, changes to any state will invalidate the current FeatureType, if
 * one exists</b><br>
 * 
 * This class is not thread-safe.
 *
 * @version $Id: FeatureTypeFactory.java,v 1.5 2003/07/21 23:49:30 ianschneider Exp $
 * @author  Ian Schneider
 */
public abstract class FeatureTypeFactory implements Factory {
  
  private String name;
  private String namespace;
  private boolean dirty = true;
  private FeatureType type = null;
  private AttributeType defaultGeometry = null;
  private boolean abstractType = false;
  private java.util.Set superTypes;
  private static Set builtInTypes = null;
  private static boolean initialized;
 
  /** An empty public constructor. Subclasses should not provide a constructor. */  
  public FeatureTypeFactory() {} 
  
  /** Create a FeatureTypeFactory which contains all of the AttributeTypes from the
   * given FeatureType. This is simply a convenience method for<br>
   * <code><pre>
   * FeatureTypeFactory factory = FeatureTypeFactory.newInstace();
   * factory.importType(yourTypeHere);
   * factory.setName(original.getName());
   * factory.setNamespace(original.getNamespace());
   * factory.setNillable(original.isNillable());
   * factory.setDefaultGeometry(original.getDefaultGeometry());
   * </pre></code>
   * @param original The FeatureType to obtain information from.
   * @throws FactoryConfigurationError If a FeatureTypeFactory cannot be found.
   * @return A new FeatureTypeFactory which is initialized with the state of the original
   * FeatureType.
   */  
  public static FeatureTypeFactory createTemplate(FeatureType original) throws FactoryConfigurationError {
    FeatureTypeFactory factory = newInstance(original.getTypeName());
    factory.importType(original);
    factory.setNamespace(original.getNamespace());
    factory.setDefaultGeometry(original.getDefaultGeometry());
    FeatureType[] ancestors = original.getAncestors();
    if (ancestors != null)
      factory.setSuperTypes(Arrays.asList(ancestors));
    return factory;
  }
  
  public static FeatureType newFeatureType(
  AttributeType[] types,String name,String ns,boolean isAbstract,FeatureType[] superTypes) 
  throws FactoryConfigurationError,
         SchemaException {
    FeatureTypeFactory factory = newInstance(name);
    factory.addTypes(types);
    factory.setNamespace(ns);
    factory.setAbstract(isAbstract);
    if (superTypes != null) 
      factory.setSuperTypes(Arrays.asList(superTypes));
    return factory.getFeatureType();
  }
  /** Create a new FeatureType with the given AttributeTypes. This is a convenience
   * method for creating a new factory which assumes that the first AttributeType,
   * if a geometry, will be the default geometry.
   * @param types An array of AttributeTypes to use.
   * @param name The name of the new FeatureType.
   * @throws FactoryConfigurationError If a FeatureTypeFactory cannot be found.
   * @throws SchemaException If there are problems creating a FeatureType.
   * @return A new FeatureType containing the above information.
   */  
  public static FeatureType newFeatureType(
  AttributeType[] types,String name,String ns,boolean isAbstract) 
  throws FactoryConfigurationError,
         SchemaException {
    return newFeatureType(types,name,ns,isAbstract,null);
  }
  
  public static FeatureType newFeatureType(
  AttributeType[] types,String name,String ns) 
  throws FactoryConfigurationError,
         SchemaException {
    return newFeatureType(types,name,ns,false);
  }
  
  public static FeatureType newFeatureType(AttributeType[] types,String name) 
  throws FactoryConfigurationError,
         SchemaException {
    return newFeatureType(types,name,null,false);        
  }
  
  /** Create a new FeatureTypeFactory. This class uses the FactoryFinder.
   * @throws FactoryConfigurationError
   * @return
   * @see
   */  
  public static FeatureTypeFactory newInstance(String name) throws FactoryConfigurationError {
    FeatureTypeFactory factory = (FeatureTypeFactory) FactoryFinder.findFactory(
    "org.geotools.feature.FeatureTypeFactory",
    "org.geotools.feature.DefaultFeatureTypeFactory"
    );
    factory.setName(name);
    return factory;
  }
  
  /** Import all of the AttributeTypes from the given FeatureType into this
   * factory.<p>
   * If strict is true, non-uniquely named AttributeTypes will throw an
   * exception.<p>
   * If strict is false, these will be silently ignored, but not added.<p>
   * No other information is imported.
   * @param type The FeatureType to import from.
   * @param strict Enforce namespace restrictions.
   * @throws IllegalArgumentException If strict is true and there are naming problems.
   */  
  public void importType(FeatureType type,boolean strict) throws IllegalArgumentException {
    for (int i = 0, ii = type.getAttributeCount(); i < ii; i++) {
      try {
        addType(type.getAttributeType(i));
      } catch (IllegalArgumentException iae) {
        if (strict)
          throw iae;
      }
    }
  }
  
  public final void setSuperTypes(java.util.Collection types) {
    superTypes = new java.util.LinkedHashSet(types);
  }
  
  public final java.util.Collection getSuperTypes() {
    Set supers = superTypes == null ? new HashSet() : superTypes;
    Set builtin = getBuiltinTypes();
    if (builtin != null)
      supers.addAll(builtin);
    return supers;
  }
  
  
  /** A convienence method for importing AttributeTypes, simply calls<br>
   * <code>
   * importType(type,false)
   * </code>
   * @param type The type to import.
   */  
  public void importType(FeatureType type) {
    importType(type,false);
  }
  
  /** Set the name of the FeatureType this factory will produce.
   * @param name The new name. May be null.
   */  
  public void setName(String name) {
    dirty |= isDifferent(name,this.name);
    this.name = name;
  }
  
  /** Get the current configuration of the name of this factory.
   * @return The current name. May be null.
   */  
  public final String getName() {
    return name;
  }
  
  /** Set the namespace of the FeatureType this factory will produce.
   * @param namespace The new namespace. May be null.
   */ 
  public void setNamespace(String namespace) {
    dirty |= isDifferent(namespace,this.namespace);
    this.namespace = namespace;    
  }
  
  /** Get the current configuration of the namespace of this factory.
   * @return The current namespace. May be null.
   */
  public final String getNamespace() {
    return namespace;
  }
  
  public final boolean isAbstract() {
    return abstractType;
  } 
  
  public final void setAbstract(boolean a) {
    dirty = true;
    this.abstractType = a;
  }
  
  
  private boolean isDifferent(String s1,String s2) {
    if (s1 != null) return !s1.equals(s2);
    if (s2 != null) return !s2.equals(s1);
    return s1 != s2;
  }
  
  /** Remove all the AttributeTypes in this factory. */  
  public final void removeAll() {
    int cnt = getAttributeCount();
    for (int i = cnt; i > 0; i++) {
      removeType(i - 1);
    }
  }
  
  /** Add an array of AttributeTypes to this factory.
   * @param types The types or a null array.
   * @throws NullPointerException If any of the types are null.
   * @throws IllegalArgumentException If there are naming problems.
   */  
  public final void addTypes(AttributeType[] types) 
  throws NullPointerException,
  IllegalArgumentException {
    if (types == null) return;
    for (int i = 0; i < types.length; i++) {
      addType(types[i]);
    }
  }
  
  /** A the given AttributeType to this factory.
   * @param type The type to add.
   * @throws NullPointerException If the type is null.
   * @throws IllegalArgumentException If another type exists with the same name.
   */  
  public final void addType(AttributeType type)
  throws NullPointerException,
  IllegalArgumentException {
    if (type == null)
      throw new NullPointerException("type");
    dirty = true;
    check(type);
    add(type);
  }
  
  /** Remove the given type from this factory.
   * @param type The type to remove.
   * @throws NullPointerException If the type is null.
   */  
  public final void removeType(AttributeType type)
  throws NullPointerException {
    if (type == null)
      throw new NullPointerException("type");
    dirty = true;
    AttributeType removed = remove(type);
    if (removed == defaultGeometry)
      defaultGeometry = null;
  }
  
  /** Insert the given type at the index specified.
   * @param idx The index to insert at.
   * @param type The AttributeType to insert.
   * @throws NullPointerException If the type is null.
   * @throws IllegalArgumentException
   * @throws ArrayIndexOutOfBoundsException If the index is out of range.
   */  
  public final void addType(int idx,AttributeType type)
  throws NullPointerException,
  IllegalArgumentException,
  ArrayIndexOutOfBoundsException {
    if (type == null)
      throw new NullPointerException("type");
    dirty = true;
    check(type);
    add(idx,type);
  }
  
  /** Remove the AttributeType at the given index. */  
  public final void removeType(int idx)
  throws NullPointerException,
  ArrayIndexOutOfBoundsException {
    dirty = true;
    AttributeType removed = remove(idx);
    if (removed == defaultGeometry)
      defaultGeometry = null;
  }
  
  /** Set the AttributeType at the given index. Overwrites the existing type.
   * @param idx
   * @param type
   * @throws IllegalArgumentException
   */  
  public final void setType(int idx,AttributeType type)
  throws IllegalArgumentException {
    if (type == null)
      throw new NullPointerException("type");
    dirty = true;
    check(type);
    AttributeType removed = set(idx,type);
    if (removed == defaultGeometry)
      defaultGeometry = null;
  }
  
  /** Swap the AttributeTypes at the given locations.
   * @param idx1 The index of the first.
   * @param idx2 The index of the second.
   * @throws ArrayIndexOutOfBoundsException
   */  
  public final void swap(int idx1,int idx2) throws ArrayIndexOutOfBoundsException {
    // implementation note:
    // we must rely on the subclass implementation, which, hopefully does
    // not do any checking. If we used setType, there is a name overlap.
    AttributeType tmp = get(idx1);
    set(idx1, get(idx2));
    set(idx2, tmp);
    // must do this!
    dirty = true;
  }
  
  /** Return the AttributeType currently used as the defaultGeometry property
   * for the FeatureType this factory will create.
   * @return The AttributeType representing the defaultGeometry or null.
   */  
  public final AttributeType getDefaultGeometry() {
    return defaultGeometry;
  }
  
  /** Sets the defaultGeometry of this factory. If the defaultGeometry AttributeType
   * does not exist as an AttributeType within this factory, it is added. This will
   * overwrite the existing defaultGeometry, yet not remove it from the existing
   * AttributeTypes.
   * @param defaultGeometry The AttributeType to use as the defaultGeometry. May
   *        be null.
   */  
  public final void setDefaultGeometry(AttributeType defaultGeometry) {
    // check if Geometry
    if (defaultGeometry != null && ! defaultGeometry.isGeometry()) {
      throw new IllegalArgumentException("Type is not defaultGeometry " 
      + defaultGeometry);
    }
    dirty = true; // do this!
    this.defaultGeometry = defaultGeometry;
    // if the defaultGeometry hasn't been added, add it!
    if (defaultGeometry != null && !contains(defaultGeometry)) {
      addType(defaultGeometry);
    }
  }

  
  /** Get a FeatureType which reflects the state of this factory. Any modifications
   * to the state of the factory (adding, removing, or reordering any AttributeTypes
   * or changing any other properties - isNillable,name,etc.), will cause the factory
   * to "retool" itself. If the factory has not changed since a call to this method,
   * the return value will be the same FeatureType which the previous method
   * returned. Otherwise, a new FeatureType will be created.
   */  
  public final FeatureType getFeatureType() throws SchemaException {
    // we're dirty, recreate the FeatureType
    if (dirty || type == null) {
      
      // no defaultGeometry assigned, search for one.
      if (defaultGeometry == null) {
        for (int i = 0, ii = getAttributeCount(); i < ii; i++) {
          if (get(i).isGeometry()) {
            defaultGeometry = get(i);
            break;
          }
        }
      }
      
      if (name == null || name.trim().length() == 0) {
        throw new SchemaException("Cannot create FeatureType with null or blank name"); 
      }
      
      type = createFeatureType();
      
      // oops, the subclass messed up...
      if (type == null) {
        throw new NullPointerException(getClass().getName() + ".createFeatureType()");
      }
      
      if (isAbstract() && ! type.isAbstract()) {
        throw new RuntimeException("FeatureTypeFactory poorly implemented, " +
          "expected abstract type, received " + type);
      } 
      
      // not dirty anymore.
      dirty = false;
    }
    return type;
  }
  
  public String toString() {
    String types = "";
    for (int i = 0, ii = getAttributeCount(); i < ii; i++) {
      types +=  get(i);
      if (i < ii)
        types += " , ";
    }
    return "FeatureTypeFactory(" + getClass().getName() + ") [ " + types + " ]";
  }
  
  /** Check to see if this factory contains the given AttributeType. The 
   * comparison is done by name.
   * @param type The AttributeType to search for by name.
   * @return true if a like-named AttributeType exists, false otherwise.
   */  
  public final boolean contains(AttributeType type) {
    for (int i = 0, ii = getAttributeCount(); i < ii; i++) {
      if (get(i).getName().equals(type.getName()))
        return true;
    }
    return false;
  }
  
  /**
   * @param type
   */  
  protected void check(AttributeType type) {
    if (contains(type)) {
      throw new IllegalArgumentException("Duplicate AttributeTypes " + type);
    } 
  }
  
  protected final Set getBuiltinTypes() {
    if (builtInTypes == null && !initialized) {
      builtInTypes = new HashSet();
        try {
          builtInTypes.add(newFeatureType(null,"Feature","http://www.opengis.net/gml",true));
          initialized = true;
        } catch (Exception e) {
          throw new RuntimeException(e); 
        }
      addBaseTypes(builtInTypes);
    }
    return builtInTypes;
  }
  
  protected void addBaseTypes(Set types) {
    // base class hook
  }
  
  /**
   * @return
   */  
  protected abstract FeatureType createFeatureType() throws SchemaException;
  
  /**
   * @param type
   * @throws IllegalArgumentException
   */  
  protected abstract void add(AttributeType type)
  throws IllegalArgumentException;
  
  /**
   * @param type
   * @return
   */  
  protected abstract AttributeType remove(AttributeType type);
  
  /**
   * @param idx
   * @param type
   * @throws ArrayIndexOutOfBoundsException
   * @throws IllegalArgumentException
   */  
  protected abstract void add(int idx,AttributeType type)
  throws ArrayIndexOutOfBoundsException,
  IllegalArgumentException;
  
  /**
   * @param idx
   * @throws ArrayIndexOutOfBoundsException
   * @return
   */  
  protected abstract AttributeType remove(int idx)
  throws ArrayIndexOutOfBoundsException;
  
  /**
   * @param idx
   * @throws ArrayIndexOutOfBoundsException
   * @return
   */  
  public abstract AttributeType get(int idx)
  throws ArrayIndexOutOfBoundsException;
  
  /**
   * @param idx
   * @param type
   * @throws ArrayIndexOutOfBoundsException
   * @throws IllegalArgumentException
   * @return
   */  
  protected abstract AttributeType set(int idx,AttributeType type)
  throws ArrayIndexOutOfBoundsException,
  IllegalArgumentException;
  
  /**
   * @return
   */  
  protected abstract int getAttributeCount();
  
}
