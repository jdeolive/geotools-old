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
 * Created on 24-dic-2003
 *
 */
package org.geotools.gui.swing.sldeditor.style.full;

import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.geotools.gui.swing.sldeditor.util.FormUtils;


/**
 * Basic component for editors that need a name/title/abstract  editing fields
 *
 * @author wolf
 */
public abstract class BasicMetadataEditor extends JComponent {
    protected JLabel metadataLabel;
    protected JLabel lblName;
    protected JTextField txtName;
    protected JLabel lblTitle;
    protected JLabel lblAbstract;
    protected JTextArea txaAbstract;
    protected JTextField txtTitle;

    public BasicMetadataEditor() {
        lblName = new JLabel("Name");
        txtName = new JTextField();
        lblTitle = new JLabel("Title");
        txtTitle = new JTextField();
        lblAbstract = new JLabel("Abstract");
        txaAbstract = new JTextArea("Abstract");

        metadataLabel = FormUtils.getTitleLabel("Metadata");

        setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, metadataLabel);
        FormUtils.addRowInGBL(this, 1, 0, lblName, txtName);
        FormUtils.addRowInGBL(this, 2, 0, lblTitle, txtTitle);
        FormUtils.addRowInGBL(this, 3, 0, lblAbstract, new JScrollPane(txaAbstract), 1.0, true);
    }
    
    public int getLastRow() {
        return 3;
    }

    protected String toText(String s) {
        if (s != null) {
            return s;
        } else {
            return "";
        }
    }

    public static void main(String[] args) throws Exception {
        FormUtils.show(new BasicMetadataEditor() {
            });
    }
}
