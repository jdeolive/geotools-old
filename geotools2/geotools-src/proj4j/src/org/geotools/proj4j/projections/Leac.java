/*
 * Lamb.java
 *
 * Created on February 28, 2002, 2:48 PM
 */

package org.geotools.proj4j.projections;
import org.geotools.proj4j.*;

/**
 *
 * @author  jamesm
 */
public class Leac extends Aea implements Constants{

    
    
    protected void setup(ParamSet params){
        phi1 = params.getRadiansParam("lat_1");
        phi2 = (params.contains("south"))?-HALFPI:HALFPI;
    }

}
