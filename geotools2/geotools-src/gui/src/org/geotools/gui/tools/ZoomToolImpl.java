package org.geotools.gui.tools;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.logging.Logger;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JComponent;
import org.geotools.ct.Adapters;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.MathTransform;
import org.geotools.ct.TransformException;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.gui.tools.MouseToolImpl;
import org.geotools.gui.widget.Widget;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;

public class ZoomToolImpl extends MouseToolImpl implements ZoomTool
{

    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.tools.ZoomToolImpl");

    private Adapters adapters = Adapters.getDefault();

    /** The factor to zoom in out by, zoomFactor=0.5 means zoom in,
     * zoomFactor=2 means zoom out. Defaults to 2.
     */
    private double zoomFactor=2;

    /**
     * Construct a ZoomTool.
     * @version $Id: ZoomToolImpl.java,v 1.2 2003/03/20 20:23:06 camerons Exp $
     * @author Cameron Shorter
     */
    public ZoomToolImpl(){
    }
    
    /**
     * Set up Click/Zoom.
     * Pan the map so that the new extent has the click point in the middle
     * of the map and then zoom in/out by the zoomFactor.
     * @param e The mouse clicked event.
     * @throws IllegalStateException If context has not been
     * initialized yet.
     * @task HACK The transform doesn't seem to take account of CoordinateSystem
     * so it would be possible to create Coordinates which are outside real
     * world coordinates.
     * @task TODO Use an AffineTransform type interface to Bbox instead of
     * setAreaOfInterest
     */
    public void mouseClicked(MouseEvent e) {
        GeoMouseEvent geoMouseEvent=(GeoMouseEvent)e;
        Envelope aoi=context.getBbox().getAreaOfInterest();
        
        try {
            // The real world coordinates of the mouse click
            CoordinatePoint mousePoint=geoMouseEvent.getMapCoordinate(null);

            // The real world coordinates of the AreaOfInterest
            Point2D minP=new Point2D.Double(aoi.getMinX(),aoi.getMinY());
            Point2D maxP=new Point2D.Double(aoi.getMaxX(),aoi.getMaxY());
            
            // ZoomTransform:
            // x=scaleFactor*x + (1-zoomFactor)*(width/2-minX)
            
            AffineTransform at = new AffineTransform();
            
            // Pan so the middle of the map is the mouse click point.
            // x=x+clickedPoint-midpoint
            at.translate(
                mousePoint.getOrdinate(0)-(minP.getX()+maxP.getX())/2,
                mousePoint.getOrdinate(1)-(minP.getY()+maxP.getY())/2);

            // Move the trasformed box so the center point remains in the same
            // place after zooming.
            // x=x+(1-zoomFactor)*(width/2-minX)
            at.translate(
                (1-zoomFactor)*((maxP.getX()-minP.getX())/2)-minP.getX(),
                (1-zoomFactor)*((maxP.getY()-minP.getY())/2)-minP.getY());
            
            // Zoom by zoomFactor
            // x=x*zoomFactor
            at.scale(zoomFactor,zoomFactor);

            MathTransform transform=
                MathTransformFactory.getDefault().createAffineTransform(at);
            
            context.getBbox().transform(adapters.export(transform));
        } catch (TransformException t) {
            LOGGER.warning(
            "Transform exception prevented mouseClicks from being processed");
        }
    }

    /*
     * Set up Click and Drap Pan.
     */
    //public void ...
    
    
    /**
     * Register for mouse events from the widget.
     * @param widget The widget.
     * @throws IllegalStateException if the widget has already been set to
     * another widget.
     */
    public void setWidget(Widget widget) throws IllegalStateException {
        super.setWidget(widget,this);
    }

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in,
     * zoomFactor=2 means zoom out. Defaults to 2.
     */
    public void setZoomFactor(double zoomFactor){
        this.zoomFactor=zoomFactor;
    }

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in,
     * zoomFactor=2 means zoom out. Defaults to 2.
     */
    public double getZoomFactor(){
        return zoomFactor;
    }
}
