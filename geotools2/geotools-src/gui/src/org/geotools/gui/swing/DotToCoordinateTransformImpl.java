package org.geotools.gui.swing;

/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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


import com.vividsolutions.jts.geom.Envelope;
import java.awt.geom.AffineTransform;
import java.util.logging.Logger;
import org.geotools.gui.swing.MapPaneImpl;
import org.geotools.map.Context;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * This class provides a transformation for Screen Coordinates to real world
 * Coordinates.  The transform needs to be updated whenever MapPane,
 * Context.boundingBox, or Context.coordinateSystem changes.
 * @version $Id: DotToCoordinateTransformImpl.java,v 1.1 2003/03/16 04:20:14 camerons Exp $
 * @author Cameron Shorter
 * @task TODO Setup an internal hash map which provides
 * getTransform(CoordinateSystem).
 */

public class DotToCoordinateTransformImpl
{
    private AffineTransform transform;
    private MapPaneImpl mapPane;
    private Context context;
    
    /**
     * The class used for identifying for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.swing.DotToCoordinateTransform");

    /**
     * Construct DotToCoordinateTransform.
     */
    public DotToCoordinateTransformImpl(MapPaneImpl mapPane, Context context) {
        this.mapPane=mapPane;
        this.context=context;
    }

    /**
     * Get the transform from Screen to Real World Cordinates using
     * Coordinate System from Context.GetBoundingBox().GetCoordinateSystem().
     */
    public AffineTransform getTransform() {
        if (transform==null){
            updateTransform();
        }
        return transform;
    }

    /**
     * Get the transform from Screen to Real World Cordinates using the
     * provided coordinate system.
     * @task This method has not been written yet.
     */
    public AffineTransform getTransform(CS_CoordinateSystem cs) {
        if (transform==null){
            updateTransform();
        }
        if (cs.equals(context.getBbox().getCoordinateSystem())){
            return transform;
        }else{
            LOGGER.warning(
                "transforming different coordinate systems not implemented,"+
                " returning default transform.");
            return transform;
        }
    }

    /**
     * Re-evaluate the transform, this method should be called by
     * MapPane whenever the MapPane widget resizes, boundingBox resizes,
     * or coordinateSystem changes.
     */
    public void updateTransform() {
        //Real World Coordinates
        Envelope aoi=context.getBbox().getAreaOfInterest();
        
        // Scaling
        double scaleX=(aoi.getMaxX()-aoi.getMinX())
            /(mapPane.getWidth()-mapPane.getInsets().left
              -mapPane.getInsets().right);
        double scaleY=(aoi.getMaxY()-aoi.getMinY())
            /(mapPane.getHeight()-mapPane.getInsets().top
              -mapPane.getInsets().bottom);

        // x'=(x-leftBorder)*scaleX+csMinX
        //   = x*scaleX -leftBorder*scaleX+csMinX
        // y'=(maxY-bottomBorder-y)*scaleY+csMinY
        //   =-y*scaleY+(maxY-bottomBorder)*scaleY+csMinY
        //
        // This equates to an AffineTransform:
        //
	// [ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
	// [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
	// [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]
        //
        // [x'] [scaleX  0       -leftBorder*scaleX+csMinX         ][x]
        // [y']=[0       -scaleY (maxY-bottomBorder)*scaleY+csMinY][y]
        // [1 ] [0       0       1                                ][1]

        transform=new AffineTransform(
            // m00: ScaleX
            scaleX,
            
            // m10
            0.0,
            
            // m01:
            0.0,
            
            // m11: -ScaleY
            -1*scaleY,
            
            // m02: TransformX
            aoi.getMinX()-scaleX*mapPane.getInsets().left,
            
            // m12: TransformY
            (mapPane.getHeight()-mapPane.getInsets().bottom)*scaleY
            +aoi.getMinY());
     }
}
