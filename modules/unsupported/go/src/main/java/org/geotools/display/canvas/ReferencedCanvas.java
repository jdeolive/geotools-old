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
package org.geotools.display.canvas;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.beans.PropertyChangeEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import javax.swing.Action;

import org.opengis.display.primitive.Graphic;
import org.opengis.display.canvas.CanvasState;
import org.opengis.display.renderer.RendererEvent;
import org.opengis.util.InternationalString;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.GeneralDerivedCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;

import org.geotools.factory.Hints;
import org.geotools.util.Utilities;
import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Loggings;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.TransformedDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.matrix.MatrixFactory;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.display.event.ReferencedEvent;
import org.geotools.display.primitive.ReferencedGraphic;
import org.geotools.display.renderer.AbstractRenderer;


/**
 * A canvas implementation with default support for Coordinate Reference System (CRS) management.
 * This abstract class provides some common facilities for various implementations. This class
 * has no dependencies to the AWT or <cite>Java2D</cite> toolkits. Subclasses can choose an other
 * graphics toolkit (e.g. SWT) if they wish, including a 3D one.
 * <p>
 * Note that because this class is not tied to any widget toolkit, it has
 * no idea about what are the widget visible area bounds. For this class, the
 * {@linkplain org.geotools.display.canvas.map.DefaultMapState#getEnvelope canvas envelope}
 * is an envelope that completly encloses all graphic primitives, regardless of any map scale
 * or zoom factor. Subclasses like {@link ReferencedCanvas2D} will restrict that to an envelope
 * that encloses only the visible part of this canvas.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author johann Sorel (Geomatys)
 */
public abstract class ReferencedCanvas extends AbstractCanvas {
    /**
     * An envelope that completly encloses all {@linkplain ReferencedGraphic#getEnvelope graphic
     * envelopes} managed by this canvas. Note that there is no guarantee that the returned envelope
     * is the smallest bounding box that encloses the canvas, only that the canvas lies entirely
     * within the indicated envelope.
     * <p>
     * On {@code ReferencedCanvas} construction, this envelope is
     * {@linkplain GeneralEnvelope#setToNull initialised to NaN values}.
     * <p>
     * The {@linkplain GeneralEnvelope#getCoordinateReferenceSystem coordinate reference system}
     * of this envelope should always be the {@linkplain #getObjectiveCRS objective CRS}.
     *
     * @see #getEnvelope
     * @see #computeGraphicsEnvelope
     */
    private final GeneralEnvelope graphicsEnvelope;

    /**
     * A set of {@link MathTransform}s from various source CRS. The target CRS must be the
     * {@linkplain #getObjectiveCRS objective CRS} for all entries. Keys are source CRS.
     * This map is used only in order to avoid the costly call to
     * {@link CoordinateOperationFactory#createOperation} as much as possible. If a
     * transformation is not available in this collection, then the usual factory will be used.
     */
    private final transient Map<CoordinateReferenceSystem,MathTransform> transforms =
            new HashMap<CoordinateReferenceSystem,MathTransform>();

    /**
     * The display coordinate reference system.
     *
     * @see #getDisplayCRS
     * @see #setDisplayCRS
     */
    private DerivedCRS displayCRS;

    /**
     * The device coordinate reference system.
     *
     * @see #getDeviceCRS
     * @see #setDeviceCRS
     */
    private DerivedCRS deviceCRS;

    /**
     * Properties for the {@linkplain #displayCRS display CRS}. They are saved here because
     * {@link #displayCRS} will be recreated often (everytime the zoom change).
     */
    private Map displayProperties;

    /**
     * Properties for the {@linkplain #deviceCRS device CRS}. They are saved here because
     * {@link #deviceCRS} may be recreated often (everytime the zoom change).
     */
    private Map deviceProperties;

    /**
     * A temporary position used for coordinate transformations from an arbitrary CRS to the
     * objective CRS. This position CRS should always be identical to the {@linkplain #graphicsEnvelope}
     * CRS. This object will be created when first needed.
     */
    private transient TransformedDirectPosition objectivePosition;

    /**
     * A temporary position used for coordinate transformations from an arbitrary CRS to the
     * display CRS. This object will be created when first needed.
     */
    private transient TransformedDirectPosition displayPosition;

    /**
     * The {@code "affine"} operation method. Cached here because used often. Will be created
     * in same time than {@link #crsFactories}, since they are usually needed together.
     */
    private transient OperationMethod affineMethod;

    /**
     * Factories for CRS objects creation, coordinate operations and math transforms.
     * Will be created when first needed. The actual instance is {@link #hints} dependent.
     */
    private transient ReferencingFactoryContainer crsFactories;

    /**
     * The coordinate operation factory. Will be created when first needed.
     * The actual instance is {@link #hints} dependent.
     */
    private transient CoordinateOperationFactory opFactory;

    /**
     * {@code true} if this canvas use a {@link #getDefaultCRS default CRS} instead of
     * an user-supplied one. In such case, a more appropriate CRS will be inferred from
     * the first graphic {@linkplain #add added}.
     */
    private boolean useDefaultCRS;

    /**
     * {@code true} if this canvas or graphic has {@value #SCALE_PROPERTY} properties listeners.
     * Used in order to reduce the amount of {@link PropertyChangeEvent} objects created in the
     * common case where no listener have interest in this property. This optimisation may be
     * worth since a {@value #SCALE_PROPERTY} property change event is sent for every graphics
     * everytime a zoom change.
     *
     * @see #listenersChanged
     */
    private boolean hasScaleListeners;

    /**
     * {@code true} if this canvas has
     * {@value org.geotools.display.canvas.DisplayObject#DISPLAY_CRS_PROPERTY} properties
     * listeners. Used in order to reduce the amount of {@link PropertyChangeEvent} objects
     * created in the common case where no listener have interest in this property. May be
     * a significant optimisation, since this property change everytime the zoom change.
     *
     * @see #listenersChanged
     */
    private boolean hasDisplayListeners;

    /**
     * {@code true} if this canvas has
     * {@value org.geotools.display.canvas.DisplayObject#ENVELOPE_PROPERTY} properties listeners.
     * Note that it is not worth to check for this flag in the all cases; only in the most frequent
     * ones (e.g. {@link #add}, {@link #remove}...).
     *
     * @see #listenersChanged
     */
    private boolean hasEnvelopeListeners;


    /**
     * Creates an initially empty canvas with a default CRS of the specified number of dimensions.
     *
     * @param renderer
     * @param  dimension The number of dimensions, which must be 2 or 3.
     * @throws IllegalArgumentException if the specified number of dimensions is not supported.
     */
    protected ReferencedCanvas(final AbstractRenderer renderer, final int dimension)
            throws IllegalArgumentException
    {
        this(renderer,getDefaultCRS(dimension), null);
    }

    /**
     * Creates an initially empty canvas with the specified objective CRS.
     *
     * @param renderer
     * @param objectiveCRS The initial objective CRS.
     * @param hints        The initial set of hints, or {@code null} if none.
     */
    protected ReferencedCanvas(final AbstractRenderer renderer,
                               final CoordinateReferenceSystem objectiveCRS,
                               final Hints hints)
    {
        super(renderer, hints);
        this.graphicsEnvelope = new GeneralEnvelope(objectiveCRS);
        this.graphicsEnvelope.setToNull();
    }

    /**
     * Returns an envelope that completly encloses all {@linkplain ReferencedGraphic#getEnvelope
     * graphic envelopes} managed by this canvas. Note that there is no guarantee that the returned
     * envelope is the smallest bounding box that encloses the canvas, only that the canvas lies
     * entirely within the indicated envelope.
     * <p>
     * This envelope is different from
     * {@link org.geotools.display.canvas.map.DefaultMapState#getEnvelope}, since the later returns
     * an envelope that encloses only the <em>visible</em> canvas area and is scale-dependent. This
     * {@code ReferencedCanvas.getEnvelope()} method is scale-independent. Both envelopes are equal
     * if the scale is choosen in such a way that all graphics fit exactly in the canvas visible
     * area.
     *
     * @return The envelope for this canvas in terms of {@linkplain #getObjectiveCRS objective CRS}.
     *
     * @see org.geotools.display.canvas.map.DefaultMapState#getEnvelope
     * @see ReferencedCanvas2D#getEnvelope2D
     */
    protected synchronized Envelope getGraphicsEnvelope() {
        return new GeneralEnvelope(graphicsEnvelope);
    }

    /**
     * Same as {@link #getEnvelope}, but returns the bounds as an {@link Rectangle2D}.
     * This method will be made public by {@link ReferencedCanvas2D}.
     */
    synchronized Rectangle2D getGraphicsEnvelope2D() {
        return graphicsEnvelope.toRectangle2D();
    }

    /**
     * Returns a copy of the current state of this {@code Canvas}. The default implementation
     * returns a {@link DefaultCanvasState} with a center position inferred from the canvas
     * envelope.
     */
    public synchronized CanvasState getState() {

        //TODO : correct this method to return an immutable object
        InternationalString title = getTitle();
        DirectPosition center = new GeneralDirectPosition(displayPosition);
        CoordinateReferenceSystem objCRS = graphicsEnvelope.getCoordinateReferenceSystem();
        MathTransform objToDisp = getObjectiveToDisplayTransform();
        MathTransform dispToObj = getDisplayToObjectiveTransform();

        return new DefaultCanvasState(title, center, objCRS, displayCRS, objToDisp, dispToObj);
    }

    /**
     * Returns a typical cell dimension in terms of {@linkplain #getObjectiveCRS objective CRS}.
     * For images, a cell is usually a pixel. For other kind of graphics, "cell dimension" shall
     * be understood as some dimension representative of the graphic resolution. The default
     * implementation invokes <code>{@link ReferencedGraphic#getTypicalCellDimension
     * getTypicalCellDimension}(position)</code> for each graphic and returns the finest
     * resolution.
     *
     * @param  position A position where to evaluate the typical cell size, or {@code null} for
     *         a default one.
     * @return A typical cell dimension in terms of objective CRS, or {@code null} is no graphic
     *         provide such information.
     */
    public synchronized double[] getTypicalCellDimension(DirectPosition position) {
        final CoordinateReferenceSystem objectiveCRS = getObjectiveCRS();
        final double[] size = new double[objectiveCRS.getCoordinateSystem().getDimension()];
        Arrays.fill(size, Double.POSITIVE_INFINITY);
        if (objectivePosition == null) {
            objectivePosition = new TransformedDirectPosition(null, objectiveCRS, hints);
        }
        if (position != null) try {
            /*
             * If the user supplied a position, make sure that it uses the objective CRS. If a
             * transformation were required but failed, we will fallback on the default value.
             */
            objectivePosition.transform(position);
        } catch (TransformException exception) {
            handleException("getTypicalCellDimension", exception);
            position = null;
        }
        if (position == null) {
            /*
             * If the user didn't specified a position, use the envelope center by default.
             * Note that it may not be the visible area center, since this class doesn't
             * know what the visible area is.
             */
            for (int i=objectivePosition.getDimension(); --i>=0;) {
                objectivePosition.setOrdinate(i, graphicsEnvelope.getCenter(i));
            }
        }
        /*
         * Iterates over all graphics contained in this canvas. Only ReferencedGraphic instances
         * will be processed. Other kind of graphics will be ignored, since we don't know how to
         * handle them.
         */
        final Collection<Graphic> graphics = getRenderer().getGraphics();
        for (final Graphic candidate : graphics) {
            if (!(candidate instanceof ReferencedGraphic)) {
                continue;
            }
            final ReferencedGraphic graphic = (ReferencedGraphic) candidate;
            final double[] cellSize = graphic.getTypicalCellDimension(position);
            if (cellSize == null) {
                continue;
            }
            /*
             * Checks the graphic CRS, which should be the same than the objective CRS
             * in most case.
             */
            final CoordinateReferenceSystem graphicCRS = graphic.getObjectiveCRS();
            final MathTransform transform;
            try {
                transform = getMathTransform(graphicCRS, objectiveCRS,
                        ReferencedCanvas.class, "getTypicalCellDimension");
            } catch (FactoryException exception) {
                handleException("getTypicalCellDimension", exception);
                continue;  // Ignores this graphic and continue...
            }
            if (!transform.isIdentity()) try {
                /*
                 * In theory, the Graphic should use the same CRS than this Canvas. However as
                 * a safety, we will check for coordinate transformations anyway.  We create a
                 * cell at the position specified in argument and transform it to this CRS.
                 */
                GeneralEnvelope cellPrototype = new GeneralEnvelope(graphicCRS);
                position = objectivePosition.inverseTransform(graphicCRS);
                for (int j=position.getDimension(); --j>=0;) {
                    final double center = position.getOrdinate(j);
                    final double width  = (j<cellSize.length) ? cellSize[j] : 0;
                    cellPrototype.setRange(j, center-width, center+width);
                }
                cellPrototype = CRS.transform(transform, cellPrototype);
                for (int j=Math.min(cellSize.length, cellPrototype.getDimension()); --j>=0;) {
                    cellSize[j] = cellPrototype.getLength(j);
                }
            } catch (TransformException exception) {
                handleException("getTypicalCellDimension", exception);
                continue;  // Ignores this graphic and continue...
            }
            /*
             * Cell size is now in terms of Canvas objective CRS.
             * Search the smallest values along each dimension.
             */
            for (int j=Math.min(size.length, cellSize.length); --j>=0;) {
                final double c = cellSize[j];
                if (c>0 && c<size[j]) {
                    size[j] = c;
                }
            }
        }
        for (int i=size.length; --i>=0;) {
            final double c = size[i];
            if (Double.isInfinite(c)) {
                return null;
            }
        }
        return size;
    }

    /**
     * Returns {@code true} if the given coordinate is visible on this {@code Canvas}. The default
     * implementation checks if the coordinate is inside the canvas envelope. Subclasses should
     * override this method if a more accurate check is possible.
     */
    public synchronized boolean isVisible(final DirectPosition coordinate) {
        try {
            return graphicsEnvelope.contains(toObjectivePosition(coordinate));
        } catch (TransformException e) {
            /*
             * A typical reason for transformation failure is a coordinate point outside the area
             * of validity. If the specified point is outside the area of validity of the CRS used
             * by this canvas, then we can reasonably assume that it is outside the canvas envelope
             * as well.
             */
            return false;
        }
    }

    /**
     * Implementation of some convenience method for mouse events.
     * This is used by the following methods:
     * <p>
     * <ul>
     *   <li>{@link ReferencedCanvas#getToolTipText}</li>
     *   <li>{@link ReferencedCanvas#getAction}</li>
     *   <li>{@link ReferencedCanvas#format}</li>
     * </ul>
     */
    final synchronized Object/*<T>*/ visit(final GraphicVisitor/*<T>*/ visitor,
                                           final ReferencedEvent event)
    {
//        final Collection/*<Graphic>*/ graphics = renderer.getGraphics();
//        for (int i=graphics.size(); --i>=0;) {
//            final AbstractGraphic candidate = (AbstractGraphic) graphics.get(i);
//            if (candidate.getVisible()) {
//                final Object value = visitor.visit(candidate, event);
//                if (value != null) {
//                    return value;
//                }
//            }
//        }
        return null;
    }

    /**
     * Formats a value for a given event. The meaning of "value" depends on the underlying
     * graphics. For example in the particular case of a Digital Elevation Model (DEM), it
     * could be the altitude at the event location. This value is typically to be appended
     * to some other informations, like the event geographic coordinates.
     * <p>
     * This method queries registered {@linkplain ReferencedGraphic graphics} in decreasing
     * {@linkplain ReferencedGraphic#getZOrderHint z-order} until one is found to formats a
     * value.
     *
     * @param  event The event.
     * @param  toAppendTo The destination buffer for formatting a value. The {@link StringBuffer}
     *         argument type allows efficient use of {@link java.text.Format} for example.
     * @return {@code true} if this method has formatted a value, or {@code false} otherwise.
     */
    public boolean format(final ReferencedEvent event, final StringBuffer toAppendTo) {
        return ((Boolean) visit(new GraphicVisitor.Format(toAppendTo), event)).booleanValue();
    }

    /**
     * Returns the string to be used as the tooltip for a given event.
     * This method queries registered {@linkplain ReferencedGraphic graphics} in decreasing
     * {@linkplain ReferencedGraphic#getZOrderHint z-order} until one is found to returns a
     * non-null string.
     *
     * @param  event The event.
     * @return The tool tip text, or {@code null} if there is no tool tip for the given location.
     */
    public String getToolTipText(final ReferencedEvent event) {
        return (String) visit(GraphicVisitor.ToolTipText.SHARED, event);
    }

    /**
     * Returns the action to be used for a given event.
     * This method queries registered {@linkplain ReferencedGraphic graphics} in decreasing
     * {@linkplain ReferencedGraphic#getZOrderHint z-order} until one is found to returns a
     * non-null action.
     *
     * @param  event The event.
     * @return The action, or {@code null} if there is no action for the given location.
     */
    public Action getAction(final ReferencedEvent event) {
        return (Action) visit(GraphicVisitor.Action.SHARED, event);
    }

    /**
     * Invoked when an unexpected exception occured. This method is a shortcut for
     * {@link AbstractCanvas#handleException} with {@code sourceClassName} set to
     * {@code "org.geotools.display.canvas.ReferencedCanvas"}.
     *
     * @param  sourceMethodName The caller's method name, for logging purpose.
     * @param  exception        The exception.
     */
    private void handleException(final String sourceMethodName, final Exception exception) {
        handleException(ReferencedCanvas.class, sourceMethodName, exception);
    }

    /**
     * Invoked when a property change listener has been {@linkplain #addPropertyChangeListener
     * added} or {@linkplain #removePropertyChangeListener removed}.
     */
    @Override
    protected void listenersChanged() {
        super.listenersChanged();
        hasScaleListeners    = propertyListeners.hasListeners(SCALE_PROPERTY);
        hasDisplayListeners  = propertyListeners.hasListeners(DISPLAY_CRS_PROPERTY);
        hasEnvelopeListeners = propertyListeners.hasListeners(ENVELOPE_PROPERTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCache() {
        super.clearCache();
        transforms.clear();
        opFactory         = null;
        crsFactories      = null;
        affineMethod      = null;
        objectivePosition = null;
        displayPosition   = null;
    }



    //---------------------- Convinient methods --------------------------------
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


    //---------------------- Renderer events -----------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    protected void graphicsAdded(RendererEvent event) {
        super.graphicsAdded(event);

        Envelope oldEnvelope = null;

        Collection<Graphic> graphics = event.getGraphics();

        for(Graphic graphic : graphics){

            if (graphic instanceof ReferencedGraphic) {
                final ReferencedGraphic   referenced   = (ReferencedGraphic) graphic;
                CoordinateReferenceSystem graphicCRS   = referenced.getObjectiveCRS();
                CoordinateReferenceSystem objectiveCRS = getObjectiveCRS();

                //canvas will have a CRS selected by the user or we should set this value  when importing datas
                //not automaticly switching to the graphic CRS
                if (useDefaultCRS) {
                    try {
                        setObjectiveCRS(graphicCRS);
                        objectiveCRS = graphicCRS;
                        useDefaultCRS = false;
                    } catch (TransformException unexpected) {
                        /*
                         * Should not happen, since this canvas do not yet have any graphic.
                         * Log the warning and continue with the Canvas CRS unchanged.
                         */
                        handleException("add", unexpected);
                    }
                }

                // prepare notify envelope change : optimisation
                if (hasEnvelopeListeners) {
                    oldEnvelope = new GeneralEnvelope(getGraphicsEnvelope());
                }
                graphicCRS = referenced.getObjectiveCRS(); // May have changed.
                final Envelope graphicEnvelope = referenced.getEnvelope();
                graphicEnvelopeChanged(null, graphicEnvelope, graphicCRS,ReferencedCanvas.class, "add");
            }

        }

        // notify envelope change : optimisation
        if (oldEnvelope != null) {
            propertyListeners.firePropertyChange(ENVELOPE_PROPERTY, oldEnvelope, graphicsEnvelope);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void graphicsRemoved(RendererEvent event) {

        Envelope oldEnvelope = null;

        Collection<Graphic> graphics = event.getGraphics();

        for(Graphic graphic : graphics){

            if (graphic instanceof ReferencedGraphic) {
                final ReferencedGraphic referenced = (ReferencedGraphic) graphic;
                if (referenced.getCanvas() == this) {

                     // prepare notify envelope change : optimisation
                    if (hasEnvelopeListeners) {
                        oldEnvelope = new GeneralEnvelope(graphicsEnvelope);
                    }
                    final CoordinateReferenceSystem graphicCRS = referenced.getObjectiveCRS();
                    final Envelope graphicEnvelope = referenced.getEnvelope();
                    graphicEnvelopeChanged(graphicEnvelope, null, graphicCRS,
                                           ReferencedCanvas.class, "remove");
                }
            }
        }

        super.graphicsRemoved(event);

        // notify envelope change : optimisation
        if (oldEnvelope != null) {
            propertyListeners.firePropertyChange(ENVELOPE_PROPERTY, oldEnvelope, graphicsEnvelope);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void graphicsChanged(RendererEvent event) {
        super.graphicsChanged(event);
    }



    //----------------------CRS & MathTransform methods-------------------------
    /**
     * Returns a default CRS for the specified number of dimensions.
     *
     * @param  dimension The number of dimension for the viewer.
     * @return A default coordinate reference system with the specified number of dimensions.
     * @throws IllegalArgumentException if the specified number of dimensions is not supported.
     */
    private static CoordinateReferenceSystem getDefaultCRS(final int dimension)
            throws IllegalArgumentException {
        switch (dimension) {
            case 2:  return DefaultEngineeringCRS.GENERIC_2D;
            case 3:  return DefaultEngineeringCRS.GENERIC_3D;
            default: throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                                        "dimension", new Integer(dimension)));
        }
    }

    /**
     * {@inheritDoc}
     */
    public final CoordinateReferenceSystem getObjectiveCRS() {
        return graphicsEnvelope.getCoordinateReferenceSystem();
    }

    /**
     * Sets the objective Coordinate Reference System for this {@code Canvas}.
     * All graphic primitives are notified of the CRS change by a call to
     * <code>{@linkplain ReferencedGraphic#setObjectiveCRS setObjectiveCRS}(crs)</code>.
     * <p>
     * After the objective CRS change, this method invokes {@link #setDisplayCRS setDisplayCRS}
     * with a new, automatically computed, display CRS. The new display CRS try to preserve the
     * same {@linkplain #getScale scale factor} than the previous one.
     * <p>
     * This method fires the following property change events in no particular order:
     * {@value org.geotools.display.canvas.DisplayObject#OBJECTIVE_CRS_PROPERTY},
     * {@value org.geotools.display.canvas.DisplayObject#DISPLAY_CRS_PROPERTY},
     * {@value org.geotools.display.canvas.DisplayObject#ENVELOPE_PROPERTY}.
     */
    public synchronized void setObjectiveCRS(final CoordinateReferenceSystem crs)
            throws TransformException
    {
        final CoordinateReferenceSystem oldCRS = getObjectiveCRS();
        assert oldCRS == getObjectiveCRS();
        if (CRS.equalsIgnoreMetadata(crs, oldCRS)) {
            return;
        }
        final GeneralEnvelope oldEnvelope = new GeneralEnvelope(graphicsEnvelope);
        /*
         * Try to setup the CRS in the envelope.  This operation may fail if the specified
         * CRS has an incompatible number of dimensions, so it is important to invoke this
         * method before to make any more change. This is in order to keep the renderer in
         * a consistent state in case of failure.
         */
        graphicsEnvelope.setCoordinateReferenceSystem(crs);
        /*
         * Compute the display CRS from the new objective CRS (keeping the same zoom factor
         * than the previous display CRS), but do not store the result yet.  We compute the
         * display CRS now in order to avoid any change in case of failure, and will invoke
         * setDisplayCRS(...) only after all transformation steps below will succeed.
         */
        DerivedCRS displayCRS = this.displayCRS;
        if (displayCRS != null) try {
            final Conversion objectiveToDisplay = displayCRS.getConversionFromBase();
            displayCRS = /*getFactoryGroup().getCRSFactory().*/createDerivedCRS(
                    displayProperties, objectiveToDisplay, crs, displayCRS.getCoordinateSystem());
        } catch (FactoryException exception) {
            throw new TransformException(Errors.format(
                    ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), exception);
        }
        /*
         * Now transform each individual graphic primitives. The transformation must succeed
         * for all of them. If there is any failure, then we will roolback the change to the
         * previous CRS.
         */
        //---------------------------------------------------Graphics should have a listener on CRS property,no the canvas that call each graphic
//        final List/*<Graphic>*/ graphics = renderer.getGraphics();
//        final int graphicCount = graphics.size();
//        int changed = 0;
//        try {
//            disableGraphicListener = true;
//            while (changed < graphicCount) {
//                final Graphic graphic = (Graphic) graphics.get(changed);
//                if (graphic instanceof ReferencedGraphic) {
//                    ((ReferencedGraphic) graphic).setObjectiveCRS(crs);
//                }
//                changed++; // Increment only if previous call succeed.
//            }
//        } catch (TransformException exception) {
//            /*
//             * At least one graphic primitives can't accept the new CRS. Roll back all
//             * CRS changes (restore the previous one) before to rethrow the exception.
//             */
//            while (--changed >= 0) {
//                final Graphic graphic = (Graphic) graphics.get(changed);
//                if (graphic instanceof ReferencedGraphic) try {
//                    ((ReferencedGraphic) graphic).setObjectiveCRS(oldCRS);
//                } catch (TransformException unexpected) {
//                    // Should not happen, since we are rolling
//                    // back to an old CRS that previously worked.
//                    handleException("setObjectiveCRS", unexpected);
//                }
//            }
//            envelope.setCoordinateReferenceSystem(oldCRS);
//            clearCache();
//            throw exception;
//        } finally {
//            disableGraphicListener = false;
//        }
        /*
         * The CRS change has been successful for all graphic primitives.
         * Now updates internal states.
         */
        clearCache();
        useDefaultCRS = false;
        updateNormalizationFactor(crs);
        computeEnvelope(ReferencedCanvas.class, "setObjectiveCRS");
        /*
         * Set the display CRS last because it may fires a property change event,
         * and we don't want to expose our changes before they are completed.
         */
        if (displayCRS != null) {
            setDisplayCRS(displayCRS);
        }
        assert getObjectiveCRS() == crs;
        propertyListeners.firePropertyChange(OBJECTIVE_CRS_PROPERTY, oldCRS, crs);
        propertyListeners.firePropertyChange(ENVELOPE_PROPERTY, oldEnvelope, graphicsEnvelope);
    }

    /**
     * Updates the normalization factor after a CRS change. Normalization factor doesn't
     * apply to this basic {@code ReferencedCanvas} class. This method is merely a hook
     * for {@link ReferencedCanvas2D} implementation.
     *
     * @see ReferencedCanvas2D#updateNormalizationFactor
     */
    void updateNormalizationFactor(final CoordinateReferenceSystem crs) {
    }

    /**
     * Recomputes inconditionnaly the {@linkplain #graphicsEnvelope}. The envelope will be computed
     * from the value provided by {@link ReferencedGraphic#envelope} for all graphics.
     * <p>
     * <strong>NOTE:</strong> Callers are responsible for firing an event after the envelope change.
     * This method doesn't fire an {@value org.geotools.display.canvas.DisplayObject#ENVELOPE_PROPERTY}
     * change event itself because this step is often only an intermediate step (see for example
     * {@link #setObjectiveCRS}).
     *
     * @param  sourceClassName  The caller's class name,  for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     */
    private void computeEnvelope(final Class<?> sourceClassName, final String sourceMethodName) {
        assert Thread.holdsLock(this);
        graphicsEnvelope.setToNull();
        CoordinateReferenceSystem lastCRS = null;
        MathTransform           transform = null;
        final Collection<Graphic>  graphics = getRenderer().getGraphics();

        for (final Graphic candidate : graphics) {
            if (!(candidate instanceof ReferencedGraphic)) {
                continue;
            }
            final ReferencedGraphic graphic = (ReferencedGraphic) candidate;
            final Envelope candidateEnvelope = graphic.getEnvelope();
            /*
             * In theory, the Graphic should use the same CRS than this Canvas.
             * However, as a safety, we will check for coordinate transformations anyway.
             */
            final CoordinateReferenceSystem crs = graphic.getObjectiveCRS();
            try {
                if (!CRS.equalsIgnoreMetadata(crs, lastCRS)) {
                    transform = getMathTransform(crs, getObjectiveCRS(),
                                                 sourceClassName, sourceMethodName);
                    lastCRS = crs;
                }
                final GeneralEnvelope bounds = CRS.transform(transform, candidateEnvelope);
                if (graphicsEnvelope.isNull()) {
                    graphicsEnvelope.setEnvelope(bounds);
                } else {
                    graphicsEnvelope.add(bounds);
                }
            } catch (FactoryException exception) {
                handleException(sourceClassName, sourceMethodName, exception);
            } catch (TransformException exception) {
                handleException(sourceClassName, sourceMethodName, exception);
                // Continue. The envelope for this graphic will be ignored.
            }
        }
    }

    /**
     * Invoked when the preferred area for a graphic primitive changed. This method try to update
     * the {@link #envelope} without iterating over all graphics again.
     * <p>
     * <strong>NOTE:</strong> Callers are responsible to fire an event after the envelope change.
     * This method doesn't fire an {@value org.geotools.display.canvas.DisplayObject#ENVELOPE_PROPERTY}
     * change event itself because this step is usually not the final step (see for example {@link #add}.
     *
     * @param  oldEnvelope      The old envelope of the graphic that changed.
     * @param  newEnvelope      The new envelope of the graphic that changed.
     * @param  crs              The coordinate system for {@code [old|new]Envelope}.
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     */
    public void graphicEnvelopeChanged(final Envelope oldEnvelope,
                                       final Envelope newEnvelope,
                                       final CoordinateReferenceSystem crs,
                                       final Class<?> sourceClassName,
                                        final String sourceMethodName)
    {
        assert Thread.holdsLock(this);
        final GeneralEnvelope oldProjected, newProjected;
        try {
            final MathTransform transform =
                    getMathTransform(crs, getObjectiveCRS(), sourceClassName, sourceMethodName);
            oldProjected = CRS.transform(transform, oldEnvelope);
            newProjected = CRS.transform(transform, newEnvelope);
        } catch (FactoryException exception) {
            handleException(sourceClassName, sourceMethodName, exception);
            computeEnvelope(sourceClassName, sourceMethodName);
            return;
        } catch (TransformException exception) {
            handleException(sourceClassName, sourceMethodName, exception);
            computeEnvelope(sourceClassName, sourceMethodName);
            return;
        }
        if (!replace(graphicsEnvelope, oldProjected, newProjected)) {
            computeEnvelope(sourceClassName, sourceMethodName);
        }
    }

    /**
     * Expands (if needed) an envelope according the specified change. This method is invoked when
     * a graphic primitives changed its envelope from {@code oldEnvelope} to {@code newEnvelope}.
     * If this change means expanding {@code envelope}, then the envelope is expanded. If the
     * change <strong>may</strong> reduce the {@code envelope}, then this method do nothing and
     * returns {@code false}.
     *
     * @param  envelope    The envelope to modify.
     * @param  oldEnvelope The old graphic primitive envelope, or {@code null} if none.
     * @param  newEnvelope The new graphic primitive envelope, or {@code null} if none.
     * @return {@code true} if the this method has been able to update the envelope, or
     *         {@code false} if {@link #computeEnvelope} needs to be invoked.
     */
    private static boolean replace(final GeneralEnvelope envelope,
                                   final GeneralEnvelope oldEnvelope,
                                   final GeneralEnvelope newEnvelope)
    {
        if (envelope.isEmpty()) {
            /*
             * If no envelope were defined prior this method call, then the new
             * envelope is the whole canvas envelope (until more graphics are added).
             */
            if (newEnvelope != null) {
                envelope.setEnvelope(newEnvelope);
            }
            return true;
        }
        if (newEnvelope == null || newEnvelope.isNull()) {
            /*
             * An envelope may have been removed and no new envelope replace it. If the old
             * envelope was fully included inside the canvas envlope (NOT touching edges),
             * then the removal of the old envelope can't reduce the canvas envelope.
             */
            return (oldEnvelope == null) || envelope.contains(oldEnvelope, false);
        }
        if (oldEnvelope != null && !oldEnvelope.isNull()) {
            /*
             * An envelope has been removed (or replaced by the new envelope). Checks if the
             * removal of the old envelope may reduces the canvas envelope, in which case we
             * need to recompute everything with the 'computeEnvelope' method.
             */
            for (int i=envelope.getDimension(); --i>=0;) {
                double t = oldEnvelope.getMinimum(i);
                if (!(t > envelope.getMinimum(i) || t >= newEnvelope.getMinimum(i))) {
                    return false;
                }
                t = oldEnvelope.getMaximum(i);
                if (!(t < envelope.getMaximum(i) || t <= newEnvelope.getMaximum(i))) {
                    return false;
                }
            }
        }
        /*
         * Expands the canvas envelope with the new one.
         */
        envelope.add(newEnvelope);
        return true;
    }

    /**
     * Returns the Coordinate Reference System associated with the display of this {@code Canvas}.
     * Unless otherwise specified by a call to {@link #setDisplayCRS setDisplayCRS}, the default
     * display CRS assumes a rendering on a flat screen with axis oriented as in the
     * <cite>Java2D</cite> default {@linkplain java.awt.Graphics2D user space}: Coordinates are
     * in "dots" (about 1/72 of inch), <var>x</var> values increasing right and <var>y</var>
     * values increasing <strong>down</strong>.
     */
    public final synchronized DerivedCRS getDisplayCRS() {
        if (displayCRS == null) try {
            final ReferencingFactoryContainer crsFactories;
            final CoordinateReferenceSystem objectiveCRS;
            final CoordinateSystem displayCS;
            final int sourceDim, targetDim;
            final Matrix identity;
            final MathTransform mt;

            crsFactories      = getFactoryGroup();
            objectiveCRS      = getObjectiveCRS();
            displayCS         = DefaultCartesianCS.DISPLAY;
            sourceDim         = objectiveCRS.getCoordinateSystem().getDimension();
            targetDim         = displayCS.getDimension();
            identity          = MatrixFactory.create(targetDim+1, sourceDim+1);
            mt                = crsFactories.getMathTransformFactory().createAffineTransform(identity);
            displayProperties = AbstractIdentifiedObject.getProperties(displayCS, null);
            displayCRS        = crsFactories.getCRSFactory().createDerivedCRS(
                                    displayProperties, affineMethod, objectiveCRS, mt, displayCS);
        } catch (FactoryException exception) {
            /*
             * Should never happen, because the CRS that we tried to create is somewhat basic
             * (an identity transform). Rethrows as an illegal state, since this exception is
             * probably caused by some misconfiguration.
             */
            throw new IllegalStateException(Errors.format(ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM));
            // TODO: include the cause when we will be allowed to compile for J2SE 1.5.
        }
        assert displayProperties!=null && !displayProperties.isEmpty() : displayProperties;
        assert displayCRS.getBaseCRS() == getObjectiveCRS() : displayCRS;
        return displayCRS;
    }

    /**
     * Sets the display Coordinate Reference System for this {@code Canvas}. The {@linkplain
     * DerivedCRS#getBaseCRS base CRS} must be the {@linkplain #getObjectiveCRS objective CRS},
     * and the {@linkplain DerivedCRS#getConversionFromBase conversion from base} is related to
     * the map scale or zoom. This method is usually invoked by subclasses rather than users, as
     * a consequence of zoom changes. For example this method may be invoked as a side-effect of
     * the following methods:
     * <p>
     * <ul>
     *   <li>{@link ReferencedCanvas#setObjectiveCRS}</li>
     *   <li>{@link ReferencedCanvas#setObjectiveToDisplayTransform(Matrix)}</li>
     * </ul>
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#DISPLAY_CRS_PROPERTY}
     * property change event.
     *
     * @param  crs The display coordinate reference system.
     * @throws TransformException If the data can't be transformed.
     */
    protected synchronized void setDisplayCRS(final DerivedCRS crs) throws TransformException {
        final CoordinateReferenceSystem oldCRS = displayCRS;
        try {
            displayCRS = validateBaseCRS(crs, getObjectiveCRS());
        } catch (FactoryException exception) {
            throw new TransformException(Errors.format(
                        ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), exception);
        }
        if (displayProperties == null) {
            displayProperties = AbstractIdentifiedObject.getProperties(crs, null);
        }
        displayPosition = null;
        if (hasDisplayListeners) {
            propertyListeners.firePropertyChange(DISPLAY_CRS_PROPERTY, oldCRS, crs);
        }
        /*
         * In theory, the 'deviceCRS' has changed too  since it need to be rebuilt with the new
         * 'displayCRS' as the base CRS. However, because must users will not care about device
         * CRS,  we do not recompute it here  (remember that this 'setDisplayCRS' method may be
         * invoked often). A new 'deviceCRS' will be automatically recomputed by 'getDeviceCRS()'
         * if needed.
         */
    }

    /**
     * Returns the Coordinate Reference System associated with the device of this {@code Canvas}.
     */
    @Override
    public final synchronized DerivedCRS getDeviceCRS() {
        final DerivedCRS displayCRS = getDisplayCRS();
        if (deviceCRS == null) try {
            final ReferencingFactoryContainer crsFactories;
            final CoordinateSystem deviceCS;
            final Matrix identity;
            final MathTransform mt;

            crsFactories     = getFactoryGroup();
            deviceCS         = displayCRS.getCoordinateSystem();
            identity         = MatrixFactory.create(deviceCS.getDimension()+1);
            mt               = crsFactories.getMathTransformFactory().createAffineTransform(identity);
            deviceProperties = AbstractIdentifiedObject.getProperties(deviceCS, null);
            deviceCRS        = crsFactories.getCRSFactory().createDerivedCRS(
                                   deviceProperties, affineMethod, displayCRS, mt, deviceCS);
        } catch (FactoryException exception) {
            /*
             * Should never happen, because the CRS that we tried to create is somewhat basic
             * (an identity transform). Rethrows as an illegal state, since this exception is
             * probably caused by some misconfiguration.
             */
            throw new IllegalStateException(Errors.format(ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM));
            // TODO: include the cause when we will be allowed to compile for J2SE 1.5.
        }
        /*
         * If the 'displayCRS' has changed since the last time that this method has been invoked,
         * then recomputes a new 'deviceCRS' using the new 'displayCRS' as its base and the same
         * conversion than the old CRS.
         */
        if (deviceCRS.getBaseCRS() != displayCRS) try {
            final Conversion displayToDevice = deviceCRS.getConversionFromBase();
            deviceCRS = /*getFactoryGroup().getCRSFactory().*/createDerivedCRS(
                    deviceProperties, displayToDevice, displayCRS, deviceCRS.getCoordinateSystem());
        } catch (FactoryException exception) {
            throw new IllegalStateException(Errors.format(
                    ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM));
            // TODO: include the cause when we will be allowed to compile for J2SE 1.5.
        }
        assert deviceProperties!=null && !deviceProperties.isEmpty() : deviceProperties;
        assert deviceCRS.getBaseCRS() == getDisplayCRS() : deviceCRS;
        assert deviceCRS.getCoordinateSystem() == displayCRS.getCoordinateSystem() : deviceCRS;
        return deviceCRS;
    }

    /**
     * Sets the device Coordinate Reference System for this {@code Canvas}.
     * This method is usually invoked by subclasses rather than users. At the difference of
     * {@link #setDisplayCRS setDisplayCRS(...)} (which is invoked everytime the zoom change),
     * this method is usually invoked only once since the
     * {@linkplain DerivedCRS#getConversionFromBase conversion} from display CRS to derived CRS
     * is usually constant.
     *
     * @param  crs The device coordinate reference system.
     * @throws TransformException If the data can't be transformed.
     */
    protected synchronized void setDeviceCRS(final DerivedCRS crs) throws TransformException {
        try {
            deviceCRS = validateBaseCRS(crs, getDisplayCRS());
        } catch (FactoryException exception) {
            throw new TransformException(Errors.format(
                        ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), exception);
        }
        if (deviceProperties == null) {
            deviceProperties = AbstractIdentifiedObject.getProperties(crs, null);
        }
    }

    /**
     * Ensures that the {@linkplain DerivedCRS#getBaseCRS base CRS} for the specified derived CRS
     * is the expected one. If this is not the case, attempt to create a new CRS derived from the
     * expected base CRS.
     *
     * @param  crs The derived CRS to check.
     * @param  baseCRS The expected base CRS.
     * @return {@code crs}, or a new one if the CRS was not derived from the expected base CRS.
     * @throws FactoryException if the CRS can't be created.
     */
    private final DerivedCRS validateBaseCRS(DerivedCRS crs, final CoordinateReferenceSystem baseCRS)
            throws FactoryException
    {
        if (crs.getBaseCRS() != baseCRS) {
            final CoordinateOperationFactory factory = getCoordinateOperationFactory();
            final CoordinateOperation operation = factory.createOperation(baseCRS, crs);
            final Conversion conversion;
            /*
             * Gets the conversion using a 'try...catch' block instead of testing with
             * 'if (operation instanceof Conversion)' in order to include the cause in
             * the stack trace, so the user can get more tips why the CRS is invalid.
             */
            try {
                conversion = (Conversion) operation;
            } catch (ClassCastException exception) {
                throw new FactoryException(Errors.format(
                            ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), exception);
            }
            crs = /*getFactoryGroup().getCRSFactory().*/createDerivedCRS(
                    AbstractIdentifiedObject.getProperties(crs, null),
                    conversion, baseCRS, crs.getCoordinateSystem());
        }
        return crs;
    }

    /**
     * Temporary placeholder for future GeoAPI method.
     *
     * @todo Delete this method when the equivalent GeoAPI method will be available (GeoAPI 2.1?).
     *       Note: make sure that our implementation will use the math transform when available
     *       instead of creating a new one from the parameter values, while ignoring the baseCRS.
     *       This would be different from {@code createProjectedCRS}, which uses a more heuristic
     *       algorithm.
     */
    private static DerivedCRS createDerivedCRS(final Map                 properties,
                                               final Conversion  conversionFromBase,
                                               final CoordinateReferenceSystem base,
                                               final CoordinateSystem     derivedCS)
            throws FactoryException
    {
        return new org.geotools.referencing.crs.DefaultDerivedCRS(properties,
                conversionFromBase, base, conversionFromBase.getMathTransform(), derivedCS);
    }

    /**
     * Creates a derived CRS for a {@linkplain #getDisplayCRS display CRS} or
     * a {@linkplain #getDeviceCRS device CRS}. The coordinate system will be
     * inherited from the current display or device CRS. The operation method
     * will always be the affine transform.
     *
     * @param  device    {@code true} for creating a device CRS, or {@code false} for a display one.
     * @param  transform The affine transform from base to the derived CRS as matrix.
     * @return The derived CRS.
     * @throws FactoryException if the CRS can't be created.
     *
     * @see #setObjectiveToDisplayTransform(Matrix)
     */
    private DerivedCRS createDerivedCRS(final boolean device, final Matrix transform)
            throws FactoryException
    {
        assert Thread.holdsLock(this);
        DerivedCRS crs;
        Map properties;
        if (device) {
            crs = getDeviceCRS();
            properties = deviceProperties;
        } else {
            crs = getDisplayCRS();
            properties = displayProperties;
        }
        final ReferencingFactoryContainer crsFactories = getFactoryGroup();
        final MathTransform mt = crsFactories.getMathTransformFactory().createAffineTransform(transform);
        crs = crsFactories.getCRSFactory().createDerivedCRS(properties, affineMethod,
                crs.getBaseCRS(), mt, crs.getCoordinateSystem());
        return crs;
    }

    /**
     * Sets the {@linkplain #getDisplayCRS display} to {@linkplain #getDeviceCRS device} transform
     * to the specified affine transform. This method creates a new device CRS and invokes
     * {@link #setDeviceCRS} with the result.
     *
     * @param  transform The {@linkplain #getDisplayCRS display} to
     *         {@linkplain #getDeviceCRS device} affine transform as a matrix.
     * @throws TransformException if the transform can not be set to the specified value.
     */
    protected synchronized void setDisplayToDeviceTransform(final Matrix transform)
            throws TransformException
    {
        final DerivedCRS crs;
        try {
            crs = createDerivedCRS(true, transform);
        } catch (FactoryException exception) {
            // Should not occurs for an affine transform, since it is quite a basic one.
            throw new TransformException(exception.getLocalizedMessage(), exception);
        }
        setDeviceCRS(crs);
    }

    /**
     * Sets the {@linkplain #getObjectiveCRS objective} to {@linkplain #getDisplayCRS display}
     * transform to the specified affine transform. This method creates a new display CRS and
     * invokes {@link #setDisplayCRS} with the result.
     *
     * @param  transform The {@linkplain #getObjectiveCRS objective} to
     *         {@linkplain #getDisplayCRS display} affine transform as a matrix.
     * @throws TransformException if the transform can not be set to the specified value.
     */
    public synchronized void setObjectiveToDisplayTransform(final Matrix transform)
            throws TransformException
    {
        final DerivedCRS crs;
        try {
            crs = createDerivedCRS(false, transform);
        } catch (FactoryException exception) {
            // Should not occurs for an affine transform, since it is quite a basic one.
            throw new TransformException(exception.getLocalizedMessage(), exception);
        }
        setDisplayCRS(crs);
    }

    /**
     * Sets the {@linkplain #getObjectiveCRS objective} to {@linkplain #getDisplayCRS display}
     * transform to the specified transform. The default implementation expects an affine
     * transform and delegates the work to {@link #setObjectiveToDisplayTransform(Matrix)}.
     *
     * @param  transform The {@linkplain #getObjectiveCRS objective} to
     *         {@linkplain #getDisplayCRS display} affine transform.
     * @throws TransformException if the transform can not be set to the specified value.
     */
    public void setObjectiveToDisplayTransform(final MathTransform transform)
            throws TransformException
    {
        if (transform instanceof LinearTransform) {
            setObjectiveToDisplayTransform(((LinearTransform) transform).getMatrix());
        } else {
            throw new TransformException(Errors.format(ErrorKeys.NOT_AN_AFFINE_TRANSFORM));
        }
    }

    /**
     * Returns the coordinate transformation object for this {@code Canvas}. This allows the
     * {@code Canvas} to resolve conversions of coordinates between the objective and display
     * Coordinate Reference Systems.
     *
     * @return MathTransform
     */
    public synchronized MathTransform getObjectiveToDisplayTransform() {
        final DerivedCRS displayCRS = getDisplayCRS();
        assert displayCRS.getBaseCRS().equals(getObjectiveCRS()) : displayCRS;
        return displayCRS.getConversionFromBase().getMathTransform();
    }

    /**
     * Returns the coordinate transformation object for this {@code Canvas}. This allows the
     * {@code Canvas} to resolve conversions of coordinates between the display and objective
     * Coordinate Reference Systems.
     * <p>
     * The default implementation returns the {@linkplain MathTransform#inverse inverse} of
     * the {@linkplain #getObjectiveToDisplayTransform objective to display transform}.
     *
     * @return MathTransform
     */
    public MathTransform getDisplayToObjectiveTransform() {
        try {
            return getObjectiveToDisplayTransform().inverse();
        } catch (NoninvertibleTransformException exception) {
            throw new IllegalStateException(Errors.format(ErrorKeys.NONINVERTIBLE_TRANSFORM));
            // TODO: Add the cause when we will be allowed to compile for J2SE 1.5.
        }
    }

    /**
     * Constructs a transform between two coordinate reference systems. If a
     * {@link Hints#COORDINATE_OPERATION_FACTORY} has been provided, then the specified
     * {@linkplain CoordinateOperationFactory coordinate operation factory} will be used.
     *
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The target coordinate reference system.
     * @param  sourceClassName  The caller class name, for logging purpose only.
     * @param  sourceMethodName The caller method name, for logging purpose only.
     * @return A transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException if the transform can't be created.
     *
     * @see #getImplHint
     * @see #setImplHint
     * @see Hints#COORDINATE_OPERATION_FACTORY
     */
    public final synchronized MathTransform getMathTransform(final CoordinateReferenceSystem sourceCRS,
                                                      final CoordinateReferenceSystem targetCRS,
                                                      final Class<?> sourceClassName,
                                                      final String sourceMethodName)
            throws FactoryException
    {
        /*
         * Fast check for a very common case. We will use the more general (but slower)
         * 'equalsIgnoreMetadata(...)' version implicitly in the call to factory method.
         */
        if (sourceCRS == targetCRS) {
            return IdentityTransform.create(sourceCRS.getCoordinateSystem().getDimension());
        }
        MathTransform tr;
        /*
         * Checks if the math transform is available in the cache. A majority of transformations
         * will be from 'graphicCRS' to 'objectiveCRS' to 'displayCRS'.  The cache looks for the
         * 'graphicCRS' to 'objectiveCRS' transform.
         */
        final CoordinateReferenceSystem objectiveCRS = getObjectiveCRS();
        final boolean cachedTransform = CRS.equalsIgnoreMetadata(targetCRS, objectiveCRS);
        if (cachedTransform) {
            tr = transforms.get(sourceCRS);
            if (tr != null) {
                return tr;
            }
        }
        /*
         * If one of the CRS is a derived CRS, then check if we can use directly its conversion
         * from base without using the costly coordinate operation factory. This check is worth
         * to be done since it is a very common situation. A majority of transformations will be
         * from 'objectiveCRS' to 'displayCRS', which is the case we test first. The converse
         * (transformations from 'displayCRS' to 'objectiveCRS') is less frequent and can be
         * handled by the 'transform' cache, which is why we let the factory check for it.
         */
        if (targetCRS instanceof GeneralDerivedCRS) {
            final GeneralDerivedCRS derivedCRS = (GeneralDerivedCRS) targetCRS;
            if (CRS.equalsIgnoreMetadata(sourceCRS, derivedCRS.getBaseCRS())) {
                return derivedCRS.getConversionFromBase().getMathTransform();
            }
        }
        /*
         * Now that we failed to reuse a pre-existing transform, ask to the factory
         * to create a new one. A message is logged in order to trace down the amount
         * of coordinate operations created.
         */
        final Logger logger = getLogger();
        if (logger.isLoggable(Level.FINER)) {
            // FINER is the default level for entering, returning, or throwing an exception.
            final LogRecord record = Loggings.getResources(getLocale()).getLogRecord(Level.FINER,
                    LoggingKeys.INITIALIZING_TRANSFORMATION_$2,
                    toString(sourceCRS), toString(targetCRS));
            record.setSourceClassName (sourceClassName.getName());
            record.setSourceMethodName(sourceMethodName);
            logger.log(record);
        }
        CoordinateOperationFactory factory = getCoordinateOperationFactory();
        tr = factory.createOperation(sourceCRS, targetCRS).getMathTransform();
        if (cachedTransform) {
            transforms.put(sourceCRS, tr);
        }
        return tr;
    }

    /**
     * Returns the coordinate operation factory. The actual instance is {@link #hints} dependent.
     */
    private CoordinateOperationFactory getCoordinateOperationFactory() throws FactoryException {
        assert Thread.holdsLock(this);
        if (opFactory == null) {
            final Hints tmp = new Hints(hints);
            tmp.putAll(getFactoryGroup().getImplementationHints());
            opFactory = ReferencingFactoryFinder.getCoordinateOperationFactory(tmp);
        }
        return opFactory;
    }

    /**
     * Returns the factories for CRS objects creation, coordinate operations and math transforms.
     *
     * @return The factory group (never {@code null}).
     * @throws FactoryException if the affine method can't be fetched.
     */
    private ReferencingFactoryContainer getFactoryGroup() throws FactoryException {
        assert Thread.holdsLock(this);
        if (crsFactories == null) {
            crsFactories = ReferencingFactoryContainer.instance(hints);
            affineMethod = crsFactories.getOperationMethod("affine");
        }
        return crsFactories;
    }

    /**
     * Returns a string representation of a coordinate reference system. This method is
     * used for formatting a logging message in {@link #getMathTransform}.
     */
    private static String toString(final CoordinateReferenceSystem crs) {
        return Classes.getShortClassName(crs) + "[\"" + crs.getName().getCode() + "\"]";
    }

    /**
     * Transforms a coordinate from an arbitrary CRS to the {@linkplain #getObjectiveCRS objective
     * CRS}. For performance reason, this method recycles always the same destination point. Do not
     * keep a reference to the returned point for a long time, since its value may be changed at
     * any time.
     *
     * @param  coordinate The direct position to transform.
     * @return The transformed direct position.
     * @throws TransformException if the transformation failed.
     */
    protected final GeneralDirectPosition toObjectivePosition(final DirectPosition coordinate)
            throws TransformException
    {
        assert Thread.holdsLock(this);
        if (objectivePosition == null) {
            objectivePosition = new TransformedDirectPosition(null, getObjectiveCRS(), hints);
        }
        assert Utilities.equals(objectivePosition.getCoordinateReferenceSystem(), getObjectiveCRS());
        objectivePosition.transform(coordinate);
        return objectivePosition;
    }

    /**
     * Transforms a coordinate from an arbitrary CRS to the {@linkplain #getDisplayCRS display CRS}.
     * For performance reason, this method recycles always the same destination point. Do not keep a
     * reference to the returned point for a long time, since its value may be changed at any time.
     *
     * @param  coordinate The direct position to transform.
     * @return The transformed direct position.
     * @throws TransformException if the transformation failed.
     */
    protected final GeneralDirectPosition toDisplayPosition(final DirectPosition coordinate)
            throws TransformException
    {
        assert Thread.holdsLock(this);
        if (displayPosition == null) {
            displayPosition = new TransformedDirectPosition(null, getDisplayCRS(), hints);
        }
        assert Utilities.equals(displayPosition.getCoordinateReferenceSystem(), getDisplayCRS());
        displayPosition.transform(coordinate);
        return displayPosition;
    }

}
