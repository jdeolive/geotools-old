/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.feature;


/**
 * An Index is built up around a FeatureTable, using one of the columns in
 * FeatureTable as a comparable reference. An object in a column can be any
 * object, but must either be a java base-type Object (Integer, String,
 * Character, etc.) or implement Comparable.
 * An Index built on such a column will sort its array of object references
 * using FeatureComparator. Implement this to perform more complex Index
 * building.
 *
 * @version $Id: FeatureIndex.java,v 1.3 2002/06/04 15:00:37 loxnard Exp $
 * @author Ray Gallagher
 */
public interface FeatureIndex extends CollectionListener {

   /**
    * Gets an array of references to the rows currently held by this Index.
    * @return all the features referenced by this Index
    */
    public Feature[] getFeatures();
}

