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
     * SymbolizerListEditor.java
     *
     * Created on 14 dicembre 2003, 10.06
     */
    package org.geotools.gui.swing.sldeditor.symbolizer;

    import java.awt.Component;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.util.AbstractPanelListEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.gui.swing.sldeditor.util.SymbolizerUtils;
import org.geotools.styling.Symbolizer;

    /**
     * DOCUMENT ME!
     *
     * @author wolf
     */
    public class SymbolizerListEditor extends AbstractPanelListEditor implements SLDEditor {
        FeatureType featureType;

        /**
         * Creates a new instance of SymbolizerListEditor
         *
         * @param ft DOCUMENT ME!
         * @param symbolizers DOCUMENT ME!
         */
        public SymbolizerListEditor(FeatureType featureType, Symbolizer[] symbolizers) {
            this(featureType);
            setSymbolizers(symbolizers);
        }

        public SymbolizerListEditor(FeatureType featureType) {
            super(false);
            this.featureType = featureType;
        }

        public void setSymbolizers(Symbolizer[] symbolizers) {
            if ((symbolizers == null) || (symbolizers.length == 0)) {
                throw new IllegalArgumentException("At least one symbolizer must be provided");
            }

            removeAllPanels();
            for (int i = 0; i < symbolizers.length; i++) {
                Symbolizer s = symbolizers[i];
                addPanel(
                    SymbolizerUtils.getSymbolizerName(s),
                    (Component) SymbolizerUtils.getSymbolizerEditor(s, featureType));
            }

            invalidate();
        }

        public Symbolizer[] getSymbolizers() {
            Component[] cs = getPanels();
            Symbolizer[] symbolizers = new Symbolizer[cs.length];
            for (int i = 0; i < cs.length; i++) {
                symbolizers[i] = ((SymbolizerEditor) cs[i]).getSymbolizer();
            }

            return symbolizers;
        }

        protected void addButtonPressed() {
            SymbolizerChooserDialog dialog = symbolizerEditorFactory.createSymbolizerChooserDialog(this, featureType);
            dialog.show();

            if (dialog.exitOk()) {
                Symbolizer s = dialog.getSelectedSymbolizer();

                addPanel(
                    SymbolizerUtils.getSymbolizerName(s),
                    (Component) SymbolizerUtils.getSymbolizerEditor(s, featureType));
                setSelectedIndex(getPanelCount() - 1);
                FormUtils.getWindowForComponent(this).pack();
            }
        }

        protected void removeButtonPressed() {
            if (getPanelCount() <= 1) {
                return;
            } else {
                super.removeButtonPressed();
            }
        }

    }
