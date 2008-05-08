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
package org.geotools.gui.swing.style;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import javax.swing.JComponent;

import javax.swing.JDialog;
import org.geotools.gui.swing.style.sld.JChannelSelectionPane;
import org.geotools.gui.swing.style.sld.JExpressionPane;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

/**
 *
 * @author  johann sorel
 */
public class JRasterSymbolizerPane extends javax.swing.JPanel implements SymbolizerPane<RasterSymbolizer> {

    private RasterSymbolizer symbol = null;
    private MapLayer layer = null;
    private Symbolizer outLine = null;

    /** Creates new form RasterStylePanel
     * @param layer the layer style to edit
     */
    public JRasterSymbolizerPane() {
        initComponents();
        init();
    }

    private void init() {

        guiOpacity.setType(JExpressionPane.EXP_TYPE.NUMBER);

        tabDemo.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                int ligne;
                Point p = e.getPoint();
                ligne = tabDemo.rowAtPoint(p);
                if (ligne < tabDemo.getModel().getRowCount() && ligne >= 0) {
                    setEdited((RasterSymbolizer) tabDemo.getModel().getValueAt(ligne, 0));
                }
            }
        });
    }

    public void setDemoSymbolizers(Map<RasterSymbolizer, String> symbols) {
        tabDemo.setMap(symbols);
    }

    public Map<RasterSymbolizer, String> getDemoSymbolizers() {
        return tabDemo.getMap();
    }

    public void setStyle(Style style) {

        FeatureTypeStyle[] sty = style.getFeatureTypeStyles();

        Rule[] rules = sty[0].getRules();
        for (int i = 0; i < rules.length; i++) {
            Rule r = rules[i];

            //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
            if (r.getFilter() == null) {
                Symbolizer[] symbolizers = r.getSymbolizers();
                for (int j = 0; j < symbolizers.length; j++) {

                    if (symbolizers[j] instanceof RasterSymbolizer) {
                        setEdited((RasterSymbolizer) symbolizers[j]);
                    }
                }
            }
        }
    }

    public Style getStyle() {
        StyleBuilder sb = new StyleBuilder();

        Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("GridCoverage",getEdited()));

        return style;
    }

    
    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiOpacity.setLayer(layer);
        guiGeom.setLayer(layer);
        guiOverLap.setLayer(layer);
        guiContrast.setLayer(layer);
        guiRelief.setLayer(layer);
    }

    public MapLayer getLayer() {
        return layer;
    }
    
    
    public void setEdited(RasterSymbolizer sym) {
        symbol = sym;

        if (sym != null) {
            guiGeom.setGeom(symbol.getGeometryPropertyName());
            guiOpacity.setExpression(symbol.getOpacity());
            guiOverLap.setExpression(symbol.getOverlap());
            guiContrast.setEdited(symbol.getContrastEnhancement());
            guiRelief.setEdited(symbol.getShadedRelief());
                                    
            outLine = symbol.getImageOutline();
            if(outLine == null){
                guinone.setSelected(true);
            }else if(outLine instanceof LineSymbolizer){
                guiLine.setSelected(true);
            }else if(outLine instanceof PolygonSymbolizer){
                guiPolygon.setSelected(true);
            }
            testOutLine();
            
            //handle by a button
            //symbol.getChannelSelection();
            symbol.getColorMap();
            
        }
    }

    public RasterSymbolizer getEdited() {

        if (symbol == null) {
            StyleBuilder sb = new StyleBuilder();
            symbol = sb.createRasterSymbolizer();
        }
        apply();
        return symbol;
    }

    public void apply() {
        if (symbol != null) {
            symbol.setGeometryPropertyName(guiGeom.getGeom());
            symbol.setOpacity(guiOpacity.getExpression());
            symbol.setOverlap(guiOverLap.getExpression());
            symbol.setImageOutline(outLine);
            symbol.setContrastEnhancement(guiContrast.getEdited());
            symbol.setShadedRelief(guiRelief.getEdited());
        }
    }
    
    public JComponent getComponent() {
        return this;
    }

    private void testOutLine(){
        if(guinone.isSelected()){
            butLineSymbolizer.setEnabled(false);
            butPolygonSymbolizer.setEnabled(false);
            outLine = null;
        }else if(guiLine.isSelected()){
            butLineSymbolizer.setEnabled(true);
            butPolygonSymbolizer.setEnabled(false);     
            outLine = new StyleBuilder().createLineSymbolizer();
        }else if(guiPolygon.isSelected()){
            butLineSymbolizer.setEnabled(false);
            butPolygonSymbolizer.setEnabled(true);      
            outLine = new StyleBuilder().createPolygonSymbolizer();
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpOutline = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        guiOpacity = new org.geotools.gui.swing.style.sld.JExpressionPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabDemo = new org.geotools.gui.swing.style.sld.JDemoTable();
        guiGeom = new org.geotools.gui.swing.style.sld.JGeomPane();
        jXTitledSeparator1 = new org.jdesktop.swingx.JXTitledSeparator();
        jXTitledSeparator2 = new org.jdesktop.swingx.JXTitledSeparator();
        guinone = new javax.swing.JRadioButton();
        guiLine = new javax.swing.JRadioButton();
        guiPolygon = new javax.swing.JRadioButton();
        butLineSymbolizer = new javax.swing.JButton();
        butPolygonSymbolizer = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        guiOverLap = new org.geotools.gui.swing.style.sld.JExpressionPane();
        guiContrast = new org.geotools.gui.swing.style.sld.JContrastEnhancement();
        jXTitledSeparator3 = new org.jdesktop.swingx.JXTitledSeparator();
        guiRelief = new org.geotools.gui.swing.style.sld.JShadedReliefPane();
        jXTitledSeparator4 = new org.jdesktop.swingx.JXTitledSeparator();
        jLabel3 = new javax.swing.JLabel();
        butChannels = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("opacity")); // NOI18N

        jScrollPane1.setViewportView(tabDemo);

        jXTitledSeparator1.setAlpha(0.5F);
        jXTitledSeparator1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jXTitledSeparator1.setTitle(bundle1.getString("general")); // NOI18N
        jXTitledSeparator1.setFont(jXTitledSeparator1.getFont().deriveFont(jXTitledSeparator1.getFont().getStyle() | java.awt.Font.BOLD));

        jXTitledSeparator2.setAlpha(0.5F);
        jXTitledSeparator2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jXTitledSeparator2.setTitle(bundle1.getString("outline")); // NOI18N

        grpOutline.add(guinone);
        guinone.setSelected(true);
        guinone.setText(bundle1.getString("none")); // NOI18N
        guinone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guinoneActionPerformed(evt);
            }
        });

        grpOutline.add(guiLine);
        guiLine.setText(bundle1.getString("line")); // NOI18N
        guiLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiLineActionPerformed(evt);
            }
        });

        grpOutline.add(guiPolygon);
        guiPolygon.setText(bundle1.getString("polygon")); // NOI18N
        guiPolygon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiPolygonActionPerformed(evt);
            }
        });

        butLineSymbolizer.setText(bundle1.getString("edit")); // NOI18N
        butLineSymbolizer.setBorderPainted(false);
        butLineSymbolizer.setEnabled(false);
        butLineSymbolizer.setPreferredSize(new java.awt.Dimension(79, 20));
        butLineSymbolizer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLineSymbolizerActionPerformed(evt);
            }
        });

        butPolygonSymbolizer.setText(bundle1.getString("edit")); // NOI18N
        butPolygonSymbolizer.setBorderPainted(false);
        butPolygonSymbolizer.setEnabled(false);
        butPolygonSymbolizer.setPreferredSize(new java.awt.Dimension(79, 20));
        butPolygonSymbolizer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPolygonSymbolizerActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle1.getString("overlap")); // NOI18N

        jXTitledSeparator3.setAlpha(0.5F);
        jXTitledSeparator3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jXTitledSeparator3.setTitle(bundle1.getString("contrast")); // NOI18N

        jXTitledSeparator4.setAlpha(0.5F);
        jXTitledSeparator4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jXTitledSeparator4.setTitle(bundle1.getString("relief")); // NOI18N

        jLabel3.setText(bundle1.getString("channels")); // NOI18N

        butChannels.setText(bundle1.getString("edit")); // NOI18N
        butChannels.setBorderPainted(false);
        butChannels.setPreferredSize(new java.awt.Dimension(79, 22));
        butChannels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butChannelsActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(guiOpacity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(guiOverLap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(18, 18, 18))
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(butChannels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18))
                    .add(layout.createSequentialGroup()
                        .add(guiContrast, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(27, 27, 27))
                    .add(layout.createSequentialGroup()
                        .add(guiRelief, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(31, 31, 31))
                    .add(layout.createSequentialGroup()
                        .add(jXTitledSeparator4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .add(jXTitledSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .add(guinone)
                        .add(92, 92, 92))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(guiLine)
                            .add(guiPolygon))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(butLineSymbolizer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(butPolygonSymbolizer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(11, 11, 11))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jXTitledSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                            .add(jXTitledSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                            .add(guiGeom, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {guiLine, guiPolygon, guinone}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {butChannels, guiOpacity, guiOverLap}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {jXTitledSeparator1, jXTitledSeparator2, jXTitledSeparator3, jXTitledSeparator4}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(guiGeom, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jXTitledSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, guiOpacity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(guiOverLap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butChannels, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .add(jLabel3))
                .add(18, 18, 18)
                .add(jXTitledSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiContrast, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jXTitledSeparator4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiRelief, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jXTitledSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guinone)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butLineSymbolizer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(guiLine))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butPolygonSymbolizer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(guiPolygon))
                .add(10, 10, 10))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void butPolygonSymbolizerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPolygonSymbolizerActionPerformed
        JDialog dia = new JDialog();
        dia.setModal(true);
        
        JPolygonSymbolizerPane pane = new JPolygonSymbolizerPane();
        pane.setEdited((PolygonSymbolizer)outLine);
        pane.setLayer(layer);
        
        dia.getContentPane().add(pane);
        
        dia.pack();
        dia.setLocationRelativeTo(butLineSymbolizer);
        dia.setVisible(true);
        
        outLine = pane.getEdited();
    }//GEN-LAST:event_butPolygonSymbolizerActionPerformed

    private void butLineSymbolizerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butLineSymbolizerActionPerformed
        JDialog dia = new JDialog();
        dia.setModal(true);
        
        JLineSymbolizerPane pane = new JLineSymbolizerPane();
        pane.setEdited((LineSymbolizer)outLine);
        pane.setLayer(layer);
        
        dia.getContentPane().add(pane);
        
        dia.pack();
        dia.setLocationRelativeTo(butLineSymbolizer);
        dia.setVisible(true);
        
        outLine = pane.getEdited();
    }//GEN-LAST:event_butLineSymbolizerActionPerformed

    private void guiLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiLineActionPerformed
        testOutLine();
}//GEN-LAST:event_guiLineActionPerformed

    private void guinoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guinoneActionPerformed
       testOutLine();
    }//GEN-LAST:event_guinoneActionPerformed

    private void guiPolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiPolygonActionPerformed
        testOutLine();
    }//GEN-LAST:event_guiPolygonActionPerformed

    private void butChannelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butChannelsActionPerformed
        
        JDialog dia = new JDialog();
        
        JChannelSelectionPane pane = new JChannelSelectionPane();
        pane.setLayer(layer);
        
        if(symbol != null){
            pane.setEdited(symbol.getChannelSelection());
        }
        
        dia.setContentPane(pane);
        dia.pack();
        dia.setLocationRelativeTo(butChannels);
        dia.setModal(true);
        dia.setVisible(true);
        
        if(symbol == null){
            symbol =  new StyleBuilder().createRasterSymbolizer();
        }
        symbol.setChannelSelection(pane.getEdited());        
        
    }//GEN-LAST:event_butChannelsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butChannels;
    private javax.swing.JButton butLineSymbolizer;
    private javax.swing.JButton butPolygonSymbolizer;
    private javax.swing.ButtonGroup grpOutline;
    private org.geotools.gui.swing.style.sld.JContrastEnhancement guiContrast;
    private org.geotools.gui.swing.style.sld.JGeomPane guiGeom;
    private javax.swing.JRadioButton guiLine;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiOpacity;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiOverLap;
    private javax.swing.JRadioButton guiPolygon;
    private org.geotools.gui.swing.style.sld.JShadedReliefPane guiRelief;
    private javax.swing.JRadioButton guinone;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator1;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator2;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator3;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator4;
    private org.geotools.gui.swing.style.sld.JDemoTable tabDemo;
    // End of variables declaration//GEN-END:variables
    
}
