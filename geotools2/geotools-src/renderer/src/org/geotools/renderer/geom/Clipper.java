/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.renderer.geom;

// J2SE dependencies
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.IllegalPathStateException;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XArray;


/**
 * The clipping area to apply on a {@link Geometry} object. A <code>Clipper</code> object
 * contains a {@link Rectangle2D} and its {@link CoordinateSystem}.
 *
 * @version $Id: Clipper.java,v 1.5 2003/05/27 18:22:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Geometry#clip
 */
public final class Clipper {
    /**
     * Coordinate system of {@link #mapClip}.
     */
    final CoordinateSystem mapCS;

    /**
     * Region to clip.  The coordinates of this region must be expressed according to the
     * {@link #mapCS} coordinate system (specified at build time) and must not be modified.
     * This rectangle must be read only.
     */
    final Rectangle2D mapClip;

    /**
     * Region to clip. The coordinates are the same as in {@link #mapClip},
     * but projected in the native coordinate system of the polyline to be clipped.
     */
    private final Rectangle2D clip = new Rectangle2D.Double();

    /**
     * Minimum and maximum <var>x</var> and <var>y</var>
     * coordinates of {@link #clip}.
     */
    private float xmin, ymin, xmax, ymax;

    /**
     * Line representing a segment of a {@link Polyline} object. This object
     * is created just once in order to avoid having to recreate it too often.
     * It is used internally to sweep the coordinates of a {@link Polyline} object.
     */
    private final Line2D.Float line = new Line2D.Float();

    /**
     * Coordinates to transmit to {@link Polyline#appendBorder}. This table is reserved for
     * internal use by {@link #clip(Polyline)}, which will constantly construct and modify it.
     */
    private float[] border = new float[16];

    /**
     * Number of valid elements in {@link #border}. This field will be incremented
     * as necessary by the {@link #addBorder} method.
     */
    private int borderLength;

    /**
     * Coordinates of intersection points between a polyline and {@link #clip}. This table is
     * reserved for internal use by {@link #clip(Polyline)}, which will constantly construct
     * and modify it.
     */
    private float[] intersect = new float[8];

    /**
     * Number of valid elements in {@link #intersect}. This field will be incremented
     * as necessary by the {@link #addIntersect} method.
     */
    private int intersectLength;

    /**
     * Constructs a clipping area.
     *
     * @param mapClip The limits of the region to keep.
     * @param mapCS <code>clip</code>'s coordinate system.
     */
    public Clipper(final Rectangle2D mapClip, final CoordinateSystem mapCS) {
        this.mapCS   = mapCS;
        this.mapClip = mapClip;
        this.clip.setRect(mapClip);
        xmin = (float) clip.getMinX();
        xmax = (float) clip.getMaxX();
        ymin = (float) clip.getMinY();
        ymax = (float) clip.getMaxY();
    }

    /**
     * Returns the clip in the polyline's internal coordinate system. This method
     * <strong>must</strong> be invoked before {@link #clip(Polyline)}.
     *
     * @param  The polyline to clip.
     * @return The clip in polyline's internal coordinate system. Will never be null, but may
     *         be empty if a transformation failed. Note: this method returns an internal
     *         rectangle. <strong>Do not modify</strong>.
     */
    final Rectangle2D getInternalClip(final Polyline polyline) {
        try {
            final MathTransform2D tr;
            tr = Polyline.getMathTransform2D(polyline.getTransformationFromInternalCS(mapCS));
            if (tr != null) {
                CTSUtilities.transform((MathTransform2D) tr.inverse(), mapClip, clip);
            } else {
                clip.setRect(mapClip);
            }
        } catch (TransformException exception) {
            // Cette exception peut être normale.
            LineString.unexpectedException("Clipper", "getInternalClip", exception);
            clip.setRect(0,0,0,0);
        }
        xmin = (float) clip.getMinX();
        xmax = (float) clip.getMaxX();
        ymin = (float) clip.getMinY();
        ymax = (float) clip.getMaxY();
        return clip;
    }

    /**
     * Adds a (<var>x0</var>,<var>y0</var>) point to
     * the end of the border {@link #border}. The table will be
     * automatically expanded as necessary.
     */
    private void addBorder(final float x0, final float y0) {
        if (borderLength >= 2) {
            if (border[borderLength-2]==x0 &&
                border[borderLength-1]==y0)
            {
                return;
            }
        }
        if (borderLength >= border.length) {
            border = XArray.resize(border, 2*borderLength);
        }
        border[borderLength++] = x0;
        border[borderLength++] = y0;
    }

    /**
     * Adds a (<var>x0</var>,<var>y0</var>) point to
     * the list of intersections {@link #intersect}.
     * The table will be automatically expanded as necessary.
     */
    private void addIntersect(final float x0, final float y0) {
        if (intersectLength >= 2) {
            if (intersect[intersectLength-2]==x0 &&
                intersect[intersectLength-1]==y0)
            {
                return;
            }
        }
        if (intersectLength >= intersect.length) {
            intersect = XArray.resize(intersect, 2*intersectLength);
        }
        intersect[intersectLength++] = x0;
        intersect[intersectLength++] = y0;
    }

    /**
     * Constructs a border so as to link point (<var>x0</var>,<var>y0</var>) to point
     * (<var>x1</var>,<var>y1</var>) exclusively without clipping the rectangle {@link #clip}. The
     * necessary points will be added to the table {@link #border}.
     *
     * @param clockwise Indicates how we should go round the rectangle {@link #clip} adding
     *        the points.  A positive value will go clockwise, whilst a negative value
     *        will go anticlockwise.  The value 0 will have no effect.
     * @param x0 <var>x</var> coordinates of the first point detected outside {@link #clip}.
     * @param y0 <var>y</var> coordinates of the first point detected outside {@link #clip}.
     * @param x1 <var>x</var> coordinates of the last point detected outside {@link #clip}.
     * @param y1 <var>y</var> coordinates of the last point detected outside {@link #clip}.
     */
    private void buildBorder(final double clockwise, float x0, float y0,
                             final float x1, final float y1)
    {
        if (clockwise > 0) {
            while (true) {
                if (y0 >= ymax) {
                    if (y1>=ymax && x1>=x0) break;
                    if (x0<xmax) addBorder(x0=xmax, y0=ymax);
                }
                if (x0 >= xmax) {
                    if (x1>=xmax && y1<=y0) break;
                    if (y0>ymin) addBorder(x0=xmax, y0=ymin);
                }
                if (y0 <= ymin) {
                    if (y1<=ymin && x1<=x0) break;
                    if (x0>xmin) addBorder(x0=xmin, y0=ymin);
                }
                if (x0 <= xmin) {
                    if (x1<=xmin && y1>=y0) break;
                    if (y0<ymax) addBorder(x0=xmin, y0=ymax);
                }
            }
        } else if (clockwise < 0) {
            while (true) {
                if (y0 >= ymax) {
                    if (y1>=ymax && x1<=x0) break;
                    if (x0>xmin) addBorder(x0=xmin, y0=ymax);
                }
                if (x0 <= xmin) {
                    if (x1<=xmin && y1<=y0) break;
                    if (y0>ymin) addBorder(x0=xmin, y0=ymin);
                }
                if (y0 <= ymin) {
                    if (y1<=ymin && x1>=x0) break;
                    if (x0<xmax) addBorder(x0=xmax, y0=ymin);
                }
                if (x0 >= xmax) {
                    if (x1>=xmax && y1>=y0) break;
                    if (y0<ymax) addBorder(x0=xmax, y0=ymax);
                }
            }
        }
    }

    /**
     * Attaches the polyline <code>subpoly</code> to the end of polyline <code>result</code>.
     * The border {@link #border}, if there is one, will be inserted between the two. This
     * method returns the polyline resulting from the merger.
     *
     * @param result  The first polyline, or <code>null</code>. This polyline will be modified.
     *                We usually create this polyline inside this method and reuse it in many
     *                calls in order to build the clipped polyline.
     * @param subpoly The second polyline (usually the result of a call to {@link Polygob#subpoly}.
     *                This polyline will never be modified.
     * @return <code>result</code>, or a new polyline if <code>result</code> was null.
     */
    private Polyline attach(Polyline result, Polyline subpoly) {
        if (subpoly != null) {
            try {
                if (result == null) {
                    result = (Polyline) subpoly.clone();
                    result.prependBorder(border, 0, borderLength, null);
                } else {
                    result.appendBorder(border, 0, borderLength, null);
                    result.append(subpoly); // 'subpoly.clone()' done by 'append'.
                }
                borderLength = 0;
            } catch (TransformException exception) {
                // Should not happen, since we are working
                // in polyline's native coordinate system.
                Polyline.unexpectedException("clip", exception);
            }
        }
        return result;
    }

    /**
     * Returns a polyline which only contains the points of <code>polyline</code> that appear
     * in the rectangle specified in the constructeur. If none of the polyline's points appear
     * inside <code>clip</code>, this method returns <code>null</code>. If all the polyline's
     * points appear inside <code>clip</code>, this method returns <code>polyline</code>.
     * Otherwise, this method returns a polyline that contains the points that appear inside
     * <code>clip</code>. These polylines will share the same data as <code>polyline</code>
     * where possible, so that memory consumption should remain reasonable.
     * <br><br>
     * Note: before calling this method, {@link #getInternalClip} must have been called.
     *
     * @param  polyline Polyline to clip in a region.
     * @return Clipped polyline.
     */
    final Polyline clip(final Polyline polyline) {
        borderLength    = 0;
        intersectLength = 0;
        Polyline result = null;
        final boolean isClosed = polyline.isClosed();
        final LineString.Iterator it;
        try {
            it = polyline.iterator(null);
        } catch (CannotCreateTransformException exception) {
            // Should not happen, since we are working in polyline's native CS.
            final IllegalPathStateException e;
            e = new IllegalPathStateException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
        /*
         * Obtains the first coordinate of the polyline. This first coordinate will be memorised
         * in the variables <code>first[X/Y]</code> so it can be reused to eventually close the
         * polyline. We should check whether this first coordinate is inside or outside the region
         * of interest.  This check serves to initialise the variable <code>inside</code>, which
         * will serve for the rest of this method.
         */
        if (it.next(line)) {
            final float firstX = line.x2;
            final float firstY = line.y2;
            boolean inside = (firstX>=xmin && firstX<=xmax) && (firstY>=ymin && firstY<=ymax);
            float  initialX1        =  Float.NaN;
            float  initialY1        =  Float.NaN;
            double initialClockwise = Double.NaN;
            float  x0               =  Float.NaN;
            float  y0               =  Float.NaN;
            double clockwise        =  0;
            int lower=0, upper=0;
            while (true) {
                /*
                 * Extracts the following coordinates. The point <code>line.p2</code>
                 * will contain the point that we have just extracted, whilst
                 * point <code>line.p1</code> will be the coordinate we had during the
                 * previous pass through this loop.  If all the coordinates have been
                 * swept, we will reuse the first point to reclose the polyline.
                 */
                if (!it.next(line)) {
                    if ((line.x2!=firstX || line.y2!=firstY) && isClosed) {
                        line.x2 = firstX;
                        line.y2 = firstY;
                    }
                    else break;
                }
                upper++;
                /*
                 * Checks whether the segment (x1,y1)-(x2,y2) goes clockwise round the rectangle.
                 * The segments inside the rectangle will not be taken into account. In the 
                 * example below, the segment goes anti-clockwise.
                 *
                 * +--------+
                 * |        |
                 * |        |   //(x2,y2)
                 * +--------+  //
                 *            //(x1,y1)
                 */
                int outcode1=0;
                int outcode2=0;
                boolean out1, out2;
                final float x1 = line.x1;
                final float y1 = line.y1;
                final float x2 = line.x2;
                final float y2 = line.y2;
                final float dx = x2-x1;
                final float dy = y2-y1;

                if (out1 = y1>ymax) outcode1 |= Rectangle2D.OUT_BOTTOM;
                if (out2 = y2>ymax) outcode2 |= Rectangle2D.OUT_BOTTOM;
                if (out1 && out2) clockwise += dx;
                else if (out1)    clockwise += dx/dy*(ymax-y1);
                else if (out2)    clockwise += dx/dy*(y2-ymax);

                if (out1 = y1<ymin) outcode1 |= Rectangle2D.OUT_TOP;
                if (out2 = y2<ymin) outcode2 |= Rectangle2D.OUT_TOP;
                if (out1 && out2) clockwise -= dx;
                else if (out1)    clockwise -= dx/dy*(ymin-y1);
                else if (out2)    clockwise -= dx/dy*(y2-ymin);

                if (out1 = x1>xmax) outcode1 |= Rectangle2D.OUT_RIGHT;
                if (out2 = x2>xmax) outcode2 |= Rectangle2D.OUT_RIGHT;
                if (out1 && out2) clockwise -= dy;
                else if (out1)    clockwise -= dy/dx*(xmax-x1);
                else if (out2)    clockwise -= dy/dx*(x2-xmax);

                if (out1 = x1<xmin) outcode1 |= Rectangle2D.OUT_LEFT;
                if (out2 = x2<xmin) outcode2 |= Rectangle2D.OUT_LEFT;
                if (out1 && out2) clockwise += dy;
                else if (out1)    clockwise += dy/dx*(xmin-x1);
                else if (out2)    clockwise += dy/dx*(x2-xmin);
                /*
                 * Now checks whether the points (x1,y1) and (x2,y2) are both outside
                 * the clip. If both are outside it does not mean that there are no 
                 * intersections between the line P1-P2 and the clip. It is necessary to 
                 * check (we will do this later).  A first stage has already been done
                 * with the condition <code>(outcode1 & outcode2)==0</code>, which allowed
                 * us to check that the two points are not from the same side of the rectangle.
                 */
                final boolean lineInsideAndOutside = (inside != (outcode2==0));
                final boolean lineCompletlyOutside = !lineInsideAndOutside &&
                                        (outcode1!=0 && outcode2!=0 && (outcode1 & outcode2)==0);
                /*
                 * Adds the intersection points to the border, if it has been determined
                 * that the intersection points should be added.  This situation occurs
                 * in the following three cases:
                 *
                 *  1) We have just entered the clip. The code below will construct all
                 *     the border that precedes the entrance. We complete this border with the
                 *     intersection point between the clip and the polyline.
                 *  2) We have just left the clip.  The code below will memorise the data
                 *     needed to render the polyline that is found entirely inside the clip.
                 *     We complete these data with the intersection point between the clip and
                 *     the polyline. Later in the loop, a border will be added after this
                 *     intersection point, followed by another intersection point (stage 1).
                 *  3) It is possible that we have gone through the whole clip without
                 *     stopping inside.  Code below will try to detect this particular situation.
                 */
                intersectLength = 0;
                if (lineInsideAndOutside || lineCompletlyOutside) {
                    final float cxmin = Math.max(xmin, Math.min(x1, x2));
                    final float cxmax = Math.min(xmax, Math.max(x1, x2));
                    final float cymin = Math.max(ymin, Math.min(y1, y2));
                    final float cymax = Math.min(ymax, Math.max(y1, y2));

                    if (ymax>=cymin && ymax<=cymax) {
                        final float v = dx/dy*(ymax-y1)+x1;
                        if (v>=cxmin && v<=cxmax) addIntersect(v, ymax);
                    }
                    if (ymin>=cymin && ymin<=cymax) {
                        final float v = dx/dy*(ymin-y1)+x1;
                        if (v>=cxmin && v<=cxmax) addIntersect(v, ymin);
                    }
                    if (xmax>=cxmin && xmax<=cxmax) {
                        final float v = dy/dx*(xmax-x1)+y1;
                        if (v>=cymin && v<=cymax) addIntersect(xmax, v);
                    }
                    if (xmin>=cxmin && xmin<=cxmax) {
                        final float v = dy/dx*(xmin-x1)+y1;
                        if (v>=cymin && v<=cymax) addIntersect(xmin, v);
                    }
                    /*
                     * Classifies intersection points using a 'classement à bulles'.
                     * This method is in theory extremely counter-productive when there is
                     * a lot of data to classify.  But in our case, there will never normally
                     * be more than 2 points to classify, which makes this technique very
                     * advantageous.
                     */
                    boolean modified; do {
                        modified = false;
                        for (int i=2; i<intersectLength; i+=2) {
                            if ((intersect[i-2]-x1)*dx+(intersect[i-1]-y1)*dy >
                                (intersect[i+0]-x1)*dx+(intersect[i+1]-y1)*dy)
                            {
                                final float x  = intersect[i-2];
                                final float y  = intersect[i-1];
                                intersect[i-2] = intersect[i+0];
                                intersect[i-1] = intersect[i+1];
                                intersect[i+0] = x;
                                intersect[i+1] = y;
                                modified = true;
                            }
                        }
                    } while (modified);
                }
                if (lineInsideAndOutside) {
                    /*
                     * An intersection has been found. We might have just entered the area of 
                     * interest or just left it.  The variable <code>inside</code> will indicate
                     * whether we have just entered or left the <code>clip</code> area.
                     */
                    inside = !inside;
                    if (inside) {
                        /*
                         * If we have just entered the area of interest {@link #clip}, checks whether
                         * it should add points to surround the clip border. These points will be
                         * effectively memorised later, when we leave the clip.
                         */
                        float xn,yn;
                        if (intersectLength >= 2) {
                            xn = intersect[0];
                            yn = intersect[1];
                        } else {
                            xn = x1;
                            yn = y1;
                        }
                        if (Float.isNaN(x0) || Float.isNaN(y0)) {
                            initialClockwise = clockwise;
                            initialX1        = xn;
                            initialY1        = yn;
                        } else {
                            buildBorder(clockwise, x0, y0, xn, yn);
                        }
                        x0 = Float.NaN;
                        y0 = Float.NaN;
                        clockwise = 0;
                    } else {
                        /*
                         * If we have just left the area of interest, we will create a new 
                         * "sub-polyline" that will contain only the data that appears in the
                         * region (the data will not be copied; only a reference game will be
                         * carried out). The coordinates x0,y0 will be those of the first
                         * point outside the clip.
                         */
                        if (intersectLength >= 2) {
                            x0 = intersect[intersectLength-2];
                            y0 = intersect[intersectLength-1];
                        } else {
                            x0 = x2;
                            y0 = y2;
                        }
                        assert upper <= polyline.getPointCount() : upper;
                        result = attach(result, polyline.subpoly(lower, upper));
                    }
                    lower = upper;
                    /*
                     * Adds the intersection points to the border.
                     * The method {@link #addBorder} will ensure that we do
                     * not repeat the same points twice.
                     */
                    for (int i=0; i<intersectLength;) {
                        addBorder(intersect[i++], intersect[i++]);
                    }
                } else if (lineCompletlyOutside) {
                    /*
                     * We now know that the points (x1,y1) and (x2,y2) are both
                     * outside the clip. But that doesn't mean that there was no 
                     * intersection between the line P1-P2 and the clip.  If there
                     * are at least two intersection points, the line goes through
                     * the clip and we must add it to the border.
                     */
                    if (intersectLength >= 4) {
                        /*
                         * First of all, we recalculate <code>clockwise</code>
                         * (see above) but only counting the component due at the
                         * end of the line (i.e. "if (out2) ...").
                         */
                        double clockwise2 = 0;
                        if ((outcode1 & Rectangle2D.OUT_BOTTOM)==0 &&
                            (outcode2 & Rectangle2D.OUT_BOTTOM)!=0) {
                                clockwise2 += dx/dy*(y2-ymax);
                        }
                        if ((outcode1 & Rectangle2D.OUT_TOP)==0 &&
                            (outcode2 & Rectangle2D.OUT_TOP)!=0) {
                                clockwise2 -= dx/dy*(y2-ymin);
                        }
                        if ((outcode1 & Rectangle2D.OUT_RIGHT)==0 &&
                            (outcode2 & Rectangle2D.OUT_RIGHT)!=0) {
                                clockwise2 -= dy/dx*(x2-xmax);
                        }
                        if ((outcode1 & Rectangle2D.OUT_LEFT)==0 &&
                            (outcode2 & Rectangle2D.OUT_LEFT)!=0) {
                                clockwise2 += dy/dx*(x2-xmin);
                        }
                        clockwise -= clockwise2;
                        if (Float.isNaN(x0) || Float.isNaN(y0)) {
                            initialClockwise = clockwise;
                            initialX1        = line.x1;
                            initialY1        = line.y1;
                        } else {
                            buildBorder(clockwise, x0, y0, intersect[0], intersect[1]);
                        }
                        x0 = intersect[intersectLength-2];
                        y0 = intersect[intersectLength-1];
                        clockwise = clockwise2;
                        /*
                         * Adds the intersection points to the border.
                         * The method {@link #addBorder} ensures that we do not
                         * repeat the same points twice.
                         */
                        for (int i=0; i<intersectLength;) {
                            addBorder(intersect[i++], intersect[i++]);
                        }
                    }
                }
            }
            /*
             * At the end of the loop, adds the remaining points
             * if they were inside the clip.
             */
            if (inside) {
                assert upper <= polyline.getPointCount() : upper;
                result = attach(result, polyline.subpoly(lower, upper));
            }
            if (isClosed) {
                if (!Float.isNaN(x0) && !Float.isNaN(y0)) {
                    buildBorder(clockwise+initialClockwise, x0, y0, initialX1, initialY1);
                }
            }
            if (result != null) {
                try {
                    result.appendBorder(border, 0, borderLength, null);
                    if (isClosed) {
                        result.close();
                    }
                } catch (TransformException exception) {
                    // Should not happen, since we are working
                    // in polyline's native coordinate system.
                    Polyline.unexpectedException("clip", exception);
                }
            } else if (borderLength != 0) {
                /*
                 * If no polyline has been created, but we have nevertheless
                 * detected intersections (i.e. if the zoom doesn't contain
                 * any point of the polyline but intercepts one of the polyline's
                 * lines) then we will add the intersection points and their borders.
                 */
                final float tmp[];
                if (border.length == borderLength) {
                    tmp = border;
                } else {
                    tmp = new float[borderLength];
                    System.arraycopy(border, 0, tmp, 0, borderLength);
                }
                final Polyline[] results = Polyline.getInstances(tmp, 0, tmp.length,
                                                    polyline.getCoordinateSystem());
                assert results.length == 1;
                result = results[0];
                if (isClosed) {
                    result.close();
                }
            } else if (polyline.contains(clip.getCenterX(), clip.getCenterY())) {
                /*
                 * If absolutely no point of the polyline is found inside the zoom
                 * then the zoom is either completely inside or completely outside
                 * the polyline.  If it is completely inside, we will memorise a rectangle
                 * that will cover the whole zoom.
                 */
                result = new Polyline(clip, polyline.getCoordinateSystem());
                if (isClosed) {
                    result.close();
                }
            }
        }
        return result;
    }
}
