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

import com.vividsolutions.jts.geom.*;
import java.util.StringTokenizer;
import org.geotools.filter.*;
/**
 * @version $Id: DefaultMark.java,v 1.7 2002/07/05 20:21:11 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class DefaultMark implements Mark {
    private static org.apache.log4j.Logger _log = 
        org.apache.log4j.Logger.getLogger(DefaultMark.class);    
    Fill fill = new DefaultFill();
    Stroke stroke = new DefaultStroke();
    
    //Polygon shape;
    private Expression wellKnownName = null;
    private Expression rotation = null;
    private Expression size = null;


    private GeometryFactory geometryFactory = new GeometryFactory();
    /** Creates a new instance of DefaultMark */
    public DefaultMark() {
        _log.info("creating defaultMark");
        try{
            wellKnownName = new ExpressionLiteral("square");
            size = new ExpressionLiteral(new Integer(6));
            rotation = new ExpressionLiteral(new Double(0.0));
        } catch (IllegalFilterException ife){
            _log.fatal("Failed to build default mark: "+ife);
            System.err.println("Failed to build default mark: "+ife);
        }
    }
    public DefaultMark(String name){
        _log.info("creating "+name+" type mark");
        setWellKnownName(name);
    }
    
    
   
    
    /**
     * This parameter defines which fill style to use when rendering the Mark.
     *
     * @return the Fill definition to use when rendering the Mark.
     */
    public Fill getFill() {
        return fill;
    }
    
    /**
     * This paramterer defines which stroke style should be used when
     * rendering the Mark.
     *
     * @return The Stroke definition to use when rendering the Mark.
     */
    public Stroke getStroke() {
        return stroke;
    }
    
    /**
     * This parameter gives the well-known name of the shape of the mark.<br>
     * Allowed names include at least "square", "circle", "triangle", "star",
     * "cross" and "x" though renderers may draw a different symbol instead
     * if they don't have a shape for all of these.<br>
     *
     * @return The well-known name of a shape.  The default value is "square".
     */
    public Expression getWellKnownName() {
        return wellKnownName;
    }
    
    /**
     * Setter for property fill.
     * @param fill New value of property fill.
     */
    public void setFill(org.geotools.styling.Fill fill) {
        this.fill = fill;
    }
    
    /**
     * Setter for property stroke.
     * @param stroke New value of property stroke.
     */
    public void setStroke(org.geotools.styling.Stroke stroke) {
        this.stroke = stroke;
    }
    
    public void setSize(Expression size){
        this.size = size;
    }
    public void setSize(int size){
        try{
            setSize(new ExpressionLiteral(new Integer(size)));
        } catch (org.geotools.filter.IllegalFilterException mfe){
            _log.fatal("Problem setting Opacity",mfe);
        }
    }
    /**
     * Setter for property wellKnownName.
     * @param wellKnownName New value of property wellKnownName.
     */
    public void setWellKnownName(Expression wellKnownName) {
        _log.debug("setting WellKnowName");
        this.wellKnownName = wellKnownName;
    }
    public void setWellKnownName(String name){
        try{
            setWellKnownName(new ExpressionLiteral(name));
        } catch (org.geotools.filter.IllegalFilterException mfe){
            _log.fatal("Problem setting Rotation",mfe);
        }
    }
    public void setRotation(Expression rotation) {
        this.rotation = rotation;
    }
    public void setRotation(double rotation){
        try{
            setRotation(new ExpressionLiteral(new Double(rotation))); 
        } catch (org.geotools.filter.IllegalFilterException mfe){
            _log.fatal("Problem setting Rotation",mfe);
        }
    }
    /**
     * Getter for property size.
     * @return Value of property size.
     */
    public Expression getSize() {
        return size;
    }
    
    /**
     * Getter for property rotation.
     * @return Value of property rotation.
     */
    public Expression getRotation() {
        return rotation;
    }
    private static String[] WellKnownNames = {"Square","Circle","Cross","Triangle","Star","X","Arrow"};
}
