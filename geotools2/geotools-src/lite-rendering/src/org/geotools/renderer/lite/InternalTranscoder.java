package org.geotools.renderer.lite;

import java.awt.image.BufferedImage;


/*
 * InternalTranscoder.java
 *
 * Created on March 31, 2004, 4:09 PM
 */


/**
 *
 * @author  jamesm 
 */
public class InternalTranscoder extends org.apache.batik.transcoder.image.ImageTranscoder {


    private BufferedImage result;


    /** Creates a new instance of InternalTranscoder */
    public InternalTranscoder() {
    }


    public java.awt.image.BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }



    //gets called by the end of the image transcoder with an actual image...
    public void writeImage(java.awt.image.BufferedImage img, org.apache.batik.transcoder.TranscoderOutput output) {
        result = img;
    }


    public BufferedImage getImage(){
        return result;
    }


}