/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
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
import java.util.Iterator;

// Geotools dependencies
import org.geotools.util.Cloneable;
import org.geotools.filter.Expression;


/**
 * DOCUMENT ME!
 *
 * @author Ian Turton, CCG
 * @version $Id: GraphicImpl.java,v 1.15 2003/08/28 15:29:42 desruisseaux Exp $
 */
public class GraphicImpl implements Graphic, Cloneable {
    /** The logger for the default core module. */
    private static final java.util.logging.Logger LOGGER = 
            java.util.logging.Logger.getLogger("org.geotools.core");
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();
    private String geometryPropertyName = "";
    private java.util.List externalGraphics = new java.util.ArrayList();
    private java.util.List marks = new java.util.ArrayList();
    private java.util.List symbols = new java.util.ArrayList();
    private Expression rotation = null;
    private Expression size = null;
    private Expression opacity = null;

    /**
     * Creates a new instance of DefaultGraphic
     */
    protected GraphicImpl() {
    }

    /**
     * Convenience method for logging a message with an exception.
     *
     * @param method the name of the calling method 
     * @param message the error message
     * @param exception The exception thrown
     */
    private static void severe(final String method, final String message, 
                               final Exception exception) {
        final java.util.logging.LogRecord record = new java.util.logging.LogRecord(
                                                           java.util.logging.Level.SEVERE, 
                                                           message);
        record.setSourceMethodName(method);
        record.setThrown(exception);
        LOGGER.log(record);
    }

    /**
     * Provides a list of external graphics which can be used to represent this
     * graphic. Each one should be an equivalent representation but in a
     * different format. If none are provided, or if none of the formats are
     * supported, then the list of Marks should be used instead.
     *
     * @return An array of ExternalGraphics objects which should be equivalents
     *         but in different formats.  If null is returned use getMarks
     *         instead.
     */
    public ExternalGraphic[] getExternalGraphics() {
        ExternalGraphic[] ret = null;

        if (externalGraphics.size() > 0) {
            ret = (ExternalGraphic[]) externalGraphics.toArray(
                            new ExternalGraphic[0]);
        }

        return ret;
    }

    public void setExternalGraphics(ExternalGraphic[] externalGraphics) {
        this.externalGraphics.clear();

        for (int i = 0; i < externalGraphics.length; i++) {
            addExternalGraphic(externalGraphics[i]);
        }
    }

    public void addExternalGraphic(ExternalGraphic externalGraphic) {
        externalGraphics.add(externalGraphic);
        symbols.add(externalGraphic);
    }

    /**
     * Provides a list of suitable marks which can be used to represent this
     * graphic. These should only be used if no ExternalGraphic is provided,
     * or if none of the external graphics formats are supported.
     *
     * @return An array of marks to use when displaying this Graphic. By
     *         default, a "square" with 50% gray fill and black outline with a
     *         size of 6 pixels (unless a size is specified) is provided.
     */
    public Mark[] getMarks() {
        Mark[] ret = new Mark[] { new MarkImpl() };

        if (marks.size() > 0) {
            ret = (Mark[]) marks.toArray(new Mark[0]);
        }

        return ret;
    }

    public void setMarks(Mark[] marks) {
        this.marks.clear();

        for (int i = 0; i < marks.length; i++) {
            addMark(marks[i]);
        }
    }

    public void addMark(Mark mark) {
        if (mark == null) {
            return;
        }

        marks.add(mark);
        symbols.add(mark);
        mark.setSize(size);
        mark.setRotation(rotation);
    }

    /**
     * Provides a list of all the symbols which can be used to represent this
     * graphic. A symbol is an ExternalGraphic, Mark or any other object which
     * implements the Symbol interface. These are returned in the order they
     * were set.
     *
     * @return An array of symbols to use when displaying this Graphic. By
     *         default, a "square" with 50% gray fill and black outline with a
     *         size of 6 pixels (unless a size is specified) is provided.
     */
    public Symbol[] getSymbols() {
        Symbol[] ret = new Symbol[] { new MarkImpl() };

        if (symbols.size() > 0) {
            ret = (Symbol[]) symbols.toArray(new Symbol[0]);
        }

        return ret;
    }

    public void setSymbols(Symbol[] symbols) {
        this.symbols.clear();

        for (int i = 0; i < symbols.length; i++) {
            addSymbol(symbols[i]);
        }
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);

        if (symbol instanceof ExternalGraphic) {
            addExternalGraphic((ExternalGraphic) symbol);
        }

        if (symbol instanceof Mark) {
            addMark((Mark) symbol);
        }

        return;
    }

    /**
     * This specifies the level of translucency to use when rendering the graphic.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with
     * 0.0 representing totally transparent and 1.0 representing totally
     * opaque, with a linear scale of translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity. The default value is
     * 1.0 (opaque).
     *
     * @return The opacity of the Graphic, where 0.0 is completely transparent
     *         and 1.0 is completely opaque.
     */
    public Expression getOpacity() {
        return opacity;
    }

    /**
     * This parameter defines the rotation of a graphic in the clockwise
     * direction about its centre point in decimal degrees. The value encoded
     * as a floating point number.
     *
     * @return The angle of rotation in decimal degrees. Negative values
     *         represent counter-clockwise rotation.  The default is 0.0 (no
     *         rotation).
     */
    public Expression getRotation() {
        return rotation;
    }

    /**
     * This paramteter gives the absolute size of the graphic in pixels encoded
     * as a floating point number.
     *
     * <p>
     * The default size of an image format (such as GIFD) is the inherent size
     * of the image.  The default size of a format without an inherent size
     * (such as SVG) is defined to be 16 pixels in height and the
     * corresponding aspect in width.  If a size is specified, the height of
     * the graphic will be scaled to that size and the corresponding aspect
     * will be used for the width.
     * </p>
     *
     * @return The size of the graphic, the default is context specific.
     *         Negative values are not possible.
     */
    public Expression getSize() {
        return size;
    }

    /**
     * Setter for property opacity.
     *
     * @param opacity New value of property opacity.
     */
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    public void setOpacity(double opacity) {
        try {
            this.opacity = filterFactory.createLiteralExpression(
                                   new Double(opacity));
        } catch (org.geotools.filter.IllegalFilterException mfe) {
            severe("setOpacity", "Problem setting Opacity", mfe);
        }
    }

    /**
     * Setter for property rotation.
     *
     * @param rotation New value of property rotation.
     */
    public void setRotation(Expression rotation) {
        this.rotation = rotation;

        java.util.Iterator iter = marks.iterator();

        while (iter.hasNext()) {
            ((MarkImpl) iter.next()).setRotation(rotation);
        }
    }

    public void setRotation(double rotation) {
        try {
            setRotation(filterFactory.createLiteralExpression(
                                new Double(rotation)));
        } catch (org.geotools.filter.IllegalFilterException mfe) {
            severe("setRotation", "Problem setting Rotation", mfe);
        }
    }

    /**
     * Setter for property size.
     *
     * @param size New value of property size.
     */
    public void setSize(Expression size) {
        this.size = size;

        java.util.Iterator iter = marks.iterator();

        while (iter.hasNext()) {
            ((MarkImpl) iter.next()).setSize(size);
        }
    }

    public void setSize(int size) {
        try {
            setSize(filterFactory.createLiteralExpression(new Integer(size)));
        } catch (org.geotools.filter.IllegalFilterException mfe) {
            severe("setSize", "Problem setting Size", mfe);
        }
    }

    public void setGeometryPropertyName(String name) {
        geometryPropertyName = name;
    }

    /**
     * Getter for property geometryPropertyName.
     *
     * @return Value of property geometryPropertyName.
     */
    public java.lang.String getGeometryPropertyName() {
        return geometryPropertyName;
    }
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
    
    /** Creates a deep copy clone. 
     * 
     * TODO: Need to complete the deep copy,
     * currently only shallow copy.
     * 
     * @return The deep copy clone.
     * 
     */
    public Object clone()  {
        GraphicImpl clone;
        try {
            clone = (GraphicImpl) super.clone();
            
            // Because ExternalGraphics and Marks are stored twice
            // and we only want to clone them once, we should use
            // the setter methods to place them in the proper lists
            for (Iterator iter = externalGraphics.iterator(); iter.hasNext();) {
                ExternalGraphic exGraphic = (ExternalGraphic) iter.next();
                clone.addExternalGraphic((ExternalGraphic) exGraphic.clone());
            }
            
            for (Iterator iter = marks.iterator(); iter.hasNext();) {
                Mark mark = (Mark) iter.next();
                clone.addMark((Mark) mark.clone());
            }
            
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }
        return clone;
    }
    
    /** Override of hashcode
     * 
     *  @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        if (geometryPropertyName != null) {
            result = PRIME * result + geometryPropertyName.hashCode();
        }
        if (symbols != null) {
            result = PRIME * result + symbols.hashCode();
        }
        if (rotation != null) {
            result = PRIME * result + rotation.hashCode();
        }
        if (size != null) {
            result = PRIME * result + size.hashCode();
        }
        if (opacity != null) {
            result = PRIME * result + opacity.hashCode();
        }

        return result;
    }

    /** Compares this GraphicImpl with another for equality.
     * 
     *  <p>Two graphics are equal if and only if they both
     *  have the same geometry property name and the same list
     *  of symbols and the same rotation, size and opacity.
     * 
     * @param oth The other GraphicsImpl to compare with.
     * @return True if this is equal to oth according to the above
     * conditions.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth.getClass() != getClass()) {
            return false;
        }

        GraphicImpl other = (GraphicImpl) oth;
        if (this.geometryPropertyName == null) {
            if (other.geometryPropertyName != null) {
                return false;
            }
        } else {
            if (!this.geometryPropertyName.equals(other.geometryPropertyName)) {
                return false;
            }
        }
        if (this.symbols == null) {
            if (other.symbols != null) {
                return false;
            }
        } else {
            if (!this.symbols.equals(other.symbols)) {
                return false;
            }
        }
        if (this.rotation == null) {
            if (other.rotation != null) {
                return false;
            }
        } else {
            if (!this.rotation.equals(other.rotation)) {
                return false;
            }
        }
        if (this.size == null) {
            if (other.size != null) {
                return false;
            }
        } else {
            if (!this.size.equals(other.size)) {
                return false;
            }
        }
        if (this.opacity == null) {
            if (other.opacity != null) {
                return false;
            }
        } else {
            if (!this.opacity.equals(other.opacity)) {
                return false;
            }
        }

        return true;
    }

}