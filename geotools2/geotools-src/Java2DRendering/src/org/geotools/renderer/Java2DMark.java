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
 * @version $Id: Java2DMark.java,v 1.4 2002/07/01 11:31:16 ianturton Exp $
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
    static GeneralPath cross,star,triangle,arrow;
    static Shape X;
    static{
            cross = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            cross.moveTo( 0.5f, 0.125f);
            cross.lineTo( 0.125f, 0.125f);
            cross.lineTo( 0.125f, 0.5f);
            cross.lineTo(-0.125f, 0.5f);
            cross.lineTo(-0.125f, 0.125f);
            cross.lineTo(-0.5f, 0.125f);
            cross.lineTo(-0.5f,-0.125f);
            cross.lineTo(-0.125f,-0.125f);
            cross.lineTo(-0.125f,-0.5f);
            cross.lineTo( 0.125f,-0.5f);
            cross.lineTo( 0.125f,-0.125f);
            cross.lineTo( 0.5f,-0.125f);
            cross.lineTo( 0.5f, 0.125f);
            AffineTransform at = new AffineTransform();
            at.rotate(Math.PI/4.0);
            X = cross.createTransformedShape(at);
            star = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            star.moveTo(0.191f, 0.0f);
            star.lineTo(0.25f, 0.344f);
            star.lineTo(0.0f, 0.588f); 
            star.lineTo(0.346f, 0.638f); 
            star.lineTo(0.5f, 0.951f);
            star.lineTo(0.654f, 0.638f); 
            star.lineTo(1.0f, 0.588f); // max = 7.887
            star.lineTo(0.75f, 0.344f); 
            star.lineTo(0.89f, 0f); 
            star.lineTo(0.5f, 0.162f); 
            star.lineTo(0.191f, 0.0f);
            at = new AffineTransform();
            at.translate(-.5,-.5);
            star.transform(at);
            triangle = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            triangle.moveTo(0f,1f);
            triangle.lineTo(0.866f,-.5f);
            triangle.lineTo(-0.866f,-.5f);
            triangle.lineTo(0f,1f);
            at = new AffineTransform();
            
            at.translate(0,-.25);
            at.scale(.5,.5);
           
            triangle.transform(at);
            
            arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            arrow.moveTo(0f,-.5f);
            arrow.lineTo(.5f,0f);
            arrow.lineTo(0f,.5f);
            arrow.lineTo(0f,.1f);
            arrow.lineTo(-.5f,.1f);
            arrow.lineTo(-.5f,-.1f);
            arrow.lineTo(0f,-.1f);
            arrow.lineTo(0f,-.5f);
            
    }
    static Shape getWellKnownMark(String wellKnownName){
        _log.debug("fetching mark of name "+wellKnownName);
        if(wellKnownName.equalsIgnoreCase("cross")){
            _log.debug("returning cross");
            return cross;
        }
        if(wellKnownName.equalsIgnoreCase("circle")){
            _log.debug("returning circle");
            return new java.awt.geom.Ellipse2D.Double(-.5,-.5,1.,1.);
        }
        if(wellKnownName.equalsIgnoreCase("triangle")){
            _log.debug("returning triangle");
            
            return triangle;
        }
        if(wellKnownName.equalsIgnoreCase("X")){
            _log.debug("returning X");
            return X;
        }
        if(wellKnownName.equalsIgnoreCase("star")){
            _log.debug("returning star");
            return star;
        }
        if(wellKnownName.equalsIgnoreCase("arrow")){
            _log.debug("returning arrow");
            return arrow;
        }
        // failing that return a square?
        _log.debug("returning square");
        return new Rectangle2D.Double(-.5,-.5,1.,1.);
    }
}
