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
package org.geotools.graph.build.line;

import org.geotools.graph.build.opt.OptDirectedGraphBuilder;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.line.OptDirectedXYNode;

/**
 * An inmplementation extended from OptDirectedGraphBuilder used to build 
 * optimized directed components for line networks.
 * 
 * @author Justin Deoliveira, Refractions Research Inc, jdeolive@refractions.net
 *
 * @source $URL$
 */
public class OptDirectedLineGraphBuilder extends OptDirectedGraphBuilder {

  /**
   * Returns a node of type OptDirectedXYNode.
   * 
   * @see OptDirectedXYNode
   * @see org.geotools.graph.build.GraphBuilder#buildNode()
   */
  public Node buildNode() {
    return(new OptDirectedXYNode());  
  }
 
}
