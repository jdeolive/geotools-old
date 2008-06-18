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
package org.geotools.display.renderer;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import javax.measure.unit.Unit;

import org.geotools.resources.i18n.Loggings;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * Statistics about rendering performance. Those statistics are filled while a
 * painting is in process. They are used for logging messages and have no impact
 * on future rendering.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 */
public final class RenderingStatistics {
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
     * {@code true} if the statistics are loggable.
     */
    private boolean loggable;

    /**
     * The starting time in milliseconds ellapsed since January 1st, 1971.
     */
    private long startTime;

    /**
     * The ellapsed time, or 0 if the rendering is not yet finished.
     */
    private long ellapsedTime;

    /**
     * The number of points recomputed, rendered and the total number of points while painting
     * a geometry.
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
    private Unit units;

    /**
     * The logger to use.
     */
    private final Logger logger;

    /**
     * Creates initially empty rendering statistics.
     */
    public RenderingStatistics(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Initializes the statistics. Invoked by {@link BufferedCanvas2D#paint} only.
     */
    final void init() {
        startTime       = System.currentTimeMillis();
        ellapsedTime    = 0;
        recomputed      = 0;
        rendered        = 0;
        total           = 0;
        resolution      = 0;
        resolutionScale = 1;
        units           = Unit.ONE;
        loggable        = logger.isLoggable(LEVEL);
    }

    /**
     * Sets the multiplication factor for the {@code resolution} argument in calls to
     * {@link #addGeometry}. This is used when the resolution is provided in degrees:
     * this scale will then be applied for transforming the resolution from degrees to
     * meters. Of course, this is only a very approximative transformation, since real
     * transformations are not linears.
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
     * Declares that a rendering is finished. This method update the statistics and logs
     * a message with the specified level. Invoked by {@link BufferedCanvas2D#paint} only.
     *
     * @param canvas The caller.
     */
    final void finish(final AbstractRenderer renderer) {
        ellapsedTime = System.currentTimeMillis() - startTime;
        if (isLoggable() && ellapsedTime>=TIME_THRESHOLD) {
            logger.log(createLogRecord(renderer));
        }
    }

    /**
     * Creates a log record for the specified canvas.
     *
     * @param canvas The caller, or {@code null} if unknown.
     */
    private LogRecord createLogRecord(final AbstractRenderer renderer) {
        final Double ellapsed = new Double((ellapsedTime!=0 ? ellapsedTime :
                                            System.currentTimeMillis() - startTime) / 1000);
        final Locale locale;
        final String title;
        if (renderer != null) {
            locale = renderer.getLocale();
            title  = renderer.getCanvas().getState().getTitle().toString();
        } else {
            locale = Locale.getDefault();
            title  = Vocabulary.format(VocabularyKeys.UNKNOW);
        }
        final Loggings resources = Loggings.getResources(locale);
        final LogRecord  record;
        if (total==0 || rendered==0) {
            record = resources.getLogRecord(LEVEL, LoggingKeys.PAINTING_LAYER_$2, title, ellapsed);
        } else {
            record = new LogRecord(LEVEL,
                     resources.getString(LoggingKeys.PAINTING_LAYER_$2, title, ellapsed) +
                     System.getProperty("line.separator", "\n") +
                     resources.getString(LoggingKeys.POLYGON_CACHE_USE_$4,
                          new Double((double)rendered/(double)total),
                          new Double((double)(rendered-recomputed)/(double)rendered),
                          new Double(resolution/rendered), units));
        }
        record.setSourceClassName(renderer.getClass().getName());
        record.setSourceMethodName("paint");
        return record;
    }

    /**
     * Returns {@code true} if the statistics are loggable.
     */
    final boolean isLoggable() {
        return loggable;
    }

    /**
     * Returns a string representation of this set of statistics.
     */
    public String toString() {
        return createLogRecord(null).getMessage();
    }
}
