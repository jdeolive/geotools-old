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
package org.geotools;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Johann Sorel
 */
public class RasterFunctionTest {

    
    
    private static void testSimpleRaster(){
        
    }
    
    
    
    
    private static void showImage(final Image image){
        
        JFrame frm = new JFrame();
        
        JPanel panel = new JPanel(){

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                g.drawImage(image, 0, 0, null);
            }
            
        };
        
        panel.setSize(image.getWidth(null), image.getHeight(null));
        
        JScrollPane jsp = new JScrollPane(panel);
        
        frm.setContentPane(jsp);
        
        frm.setSize(800, 600);
        frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);
        
    }
    
    public static void main(String[] args){
        
        
        testSimpleRaster();
        
        
        
        
    }
    
}
