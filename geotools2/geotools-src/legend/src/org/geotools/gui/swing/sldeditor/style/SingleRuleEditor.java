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
 * RuleEditor.java
 *
 * Created on 13 dicembre 2003, 19.18
 */
package org.geotools.gui.swing.sldeditor.style;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.property.FilterEditor;
import org.geotools.gui.swing.sldeditor.property.ScaleEditor;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerListEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.gui.swing.sldeditor.util.SymbolizerUtils;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class SingleRuleEditor extends JPanel implements SLDEditor, StyleEditor {
    /** Holds value of property expertMode. */
    private boolean expertMode;
    Rule rule;
    Style style;
    FeatureType featureType;
    JTabbedPane tbpMain;
    JPanel pnlMetadata;
    JComponent titleGeneral;
    JComponent titleFilterScale;
    JComponent titleSymbolizers;
    JLabel lblName;
    JTextField txtName;
    JLabel lblTitle;
    JTextField txtTitle;
    JLabel lblAbstract;
    JTextArea txaAbstract;
    JCheckBox chkFilter;
    FilterEditor filterEditor;
    JCheckBox chkMinScale;
    ScaleEditor cmbMinScale;
    JCheckBox chkMaxScale;
    ScaleEditor cmbMaxScale;
    SymbolizerListEditor symbolizerListEditor;

    /**
     * Creates a new instance of RuleEditor
     *
     * @param ft DOCUMENT ME!
     */
    public SingleRuleEditor(FeatureType ft) {
        this(null, ft, true);
    }

    /**
     * Creates a new instance of RuleEditor
     *
     * @param ft DOCUMENT ME!
     * @param asSimpleStyleEditor DOCUMENT ME!
     */
    public SingleRuleEditor(FeatureType ft, boolean asSimpleStyleEditor) {
        this(null, ft, asSimpleStyleEditor);
    }

    public SingleRuleEditor(Rule r, FeatureType ft) {
        this(r, ft, true);
    }

    public SingleRuleEditor(FeatureType ft, Style s) {
        this(s.getFeatureTypeStyles()[0].getRules()[0], ft);
    }

    public SingleRuleEditor(Rule r, FeatureType ft, boolean asSimpleStyleEditor) {
        lblName = new JLabel("Name");
        txtName = new JTextField();
        lblTitle = new JLabel("Title");
        txtTitle = new JTextField();
        lblAbstract = new JLabel("Abstract");
        txaAbstract = new JTextArea();
        chkFilter = new JCheckBox("Filter");
        filterEditor = propertyEditorFactory.createFilterEditor(ft);
        chkMinScale = new JCheckBox("Min scale den.");
        cmbMinScale = propertyEditorFactory.createScaleEditor();
        chkMaxScale = new JCheckBox("Max scale den.");
        cmbMaxScale = propertyEditorFactory.createScaleEditor();

        symbolizerListEditor = new SymbolizerListEditor(ft);

        titleGeneral = FormUtils.getTitleLabel("Metadata");
        titleFilterScale = FormUtils.getTitleLabel("Filter and scale");
        titleSymbolizers = FormUtils.getTitleLabel("Symbolizers");

        chkFilter.setBorder(BorderFactory.createEmptyBorder());
        chkMinScale.setBorder(BorderFactory.createEmptyBorder());
        chkMaxScale.setBorder(BorderFactory.createEmptyBorder());
        chkFilter.setSelected(true);

        chkFilter.setSelected(true);
        chkMinScale.setSelected(true);
        chkMaxScale.setSelected(true);
        chkFilter.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    filterEditor.setEnabled(chkFilter.isSelected());
                }
            });
        chkMinScale.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    cmbMinScale.setEnabled(chkMinScale.isSelected());
                }
            });
        chkMaxScale.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    cmbMaxScale.setEnabled(chkMaxScale.isSelected());
                }
            });

        pnlMetadata = new JPanel();
        pnlMetadata.setLayout(new GridBagLayout());

        FormUtils.addRowInGBL(pnlMetadata, 0, 0, titleGeneral);
        FormUtils.addRowInGBL(pnlMetadata, 1, 0, lblName, txtName);
        FormUtils.addRowInGBL(pnlMetadata, 2, 0, lblTitle, txtTitle);
        FormUtils.addRowInGBL(pnlMetadata, 3, 0, lblAbstract, new JScrollPane(txaAbstract), 1.0,
            true);

        FormUtils.addRowInGBL(pnlMetadata, 4, 0, titleFilterScale);
        FormUtils.addRowInGBL(pnlMetadata, 5, 0, chkFilter, filterEditor);
        FormUtils.addRowInGBL(pnlMetadata, 7, 0, chkMinScale, cmbMinScale);
        FormUtils.addRowInGBL(pnlMetadata, 8, 0, chkMaxScale, cmbMaxScale);

        // FormUtils.addRowInGBL(pnlMetadata, 9, 0, titleSymbolizers);
        tbpMain = new JTabbedPane();
        tbpMain.add("Metadata", pnlMetadata);
        tbpMain.add("Symbolizers", symbolizerListEditor);

        setLayout(new BorderLayout());
        add(tbpMain);

        this.featureType = ft;
        setRule(r);
    }

    private String toText(String s) {
        if (s != null) {
            return s;
        } else {
            return "";
        }
    }

    public void setRule(Rule r) {
        if (r == null) {
            rule = styleBuilder.createRule(SymbolizerUtils.getDefaultSymbolizer(featureType));
        } else {
            rule = r;
        }

        txtName.setText(toText(rule.getName()));
        txtTitle.setText(toText(rule.getTitle()));
        txaAbstract.setText(toText(rule.getAbstract()));
        chkFilter.setSelected(rule.getFilter() != null);

        if (rule.getFilter() != null) {
            filterEditor.setFilter(rule.getFilter());
        }

        double max = rule.getMaxScaleDenominator();

        if ((max != Double.POSITIVE_INFINITY) && (max != Double.MAX_VALUE) && !Double.isNaN(max)) {
            chkMaxScale.setSelected(true);
            cmbMaxScale.setScaleDenominator(max);
        } else {
            chkMaxScale.setSelected(false);
        }

        double min = rule.getMinScaleDenominator();

        if ((min > 0) && !Double.isNaN(min)) {
            chkMinScale.setSelected(true);
            cmbMinScale.setScaleDenominator(min);
        } else {
            chkMinScale.setSelected(false);
        }

        symbolizerListEditor.setSymbolizers(rule.getSymbolizers());
    }

    public Rule getRule() {
        rule.setName(txtName.getText());
        rule.setTitle(txtTitle.getText());
        rule.setAbstract(txaAbstract.getText());

        if (chkFilter.isSelected()) {
            rule.setFilter(filterEditor.getFilter());
        } else {
            rule.setFilter(null);
        }

        if (chkMinScale.isSelected()) {
            rule.setMinScaleDenominator(cmbMinScale.getScaleDenominator());
        } else {
            rule.setMinScaleDenominator(0);
        }

        if (chkMaxScale.isSelected()) {
            rule.setMaxScaleDenominator(cmbMaxScale.getScaleDenominator());
        } else {
            rule.setMaxScaleDenominator(Double.MAX_VALUE);
        }

        rule.setSymbolizers(symbolizerListEditor.getSymbolizers());

        return rule;
    }

    /**
     * Getter for property expertMode.
     *
     * @return Value of property expertMode.
     */
    public boolean isExpertMode() {
        return this.expertMode;
    }

    /**
     * Setter for property expertMode.
     *
     * @param expertMode New value of property expertMode.
     */
    public void setExpertMode(boolean expertMode) {
        this.expertMode = expertMode;
    }

    public static void main(String[] args) throws Exception {
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom",
                com.vividsolutions.jts.geom.Polygon.class);
        AttributeType[] attributeTypes = new AttributeType[] {
                geom, AttributeTypeFactory.newAttributeType("name", String.class),
                AttributeTypeFactory.newAttributeType("population", Long.class)
            };

        FeatureType ft = DefaultFeatureTypeFactory.newFeatureType(attributeTypes, "demo", "",
                false, null, (GeometryAttributeType) geom);
        FormUtils.show(new SingleRuleEditor(ft, false));
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.StyleEditor#getStyle()
     */
    public Style getStyle() {
        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("", new Rule[] { getRule() });

        if (featureType != null) {
            fts.setFeatureTypeName(featureType.getTypeName());
        }

        if (style == null) {
            style = styleBuilder.createStyle();
        }

        style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts });

        return style;
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.StyleEditor#setStyle(org.geotools.styling.Style)
     */
    public void setStyle(Style s) {
        this.style = s;

        if ((s != null) && (s.getFeatureTypeStyles() != null)
                && (s.getFeatureTypeStyles().length > 0)
                && (s.getFeatureTypeStyles()[0].getRules() != null)
                && (s.getFeatureTypeStyles()[0].getRules().length > 0)) {
            setRule(s.getFeatureTypeStyles()[0].getRules()[0]);
        } else {
            setRule(null);
        }
    }

    /**
     * This style editor can meaningfully edit styles with a single featureTypeStyle owning a
     * single Rule
     *
     * @see org.geotools.gui.swing.sldeditor.StyleEditor#canEdit(org.geotools.styling.Style)
     */
    public boolean canEdit(Style s) {
        return canEditStyle(s);
    }

    public static boolean canEditStyle(Style s) {
        return (s != null) && (s.getFeatureTypeStyles() != null)
        && (s.getFeatureTypeStyles().length == 1)
        && (s.getFeatureTypeStyles()[0].getRules() != null)
        && (s.getFeatureTypeStyles()[0].getRules().length == 1);
    }
}
