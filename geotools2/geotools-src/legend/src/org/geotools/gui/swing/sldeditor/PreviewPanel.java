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
 * PreviewPanel.java
 *
 * Created on 21 July 2003, 21:33
 */
package org.geotools.gui.swing.sldeditor;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.JLabel;

import org.geotools.renderer.LegendIconMaker;
import org.geotools.styling.Symbolizer;


/**
 * PreviewPanel actually is a place symbolizereditor drawing its symbolizer as a icon which is
 * calling LegendIconMaker in a JLabel. so in legend sldeditor before you save it you could have a
 * preview.a feature sample needs to be set in RuleEdit.sample for each layer.
 *
 * @author jianhuij, aaime
 */
public class PreviewPanel extends javax.swing.JComponent implements SLDEditor {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gui.swing.sldeditor");
    private Symbolizer symbolizer;
    private Symbolizer[] symbolizers;
    private JLabel lblPreview;

    public PreviewPanel() {
        this(null);
    }

    /**
     * Creates new form PreviewPanel
     *
     * @param symb symbolizer for preview, a feature sample needs to be set in RuleEdit.sample
     */
    public PreviewPanel(Symbolizer symb) {
        lblPreview = new JLabel();

        setLayout(new java.awt.BorderLayout());
        

        setBorder(new javax.swing.border.TitledBorder("Preview"));
        add(lblPreview, java.awt.BorderLayout.CENTER);

        if (symb == null) {
            setSymbolizer(styleFactory.getDefaultLineSymbolizer());
        }
    }

    public Symbolizer getSymbolizer() {
        return symbolizer;
    }

    public void setSymbolizer(Symbolizer symb) {
        setSymbolizer(new Symbolizer[] { symbolizer });
    }

    public void setSymbolizer(Symbolizer[] symb) {
        symbolizers = symb;
        updateIcon();
        repaint();
    }
    
    private void updateIcon() {
        lblPreview.setIcon(LegendIconMaker.makeLegendIcon(50, 50,
                            new Color(0, 0, 0, 0), symbolizers, null, false));
    }
}
