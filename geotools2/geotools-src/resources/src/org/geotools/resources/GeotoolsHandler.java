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
import java.util.logging.LogManager;
import java.util.logging.StreamHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

// Input/Output
import java.io.UnsupportedEncodingException;


/**
 * A {@link ConsoleHandler} sending output to {@link System#out} instead of {@link System#err}.
 * This handler will use a <code>MonolineFormatter</code> writting log message on a single line.
 * This formatter is used by Geotools 2 instead of {@link SimpleFormatter}. The main difference
 * is that this formatter use only one line per message instead of two. For example, a message
 * logged to this handler will looks like:
 *
 * <blockquote><pre>
 * [core FINE] A log message logged with level FINE from the "org.geotools.core" logger.
 * </pre></blockquote>
 *
 * There is two way to register a <code>GeotoolsHandler</code>. The fist way is to call
 * explicitely the following method during the initialization of an application (e.g. in
 * its <code>main(String[])</code> method, or as a static class initializer):
 *
 * <blockquote><pre>
 * GeotoolsHandler.{@link #init(String[]) init}("org.geotools");
 * </pre></blockquote>
 *
 * With this example, a <code>GeotoolsHandler</code> is registered unconditionnaly for the
 * <code>"org.geotools"</code> logger and all its children. This logger will replace any
 * previously registered {@link ConsoleHandler} for this logger, no matter what the user's
 * setting was. If you want to use the <code>GeotoolsHandler</code> for only some selected
 * Java environment, without hard-coded registration, then you can edit the
 * <code>jre/lib/logging.properties</code> file and append
 * <code>org.geotools.resources.GeotoolsHandler</code> to the list of handlers. For example:
 *
 * <blockquote><pre>
 * handlers= java.util.logging.ConsoleHandler, org.geotools.resources.GeotoolsHandler
 * </pre></blockquote>
 *
 * Note that <code>GeotoolsHandler</code> should be declared <em>after</em>
 * <code>ConsoleHandler</code>, since it will replace <code>ConsoleHandler</code> for the
 * <code>"org.geotools"</code> logger and its children only.
 *
 * @version $Is$
 * @author Martin Desruisseaux
 *
 * @task TODO: This class should subclass {@link ConsoleHandler}. Unfortunatly, this is
 *             currently not possible because {@link ConsoleHandler#setOutputStream}
 *             close {@link System#err}. If this bug get fixed, then {@link #close}
 *             no longer need to be overriden.
 */
public class GeotoolsHandler extends StreamHandler {
    /**
     * The base logger name. This is the same than {@link MonolineFormatter#base}.
     */
    private final String base;

    /**
     * Construct a handler for the "<code>org.geotools</code>" packages. User should not invokes
     * this constructor explicitely. Instead, this constructor is invoked by {@link LogManager}
     * when a message is first logged. This constructor will be invoked providing that the
     * <code>jre/lib/logging.properties</code> file contains at least the following declaration:
     *
     * <blockquote><pre>
     * handlers= java.util.logging.ConsoleHandler, org.geotools.resources.GeotoolsHandler
     * </pre></blockquote>
     *
     * This handler will log messages for the <code>"org.geotools"</code> logger and its
     * children only. Other loggers will continue to use {@link ConsoleHandler}.
     */
    public GeotoolsHandler() {
        this("org.geotools");
    }

    /**
     * Construct a handler for the specified logger and its children. This constructor is available
     * for subclasses that wants to use the same functionality than <code>GeotoolsHandler</code>
     * with loggers outside the <code>"org.geotools"</code> namespace. For example, a user
     * wanting the same kind of logging for <code>"com.mycomp"</code> packages could subclass
     * <code>GeotoolsHandler</code> and invokes <code>super("com.mycomp")</code> in its
     * no-args constructor.
     *
     * @param base The base logger name to apply the handler on (e.g. "org.geotools").
     */
    protected GeotoolsHandler(final String base) {
        this(base, true);
    }

    /**
     * Construct a handler for the specified logger and its children.
     *
     * @param base The base logger name to apply the handler on (e.g. "org.geotools").
     * @param init <code>true</code> if we want to replace <code>ConsoleHandler</code>
     *        instances by this logger.
     */
    private GeotoolsHandler(final String base, final boolean init) {
        super(System.out, new MonolineFormatter(base));
        this.base = base;
        if (init) {
            init(base, this);
        }
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
        init(base, null);
    }

    /**
     * Do the setup.
     *
     * @param  base The base logger name to apply the change on (e.g. "org.geotools").
     * @return toInit If a {@link GeotoolsHandler} has already been constructed but not
     *         yet initialized, this handler. Otherwise, <code>null</code>.
     */
    private static void init(final String base, GeotoolsHandler toInit) {
//System.out.println("explicit=" + (toInit==null));
        final Logger logger = Logger.getLogger(base);
        if (false) {
            final Handler[] handlers = logger.getParent().getHandlers();
            for (int i=0; i<handlers.length; i++) {
                final Handler handler = handlers[i];
                if (handler.getClass().equals(toInit.getClass())) {
                    if (((GeotoolsHandler) handler).base.equals(toInit.base)) {
//System.out.println("skip");
                        return;
                    }
                }
            }
        }
        for (Logger parent=logger; parent.getUseParentHandlers();) {
            parent = parent.getParent();
            if (parent == null) {
                break;
            }
            final Handler[] handlers = parent.getHandlers();
            for (int i=0; i<handlers.length; i++) {
                /*
                 * Search for a ConsoleHandler. Search is performed in the target handler
                 * and all its parent loggers. When a ConsoleHandler is found, it will be
                 * replaced by this GeotoolsHandler for 'logger' only.
                 */
                Handler handler = handlers[i];
//System.out.println("test=" +  (handler instanceof GeotoolsHandler));
                if (handler.getClass().equals(ConsoleHandler.class)) {
                    final Formatter formatter = handler.getFormatter();
                    if (formatter.getClass().equals(SimpleFormatter.class)) {
                        try {
                            if (toInit == null) {
                                toInit = new GeotoolsHandler(base, false);
                            }
                            toInit.setErrorManager(handler.getErrorManager());
                            toInit.setFilter      (handler.getFilter      ());
                            toInit.setLevel       (handler.getLevel       ());
                            toInit.setEncoding    (handler.getEncoding    ());
                            handler = toInit;
                            toInit  = null;
//System.out.println("Registering "+base+" in "+logger.getName()+"  ["+parent.getName()+"]" + Thread.currentThread());
                        } catch (UnsupportedEncodingException exception) {
                            unexpectedException(exception);
                        } catch (SecurityException exception) {
                            unexpectedException(exception);
                        }
                    }
                }
                logger.addHandler(handler);
            }
        }
        logger.setUseParentHandlers(false);
//System.out.println("finished");
    }

    /**
     * Invoked when an error occurs during the initialization.
     */
    private static void unexpectedException(final Exception e) {
        Utilities.unexpectedException("org.geotools.resources", "GeotoolsHandler", "init", e);
    }

    /**
     * Publish a {@link LogRecord} and flush the stream.
     */
    public void publish(final LogRecord record) {
        final String name = record.getLoggerName();
        if (!name.startsWith(base)) {
            /*
             * We are not supposed to log this message. However, we may fall in this block
             * if the GeotoolsHandler has been registered using logging.properties file,
             * since this properties file register all handlers in the root looger.
             * Remove this handler from the logger and all parent loggers.
             */
            Logger logger = LogManager.getLogManager().getLogger(name);
            while (logger != null) {
                logger.removeHandler(this);
                logger = logger.getParent();
            }
            return;
        }
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
