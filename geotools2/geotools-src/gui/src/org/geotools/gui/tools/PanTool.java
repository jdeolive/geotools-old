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
import org.geotools.gui.tools.MouseTool;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;

public class PanTool extends MouseTool {

    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.tools.PanTool");

    /**
     * Construct a PanTool.
     */
    public PanTool() {
    }
    
//    /**
//     * Construct a tool.
//     * @context Where state data for this mapPane is stored.
//     * @mapPane The mapPane from which this tool gets MouseEvents.
//     * @thows IllegalArgumentException
//     */
//    public PanTool(
//        Context context,
//        JComponent mapPane) throws IllegalArgumentException
//    {
//        super(context,mapPane);
//    }
    
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
     */
     public void mouseClicked(MouseEvent e)
     {
        if ((widget==null)||(context==null)){
            LOGGER.warning("Widget or Context is NULL");
        }else{
            AffineTransform at = new AffineTransform();
            // x=x(1+(clickX-w/2)/w)
            at.scale(
                1+(double)(e.getX()-widget.getWidth()/2)/(double)widget.getWidth(),
                1+(double)(e.getY()-widget.getHeight()/2)/(double)widget.getHeight());
            MathTransform2D mt=
                MathTransformFactory.getDefault().createAffineTransform(at);

            CoordinatePoint maxP = new CoordinatePoint(
                context.getBbox().getAreaOfInterest().getMaxX(),
                context.getBbox().getAreaOfInterest().getMaxY());
            CoordinatePoint minP = new CoordinatePoint(
                context.getBbox().getAreaOfInterest().getMinX(),
                context.getBbox().getAreaOfInterest().getMinY());

            try {
                maxP=mt.transform(maxP,maxP);
                minP=mt.transform(minP, minP);

                context.getBbox().setAreaOfInterest(
                    new Envelope(
                        minP.getOrdinate(0),   // minX
                        maxP.getOrdinate(0),   // maxX
                        minP.getOrdinate(1),   // minY
                        minP.getOrdinate(1))); // maxY
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
    public void setWidget(JComponent widget) throws IllegalStateException {
        super.setWidget(widget,this);
        if (this.widget!=null){
            widget.addMouseListener(this);
        }
    }
}
