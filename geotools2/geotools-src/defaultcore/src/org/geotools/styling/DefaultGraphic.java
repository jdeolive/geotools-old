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
import java.util.*;

/**
 * @version $Id: DefaultGraphic.java,v 1.7 2002/06/07 16:41:44 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class DefaultGraphic implements org.geotools.styling.Graphic {
    ArrayList externalGraphics = new ArrayList();
    ArrayList marks = new ArrayList();
    private static org.apache.log4j.Category _log = 
        org.apache.log4j.Category.getInstance("defaultcore.styling");
    double opacity = 1.0;
    double size = 6;
    double rotation = 0.0;
    
    /** Creates a new instance of DefaultGraphic */
    public DefaultGraphic() {
        
    }
    

    /**
     * Provides a list of external graphics which can be used to represent
     * this graphic.
     * Each one should be an equivalent representation but in a
     * different format.
     * If none are provided, or if none of the formats are supported, then the
     * list of Marks should be used instead.
     *
     * @return An array of ExternalGraphics objects which should be equivalents
     *         but in different formats.  If null is returned use
     *         getMarks instead.
     */
    public ExternalGraphic[] getExternalGraphics() {
        if(externalGraphics.size()>0){
            return (ExternalGraphic[])externalGraphics.toArray(new ExternalGraphic[0]);
        }else{
            return null;
        }
    }
    
    public void addExternalGraphic(ExternalGraphic g){
        externalGraphics.add(g);
    }
    
    /**
     * Provides a list of suitable marks which can be used to represent this
     * graphic.
     * These should only be used if no ExternalGraphic is provided, or if none
     * of the external graphics formats are supported.
     *
     * @return An array of marks to use when displaying this Graphic.
     * By default, a "square" with 50% gray fill and black outline with a size
     * of 6 pixels (unless a size is specified) is provided.
     */
    public Mark[] getMarks() {
        if(marks.size()>0){
            return (Mark[])marks.toArray(new Mark[0]);
        }else{
            return new Mark[]{new DefaultMark()};
        }
    }
    
    public void addMark(DefaultMark m){
        if(m == null) return;
        marks.add(m);
        m.setSize(size);
    }
        
    /**
     * This specifies the level of translucency to use when rendering the
     * graphic.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0
     * with 0.0 representing totally transparent and 1.0 representing totally
     * opaque, with a linear scale of translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.
     * The default value is 1.0 (opaque).
     *
     * @return The opacity of the Graphic, where 0.0 is completely transparent
     * and 1.0 is completely opaque.
     */
    public double getOpacity() {
        return opacity;
    }
    
    /**
     * This parameter defines the rotation of a graphic in the clockwise
     * direction about its centre point in decimal degrees.
     * The value encoded as a floating point number.
     *
     * @return The angle of rotation in decimal degrees. Negative values
     * represent counter-clockwise rotation.  The default is 0.0 (no rotation).
     */
    public double getRotation() {
        return rotation;
    }
    
    /**
     * This paramteter gives the absolute size of the graphic in pixels encoded
     * as a floating point number.<p>
     * The default size of an image format (such as GIFD) is the inherent size
     * of the image.  The default size of a format without an inherent size
     * (such as SVG) is defined to be 16 pixels in height and the corresponding
     * aspect in width.  If a size is specified, the height of the graphic will
     * be scaled to that size and the corresponding aspect will be used for the
     * width.
     *
     * @return The size of the graphic, the default is context specific.
     * Negative values are not possible.
     */
    public double getSize() {
        return size;
    }
    
    /**
     * Setter for property opacity.
     * @param opacity New value of property opacity.
     */
    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }
    
    /**
     * Setter for property rotation.
     * @param rotation New value of property rotation.
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
        Iterator i = marks.iterator();
        while(i.hasNext()){
            ((DefaultMark)i.next()).setRotation(rotation);
        }
    }
    
    /**
     * Setter for property size.
     * @param size New value of property size.
     */
    public void setSize(double size) {
        this.size = size;
        Iterator i = marks.iterator();
        while(i.hasNext()){
            ((DefaultMark)i.next()).setSize(size);
        }
    }
    
}
