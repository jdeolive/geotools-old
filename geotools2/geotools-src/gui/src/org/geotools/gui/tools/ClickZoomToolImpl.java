package org.geotools.gui.tools;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Component;
import java.awt.Cursor;
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

/**
 * Pan the map so that the new extent has the click point in the middle
 * of the map and then zoom in/out by the zoomFactor.
 * @version $Id: ClickZoomToolImpl.java,v 1.3 2003/05/02 10:54:57 desruisseaux Exp $
 * @author Cameron Shorter
 */
public class ClickZoomToolImpl extends AbstractTool implements ClickZoomTool
{

    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.tools.ClickZoomToolImpl");

    private Adapters adapters = Adapters.getDefault();

    /** The factor to zoom in/out by, zoomFactor=0.5 means zoom in,
     * zoomFactor=2 means zoom out. Defaults to 2.
     */
    private double inverseZoomFactor=0.5;

    /**
     * Construct a ZoomTool.
     */
    public ClickZoomToolImpl(){
        setName("Click Zoom");
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Construct a ZoomTool.
    /* @parma zoomFactor he factor to zoom in/out by, zoomFactor=0.5 means
     * zoom in, zoomFactor=2 means zoom out.
     */
    public ClickZoomToolImpl(double zoomFactor){
        this.inverseZoomFactor=1/zoomFactor;
        if (zoomFactor==1){
            setName("Pan");
        }else if (zoomFactor<1){
            setName("Click Zoom In");
        }else{
            setName("Click Zoom Out");
        }
    }
    
    /**
     * Set up Click/Zoom.
     * Pan the map so that the new extent has the click point in the middle
     * of the map and then zoom in/out by the zoomFactor.
     * @param e The mouse clicked event.
     */
    public void mouseClicked(MouseEvent e) {
        try {
            // The real world coordinates of the mouse click
            CoordinatePoint
                mousePoint=((GeoMouseEvent)e).getMapCoordinate(null);
            Envelope aoi=context.getBbox().getAreaOfInterest();
            
            AffineTransform at = new AffineTransform();
            at.translate(mousePoint.getOrdinate(0), mousePoint.getOrdinate(1));
            at.scale(inverseZoomFactor, inverseZoomFactor);
            at.translate(
                -(aoi.getMinX()+aoi.getMaxX())/2,
                -(aoi.getMinY()+aoi.getMaxY())/2);

            MathTransform transform=
                MathTransformFactory.getDefault().createAffineTransform(at);
            
            context.getBbox().transform(adapters.export(transform));
        } catch (TransformException t) {
            LOGGER.warning(
            "Transform exception prevented mouseClicks from being processed");
        } catch (java.rmi.RemoteException t) {
            // TODO: We should not hide a checked exception that way.
            throw new java.lang.reflect.UndeclaredThrowableException(t, "Remote call failed");
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

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in,
     * zoomFactor=2 means zoom out. Defaults to 2.
     */
    public void setZoomFactor(double zoomFactor){
        this.inverseZoomFactor=1/zoomFactor;
    }

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in,
     * zoomFactor=2 means zoom out. Defaults to 2.
     */
    public double getZoomFactor(){
        return 1/inverseZoomFactor;
    }
}
