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
 * Informations relatives to the rendering of {@link RenderedObject}s. An
 * <code>RenderingContext</code> object is created by {@link Renderer#paint} and
 * passed to {@link RenderedObject#paint} method for every layer to renderer. A
 * <code>RenderingContext</code> contains informations about coordinates transformations
 * to apply.  A rendering process usually imply the following transformations (names are
 * {@linkplain CoordinateSystem coordinate systems} and arrows are {@linkplain MathTransform
 * transforms}:
 *
 * <p align="center">
 * {@link RenderedObject#getCoordinateSystem layerCS} <img src="doc-files/right.png">
 * {@link #mapCS} <img src="doc-files/right.png">
 * {@link #textCS} <img src="doc-files/right.png">
 * {@link #deviceCS}
 * </p>
 *
 * @version $Id: RenderingContext.java,v 1.2 2003/01/20 23:21:10 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Renderer#paint
 * @see RenderedObject#paint
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
     */
    public final Graphics2D graphics;

    /**
     * The "real world" coordinate system for rendering. This is the coordinate system for
     * what the user will see on the screen. Data from all {@link RenderedObject}s will be
     * projected in this coordinate system before to be rendered.  Units are usually "real
     * world" metres.
     */
    public final CoordinateSystem mapCS;

    /**
     * The {@linkplain Graphics2D Java2D coordinate system}. Each "unit" is a dot (about
     * 1/72 of inch). <var>x</var> values increase toward the right of the screen and
     * <var>y</var> values increase toward the bottom of the screen. This coordinate system
     * is appropriate for rendering text and labels.
     */
    public final CoordinateSystem textCS;

    /**
     * The device coordinate system. Each "unit" is a pixel of device-dependent size. When
     * rendering on screen, this coordinate system is identical to {@link #textCS}. When
     * rendering on printer or some other devices, it may be different. This coordinate
     * system is rarely directly used.
     */
    public final CoordinateSystem deviceCS;

    /**
     * Position et dimension de la région de la
     * fenêtre dans lequel se fait le traçage.
     */
    private final Rectangle bounds;

    /**
     * The {@link #bounds} rectangle transformed into logical
     * coordinates (according {@link #getViewCoordinateSystem}).
     */
//    private transient Rectangle2D logicalClip;

    /**
     * Objet à utiliser pour découper les polygones. Cet objet
     * ne sera créé que la première fois où il sera demandé.
     */
//    private transient Clipper clipper;

    /**
     * Construit un objet <code>RenderingContext</code> avec les paramètres spécifiés.
     * Ce constructeur ne fait pas de clones.
     */
    RenderingContext(final Renderer         renderer,
                     final Graphics2D       graphics,
                     final CoordinateSystem    mapCS,
                     final CoordinateSystem   textCS,
                     final CoordinateSystem deviceCS,
                     final Rectangle          bounds)
    {
        this.renderer = renderer;
        this.graphics = graphics;
        this.mapCS    =    mapCS;
        this.textCS   =   textCS;
        this.deviceCS = deviceCS;
        this.bounds   = bounds;
    }

    /**
     * Set the coordinate system in use for rendering in {@link Graphics2D}. Invoking this
     * method do not alter the {@link Renderer} coordinate system. It is only a convenient
     * way to set the  {@linkplain Graphics2D#setTransform <code>Graphics2D</code>'s
     * affine transform}, for example in order to alternate between rendering geographic
     * features and text. The coordinate system specified in argument shoud be one of
     * {@link #mapCS}, {@link #textCS} or {@link #deviceCS} fields. Other coordinate systems
     * may thrown an exception.
     *
     * @param cs The {@link graphics} coordinate system. Should be {@link #mapCS},
     *          {@link #textCS} or {@link #deviceCS}.
     * @throw IllegalArgumentException if the coordinate system is invalid.
     *
     * @see #graphics
     * @see #getMathTransform
     * @see Graphics2D#setTransform
     */
    public void setCoordinateSystem(final CoordinateSystem cs) throws IllegalArgumentException {
        CannotCreateTransformException cause;
        try {
            final MathTransform tr = getMathTransform(cs, deviceCS);
            if (tr instanceof AffineTransform) {
                graphics.setTransform((AffineTransform) tr);
                return;
            }
            cause = null;
        } catch (CannotCreateTransformException exception) {
            cause = exception;
        }
        final IllegalArgumentException exception = new IllegalArgumentException(
                  org.geotools.resources.cts.Resources.format(
                  org.geotools.resources.cts.ResourceKeys.ERROR_NOT_AN_AFFINE_TRANSFORM));
        exception.initCause(cause);
        throw exception;
    }

    /**
     * Construct a transform between two coordinate systems. The coordinate arguments
     * are usually (but not necessarily) one of the following pairs:
     * <ul>
     *   <li><code>({@link RenderedObject#getCoordinateSystem layerCS}, {@link #mapCS})</code>:
     *       Arbitrary transform from the {@link RenderedObject} coordinate system to the
     *       rendering coordinate system.</li>
     *   <li><code>({@link #mapCS}, {@link #textCS})</code>:
     *       Transformation affine convertissant les mètres vers les unités de texte (1/72 de pouce).
     *       Ces unités de textes pourront ensuite être converties en unités du périphérique avec la
     *       transformation ci-dessous. Cette transformation peut varier en fonction de l'échelle de
     *       la carte.</li>
     *   <li><code>({@link #textCS}, {@link #deviceCS})</code>:
     *       Transformation affine convertissant des unités de texte (1/72 de pouce) en unités
     *       dépendantes du périphérique. Lors des sorties vers l'écran, cette transformation
     *       est généralement la matrice identité. Pour les écritures vers l'imprimante, il s'agit
     *       d'une matrice configurée d'une façon telle que chaque point correspond à environ 1/72
     *       de pouce. Cette transformation affine reste habituellement identique d'un traçage à
     *       l'autre de la composante. Elle ne varie que si on change de périphérique, par exemple
     *       si on dessine vers l'imprimante plutôt que l'écran.</li>
     * </ul>
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return A transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if the transformation can't be created.
     */
    public MathTransform getMathTransform(final CoordinateSystem sourceCS,
                                          final CoordinateSystem targetCS)
            throws CannotCreateTransformException
    {
        return renderer.getMathTransform(sourceCS, targetCS);
    }

    /**
     * Returns <code>true</code> if the rendering is performed on the
     * screen or any other devices with an identity default transform.
     * This method usually returns <code>false</code> during printing.
     */
    final boolean normalDrawing() {
        return textCS == deviceCS;
    }

    /**
     * Returns the drawing area in point coordinates.
     * For performance reason, this method do not clone
     * the area. Do not modify it!
     */
    public Rectangle getDrawingArea() {
        return bounds;
    }

    /**
     * Returns the rendered area in point coordinates.
     *
     * @task TODO: Not yet implemented.
     */
    final Shape getRenderedArea() {
        return null; // TODO
    }

    /**
     * Clip a contour to the current widget's bounds. The clip is only approximative
     * in that the resulting contour may extends outside the widget's area. However,
     * it is garanteed that the resulting contour will contains at least the interior
     * of the widget's area (providing that the first contour in the supplied list
     * cover this area).
     *
     * This method is used internally by some layers (like {@link fr.ird.map.layer.IsolineLayer})
     * when computing and drawing a clipped contour may be faster than drawing the full contour
     * (especially if clipped contours are cached for reuse).
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
//         * Gets the clip area expressed in MapPanel's coordinate system
//         * (i.e. gets bounds in "logical visual coordinates").
//         */
//        if (logicalClip==null) try {
//            logicalClip = XAffineTransform.inverseTransform(fromWorld, bounds, logicalClip);
//        } catch (NoninvertibleTransformException exception) {
//            // (should not happen) Clip failed: conservatively returns the whole contour.
//            Utilities.unexpectedException("fr.ird.map", "RenderingContext", "clip", exception);
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
//             * The {@link fr.ird.map.layer.IsolineLayer} usually keeps
//             * isoline in the same coordinate system than the MapPanel's
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
//                Utilities.unexpectedException("fr.ird.map", "RenderingContext", "clip", exception);
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
