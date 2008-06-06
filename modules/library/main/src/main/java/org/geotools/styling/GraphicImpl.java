/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.styling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.resources.Utilities;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.util.Cloneable;


/**
 * Direct implementation of Graphic.
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class GraphicImpl implements Graphic,
    Cloneable {
    /** The logger for the default core module. */
    //private static final java.util.logging.Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.core");
    private FilterFactory filterFactory;
    private String geometryPropertyName = "";
    private java.util.List<ExternalGraphic> externalGraphics = new java.util.ArrayList<ExternalGraphic> ();
    private java.util.List<Mark> marks = new java.util.ArrayList<Mark>();
    private java.util.List<Symbol> symbols = new java.util.ArrayList<Symbol>();
    private Expression rotation = null;
    private Expression size = null;
    private Displacement displacement = null;
    private Expression opacity = null;

    /**
     * Creates a new instance of DefaultGraphic
     */
    protected GraphicImpl() {
        this( CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints())); 
    }

    public GraphicImpl(FilterFactory factory) {
        filterFactory = factory;
    }

    public void setFilterFactory(FilterFactory factory) {
        filterFactory = factory;
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
            ret = externalGraphics.toArray(new ExternalGraphic[0]);
        }

        return ret;
    }

    public void setExternalGraphics(ExternalGraphic[] externalGraphics) {
        this.externalGraphics.clear();

        for (int i = 0; i < symbols.size();) {
            Object symbol = symbols.get(i);

            if (symbol instanceof ExternalGraphic) {
                symbols.remove(i);
            } else {
                i++;
            }
        }

        if (externalGraphics != null) {
            for (int i = 0; i < externalGraphics.length; i++) {
                addExternalGraphic(externalGraphics[i]);
            }
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
        Mark[] ret = new Mark[0];

        if (marks.size() > 0) {
            ret = marks.toArray(new Mark[0]);
        }

        return ret;
    }

    public void setMarks(Mark[] marks) {
        this.marks.clear();

        for (int i = 0; i < symbols.size();) {
            Object symbol = symbols.get(i);

            if (symbol instanceof Mark) {
                symbols.remove(i);
            } else {
                i++;
            }
        }

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
     * graphic
     * <p>
     * A symbol is an ExternalGraphic, Mark or any other object which
     * implements the Symbol interface. These are returned in the order they
     * were set.
     * <p>
     * This class operates as a "view" on getMarks() and getExternalGraphics()
     * with the added magic that if nothing has been set ever a single default
     * MarkImpl will be provided. This default will not effect the internal
     * state it is only there as a sensible default for rendering.
     *
     * @return An array of symbols to use when displaying this Graphic. By
     *         default, a "square" with 50% gray fill and black outline with a
     *         size of 6 pixels (unless a size is specified) is provided.
     */
    public Symbol[] getSymbols() {
        Symbol[] ret = null;

        if (symbols.size() > 0) {
            ret = symbols.toArray(new Symbol[symbols.size()]);
        } else {
            ret = new Symbol[] { new MarkImpl() };
        }

        return ret;
    }

    public void setSymbols(Symbol[] symbols) {
        this.symbols.clear();

        if (symbols != null) {
            for (int i = 0; i < symbols.length; i++) {
                addSymbol(symbols[i]);
            }
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

    public Displacement getDisplacement() {
        return displacement;
    }

    public void setDisplacement(Displacement offset) {
        this.displacement = offset;
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
        setOpacity(filterFactory.literal(opacity));
    }

    /**
     * Setter for property rotation.
     *
     * @param rotation New value of property rotation.
     */
    public void setRotation(Expression rotation) {
        this.rotation = rotation;

        java.util.Iterator<Mark> iter = marks.iterator();

        while (iter.hasNext()) {
            ((MarkImpl) iter.next()).setRotation(rotation);
        }
    }

    public void setRotation(double rotation) {
        setRotation(filterFactory.literal(rotation));
    }

    /**
     * Setter for property size.
     *
     * @param size New value of property size.
     */
    public void setSize(Expression size) {
        this.size = size;

        java.util.Iterator<Mark> iter = marks.iterator();

        while (iter.hasNext()) {
            ((MarkImpl) iter.next()).setSize(size);
        }
    }

    public void setSize(int size) {
        setSize(filterFactory.literal(size));
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

    /**
     * Creates a deep copy clone.
     *
     * @return The deep copy clone.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Object clone() {
        GraphicImpl clone;

        try {
            clone = (GraphicImpl) super.clone();
            clone.marks = new ArrayList<Mark>();
            clone.externalGraphics = new ArrayList<ExternalGraphic>();
            clone.symbols = new ArrayList<Symbol>();

            // Because ExternalGraphics and Marks are stored twice
            // and we only want to clone them once, we should use
            // the setter methods to place them in the proper lists
            for (Iterator<ExternalGraphic> iter = externalGraphics.iterator(); iter.hasNext();) {
                ExternalGraphic exGraphic = iter.next();
                clone.addExternalGraphic((ExternalGraphic) ((Cloneable) exGraphic)
                    .clone());
            }

            for (Iterator<Mark> iter = marks.iterator(); iter.hasNext();) {
                Mark mark = iter.next();
                clone.addMark((Mark) ((Cloneable) mark).clone());
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }

        return clone;
    }

    /**
     * Override of hashcode
     *
     * @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (geometryPropertyName != null) {
            result = (PRIME * result) + geometryPropertyName.hashCode();
        }

        if (symbols != null) {
            result = (PRIME * result) + symbols.hashCode();
        }

        if (rotation != null) {
            result = (PRIME * result) + rotation.hashCode();
        }

        if (size != null) {
            result = (PRIME * result) + size.hashCode();
        }

        if (opacity != null) {
            result = (PRIME * result) + opacity.hashCode();
        }

        return result;
    }

    /**
     * Compares this GraphicImpl with another for equality.
     * 
     * <p>
     * Two graphics are equal if and only if they both have the same geometry
     * property name and the same list of symbols and the same rotation, size
     * and opacity.
     * </p>
     *
     * @param oth The other GraphicsImpl to compare with.
     *
     * @return True if this is equal to oth according to the above conditions.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof GraphicImpl) {
            GraphicImpl other = (GraphicImpl) oth;

            return Utilities.equals(this.geometryPropertyName,
                other.geometryPropertyName)
            && Utilities.equals(this.size, other.size)
            && Utilities.equals(this.rotation, other.rotation)
            && Utilities.equals(this.opacity, other.opacity)
            &&    Arrays.equals(this.getMarks(), other.getMarks() )
            &&    Arrays.equals( this.getExternalGraphics(), other.getExternalGraphics() )
            &&    Arrays.equals( this.getSymbols(), other.getSymbols() );                    
        }

        return false;
    }
}
