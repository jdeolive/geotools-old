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
package org.geotools.gui.swing.sldeditor.style.full;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.property.FilterEditor;
import org.geotools.gui.swing.sldeditor.property.ScaleEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.Rule;

/**
 * DOCUMENT ME!
 *
 * @author wolf To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RuleMetadataEditor extends BasicMetadataEditor implements SLDEditor {
    private JCheckBox chkMinScale;
    private JCheckBox chkElseFilter;
    private FilterEditor filterEditor;
    private JCheckBox chkFilter;
    private JLabel titleFilterScale;
    private ScaleEditor maxScaleEditor;
    private JCheckBox chkMaxScale;
    private ScaleEditor minScaleEditor;

    public RuleMetadataEditor(Rule rule, FeatureType ft) {
        metadataLabel.setText("Rule metadata");

        titleFilterScale = FormUtils.getTitleLabel("Filter and scale");
        chkFilter = new JCheckBox("Filter");
        filterEditor = propertyEditorFactory.createFilterEditor(ft);
        chkElseFilter = new JCheckBox("Else rule");
        chkMinScale = new JCheckBox("Min scale den.");
        minScaleEditor = propertyEditorFactory.createScaleEditor();
        chkMaxScale = new JCheckBox("Max scale den.");
        maxScaleEditor = propertyEditorFactory.createScaleEditor();

        int lastRow = getLastRow();
        FormUtils.addRowInGBL(this, lastRow + 1, 0, titleFilterScale);
        FormUtils.addRowInGBL(this, lastRow + 2, 0, chkFilter, filterEditor);
        FormUtils.addRowInGBL(this, lastRow + 3, 0, chkElseFilter);
        FormUtils.addRowInGBL(this, lastRow + 4, 0, chkMinScale, minScaleEditor);
        FormUtils.addRowInGBL(this, lastRow + 5, 0, chkMaxScale, maxScaleEditor);

        txtName.setText(toText(rule.getName()));
        txtTitle.setText(toText(rule.getTitle()));
        txaAbstract.setText(toText(rule.getAbstract()));
        chkFilter.setSelected(rule.getFilter() != null);

        chkFilter.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                filterEditor.setEnabled(chkFilter.isSelected());

                if (chkFilter.isSelected()) {
                    chkElseFilter.setSelected(false);
                }
            }
        });
        chkElseFilter.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                if (chkElseFilter.isSelected()) {
                    chkFilter.setSelected(false);
                }
            }
        });
        chkMinScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                minScaleEditor.setEnabled(chkMinScale.isSelected());
            }
        });
        chkMaxScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                maxScaleEditor.setEnabled(chkMaxScale.isSelected());
            }
        });
        chkMinScale.setSelected(true);
        chkMaxScale.setSelected(true);

        if (rule.getFilter() != null) {
            filterEditor.setFilter(rule.getFilter());
        }

        chkElseFilter.setSelected(rule.hasElseFilter());

        double max = rule.getMaxScaleDenominator();

        if ((max != Double.POSITIVE_INFINITY) && (max != Double.MAX_VALUE) && !Double.isNaN(max)) {
            chkMaxScale.setSelected(true);
            maxScaleEditor.setScaleDenominator(max);
        } else {
            chkMaxScale.setSelected(false);
        }

        double min = rule.getMinScaleDenominator();

        if ((min > 0) && !Double.isNaN(min)) {
            chkMinScale.setSelected(true);
            minScaleEditor.setScaleDenominator(min);
        } else {
            chkMinScale.setSelected(false);
        }
    }

    public void fillRule(Rule rule) {
        rule.setName(txtName.getText());
        rule.setTitle(txtTitle.getText());
        rule.setAbstract(txaAbstract.getText());

        if (chkFilter.isSelected()) {
            rule.setFilter((Filter) filterEditor.getFilter());
        } else {
            rule.setFilter(null);
        }

        rule.setIsElseFilter(chkElseFilter.isSelected());

        if (chkMinScale.isSelected()) {
            rule.setMinScaleDenominator(minScaleEditor.getScaleDenominator());
        } else {
            rule.setMinScaleDenominator(Double.NaN);
        }

        if (chkMaxScale.isSelected()) {
            rule.setMaxScaleDenominator(maxScaleEditor.getScaleDenominator());
        } else {
            rule.setMaxScaleDenominator(Double.NaN);
        }
    }
}
