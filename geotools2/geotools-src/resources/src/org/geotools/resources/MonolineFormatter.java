/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.StreamHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

// Formatting
import java.util.Date;
import java.util.TimeZone;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;

// Writer
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import org.geotools.io.LineWriter;


/**
 * A formatter writting log messages on a single line. This formatter is used by
 * Geotools 2 instead of {@link SimpleFormatter}. The main difference is that
 * this formatter use only one line per message instead of two. For example, a
 * message formatted by <code>MonolineFormatter</code> looks like:
 *
 * <blockquote><pre>
 * FINE core - A log message logged with level FINE from the "org.geotools.core" logger.
 * </pre></blockquote>
 *
 * By default, <code>MonolineFormatter</code> display only the level and the
 * message.  Additional fields can be formatted if {@link #setTimeFormat} or
 * {@link #setSourceFormat} methods are invoked with a non-null argument. The
 * format can also be set from the <code>jre/lib/logging.properties</code>
 * file. For example, user can cut and paste the following properties into
 * <code>logging.properties</code>:
 *
 * <blockquote><pre>
 * ############################################################
 * # Properties for the Geotools's MonolineFormatter.
 * # By default, the monoline formatter display only the level
 * # and the message. Additional fields can be specified here:
 * #
 * #   time:  If set, writes the time ellapsed since the initialization.
 * #          The argument specifies the output pattern. For example, the
 * #          pattern HH:mm:ss.SSSS display the hours, minutes, seconds
 * #          and milliseconds.
 * #
 * #  source: If set, writes the source logger or the source class name.
 * #          The argument specifies the type of source to display. Valid
 * #          values are none, logger:short, logger:long, class:short and
 * #          class:long.
 * ############################################################
 * org.geotools.resources.MonolineFormatter.time = HH:mm:ss.SSS
 * org.geotools.resources.MonolineFormatter.source = class:short
 * </pre></blockquote>
 *
 * If the <code>MonolineFormatter</code> is wanted for the whole system
 * (not just the <code>org.geotools</code> packages) with level FINE (for
 * example), then the following properties can be defined as below:
 *
 * <blockquote><pre>
 * java.util.logging.ConsoleHandler.formatter = org.geotools.resources.MonolineFormatter
 * java.util.logging.ConsoleHandler.encoding = Cp850
 * java.util.logging.ConsoleHandler.level = FINE
 * </pre></blockquote>
 *
 * @version $Id: MonolineFormatter.java,v 1.14 2003/07/24 14:11:20 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MonolineFormatter extends Formatter {
    /**
     * The string to write at the begining of all log headers (e.g. "[FINE core]")
     */
    private static final String PREFIX = "";

    /**
     * The string to write at the end of every log header (e.g. "[FINE core]").
     * It should includes the spaces between the header and the message body.
     */
    private static final String SUFFIX = " - ";

    /**
     * The default header width.
     */
    private static final int DEFAULT_WIDTH = 9;

    /** Do not format source class name.       */ private static final int NO_SOURCE    = 0;
    /** Explicit value for 'none'.             */ private static final int NO_SOURCE_EX = 1;
    /** Format the source logger without base. */ private static final int LOGGER_SHORT = 2;
    /** Format the source logger only.         */ private static final int LOGGER_LONG  = 3;
    /** Format the class name without package. */ private static final int CLASS_SHORT  = 4;
    /** Format the fully qualified class name. */ private static final int CLASS_LONG   = 5;

    /**
     * The label to use in the <code>logging.properties</code> for setting the source format.
     */
    private static String[] FORMAT_LABELS = new String[6];
    static {
        FORMAT_LABELS[NO_SOURCE_EX] = "none";
        FORMAT_LABELS[LOGGER_SHORT] = "logger:short";
        FORMAT_LABELS[LOGGER_LONG ] = "logger:long";
        FORMAT_LABELS[ CLASS_SHORT] =  "class:short";
        FORMAT_LABELS[ CLASS_LONG ] =  "class:long";
    }

    /**
     * The line separator. This is the value of the "line.separator"
     * property at the time the <code>MonolineFormatter</code> was created.
     */
    private final String lineSeparator = System.getProperty("line.separator", "\n");

    /**
     * The line separator for the message body. This line always begin with
     * {@link #lineSeparator}, followed by some amount of spaces in order to
     * align the message.
     */
    private String bodyLineSeparator = lineSeparator;

    /**
     * The minimum amount of spaces to use for writting level and module name 
     * before the message.  For example if this value is 12, then a message from 
     * module "org.geotools.core" with level FINE would be formatted as 
     * "<code>[core&nbsp;&nbsp;FINE]</code> <cite>the message</cite>"
     * (i.e. the whole <code>[&nbsp;]</code> part is 12 characters wide).
     */
    private final int margin;

    /**
     * The base logger name. This is used for shortening the logger name when 
     * formatting message. For example, if the base logger name is "org.geotools" 
     * and a log record come from the "org.geotools.core" logger, it will be 
     * formatted as "[LEVEL core]" (i.e. the "org.geotools" part is ommited).
     */
    private final String base;

    /**
     * Time of <code>MonolineFormatter</code> creation,
     * in milliseconds ellapsed since January 1, 1970.
     */
    private final long startMillis;

    /**
     * The format to use for formatting ellapsed time,
     * or <code>null</code> if there is none.
     */
    private SimpleDateFormat timeFormat = null;

    /**
     * One of the following constants: {@link #NO_SOURCE},
     * {@link #LOGGER_SHORT}, {@link #LOGGER_LONG},
     * {@link #CLASS_SHORT} or {@link #CLASS_LONG}.
     */
    private int sourceFormat = NO_SOURCE;

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
     * Construct a default <code>MonolineFormatter</code>.
     */
    public MonolineFormatter() {
        this("");
    }

    /**
     * Construct a <code>MonolineFormatter</code>.
     *
     * @param base   The base logger name. This is used for shortening the logger 
     *               name when formatting message. For example, if the base 
     *               logger name is "org.geotools" and a log record come from 
     *               the "org.geotools.core" logger, it will be formatted as 
     *               "[LEVEL core]" (i.e. the "org.geotools" part is ommited).
     */
    public MonolineFormatter(final String base) {
        this.startMillis = System.currentTimeMillis();
        this.margin      = DEFAULT_WIDTH;
        this.base        = base.trim();
        StringWriter str = new StringWriter();
        writer = new LineWriter(str);
        buffer = str.getBuffer();
        buffer.append(PREFIX);

        // Configure this formatter
        final LogManager manager = LogManager.getLogManager();
	final String   classname = MonolineFormatter.class.getName();
        try {
            setTimeFormat(manager.getProperty(classname + ".time"));
        } catch (IllegalArgumentException exception) {
            // Can't use the logging framework, since we are configuring it.
            // Display the exception name only, not the trace.
            System.err.println(exception);
        }
        try {
            setSourceFormat(manager.getProperty(classname + ".source"));
        } catch (IllegalArgumentException exception) {
            System.err.println(exception);
        }
    }

    /**
     * Set the format for displaying ellapsed time. The pattern must matches
     * the format specified in {@link SimpleDateFormat}. For example, the
     * pattern <code>"HH:mm:ss.SSS"</code> will display the ellapsed time
     * in hours, minutes, seconds and milliseconds.
     *
     * @param pattern The time patter, or <code>null</code> to disable time
     *        formatting.
     */
    public synchronized void setTimeFormat(final String pattern) {
        if (pattern == null) {
            timeFormat = null;
        } else if (timeFormat == null) {
            timeFormat = new SimpleDateFormat(pattern);
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        } else {
            timeFormat.applyPattern(pattern);
        }
    }

    /**
     * Returns the format for displaying ellapsed time. This is the pattern specified
     * to the last call to {@link #setTimeFormat}, or the patten specified in the
     * <code>org.geotools.MonolineFormater.time</code> property in the
     * <code>jre/lib/logging.properties</code> file.
     *
     * @return The time pattern, or <code>null</code> if time is not formatted.
     */
    public synchronized String getTimeFormat() {
        return (timeFormat!=null) ? timeFormat.toPattern() : null;
    }

    /**
     * Set the format for displaying the source. The pattern may be one of the following:
     *
     * <code>"none"</code>,
     * <code>"logger:short"</code>,  <code>"class:short"</code>,
     * <code>"logger:long"</code> or <code>"class:long"</code>.
     *
     * The difference between a <code>null</code> and <code>"none"</code> is that <code>null</code>
     * may be replaced by a default value (see {@link Geotools#init()},   while <code>"none"</code>
     * means that the user explicitly requested no source.
     *
     * @param format The format for displaying the source, or <code>null</code>
     *        to disable source formatting.
     */
    public synchronized void setSourceFormat(String format) {
        if (format != null) {
            format = format.trim().toLowerCase();
        }
        for (int i=0; i<FORMAT_LABELS.length; i++) {
            if (Utilities.equals(FORMAT_LABELS[i], format)) {
                sourceFormat = i;
                return;
            }
        }
        throw new IllegalArgumentException(format);
    }

    /**
     * Returns the format for displaying the source. This is the pattern specified
     * to the last call to {@link #setSourceFormat}, or the patten specified in the
     * <code>org.geotools.MonolineFormater.source</code> property in the
     * <code>jre/lib/logging.properties</code> file.
     *
     * @return The source pattern, or <code>null</code> if source is not formatted.
     */
    public String getSourceFormat() {
        return FORMAT_LABELS[sourceFormat];
    }

    /**
     * Format the given log record and return the formatted string.
     *
     * @param  record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(final LogRecord record) {
        buffer.setLength(PREFIX.length());
        /*
         * Format the time (e.g. "00:00:12.365").  The time pattern can be set
         * either programmatically with a call to setTimeFormat(String), or in
         * the logging.properties file with the
         * "org.geotools.resources.MonolineFormatter.time" property.
         */
        if (timeFormat != null) {
            Date time = new Date(record.getMillis() - startMillis);
            timeFormat.format(time, buffer, new FieldPosition(0));
            buffer.append(' ');
        }
        /*
         * Format the level (e.g. "FINE"). We do not provide
         * the option to turn level off for now.
         */
        if (true) {
            int offset = buffer.length();
            buffer.append(record.getLevel().getLocalizedName());
            offset = buffer.length() - offset;
            buffer.append(Utilities.spaces(margin-offset));
        }
        /*
         * Add the source. It may be either the source logger or the source
         * class name.
         */
        String logger    = record.getLoggerName();
        String classname = record.getSourceClassName();
        switch (sourceFormat) {
            case LOGGER_SHORT: {
                if (logger.startsWith(base)) {
                    int pos = base.length();
                    if (pos<logger.length()-1 && logger.charAt(pos)=='.') {
                        pos++;
                    }
                    logger = logger.substring(pos);
                }
                // fall through
            }
            case LOGGER_LONG: {
                buffer.append(' ');
                buffer.append(logger);
                break;
            }
            case CLASS_SHORT: {
                int dot = classname.lastIndexOf('.');
                if (dot >= 0) {
                    classname = classname.substring(dot+1);
                }
                classname = classname.replace('$','.');
                // fall through
            }
            case CLASS_LONG: {
                buffer.append(' ');
                buffer.append(classname);
                break;
            }
        }
        buffer.append(SUFFIX);
        /*
         * Now format the message. We will use a line separator made of the 
         * usual EOL ("\r", "\n", or "\r\n", which is plateform specific) 
         * following by some amout of space in order to align message body.
         */
        final int margin  = buffer.length();
        assert margin >= this.margin;
        if (bodyLineSeparator.length() != lineSeparator.length()+margin) {
            bodyLineSeparator = lineSeparator + Utilities.spaces(margin);
        }
        try {
            writer.setLineSeparator(bodyLineSeparator);
            writer.write(String.valueOf(formatMessage(record)));
            writer.setLineSeparator(lineSeparator);
            writer.write('\n');
            writer.flush();
        } catch (IOException exception) {
            // Should never happen, since we are writting into a StringBuffer.
            throw new AssertionError(exception);
        }
        return buffer.toString();
    }

    /**
     * Setup a <code>MonolineFormatter</code> for the specified logger and its
     * children.  This method search for all instances of {@link ConsoleHandler}
     * using the {@link SimpleFormatter}. If such instances are found, they are
     * replaced by a single instance of <code>MonolineFormatter</code> writting
     * to the {@linkplain System#out standard output stream} (instead of the
     * {@linkplain System#err standard error stream}). If no such {@link ConsoleHandler}
     * are found, then a new one is created with this <code>MonolineFormatter</code>.
     * This action has no effect on any loggers outside the <code>base</code> namespace.
     *
     * @param  base The base logger name to apply the change on (e.g. "org.geotools").
     * @return The registered <code>MonolineFormatter</code> (never <code>null</code>).
     *         The formatter output can be configured using the {@link #setTimeFormat}
     *         and {@link #setSourceFormat} methods.
     */
    public static MonolineFormatter init(final String base) {
        return init(base, null);
    }

    /**
     * Convenience method setting both a <code>MonolineFormatter</code> and the logger levels.
     * This method behave like {@link #init(String)}, except that it accepts an optional level
     * argument. If the level is non-null, then all {@link Handler}s using the monoline formatter
     * will be set to the specified level. The logger named <code>base</code> will also be set to
     * this level.
     * <br><br>
     * Note: Avoid this method as much as possible,  since it overrides user's level setting for
     *       the <code>base</code> logger. A user trying to configure its logging properties may
     *       find confusing to see his setting ignored.
     *
     * @param  base The base logger name to apply the change on (e.g. "org.geotools").
     * @param  level The desired level, or <code>null</code> if no level should be set.
     * @return The registered <code>MonolineFormatter</code> (never <code>null</code>).
     *         The formatter output can be configured using the {@link #setTimeFormat}
     *         and {@link #setSourceFormat} methods.
     */
    public static MonolineFormatter init(final String base, final Level level) {
        MonolineFormatter monoline = null;
        final Logger logger = Logger.getLogger(base);
        Logger scan = logger;
        do {
            final Handler[] handlers = scan.getHandlers();
            for (int i=0; i<handlers.length; i++) {
                /*
                 * Search for a ConsoleHandler. Search is performed in the target
                 * handler and all its parent loggers.   When a ConsoleHandler is
                 * found,  it will be replaced by the Stdout handler for 'logger'
                 * only.
                 */
                Handler handler = handlers[i];
                if (handler.getClass().equals(ConsoleHandler.class)) {
                    final Formatter formatter = handler.getFormatter();
                    if (formatter instanceof MonolineFormatter) {
                        /*
                         * A MonolineFormatter already existed. Set the level only for the first
                         * instance  (only one instance should exists anyway),   for consistency
                         * with the fact that this method returns only one MonolineFormatter for
                         * further configuration.
                         */
                        if (monoline == null) {
                            monoline = (MonolineFormatter) formatter;
                            if (level != null) {
                                handler.setLevel(level);
                            }
                        }
                    } else if (formatter.getClass().equals(SimpleFormatter.class)) {
                        /*
                         * A ConsoleHandler using the SimpleFormatter has been found. Replace
                         * the SimpleFormatter by MonolineFormatter, creating it if necessary.
                         * If the handler creation fail with an exception, then we will continue
                         * to use the old J2SE handler instead.
                         */
                        if (monoline == null) {
                            monoline = new MonolineFormatter(base);
                        }
                        try {
                            handler = new Stdout(handler, monoline);
                            if (level != null) {
                                handler.setLevel(level);
                            }
                        } catch (UnsupportedEncodingException exception) {
                            unexpectedException(exception);
                        } catch (SecurityException exception) {
                            unexpectedException(exception);
                        }
                    }
                }
                logger.addHandler(handler);
            }
        } while ((scan=scan.getParent())!=null && scan.getUseParentHandlers());
        /*
         * If no formatter has been found, create a new one and add the handler to the logger.
         * If the creation fail with an exception, then we will continue to use the old J2SE
         * handler instead.
         */
        if (monoline == null) {
            monoline = new MonolineFormatter(base);
            try {
                final Handler handler = new Stdout(monoline);
                if (level != null) {
                    handler.setLevel(level);
                }
                logger.addHandler(handler);
            } catch (SecurityException exception) {
                /*
                 * Returns without any change to the J2SE configuration. Note that the returned
                 * MonolineFormatter is really a dummy one, since we failed to register it.  It
                 * will not prevent to program to work; just produces different logging outputs.
                 */
                unexpectedException(exception);
                return monoline;
            }
        }
        logger.setUseParentHandlers(false);
        if (level != null) {
            logger.setLevel(level);
        }
        return monoline;
    }

    /**
     * Invoked when an error occurs during the initialization.
     */
    private static void unexpectedException(final Exception e) {
        Utilities.unexpectedException("org.geotools.resources", 
                                      "MonolineFormatter", "init", e);
    }

    /**
     * A {@link ConsoleHandler} sending output to {@link System#out} instead of 
     * {@link System#err}  This handler will use a {@link MonolineFormatter} 
     * writting log message on a single line.
     *
     * @task TODO: This class should subclass {@link ConsoleHandler}. 
     * Unfortunatly, this is currently not possible because 
     * {@link ConsoleHandler#setOutputStream} close {@link System#err}. If this 
     * bug get fixed, then {@link #close} no longer need to be overriden.
     */
    private static final class Stdout extends StreamHandler {
        /**
         * Construct a handler.
         *
         * @param formatter The formatter to use.
         */
        public Stdout(final Formatter formatter) {
            super(System.out, formatter);
        }

        /**
         * Construct a handler.
         *
         * @param handler The handler to copy properties from.
         * @param formatter The formatter to use.
         */
        public Stdout(final Handler handler, final Formatter formatter)
            throws UnsupportedEncodingException
        {
            super(System.out, formatter);
            setErrorManager(handler.getErrorManager());
            setFilter      (handler.getFilter      ());
            setLevel       (handler.getLevel       ());
            setEncoding    (handler.getEncoding    ());
        }

        /**
         * Publish a {@link LogRecord} and flush the stream.
         */
        public void publish(final LogRecord record) {
            super.publish(record);	
            flush();
        }

        /**
         * Override {@link StreamHandler#close} to do a flush but not
         * to close the output stream. That is, we do <b>not</b>
         * close {@link System#out}.
         */
        public void close() {
            flush();
        }
    }
}
