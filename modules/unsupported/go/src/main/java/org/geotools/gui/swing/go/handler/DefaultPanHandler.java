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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.event.MouseInputListener;

import org.geotools.display.canvas.AWTCanvas2D;
import org.geotools.gui.swing.icon.IconBundle;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import org.geotools.gui.swing.go.GoMap2D;

/**
 *
 * @author johann sorel
 */
public class DefaultPanHandler implements CanvasHandler {

    private double zoomFactor = 2;
    
    
    private static final ImageIcon ICON = IconBundle.getResource().getIcon("16_select_default");
    private Cursor CUR_ZOOM_PAN;
    private static final String title = ResourceBundle.getBundle("org/geotools/gui/swing/map/map2d/handler/Bundle").getString("default");
    protected final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final MouseListen mouseInputListener = new MouseListen();
    private final GoMap2D map2D;
    private boolean installed = false;

    public DefaultPanHandler(GoMap2D map) {
        buildCursors();
        map2D = map;
    }

    private void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon ico_zoomPan = IconBundle.getResource().getIcon("16_zoom_pan");

        BufferedImage img3 = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img3.getGraphics().drawImage(ico_zoomPan.getImage(), 0, 0, null);
        CUR_ZOOM_PAN = tk.createCustomCursor(img3, new Point(1, 1), "in");
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
    
    //---------------------PRIVATE CLASSES--------------------------------------
    private class MouseListen implements MouseInputListener, MouseWheelListener {

        private int startX;
        private int startY;
        private int lastX;
        private int lastY;
        private int mousebutton = 0;

//        private void processDrag(int x1, int y1, int x2, int y2, boolean pan) {
//
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
//            //mapArea = fixAspectRatio(getBounds(), new Envelope(ll, ur));
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
//            }
//        }

        public void mouseClicked(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            lastX = startX;
            lastY = startY;
        }

        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            lastX = startX;
            lastY = startY;
        }

        public void mouseReleased(MouseEvent e) {
            int endX = e.getX();
            int endY = e.getY();

            lastX = 0;
            lastY = 0;
        }

        public void mouseEntered(MouseEvent e) {
            map2D.getComponent().setCursor(CUR_ZOOM_PAN);
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int dx = x - lastX ;
            int dy = y - lastY ;
            
            if(dx != 0 && dy != 0){
                map2D.getCanvas().getController().displayTranslate(dx, dy);
            }
            
            lastX = x;
            lastY = y;
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int rotate = e.getWheelRotation();
            
            if(rotate<0){
                scale(e.getPoint(),zoomFactor);                
            }else if(rotate>0){
                scale(e.getPoint(),1d/zoomFactor);                
            }
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
    }

    public void uninstall(Component component) {
        map2D.getComponent().removeMouseListener(mouseInputListener);
        map2D.getComponent().removeMouseMotionListener(mouseInputListener);
        map2D.getComponent().removeMouseWheelListener(mouseInputListener);
        installed = false;
    }
}
