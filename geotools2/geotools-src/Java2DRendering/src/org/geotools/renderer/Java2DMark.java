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

/**
 * @version $Id: Java2DMark.java,v 1.2 2002/06/04 19:22:23 loxnard Exp $
 * @author Ian Turton
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
