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


// Geotools dependencies
import org.geotools.util.Cloneable;
import org.geotools.util.EqualsUtils;
/**
 * Provides a Java representation of an SLD TextSymbolizer that
 * defines how text symbols should be rendered.
 * 
 * @version $Id: TextSymbolizerImpl.java,v 1.17 2003/09/06 04:52:31 seangeo Exp $
 * @author Ian Turton, CCG
 */
public class TextSymbolizerImpl implements TextSymbolizer, Cloneable {
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();
    private Fill fill;
    private java.util.List fonts = new java.util.ArrayList();
    private Halo halo;
    private LabelPlacement labelPlacement;
    private String geometryPropertyName = null;
    private org.geotools.filter.Expression label = null;

    /** Creates a new instance of DefaultTextSymbolizer */
    protected TextSymbolizerImpl() {
        fill = new FillImpl();
        fill.setColor(filterFactory.createLiteralExpression("#000000")); // default text fill is black
        halo = null;
        labelPlacement = new PointPlacementImpl();
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

    /**
     * Returns the fill to be used to fill the text when rendered.
     * @return The fill to be used.
     */
    public Fill getFill() {
        return fill;
    }

    /** Setter for property fill.
     * @param fill New value of property fill.
     */
    public void setFill(org.geotools.styling.Fill fill) {
        this.fill = fill;
    }

    /**
     * Returns a device independent Font object that is to be used to render
     * the label.
     * @return Device independent Font object to be used to render the label.
     */
    public Font[] getFonts() {
        if (fonts.size() == 0) {
            fonts.add(new FontImpl());
        }

        return (Font[]) fonts.toArray(new Font[] {  });
    }

    /**
     * Setter for property font.
     * @param font New value of property font.
     */
    public void addFont(org.geotools.styling.Font font) {
        this.fonts.add(font);
    }

    /** Sets the list of fonts in the TextSymbolizer to the
     *  provided array of Fonts.
     * 
     *  @param fonts The array of fonts to use in the symbolizer.
     */
    public void setFonts(Font[] fonts) {
        this.fonts.clear();
        for (int i = 0; i < fonts.length; i++) {
            addFont(fonts[i]);
        }
    }

    /**
     * A halo fills an extended area outside the glyphs of a rendered text
     * label to make the label easier to read over a background.
     */
    public Halo getHalo() {
        return halo;
    }

    /**
     * Setter for property halo.
     * @param halo New value of property halo.
     */
    public void setHalo(org.geotools.styling.Halo halo) {
        this.halo = halo;
    }

    /**
     * Returns the label expression.
     * @return Label expression.
     */
    public org.geotools.filter.Expression getLabel() {
        return label;
    }

    /**
     * Setter for property label.
     * @param label New value of property label.
     */
    public void setLabel(org.geotools.filter.Expression label) {
        this.label = label;
    }

    /**
     * A pointPlacement specifies how a text element should be rendered
     * relative to its geometric point.
     * @return Value of property labelPlacement.
     */
    public LabelPlacement getLabelPlacement() {
        return labelPlacement;
    }

    /**
     * Setter for property labelPlacement.
     * @param labelPlacement New value of property labelPlacement.
     */
    public void setLabelPlacement(org.geotools.styling.LabelPlacement labelPlacement) {
        this.labelPlacement = labelPlacement;
    }

    /**
     * Getter for property geometryPropertyName.
     * @return Value of property geometryPropertyName.
     */
    public java.lang.String getGeometryPropertyName() {
        return geometryPropertyName;
    }

    /**
     * Setter for property geometryPropertyName.
     * @param geometryPropertyName New value of property geometryPropertyName.
     */
    public void setGeometryPropertyName(java.lang.String geometryPropertyName) {
        this.geometryPropertyName = geometryPropertyName;
    }
    
    /** Accept a StyleVisitor to perform an operation
     *  on this symbolizer.
     * 
     *  @param visitor The StyleVisitor to accept.
     */
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
    public Object clone() {
        try {
            return super.clone();            
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // this should never happen.
        }
    }
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        if (fill != null) {
            result = PRIME * result + fill.hashCode();
        }
        if (fonts != null) {
            result = PRIME * result + fonts.hashCode();
        }
        if (halo != null) {
            result = PRIME * result + halo.hashCode();
        }
        if (labelPlacement != null) {
            result = PRIME * result + labelPlacement.hashCode();
        }
        if (geometryPropertyName != null) {
            result = PRIME * result + geometryPropertyName.hashCode();
        }
        if (label != null) {
            result = PRIME * result + label.hashCode();
        }

        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth instanceof TextSymbolizerImpl) {
            TextSymbolizerImpl other = (TextSymbolizerImpl) oth;
            return EqualsUtils.equals(this.geometryPropertyName, other.geometryPropertyName) &&
                    EqualsUtils.equals(this.label, other.label) &&
                    EqualsUtils.equals(this.halo, other.halo) &&
                    EqualsUtils.equals(this.fonts, other.fonts) &&
                    EqualsUtils.equals(this.labelPlacement, other.labelPlacement) &&
                    EqualsUtils.equals(this.fill, other.fill);
        }

        return false;
    }

}