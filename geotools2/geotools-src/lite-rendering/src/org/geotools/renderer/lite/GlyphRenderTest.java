/*
 * GlyphRenderTest.java
 *
 * Created on April 6, 2004, 11:20 AM
 */

package org.geotools.renderer.lite;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author  jfc173
 */
public class GlyphRenderTest extends JPanel{
    
    private int radius = 50;
    private Color circleColor = Color.BLUE.darker();
    private int barHeight = 150;
    private Color barColor = Color.BLACK;
    private int barUncertainty = 50;
    private int barUncWidth = 5;
    private Color barUncColor = Color.GRAY;
    private int pointerDirection = 21;
    private Color pointerColor = Color.RED;
    private int pointerLength = 100;
    private int wedgeWidth = 25;
    private int circleCenterX, circleCenterY, imageHeight, imageWidth;
    private Color wedgeColor = Color.BLUE;
    private BufferedImage image;
    private Graphics2D imageGraphic;
    private Canvas observer = new Canvas();
    
    
    /** Creates a new instance of GlyphRenderTest */
    public GlyphRenderTest() {
        init();
    }
    

    
    public void init(){
        //        frame.setContentPane(ipanel);
        circleCenterX = Math.max(pointerLength, radius);
        circleCenterY = Math.max(barHeight + barUncertainty, Math.max(pointerLength, radius));
        imageHeight = Math.max(radius * 2, Math.max(radius + pointerLength, Math.max(radius + barHeight + barUncertainty, pointerLength + barHeight + barUncertainty)));
        imageWidth = Math.max(radius * 2, pointerLength * 2);
        image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        pointerLength = Math.max(pointerLength, radius);
        imageGraphic = image.createGraphics();
        imageGraphic.setBackground(Color.WHITE);
        imageGraphic.clearRect(0, 0, imageWidth, imageHeight);
        imageGraphic.setColor(circleColor);
        imageGraphic.fillOval(circleCenterX - radius, circleCenterY - radius, radius * 2, radius * 2);
        imageGraphic.setColor(wedgeColor); 
        imageGraphic.fillArc(circleCenterX - radius,
                             circleCenterY - radius,
                             radius * 2,
                             radius * 2, 
                             calculateWedgeAngle(),
                             wedgeWidth * 2);
        imageGraphic.setColor(barUncColor);
        imageGraphic.fillRect(circleCenterX - barUncWidth, 
                              circleCenterY - barHeight - barUncertainty, 
                              barUncWidth * 2, 
                              barUncertainty * 2);
        imageGraphic.setColor(barColor);
        imageGraphic.drawLine(circleCenterX, circleCenterY, circleCenterX, circleCenterY - barHeight);
        imageGraphic.setColor(pointerColor);
        int[] endPoint = calculateEndOfPointer();
        imageGraphic.drawLine(circleCenterX, circleCenterY, endPoint[0], endPoint[1]);
        imageGraphic.dispose();
    }
    
    private int calculateWedgeAngle(){
        int ret = 450 - (pointerDirection + wedgeWidth);
        return ret;
    }
    
    private int[] calculateEndOfPointer(){
        int x = circleCenterX + (int) Math.round(pointerLength * Math.cos(Math.toRadians(pointerDirection - 90)));
        int y = circleCenterY + (int) Math.round(pointerLength * Math.sin(Math.toRadians(pointerDirection - 90)));
        return new int[]{x, y};
    }
    
    public void paintComponent(Graphics g){
        g.drawImage(image, 25, 25, observer);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GlyphRenderTest test = new GlyphRenderTest();
        test.setSize(200,200);
        // Create a frame and container for the panels.
        // Set the look and feel.
        try {
            UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {}
        
        test.init();

        JFrame frame = new JFrame("glyph test");
        frame.setContentPane(test);
        frame.repaint();
        
        // Exit when the window is closed.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Show the whole thing.
        frame.pack();
        frame.setVisible(true);
    }
    
    
}
