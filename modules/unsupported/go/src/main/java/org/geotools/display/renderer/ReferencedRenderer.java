/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.display.renderer;

import org.geotools.display.canvas.ReferencedCanvas;
import org.geotools.display.primitive.ReferencedGraphic;
import org.geotools.geometry.GeneralEnvelope;

import org.opengis.display.primitive.Graphic;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author johann sorel
 */
public abstract class ReferencedRenderer extends AbstractRenderer{

    /**
     * Adds the given {@code Graphic} to this {@code Canvas}. This implementation respect the
     * <var>z</var>-order retrieved by calling {@link Graphic#getZOrderHint()}. When two added
     * {@code Graphic}s have the same <var>z</var>-order, the most recently added will be on top.
     * <p>
     * If no CRS were explicitly set to this canvas (either at construction time or through a call
     * to {@link #setObjectiveCRS setObjectiveCRS}), then this method will set the canvas objective
     * CRS to the CRS of the first graphic added.
     * <p>
     * This method fires {@value org.geotools.display.canvas.DisplayObject#GRAPHICS_PROPERTY} and
     * {@value org.geotools.display.canvas.DisplayObject#ENVELOPE_PROPERTY} property change events.
     */
    @Override
    public synchronized void add(Graphic graphic) {
        Envelope oldEnvelope = null;
        
        super.add(graphic);        
//        graphic = super.add(graphic); ----------------------------------------------------------- parent was cloning the graphic
        if (graphic instanceof ReferencedGraphic) {
            final ReferencedGraphic   referenced   = (ReferencedGraphic) graphic;
            CoordinateReferenceSystem graphicCRS   = referenced.getObjectiveCRS();
            CoordinateReferenceSystem objectiveCRS = getCanvas().getObjectiveCRS();
            
            //canvas will have a CRS selected by the user or we should set this value  when importing datas
            //not automaticly switching to the graphic CRS
//            if (useDefaultCRS) {
//                try {
//                    setObjectiveCRS(graphicCRS);
//                    objectiveCRS = graphicCRS;
//                    useDefaultCRS = false;
//                } catch (TransformException unexpected) {
//                    /*
//                     * Should not happen, since this canvas do not yet have any graphic.
//                     * Log the warning and continue with the Canvas CRS unchanged.
//                     */
//                    handleException("add", unexpected);
//                }
//            }
            /*
             * Now set the graphic CRS to this canvas CRS and update the canvas envelope.
             */
//            try {
//                referenced.setObjectiveCRS(objectiveCRS);
//            } catch (TransformException exception) {
//                throw new IllegalArgumentException(exception.getLocalizedMessage(), exception);
//            }
//            if (hasEnvelopeListeners) {   -----------------------------------------------------------------------OPTIMISATION
                oldEnvelope = new GeneralEnvelope(getCanvas().getEnvelope());
//            }
            graphicCRS = referenced.getObjectiveCRS(); // May have changed.
            final Envelope graphicEnvelope = referenced.getEnvelope();
            getCanvas().graphicEnvelopeChanged(null, graphicEnvelope, graphicCRS,ReferencedCanvas.class, "add");
        }
        
        // not the renderers works ! canvas should handle this
//        if (oldEnvelope != null) {
//            propertyListeners.firePropertyChange(ENVELOPE_PROPERTY, oldEnvelope, envelope);
//        }
//        return graphic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove(final Graphic graphic) {
        Envelope oldEnvelope = null;
        if (graphic instanceof ReferencedGraphic) {
            final ReferencedGraphic referenced = (ReferencedGraphic) graphic;
            if (referenced.getCanvas() == this) {
//                if (hasEnvelopeListeners) {
                    oldEnvelope = new GeneralEnvelope(getCanvas().getEnvelope());
//                }
                final CoordinateReferenceSystem graphicCRS = referenced.getObjectiveCRS();
                final Envelope graphicEnvelope = referenced.getEnvelope();
                getCanvas().graphicEnvelopeChanged(graphicEnvelope, null, graphicCRS,
                                       ReferencedCanvas.class, "remove");
            }
        }
        super.remove(graphic);
        
        //should be handle by the canvas    ---------------------------------------------------------------------
//        if (oldEnvelope != null) {
//            listeners.firePropertyChange(ENVELOPE_PROPERTY, oldEnvelope, envelope);
//        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeAll() {
        // envelope changes should be handle by the canvas ---------------------------------------------------------
//        final Envelope oldEnvelope = new GeneralEnvelope(envelope);
        super.removeAll();
//        envelope.setToNull();
//        listeners.firePropertyChange(ENVELOPE_PROPERTY, oldEnvelope, envelope);
    }

//    /**
//     * {@inheritDoc}
//     */
//    protected void graphicPropertyChanged(final AbstractGraphic graphic,
//                                          final PropertyChangeEvent event)
//    {
//        super.graphicPropertyChanged(graphic, event);
//        if (disableGraphicListener) {
//            return;
//        }
//        final String propertyName = event.getPropertyName();
//        final Envelope oldEnvelope;
//        if (propertyName.equalsIgnoreCase(ENVELOPE_PROPERTY)) {
//            oldEnvelope = hasEnvelopeListeners ? new GeneralEnvelope(envelope) : null;
//            final Object source = event.getSource();
//            final CoordinateReferenceSystem crs;
//            if (source instanceof ReferencedGraphic) {
//                crs = ((ReferencedGraphic) source).getObjectiveCRS();
//            } else {
//                crs = getObjectiveCRS();
//            }
//            graphicEnvelopeChanged((Envelope) event.getOldValue(),
//                                   (Envelope) event.getNewValue(),
//                                   crs, ReferencedGraphic.class, "setEnvelope");
//            // Note: we declare "ReferencedGraphic" instead of "ReferencedCanvas" as the source
//            //       class name because it is the one that trigged the envelope recomputation.
//        } else if (propertyName.equalsIgnoreCase(OBJECTIVE_CRS_PROPERTY)) {
//            oldEnvelope = new GeneralEnvelope(envelope);
//            computeEnvelope(ReferencedGraphic.class, "setObjectiveCRS");
//            // Note: we declare "ReferencedGraphic" instead of "ReferencedCanvas" as the source
//            //       class name because it is the one that trigged the envelope recomputation.
//        } else {
//            return;
//        }
//        if (oldEnvelope != null) {
//            listeners.firePropertyChange(ENVELOPE_PROPERTY, oldEnvelope, envelope);
//        }
//    }

    /**
     * Returns the top-most {@code Graphic} that occupies given direct position. The top-most
     * {@code Graphic} will have the highest <var>z</var>-order.
     *
     * @todo Not yet implemented.
     */
    public Graphic getTopGraphicAt(final DirectPosition directPosition) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@code Graphic}s that occupy the given direct position.
     *
     * @todo Not yet implemented.
     */
    public Graphic[] getGraphicsAt(final DirectPosition directPosition) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@code Graphic}s that occupy the given envelope.
     *
     * @todo Not yet implemented.
     */
    public Graphic[] getGraphicsIn(final Envelope bounds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract ReferencedCanvas getCanvas();
    
        
}
