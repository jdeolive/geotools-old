/*
 *
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
 *
 * Contacts:
 *     UNITED KINGDOM: Ian Turton, ian@geog.leeds.ac.uk
 *
 * TextSymbolizer.java
 *
 * Created on 03 July 2002, 10:37
 */

package org.geotools.styling;
import org.geotools.filter.Expression;
/**
 * $Id: TextSymbolizer.java,v 1.2 2002/07/05 15:28:31 ianturton Exp $
 * @author  iant
 */

public interface TextSymbolizer extends Symbolizer {
    /**
     * returns the label expression
     */
    public Expression getLabel();
    /** 
     * returns a device independent Font object that is tobe used to render the 
     * label
     */
    public Font[] getFonts();
    /**
     * A pointPlacement specifies how a text element should be rendered relative
     * to its geometric point
     */
    public LabelPlacement getLabelPlacement();
    
    /** A halo fills an extended area outside the glyphs of a rendered text label 
     * to make the label easier to read over a background.
     */
    public Halo getHalo();
    
    /** return the fill to be used to fill the text when rendered.
     */
    public Fill getFill();
    
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
     *  that should be used.  If null then the default geometry should be used.
     */
    public String geometryPropertyName();    
}
