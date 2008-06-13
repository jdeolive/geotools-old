/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.gui.swing.style.sld;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.geotools.gui.swing.icon.IconBundle;

/**
 * Simple dialog
 * 
 * @author Johann Sorel
 */
public class JSimpleDialog extends javax.swing.JDialog {

    private static final ImageIcon ICO_CLOSE = IconBundle.getResource().getIcon("16_close");
    
    /** Creates new form JSimpleDialog */
    public JSimpleDialog(java.awt.Frame parent, boolean modal,Component child) {
        super(parent, modal);
        initComponents();
        
        JToolBar guiBar = new JToolBar();
        guiBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        guiBar.add(guiClose);
        guiBar.setFloatable(false);
        
        JScrollPane jsp = new JScrollPane(child);
        
        add(BorderLayout.CENTER,jsp);
        add(BorderLayout.SOUTH,guiBar);
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        guiClose = new javax.swing.JButton();

        guiClose.setIcon(ICO_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/propertyedit/Bundle"); // NOI18N
        guiClose.setText(bundle.getString("close")); // NOI18N
        guiClose.setFocusable(false);
        guiClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        guiClose.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        guiClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiCloseActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void guiCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiCloseActionPerformed
    
    this.dispose();
}//GEN-LAST:event_guiCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton guiClose;
    // End of variables declaration//GEN-END:variables

}
