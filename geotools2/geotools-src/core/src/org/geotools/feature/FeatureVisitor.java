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
 * An interface for classes that want to perform operations on a Feature
 * hiarachy.
 * It forms part of a GoF Visitor Patern implementation.
 * A call to feature.accept(FeatureVisitor) will result in a call to one
 * of the methods in this interface. The responsibility for traversing
 * sub features is intended to lie with the visitor (this is unusual, but
 * permited under the Visitor pattern).
 *
 * A typical use would be to transcribe a feature into a specific format,
 * e.g. XML or SQL.  Alternativly it may be to extract specific infomration
 * from the Feature strucure, for example a list of all bboxes.
 *
 * @version $Id: FeatureVisitor.java,v 1.1 2002/11/25 23:10:14 robhranac Exp $
 * @author James Macgill
 */
public interface FeatureVisitor {
    
    /**
     * Called when accept is called on an AbstractFeature.
     * As it is imposible to create an instance of AbstractFeature this should
     * never happen.  If it does it means that a subclass of AbstractFeature
     * has failed to implement accept(FeatureVisitor) correctly.
     * Implementers of this method should probaly log a warning.
     * @param feature The feature to visit
     */
    void visit(Feature feature);
        
}
