/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import javax.swing.Icon;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;
import org.geotools.styling.StyleBuilder;
import org.opengis.filter.expression.Expression;

/**
 * Line join panel
 * 
 * @author  Johann Sorel
 */
public class JLinejoinPane extends javax.swing.JPanel {
    
    private static final Icon ICON_ROUND = IconBundle.getResource().getIcon("16_linejoin_round");
    private static final Icon ICON_MITRE = IconBundle.getResource().getIcon("16_linejoin_mitre");
    private static final Icon ICON_BEVEL = IconBundle.getResource().getIcon("16_linejoin_bevel");
    private static final Icon ICON_EXP = IconBundle.getResource().getIcon("16_expression");
    
    private MapLayer layer = null;
    private Expression exp = null;

    public JLinejoinPane() {
        initComponents();
        but_round.setSelected(true);
    }

    public void setLayer(MapLayer layer) {
        this.layer = layer;
    }
    
     public MapLayer getLayer() {
        return layer;
    }

    public void setLineJoin(Expression exp) {

        this.exp = exp;
        if (exp != null) {
            if (exp.toString().toLowerCase().equals("bevel")) {
                but_bevel.setSelected(true);
                but_bevel.setContentAreaFilled(true);
                but_mitre.setContentAreaFilled(false);
                but_round.setContentAreaFilled(false);
            } else if (exp.toString().toLowerCase().equals("mitre")) {
                but_mitre.setSelected(true);
                but_bevel.setContentAreaFilled(false);
                but_mitre.setContentAreaFilled(true);
                but_round.setContentAreaFilled(false);
            } else if (exp.toString().toLowerCase().equals("round")) {
                but_round.setSelected(true);
                but_bevel.setContentAreaFilled(false);
                but_mitre.setContentAreaFilled(false);
                but_round.setContentAreaFilled(true);
            } else {
                but_bevel.setContentAreaFilled(false);
                but_mitre.setContentAreaFilled(false);
                but_round.setContentAreaFilled(false);
            }
        } else {
            but_bevel.setContentAreaFilled(false);
            but_mitre.setContentAreaFilled(false);
            but_round.setContentAreaFilled(false);
        }

    }

    public Expression getLineJoin() {

        if (exp == null) {
            exp = new StyleBuilder().literalExpression("round");
        }
        
        return exp;
    } 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        but_round = new javax.swing.JToggleButton();
        but_bevel = new javax.swing.JToggleButton();
        but_mitre = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();

        setOpaque(false);

        buttonGroup1.add(but_round);
        but_round.setIcon(ICON_ROUND);
        but_round.setBorderPainted(false);
        but_round.setContentAreaFilled(false);
        but_round.setIconTextGap(0);
        but_round.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_round.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_roundActionPerformed(evt);
            }
        });

        buttonGroup1.add(but_bevel);
        but_bevel.setIcon(ICON_BEVEL);
        but_bevel.setBorderPainted(false);
        but_bevel.setContentAreaFilled(false);
        but_bevel.setIconTextGap(0);
        but_bevel.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_bevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_bevelActionPerformed(evt);
            }
        });

        buttonGroup1.add(but_mitre);
        but_mitre.setIcon(ICON_MITRE);
        but_mitre.setBorderPainted(false);
        but_mitre.setContentAreaFilled(false);
        but_mitre.setIconTextGap(0);
        but_mitre.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_mitre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_mitreActionPerformed(evt);
            }
        });

        jButton1.setIcon(ICON_EXP);
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1actionDialogLineCap(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(but_round)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_bevel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_mitre)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1))
        );

        layout.linkSize(new java.awt.Component[] {but_bevel, but_mitre, but_round, jButton1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(but_round)
            .add(but_bevel)
            .add(but_mitre)
            .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {but_bevel, but_mitre, but_round, jButton1}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents
    private void jButton1actionDialogLineCap(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1actionDialogLineCap
        JExpressionDialog dialog = new JExpressionDialog();

        dialog.setModal(true);
        dialog.setLocationRelativeTo(jButton1);
        dialog.setLayer(layer);
        dialog.setExpression(exp);
        dialog.setVisible(true);

        exp = dialog.getExpression();

        but_bevel.setSelected(false);
        but_round.setSelected(false);
        but_mitre.setSelected(false);
        but_bevel.setContentAreaFilled(false);
        but_mitre.setContentAreaFilled(false);
        but_round.setContentAreaFilled(false);
    }//GEN-LAST:event_jButton1actionDialogLineCap

    private void but_roundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_roundActionPerformed
        StyleBuilder sb = new StyleBuilder();
        setLineJoin( sb.literalExpression("round"));   
    }//GEN-LAST:event_but_roundActionPerformed

    private void but_bevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_bevelActionPerformed
        StyleBuilder sb = new StyleBuilder();
        setLineJoin( sb.literalExpression("bevel"));
    }//GEN-LAST:event_but_bevelActionPerformed

    private void but_mitreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_mitreActionPerformed
        StyleBuilder sb = new StyleBuilder();
        setLineJoin( sb.literalExpression("mitre"));
    }//GEN-LAST:event_but_mitreActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton but_bevel;
    private javax.swing.JToggleButton but_mitre;
    private javax.swing.JToggleButton but_round;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables

   
}
