/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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

import java.awt.Color;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.SpinnerNumberModel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotools.map.MapLayer;
import org.geotools.styling.SLD;
import org.geotools.styling.StyleBuilder;
import org.opengis.filter.expression.Expression;

/**
 *
 * @author  johann Sorel
 */
public class JExpressionPane extends javax.swing.JPanel{

    private static Icon ICON_COLOR = IconBundle.getResource().getIcon("JS16_color");

    public static enum EXP_TYPE {

        COLOR,
        NUMBER,
        OPACITY,
        WELL_KNOWN_NAME,
        OTHER
    }
    private EXP_TYPE type = EXP_TYPE.OTHER;
    private MapLayer layer = null;
    private JExpressionDialog dialog = new JExpressionDialog();
    private Expression exp = null;

    /** 
     * Creates new form JExpressionPanel 
     */
    public JExpressionPane() {
        initComponents();
        init();
    }

    private void init() {
        jcb_exp.addItem("square");
        jcb_exp.addItem("circle");
        jcb_exp.addItem("triangle");
        jcb_exp.addItem("star");
        jcb_exp.addItem("cross");
        jcb_exp.addItem("x");
        jcb_exp.setSelectedItem("cross");
        parse();
    }

    public void setType(EXP_TYPE type) {
        this.type = type;
        parse();
    }

    public EXP_TYPE getType() {
        return type;
    }

    private void parse() {

        switch (type) {
            case COLOR:
                pan_exp.removeAll();
                pan_exp.add(jtf_exp);
                jtf_exp.setEditable(false);
                but_color.setVisible(true);
                but_color.setEnabled(true);
                but_color.setIcon(ICON_COLOR);
                break;
            case NUMBER:
                pan_exp.removeAll();
                pan_exp.add(jsp_exp);
                jtf_exp.setEditable(false);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
            case OPACITY:
                pan_exp.removeAll();
                pan_exp.add(jsp_exp);
                jsp_exp.setModel(new SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.0d), Double.valueOf(1.0d), Double.valueOf(0.1d)));
                jtf_exp.setEditable(false);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
            case WELL_KNOWN_NAME:
                pan_exp.removeAll();
                pan_exp.add(jcb_exp);
                jtf_exp.setEditable(false);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
            case OTHER:
                pan_exp.removeAll();
                pan_exp.add(jtf_exp);
                jtf_exp.setEditable(true);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
        }
    }

    public void setLayer(MapLayer layer) {
        this.layer = layer;
    }

    public MapLayer getLayer() {
        return layer;
    }

    public void setExpression(Expression exp) {

        this.exp = exp;

        if (exp != null) {
            if (exp != Expression.NIL) {
                jtf_exp.setText(exp.toString());

                try {
                    jcb_exp.setSelectedItem(exp.toString());
                } catch (Exception e) {
                }
            }
        }

        if (exp != null) {

            if (exp.toString().startsWith("#")) {
                try {
                    Color col = SLD.color(exp);
                    if (col != null) {
                        jtf_exp.setBackground(col);
                    } else {
                        jtf_exp.setBackground(Color.WHITE);
                    }
                } catch (Exception e) {
                    jtf_exp.setBackground(Color.WHITE);
                }
            } else {
                try {
                    jsp_exp.setValue(Double.valueOf(exp.toString()));
                } catch (Exception e) {
                }
            }
        } else {
            jtf_exp.setBackground(Color.WHITE);
            jsp_exp.setValue(1);
        }
    }

    public Expression getExpression() {

        if (exp == null) {
            exp = new StyleBuilder().literalExpression(jtf_exp.getText());
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

        jtf_exp = new javax.swing.JTextField();
        jsp_exp = new javax.swing.JSpinner();
        jcb_exp = new javax.swing.JComboBox();
        but_exp = new javax.swing.JButton();
        but_color = new javax.swing.JButton();
        pan_exp = new javax.swing.JPanel();

        jtf_exp.setOpaque(false);
        jtf_exp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtf_expActionPerformed(evt);
            }
        });
        jtf_exp.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jtf_expFocusLost(evt);
            }
        });

        jsp_exp.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));
        jsp_exp.setOpaque(false);
        jsp_exp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsp_expStateChanged(evt);
            }
        });

        jcb_exp.setOpaque(false);
        jcb_exp.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jcb_expItemStateChanged(evt);
            }
        });

        setOpaque(false);

        but_exp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/crystalproject/16x16/actions/irc_channel.png"))); // NOI18N
        but_exp.setBorderPainted(false);
        but_exp.setContentAreaFilled(false);
        but_exp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDialog(evt);
            }
        });

        but_color.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/jsorel/16x16/color.png"))); // NOI18N
        but_color.setBorderPainted(false);
        but_color.setContentAreaFilled(false);
        but_color.setMaximumSize(new java.awt.Dimension(22, 22));
        but_color.setMinimumSize(new java.awt.Dimension(22, 22));
        but_color.setPreferredSize(new java.awt.Dimension(22, 22));
        but_color.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_colorActionPerformed(evt);
            }
        });

        pan_exp.setOpaque(false);
        pan_exp.setPreferredSize(new java.awt.Dimension(100, 22));
        pan_exp.setLayout(new java.awt.GridLayout(1, 1));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(pan_exp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_exp))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(but_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(but_exp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(pan_exp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void actionDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionDialog
        dialog.setModal(true);
        dialog.setLocationRelativeTo(but_exp);
        dialog.setLayer(layer);
        dialog.setExpression(getExpression());
        dialog.setVisible(true);

        setExpression(dialog.getExpression());
    }//GEN-LAST:event_actionDialog

    private void jtf_expActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtf_expActionPerformed
        StyleBuilder sb = new StyleBuilder();
        setExpression(sb.literalExpression(jtf_exp.getText()));
}//GEN-LAST:event_jtf_expActionPerformed

    private void jtf_expFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtf_expFocusLost
        StyleBuilder sb = new StyleBuilder();
        setExpression(sb.literalExpression(jtf_exp.getText()));
}//GEN-LAST:event_jtf_expFocusLost

    private void but_colorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_colorActionPerformed
        StyleBuilder sb = new StyleBuilder();

        Color col = Color.WHITE;
        if (exp != null) {
            try {
                Color origin = SLD.color(exp);
                col = JColorChooser.showDialog(null, "", (origin != null) ? origin : Color.WHITE);
            } catch (Exception e) {
                col = JColorChooser.showDialog(null, "", Color.WHITE);
            }
        } else {
            col = JColorChooser.showDialog(null, "", Color.WHITE);
        }

        setExpression(sb.colorExpression(col));
    }//GEN-LAST:event_but_colorActionPerformed

    private void jsp_expStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsp_expStateChanged
        StyleBuilder sb = new StyleBuilder();
        setExpression(sb.literalExpression(jsp_exp.getValue()));
    }//GEN-LAST:event_jsp_expStateChanged

    private void jcb_expItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jcb_expItemStateChanged
        if (exp != null) {
            StyleBuilder sb = new StyleBuilder();

            Object obj = jcb_exp.getSelectedItem();
            if (obj != null) {
                exp = sb.literalExpression((String) obj);
            }
        }
    }//GEN-LAST:event_jcb_expItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_color;
    private javax.swing.JButton but_exp;
    private javax.swing.JComboBox jcb_exp;
    private javax.swing.JSpinner jsp_exp;
    private javax.swing.JTextField jtf_exp;
    private javax.swing.JPanel pan_exp;
    // End of variables declaration//GEN-END:variables

    
}
