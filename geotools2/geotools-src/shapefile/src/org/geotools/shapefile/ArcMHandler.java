/*
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

package org.geotools.shapefile;

import java.io.*;
import cmp.LEDataStream.*;

/**
 * Wrapper for a Shapefile arc, yet to be updated to JTS.
 *
 * @version $Id: ArcMHandler.java,v 1.2 2002/06/05 12:46:51 loxnard Exp $
 * @author James Macgill, CCG
 */
public class ArcMHandler  {
    
    /**
     * Gets the type of shape stored (Shapefile.ARC)
     */
    public int getShapeType(){
        return Shapefile.ARC_M;
    }
    /*
    public int getLength(){
        return (44+(4*numParts)+16+(16*numParts));
    }
    */
  
}