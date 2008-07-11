/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
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
package org.geotools.display.primitive;


import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.display.canvas.AbstractCanvas;
import org.opengis.display.canvas.Canvas;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.display.canvas.ReferencedCanvas;
import org.geotools.display.event.ReferencedEvent;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A graphic implementation with default support for Coordinate Reference System (CRS) management.
 * This class provides some methods specific to the Geotools implementation of graphic primitive.
 * The {@link org.geotools.display.canvas.ReferencedCanvas} expects instances of this class.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 */
public abstract class ReferencedGraphic extends AbstractGraphic {
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas#getObjectiveCRS objective CRS} changed.
     */
    public static final String OBJECTIVE_CRS_PROPERTY = "objectiveCRS";
    
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event}
     * fired when the {@linkplain ReferencedCanvas#getEnvelope canvas envelope} or
     * {@linkplain ReferencedGraphic#getEnvelope graphic envelope} changed.
     */
    public static final String ENVELOPE_PROPERTY = "envelope";
    
    /**
     * An envelope that completly encloses the graphic. Note that there is no guarantee
     * that the returned envelope is the smallest bounding box that encloses the graphic,
     * only that the graphic lies entirely within the indicated envelope.
     * <p>
     * The {@linkplain GeneralEnvelope#getCoordinateReferenceSystem coordinate reference system}
     * of this envelope should always be the {@linkplain #getObjectiveCRS objective CRS}.
     */
    private final GeneralEnvelope envelope;

    /**
     * A typical cell dimension for this graphic, or {@code null} if none.
     *
     * @see #getTypicalCellDimension
     * @see #setTypicalCellDimension
     */
    private double[] typicalCellDimension;

    /**
     * Constructs a new graphic using the specified objective CRS.
     *
     * @param  crs The objective coordinate reference system.
     * @throws IllegalArgumentException if {@code crs} is null.
     *
     * @see #setObjectiveCRS
     * @see #setEnvelope
     * @see #setTypicalCellDimension
     * @see #setZOrderHint
     */
    protected ReferencedGraphic(final Canvas canvas, final CoordinateReferenceSystem crs)
            throws IllegalArgumentException {
        super(canvas);
        if (crs == null) {
            throw new IllegalArgumentException(Errors.getResources(getLocale())
                      .getString(ErrorKeys.ILLEGAL_ARGUMENT_$2, "crs", crs));
        }
        envelope = new GeneralEnvelope(crs);
        envelope.setToNull();
    }

    /**
     * {@inheritDoc }
     * <p>
     * The referenced graphic listen to objective crs changes to update
     * the envelope and fire an event if needed.
     * </p>
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        
        if(evt.getPropertyName().equals(AbstractCanvas.OBJECTIVE_CRS_PROPERTY)){
            final CoordinateReferenceSystem newCRS = (CoordinateReferenceSystem) evt.getNewValue();
            final CoordinateReferenceSystem oldCRS = (CoordinateReferenceSystem) evt.getOldValue();
            try {
                setObjectiveCRS(newCRS, oldCRS);
            } catch (TransformException ex) {
                ex.printStackTrace();
                Logger.getLogger(ReferencedGraphic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    /**
     * Sets the objective coordinate refernece system for this graphic. This method is usually
     * invoked in any of the following cases:
     * <p>
     * <ul>
     *   <li>From the graphic constructor.</li>
     *   <li>When this graphic has just been added to a canvas.</li>
     *   <li>When canvas objective CRS is modified.</li>
     * </ul>
     * <p>
     * This method transforms the {@linkplain #getEnvelope envelope} if needed. If a
     * subclass need to transform some additional internal data, it should override the
     * {@link #transform} method.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#OBJECTIVE_CRS_PROPERTY}
     * property change event.
     *
     * @param  crs The new objective CRS.
     * @throws TransformException If this method do not accept the new CRS. In such case,
     *         this method should keep the old CRS and leaves this graphic in a consistent state.
     */
    protected void setObjectiveCRS(final CoordinateReferenceSystem newCRS, final CoordinateReferenceSystem oldCRS) throws TransformException {
                if (newCRS == null) {
                    throw new IllegalArgumentException(Errors.getResources(getLocale())
                              .getString(ErrorKeys.ILLEGAL_ARGUMENT_$2, "crs", newCRS));
                }
                synchronized (getTreeLock()) {
                    if (CRS.equalsIgnoreMetadata(oldCRS, newCRS)) {
                        /*
                         * If the new CRS is equivalent to the old one (except for metadata), then there
                         * is no need to apply any transformation. Just set the new CRS.  Note that this
                         * step may throws an IllegalArgumentException if the given CRS doesn't have the
                         * expected number of dimensions (actually it should never happen, since we just
                         * said that this CRS is equivalent to the previous one).
                         */
                        envelope.setCoordinateReferenceSystem(newCRS);
                    } else {
                        /*
                         * If a coordinate transformation is required, gets the math transform preferably
                         * from the Canvas that own this graphic (in order to use any user supplied hints).
                         */
                        final MathTransform transform;
                        transform = getMathTransform(oldCRS, newCRS, "setObjectiveCRS");
                        if (!transform.isIdentity()) {
                            /*
                             * Transforms the envelope, but do not modify yet the 'envelope' field.
                             * This change will be commited only after all computations have been successful.
                             */
                            final GeneralEnvelope newEnvelope;
                            final DirectPosition origin;
                            if (envelope.isNull() || envelope.isInfinite()) {
                                origin = new GeneralDirectPosition(oldCRS);
                                newEnvelope = new GeneralEnvelope(envelope);
                            } else {
                                origin = envelope.getCenter();
                                newEnvelope = CRS.transform(transform, envelope);
                            }
                            newEnvelope.setCoordinateReferenceSystem(newCRS);
                            /*
                             * Transforms the cell dimension. Only after all computations are successful,
                             * commit the changes to the 'envelope' and typicalCellDimension' class fields.
                             */
                            double[] cellDimension = typicalCellDimension;
                            if (cellDimension != null) {
                                DirectPosition vector = new GeneralDirectPosition(cellDimension);
                                vector = CRSUtilities.deltaTransform(transform, origin, vector);
                                cellDimension = vector.getCoordinates();
                                for (int i=0; i<cellDimension.length; i++) {
                                    cellDimension[i] = Math.abs(cellDimension[i]);
                                }
                            }
                            transform(transform);
                            envelope.setEnvelope(newEnvelope);
                            typicalCellDimension = cellDimension;
                        }
                    }
                    propertyListeners.firePropertyChange(OBJECTIVE_CRS_PROPERTY, oldCRS, newCRS);
                }
            }

    /**
     * Constructs a transform between two coordinate reference systems.
     *
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The target coordinate reference system.
     * @param  sourceMethodName The caller method name, for logging purpose only.
     * @return A transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws TransformException if the transform can't be created.
     */
    private MathTransform getMathTransform(final CoordinateReferenceSystem sourceCRS,
                                           final CoordinateReferenceSystem targetCRS,
                                           final String sourceMethodName)
            throws TransformException{

        try {


            return CRS.findMathTransform(sourceCRS, targetCRS, true);

//            final Canvas owner = getCanvas();
//            if (owner instanceof ReferencedCanvas) {
//                return ((ReferencedCanvas) owner).getMathTransform(sourceCRS, targetCRS,
//                       ReferencedGraphic.class, sourceMethodName);
//            } else {
//                return ReferencingFactoryFinder.getCoordinateOperationFactory(null)
//                       .createOperation(sourceCRS, targetCRS).getMathTransform();
//            }
        } catch (FactoryException exception) {
            throw new TransformException(Errors.getResources(getLocale()).getString(
                        ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), exception);
        }
    }

    /**
     * Notifies subclasses that a new {@linkplain #getObjectiveCRS objective CRS} has been set
     * and the internal data should be transformed accordingly. This method is a hook invoked
     * automatically by {@link #setObjectiveCRS}. The default implementation does nothing.
     * Subclasses should override this method if they need to transform their internal data.
     * <p>
     * When {@link #setObjectiveCRS setObjectiveCRS} invokes this method, this {@code Graphic}
     * object still in its old state. This method should have a <cite>all or nothing</cite>
     * behavior: in case of failure, it should throws an exception and leave this {@code Graphic}
     * as if no change were applied at all.
     *
     * @param mt The math transform from the old objective CRS to the new one.
     * @throws TransformException If a transformation failed.
     */
    protected void transform(final MathTransform mt) throws TransformException {
    }

    /**
     * Returns an envelope that completly encloses the graphic. Note that there is no guarantee
     * that the returned envelope is the smallest bounding box that encloses the graphic, only
     * that the graphic lies entirely within the indicated envelope.
     * <p>
     * The default implementation returns a {@linkplain GeneralEnvelope#setToNull null envelope}.
     * Subclasses should compute their envelope and invoke {@link #setEnvelope} as soon as they can.
     *
     * @see #setEnvelope
     */
    public Envelope getEnvelope() {
        synchronized (getTreeLock()) {
            return new GeneralEnvelope(envelope);
        }
    }

    /**
     * Set the envelope for this graphic. Subclasses should invokes this method as soon as they
     * known their envelope.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#ENVELOPE_PROPERTY}
     * property change event.
     *
     * @throws TransformException if the specified envelope can't be transformed to the
     *         {@linkplain #getObjectiveCRS objective CRS}.
     */
    protected void setEnvelope(final Envelope newEnvelope) throws TransformException {
        final GeneralEnvelope old;
        synchronized (getTreeLock()) {
            
            
            CoordinateReferenceSystem sourceCRS = canvas.getState().getObjectiveCRS();
            CoordinateReferenceSystem targetCRS = newEnvelope.getCoordinateReferenceSystem();
            if (targetCRS == null) {
                targetCRS = sourceCRS;
            }
            
            //-------------------------------- strange behavior
            
            final MathTransform mt;
            mt = getMathTransform(sourceCRS, targetCRS, "setEnvelope");
            old = new GeneralEnvelope(envelope);
            envelope.setEnvelope(CRS.transform(mt, newEnvelope));
            assert envelope.getCoordinateReferenceSystem() == old.getCoordinateReferenceSystem();
            propertyListeners.firePropertyChange(ENVELOPE_PROPERTY, old, envelope);
        }
    }

    /**
     * Returns a typical cell dimension in terms of {@linkplain #getObjectiveCRS objective CRS}.
     * For images, this is the pixels size in "real world" units. For other kind of graphics, "cell
     * dimension" are to be understood as some dimension representative of the graphic resolution.
     *
     * @param  position The position where to evaluate the cell dimension. In the default
     *         implementation, this position is ignored.
     * @return A typical cell size in {@linkplain #getObjectiveCRS objective CRS},
     *         or {@code null} if none.
     */
    public double[] getTypicalCellDimension(final DirectPosition position) {
        synchronized (getTreeLock()) {
            return (typicalCellDimension!=null) ? (double[]) typicalCellDimension.clone() : null;
        }
    }

    /**
     * Set the typical cell dimension. Subclasses may invoke this method after they computed
     * some typical value. The default implementation of {@link #getTypicalCellDimension}
     * will returns this value for all positions.
     *
     * @param  size A typical cell size, in terms of objective CRS.
     * @throws MismatchedDimensionException if the specified cell size doesn't have the
     *         expected number of dimensions.
     */
    protected void setTypicalCellDimension(final double[] size)
            throws MismatchedDimensionException
    {
        synchronized (getTreeLock()) {
            if (size != null) {
                final int dimension = size.length;
                final int expectedDimension = envelope.getDimension();
                if (dimension != expectedDimension) {
                    throw new MismatchedDimensionException(Errors.getResources(getLocale()).
                              getString(ErrorKeys.MISMATCHED_DIMENSION_$3,
                              new Integer(dimension), new Integer(expectedDimension)));
                }
            }
            final double[] oldSize = typicalCellDimension;
            typicalCellDimension = (size!=null) ? (double[])size.clone() : null;
        }
    }

    /**
     * Returns the string to be used as the tooltip for a given event. The default implementation
     * always returns {@code null}. Subclasses should override this method if they can provide
     * tool tips for some location.
     *
     * @param  event The event.
     * @return The tool tip text, or {@code null} if there is no tool tip for the given location.
     *
     * @see ReferencedCanvas#getToolTipText
     */
    public String getToolTipText(final ReferencedEvent event) {
        return null;
    }

    /**
     * Formats a value for the specified event position. This method doesn't have to format the
     * coordinate (this is {@link MouseCoordinateFormat#format(GeoMouseEvent)} business). Instead,
     * it is invoked for formatting a value at the specified event position. For example a remote
     * sensing image of <cite>Sea Surface Temperature</cite> (SST) can format the temperature in
     * geophysical units (e.g. "12°C"). The default implementation do nothing and returns
     * {@code false}.
     *
     * @param  event The event.
     * @param  toAppendTo The destination buffer for formatting a value.
     * @return {@code true} if this method has formatted a value, or {@code false} otherwise.
     *
     * @see ReferencedCanvas#format
     * @see MouseCoordinateFormat#format(GeoMouseEvent)
     */
    public boolean format(final ReferencedEvent event, final StringBuffer toAppendTo) {
        return false;
    }
}
