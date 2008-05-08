/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.renderer.lite;


import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform2D;

/**
 * Seems to be a cache of fun information associated with the Symbolizer.
 * 
 * @source $URL$
 */
public class SymbolizerAssociation
{
     public MathTransform2D  xform = null;
     public CoordinateReferenceSystem crs = null;
     
     public SymbolizerAssociation()
     {

     }
     
     public MathTransform2D getXform()
     {
     	return xform;
     }
     
     public void setXform(MathTransform2D xform)
     {
     	this.xform = xform;
     }
     
     public CoordinateReferenceSystem getCRS()
     {
     	return crs;
     }
     
     public void setCRS(CoordinateReferenceSystem crs)
     {
     	this.crs = crs;
     }
     
}
