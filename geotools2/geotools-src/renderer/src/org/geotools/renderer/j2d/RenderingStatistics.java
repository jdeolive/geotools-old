/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.renderer.geom.Geometry; // For Javadoc


/**
 * Statistics about rendering performance. Those statistics are filled while a
 * painting is in process. They are used for logging messages and have no impact
 * on future rendering.
 *
 * @version $Id: RenderingStatistics.java,v 1.9 2003/05/30 18:20:54 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class RenderingStatistics {
    /**
     * Minimum amout of milliseconds during rendering before logging a message.
     * A message will be logged only if rendering take longer, which is usefull
     * for tracking down performance bottleneck. Set this value to 0 for logging
     * all paint events.
     */
    private static final int TIME_THRESHOLD = 250;

    /**
     * The level for logging statistics.
     */
    private static final Level LEVEL = Level.FINE;

    /**
     * <code>true</code> if the statistics are loggable.
     */
    private boolean loggable;

    /**
     * While a rendering is in process, the starting time in milliseconds ellapsed
     * since January 1st, 1971. Once the rendering is finished, the ellapsed time.
     */
    private long time;

    /**
     * The number of points recomputed, rendered and the total number of points while painting
     * an {@link Geometry}. Those informations are updated only by {@link RenderedGeometries}.
     */
    private int recomputed, rendered, total;

    /**
     * The mean resolution of rendered polygons.
     */
    private double resolution;

    /**
     * A correction factor for the {@link #resolution} value, or 1 is none.
     * Used when the resolution is provided in degrees: this scale will then
     * be applied for transforming the resolution from degrees to meters. Of
     * course, this is only a very approximative transformation, since real
     * transformations are not linears.
     */
    private double resolutionScale = 1;

    /**
     * The resolution units.
     */
    private Unit units = Unit.DIMENSIONLESS;

    /**
     * Default constructor.
     */
    public RenderingStatistics() {
    }

    /**
     * Initialize the statistics. Invoked by {@link Renderer#paint} only.
     */
    final void init() {
        loggable        = Renderer.LOGGER.isLoggable(LEVEL);
        time            = System.currentTimeMillis();
        recomputed      = rendered = total = 0;
        units           = Unit.DIMENSIONLESS;
        resolution      = 0;
        resolutionScale = 1;
    }

    /**
     * Set the multiplication factor for the <code>resolution</code> argument in calls to
     * {@link #addGeometry}. This is used when the resolution is provided in degrees: this
     * scale will then be applied for transforming the resolution from degrees to meters.
     * Of course, this is only a very approximative transformation, since real transformations
     * are not linears.
     */
    final void setResolutionScale(final double scale, final Unit units) {
        resolutionScale = scale;
        this.units      = units;
    }

    /**
     * Update statistics about the rendering of a geometry.
     *
     * @param total The total number of points in rendered polygons.
     * @param rendered The total number of <em>rendered</em> points
     *        (i.e. taking decimation in account).
     * @param recomputed The number of points that has been recomputed
     *        (i.e. decompressed, decimated, projected and transformed).
     * @param resolution The mean resolution of rendered polygons.
     */
    final void addGeometry(final int total, final int rendered, final int recomputed,
                            double resolution)
    {
        this.total      += total;
        this.rendered   += rendered;
        this.recomputed += recomputed;
        resolution *= rendered;
        if (!Double.isNaN(resolution)) {
            this.resolution += resolution*resolutionScale;
        }
    }

    /**
     * Declare that a rendering is finished. This method update the statistics and logs
     * a message with the specified level. Invoked by {@link Renderer#paint} only.
     *
     * @param renderer The caller.
     */
    final void finish(final Renderer renderer) {
        resolution /= rendered;
        time = System.currentTimeMillis() - time;
        if (isLoggable() && time>=TIME_THRESHOLD) {
            final Locale       locale = renderer.getLocale();
            final Resources resources = Resources.getResources(locale);
            final String         name = renderer.getName(locale);
            final Double         time = new Double(this.time/1000.0);
            final LogRecord    record;
            if (total==0 || rendered==0) {
                record = resources.getLogRecord(LEVEL, ResourceKeys.PAINTING_$2, name, time);
            } else {
                record = new LogRecord(LEVEL,
                         resources.getString(ResourceKeys.PAINTING_$2, name, time) +
                         System.getProperty("line.separator", "\n") +
                         resources.getString(ResourceKeys.POLYGON_CACHE_USE_$4,
                              new Double((double)rendered/(double)total),
                              new Double((double)(rendered-recomputed)/(double)rendered),
                              new Double(resolution), units));
            }
            record.setSourceClassName(Utilities.getShortClassName(renderer));
            record.setSourceMethodName("paint");
            Renderer.LOGGER.log(record);
        }
    }

    /**
     * Returns <code>true</code> if the statistics are loggable.
     */
    final boolean isLoggable() {
        return loggable;
    }
}
