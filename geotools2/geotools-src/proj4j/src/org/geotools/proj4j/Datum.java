/*
 * Datum.java
 *
 * Created on 19 February 2002, 22:36
 */

package org.geotools.proj4j;

import java.util.StringTokenizer;
/**
 *
 * @author  James Macgill
 */
public class Datum {
    public static final double SEC_TO_RAD = 4.84813681109535993589914102357e-6;
    
    String    id;     /* datum keyword */
    String    defn;   /* ie. "to_wgs84=..." */
    String    ellipse_id; /* ie from ellipse table */
    String    comments; /* EPSG code, etc */
    double[]    params;

    /** Creates a new instance of Datum */
    public Datum(String id,String defn,String ellipse_id,String comments) {
        this.id = id;
        this.defn = defn;
        this.ellipse_id = ellipse_id;
        this.comments = comments;
        if(defn.startsWith("towgs84")){
            StringTokenizer tok = new StringTokenizer(defn.substring(defn.indexOf("=")+1),",");
            params = new double[tok.countTokens()];
            for(int i=0;i<params.length;i++){
                double value = Double.parseDouble(tok.nextToken());
                switch(i){
                    case 3:
                    case 4:
                    case 5:
                        value*=SEC_TO_RAD;
                        break;
                    case 6:// transform from parts per million to scaling factor 
                        value=(value/1000000.0d)+1;
                }   
                params[i] = value;
            }
        }
    }
    
    public String getDefn(){
        return defn;
    }
    
    public String getEllipseID(){
        return ellipse_id;
    }
    
    
    public int getParamCount(){
        if(params==null)return 0;
        return params.length;
    }
    
    public double[] getParams(){
        return params;
    }
    
     public static Datum getDatum(String id){
        for(int i=0;i<datums.length;i++){
            if(id.equalsIgnoreCase(datums[i].id)){
                return datums[i];
            }
        }
        return null;
    }
    
     public static final Datum[] datums={
        new Datum("WGS84","towgs84=0,0,0","WGS84","")
     };

}
