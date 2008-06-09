/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author sorel
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
