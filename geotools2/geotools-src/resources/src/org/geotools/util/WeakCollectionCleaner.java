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
package org.geotools.util;

// J2SE dependencies
import java.lang.ref.Reference;     // For javadoc
import java.lang.ref.WeakReference; // For javadoc
import java.lang.ref.ReferenceQueue;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A thread invoking {@link Reference#clear} on each enqueded reference.
 * This is usefull only if <code>Reference</code> subclasses has overriden
 * their <code>clear()</code> method in order to perform some cleaning.
 * This thread is used by {@link WeakHashSet} and {@link WeakValueHashMap},
 * which remove their entry from the collection when {@link Reference#clear}
 * is invoked.
 *
 * @version $Id: WeakCollectionCleaner.java,v 1.3 2003/08/04 18:21:32 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class WeakCollectionCleaner extends Thread {
    /**
     * The default thread.
     */
    public static final WeakCollectionCleaner DEFAULT = new WeakCollectionCleaner();

    /**
     * List of reference collected by the garbage collector.
     * Those elements must be removed from {@link #table}.
     */
    public final ReferenceQueue referenceQueue = new ReferenceQueue();

    /**
     * Construct and stard a new thread as a daemon. This thread will be stoped
     * most of the time.  It will run only some few nanoseconds each time a new
     * {@link WeakReference} is enqueded.
     */
    private WeakCollectionCleaner() {
        super("WeakCollectionCleaner");
        setDaemon(true);
        start();
    }

    /**
     * Loop to be run during the virtual machine lifetime.
     */
    public void run() {
        while (true) {
            try {
                // Block until a reference is enqueded.
                // Note: To be usefull, the clear() method must have
                //       been overriden in Reference subclasses.
                referenceQueue.remove().clear();
            } catch (InterruptedException exception) {
                // Somebody doesn't want to lets
                // us sleep... Go back to work.
            } catch (Exception exception) {
                Utilities.unexpectedException("org.geotools.resources", "WeakCollection",
                                              "remove", exception);
            } catch (AssertionError exception) {
                Utilities.unexpectedException("org.geotools.resources", "WeakCollection",
                                              "remove", exception);
                // Do not kill the thread on assertion failure, in order to
                // keep the same behaviour as if assertions were turned off.
            }
        }
    }
}
