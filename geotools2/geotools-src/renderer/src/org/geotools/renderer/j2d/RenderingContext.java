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
import java.util.List;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.FittedCoordinateSystem;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Informations relatives to a rendering in progress.  A <code>RenderingContext</code> object is
 * automatically created by {@link Renderer#paint} at rendering time. Then the renderer iterates
 * over layers and invokes {@link RenderedLayer#paint} for each of them.   The rendering context
 * is disposed once the rendering is completed. <code>RenderingContext</code> contains the
 * following informations:
 *
 * <ul>
 *   <li>The {@link Graphics2D} handler to use for rendering.</li>
 *   <li>The coordinate systems in use and the transformations between them.</li>
 *   <li>The area rendered up to date. This information is updated by each
 *       {@link RenderedLayer} while they are painting.</li>
 * </ul>
 *
 * A rendering usually imply the following transformations (names are {@linkplain CoordinateSystem
 * coordinate systems} and arrows are {@linkplain MathTransform transforms}):
 *
 * <p align="center">
 * {@link RenderedLayer#getCoordinateSystem layerCS} <img src="doc-files/right.png">
 * {@link #mapCS} <img src="doc-files/right.png">
 * {@link #textCS} <img src="doc-files/right.png">
 * {@link #deviceCS}
 * </p>
 *
 * @version $Id: RenderingContext.java,v 1.5 2003/01/24 23:40:21 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Renderer#paint
 * @see RenderedLayer#paint
 */
public final class RenderingContext {
    /**
     * Expansion factor for clip. When a clip for some rectangle is requested, a bigger
     * clip will be computed in order to avoid recomputing a new one if user zoom up or
     * apply translation. A scale of 2 means that rectangle two times wider and heigher
     * will be computed.
     *
     * DEBUGGING TIPS: Set this scale to a value below 1 to <em>see</em> the clipping's
     *                 effect in the window area.
     */
    private static final double CLIP_SCALE = 0.75;

    /**
     * The originating {@link Renderer}.
     */
    private final Renderer renderer;

    /**
     * The graphics where painting occurs. This graphics is initialized with
     * an affine transform appropriate for rendering geographic features in the
     * {@link #mapCS} coordinate system. The affine transform can be changed in
     * a convenient way with {@link #setCoordinateSystem}.
     * <br><br>
     * This graphics is set by {@link Renderer} when a new process in underway.
     * It is reset to <code>null</code> once the rendering is finished.
     *
     * @see #getGraphics
     * @see Renderer#paint
     */
    Graphics2D graphics;

    /**
     * The "real world" coordinate system for rendering. This is the coordinate system for
     * what the user will see on the screen.  Data from all {@link RenderedLayer}s will be
     * projected in this coordinate system before to be rendered.  Units are usually "real
     * world" metres.
     * <br><br>
     * This coordinate system is usually set once for a given {@link Renderer} and do not
     * change anymore, except if the user want to change the projection see on screen.
     *
     * @see #textCS
     * @see #setCoordinateSystem
     */
    public final CoordinateSystem mapCS;

    /**
     * The {@linkplain Graphics2D Java2D coordinate system}. Each "unit" is a dot (about
     * 1/72 of inch). <var>x</var> values increase toward the right of the screen and
     * <var>y</var> values increase toward the bottom of the screen.  This coordinate
     * system is appropriate for rendering text and labels.
     * <br><br>
     * This coordinate system may be different between two different renderings,
     * especially if the zoom (or map scale) has changed since the last rendering.
     *
     * @see #mapCS
     * @see #deviceCS
     * @see #setCoordinateSystem
     */
    public final CoordinateSystem textCS;

    /**
     * The device coordinate system. Each "unit" is a pixel of device-dependent size. When
     * rendering on screen, this coordinate system is identical to {@link #textCS}. When
     * rendering on printer or some other devices, it depends of the device's resolution.
     * This coordinate system is rarely used.
     *
     * @see #textCS
     * @see #setCoordinateSystem
     */
    public final CoordinateSystem deviceCS;

    /**
     * The painted area in the {@linkplain #deviceCS device coordinate system}, or
     * <code>null</code> if unknow. This field is built by {@link #addPaintedArea}
     * at rendering time.  The package-private method {@link RenderedLayer#update}
     * use and update this field.
     */
    Shape paintedArea;

    /**
     * The widget bounding box, in coordinates of {@link #deviceCS}.
     */
//    private final Rectangle bounds;

    /**
     * The {@link #bounds} rectangle transformed into logical coordinates
     * (according {@link #mapCS}). Will be computed only when first requested.
     */
//    private transient Rectangle2D logicalClip;

    /**
     * Objet à utiliser pour découper les polygones. Cet objet
     * ne sera créé que la première fois où il sera demandé.
     */
//    private transient Clipper clipper;

    /**
     * Construct a new <code>RenderingContext</code> for the specified {@link Renderer}.
     */
    RenderingContext(final Renderer         renderer,
                     final CoordinateSystem    mapCS,
                     final CoordinateSystem   textCS,
                     final CoordinateSystem deviceCS)
    {
        this.renderer = renderer;
        this.mapCS    =    mapCS;
        this.textCS   =   textCS;
        this.deviceCS = deviceCS;
    }

    /**
     * Returns the graphics where painting occurs. This graphics is initialized with
     * an affine transform appropriate for rendering geographic features in the
     * {@link #mapCS} coordinate system. The affine transform can be changed in
     * a convenient way with {@link #setCoordinateSystem}.
     */
    public Graphics2D getGraphics() {
        return graphics;
    }

    /**
     * Set the coordinate system in use for rendering in {@link Graphics2D}. Invoking this
     * method do not alter the current {@link Renderer}'s coordinate system.  It is only a
     * convenient way to set the {@linkplain Graphics2D#setTransform <code>Graphics2D</code>'s
     * affine transform}, for example in order to alternate rendering mode between geographic
     * features and texts. The specified coordinate system (argument <code>cs</code>) shoud be
     * one of {@link #mapCS}, {@link #textCS} or {@link #deviceCS} fields. Other coordinate
     * systems may work, but most of them will thrown an exception.
     *
     * @param cs The {@link #getGraphics() graphics} coordinate system.
     *           Should be {@link #mapCS}, {@link #textCS} or {@link #deviceCS}.
     * @throw TransformException if this method failed to find an affine transform from the
     *        specified coordinate system to the device coordinate system ({@link #deviceCS}).
     *
     * @see #getGraphics
     * @see #getAffineTransform
     * @see Graphics2D#setTransform
     */
    public void setCoordinateSystem(final CoordinateSystem cs) throws TransformException {
        graphics.setTransform(getAffineTransform(CTSUtilities.getCoordinateSystem2D(cs), deviceCS));
    }

    /**
     * Returns an affine transform between two coordinate systems.  This method is equivalents
     * to the following pseudo-code, except for the exception to be thrown if the transform is
     * not an instance of {@link AffineTransform}.
     * <blockquote><pre>
     * return (AffineTransform) {@link #getMathTransform getMathTransform}(sourceCS, targetCS);
     * </pre></blockquote>
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return An affine transform from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if the transform can't be created or is not affine.
     *
     * @see #getMathTransform
     * @see Renderer#getRenderingHint
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    public AffineTransform getAffineTransform(final CoordinateSystem sourceCS,
                                              final CoordinateSystem targetCS)
            throws CannotCreateTransformException
    {
        try {
            return (AffineTransform) renderer.getMathTransform(sourceCS, targetCS,
                                         "RenderingContext","getAffineTransform");
        } catch (ClassCastException cause) {
            throw new CannotCreateTransformException(
                    org.geotools.resources.cts.Resources.format(
                    org.geotools.resources.cts.ResourceKeys.ERROR_NOT_AN_AFFINE_TRANSFORM), cause);
        }
    }

    /**
     * Returns a transform between two coordinate systems. If a {@link
     * Hints#COORDINATE_TRANSFORMATION_FACTORY} has been provided to the {@link Renderer},
     * then the specified {@link CoordinateTransformationFactory} will be used. The arguments
     * are usually (but not necessarily) one of the following pairs:
     * <ul>
     *   <li><code>({@link RenderedLayer#getCoordinateSystem layerCS}, {@link #mapCS})</code>:
     *       Arbitrary transform from the data coordinate system (set in {@link RenderedLayer})
     *       to the rendering coordinate system (set in {@link Renderer}).</li>
     *   <li><code>({@link #mapCS}, {@link #textCS})</code>:
     *       {@linkplain AffineTransform Affine transform} from the rendering coordinate system
     *       in "real world" units (usually metres or degrees) to the Java2D coordinate system
     *       in dots (usually 1/72 of inch). This transform changes every time the zoom (or map
     *       scale) changes.</li>
     *   <li><code>({@link #textCS}, {@link #deviceCS})</code>:
     *       {@linkplain AffineTransform Affine transform} from dots to device units. When
     *       rendering target the screen, this transform is usually the identity transform.
     *       When printing, the affine transform is set up in such a way that rendering in
     *       Java2D is isolated from the printer resolution:  rendering in {@link #textCS}
     *       is performed as if printers always have a 72 DPI resolution (except that fractional
     *       coordinates are valid, e.g. a dot of size 0.1&times;&0.1), and the transform maps
     *       it to whatever the printer resolution is. This transform is zoom insensitive and
     *       constant as long as the target device do not change.</li>
     * </ul>
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return A transform from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if the transformation can't be created.
     *
     * @see #getAffineTransform
     * @see Renderer#getRenderingHint
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    public MathTransform getMathTransform(final CoordinateSystem sourceCS,
                                          final CoordinateSystem targetCS)
            throws CannotCreateTransformException
    {
        return renderer.getMathTransform(sourceCS, targetCS, "RenderingContext","getMathTransform");
    }

    /**
     * Declares that an area in {@link #getGraphics() graphics} coordinates has been painted.
     * The coordinate system for <code>area</code>  should be the same than the one just used for
     * painting in the {@link #getGraphics() graphics} handler, which depends of the {@linkplain
     * Graphics2D#getTransform affine transform currently set}. This method is equivalents to
     * <code>{@link #addPaintedArea(Shape, CoordinateSystem) addPaintedArea}(area, null)</code>.
     *
     * @param area A bounding shape of the area just painted. This shape may be approximative,
     *             as long as it completely encloses the painted area. Simple shape with fast
     *             <code>contains(...)</code> and <code>intersects(...)</code> methods are
     *             encouraged. The coordinate system is infered from the current
     *             {@link #getGraphics() graphics} state.
     *
     * @see Graphics2D#getTransform
     * @see #addPaintedArea(Shape, CoordinateSystem)
     */
    public void addPaintedArea(final Shape area) {
        try {
            addPaintedArea(area, null);
        } catch (TransformException exception) {
            // Should never happen, since the 'cs' argument was null.
            throw new AssertionError(exception);
        }
    }

    /**
     * Declares that an area has been painted. This method should be invoked from
     * {@link RenderedLayer#paint} at rendering time.   The {@link Renderer} uses
     * this information  in order  to determine which layers need to be repainted
     * when a screen area is damaged. If <code>addPaintedArea(...)</code> methods
     * are never invoked from a particular {@link RenderedLayer}, then the renderer
     * will assume that the painted area is unknow and conservatively repaint the full
     * layer during subsequent rendering.
     *
     * @param  area A bounding shape of the area just painted,  in <code>cs</code> coordinate
     *         system. This shape may be approximative, as long as it completely encloses the
     *         painted area. Simple shape with fast <code>contains(...)</code> and
     *         <code>intersects(...)</code> methods are encouraged.
     * @param  cs The coordinate system for <code>area</code>, or <code>null</code>
     *         to infer it from the current {@link #getGraphics() graphics} state.
     * @throws TransformException if <code>area</code> coordinates can't be transformed.
     */
    public void addPaintedArea(Shape area, final CoordinateSystem cs) throws TransformException {
        final Shape userArea = area;
        if (cs != null) {
            final MathTransform2D transform = (MathTransform2D)
                    renderer.getMathTransform(CTSUtilities.getCoordinateSystem2D(cs), deviceCS,
                                              "RenderingContext","addPaintedArea");
            if (!transform.isIdentity()) {
                area = transform.createTransformedShape(area);
            }
        } else {
            final AffineTransform transform = graphics.getTransform();
            if (!transform.isIdentity()) {
                area = transform.createTransformedShape(area);
            }
        }
        if (paintedArea == null) {
            if (area==userArea && area instanceof Area) {
                // Protect the user's object from changes,
                // since the code below may update the area.
                area = new Area(area);
            }
            paintedArea = area;
        } else {
            if (!(paintedArea instanceof Area)) {
                paintedArea = new Area(area);
            }
            ((Area) paintedArea).add((area instanceof Area) ? (Area) area : new Area(area));
        }
    }

    /**
     * Returns the drawing area in point coordinates.
     * For performance reason, this method do not clone
     * the area. Do not modify it!
     */
//    public Rectangle getDrawingArea() {
//        return bounds;
//    }

    /**
     * Clip a contour to the current widget's bounds. The clip is only approximative
     * in that the resulting contour may extends outside the widget's area. However,
     * it is garanteed that the resulting contour will contains at least the interior
     * of the widget's area (providing that the first contour in the supplied list
     * cover this area).
     *
     * This method is used internally by some layers (like
     * {@link org.geotools.renderer.j2d.IsolineLayer}) when computing and drawing a
     * clipped contour may be faster than drawing the full contour (especially if
     * clipped contours are cached for reuse).
     * <br><br>
     * This method expect a <em>modifiable</em> list of {@link Contour} objects as argument.
     * The first element in this list must be the "master" contour (the contour to clip) and
     * will never be modified.  Elements at index greater than 0 may be added and removed at
     * this method's discression, so that the list is actually used as a cache for clipped
     * <code>Contour</code> objects.
     *
     * <br><br>
     * <strong>WARNING: This method is not yet debugged</strong>
     *
     * @param  contours A modifiable list with the contour to clip at index 0.
     * @return A possibly clipped contour. May be any element of the list or a new contour.
     *         May be <code>null</code> if the "master" contour doesn't intercept the clip.
     */
//    public Contour clip(final List<Contour> contours) {
//        if (contours.isEmpty()) {
//            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_EMPTY_LIST));
//        }
//        if (isPrinting) {
//            return contours.get(0);
//        }
//        /*
//         * Gets the clip area expressed in MapPane's coordinate system
//         * (i.e. gets bounds in "logical visual coordinates").
//         */
//        if (logicalClip==null) try {
//            logicalClip = XAffineTransform.inverseTransform(fromWorld, bounds, logicalClip);
//        } catch (NoninvertibleTransformException exception) {
//            // (should not happen) Clip failed: conservatively returns the whole contour.
//            Utilities.unexpectedException("org.geotools.renderer.j2d",
//                                          "RenderingContext", "clip", exception);
//            return contours.get(0);
//        }
//        final CoordinateSystem targetCS = getViewCoordinateSystem();
//        /*
//         * Iterate through the list (starting from the last element)
//         * until we found a contour capable to handle the clip area.
//         */
//        Contour contour;
//        Rectangle2D clip;
//        Rectangle2D bounds;
//        Rectangle2D temporary=null;
//        int index=contours.size();
//        do {
//            contour = contours.get(--index);
//            clip    = logicalClip;
//            /*
//             * First, we need to know the clip in contour's coordinates.
//             * The {@link org.geotools.renderer.j2d.IsolineLayer} usually keeps
//             * isoline in the same coordinate system than the MapPane's
//             * one. But a user could invoke this method in a more unusual
//             * way, so we are better to check...
//             */
//            final CoordinateSystem sourceCS;
//            synchronized (contour) {
//                bounds   = contour.getCachedBounds();
//                sourceCS = contour.getCoordinateSystem();
//            }
//            if (!targetCS.equals(sourceCS, false)) try {
//                CoordinateTransformation toView = this.toView;
//                if (!toView.getSourceCS().equals(sourceCS, false)) {
//                    toView = Contour.getCoordinateTransformation(sourceCS, targetCS, "RenderingContext", "clip");
//                }
//                clip = temporary = CTSUtilities.transform((MathTransform2D)toView.getMathTransform(), clip, temporary);
//            } catch (TransformException exception) {
//                Utilities.unexpectedException("org.geotools.renderer.j2d",
//                                              "RenderingContext", "clip", exception);
//                continue; // A contour seems invalid. It will be ignored (and probably garbage collected soon).
//            }
//            /*
//             * Now that both rectangles are using the same coordinate system,
//             * test if the clip fall completly inside the contour. If yes,
//             * then we should use this contour for clipping.
//             */
//            if (Layer.contains(bounds, clip, true)) {
//                break;
//            }
//        } while (index!=0);
//        /*
//         * A clipped contour has been found (or we reached the begining
//         * of the list). Check if the requested clip is small enough to
//         * worth a clipping.
//         */
//        final double ratio2 = (bounds.getWidth()*bounds.getHeight()) / (clip.getWidth()*clip.getHeight());
//        if (ratio2 >= CLIP_SCALE*CLIP_SCALE) {
//            if (clipper == null) {
//                clipper = new Clipper(scale(logicalClip, CLIP_SCALE), targetCS);
//            }
//            // Remove the last part of the list, which is likely to be invalide.
//            contours.subList(index+1, contours.size()).clear();
//            contour = contour.getClipped(clipper);
//            if (contour != null) {
//                contours.add(contour);
//                Contour.LOGGER.finer("Clip performed"); // TODO: give more precision
//            }
//        }
//        return contour;
//    }

    /**
     * Expand or shrunk a rectangle by some factor. A scale of 1 lets the rectangle
     * unchanged. A scale of 2 make the rectangle two times wider and heigher. In
     * any case, the rectangle's center doesn't move.
     */
    private static Rectangle2D scale(final Rectangle2D rect, final double scale) {
        final double trans  = 0.5*(scale-1);
        final double width  = rect.getWidth();
        final double height = rect.getHeight();
        return new Rectangle2D.Double(rect.getX()-trans*width,
                                      rect.getY()-trans*height,
                                      scale*width, scale*height);
    }
}
