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
 * Created on 1-gen-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.FillEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.gui.swing.sldeditor.util.StyleCloner;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.Fill;
import org.geotools.styling.StyleFactory;


/**
 * A button that shows the current fill and pops up a fill editing
 * dialog when pressed. Useful for editing fills with few space available
 *
 * @author wolf To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class DefaultCompactFillEditor extends FillEditor {
	private static LiteRenderer renderer = new LiteRenderer();
    FillButton fillButton;
    StyleCloner styleCloner;
    Fill fill;
    FeatureType featureType;

    public DefaultCompactFillEditor(FeatureType featureType) {
        this(featureType, null);
    }

    public DefaultCompactFillEditor(FeatureType featureType, Fill fill) {
        styleCloner = new StyleCloner(StyleFactory.createStyleFactory());

        this.featureType = featureType;
        this.fill = fill;

        fillButton = new FillButton();
        this.setLayout(new BorderLayout());
        add(fillButton);

        fillButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    buttonPressed();
                }
            });
    }
    
    public Fill getFill() {
    	return fill;
    }
    
    public void setFill(Fill fill) {
    	this.fill = fill;
    	fillButton.repaint();
    }

    protected void buttonPressed() {
        Window w = FormUtils.getWindowForComponent(this);
        FillDialog dialog;
        Fill clonedFill = styleCloner.clone(fill);

        if (w instanceof Frame) {
            dialog = new FillDialog((Frame) w, true, featureType, clonedFill);
        } else {
            dialog = new FillDialog((Dialog) w, true, featureType, clonedFill);
        }

        dialog.show();

        if (dialog.exitOk()) {
            fill = dialog.getFill();
            fillButton.repaint();
        }
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultCompactFillEditor(null));
    }

    private class FillButton extends JButton {
       /**
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        protected void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (fill != null) {
                try {
                    renderer.applyFill((Graphics2D) g, fill, null);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(0, 0, getWidth(), getHeight());
                g.drawLine(0, getHeight(), getWidth(), 0);
            }
        }
    }
}
