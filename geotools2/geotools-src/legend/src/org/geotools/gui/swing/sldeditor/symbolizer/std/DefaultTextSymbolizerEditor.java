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
package org.geotools.gui.swing.sldeditor.symbolizer.std;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.filter.Expression;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.property.FillEditor;
import org.geotools.gui.swing.sldeditor.property.FontListChooser;
import org.geotools.gui.swing.sldeditor.property.GeometryChooser;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.Font;
import org.geotools.styling.Halo;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;


/**
 * DOCUMENT ME!
 *
 * @author wolf To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class DefaultTextSymbolizerEditor extends SymbolizerEditor implements SLDEditor {
    private static String[] styles = new String[] {
            "Normal", "Bold", "Italic", "Bold italic"
        };
    private TextSymbolizer symbolizer;
    private FillEditor fbeHaloFill;
    private JLabel lblHaloRadius;
    private ExpressionEditor neHaloRadius;
    private JCheckBox chkUseHalo;
    private FillEditor fillEditor;
    private JLabel lblFontSize;
    private ExpressionEditor neFontSize;
    private JComboBox cmbFontStyle;
    private JLabel lblFontStyle;
    private FontListChooser fontChooser;
    private JLabel lblFont;
    private ExpressionEditor attributeChooser;
    private JLabel lblLabel;
    private GeometryChooser geomChooser;
    private JLabel lblGeometry;

    public DefaultTextSymbolizerEditor(FeatureType ft) {
        this(null, null);
    }

    public DefaultTextSymbolizerEditor(TextSymbolizer ts, FeatureType ft) {
        if (ts == null) {
            ts = styleBuilder.createTextSymbolizer();
        }

        // init components
        lblGeometry = new JLabel("Geometry property");
        geomChooser = propertyEditorFactory.createGeometryChooser(ft);
        lblLabel = new JLabel("Label");
        attributeChooser = propertyEditorFactory.createFeatureAttributeChooser(ft);
        lblFont = new JLabel("Font");
        fontChooser = propertyEditorFactory.createFontListChooser();
        lblFontStyle = new JLabel("Font style");
        cmbFontStyle = new JComboBox(styles);
        lblFontSize = new JLabel("Font size");
        neFontSize = propertyEditorFactory.createIntSizeEditor(ft);
        fillEditor = propertyEditorFactory.createFillEditor(ft);
        chkUseHalo = new JCheckBox("Use halo");
        chkUseHalo.setBorder(BorderFactory.createEmptyBorder());
        fbeHaloFill = propertyEditorFactory.createCompactFillEditor(ft);
        fbeHaloFill.setPreferredSize(FormUtils.getButtonDimension());
        neHaloRadius = propertyEditorFactory.createIntSizeEditor(ft);
        lblHaloRadius = new JLabel("Halo fill");

        JPanel column1 = new JPanel();
        JPanel column2 = new JPanel();

        // no need to choose the geometry if there is only one
        if (geomChooser.getGeomPropertiesCount() < 2) {
            lblGeometry.setVisible(false);
            geomChooser.setVisible(false);
        }

        // form layout			
        setLayout(new GridBagLayout());
        column1.setLayout(new GridBagLayout());
        column2.setLayout(new GridBagLayout());

        // ... general section
        FormUtils.addRowInGBL(column1, 0, 0, FormUtils.getTitleLabel("General"));
        FormUtils.addRowInGBL(column1, 1, 0, lblGeometry, geomChooser);
        FormUtils.addRowInGBL(column1, 2, 0, lblLabel, attributeChooser);
        FormUtils.addRowInGBL(column1, 3, 0, lblFont, fontChooser);
        FormUtils.addRowInGBL(column1, 4, 0, lblFontStyle, cmbFontStyle);
        FormUtils.addRowInGBL(column1, 5, 0, lblFontSize, neFontSize);

        // ... halo section
        FormUtils.addRowInGBL(column1, 6, 0, FormUtils.getTitleLabel("Halo"));
        FormUtils.addRowInGBL(column1, 7, 0, chkUseHalo, fbeHaloFill);
        FormUtils.addRowInGBL(column1, 8, 0, lblHaloRadius, neHaloRadius);
        FormUtils.addFiller(column1, 10, 0);

        // ... fill section
        FormUtils.addRowInGBL(column2, 0, 0, FormUtils.getTitleLabel("Fill"));
        FormUtils.addRowInGBL(column2, 1, 0, fillEditor);
        FormUtils.addFiller(column2, 10, 0);

        // ... label placement section
        // ... columns
        FormUtils.addRowInGBL(this, 0, 0, column1, column2, 1.0, false);
        FormUtils.addFiller(this, 0, 2);
    }

    public static void main(String[] args) throws Exception {
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom",
                com.vividsolutions.jts.geom.Polygon.class);
        AttributeType[] attributeTypes = new AttributeType[] {
                geom,
                AttributeTypeFactory.newAttributeType("name", String.class),
                AttributeTypeFactory.newAttributeType("population", Long.class)
            };

        FeatureType ft = DefaultFeatureTypeFactory.newFeatureType(attributeTypes,
                "demo", "", false, null, (GeometryAttributeType) geom);
        System.out.println("Get default geometry: " + ft.getDefaultGeometry());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new DefaultTextSymbolizerEditor(ft));
        FormUtils.show(panel);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.SymbolizerEditor#getSymbolizer()
     */
    public Symbolizer getSymbolizer() {
        // geometry
        if (geomChooser.isVisible()) {
            symbolizer.setGeometryPropertyName(geomChooser.getSelectedName());
        }

        // fonts
        String[] fontNames = fontChooser.getFontNames();

        if ((fontNames == null) || (fontNames.length == 0)) {
            symbolizer.setFonts(null);
        } else {
            int cmbIndex = cmbFontStyle.getSelectedIndex();
            boolean bold = (cmbIndex == 1) || (cmbIndex == 3);
            boolean italic = (cmbIndex == 2) || (cmbIndex == 3);
            Expression weight = null;

            if (bold) {
                weight = styleBuilder.literalExpression("bold");
            }

            Expression style = null;

            if (italic) {
                style = styleBuilder.literalExpression("italic");
            }

            Expression size = neFontSize.getExpression();

            Font[] fonts = new Font[fontNames.length];

            for (int i = 0; i < fonts.length; i++) {
                fonts[i] = styleBuilder.createFont(styleBuilder
                        .literalExpression(fontNames[i]), style, weight, size);
            }

            symbolizer.setFonts(fonts);
        }

        // fill
        symbolizer.setFill(fillEditor.getFill());

        // halo
        if (!chkUseHalo.isSelected()) {
            symbolizer.setHalo(null);
        } else {
            Halo halo = styleBuilder.createHalo();
            halo.setFill(fbeHaloFill.getFill());
            halo.setRadius(neHaloRadius.getExpression());
        }

        return symbolizer;
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.SymbolizerEditor#setSymbolizer(org.geotools.styling.Symbolizer)
     */
    public void setSymbolizer(Symbolizer s) {
        if (!(s instanceof TextSymbolizer)) {
            throw new IllegalArgumentException(
                "Cannot set symbolizer other than a text symbolizer");
        }

        this.symbolizer = (TextSymbolizer) s;

        // geometry 
        geomChooser.setSelectedName(this.symbolizer.getGeometryPropertyName());

        // fonts
        Font[] fonts = symbolizer.getFonts();

        if (fonts != null) {
            // font names
            String[] names = new String[fonts.length];

            for (int i = 0; i < fonts.length; i++) {
                names[i] = fonts[i].getFontFamily().toString();
            }

            fontChooser.setFontNames(names);

            // style and weight
            int comboIndex = 0;

            if (fonts[0].getFontWeight() != null) {
                String weight = fonts[0].getFontWeight().toString();

                if (weight.equalsIgnoreCase("bold")) {
                    comboIndex = 1;
                }
            }

            if (fonts[0].getFontStyle() != null) {
                String style = fonts[0].getFontStyle().toString();

                if (style.equalsIgnoreCase("italic")
                        || style.equalsIgnoreCase("oblique")) {
                    comboIndex += 2;
                }
            }

            cmbFontStyle.setSelectedIndex(comboIndex);

            // size
            if (fonts[0].getFontSize() != null) {
                neFontSize.setExpression(fonts[0].getFontSize());
            } else {
                neFontSize.setExpression(styleBuilder.literalExpression(10));
            }
        }

        //		fill
        fillEditor.setFill(symbolizer.getFill());

        // halo
        if (symbolizer.getHalo() == null) {
            chkUseHalo.setSelected(false);
        } else {
            chkUseHalo.setSelected(true);
            neHaloRadius.setExpression(symbolizer.getHalo().getRadius());
            fbeHaloFill.setFill(symbolizer.getHalo().getFill());
        }
    }
}
