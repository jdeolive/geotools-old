/*
 * Java2DMark.java
 *
 * Created on 03 June 2002, 11:11
 */

package org.geotools.renderer;

/**
 *
 * @author  iant
 */

import java.awt.*;
import java.awt.geom.*;

public class Java2DMark {
    private static org.apache.log4j.Logger _log =
        org.apache.log4j.Logger.getLogger(Java2DMark.class);
    /** Creates a new instance of Java2DMark */
    public Java2DMark() {
        
    }
    
    static Shape getWellKnownMark(String wellKnownName){
        _log.debug("fetching mark of name "+wellKnownName);
        if(wellKnownName.equalsIgnoreCase("cross")){
            int x[] = { 4, 1, 1,-1,-1,-4,-4,-1,-1, 1, 1, 4};
            int y[] = { 1, 1, 4, 4, 1, 1,-1,-1,-4,-4,-1,-1};
            _log.debug("returning cross");
            return new java.awt.Polygon(x,y,12);
        }
        if(wellKnownName.equalsIgnoreCase("circle")){
            _log.debug("returning circle");
            return new java.awt.geom.Ellipse2D.Double(-.5,-.5,1.,1.);
        }
        if(wellKnownName.equalsIgnoreCase("triangle")){
            _log.debug("returning triangle");
            int x[] = {-1,0,1};
            int y[] = {1,-1,1};
            return new java.awt.Polygon(x,y,3);
        }
        if(wellKnownName.equalsIgnoreCase("X")){
            _log.debug("returning X");
            int x[] = { 1, 2, 1, 0,-1,-2,-1,-2,-1, 0, 1, 2};
            int y[] = { 0, 1, 2, 1, 2, 1, 0,-1,-2,-1,-2,-1};
            return new java.awt.Polygon(x,y,12);
        }
        if(wellKnownName.equalsIgnoreCase("star")){
            _log.debug("returning star");
            int x[] = { 2, 1, 0,-1,-2,-1,-2,-1, 0, 1, 2, 1};
            int y[] = { 1, 1, 2, 1, 1, 0,-1,-1,-2,-1,-1, 0};
            return new java.awt.Polygon(x,y,12);
        }
        // failing that return a square?
        _log.debug("returning square");
        return new Rectangle2D.Double(-.5,-.5,1.,1.);
    }
}
