/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2000, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
package org.geotools.gui.swing;

// Graphical user interface
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;

// Graphics
import java.awt.Font;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

// Geometry
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

// Miscellaneous
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Axis
import org.geotools.axis.Graduation;
import org.geotools.axis.TickIterator;
import org.geotools.axis.NumberGraduation;
import org.geotools.axis.AbstractGraduation;
import org.geotools.axis.LogarithmicNumberGraduation;

// Geotools dependencies
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.geotools.units.Unit;
import org.geotools.util.NumberRange;
import org.geotools.resources.Utilities;
import org.geotools.resources.GCSUtilities;

// Resources (Note: CTS resources are okay for this class).
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A color bar with a graduation. The colors can be specified with a {@link SampleDimension},
 * an array of {@link Color}s or an {@link IndexColorModel} bject, and the graduation is
 * specified with a {@link Graduation} object. The resulting <code>ColorBar</code> object
 * is usually painted together with a remote sensing image, for example in a
 * {@link org.geotools.gui.swing.MapPane} object.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/ColorBar.png"></p>
 * <p>&nbsp;</p>
 *
 * @version $Id: ColorBar.java,v 1.9 2004/02/13 14:29:37 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ColorBar extends JComponent {
    /**
     * Margin (in pixel) on each sides: top, left, right and bottom of the color ramp.
     */
    private static final int MARGIN = 10;

    /**
     * An empty list of colors.
     */
    private static final Color[] EMPTY = new Color[0];

    /**
     * The graduation to write over the color ramp.
     */
    private Graduation graduation;

    /**
     * Graduation units. This is constructed from {@link Graduation#getUnit} and cached
     * for faster rendering.
     */
    private String units;

    /**
     * The colors to paint (never <code>null</code>).
     */
    private Color[] colors = EMPTY;

    /**
     * <code>true</code> if tick label must be display.
     */
    private boolean labelVisibles = true;

    /**
     * <code>true</code> if tick label can be display with an automatic
     * color. The automatic color will be white or black depending the
     * background color.
     */
    private boolean autoForeground = true;

    /**
     * <code>true</code> if the color bar should be drawn horizontally,
     * or <code>false</code> if it should be drawn vertically.
     */
    private boolean horizontal = true;

    /**
     * Rendering hints for the graduation. This include the color bar
     * length, which is used for the space between ticks.
     */
    private transient RenderingHints hints;

    /**
     * The tick iterator used during the last painting.
     * This iterator will be reused as mush as possible
     * in order to reduce garbage-collections.
     */
    private transient TickIterator reuse;

    /**
     * A temporary buffer for conversions from RGB to HSB
     * values. This is used by {@link #getForeground(int)}.
     */
    private transient float[] HSB;

    /**
     * The {@link ComponentUI} object for computing preferred
     * size, drawn the component and handle some events.
     */
    private final UI ui = new UI();

    /**
     * Construct an initially empty color bar. Colors can be
     * set using one of the <code>setColors(...)</code> methods.
     */
    public ColorBar() {
        setOpaque(true);
        setUI(ui);
    }

    /**
     * Construct a color bar for the specified grid coverage.
     *
     * @param coverage The grid coverage.
     */
    public ColorBar(final GridCoverage coverage) {
        this();
        setColors(coverage);
    }

    /**
     * Returns the graduation to paint over colors. If the graduation is
     * not yet defined, then this method returns <code>null</code>.
     */
    public Graduation getGraduation() {
        return graduation;
    }

    /**
     * Set the graduation to paint on top of the color bar. The graduation can be set also
     * by a call to {@link #setColors(SampleDimension)} and {@link #setColors(GridCoverage)}.
     * This method will fire a property change event with the <code>"graduation"</code> name.
     *
     * @param  graduation The new graduation, or <code>null</code> if none.
     * @return <code>true</code> if this object changed as a result of this call.
     */
    public boolean setGraduation(final Graduation graduation) {
        final Graduation oldGraduation = this.graduation;
        if (graduation != oldGraduation) {
            if (oldGraduation != null) {
                oldGraduation.removePropertyChangeListener(ui);
            }
            if (graduation != null) {
                graduation.addPropertyChangeListener(ui);
            }
            this.graduation = graduation;
            units = null;
            if (graduation != null) {
                final Unit unit = graduation.getUnit();
                if (unit != null) {
                    units = unit.toString();
                }
            }
        }
        final boolean changed = !Utilities.equals(graduation, oldGraduation);
        if (changed) {
            repaint();
        }
        firePropertyChange("graduation", oldGraduation, graduation);
        return changed;
    }

    /**
     * Returns the colors painted by this <code>ColorBar</code>.
     * 
     * @return The colors (never <code>null</code>).
     */
    public Color[] getColors() {
        return (colors.length!=0) ? (Color[])colors.clone() : colors;
    }

    /**
     * Sets the colors to paint.
     * This method will fire a property change event with the <code>"colors"</code> name.
     *
     * @param colors The colors to paint.
     * @return <code>true</code> if the state of this <code>ColorBar</code>
     *         changed as a result of this call.
     *
     * @see #setColors(GridCoverage)
     * @see #setColors(SampleDimension)
     * @see #setColors(IndexColorModel)
     * @see #getColors()
     * @see #getGraduation()
     */
    public boolean setColors(final Color[] colors) {
        final Color[] oldColors = this.colors;
        this.colors = (colors!=null && colors.length!=0) ? (Color[])colors.clone() : EMPTY;
        final boolean changed = !Arrays.equals(oldColors, this.colors);
        if (changed) {
            repaint();
        }
        firePropertyChange("colors", oldColors, colors);
        return changed;
    }

    /**
     * Sets the colors to paint from an {@link IndexColorModel}.
     *
     * @param model The colors to paint.
     * @return <code>true</code> if the state of this <code>ColorBar</code>
     *         changed as a result of this call.
     *
     * @see #setColors(GridCoverage)
     * @see #setColors(SampleDimension)
     * @see #setColors(Color[])
     * @see #getColors()
     * @see #getGraduation()
     */
    public boolean setColors(final IndexColorModel model) {
        return (model!=null) ? setColors(model, 0, model.getMapSize()) : setColors(EMPTY);
    }

    /**
     * Sets the colors to paint from an {@link IndexColorModel}. Only indexed colors in the
     * range <code>lower</code> inclusive to <code>upper</code> exclusive will be painted.
     *
     * @param model The colors to paint.
     * @param lower First color index to paint, inclusive.
     * @param upper Last  color index to paint, exclusive.
     * @return <code>true</code> if the state of this <code>ColorBar</code>
     *         changed as a result of this call.
     *
     * @see #setColors(GridCoverage)
     * @see #setColors(SampleDimension)
     * @see #setColors(Color[])
     * @see #getColors()
     * @see #getGraduation()
     */
    private boolean setColors(final IndexColorModel model, final int lower, final int upper) {
        if (model == null) {
            return setColors(EMPTY);
        }
        final Color[] colors = new Color[upper-lower];
        for (int i=0; i<colors.length; i++) {
            final int j = i+lower;
            colors[i] = new Color(model.getRed  (j),
                                  model.getGreen(j),
                                  model.getBlue (j),
                                  model.getAlpha(j));
        }
        return setColors(colors);
    }

    /**
     * Sets the colors to paint. The range of indexed colors and the
     * minimum and maximum values are fetched from the supplied band.
     *
     * @param  band The band.
     * @param  colors The colors to paint.
     * @return <code>true</code> if the state of this <code>ColorBar</code>
     *         changed as a result of this call.
     *
     * @see #setColors(GridCoverage)
     * @see #setColors(SampleDimension)
     * @see #setColors(IndexColorModel)
     * @see #setColors(Color[])
     * @see #getColors()
     * @see #getGraduation()
     */
    private boolean setColors(final SampleDimension band, final IndexColorModel colors) {
        /*
         * Looks for what seems to be the "main" category. We look for the
         * quantitative category (if there is one) with the widest sample range.
         */
        double maxRange = 0;
        Category category = null;
        final List categories = band.getCategories();
        for (int i=categories.size(); --i>=0;) {
            final Category candidate = ((Category) categories.get(i)).geophysics(false);
            if (candidate!=null && candidate.isQuantitative()) {
                final NumberRange range = candidate.getRange();
                final double rangeValue = range.getMaximum() - range.getMinimum();
                if (rangeValue >= maxRange) {
                    maxRange = rangeValue;
                    category = candidate;
                }
            }
        }
        if (category == null) {
            return setGraduation(null) | setColors(EMPTY); // Realy |, not ||
        }
        /*
         * Now that we know what seems to be the "main" category,
         * construct a graduation for it.
         */
        final NumberRange range = category.getRange();
        final int lower = ((Number)range.getMinValue()).intValue();
        final int upper = ((Number)range.getMaxValue()).intValue();
        double min,max;
        try {
            final MathTransform1D tr = category.getSampleToGeophysics();
            min = tr.transform(lower);
            max = tr.transform(upper);
        } catch (TransformException cause) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "category", category));
            e.initCause(cause);
            throw e;
        }
        if (min > max) {
            // This case occurs typically when displaying a color ramp for
            // sea bathymetry, for which floor level are negative numbers.
            min = -min;
            max = -max;
        }
        if (!(min <= max)) {
            throw new IllegalStateException(Resources.format(ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                                                             "category", category));
        }
        AbstractGraduation graduation = (this.graduation instanceof AbstractGraduation) ?
                                        (AbstractGraduation) this.graduation : null;
        graduation = createGraduation(graduation, category, band.getUnits());
        graduation.setMinimum(min);
        graduation.setMaximum(max);
        return setGraduation(graduation) | setColors(colors, lower, upper); // Realy |, not ||
    }

    /**
     * Sets the graduation and the colors to paint from a {@link SampleDimension}.
     * The range of indexed colors and the minimum and maximum values are fetched
     * from the supplied band.
     *
     * @param band The band, or <code>null</code>.
     * @return <code>true</code> if the state of this <code>ColorBar</code>
     *         changed as a result of this call.
     *
     * @see #setColors(GridCoverage)
     * @see #setColors(IndexColorModel)
     * @see #setColors(Color[])
     * @see #getColors()
     * @see #getGraduation()
     */
    public boolean setColors(SampleDimension band) {
        if (band == null) {
            return setGraduation(null) | setColors(EMPTY); // Really |, not ||
        }
        band = band.geophysics(false);
        final ColorModel colors = band.getColorModel();
        if (colors instanceof IndexColorModel) {
            return setColors(band, (IndexColorModel) colors);
        } else {
            throw new UnsupportedOperationException("Only IndexColorModel are currently supported");
        }
    }

    /**
     * Sets the graduation and the colors to paint from a {@link GridCoverage}.
     * The range of indexed colors and the minimum and maximum values are fetched
     * from the supplied grid coverage.
     *
     * @param coverage The grid coverage, or <code>null</code>.
     * @return <code>true</code> if the state of this <code>ColorBar</code>
     *         changed as a result of this call.
     *
     * @see #setColors(IndexColorModel)
     * @see #setColors(SampleDimension)
     * @see #getColors()
     * @see #getGraduation()
     */
    public boolean setColors(GridCoverage coverage) {
        SampleDimension band = null;
        if (coverage != null) {
            coverage = coverage.geophysics(false);
            final RenderedImage image = coverage.getRenderedImage();
            band = coverage.getSampleDimensions()[GCSUtilities.getVisibleBand(image)];
            final ColorModel colors = image.getColorModel();
            if (colors instanceof IndexColorModel) {
                return setColors(band, (IndexColorModel) colors);
            }
        }
        return setColors(band);
    }

    /**
     * Returns the component's orientation (horizontal or vertical).
     * It should be one of the following constants:
     * ({@link SwingConstants#HORIZONTAL} or {@link SwingConstants#VERTICAL}).
     */
    public int getOrientation() {
        return (horizontal) ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL;
    }

    /**
     * Set the component's orientation (horizontal or vertical).
     *
     * @param orient {@link SwingConstants#HORIZONTAL}
     *        or {@link SwingConstants#VERTICAL}.
     */
    public void setOrientation(final int orient) {
        switch (orient) {
            case SwingConstants.HORIZONTAL: horizontal=true;  break;
            case SwingConstants.VERTICAL:   horizontal=false; break;
            default: throw new IllegalArgumentException(String.valueOf(orient));
        }
    }

    /**
     * Tests if graduation labels are paint on top of the
     * colors ramp. Default value is <code>true</code>.
     */
    public boolean isLabelVisibles() {
        return labelVisibles;
    }

    /**
     * Sets whatever the graduation labels should
     * be painted on top of the colors ramp.
     */
    public void setLabelVisibles(final boolean visible) {
        labelVisibles = visible;
    }

    /**
     * Sets the label colors. A <code>null</code>
     * value reset the automatic color.
     *
     * @see #getForeground
     */
    public void setForeground(final Color color) {
        super.setForeground(color);
        autoForeground = (color==null);
    }

    /**
     * Returns a color for label at the specified index.
     * The default color will be black or white, depending
     * of the background color at the specified index.
     */
    private Color getForeground(final int colorIndex) {
        final Color color = colors[colorIndex];
        HSB = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), HSB);
        return (HSB[2]>=0.5f) ? Color.black : Color.white;
    }

    /**
     * Paint the color ramp. This method doesn't need to restore
     * {@link Graphics2D} to its initial state once finished.
     *
     * @param  graphics The graphic context in which to paint.
     * @param  bounds   The bounding box where to paint the color ramp.
     * @return Bounding box of graduation labels (NOT takind in account
     *         the color ramp in behind them), or <code>null</code> if
     *         no label has been painted.
     */
    private Rectangle2D paint(final Graphics2D graphics, final Rectangle bounds) {
        final int length = colors.length;
        if (length != 0) {
            int i=0, lastIndex=0;
            Color color=colors[i];
            Color nextColor=color;
            int R,G,B;
            int nR = R = color.getRed  ();
            int nG = G = color.getGreen();
            int nB = B = color.getBlue ();
            final int    ox = bounds.x + MARGIN;
            final int    oy = bounds.y + bounds.height - MARGIN;
            final double dx = (double)(bounds.width -2*MARGIN)/length;
            final double dy = (double)(bounds.height-2*MARGIN)/length;
            final Rectangle2D.Double rect = new Rectangle2D.Double();
            rect.setRect(bounds);
            while (++i <= length) {
                if (i != length) {
                    nextColor = colors[i];
                    nR = nextColor.getRed  ();
                    nG = nextColor.getGreen();
                    nB = nextColor.getBlue ();
                    if (R==nR && G==nG && B==nB) {
                        continue;
                    }
                }
                if (horizontal) {
                    rect.x      = ox+dx*lastIndex;
                    rect.width  = dx*(i-lastIndex);
                    if (lastIndex == 0) {
                        rect.x     -= MARGIN;
                        rect.width += MARGIN;
                    }
                    if (i == length) {
                        rect.width += MARGIN;
                    }
                } else {
                    rect.y      = oy-dy*i;
                    rect.height = dy*(i-lastIndex);
                    if (lastIndex == 0) {
                        rect.height += MARGIN;
                    }
                    if (i == length) {
                        rect.y      -= MARGIN;
                        rect.height += MARGIN;
                    }
                }
                graphics.setColor(color);
                graphics.fill(rect);
                lastIndex = i;
                color = nextColor;
                R = nR;
                G = nG;
                B = nB;
            }
        }
        Rectangle2D labelBounds=null;
        if (labelVisibles && graduation!=null) {
            /*
             * Prépare l'écriture de la graduation. On vérifie quelle longueur
             * (en pixels) a la rampe de couleurs et on calcule les coéfficients
             * qui permettront de convertir les valeurs logiques en coordonnées
             * pixels.
             */
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            final double axisRange   = graduation.getRange();
            final double axisMinimum = graduation.getMinimum();
            final double visualLength, scale, offset;
            if (horizontal) {
                visualLength = bounds.getWidth() - 2*MARGIN;
                scale        = visualLength/axisRange;
                offset       = (bounds.getMinX()+MARGIN) - scale*axisMinimum;
            } else {
                visualLength = bounds.getHeight() - 2*MARGIN;
                scale        = -visualLength/axisRange;
                offset       = (bounds.getMaxY()-MARGIN) + scale*axisMinimum;
            }
            if (hints==null)          hints = new RenderingHints(null);
            final RenderingHints      hints = this.hints;
            final double              ratio = length/axisRange;
            final Font                 font = getFont();
            final FontRenderContext context = graphics.getFontRenderContext();
            hints.put(Graduation.VISUAL_AXIS_LENGTH, new Float((float)visualLength));
            graphics.setColor(getForeground());
            /*
             * Procède à l'écriture de la graduation.
             */
            for (final TickIterator ticks = reuse = graduation.getTickIterator(hints, reuse);
                                                    ticks.hasNext(); ticks.nextMajor())
            {
                if (ticks.isMajorTick()) {
                    final GlyphVector glyph = font.createGlyphVector(context, ticks.currentLabel());
                    final Rectangle2D rectg = glyph.getVisualBounds();
                    final double      width = rectg.getWidth();
                    final double     height = rectg.getHeight();
                    final double      value = ticks.currentPosition();
                    final double   position = value*scale+offset;
                    final int    colorIndex = Math.min(Math.max((int)Math.round(
                                              (value-axisMinimum)*ratio),0), length-1);
                    if (horizontal) x=position;
                    else            y=position;
                    rectg.setRect(x-0.5*width, y-0.5*height, width, height);
                    if (autoForeground) {
                        graphics.setColor(getForeground(colorIndex));
                    }
                    graphics.drawGlyphVector(glyph, (float)rectg.getMinX(), (float)rectg.getMaxY());
                    if (labelBounds != null) {
                        labelBounds.add(rectg);
                    } else {
                        labelBounds = rectg;
                    }
                }
            }
            /*
             * Ecrit les unités.
             */
            if (units != null) {
                final GlyphVector glyph = font.createGlyphVector(context, units);
                final Rectangle2D rectg = glyph.getVisualBounds();
                final double      width = rectg.getWidth();
                final double     height = rectg.getHeight();
                if (horizontal) {
                    double left = bounds.getMaxX()-width;
                    if (labelBounds != null) {
                        final double check = labelBounds.getMaxX()+4;
                        if (check<left) {
                            left = check;
                        }
                    }
                    rectg.setRect(left, y-0.5*height, width, height);
                } else {
                    rectg.setRect(x-0.5*width, bounds.getMinY()+height, width, height);
                }
                if (autoForeground) {
                    graphics.setColor(getForeground(length-1));
                }
                if (labelBounds==null || !labelBounds.intersects(rectg)) {
                    graphics.drawGlyphVector(glyph, (float)rectg.getMinX(), (float)rectg.getMaxY());
                }
            }
        }
        return labelBounds;
    }

    /**
     * Returns a graduation for the specified category. This method must returns
     * a graduation of the appropriate class (e.g. {@link NumberGraduation} or
     * {@link LogarithmicNumberGraduation}), but doesn't have to set any graduation's
     * properties like minimum and maximum values. This will be handle by the caller.
     * <br><br>
     * If the supplied <code>reuse</code> object is non-null and is of the appropriate
     * class, then this method can returns <code>reuse</code> without creating a new
     * graduation. This help to reduce garbage collection.
     *
     * @param  reuse The graduation to reuse if possible.
     * @param  category The category to create graduation for.
     * @param  units The units for the graduation.
     * @return A graduation for the supplied category. The minimum, maximum
     *         and units doesn't need to bet set at this stage.
     */
    protected AbstractGraduation createGraduation(final AbstractGraduation reuse,
                                                  final Category category, final Unit units)
    {
        MathTransform1D tr = category.geophysics(false).getSampleToGeophysics();
        boolean linear      = false;
        boolean logarithmic = false;
        try {
            /*
             * An heuristic approach to determine if the transform is linear or logarithmic.
             * We look at the derivative, which should be constant everywhere for a linear
             * scale and be proportional to the inverse of 'x' for a logarithmic one.
             */
            tr = (MathTransform1D) tr.inverse();
            final double     EPS = 1E-6; // For rounding error.
            final NumberRange range = category.geophysics(true).getRange();
            final double minimum = range.getMinimum();
            final double maximum = range.getMaximum();
            final double ratio   = tr.derivative(minimum) / tr.derivative(maximum);
            if (Math.abs(ratio-1) <= EPS) {
                linear = true;
            }
            if (Math.abs(ratio*(minimum/maximum) - 1) <= EPS) {
                logarithmic = true;
            }
        } catch (TransformException exception) {
            // Transformation failed. We don't know if the scale is linear or logarithmic.
            // Continue anyway...
        }
        if (linear) {
            if (reuse==null || !reuse.getClass().equals(NumberGraduation.class)) {
                return new NumberGraduation(units);
            }
        } else if (logarithmic) {
            if (reuse==null || !reuse.getClass().equals(LogarithmicNumberGraduation.class)) {
                return new LogarithmicNumberGraduation(units);
            }
        } else {
            // TODO: Should we localize this message? (it should not occurs often)
            Logger.getLogger("org.geotools.gui.swing").warning("Unknow scale type: \""+
                             Utilities.getShortClassName(tr)+"\". Default to linear.");
            return new NumberGraduation(units);
        }
        return reuse;
    }

    /**
     * Returns a string representation for this color ramp.
     */
    public String toString() {
        int count=0;
        int i = 0;
        if (i < colors.length) {
            Color last = colors[i];
            while (++i < colors.length) {
                Color c = colors[i];
                if (!c.equals(last)) {
                    last = c;
                    count++;
                }
            }
        }
        return Utilities.getShortClassName(this)+'['+count+" colors]";
    }

    /**
     * Notifies this component that it now has a parent component.
     * This method is invoked by <em>Swing</em> and shouldn't be
     * directly used.
     */
    public void addNotify() {
        super.addNotify();
        if (graduation != null) {
            graduation.removePropertyChangeListener(ui); // Avoid duplication
            graduation.addPropertyChangeListener(ui);
        }
    }

    /**
     * Notifies this component that it no longer has a parent component.
     * This method is invoked by <em>Swing</em> and shouldn't be directly used.
     */
    public void removeNotify() {
        if (graduation != null) {
            graduation.removePropertyChangeListener(ui);
        }
        super.removeNotify();
    }





    /**
     * Classe ayant la charge de dessiner la rampe de couleurs, ainsi que
     * de calculer l'espace qu'elle occupe. Cette classe peut aussi réagir
     * à certains événements.
     *
     * @version $Id: ColorBar.java,v 1.9 2004/02/13 14:29:37 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class UI extends ComponentUI implements PropertyChangeListener {
        /**
         * Retourne la dimension minimale de cette rampe de couleurs.
         */
        public Dimension getMinimumSize(final JComponent c) {
            return (((ColorBar) c).horizontal) ? new Dimension(2*MARGIN,16)
                                               : new Dimension(16,2*MARGIN);
        }

        /**
         * Retourne la dimension préférée de cette rampe de couleurs.
         */
        public Dimension getPreferredSize(final JComponent c) {
            return (((ColorBar) c).horizontal) ? new Dimension(256,16)
                                               : new Dimension(16,256);
        }

        /**
         * Dessine la rampe de couleurs vers le graphique spécifié.  Cette méthode a
         * l'avantage d'être appelée automatiquement par <i>Swing</i> avec une copie
         * d'un objet {@link Graphics}, ce qui nous évite d'avoir à le remettre dans
         * son état initial lorsqu'on a terminé le traçage de la rampe de couleurs.
         * On n'a pas cet avantage lorsque l'on ne fait que redéfinir
         * {@link JComponent#paintComponent}.
         */
        public void paint(final Graphics graphics, final JComponent component) {
            final ColorBar ramp = (ColorBar) component;
            if (ramp.colors != null) {
                final Rectangle bounds=ramp.getBounds();
                bounds.x = 0;
                bounds.y = 0;
                ramp.paint((Graphics2D) graphics, bounds);
            }
        }

        /**
         * Méthode appelée automatiquement chaque
         * fois qu'une propriété de l'axe a changée.
         */
        public void propertyChange(final PropertyChangeEvent event) {
            repaint();
        }
    }
}
