/*
 * Datum.java
 *
 * Created on 19 February 2002, 22:36
 */

package org.geotools.proj4j;

import java.util.StringTokenizer;
/** Stores the defenition of a Datum
 *
 * @author James Macgill
 */
public class Datum {
    /** Conversion factor between seconds and radians
     */
    public static final double SEC_TO_RAD = 4.84813681109535993589914102357e-6;
    /** constant for use in 'type' denotes unknown projection type
     */
    public static final int  PJD_UNKNOWN   =0;
    /** constant for use in 'type' denotes three parameter projection
     */
    public static final int  PJD_3PARAM    =1;   /* Molodensky */
    /** constant for use in 'type' denotes seven parameter projection
     */
    public static final int  PJD_7PARAM    =2;   /* Molodensky */
    /** constant for use in 'type' denotes projection uses a grid shift
     */
    public static final int  PJD_GRIDSHIFT =3;
    /** constant for use in 'type' denotes standard WSG84 datum
     */
    public static final int  PJD_WGS84     =4;   /* WGS84 (or anything considered equivelent) */
    String    id;     /* datum keyword */
    String    defn;   /* ie. "to_wgs84=..." */
    String    ellipse_id; /* ie from ellipse table */
    String    comments; /* EPSG code, etc */
    double[]    datumParams;
    int datumType;
    
    /** Creates a new instance of Datum
     * @param params A param set which may contain parameters for this datum
     * @throws ProjectionException May be thrown if invalid paramiters are use to construct the dataum
     */
    public Datum(ParamSet params) throws ProjectionException{
        String name, towgs84, nadgrids;
        
        datumType = PJD_UNKNOWN;
        
        if(params.contains("datum")){//more of this needs to move into Datum.java
            String[] defaults = Datum.getDefaultsForDatum(params.getStringParam("datum"));
            
            if(defaults==null) throw new ProjectionException("datum unknown");
            this.id = defaults[0]; //id;
            this.defn = defaults[1]; //defn;
            this.ellipse_id = defaults[2]; //ellipse_id;
            this.comments = defaults[3]; //comments;
            
            
            if(ellipse_id != null || ellipse_id.length() >0){
                
                // if a different ellipse has been defined in the parameters then it over rides
                // the default in the datum
                //TODO: need to notify user in very verbose mode that this has happened
                params.addParamIfNotSet("ellips="+ellipse_id);
                
            }
            
            if(defn != null || defn.length() >0){
                params.addParamIfNotSet(defn);
            }
        }
        if(params.contains("nadgrids")){
                 /* We don't actually save the value separately.  It will continue
                  *  to exist in the param list for use in applyGridShift
                  */
            datumType = PJD_GRIDSHIFT;
        }
        else if(params.contains("towgs84")){
            StringTokenizer tok = new StringTokenizer(defn.substring(defn.indexOf("=")+1),",");
            datumParams = new double[tok.countTokens()];
            for(int i=0;i<datumParams.length;i++){
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
                datumParams[i] = value;
            }
            if(getParamCount()==3) datumType = PJD_3PARAM;
            if(getParamCount()==7){
                if((datumParams[3]==0.0)
                &&(datumParams[4]==0.0)
                &&(datumParams[5]==0.0)
                &&(datumParams[6]==0.0)){
                    datumType = PJD_3PARAM;
                }else{
                    datumType = PJD_7PARAM;
                }
            }
            // its possible the following needs to be called after ellipse is set.
            if(datumType==PJD_3PARAM
            && datumParams[0]==0
            && datumParams[1]==0
            && datumParams[2]==0
            && params.getFloatParam("a") == 6378137
            && Math.abs(params.getFloatParam("es")-0.006694379990) < 0.000000000050 )/*WSG84/GRS80*/ {
                datumType=PJD_WGS84;
            }
            
        }
    }
    /** gets the datum definitttion as a string
     * @return the datum params, e.g. towsg84=...
     */
    public String getDefn(){
        return defn;
    }
    
    /** gets the String id of this datums ellipse
     * TODO: should probably return an Ellipse object instead
     * @return String containing the id of this datums ellipse
     */
    public String getEllipseID(){
        return ellipse_id;
    }
    
    
    private int getParamCount(){
        if(datumParams==null)return 0;
        return datumParams.length;
    }
    
    /** Get the datum params
     * @return a double[] of the datum's params
     */
    public double[] getParams(){
        return datumParams;
    }
    
    /** Get the default parameters for a standard datum
     * @param id The id string of the parameter to fetch defailts for
     * @return A string[] containg default param values
     */
    public static String[] getDefaultsForDatum(String id){
        for(int i=0;i<datums.length;i++){
            if(id.equalsIgnoreCase(datums[i][0])){
                return datums[i];
            }
        }
        return null;
    }
    
    /** List of standard datum defeintion
     */
    public static final String[][] datums={
        new String[] {"WGS84","towgs84=0,0,0","WGS84",""},
        new String[] {"GGRS87","towgs84=-199.87,74.79,246.62", "GRS80","Greek_Geodetic_Reference_System_1987"},
        new String[] {"NAD83","towgs84=0,0,0","GRS80","North_American_Datum_1983"},
        new String[] {"NAD27","nadgrids=conus,ntv1_can.dat","clrk66","North_American_Datum_1927"}
    };
    
}
