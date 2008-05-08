/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Refractions Reserach Inc.
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
 */
package org.geotools.graph.structure;

import java.util.Iterator;

/**
 * Reperesents a component in a directed graph. 
 * 
 * @see DirectedGraph
 * 
 * @author Justin Deoliveira, Refractions Research Inc, jdeolive@refractions.net
 * @source $URL$
 */
public interface DirectedGraphable extends Graphable {
  
  /**
   * Returns other components related through an <B>in</B> relationship. 
   *   
   * @return An iterator over the other directed components related through an 
   * in relationship.
   * 
   * @see Graphable#getRelated()
   */
  public Iterator getInRelated();
  
  /**
   * Returns other components related through an <B>out</B> relationship.
   * 
   * @return An iterator over the other directed components related through an 
   * out relationship.
   * 
   * @see Graphable#getRelated()
   */
  public Iterator getOutRelated();	
}
