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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.StreamHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

// Preferences
import java.util.prefs.Preferences;

// Writer
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import org.geotools.io.LineWriter;


/**
 * A formatter writting log message on a single line. This formatter is used by 
 * Geotools 2 instead of {@link SimpleFormatter}. The main difference is that 
 * this formatter use only one line per message instead of two. For example, a 
 * message formatted by
 * <code>Log4JFormatter</code> looks like:
 *
 * <blockquote><pre>
 * [core FINE] A log message logged with level FINE from the "org.geotools.core"
 * logger.</pre></blockquote>
 *
 * @version $Id: Log4JFormatter.java,v 1.3 2002/08/19 18:15:30 desruisseaux Exp 
 * @author Martin Desruisseaux
 *
 * @deprecated Use {@link MonolineFormatter} instead.
 */
public class Log4JFormatter 
    extends Formatter {

    /**
     * The string to write at the begining of all log headers (e.g. "[FINE core]")
     */
    private static final String PREFIX = "[";

    /**
     * The string to write at the end of every log header (e.g. "[FINE core]").
     * It should includes the spaces between the header and the message body.
     */
    private static final String SUFFIX = "] ";

    /**
     * The string to write at the end of every log header (e.g. "[FINE core]").
     * It should includes the spaces between the header and the message body.
     */
    private static long startMillis;

    /**
     * The line separator. This is the value of the "line.separator"
     * property at the time the <code>Log4JFormatter</code> was created.
     */
    private final String lineSeparator = 
        System.getProperty("line.separator", "\n");

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
     * formatted as "[LEVEL core]"
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
     * Construct a <code>Log4JFormatter</code>.
     *
     * @param base   The base logger name. This is used for shortening the logger 
     *               name when formatting message. For example, if the base 
     *               logger name is "org.geotools" and a log record come from 
     *               the "org.geotools.core" logger, it will be formatted as 
     *               "[LEVEL core]" (i.e. the "org.geotools" part is ommited).
     */
    public Log4JFormatter(final String base) {
        this.base   = base.trim();
        this.margin = getHeaderWidth();
        this.startMillis = System.currentTimeMillis();
        final StringWriter str = new StringWriter();
        writer = new LineWriter(str);
        buffer = str.getBuffer();
    }

    /**
     * Format the given log record and return the formatted string.
     *
     * @param  record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(final LogRecord record) {
        String logger = record.getLoggerName();

        final String recordLevel = record.getLevel().getLocalizedName();
        
        try {
            
            buffer.setLength(1);
            final Long millis = new Long(record.getMillis() - startMillis);
            writer.write(millis.toString());
            writer.write(" ");
            writer.write(PREFIX);
            writer.write(recordLevel);
            writer.write(SUFFIX);
            writer.write(record.getSourceClassName());
            writer.write(" - ");
            
            /*
             * Now format the message. We will use a line separator made of 
             * the usual EOL ("\r", "\n", or "\r\n", which is plateform 
             * specific) following by some amout of space in order to align 
             * message body.
             */
        
            writer.setLineSeparator(bodyLineSeparator);
            writer.write(formatMessage(record));
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
     * {@linkplain System#err standard error stream}).  This action has no effect 
     * on any loggers outside the <code>base</code> namespace.
     *
     * @param base The base logger name to apply the change on 
     * (e.g. "org.geotools").
     */
    public static void init(final String base, Level filterLevel) {
        Formatter log4j = null;

        final Logger logger = Logger.getLogger(base);

        for (Logger parent=logger; parent.getUseParentHandlers();) {
            parent = parent.getParent();
            if (parent == null) {
                break;
            }
            final Handler[] handlers = parent.getHandlers();
            for (int i=0; i<handlers.length; i++) {
                /*
                 * Search for a ConsoleHandler. Search is performed in the target 
                 * handler and all its parent loggers. When a ConsoleHandler is 
                 * found, it will be replaced by the Stdout handler for 'logger' 
                 * only.
                 */
                Handler handler = handlers[i];
                if (handler.getClass().equals(ConsoleHandler.class)) {
                    final Formatter formatter = handler.getFormatter();
                    if (formatter.getClass().equals(SimpleFormatter.class)) {
                        if (log4j == null) {
                            log4j = new Log4JFormatter(base);
                        }
                        try {
                            handler = new Stdout(handler, log4j);
                            handler.setLevel(filterLevel);
                        } catch (UnsupportedEncodingException exception) {
                            unexpectedException(exception);
                        } catch (SecurityException exception) {
                            unexpectedException(exception);
                        }
                    }
                }
                logger.addHandler(handler);
		logger.setLevel(filterLevel);
            }
        }
        logger.setUseParentHandlers(false);
    }

    /**
     * Invoked when an error occurs during the initialization.
     */
    private static void unexpectedException(final Exception e) {
        Utilities.unexpectedException("org.geotools.resources", 
                                      "GeotoolsHandler", "init", e);
    }

    /**
     * Returns the header width. This is the default value to use for 
     * {@link #margin}, if no value has been explicitely set. This value can be 
     * set in user's preferences.
     */
    private static int getHeaderWidth() {
        return Preferences.userNodeForPackage(Log4JFormatter.class).
            getInt("logging.header", 15);
    }

    /**
     * Set the header width. This is the default value to use for {@link #margin}
     * for next {@link Log4JFormatter} to be created.
     */
    static void setHeaderWidth(final int margin) {
        Preferences.userNodeForPackage(Log4JFormatter.class).
            putInt("logging.header", margin);
    }

    /**
     * A {@link ConsoleHandler} sending output to {@link System#out} instead of 
     * {@link System#err}  This handler will use a {@link Log4JFormatter} 
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
