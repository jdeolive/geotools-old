/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// Logging
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

// Preferences
import java.util.prefs.Preferences;

// Writer
import java.io.IOException;
import java.io.StringWriter;
import org.geotools.io.LineWriter;


/**
 * A formatter writting log message on a single line. This formatter is used by Geotools 2
 * instead of {@link SimpleFormatter}. The main difference is that this formatter use only
 * one line per message instead of two. For example, a message formatted by
 * <code>MonolineFormatter</code> looks like:
 *
 * <blockquote><pre>
 * [FINE core] A log message logged with level FINE from the "org.geotools.core" logger.
 * </pre></blockquote>
 *
 * @version $Id: MonolineFormatter.java,v 1.1 2002/08/14 16:13:30 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MonolineFormatter extends Formatter {
    /**
     * The string to write at the begining of every log header (e.g. "[FINE core]").
     */
    private static final String PREFIX = "[";

    /**
     * The string to write at the end of every log header (e.g. "[FINE core]").
     * It should includes the spaces between the header and the message body.
     */
    private static final String SUFFIX = "] ";

    /**
     * The defautl header width.
     */
    private static final int DEFAULT_WIDTH = 15;

    /**
     * The line separator. This is the value of the "line.separator"
     * property at the time the <code>MonolineFormatter</code> was created.
     */
    private final String lineSeparator = System.getProperty("line.separator", "\n");

    /**
     * The minimum amount of spaces to use for writting level and module name before the message.
     * For example if this value is 12, then a message from module "org.geotools.core" with level
     * FINE would be formatted as "<code>[FINE&nbsp;&nbsp;core]</code> <cite>the message</cite>"
     * (i.e. the whole <code>[&nbsp;]</code> part is 12 characters wide).
     */
    private final int margin;

    /**
     * The base logger name. This is used for shortening the logger name when formatting
     * message. For example, if the base logger name is "org.geotools" and a log record
     * come from the "org.geotools.core" logger, it will be formatted as "[LEVEL core]"
     * (i.e. the "org.geotools" part is ommited).
     */
    private final String base;

    /**
     * Buffer for formatting messages. We will reuse this
     * buffer in order to reduce memory allocations.
     */
    private final StringBuffer buffer;

    /**
     * The line writer. This object transform all "\r", "\n" or "\r\n" occurences
     * into a single line separator. This line separator will include space for
     * the marging, if needed.
     */
    private final LineWriter writer;

    /**
     * Construct a default instance of <code>MonolineFormatter</code>.
     * Base logger name is <code>"org.geotools"</code> (since this
     * logger is designed for use with Geotools 2).
     */
    public MonolineFormatter() {
        this("org.geotools", getHeaderWidth());
    }

    /**
     * Construct a <code>MonolineFormatter</code>.
     *
     * @param base   The base logger name. This is used for shortening the logger name
     *               when formatting message. For example, if the base logger name is
     *               "org.geotools" and a log record come from the "org.geotools.core"
     *               logger, it will be formatted as "[LEVEL core]" (i.e. the
     *               "org.geotools" part is ommited).
     * @param margin The minimum amount of spaces to use for writting level and module
     *               name before the message. For example if this value is 12, then a
     *               message from module "org.geotools.core" with level FINE would be
     *               formatted as "<code>[FINE&nbsp;&nbsp;core]</code> <cite>the message</cite>"
     *               (i.e. the whole <code>[&nbsp;]</code> part is 12 characters wide).
     */
    public MonolineFormatter(final String base, final int margin) {
        this.base   = base.trim();
        this.margin = margin;
        final StringWriter str = new StringWriter();
        writer = new LineWriter(str);
        buffer = str.getBuffer();
        buffer.append(PREFIX);
    }

    /**
     * Format the given log record and return the formatted string.
     *
     * @param  record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(final LogRecord record) {
        String logger = record.getLoggerName();
        if (logger.startsWith(base)) {
            logger = logger.substring(base.length());
        }
        String bodyLineSeparator = writer.getLineSeparator();
        try {
            buffer.setLength(PREFIX.length());
            writer.setLineSeparator(lineSeparator);
            writer.write(record.getLevel().getLocalizedName());
            writer.write(Utilities.spaces(Math.max(1, margin-(buffer.length()+logger.length()+1))));
            writer.write(logger);
            writer.write(SUFFIX);
            /*
             * Now format the message. We will use a line separator made of the usual EOL
             * ("\r", "\n", or "\r\n", which is plateform specific) following by some amout
             * of space in order to align message body.
             */
            final int margin  = buffer.length();
            assert margin >= this.margin;
            if (bodyLineSeparator.length() != lineSeparator.length()+margin) {
                bodyLineSeparator = lineSeparator + Utilities.spaces(margin);
            }
            writer.setLineSeparator(bodyLineSeparator);
            writer.write(formatMessage(record));
            writer.write('\n');
            writer.flush();
        } catch (IOException exception) {
            // Should never happen, since we are writting into a StringBuffer.
            throw new AssertionError(exception);
        }
        return buffer.toString();
    }

    /**
     * Setup a <code>MonolineFormatter</code> for the specified logger and its children.
     * This method search for all instances of {@link ConsoleHandler} using the {@link
     * SimpleFormatter}. If such instances are found, they are replaced by a single
     * instance of <code>MonolineFormatter</code> writting to the standard output stream
     * (instead of the standard error stream). This action has no effect on any loggers
     * outside the <code>base</code> namespace.
     *
     * @param base The base logger name to apply the change on (e.g. "org.geotools").
     */
    public static void init(final String base) {
        Formatter formatter = null;
        final Logger logger = Logger.getLogger(base);
        for (Logger parent=logger; parent.getUseParentHandlers();) {
            parent = parent.getParent();
            if (parent == null) {
                break;
            }
            final Handler[] handlers = parent.getHandlers();
            for (int i=0; i<handlers.length; i++) {
                Handler handler = handlers[i];
                if (formatter == null) {
                    // Check only if no formatter has been set yet.
                    if (handler.getClass().equals(ConsoleHandler.class)) {
                        formatter = handler.getFormatter();
                        if (formatter.getClass().equals(SimpleFormatter.class)) {
                            formatter = new MonolineFormatter(base, getHeaderWidth());
                            handler = new Stdout(formatter);
                        }
                    }
                }
                logger.addHandler(handler);
            }
        }
        logger.setUseParentHandlers(false);
    }

    /**
     * Returns the header width. This is the default value to use for {@link #margin},
     * if no value has been explicitely set. This value can be set in user's preferences.
     */
    private static int getHeaderWidth() {
        return Preferences.userNodeForPackage(Arguments.class).getInt("logging.header", 15);
    }

    /**
     * A {@link ConsoleHandler} sending output to {@link System#out}
     * instead of {@link System#err}.
     */
    private static final class Stdout extends ConsoleHandler {
        /**
         * Construct the console handler.
         *
         * @param  formatter The formatter to use.
         * @throws SecurityException if we don't have the
         *         permission to set the output stream.
         */
        public Stdout(final Formatter formatter) throws SecurityException {
            setOutputStream(System.out);
            setFormatter(formatter);
        }
    }
}
