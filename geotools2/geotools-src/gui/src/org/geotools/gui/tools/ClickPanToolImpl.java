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

/**
 * Pan the map so that the new extent has the click point in the middle
 * of the map and then zoom in/out by the zoomFactor.
 * @version $Id: ClickPanToolImpl.java,v 1.1 2003/03/30 11:22:26 camerons Exp $
 * @author Cameron Shorter
 */
public class ClickPanToolImpl extends PanToolImpl implements ClickPanTool
{

    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.tools.ClickPanImpl");

    private Adapters adapters = Adapters.getDefault();

    /**
     * Construct a ZoomTool.
     */
    public ClickPanToolImpl(){
        setName("Click Pan");
    }

    /**
     * Do nothing for mousePressed.
     */
    public void mousePressed(MouseEvent e)
    {
    }

    /**
     * Do nothing for mouseReleased.
     */
    public void mouseReleased(MouseEvent e){
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
