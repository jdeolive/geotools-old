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

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotools.map.MapLayer;
import org.geotools.styling.Mark;
import org.geotools.styling.StyleBuilder;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.macosx.MacOSXLookAndFeelAddons;

/**
 * Mark panel
 * 
 * @author Johann Sorel
 */
public class JMarkPane extends javax.swing.JPanel implements StyleElementEditor<Mark> {

    private static final ImageIcon ICO_FILL = IconBundle.getResource().getIcon("16_paint_fill");
    private static final ImageIcon ICO_STROKE = IconBundle.getResource().getIcon("16_paint_stroke");
    
    private MapLayer layer = null;
    private Mark mark = null;

    public JMarkPane() {

        LookAndFeel oldLnF = UIManager.getLookAndFeel();

        UIManager.put("win.xpstyle.name", null);

        try {
            LookAndFeelAddons.setAddon(MacOSXLookAndFeelAddons.class);
        } catch (InstantiationException ex) {
            Logger.getLogger(JMarkPane.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JMarkPane.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();
        init();
    }

    private void init() {
        guiRotation.setType(JExpressionPane.EXP_TYPE.NUMBER);
        guiSize.setType(JExpressionPane.EXP_TYPE.NUMBER);
        guiWKN.setType(JExpressionPane.EXP_TYPE.WELL_KNOWN_NAME);



    }

    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiFill.setLayer(layer);
        guiRotation.setLayer(layer);
        guiSize.setLayer(layer);
        guiStroke.setLayer(layer);
        guiWKN.setLayer(layer);
    }

    public MapLayer getLayer() {
        return layer;
    }

    public void setEdited(Mark mk) {
        this.mark = mk;

        if (mark != null) {
            guiFill.setEdited(mark.getFill());
            guiRotation.setExpression(mark.getRotation());
            guiSize.setExpression(mark.getSize());
            guiStroke.setEdited(mark.getStroke());
            guiWKN.setExpression(mark.getWellKnownName());
        }
    }

    public Mark getEdited() {

        if (mark == null) {
            mark = new StyleBuilder().createMark("triangle");
        }

        apply();
        return mark;
    }

    public void apply() {
        if (mark != null) {
            mark.setFill(guiFill.getEdited());
            mark.setRotation(guiRotation.getExpression());
            mark.setSize(guiSize.getExpression());
            mark.setStroke(guiStroke.getEdited());
            mark.setWellKnownName(guiWKN.getExpression());
        }
    }

    public Component getComponent() {
        return this;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jXTaskPaneContainer1 = new org.jdesktop.swingx.JXTaskPaneContainer();
        jXTaskPane1 = new org.jdesktop.swingx.JXTaskPane();
        jPanel1 = new javax.swing.JPanel();
        guiRotation = new org.geotools.gui.swing.style.sld.JExpressionPane();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        guiSize = new org.geotools.gui.swing.style.sld.JExpressionPane();
        guiWKN = new org.geotools.gui.swing.style.sld.JExpressionPane();
        jLabel1 = new javax.swing.JLabel();
        jXTaskPane2 = new org.jdesktop.swingx.JXTaskPane();
        guiStroke = new org.geotools.gui.swing.style.sld.JStrokePane();
        jXTaskPane3 = new org.jdesktop.swingx.JXTaskPane();
        guiFill = new org.geotools.gui.swing.style.sld.JFillPane();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jXTaskPane1.setTitle(bundle.getString("general")); // NOI18N

        jPanel1.setOpaque(false);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText(bundle.getString("rotation")); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText(bundle.getString("size")); // NOI18N

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(bundle.getString("wellknownname")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiWKN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(73, 73, 73)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(102, 102, 102)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(guiWKN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .add(guiSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .add(guiRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jXTaskPane1.getContentPane().add(jPanel1);

        jXTaskPaneContainer1.add(jXTaskPane1);

        jXTaskPane2.setExpanded(false);
        jXTaskPane2.setIcon(ICO_STROKE);
        jXTaskPane2.setTitle(bundle.getString("stroke")); // NOI18N
        jXTaskPane2.getContentPane().add(guiStroke);

        jXTaskPaneContainer1.add(jXTaskPane2);

        jXTaskPane3.setExpanded(false);
        jXTaskPane3.setIcon(ICO_FILL);
        jXTaskPane3.setTitle(bundle.getString("fill")); // NOI18N
        jXTaskPane3.getContentPane().add(guiFill);

        jXTaskPaneContainer1.add(jXTaskPane3);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jXTaskPaneContainer1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jXTaskPaneContainer1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JFillPane guiFill;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiRotation;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiSize;
    private org.geotools.gui.swing.style.sld.JStrokePane guiStroke;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiWKN;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane1;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane2;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane3;
    private org.jdesktop.swingx.JXTaskPaneContainer jXTaskPaneContainer1;
    // End of variables declaration//GEN-END:variables
}
