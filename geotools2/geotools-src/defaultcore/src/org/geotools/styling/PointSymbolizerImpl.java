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

package org.geotools.styling;

/**
 * @version $Id: PointSymbolizerImpl.java,v 1.3 2003/01/10 13:13:21 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class PointSymbolizerImpl implements PointSymbolizer {
    String geometryPropertyName = null;
    Graphic graphic = new GraphicImpl(); 
    /** Creates a new instance of DefaultPointSymbolizer */
    protected PointSymbolizerImpl() {
    }
    public int hashcode(){
        int key = 0;
        key = graphic.hashCode();
        key = key *13 + geometryPropertyName.hashCode();
        return key;
    }
    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.
     *
     * Geometry types other than inherently point types can be used.
     *
     * The geometryPropertyName is the name of a geometry property in the
     * Feature being styled.  Typically, features only have one geometry so,
     * in general, the need to select one is not required.
     *
     * Note: this moves a little away from the SLD spec which provides an XPath
     * reference to a Geometry object, but does follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     * that should be used.  If null then the default geometry should be used.
     */
    public String geometryPropertyName() {
        return geometryPropertyName;
    }
    public void setGeometryPropertyName(String name) {
        geometryPropertyName = name;
    }
        
    /**
     * Provides the graphical-symbolization parameter to use for the
     * point geometry.
     *
     * @return The Graphic to be used when drawing a point
     */
    public Graphic getGraphic() {
        return graphic;
    }
    
    /**
     * Setter for property graphic.
     * @param graphic New value of property graphic.
     */
    public void setGraphic(org.geotools.styling.Graphic graphic) {
        this.graphic = graphic;
    }
    
}
