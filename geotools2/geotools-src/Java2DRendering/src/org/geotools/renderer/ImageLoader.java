/*
 * ImageLoader.java
 *
 * Created on 07 June 2002, 14:58
 */

package org.geotools.renderer;

/**
 *
 * @author  iant
 */
import java.net.*;
import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;

//Logging system
import org.apache.log4j.Logger;

public class ImageLoader implements Runnable{
    static HashMap images = new HashMap();
    static Canvas obs = new Canvas();
    static MediaTracker tracker = new MediaTracker(obs);
    static int imageID = 1;
    static java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
    private static Logger _log = Logger.getLogger(ImageLoader.class);
    /** Creates a new instance of ImageLoader */
    public ImageLoader() {
    }
    URL location;
    boolean waiting = true;
    private void add(URL location, boolean interactive){
        this.location = location;
        Thread t = new Thread(this);
        t.start();
        if(interactive){
            return;
        }else{
            while(waiting){
                try{
                    Thread.sleep(500);
                } catch (InterruptedException e){
                }
            }
            return;
        }
            
    }
    
    public BufferedImage get(URL location,boolean interactive){
        if(images.containsKey(location)){
            _log.debug("found it ");
            return (BufferedImage)images.get(location);
        }else{
            images.put(location,null);
            _log.debug("adding "+location);
            add(location,interactive);
            return (BufferedImage)images.get(location);
        }
    }
    
    public void run() {
        int myID = 0;
        Image img = null;
        try {
            img = tk.createImage(location);
            myID = imageID++;
            tracker.addImage(img,myID);
            
        } catch ( Exception e ) {
            _log.error("Exception fetching image from "+location+"\n"+e);
            images.remove(location);
            waiting = false;
            return;
        }
        
        try{
            while((tracker.statusID(myID,true)&tracker.LOADING)!=0){
                tracker.waitForID(myID,500);
                
            }
        } catch (InterruptedException ie){
        }
        
        int state=tracker.statusID(myID,true);
        
        
        if(state==tracker.ERRORED){
            _log.debug("Il: ERRORED");
            images.remove(location);
            waiting = false;
            return;
        }
        
        if((state&tracker.COMPLETE) == tracker.COMPLETE){
            
            int iw = img.getWidth(obs);
            int ih = img.getHeight(obs);
            BufferedImage bi = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
            Graphics2D big = bi.createGraphics();
            big.drawImage(img,0,0,obs);
            images.put(location,bi);
        }
        waiting = false;
        return;
    }
}