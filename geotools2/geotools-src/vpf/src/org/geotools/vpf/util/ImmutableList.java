/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package org.geotools.vpf.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.geotools.vpf.util.ImmutableIterator;
import org.geotools.vpf.util.ImmutableListIterator;

/**
 * Class ImmutableList.java is responsible for 
 *
 * <p>
 * Created: Fri Jan 24 13:19:22 2003
 * </p>
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */

public class ImmutableList implements List {

  protected List list = null;

  public ImmutableList(List l) 
  {
    list = l;
  }
  
  // Implementation of java.util.List

  /**
   * Method <code>hashCode</code> is used to perform 
   * @return an <code>int</code> value
   */
  public int hashCode()
  {
    return list.hashCode();
  }

  /**
   * Method <code>equals</code> is used to perform 
   * @param object an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean equals(Object object)
  {
    return list.equals(object);
  }

  /**
   * Method <code>indexOf</code> is used to perform 
   * @param object an <code>Object</code> value
   * @return an <code>int</code> value
   */
  public int indexOf(Object object)
  {
    return list.indexOf(object);
  }

  /**
   * Method <code>lastIndexOf</code> is used to perform 
   * @param object an <code>Object</code> value
   * @return an <code>int</code> value
   */
  public int lastIndexOf(Object object)
  {
    return list.lastIndexOf(object);
  }

  /**
   * Method <code>addAll</code> is used to perform 
   * @param collection a <code>Collection</code> value
   * @return a <code>boolean</code> value
   */
  public boolean addAll(Collection collection)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>addAll</code> is used to perform 
   * @param n an <code>int</code> value
   * @param collection a <code>Collection</code> value
   * @return a <code>boolean</code> value
   */
  public boolean addAll(int n, Collection collection)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>add</code> is used to perform 
   * @param object an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean add(Object object)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>add</code> is used to perform 
   * @param n an <code>int</code> value
   * @param object an <code>Object</code> value
   */
  public void add(int n, Object object)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>get</code> is used to perform 
   * @param n an <code>int</code> value
   * @return an <code>Object</code> value
   */
  public Object get(int n)
  {
    return list.get(n);
  }

  /**
   * Method <code>contains</code> is used to perform 
   * @param object an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean contains(Object object)
  {
    return list.contains(object);
  }

  /**
   * Method <code>size</code> is used to perform 
   * @return an <code>int</code> value
   */
  public int size()
  {
    return list.size();
  }

  /**
   * Method <code>toArray</code> is used to perform 
   * @return an <code>Object[]</code> value
   */
  public Object[] toArray()
  {
    return list.toArray();
  }

  /**
   * Method <code>toArray</code> is used to perform 
   * @param objectArray an <code>Object[]</code> value
   * @return an <code>Object[]</code> value
   */
  public Object[] toArray(Object[] objectArray)
  {
    return list.toArray(objectArray);
  }

  /**
   * Method <code>iterator</code> is used to perform 
   * @return an <code>Iterator</code> value
   */
  public Iterator iterator()
  {
    return new ImmutableIterator(list.iterator());
  }

  /**
   * Method <code>remove</code> is used to perform 
   * @param object an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean remove(Object object)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>remove</code> is used to perform 
   * @param n an <code>int</code> value
   * @return an <code>Object</code> value
   */
  public Object remove(int n)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>clear</code> is used to perform 
   */
  public void clear()
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>isEmpty</code> is used to perform 
   * @return a <code>boolean</code> value
   */
  public boolean isEmpty()
  {
    return list.isEmpty();
  }

  /**
   * Method <code>set</code> is used to perform 
   * @param n an <code>int</code> value
   * @param object an <code>Object</code> value
   * @return an <code>Object</code> value
   */
  public Object set(int n, Object object)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>containsAll</code> is used to perform 
   * @param collection a <code>Collection</code> value
   * @return a <code>boolean</code> value
   */
  public boolean containsAll(Collection collection)
  {
    return list.containsAll(collection);
  }

  /**
   * Method <code>removeAll</code> is used to perform 
   * @param collection a <code>Collection</code> value
   * @return a <code>boolean</code> value
   */
  public boolean removeAll(Collection collection)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>retainAll</code> is used to perform 
   * @param collection a <code>Collection</code> value
   * @return a <code>boolean</code> value
   */
  public boolean retainAll(Collection collection)
  {
    throw new
      UnsupportedOperationException("It is immutable list implementation");
  }

  /**
   * Method <code>subList</code> is used to perform 
   * @param n an <code>int</code> value
   * @param n1 an <code>int</code> value
   * @return a <code>List</code> value
   */
  public List subList(int n, int n1)
  {
    return new ImmutableList(list.subList(n, n1));
  }

  /**
   * Method <code>listIterator</code> is used to perform 
   * @param n an <code>int</code> value
   * @return a <code>ListIterator</code> value
   */
  public ListIterator listIterator(int n)
  {
    return new ImmutableListIterator(list.listIterator(n));
  }

  /**
   * Method <code>listIterator</code> is used to perform 
   * @return a <code>ListIterator</code> value
   */
  public ListIterator listIterator()
  {
    return new ImmutableListIterator(list.listIterator());
  }
  
}// ImmutableList
