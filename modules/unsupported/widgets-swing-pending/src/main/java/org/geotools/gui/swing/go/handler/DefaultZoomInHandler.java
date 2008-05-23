/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.go.handler;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.event.MouseInputListener;

import org.geotools.display.canvas.AWTCanvas2D;
import org.geotools.gui.swing.icon.IconBundle;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import org.geotools.gui.swing.go.GoMap2D;

/**
 *
 * @author johann sorel
 */
public class DefaultZoomInHandler implements CanvasHandler {

    private static final ImageIcon ICON = IconBundle.getResource().getIcon("16_select_default");
    private Cursor CUR_ZOOM_IN;
    private static final String title = ResourceBundle.getBundle("org/geotools/gui/swing/map/map2d/handler/Bundle").getString("default");
    protected final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final MouseListen mouseInputListener = new MouseListen();
    private double zoomFactor = 2;
    private final GoMap2D map2D;
    private boolean installed = false;

    public DefaultZoomInHandler(GoMap2D map) {
        buildCursors();
        map2D = map;
    }

    private void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon ico_zoomIn = IconBundle.getResource().getIcon("16_zoom_in");

        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(ico_zoomIn.getImage(), 0, 0, null);
        CUR_ZOOM_IN = tk.createCustomCursor(img, new Point(1, 1), "in");
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getTitle() {
        return title;
    }

    public ImageIcon getIcon() {
        return ICON;
    }
    
    
    private void scale(Point2D center, double zoom){
        map2D.getCanvas().scale(zoom, center);
    }
    
    
    private void zoom(int startx,int starty, int endx, int endy){
        
        // TODO : Zoom on the given rectangle
        
//        RenderingStrategy strategy = map2D.getRenderingStrategy();
//        
//        Coordinate coord1 = strategy.toMapCoord(startx, starty);
//        Coordinate coord2 = strategy.toMapCoord(endx, endy);
//        
//        Envelope env = new Envelope(coord1, coord2);
//        
//        strategy.setMapArea(env);
       
    }

    //---------------------PRIVATE CLASSES--------------------------------------
    private class MouseListen implements MouseInputListener, MouseWheelListener,KeyListener {

        private boolean CTRL_FLAG = false;
        
        private int startX;
        private int startY;
        private int lastX;
        private int lastY;
        private int mousebutton = 0;


        private void processDrag(int x1, int y1, int x2, int y2, boolean pan) {

//            Envelope mapArea = map2D.getRenderingStrategy().getMapArea();
//
//            if ((x1 == x2) && (y1 == y2)) {
//                return;
//            }
//
//            Rectangle bounds = map2D.getComponent().getBounds();
//
//            double mapWidth = mapArea.getWidth();
//            double mapHeight = mapArea.getHeight();
//
//            double startX = ((x1 * mapWidth) / (double) bounds.width) + mapArea.getMinX();
//            double startY = (((bounds.getHeight() - y1) * mapHeight) / (double) bounds.height) + mapArea.getMinY();
//            double endX = ((x2 * mapWidth) / (double) bounds.width) + mapArea.getMinX();
//            double endY = (((bounds.getHeight() - y2) * mapHeight) / (double) bounds.height) + mapArea.getMinY();
//
//            double left;
//            double right;
//            double bottom;
//            double top;
//            Coordinate ll;
//            Coordinate ur;
//
//            if (!pan) {
//
//                // make the dragged rectangle (in map coords) the new BBOX
//                left = Math.min(startX, endX);
//                right = Math.max(startX, endX);
//                bottom = Math.min(startY, endY);
//                top = Math.max(startY, endY);
//                ll = new Coordinate(left, bottom);
//                ur = new Coordinate(right, top);
//
//                map2D.getRenderingStrategy().setMapArea(new Envelope(ll, ur));
////                        mapArea = fixAspectRatio(getBounds(), new Envelope(ll, ur));
//
//            } else {
//                // move the image with the mouse
//                // calculate X offsets from start point to the end Point
//                double deltaX1 = endX - startX;
//
//                // System.out.println("deltaX " + deltaX1);
//                // new edges
//                left = mapArea.getMinX() - deltaX1;
//                right = mapArea.getMaxX() - deltaX1;
//
//                // now for Y
//                double deltaY1 = endY - startY;
//
//                // System.out.println("deltaY " + deltaY1);
//                bottom = mapArea.getMinY() - deltaY1;
//                top = mapArea.getMaxY() - deltaY1;
//                ll = new Coordinate(left, bottom);
//                ur = new Coordinate(right, top);
//
//                map2D.getRenderingStrategy().setMapArea(new Envelope(ll, ur));
//
//
//            }
        }

        public void mouseClicked(MouseEvent e) {

            mousebutton = e.getButton();

            // left mouse button
            if (e.getButton() == MouseEvent.BUTTON1) {                                
                scale(e.getPoint(), zoomFactor);                


            } //right mouse button : pan action
//            else if (e.getButton() == MouseEvent.BUTTON3) {
//                zlevel = 1.0;
//                Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY - (height2 / zlevel));
//                Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY + (height2 / zlevel));
//                map2D.getRenderingStrategy().setMapArea(new Envelope(ll, ur));
//            }

        }

        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            lastX = 0;
            lastY = 0;

            mousebutton = e.getButton();
            if (mousebutton == MouseEvent.BUTTON1) {

            } else if (mousebutton == MouseEvent.BUTTON3) {
//                zoompanPanel.setCoord(0, 0, map2D.getComponent().getWidth(), map2D.getComponent().getHeight(), true);
            }


        }

        public void mouseReleased(MouseEvent e) {
            int endX = e.getX();
            int endY = e.getY();


            if (mousebutton == MouseEvent.BUTTON1) {

                if(startX != endX && startY != endY){
                    zoom(startX,startY,endX,endY);
                }
                
                int width = map2D.getComponent().getWidth() / 2;
                int height = map2D.getComponent().getHeight() / 2;
                int left = e.getX() - (width / 2);
                int bottom = e.getY() - (height / 2);

            } //right mouse button : pan action
//            else if (mousebutton == MouseEvent.BUTTON3) {
//                zoompanPanel.setFill(false);
//                zoompanPanel.setCoord(0, 0, 0, 0, false);
//                processDrag(startX, startY, endX, endY, true);
//            }

            lastX = 0;
            lastY = 0;

        }

        public void mouseEntered(MouseEvent e) {
            map2D.getComponent().requestFocus();
            map2D.getComponent().setCursor(CUR_ZOOM_IN);
        }

        public void mouseExited(MouseEvent e) {
//            zoompanPanel.setFill(false);
//            zoompanPanel.setCoord(0, 0, 0, 0, true);
        }

        public void mouseDragged(MouseEvent e) {
//            int x = e.getX();
//            int y = e.getY();
//
//
//            // left mouse button
//            if (mousebutton == MouseEvent.BUTTON1) {
//
//                if ((lastX > 0) && (lastY > 0)) {
//                    drawRectangle(true, true);
//                }
//
//                // draw new box
//                lastX = x;
//                lastY = y;
//                drawRectangle(true, true);
//
//            } //right mouse button : pan action
//            else if (mousebutton == MouseEvent.BUTTON3) {
//                if ((lastX > 0) && (lastY > 0)) {
//                    int dx = lastX - startX;
//                    int dy = lastY - startY;
//                    zoompanPanel.setFill(false);
//                    zoompanPanel.setCoord(dx, dy, map2D.getComponent().getWidth(), map2D.getComponent().getHeight(), true);
//                }
//                lastX = x;
//                lastY = y;
//
//
//            }



        }

        public void mouseMoved(MouseEvent e) {

//            int width = map2D.getComponent().getWidth() / 2;
//            int height = map2D.getComponent().getHeight() / 2;
//
//            int left = e.getX() - (width / 2);
//            int bottom = e.getY() - (height / 2);

//            zoompanPanel.setFill(false);
//            zoompanPanel.setCoord(left, bottom, width, height, true);


        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int rotate = e.getWheelRotation();
            
            if(CTRL_FLAG){
                if(rotate<0){
                    map2D.getCanvas().getController().rotate(Math.toRadians(-5));               
                }else if(rotate>0){
                    map2D.getCanvas().getController().rotate(Math.toRadians(5));                    
                }
            }else{
                if(rotate<0){
                    scale(e.getPoint(),zoomFactor);                
                }else if(rotate>0){
                    scale(e.getPoint(),1d/zoomFactor);                
                }
            }
        }

        public void keyTyped(KeyEvent evt) {
        }

        public void keyPressed(KeyEvent evt) {
            if(KeyEvent.VK_CONTROL == evt.getKeyCode()){
                CTRL_FLAG = true;
            }
        }

        public void keyReleased(KeyEvent evt) {
            CTRL_FLAG = false;
        }
    }

    public void setCanvas(AWTCanvas2D canvas) {
        
    }

    public AWTCanvas2D getCanvas() {
        return map2D.getCanvas();
    }

    public void install(Component component) {
        installed = true;
        map2D.getComponent().addMouseListener(mouseInputListener);
        map2D.getComponent().addMouseMotionListener(mouseInputListener);
        map2D.getComponent().addMouseWheelListener(mouseInputListener);
        map2D.getComponent().addKeyListener(mouseInputListener);
    }

    public void uninstall(Component component) {
        map2D.getComponent().removeMouseListener(mouseInputListener);
        map2D.getComponent().removeMouseMotionListener(mouseInputListener);
        map2D.getComponent().removeMouseWheelListener(mouseInputListener);
        map2D.getComponent().removeKeyListener(mouseInputListener);
        installed = false;
    }
}
