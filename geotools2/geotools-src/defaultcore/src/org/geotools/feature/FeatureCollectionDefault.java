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

// J2SE dependencies
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

// Geotools dependencies
import org.geotools.data.DataSource;
import org.geotools.data.Extent;
import org.geotools.data.DataSourceException;
import org.geotools.datasource.extents.EnvelopeExtent;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.IllegalFilterException;


/**
 * The default feature collection holds and passes out features promiscuously
 * to requesting clients.  It does not guarantee that features are of a certain
 * type or that they follow a specific schema.
 *
 * @version $Id: FeatureCollectionDefault.java,v 1.12 2003/05/15 18:52:47 cholmesny Exp $
 * @author  James Macgill, CCG<br>
 * @author  Rob Hranac, VFNY<br>
 */
public class FeatureCollectionDefault implements FeatureCollection {
  
  /**
   * The logger for the default core module.
   */
  private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
  
  /**
   * Shared filter factory (for extents filtering)
   */
  private static final FilterFactory factory = FilterFactory.createFilterFactory();
  
  /* Internal feature storage list */
  private Set features = new LinkedHashSet();
  
  /* Internal listener storage list */
  private List listeners = new ArrayList(2);
  
  /* Pointer to the datasource */
  private DataSource data;
  
  /* The currently loaded extent */
  private Extent loadedExtent;
  
  /**
   * Creates a new instance of DefaultFeatureTable
   *
   */
  public FeatureCollectionDefault() {
  }
  
  /** 
   * Creates a new instance of DefaultFeatureTable
   *
   * @param data
   */
  public FeatureCollectionDefault(DataSource data){
    setDataSource(data);
  }
  
  
  
   /* ***********************************************************************
    * Managing data source and extents.
    * ***********************************************************************/
  
  /**
   * Creates a new instance of DefaultFeatureTable.
   *
   * @param data
   */
  public void setDataSource(DataSource data) {
    this.data = data;
  }
  
  /**
   * Creates a new instance of DefaultFeatureTable.
   *
   * @return The datasource that the Feature Collection is currently attached
   *         to.
   */
  public DataSource getDataSource() {
    return this.data;
  }
  
  
  /**
   * Gets the loaded Extent of this FeatureTable.
   * The Extent of current loaded Features in this table.
   */
  public void setExtent(Extent extent) {
    this.loadedExtent = extent;
  }
  
  
  /**
   * Gets the loaded Extent of this FeatureTable.
   * The Extent of current loaded Features in this table.
   */
  public Extent getExtent() {
    return this.loadedExtent;
  }
  
   /**
     * Gets the bounding box for the features in this feature collection.
     *
     * @return the envelope of the default geometries contained by this feature
     * collection.
     * @task TODO: do not recalculate each time.  Keep the calculated envelope
     * in the object.  Would be nice to get rid of extent stuff first, so 
     * it doesn't lead to confusion as to what's going on.
     */
    public Envelope getBoundingBox() {
	Envelope bounding = new Envelope();
	for (Iterator i = features.iterator(); i.hasNext();){
	    Geometry geom = ((Feature)i.next()).getDefaultGeometry();
	    bounding.expandToInclude(geom.getEnvelopeInternal());
	}
	return bounding;
    }
    


    /* ***********************************************************************
     * Managing collection listeners.
     * ***********************************************************************/
  /**
   * Adds a listener for table events.
   */
  public void addListener(CollectionListener spy) {
    listeners.add(spy);
  }
  
  /**
   * Removes a listener for table events.
   */
  public void removeListener(CollectionListener spy) {
    listeners.remove(spy);
  }
  
  protected void fireChange() {
    CollectionEvent ce = new CollectionEvent(this);
    for (int i = 0, ii = listeners.size(); i < ii; i++) {
      ((CollectionListener) listeners.get(i)).collectionChanged(ce);
    }
  }
  
  
    /* ***********************************************************************
     * Managing features via the datasource.
     * ***********************************************************************/
  /**
   * Gets the features in the datasource inside the loadedExtent.
   * Will not trigger a datasourceload.
   * Functionally equivalent to getFeatures(getLoadedExtent());
   *
   * @see #getfeatures(Extent ex)
   */
  public Feature[] getFeatures() {
    Feature[] list = (Feature[]) features.toArray(new Feature[features.size()]);
    return list;
  }
  
  
  /**
   * Gets the features in the datasource inside the Extent ex.
   * This may trigger a load on the datasource.
   */
  public Feature[] getFeatures(Extent ex) throws DataSourceException {
    try{
      // TODO: 2
      // Replace this idiom with a loadedExtent = loadedExtent.or(extent)
      //  idiom.  I think?
      Extent toLoad[];
      if (loadedExtent != null){
        toLoad = loadedExtent.difference(ex);
      }
      else {
        toLoad = new Extent[]{ex};
      }
      
      for (int i = 0; i < toLoad.length; i++){
        //TODO: move this code to its own method?
        if (toLoad[i] != null){
          if (data != null){
            LOGGER.finer("loading " + i);
            GeometryFilter gf =
            factory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
            LiteralExpression right =
            factory.createBBoxExpression(((EnvelopeExtent)toLoad[i]).getBounds());
            gf.addRightGeometry(right);
            data.getFeatures(this,gf);
          }
          if (loadedExtent == null){
            loadedExtent = toLoad[i];
          }
          else {
            loadedExtent = loadedExtent.combine(toLoad[i]);
          }
        }
      }
      LOGGER.finer("calling getfeatures");
      return getFeatures();
    }
    catch(IllegalFilterException ife){
      DataSourceException e = new DataSourceException(ife.toString());
      e.initCause(ife);
      throw e;
    }
  }
  
  
  
  /**
   * Removes the features from this FeatureTable which fall into the
   * specified extent, notifying TableChangedListeners that the table has
   * changed.
   * @param ex The extent defining which features to remove
   */
  public void removeFeatures(Extent ex) {
    //TODO: remove the features
  }
  
  /**
   * Removes the features from this FeatureTable, notifying
   * TableChangedListeners that the table has changed.
   * @param features The Features to remove
   */
  public void removeFeatures(final Feature[] f) {
    boolean change = false;
    for (int i = 0; i < f.length; i++) {
      change |= features.remove(f[i]);
    }
    if (change)
      fireChange();
  }
  
  /**
   * Adds the given List of Features to this FeatureTable.
   *
   * @param features The List of Features to add
   */
  public void addFeatures(final Feature[] f) {
    boolean change = false;
    for (int i = 0; i < f.length; i++) {
      change |= features.add(f[i]);
    }
    if (change)
      fireChange();
  }
  
  public void addFeatures(List features){
    addAll(features);
  }
  
  /** Ensures that this collection contains the specified element (optional
   * operation).  Returns <tt>true</tt> if this collection changed as a
   * result of the call.  (Returns <tt>false</tt> if this collection does
   * not permit duplicates and already contains the specified element.)<p>
   *
   * Collections that support this operation may place limitations on what
   * elements may be added to this collection.  In particular, some
   * collections will refuse to add <tt>null</tt> elements, and others will
   * impose restrictions on the type of elements that may be added.
   * Collection classes should clearly specify in their documentation any
   * restrictions on what elements may be added.<p>
   *
   * If a collection refuses to add a particular element for any reason
   * other than that it already contains the element, it <i>must</i> throw
   * an exception (rather than returning <tt>false</tt>).  This preserves
   * the invariant that a collection always contains the specified element
   * after this call returns.
   *
   * @param o element whose presence in this collection is to be ensured.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException <tt>add</tt> is not supported by
   *         this collection.
   * @throws ClassCastException class of the specified element prevents it
   *         from being added to this collection.
   * @throws NullPointerException if the specified element is null and this
   *         collection does not support null elements.
   * @throws IllegalArgumentException some aspect of this element prevents
   *         it from being added to this collection.
   *
   */
  public boolean add(Object o) {
    // This cast is neccessary to keep with the contract of Set!
    boolean changed = features.add( (Feature) o);
    if (changed)
      fireChange();
    return changed;
  }
  
  /** Adds all of the elements in the specified collection to this collection
   * (optional operation).  The behavior of this operation is undefined if
   * the specified collection is modified while the operation is in progress.
   * (This implies that the behavior of this call is undefined if the
   * specified collection is this collection, and this collection is
   * nonempty.)
   *
   * @param c elements to be inserted into this collection.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException if this collection does not
   *         support the <tt>addAll</tt> method.
   * @throws ClassCastException if the class of an element of the specified
   * 	       collection prevents it from being added to this collection.
   * @throws NullPointerException if the specified collection contains one
   *         or more null elements and this collection does not support null
   *         elements, or if the specified collection is <tt>null</tt>.
   * @throws IllegalArgumentException some aspect of an element of the
   * 	       specified collection prevents it from being added to this
   * 	       collection.
   * @see #add(Object)
   *
   */
  public boolean addAll(Collection c) {
    boolean changed = false;
    Iterator f = c.iterator();
    while (f.hasNext()) {
      changed |= features.add( (Feature) f.next() );
    }
    if (changed)
      fireChange();
    return changed;
  }
  
  /** Removes all of the elements from this collection (optional operation).
   * This collection will be empty after this method returns unless it
   * throws an exception.
   *
   * @throws UnsupportedOperationException if the <tt>clear</tt> method is
   *         not supported by this collection.
   *
   */
  public void clear() {
    features.clear();
    fireChange();
  }
  
  /** Returns <tt>true</tt> if this collection contains the specified
   * element.  More formally, returns <tt>true</tt> if and only if this
   * collection contains at least one element <tt>e</tt> such that
   * <tt>(o==null ? e==null : o.equals(e))</tt>.
   *
   * @param o element whose presence in this collection is to be tested.
   * @return <tt>true</tt> if this collection contains the specified
   *         element
   * @throws ClassCastException if the type of the specified element
   * 	       is incompatible with this collection (optional).
   * @throws NullPointerException if the specified element is null and this
   *         collection does not support null elements (optional).
   *
   */
  public boolean contains(Object o) {
    
    // The contract of Set doesn't say we have to cast here, but I think its
    // useful for client sanity to get a ClassCastException and not just a
    // false.
    return features.contains( (Feature) o);
  }
  
  /** Returns <tt>true</tt> if this collection contains all of the elements
   * in the specified collection.
   *
   * @param  c collection to be checked for containment in this collection.
   * @return <tt>true</tt> if this collection contains all of the elements
   * 	       in the specified collection
   * @throws ClassCastException if the types of one or more elements
   *         in the specified collection are incompatible with this
   *         collection (optional).
   * @throws NullPointerException if the specified collection contains one
   *         or more null elements and this collection does not support null
   *         elements (optional).
   * @throws NullPointerException if the specified collection is
   *         <tt>null</tt>.
   * @see    #contains(Object)
   *
   */
  public boolean containsAll(Collection c) {
    boolean contains = true;
    Iterator f = c.iterator();
    while (f.hasNext())
      contains &= contains( f.next() );
    return contains;
  }
  
  /** Returns <tt>true</tt> if this collection contains no elements.
   *
   * @return <tt>true</tt> if this collection contains no elements
   *
   */
  public boolean isEmpty() {
    return features.isEmpty();
  }
  
  /** Returns an iterator over the elements in this collection.  There are no
   * guarantees concerning the order in which the elements are returned
   * (unless this collection is an instance of some class that provides a
   * guarantee).
   *
   * @return an <tt>Iterator</tt> over the elements in this collection
   *
   */
  public Iterator iterator() {
    return features.iterator();
  }
  
  /** Removes a single instance of the specified element from this
   * collection, if it is present (optional operation).  More formally,
   * removes an element <tt>e</tt> such that <tt>(o==null ?  e==null :
   * o.equals(e))</tt>, if this collection contains one or more such
   * elements.  Returns true if this collection contained the specified
   * element (or equivalently, if this collection changed as a result of the
   * call).
   *
   * @param o element to be removed from this collection, if present.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   *
   * @throws ClassCastException if the type of the specified element
   * 	       is incompatible with this collection (optional).
   * @throws NullPointerException if the specified element is null and this
   *         collection does not support null elements (optional).
   * @throws UnsupportedOperationException remove is not supported by this
   *         collection.
   *
   */
  public boolean remove(Object o) {
    boolean changed = features.remove( (Feature) o );
    if (changed)
      fireChange();
    return changed;
  }
  
  /**
   * Removes all this collection's elements that are also contained in the
   * specified collection (optional operation).  After this call returns,
   * this collection will contain no elements in common with the specified
   * collection.
   *
   * @param c elements to be removed from this collection.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
   * 	       is not supported by this collection.
   * @throws ClassCastException if the types of one or more elements
   *         in this collection are incompatible with the specified
   *         collection (optional).
   * @throws NullPointerException if this collection contains one or more
   *         null elements and the specified collection does not support
   *         null elements (optional).
   * @throws NullPointerException if the specified collection is
   *         <tt>null</tt>.
   * @see #remove(Object)
   * @see #contains(Object)
   *
   */
  public boolean removeAll(Collection c) {
    boolean changed = false;
    Iterator f = c.iterator();
    while (f.hasNext())
      changed |= features.remove( f.next() );
    if (changed)
      fireChange();
    return changed;
  }
  
  /** Retains only the elements in this collection that are contained in the
   * specified collection (optional operation).  In other words, removes from
   * this collection all of its elements that are not contained in the
   * specified collection.
   *
   * @param c elements to be retained in this collection.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
   * 	       is not supported by this Collection.
   * @throws ClassCastException if the types of one or more elements
   *         in this collection are incompatible with the specified
   *         collection (optional).
   * @throws NullPointerException if this collection contains one or more
   *         null elements and the specified collection does not support null
   *         elements (optional).
   * @throws NullPointerException if the specified collection is
   *         <tt>null</tt>.
   * @see #remove(Object)
   * @see #contains(Object)
   *
   */
  public boolean retainAll(Collection c) {
    boolean modified = features.retainAll(c);
    if (modified)
      fireChange();
    return modified;
  }
  
  /** Returns the number of elements in this collection.  If this collection
   * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
   * <tt>Integer.MAX_VALUE</tt>.
   *
   * @return the number of elements in this collection
   *
   */
  public int size() {
    return features.size();
  }
  
  /** Returns an array containing all of the elements in this collection.  If
   * the collection makes any guarantees as to what order its elements are
   * returned by its iterator, this method must return the elements in the
   * same order.<p>
   *
   * The returned array will be "safe" in that no references to it are
   * maintained by this collection.  (In other words, this method must
   * allocate a new array even if this collection is backed by an array).
   * The caller is thus free to modify the returned array.<p>
   *
   * This method acts as bridge between array-based and collection-based
   * APIs.
   *
   * @return an array containing all of the elements in this collection
   *
   */
  public Object[] toArray() {
    return features.toArray();
  }
  
  /** Returns an array containing all of the elements in this collection;
   * the runtime type of the returned array is that of the specified array.
   * If the collection fits in the specified array, it is returned therein.
   * Otherwise, a new array is allocated with the runtime type of the
   * specified array and the size of this collection.<p>
   *
   * If this collection fits in the specified array with room to spare
   * (i.e., the array has more elements than this collection), the element
   * in the array immediately following the end of the collection is set to
   * <tt>null</tt>.  This is useful in determining the length of this
   * collection <i>only</i> if the caller knows that this collection does
   * not contain any <tt>null</tt> elements.)<p>
   *
   * If this collection makes any guarantees as to what order its elements
   * are returned by its iterator, this method must return the elements in
   * the same order.<p>
   *
   * Like the <tt>toArray</tt> method, this method acts as bridge between
   * array-based and collection-based APIs.  Further, this method allows
   * precise control over the runtime type of the output array, and may,
   * under certain circumstances, be used to save allocation costs<p>
   *
   * Suppose <tt>l</tt> is a <tt>List</tt> known to contain only strings.
   * The following code can be used to dump the list into a newly allocated
   * array of <tt>String</tt>:
   *
   * <pre>
   *     String[] x = (String[]) v.toArray(new String[0]);
   * </pre><p>
   *
   * Note that <tt>toArray(new Object[0])</tt> is identical in function to
   * <tt>toArray()</tt>.
   *
   * @param a the array into which the elements of this collection are to be
   *        stored, if it is big enough; otherwise, a new array of the same
   *        runtime type is allocated for this purpose.
   * @return an array containing the elements of this collection
   *
   * @throws ArrayStoreException the runtime type of the specified array is
   *         not a supertype of the runtime type of every element in this
   *         collection.
   * @throws NullPointerException if the specified array is <tt>null</tt>.
   *
   */
  public Object[] toArray(Object[] a) {
    return features.toArray(a);
  }
  
 
}
