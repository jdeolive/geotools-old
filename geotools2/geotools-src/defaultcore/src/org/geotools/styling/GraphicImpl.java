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

// J2SE dependencies
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.filter.*;


/**
 * @version $Id: GraphicImpl.java,v 1.4 2002/10/23 17:03:58 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class GraphicImpl implements org.geotools.styling.Graphic {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    String geometryPropertyName = "";
    ArrayList externalGraphics = new ArrayList();
    ArrayList marks = new ArrayList();
    ArrayList symbols = new ArrayList();
    private Expression rotation = null;
    private Expression size = null;
    private Expression opacity = null;
    /** Creates a new instance of DefaultGraphic */
    protected GraphicImpl() {
         
    }

    /**
     * Convenience method for logging a message with an exception.
     */
    private static void severe(final String method, final String message, final Exception exception) {
        final LogRecord record = new LogRecord(Level.SEVERE, message);
        record.setSourceMethodName(method);
        record.setThrown(exception);
        LOGGER.log(record);
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
        if (externalGraphics.size() > 0){
            return (ExternalGraphic[]) externalGraphics.toArray(new ExternalGraphic[0]);
        } else {
            return null;
        }
    }
    public void setExternalGraphics(ExternalGraphic[] externalGraphics) {
        this.externalGraphics = new ArrayList();
        for(int i=0;i<externalGraphics.length;i++){
            addExternalGraphic(externalGraphics[i]);
        }
    }
    public void addExternalGraphic(ExternalGraphic g){
        externalGraphics.add(g);
        symbols.add(g);
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
        if (marks.size() > 0){
            return (Mark[]) marks.toArray(new Mark[0]);
        } else {
            return new Mark[]{new MarkImpl()};
        }
    }
    public void setMarks(Mark[] marks) {
        this.marks = new ArrayList();
        for(int i=0;i<marks.length;i++){
            addMark(marks[i]);
        }
    }
    public void addMark(Mark m){
        if (m == null) {
            return;
        }
        marks.add(m);
        symbols.add(m);
        m.setSize(size);
        m.setRotation(rotation);
    }
     
    /** Provides a list of all the symbols which can be used to represent this
     * graphic. A symbol is an ExternalGraphic, Mark or any other object which
     * implements the Symbol interface.
     * These are returned in the order they were set.
     *
     * @return An array of symbols to use when displaying this Graphic.
     * By default, a "square" with 50% gray fill and black outline with a size
     * of 6 pixels (unless a size is specified) is provided.
     */
    public Symbol[] getSymbols() {
        if (symbols.size() > 0){
            return (Symbol[]) symbols.toArray(new Symbol[0]);
        } else {
            return new Symbol[]{new MarkImpl()};
        }
    }
    public void setSymbols(Symbol[] symbols) {
        this.symbols = new ArrayList();
        for(int i=0; i<symbols.length;i++){
            addSymbol(symbols[i]);
        }
    }
    
    
    public void addSymbol(Symbol symbol){
        symbols.add(symbol);
        if ( symbol instanceof ExternalGraphic){
            addExternalGraphic((ExternalGraphic) symbol);
            return;
        }
        if ( symbol instanceof Mark){
            addMark((Mark) symbol);
            return;
        }
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
    public Expression getOpacity() {
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
    public Expression getRotation() {
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
    public Expression getSize() {
        return size;
    }
    
    /**
     * Setter for property opacity.
     * @param opacity New value of property opacity.
     */
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }
    
    public void setOpacity(double opacity){
        try {
            this.opacity = new LiteralExpression(new Double(opacity));
        } catch (org.geotools.filter.IllegalFilterException mfe){
            severe("setOpacity", "Problem setting Opacity", mfe);
        }
    }
    /**
     * Setter for property rotation.
     * @param rotation New value of property rotation.
     */
    public void setRotation(Expression rotation) {
        this.rotation = rotation;
        Iterator i = marks.iterator();
        while (i.hasNext()){
            ((MarkImpl) i.next()).setRotation(rotation);
        }
    }

    public void setRotation(double rotation){
        try {
            setRotation(new LiteralExpression(new Double(rotation)));
        } catch (org.geotools.filter.IllegalFilterException mfe){
            severe("setRotation", "Problem setting Rotation", mfe);
        }
    }

    /**
     * Setter for property size.
     * @param size New value of property size.
     */
    public void setSize(Expression size) {
        this.size = size;
        Iterator i = marks.iterator();
        while (i.hasNext()){
            ((MarkImpl) i.next()).setSize(size);
        }
    }

    public void setSize(int size){
        try {
            setSize(new LiteralExpression(new Integer(size)));
        } catch (org.geotools.filter.IllegalFilterException mfe){
            severe("setSize", "Problem setting Size", mfe);
        }
    }
    
    public void setGeometryPropertyName(String name){
        geometryPropertyName=name;
    }
    
    /** Getter for property geometryPropertyName.
     * @return Value of property geometryPropertyName.
     *
     */
    public java.lang.String getGeometryPropertyName() {
        return geometryPropertyName;
    }
    
}
