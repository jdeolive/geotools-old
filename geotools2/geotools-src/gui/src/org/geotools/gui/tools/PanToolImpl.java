package org.geotools.gui.tools;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.logging.Logger;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JComponent;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.gui.tools.MouseToolImpl;
import org.geotools.gui.widget.Widget;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;

public class PanToolImpl extends MouseToolImpl implements PanTool {

    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.tools.PanToolImpl");

    /**
     * Construct a PanTool.
     */
    public PanToolImpl(){
    }
    
    /**
     * Set up Click/Pan.
     * Pan the map so that the new extent has the click point in the middle
     * of the map.
     * @param e The mouse clicked event.
     * @throws IllegalStateException If widget or context has not been
     * initialized yet.
     * @task HACK The transform doesn't seem to take account of CoordinateSystem
     * so it would be possible to create Coordinates which are outside real
     * world coordinates.
     * @task TODO Use an AffineTransform type interface to Bbox instead of
     * setAreaOfInterest
     */
     public void mouseClicked(MouseEvent e)
     {
        if ((widget==null)||(context==null)){
            LOGGER.warning("Widget or Context is NULL");
        }else{
            CoordinatePoint maxP = new CoordinatePoint(
                context.getBbox().getAreaOfInterest().getMaxX(),
                context.getBbox().getAreaOfInterest().getMaxY());
            CoordinatePoint minP = new CoordinatePoint(
                context.getBbox().getAreaOfInterest().getMinX(),
                context.getBbox().getAreaOfInterest().getMinY());

            AffineTransform at = new AffineTransform();
            // xScaleFactor=
            //    =1+screenScaleFactor*CSwidth/CSmaxX
            //    =1+((clickX-screenWidth/2)/screenWidth)*CSwidth/CSmidX
            // where:
            //  CS_Param=Param in Coordinate System units
            // note:
            //   (0,0) in screenCoords is top left of screen, while
            //   (0,0) in Cordinate System Coords is bottom left.
            at.scale(
                1+
                ((double)e.getX()-(double)widget.getWidth()/2)
                /widget.getWidth()
                *(maxP.getOrdinate(0)-minP.getOrdinate(0))
                /((minP.getOrdinate(0)+maxP.getOrdinate(0))/2),
            
                1+
                ((double)widget.getWidth()/2-(double)e.getY())
                /widget.getWidth()
                *(maxP.getOrdinate(1)-minP.getOrdinate(1))
                /((minP.getOrdinate(1)+maxP.getOrdinate(1))/2)
            );
             
           MathTransform2D mt=
                MathTransformFactory.getDefault().createAffineTransform(at);

            try {
                mt.transform(maxP,maxP);
                mt.transform(minP, minP);

                context.getBbox().setAreaOfInterest(
                    new Envelope(
                        minP.getOrdinate(0),   // minX
                        maxP.getOrdinate(0),   // maxX
                        minP.getOrdinate(1),   // minY
                        maxP.getOrdinate(1))); // maxY
            } catch (TransformException ex){
                LOGGER.warning("Exception: "+ex+" while transforming coordinates");
            }
        }
    }

    /*
     * Set up Click and Drap Pan.
     */
    //public void ...
    
    
    /**
     * Set the Widget which sends MouseEvents and contains widget size
     * information.
     * @param widget The widget to get size information from.
     * @throws IllegalStateException if the widget has already been set to
     * another widget.
     */
    public void setWidget(Widget widget) throws IllegalStateException {
        super.setWidget(widget,this);
    }
}
