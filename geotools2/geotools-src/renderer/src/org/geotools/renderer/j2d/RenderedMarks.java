/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Font;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.units.Unit;
import org.geotools.resources.XMath;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XAffineTransform;


/**
 * A set of marks and/or labels to be rendered. Marks can have different sizes and orientations
 * (for example a field of wind arrows). This abstract class is not a container for marks.
 * Subclasses must override the {@link #getMarkIterator} method in order to returns informations
 * about marks.
 *
 * @version $Id: RenderedMarks.java,v 1.9 2003/03/19 23:50:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class RenderedMarks extends RenderedLayer {
    /**
     * The number of entries in {@link #markTransforms} for each {@link AffineTransform}.
     */
    private static final int TRANSFORM_RECORD_LENGTH = 6;

    /**
     * The bit to set in {@link #validMask} if the following arrays are valids:
     * {@link #markShapes} and {@link #markTransforms}.
     *
     * @task TODO: Current implementation doesn't yet support "partial" revalidation (e.g.
     *             revalidating MARKS_MASK without GLYPHS_MASK). We invalidate/revalidate
     *             everything or nothing. Future implementations may add more fine grain
     *             validations.
     */
    private static final int MARKS_MASK = 1;

    /**
     * The bit to set in {@link #validMask} if the following arrays are valids:
     * {@link #glyphVectors} and {@link #glyphPositions}.
     *
     * @task TODO: Current implementation doesn't yet support "partial" revalidation (e.g.
     *             revalidating MARKS_MASK without GLYPHS_MASK). We invalidate/revalidate
     *             everything or nothing. Future implementations may add more fine grain
     *             validations.
     */
    private static final int GLYPHS_MASK = 2;

    /**
     * The 'or'-ed together valid bitmasks. Specify if arrays {@link #markShapes},
     * {@link #markTransforms}, {@link #glyphVectors} and {@link #glyphPositions}
     * are valids.
     *
     * @see #isValid
     * @see #MARKS_MASK
     * @see #GLYPHS_MASK
     */
    private transient int validMask;

    /**
     * The mark index for each mark in {@link #markShapes} and {@link #markTransforms}.
     */
    private transient int[] markIndex;

    /**
     * The shape returned by {@link MarkIterator#markShape} for each mark. In most cases, each
     * references in this array point to the same {@link Shape} instance, or to a small amount
     * of {@link Shape} instances. Consequently, the amount of memory used here should be low.
     */
    private transient Shape[] markShapes;

    /**
     * The {@link AffineTransform} to apply on each shapes in {@link #markShapes}. Each record
     * in this array is {@link #TRANSFORM_RECORD_LENGTH} entries long. Entries are given to
     * {@link AffineTransform#setTransform(double,double,double,double,double,double)}
     */
    private transient double[] markTransforms;

    /**
     * The gylphs for each labels to be rendered, or <code>null</code> if there is no glyphs
     * for this layer. Position for each glyph vectors will be stored in {@link #glyphPositions}.
     */
    private transient GlyphVector[] glyphVectors;

    /**
     * (<var>x</var>,<var>y</var>) positions for each glyphs, or <code>null</code> if there
     * is no glyphs for this layer.
     */
    private transient float[] glyphPositions;

    /**
     * The bounding box of all {@link #markShapes} transformed with the corresponding
     * {@link #markTransforms}, as well as {@link #glyphVectors} at their {@link #glyphPositions}.
     * Coordinates for this box must be pixels (or dots).
     */
    private transient Rectangle boundingBox;

    /**
     * Typical amplitude of marks, or 0 or {@link Double#NaN} if it need to be recomputed.
     * This value is computed by {@link #getTypicalAmplitude} and cached here for faster
     * access. The default implementation computes the Root Mean Square (RMS) value of all
     * {@linkplain MarkIterator#amplitude marks amplitude}.
     *
     * Note: this field is read and write by {@link RenderedGridMarks}, which overrides
     * {@link #getTypicalAmplitude}.
     */
    transient double typicalAmplitude;

    /**
     * An instance of {@link TransformedShape} to be reused every time this layer is rendered.
     * Will be created only when first needed.
     */
    private transient TransformedShape transformedShape;

    /**
     * Construct a new layer of marks.
     */
    public RenderedMarks() {
        super();
    }

    /**
     * Returns a guess of the number of marks. This method will be overrided with
     * a more efficient guess by {@link RenderedGridMarks}.
     */
    int getCount() {
        return (markIndex!=null) ? markIndex.length : 32;
    }

    /**
     * Returns an iterator for iterating through the marks. Whenever it is invoked on the same
     * <code>RenderedMarks</code> more than once, the mark iterator must consistently iterates
     * through the same marks in the same order, unless {@link #repaint()} has been invoked.
     * If some marks are added, removed or changed, then {@link #repaint()} must be invoked
     * first (usually by the methods implementing the addition, change or removal of marks).
     *
     * @return An iterator for iterating through the marks.
     *         This iterator doesn't need to be thread-safe.
     */
    public abstract MarkIterator getMarkIterator();

    /**
     * Returns the units for {@linkplain MarkIterator#amplitude marks amplitude}, or
     * <code>null</code> if unknow. All marks must use the same units. The default
     * implementation returns always <code>null</code>.
     */
    public Unit getAmplitudeUnit() {
        return null;
    }

    /**
     * Returns the typical amplitude of marks. The default implementation computes the <cite>Root
     * Mean Square</cite> (RMS) value of all {@linkplain MarkIterator#amplitude marks amplitude}.
     *
     * This information is used with mark's {@linkplain MarkIterator#amplitude amplitude} and
     * {@linkplain MarkIterator#markShape shape} in order to determine how big they should be
     * rendered. Marks with an {@linkplain MarkIterator#amplitude amplitude} equals to the
     * typical amplitude will be rendered with their {@linkplain MarkIterator#markShape shape}
     * unscaled. Other marks will be rendered with scaled versions of their shapes.
     */
    public double getTypicalAmplitude() {
        synchronized (getTreeLock()) {
            if (!(typicalAmplitude>0)) {
                int n=0;
                double rms=0;
                for (final MarkIterator it=getMarkIterator(); it.next();) {
                    final double v = it.amplitude();
                    if (!Double.isNaN(v)) {
                        rms += v*v;
                        n++;
                    }
                }
                typicalAmplitude = (n>0) ? Math.sqrt(rms/n) : 1;
            }
            return typicalAmplitude;
        }
    }

    /**
     * Returns the grid indices for the specified zoomable bounds.
     * Those indices will be used by {@link Iterator#visible(Rectangle)}.
     * This method is overriden by {@link RenderedGridMarks}.
     *
     * @param  zoomableBounds The zoomable bounds. Do not modify!
     * @param  csToMap  The transform from {@link #getCoordinateSystem()} to the rendering CS.
     * @param  mapToTxt The transform from the rendering CS to the Java2D CS.
     * @return The grid clip, or <code>null</code> if it can't be computed.
     */
    Rectangle getGridClip(final Rectangle zoomableBounds,
                          final MathTransform2D  csToMap,
                          final AffineTransform mapToTxt)
    {
        return null;
    }

    /**
     * Procède au traçage des marques de cette couche. Les classes dérivées ne
     * devraient pas avoir besoin de redéfinir cette méthode. Pour modifier la
     * façon de dessiner les marques, redéfinissez plutôt les méthodes de
     * {@link MarkIterator}.
     *
     * @throws TransformException if a coordinate transformation was required and failed.
     *
     * @see MarkIterator#visible
     * @see MarkIterator#position
     * @see MarkIterator#geographicArea
     * @see MarkIterator#markShape
     * @see MarkIterator#direction
     * @see MarkIterator#amplitude
     * @see #getTypicalAmplitude
     * @see #getAmplitudeUnit
     * @see MarkIterator#paint(Graphics2D, Shape)
     */
    protected void paint(final RenderingContext context) throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        final Graphics2D      graphics   = context.getGraphics();
        final AffineTransform graphicsTr = graphics.getTransform();
        final Stroke          oldStroke  = graphics.getStroke();
        final Paint           oldPaint   = graphics.getPaint();
        final Rectangle   zoomableBounds = context.getPaintingArea(); // Do not modify!
        final MarkIterator      iterator = getMarkIterator();
        iterator.font = graphics.getFont();
        if (transformedShape == null) {
            transformedShape = new TransformedShape();
        }
        try {
            context.setCoordinateSystem(context.textCS);
            graphics.setStroke(DEFAULT_STROKE);
            /*
             * If the transforms are not valids, compute them now. We will compute one affine
             * transform for each mark to be rendered. Affine transforms will be used for all
             * subsequent repaints, including repaints after zoom changes, unless the repaint
             * was trigged programatically through the {@link #repaint()} method.
             */
            if (!isValid(MARKS_MASK|GLYPHS_MASK)) {
                final MathTransform2D  csToMap;
                final AffineTransform mapToTxt;
                mapToTxt = context.getAffineTransform(context.mapCS, context.textCS);
                csToMap  = (MathTransform2D)context.getMathTransform(getCoordinateSystem(), context.mapCS);
                final Rectangle gridClip = getGridClip(zoomableBounds, csToMap, mapToTxt);
                /*
                 * On veut utiliser une transformation affine identitée (donc en utilisant
                 * une échelle basée sur les pixels plutôt que les coordonnées utilisateur),
                 * mais en utilisant la même rotation que celle qui a cours dans la matrice
                 * <code>mapToTxt</code>. On peut y arriver en utilisant l'identité ci-dessous:
                 *
                 *    [ m00  m01 ]     m00² + m01²  == constante sous rotation
                 *    [ m10  m11 ]     m10² + m11²  == constante sous rotation
                 */
                final double[] matrix = new double[6];
                mapToTxt.getMatrix(matrix);
                if (true) {
                    double scale;
                    scale = XMath.hypot(matrix[0], matrix[2]);
                    matrix[0] /= scale;
                    matrix[2] /= scale;
                    scale = XMath.hypot(matrix[1], matrix[3]);
                    matrix[1] /= scale;
                    matrix[3] /= scale;
                }
                /*
                 * Iterates through each mark while performing the following steps:
                 * (results will be saved for reuse during next rendering)
                 *
                 *   - Computes the glyph vectors and the positions of each labels (if any).
                 *   - Setup an affine transform taking in account the translation, scale and
                 *     rotation of the mark (if any).
                 */
                final double           typicalScale = getTypicalAmplitude();
                final FontRenderContext fontContext = graphics.getFontRenderContext();
                boolean hasMarks  = false;
                boolean hasLabels = false;
                int numShapes = 0;
                while (iterator.next()) {
                    if (!iterator.visible(gridClip)) {
                        continue;
                    }
                    Point2D position = iterator.position();
                    if (position == null) {
                        continue;
                    }
                    position = csToMap.transform(position, position);
                    if (Double.isNaN(matrix[4] = position.getX())) continue;
                    if (Double.isNaN(matrix[5] = position.getY())) continue;
                    mapToTxt.transform(matrix, 4, matrix, 4, 1);
                    /*
                     * Step 1 - Creates the glyph vector
                     */
                    GlyphVector glyphs = null;
                    float       glyphX = Float.NaN;
                    float       glyphY = Float.NaN;
                    final String label = iterator.label();
                    if (label != null) {
                        glyphs = iterator.font().createGlyphVector(fontContext, label);
                        LegendPosition labelPos = iterator.labelPosition();
                        Rectangle2D labelBounds = glyphs.getVisualBounds();
                        labelPos.setLocation(labelBounds, matrix[4], matrix[5]);
                        if (labelBounds.intersects(zoomableBounds)) {
                            glyphX = (float) labelBounds.getMinX();
                            glyphY = (float) labelBounds.getMaxY();
                            if (boundingBox == null) {
                                boundingBox = labelBounds.getBounds();
                            } else {
                                boundingBox.add(labelBounds);
                            }
                            hasLabels = true;
                        } else {
                            glyphs = null;
                        }
                    }
                    /*
                     * Step 2 - Computes the transform for the mark
                     */
                    transformedShape.shape = iterator.markShape();
                    if (transformedShape.shape != null) {
                        final double amplitude = iterator.amplitude();
                        if (!Double.isNaN(amplitude) && amplitude!=0) {
                            transformedShape.setTransform(matrix, 0);
                            transformedShape.scale(amplitude/typicalScale);
                            transformedShape.rotate(iterator.direction());
                            if (transformedShape.intersects(zoomableBounds)) {
                                final Rectangle bounds = transformedShape.getBounds();
                                if (boundingBox == null) {
                                    boundingBox = bounds;
                                } else {
                                    boundingBox.add(bounds);
                                }
                                hasMarks = true;
                            } else {
                                transformedShape.shape = null;
                            }
                        } else {
                            transformedShape.shape = null;
                        }
                    }
                    /*
                     * Final step - stores the result for future use.
                     */
                    if (glyphs==null && transformedShape.shape==null) {
                        continue;
                    }
                    if (markIndex == null) {
                        markIndex = new int[getCount()];
                    }
                    if (numShapes >= markIndex.length) {
                        final int capacity = numShapes + Math.min(Math.max(numShapes, 8), 2048);
                        markIndex = XArray.resize(markIndex, capacity);
                        if (glyphVectors != null) {
                            glyphVectors   = (GlyphVector[]) XArray.resize(glyphVectors, capacity);
                            glyphPositions = XArray.resize(glyphPositions, capacity*2);
                        }
                        if (markShapes != null) {
                            markShapes     = (Shape[])XArray.resize(markShapes, capacity);
                            markTransforms = XArray.resize(markTransforms, capacity*TRANSFORM_RECORD_LENGTH);
                        }
                    }
                    if (glyphs != null) {
                        if (glyphVectors == null) {
                            glyphVectors   = new GlyphVector[markIndex.length];
                            glyphPositions = new float[markIndex.length*2];
                        }
                        glyphVectors  [  numShapes  ] = glyphs;
                        glyphPositions[2*numShapes+0] = glyphX;
                        glyphPositions[2*numShapes+1] = glyphY;
                    }
                    if (transformedShape.shape != null) {
                        if (markShapes == null) {
                            markShapes     = new Shape[markIndex.length];
                            markTransforms = new double[markIndex.length*TRANSFORM_RECORD_LENGTH];
                        }
                        transformedShape.getMatrix(markTransforms, numShapes*TRANSFORM_RECORD_LENGTH);
                        markShapes[numShapes] = transformedShape.shape;
                        markIndex [numShapes] = iterator.getIteratorPosition();
                    }
                    numShapes++;
                }
                if (!hasMarks) {
                    markShapes     = null;
                    markTransforms = null;
                }
                if (!hasLabels) {
                    glyphVectors   = null;
                    glyphPositions = null;
                }
                if (markIndex != null) {
                    markIndex = XArray.resize(markIndex, numShapes);
                }
                if (glyphVectors != null) {
                    glyphVectors   = (GlyphVector[]) XArray.resize(glyphVectors, numShapes);
                    glyphPositions = XArray.resize(glyphPositions, numShapes*2);
                }
                if (markShapes != null) {
                    markShapes = (Shape[])XArray.resize(markShapes, numShapes);
                    markTransforms = XArray.resize(markTransforms, numShapes*TRANSFORM_RECORD_LENGTH);
                }
                validMask |= MARKS_MASK|GLYPHS_MASK;
            }
            /*
             * Now, paints the marks. 
             */
            final Point2D.Float labelXY = new Point2D.Float();
            for (int i=0; i<markIndex.length; i++) {
                Shape geographicArea = null;
                Shape      markShape = null;
                GlyphVector    label = null;
                iterator.setIteratorPosition(markIndex[i]);
                if (markShapes != null) {
                    transformedShape.shape = markShapes[i];
                    if (transformedShape.shape != null) {
                        transformedShape.setTransform(markTransforms, i*TRANSFORM_RECORD_LENGTH);
                        markShape = transformedShape;
                    }
                }
                if (glyphVectors!=null && glyphVectors[i]!=null) {
                    label = glyphVectors[i];
                    labelXY.x = glyphPositions[2*i+0];
                    labelXY.y = glyphPositions[2*i+1];
                }
                // TODO: geographic area and icon not yet implemented.
                iterator.paint(graphics, null, markShape, null, null, label, labelXY);
            }
        } finally {
            graphics.setTransform(graphicsTr);
            graphics.setStroke(oldStroke);
            graphics.setPaint(oldPaint);
        }
        context.addPaintedArea(boundingBox, context.textCS);
    }

    /**
     * Tells that some marks may have been added, removed or changed and that this layer need
     * to be repainted. This method can be invoked from any thread; it doesn't need to be the
     * <cite>Swing</cite> thread.
     */
    public void repaint() {
        synchronized (getTreeLock()) {
            clearCache();
        }
        super.repaint();
    }

    /**
     * Tells that a single mark need to be repainted. This method can be invoked iteratively when
     * few marks changed,  and when the change was slight  (e.g. a change of color).  If a lot of
     * marks changed, of if the changes are importants (e.g. marks were added, removed or moved),
     * then invoking {@link #repaint()} may be more effective.
     *
     * @param An iterator over the mark to be repainted.
     *        Only the current mark will be repainted.
     *
     * @task TODO: If the shape expanded or if the mark moved, we would need to repaint
     *             a bigger area. We can do this with the information provided by the
     *             iterator.
     */
    protected void repaint(final MarkIterator iterator) {
        synchronized (getTreeLock()) {
            if (isValid(MARKS_MASK)) {
                final int i = Arrays.binarySearch(markIndex, iterator.getIteratorPosition());
                if (i < 0) {
                    // The mark doesn't appear in current clip.
                    return;
                }
                if (transformedShape == null) {
                    transformedShape = new TransformedShape();
                }
                transformedShape.shape = markShapes[i];
                if (transformedShape.shape != null) {
                    transformedShape.setTransform(markTransforms, i*TRANSFORM_RECORD_LENGTH);
                    repaint(transformedShape.getBounds());
                    return;
                }
            }
            repaint();
        }
    }

    /**
     * Test is all the specified bits are set in {@link #validMask}.
     *
     * @see #MARKS_MASK
     * @see #GLPYHS_MASK
     */
    private boolean isValid(final int mask) {
        assert (markShapes  !=null) == (markTransforms!=null) : validMask;
        assert (glyphVectors!=null) == (glyphPositions!=null) : validMask;
        return (validMask & mask) == mask;
    }

    /**
     * Declares that some data need to be recomputed.
     * The mask -1 invalidate all.
     */
    private void invalidate(final int mask) {
        final int toClear = validMask & mask;
        validMask &= ~mask;
        if ((toClear & MARKS_MASK)!=0 && markShapes!=null) {
            Arrays.fill(markShapes,  null);
            Arrays.fill(markTransforms, 0);
            boundingBox = null;
        }
        if ((toClear & GLYPHS_MASK)!=0 && glyphVectors!=null) {
            Arrays.fill(glyphVectors, null);
            Arrays.fill(glyphPositions,  0);
        }
    }

    /**
     * Invoked when the zoom changed.
     *
     * @param change The zoom <strong>change</strong> in <strong>Java2D</strong> coordinate
     *        system, or <code>null</code> if unknow. If <code>null</code>, then this layer
     *        will be fully redrawn during the next rendering.
     */
    void zoomChanged(final AffineTransform change) {
        super.zoomChanged(change);
        if (change == null) {
            invalidate(-1);
            boundingBox = null;
        } else if (!change.isIdentity()) {
            /*
             * TODO: We could add an optimization here:  apply the change on all transforms
             *       instead of invalidating the whole layer. A starting point could be the
             *       following code:
             *
             *       if (transformedShape == null) {
             *           transformedShape = new TransformedShape();
             *       }
             *       for (int i=0; i<markTransforms.length; i+=TRANSFORM_RECORD_LENGTH) {
             *           transformedShape.setTransform(markTransforms, i);
             *           transformedShape.preConcatenate(change);
             *           transformedShape.getMatrix(markTransforms, i);
             *       }
             *
             *       However, some issues need to be solved: 1) We want to apply the full transform
             *       (translation, scale, rotation) on the mark positions.   However, we don't want
             *       to scale the mark shapes. But we do want to apply the rotation...   2) Current
             *       'Renderer' implementation does not give us exactly the "change" transform, but
             *       rather a slightly scaled one.
             */
            invalidate(-1);
            boundingBox = null;
        }
    }

    /**
     * Efface des informations qui avaient été conservées dans une mémoire cache.
     * Cette méthode est automatiquement appelée lorsqu'il a été déterminé que cette
     * couche ne sera plus affichée avant un certain temps.
     */
    void clearCache() {
        assert Thread.holdsLock(getTreeLock());
        validMask        = 0;
        glyphVectors     = null;
        glyphPositions   = null;
        markIndex        = null;
        markShapes       = null;
        markTransforms   = null;
        transformedShape = null;
        boundingBox      = null;
        typicalAmplitude = Double.NaN;
        super.clearCache();
    }




    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////    EVENTS (note: may be moved out of this class in a future version)    ////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Temporary point for mouse events.
     */
    private transient Point2D point;

    /**
     * Retourne le texte à afficher dans une bulle lorsque le curseur
     * de la souris traîne sur la carte. L'implémentation par défaut
     * identifie la marque sur laquelle traîne le curseur et appelle
     * {@link MarkIterator#getToolTipText()}.
     *
     * @param  event Coordonnées du curseur de la souris.
     * @return Le texte à afficher lorsque la souris traîne sur cet élément.
     *         Ce texte peut être nul pour signifier qu'il ne faut pas en écrire.
     */
    final String getToolTipText(final GeoMouseEvent event) {
        synchronized (getTreeLock()) {
            if (isValid(MARKS_MASK)) {
                MarkIterator iterator = null;
                if (transformedShape == null) {
                    transformedShape = new TransformedShape();
                }
                final Point2D point = this.point = event.getPixelCoordinate(this.point);
                for (int i=markIndex.length; --i>=0;) {
                    transformedShape.shape = markShapes[i];
                    if (transformedShape.shape != null) {
                        transformedShape.setTransform(markTransforms, i*TRANSFORM_RECORD_LENGTH);
                        if (transformedShape.contains(point)) {
                            if (iterator == null) {
                                iterator = getMarkIterator();
                            }
                            iterator.setIteratorPosition(markIndex[i]);
                            final String text = iterator.getToolTipText(event);
                            if (text != null) {
                                return text;
                            }
                        }
                    }
                }
            }
        }
        return super.getToolTipText(event);
    }
}
