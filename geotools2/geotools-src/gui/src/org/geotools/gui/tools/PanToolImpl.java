package org.geotools.gui.tools;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Component;
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
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;

public class PanToolImpl extends AbstractTool implements PanTool {

    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.tools.PanToolImpl");

    private Adapters adapters = Adapters.getDefault();
    private AffineTransform at = new AffineTransform();
    private CoordinatePoint pressPoint;
    private CoordinatePoint releasePoint;

    /**
     * Construct a PanTool.
     * @version $Id: PanToolImpl.java,v 1.15 2003/03/30 11:22:01 camerons Exp $
     * @author Cameron Shorter
     */
    public PanToolImpl(){
        setName("Pan");
    }

    /**
     * Process Click and Drag Pan.
     * @param e The mouse clicked event.
     */
    public void mouseClicked(MouseEvent e) {
        try {
            // The real world coordinates of the mouse click
            CoordinatePoint
                mousePoint=((GeoMouseEvent)e).getMapCoordinate(null);
            Envelope aoi=context.getBbox().getAreaOfInterest();
            
            at.setToIdentity();
            at.translate(
                mousePoint.getOrdinate(0),
                mousePoint.getOrdinate(1));
            at.translate(
                -(aoi.getMinX()+aoi.getMaxX())/2,
                -(aoi.getMinY()+aoi.getMaxY())/2);

            MathTransform transform=
                MathTransformFactory.getDefault().createAffineTransform(at);
            
            context.getBbox().transform(adapters.export(transform));
        } catch (TransformException t) {
            LOGGER.warning(
            "Transform exception prevented mouseClicks from being processed");
        }
    }
    
    /**
     * Set the press point in a click-drag operation.
     * @param e contains the mouse click.
     */
    public void mousePressed(MouseEvent e)
    {
        try{
            pressPoint=((GeoMouseEvent)e).getMapCoordinate(pressPoint);
        } catch (TransformException t) {
            LOGGER.warning(
            "Transform exception prevented mouseClicks from being processed");
        }
    }

    /**
     * Set the release point in a click-drag operation and process the drag
     * operation.
     * @param e contains the mouse click.
     */
    public void mouseReleased(MouseEvent e)
    {
        try {
            releasePoint=((GeoMouseEvent)e).getMapCoordinate(releasePoint);

            // Don't process mouse drag if this is a mouse click.
            if (releasePoint.equals(pressPoint)){
                return;
            }
        
            at.setToIdentity();
            at.translate(
                pressPoint.getOrdinate(0)-releasePoint.getOrdinate(0),
                pressPoint.getOrdinate(1)-releasePoint.getOrdinate(1));

            MathTransform transform=
                MathTransformFactory.getDefault().createAffineTransform(at);
            
            context.getBbox().transform(adapters.export(transform));
        } catch (TransformException t) {
            LOGGER.warning(
            "Transform exception prevented mouseClicks from being processed");
        }
    }
    
    /**
     * Register this tool to receive MouseEvents from <code>component<code>.
     * @param component The tool will process mouseEvents from this component.
     * @param context The Context that will be changed by this Tool.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     * or the tool is being assigned a different context to before.
     */
    public void addMouseListener(Component component, Context context)
    {
        super.addMouseListener(
            component,context,this);
    }
}