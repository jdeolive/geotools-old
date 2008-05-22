/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.go.control;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import org.geotools.gui.swing.go.GoMap2D;

/**
 *
 * @author sorel
 */
public class JRotationPane extends JComponent implements MouseListener, MouseMotionListener{

    private final int WIDTH = 100;
    
    private final GoMap2D map;
    
    public JRotationPane(GoMap2D map){
        this.map = map;
        addMouseListener(this);
        addMouseMotionListener(this);
    }
        
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g ;
        
        g2.drawOval(1, 1, WIDTH, WIDTH);
        
        g2.rotate(0);

        
        
    }

    public void mouseClicked(MouseEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mousePressed(MouseEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseReleased(MouseEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseEntered(MouseEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseExited(MouseEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseDragged(MouseEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseMoved(MouseEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    
    
    
}
