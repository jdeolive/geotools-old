/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.gui.swing;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.util.EventObject;

import com.vividsolutions.jts.geom.Envelope;

/**
 *
 * @author  jamesm
 */
public class NavigationPane extends javax.swing.JComponent implements org.geotools.map.events.AreaOfInterestChangedListener{
    /**
     * The maximum extent of any aoi to be displayed.
     * This determins the extent of the map displayed in this panel,
     * and not the extent of the navigation rectangle displayed.
     */
    private Envelope totalAoi;
    
    /**
     * This records the current area of interest to be displayed
     * as a rectangle.<br>
     * In order to be displayed properly the subAoi should
     * be within the toalAoi.<br>
     * The subAoi is updated whenever the AoI Model that this component 
     * is listening to fires a changed event.
     */
    private Envelope subAoi;
    
    /** Holds value of property aoiModel. */
    private org.geotools.map.AreaOfInterestModel aoiModel;
    
    /** Creates a new instance of NavigationMap */
    public NavigationPane() {
        totalAoi = new Envelope(0,1000,0,1000);
    }
    
    /** 
     * Process an AreaOfInterestChangedEvent, probably involves a redraw.
     * @param areaOfInterestChangedEvent The new extent.
     */
    public void areaOfInterestChanged(EventObject aoiEvent) {
        subAoi = aoiModel.getAreaOfInterest();
        System.out.println(subAoi);
        repaint();
    }
    
    public void paintComponent(Graphics g){
        if(subAoi == null){
            return;
        }
        Graphics2D g2d = (Graphics2D)g;
        AffineTransform at = new AffineTransform();
        Dimension screenSize = this.getSize();
        
        double scale = Math.min(screenSize.getHeight()/totalAoi.getHeight(),
        screenSize.getWidth()/totalAoi.getWidth());
        //TODO: angle is almost certainly not needed and should be dropped
        double angle = 0;//-Math.PI/8d;// rotation angle
        double tx = -totalAoi.getMinX()*scale; // x translation - mod by ian
        double ty = totalAoi.getMinY()*scale + screenSize.getHeight();// y translation
        
        double sc = scale*Math.cos(angle);
        double ss = scale*Math.sin(angle);
        
        at = new AffineTransform(sc,-ss,ss,-sc,tx,ty);
        System.out.println("Scale X is " + at.getScaleX());
        g2d.setTransform(at);
        
        System.out.println(" " + subAoi.getMinX() + " " + subAoi.getMaxX());
        g2d.drawRect((int) subAoi.getMinX(), (int) subAoi.getMinY(),
                   (int) subAoi.getWidth(), (int) subAoi.getHeight());
    }
  
    /** 
     * Getter for property aoiModel.
     * @return Value of property aoiModel.
     */
    public org.geotools.map.AreaOfInterestModel getAoiModel() {
        return this.aoiModel;
    }
    
    /** 
     * Setter for property aoiModel.
     * @param aoiModel New value of property aoiModel.
     */
    public void setAoiModel(org.geotools.map.AreaOfInterestModel aoiModel) {
        this.aoiModel = aoiModel;
        subAoi = aoiModel.getAreaOfInterest();
        this.aoiModel.addAreaOfInterestChangedListener(this);
    }
    
    /** Getter for property totalAoi.
     * @return Value of property totalAoi.
     */
    public Envelope getTotalAoi() {
        return this.totalAoi;
    }
    
    /** Setter for property totalAoi.
     * @param totalAoi New value of property totalAoi.
     */
    public void setTotalAoi(Envelope totalAoi) {
        this.totalAoi = totalAoi;
    }
    
}
