/**
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.renderer;

// J2SE dependencies
import java.net.*;
import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * $Id: ImageLoader.java,v 1.6 2002/08/07 07:36:26 desruisseaux Exp $
 * @author Ian Turton
 */
public class ImageLoader implements Runnable{
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");

    static HashMap images = new HashMap();
    static Canvas obs = new Canvas();
    static MediaTracker tracker = new MediaTracker(obs);
    static int imageID = 1;
    static java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();

    /**
     * Creates a new instance of ImageLoader
     */
    public ImageLoader() {
    }
    URL location;
    boolean waiting = true;
    private void add(URL location, boolean interactive){
        
        int localId = imageID;
        this.location = location;
        LOGGER.finest("adding image, interactive? "+interactive);
        Thread t = new Thread(this);
        t.start();
        if (interactive){
            LOGGER.finest("fast return");
            return;
        } else{
            waiting = true;
            while (waiting){
                LOGGER.finest("waiting..."+waiting);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e){
                }
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest(""+localId+" complete?: "+((tracker.statusID(localId,true)&tracker.COMPLETE) == tracker.COMPLETE));
                LOGGER.finest(""+localId+" abort?: "+((tracker.statusID(localId,true)&tracker.ABORTED) == tracker.ABORTED));
                LOGGER.finest(""+localId+" error?: "+((tracker.statusID(localId,true)&tracker.ERRORED) == tracker.ERRORED));
                LOGGER.finest(""+localId+" loading?: "+((tracker.statusID(localId,true)&tracker.LOADING) == tracker.LOADING));
                LOGGER.finest(""+localId+"slow return "+waiting);
            }
            return;
        }
            
    }
    
    public BufferedImage get(URL location, boolean interactive){
        if (images.containsKey(location)){
            LOGGER.finest("found it");
            return (BufferedImage) images.get(location);
        } else{
            if (!interactive){
                images.put(location, null);
            }
            LOGGER.finest("adding "+location);
            add(location, interactive);
            return (BufferedImage) images.get(location);
        }
    }
    
    public void run() {
        int myID = 0;
        Image img = null;
        try {
            img = tk.createImage(location);
            myID = imageID++;
            tracker.addImage(img, myID);
            
        } catch ( Exception e ) {
            LOGGER.warning("Exception fetching image from " + location + "\n" + e);
            images.remove(location);
            waiting = false;
            return;
        }
        
        try {
            while ((tracker.statusID(myID, true)&tracker.LOADING) != 0){
                tracker.waitForID(myID, 500);
                LOGGER.finest(""+myID+"loading - waiting....");
            }
        } catch (InterruptedException ie){
        }
        
        int state = tracker.statusID(myID, true);
        
        
        if (state == tracker.ERRORED){
            LOGGER.finer("" + myID + " Error loading");
            images.remove(location);
            waiting = false;
            return;
        }
        
        if ((state&tracker.COMPLETE) == tracker.COMPLETE){
            LOGGER.finest("" + myID + "completed load");
            int iw = img.getWidth(obs);
            int ih = img.getHeight(obs);
            BufferedImage bi = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
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