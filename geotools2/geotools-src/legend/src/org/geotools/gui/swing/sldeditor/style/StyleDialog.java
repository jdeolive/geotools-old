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
 * Created on 26-dic-2003
 *
 */
package org.geotools.gui.swing.sldeditor.style;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.gui.swing.sldeditor.util.WindowMinSizer;
import org.geotools.styling.Style;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class StyleDialog extends JDialog {
    private WindowMinSizer minSizer;
    StyleEditorChooser editorChooser;
    boolean exitOk = false;
    
    
    public StyleDialog(Dialog parent, FeatureSource fs, Style s) {
        super(parent, true);
        initialize(fs, s);
    }

    public StyleDialog(Frame parent, FeatureSource fs, Style s) {
        super(parent, true);
        initialize(fs, s);
    }

    public static StyleDialog createDialog(Component parent, FeatureSource fs, Style s) {
        Window w = FormUtils.getWindowForComponent(parent);
        if (w instanceof Dialog) {
            return new StyleDialog((Dialog) w, fs, s);
        } else {
            return new StyleDialog((Frame) w, fs, s);
        }
    }

    /**
     * Common initialization method
     *
     * @param fs
     * @param s
     */
    private void initialize(FeatureSource fs, Style s) {
        editorChooser = new StyleEditorChooser(fs, s);
        
        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Cancel");
        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 3));
        commandPanel.add(okButton);
        commandPanel.add(cancelButton);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(editorChooser);
        panel.add(commandPanel, BorderLayout.SOUTH);
        
        setContentPane(panel);
        
        setTitle("Edit map layer style");
        
        okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitOk = true;
                dispose();
			}
		});
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitOk = false;
                dispose();
            }
        });
        
        pack();
        minSizer = new WindowMinSizer(this);
    }
    
    public boolean exitOk() {
    	return exitOk;
    }
    
    public Style getStyle() {
    	return editorChooser.getStyle();
    }
}
