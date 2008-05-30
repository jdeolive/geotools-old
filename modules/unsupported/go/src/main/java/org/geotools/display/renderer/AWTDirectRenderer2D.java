/*
 *    GeoTools - An Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.display.renderer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.display.canvas.ReferencedCanvas2D;
import org.geotools.display.primitive.AbstractGraphic;
import org.geotools.display.primitive.GraphicJ2D;
import org.geotools.factory.Hints;

import org.opengis.display.canvas.Canvas;
import org.opengis.display.primitive.Graphic;
import org.opengis.referencing.operation.TransformException;

/**
 * This is simplest implementation of AbstractRenderer2D.
 * No image cache are performed by this renderer, it only paints
 * graphics using the provided Graphics2D object.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class AWTDirectRenderer2D extends AbstractRenderer2D{

    /**
     * The canvas associated this renderer.
     */
    protected ReferencedCanvas2D canvas;

    /**
     * Create a 2D renderer with no particular hints.
     */
    protected AWTDirectRenderer2D(){
        super(null);
    }

    /**
     * Create a 2D renderer with particular hints.
     * 
     * @param hints Hints object or null, if null the renderer will create
     * an empty Hints object.
     */
    protected AWTDirectRenderer2D(Hints hints){
        super(hints);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException is throw if the canvas
     * is not supported by this renderer.
     */
    public void setCanvas(Canvas canvas)throws IllegalArgumentException {
        if(canvas != null && canvas != this.canvas){
            
            if(canvas instanceof ReferencedCanvas2D){
                final ReferencedCanvas2D oldCanvas = this.canvas;
                this.canvas = (ReferencedCanvas2D) canvas;
            
                propertyListeners.firePropertyChange(CANVAS_PROPERTY, this, this.canvas);
            }else{
                throw new IllegalArgumentException("This canvas is not support by the renderer");
            }
            
        }
        this.canvas = (ReferencedCanvas2D) canvas;
        
        
        
    }

    /**
     * {@inheritDoc}
     */
    public ReferencedCanvas2D getCanvas() {
        return canvas;
    }

    /**
     * Convient method that calls the setCanvas method on all graphics.
     */
    protected void canvasChanged(){
        synchronized(canvas){
            Collection<Graphic> graphics = getGraphics();
            
            for(Graphic graphic : graphics){
                if(graphic instanceof AbstractGraphic){
                    ((AbstractGraphic)graphic).setCanvas(canvas);
                }
            }
        }
    }
    
    /**
     * Paint all graphics with the given AffineTransform and Graphics2D.
     * 
     * @param output : Graphics2D object to use.
     * @param objToDisp : Objective to Display transform to use.
     * 
     * @return true if the painting has succeed.
     */
    public boolean paint(Graphics2D output, AffineTransform objToDisp) {

        output.addRenderingHints(hints);

        final Rectangle displayBounds = canvas.getDisplayBounds().getBounds();
        
        final RenderingContext context = new RenderingContext(getCanvas(), displayBounds, false);
        context.setGraphics(output, objToDisp);

        /*
         * Draw all graphics, starting with the one with the lowest <var>z</var> value. Before
         * to start the actual drawing,  we will notify all graphics that they are about to be
         * drawn. Some graphics may spend one or two threads for pre-computing data.
         */
        final List<Graphic> graphics = getSortedGraphics();

        for(Graphic graphic : graphics){
            if(graphic instanceof GraphicJ2D){
                try {
                    ((GraphicJ2D) graphic).paint(context);
                } catch (TransformException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(AWTDirectRenderer2D.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return true;
    }

}