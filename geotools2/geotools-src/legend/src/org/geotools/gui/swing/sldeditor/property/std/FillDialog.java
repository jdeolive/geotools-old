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
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.FillEditor;
import org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory;
import org.geotools.styling.Fill;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class FillDialog extends JDialog {
    private FillEditor fillEditor;
    private FeatureType featureType;
    boolean exitOk = false;

    public FillDialog(Frame parent, boolean modal, FeatureType featureType, Fill fill) {
        super(parent, modal);
        init(fill, featureType);
		setLocationRelativeTo(parent);
    }

    public FillDialog(Frame parent, boolean modal, FeatureType featureType) {
        this(parent, modal, featureType, null);
    }

    public FillDialog(Dialog parent, boolean modal, FeatureType featureType, Fill fill) {
        super(parent, modal);
        init(fill, featureType);
		setLocationRelativeTo(parent);
    }

    public FillDialog(Dialog parent, boolean modal, FeatureType featureType) {
        this(parent, modal, featureType, null);
    }

    private void init(Fill fill, FeatureType featureType) {
        this.featureType = featureType;
        
        JPanel commandPanel = new JPanel();
        JButton btnOk = new JButton("Ok");
        JButton btnCancel = new JButton("Cancel");
        JPanel mainPanel = new JPanel();
        fillEditor = PropertyEditorFactory.createPropertyEditorFactory().createFillEditor(featureType);

        commandPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 3));
        commandPanel.add(btnOk);
        commandPanel.add(btnCancel);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(fillEditor);
		mainPanel.add(commandPanel, BorderLayout.SOUTH);
        setTitle("Edit fill");
        setContentPane(mainPanel);
        pack();

        btnOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitOk = true;
                    dispose();
                }
            });
        btnCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitOk = false;
                    dispose();
                }
            });
    }
    
    public boolean exitOk() {
    	return exitOk;
    }
    
    public Fill getFill() {
    	return fillEditor.getFill();
    }
}
