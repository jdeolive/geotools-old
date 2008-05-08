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
 * 
 *    Created on March 31, 2004, 4:09 PM
 */
package org.geotools.renderer.lite;

import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import org.w3c.dom.Document;


/**
 *
 * @author  jamesm 
 * @source $URL$
 */
public class InternalTranscoder extends org.apache.batik.transcoder.image.ImageTranscoder {


    private BufferedImage result;


    /** Creates a new instance of InternalTranscoder */
    public InternalTranscoder() {
    }


	public java.awt.image.BufferedImage createImage(int width, int height) {
        if(GraphicsEnvironment.isHeadless())
            return new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        else
            return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height, Transparency.TRANSLUCENT);
	}



    //gets called by the end of the image transcoder with an actual image...
    public void writeImage(java.awt.image.BufferedImage img, org.apache.batik.transcoder.TranscoderOutput output) {
        result = img;
    }


    public BufferedImage getImage(){
        return result;
    }
  
   public void transcode (  Document inputDoc ) throws Exception
   {
	   super.transcode(inputDoc,null,null);
   }

}
