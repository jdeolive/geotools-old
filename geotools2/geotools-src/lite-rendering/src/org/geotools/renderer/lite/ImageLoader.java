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
package org.geotools.renderer.lite;

import java.awt.*;
import java.awt.image.*;

// J2SE dependencies
import java.net.*;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * $Id: ImageLoader.java,v 1.1 2003/02/09 09:49:15 aaime Exp $
 * 
 * @author Ian Turton
 */
public class ImageLoader implements Runnable {
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    static HashMap images = new HashMap();
    static Canvas obs = new Canvas();
    static MediaTracker tracker = new MediaTracker(obs);
    static int imageID = 1;
    static java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
    URL location;
    boolean waiting = true;

    /**
     * Creates a new instance of ImageLoader
     */
    public ImageLoader() {
    }

    private void add(URL location, boolean interactive) {
        int localId = imageID;
        this.location = location;
        LOGGER.finest("adding image, interactive? " + interactive);

        Thread t = new Thread(this);
        t.start();

        if (interactive) {
            LOGGER.finest("fast return");

            return;
        } else {
            waiting = true;

            while (waiting) {
                LOGGER.finest("waiting..." + waiting);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("" + localId + " complete?: " + 
                              ((tracker.statusID(localId, true) & tracker.COMPLETE) == tracker.COMPLETE));
                LOGGER.finest("" + localId + " abort?: " + 
                              ((tracker.statusID(localId, true) & tracker.ABORTED) == tracker.ABORTED));
                LOGGER.finest("" + localId + " error?: " + 
                              ((tracker.statusID(localId, true) & tracker.ERRORED) == tracker.ERRORED));
                LOGGER.finest("" + localId + " loading?: " + 
                              ((tracker.statusID(localId, true) & tracker.LOADING) == tracker.LOADING));
                LOGGER.finest("" + localId + "slow return " + waiting);
            }

            return;
        }
    }

    /**
     * Fetch a buffered image from the loader, if interactive is false then the
     * loader will wait for  the image to be available before returning, used
     * by printers and file output renderers. If interactive is true and the
     * image is ready then return, if image is not ready start loading it  and
     * return null. The renderer is responsible for finding an alternative to
     * use.
     * 
     * @param location the url of the image to be fetched
     * @param interactive boolean to signal if the loader should wait for the
     *        image to be ready.
     * 
     * @return the buffered image or null
     */
    public BufferedImage get(URL location, boolean interactive) {
        if (images.containsKey(location)) {
            LOGGER.finest("found it");

            return (BufferedImage) images.get(location);
        } else {
            if (!interactive) {
                images.put(location, null);
            }

            LOGGER.finest("adding " + location);
            add(location, interactive);

            return (BufferedImage) images.get(location);
        }
    }

    /**
     * Runs the loading thread
     */
    public void run() {
        int myID = 0;
        Image img = null;

        try {
            img = tk.createImage(location);
            myID = imageID++;
            tracker.addImage(img, myID);
        } catch (Exception e) {
            LOGGER.warning("Exception fetching image from " + location + 
                           "\n" + e);
            images.remove(location);
            waiting = false;

            return;
        }

        try {
            while ((tracker.statusID(myID, true) & tracker.LOADING) != 0) {
                tracker.waitForID(myID, 500);
                LOGGER.finest("" + myID + "loading - waiting....");
            }
        } catch (InterruptedException ie) {
        }

        int state = tracker.statusID(myID, true);

        if (state == tracker.ERRORED) {
            LOGGER.finer("" + myID + " Error loading");
            images.remove(location);
            waiting = false;

            return;
        }

        if ((state & tracker.COMPLETE) == tracker.COMPLETE) {
            LOGGER.finest("" + myID + "completed load");

            int iw = img.getWidth(obs);
            int ih = img.getHeight(obs);
            BufferedImage bi = new BufferedImage(iw, ih, 
                                                 BufferedImage.TYPE_INT_ARGB);
            Graphics2D big = bi.createGraphics();
            big.drawImage(img, 0, 0, obs);
            images.put(location, bi);

            waiting = false;

            return;
        }

        LOGGER.finer("" + myID + " whoops - some other outcome " + state);
        waiting = false;

        return;
    }
}