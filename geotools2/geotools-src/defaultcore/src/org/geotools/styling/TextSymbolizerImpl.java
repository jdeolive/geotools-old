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
 * @version $Id: TextSymbolizerImpl.java,v 1.11 2003/08/01 16:55:04 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class TextSymbolizerImpl implements TextSymbolizer {
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

    public int hashcode() {
        int key = 0;
        key = fill.hashCode();
        key = (key * 13) + fonts.hashCode();
        key = (key * 13) + halo.hashCode();
        key = (key * 13) + labelPlacement.hashCode();
        key = (key * 13) + label.hashCode();
        key = (key * 13) + geometryPropertyName.hashCode();

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

    public void setFonts(Font[] fonts) {
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
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
    
}