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

import org.geotools.styling.StyleBuilder;
import org.opengis.filter.expression.Expression;

/**
 *
 * @author Johann Sorel
 */
public class JDashPane extends javax.swing.JPanel {

    /** 
     * Dashes panel
     * 
     * Creates new form JDashPanel 
     */
    public JDashPane() {
        initComponents();

    }

    /**
     * 
     * @return float[]
     */
    public float[] getDashes() {
        if ( (Float)jsp_lenght.getValue() == 0 || (Float)jsp_between.getValue() == 0) {
            return new float[0];
        } else {
            float[] dashes = new float[2];
            dashes[0] = (Float)jsp_lenght.getValue();
            dashes[1] = (Float)jsp_between.getValue();
            return dashes;
        }
    }

    /**
     * 
     * @param dashes , the default dashes array
     */
    public void setDashes(float[] dashes) {

        if (dashes.length != 0) {
            jsp_lenght.setValue(dashes[0]);
            jsp_between.setValue(dashes[1]);
        }
    }
    
    /**
     * 
     * @return Expression dashes offset
     */
    public Expression getOffset(){
        StyleBuilder sb = new StyleBuilder();        
        return sb.literalExpression(jsp_offset.getValue());
    }

       
    /**
     * 
     * @param exp default dashes offset
     */
    public void setOffset(Expression exp){
        
        if(exp != null)
            jsp_offset.setValue( Float.parseFloat(exp.toString()) );
      
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jsp_offset = new javax.swing.JSpinner();
        jsp_between = new javax.swing.JSpinner();
        jsp_lenght = new javax.swing.JSpinner();

        jsp_offset.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jsp_offset.setToolTipText(bundle.getString("tooltip_offset")); // NOI18N

        jsp_between.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), null, Float.valueOf(1.0f)));
        jsp_between.setToolTipText(bundle.getString("tooltip_gap")); // NOI18N

        jsp_lenght.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), null, Float.valueOf(1.0f)));
        jsp_lenght.setToolTipText(bundle.getString("tooltip_lenght")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jsp_lenght, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jsp_between, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jsp_offset, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(new java.awt.Component[] {jsp_between, jsp_lenght, jsp_offset}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jsp_lenght, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jsp_between, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jsp_offset, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(new java.awt.Component[] {jsp_between, jsp_lenght, jsp_offset}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner jsp_between;
    private javax.swing.JSpinner jsp_lenght;
    private javax.swing.JSpinner jsp_offset;
    // End of variables declaration//GEN-END:variables
}
