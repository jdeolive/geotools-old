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

// J2SE dependencies
import java.util.ArrayList;


/**
 * A simple DefaultFeatureTypeFactory which stores its Attributes in a list.
 * Oppurtunistic reuse is easy! Simply subclass this and override the method
 * <code>createFeatureType</code> to return whatever type of FeatureType you 
 * want.
 *
 * @version $Id: DefaultFeatureTypeFactory.java,v 1.5 2003/07/22 06:20:44 aaime Exp $
 * @author Ian Schneider, USDA-ARS
 */
public class DefaultFeatureTypeFactory extends FeatureTypeFactory {
  
  private ArrayList attributeTypes = new ArrayList();

  protected void add(AttributeType type) throws IllegalArgumentException {
    attributeTypes.add(type);
  }
  
  protected void add(int idx, AttributeType type) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
    attributeTypes.add(idx,type);
  }
  
  protected FeatureType createFeatureType() throws SchemaException {
    if (isAbstract())
      return createAbstractType();
    return new DefaultFeatureType(getName(), getNamespace(), attributeTypes, getSuperTypes(),getDefaultGeometry());
  }
  
  public AttributeType get(int idx) throws ArrayIndexOutOfBoundsException {
    return (AttributeType) attributeTypes.get(idx);
  }
  
  public int getAttributeCount() {
    return attributeTypes.size();
  }
  
  protected AttributeType remove(int idx) throws ArrayIndexOutOfBoundsException {
    return (AttributeType) attributeTypes.remove(idx);
  }
  
  protected AttributeType remove(AttributeType type) {
    if (attributeTypes.remove(type))
      return type;
    return null;
  }
  
  protected AttributeType set(int idx, AttributeType type) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
    AttributeType former = get(idx);
    attributeTypes.set(idx,type);
    return former;
  }
  
  protected FeatureType createAbstractType() throws SchemaException {
    return new DefaultFeatureType.Abstract(getName(), getNamespace(), attributeTypes, getSuperTypes(), getDefaultGeometry());
  }
  
}
