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
package org.geotools.gui.swing.worker;

import java.awt.Cursor;
import java.awt.Toolkit;
import javax.swing.JOptionPane;
import javax.swing.UIManager;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class MessagePopup extends javax.swing.JDialog {
    // Variables declaration - do not modify
    private javax.swing.JLabel lblMessage;
    private boolean beep = false;

    /**
     * Creates new form MessageDialog
     *
     * @param parent The parent component
     * @param title DOCUMENT ME!
     * @param message The message that will be showed
     * @param beep If true, beeps when clicked on
     */
    public MessagePopup(java.awt.Component parent, String title, String message, boolean beep) {
        super(JOptionPane.getFrameForComponent(parent), false);

        this.beep = beep;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(title);

        lblMessage = new javax.swing.JLabel();
        lblMessage.setText(message);
        lblMessage.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        lblMessage.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        lblMessage.setIconTextGap(10);
        lblMessage.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 30, 5, 30)));
        lblMessage.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (MessagePopup.this.beep) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        getContentPane().add(lblMessage, java.awt.BorderLayout.CENTER);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        pack();
        setLocationRelativeTo(JOptionPane.getFrameForComponent(parent));
    }

    public void close() {
        setVisible(false);
        dispose();
    }
}
