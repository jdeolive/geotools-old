/*
 * ParamSet.java
 *
 * Created on 20 February 2002, 01:58
 */

package org.geotools.proj4j;

import java.util.Hashtable;

/** 
 * Holds a set of parameters.<br>
 * The parameters are stored as strings but can be returned as primitive types using different getter methods.<br>
 * Typically, they are stored in the form 'name=value'.<br>
 *
 * @author James Macgill
 * @version $Revision: 1.5 $ $Date: 2002/03/05 23:53:27 $
 */
public class ParamSet {
    /**
     * Used to store the parameter values.
     */
    protected Hashtable params = new Hashtable();

    /** 
     * Creates a new instance of ParamSet.
     */
    public ParamSet() {
    }
    
    /**
     * Adds a new parameter to the set.<br>
     * If the parameter is already set, this will replace its existing value.<br>
     * Values can be given either in the form 'name=value' or simply 'name' if it is a flag parameter.
     *
     * @param param The param to set in the form 'name=value' or 'name'.
     */
    public void addParam(String param){
        int split = param.indexOf('=');
        if(split>=0){
            params.put(param.substring(0,split),param.substring(split+1));
        }
        else{
            params.put(param,param);
        }
    }
    
    /** 
     * Adds a new parameter and value if and only if the parameter has not already been set.
     *
     * @param param The param to set in the form 'name=value' or 'name'.
     */    
    public void addParamIfNotSet(String param){
        String arg_1,arg_2;
        int split = param.indexOf('=');
        if(split>=0){
            arg_1=param.substring(0,split);
            arg_2=param.substring(split+1);
        }else{
            arg_1=arg_2=param;
        }
        if(!this.contains(arg_1)){
            params.put(arg_1,arg_2);
        }
    }           
    
    /**
     * Adds a parameter using a pair of strings.<br>
     * If the parameter is already set, this will replace its existing value.<br>
     *
     * @param param The name of the parameter to add.
     * @param value The value of the parameter.
     */
    public void addParam(String param,String value){
        params.put(param,value);
    }
    
    /**
     * Checks to see if the named parameter has been set.<br>
     * The param may have been set without a value.  This method will still return true.<br>
     * Note: the test is case sensitive.<br>
     *
     * @param paramName The parameter to check.
     * @return True if paramName exists in this set.
     */
    public boolean contains(String paramName){
     return params.get(paramName)!=null;   
    }

    /**
     * Gets the value associated with a given parameter as an integer.<br>
     *
     * @param paramName The parameter to fetch the value for.
     * @return The int value of the parameter, if possible, otherwise 0.<br>  If the parameter is not set or if it cannot be parsed to an int, 0 is returned.
     */
    public int getIntegerParam(String paramName){
        Object value = params.get(paramName);
        if(value!=null){
            try{
                return Integer.parseInt(value.toString());
            }
            catch(NumberFormatException nfe){
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * Gets the value associated with a given parameter as a float.<br>
     * 
     * @param paramName The parameter to fetch the value for.
     * @return The float value of the parameter, if possible, otherwise 0.<br>  If the parameter is not set or if it cannot be parsed to a float, 0 is returned.
     */
    public float getFloatParam(String paramName){
        Object value = params.get(paramName);
        if(value!=null){
            try{
                return Float.parseFloat(value.toString());
            }
            catch(NumberFormatException nfe){
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * Gets the value associated with the given parameter as a String.<br>
     *
     * @param paramName The parameter to fetch the value for.
     * @return The value of the parameter as a String.
     */
    public String getStringParam(String paramName){
        Object value = params.get(paramName);
        if(value!=null)return value.toString();
        return null;
    }
    
    /**
     * Gets the value associated with the given parameter in radians as a double.<br>
     * Strings in the form dms are automatically converted.  Valid examples include:<br>
     * 12d14'20"E<br>
     * 30.4<br>
     * 0.3R<br>
     * {@link Functions#dmsToR(String) dmsToR()} for more details.
     *
     * @param paramName The parameter to fetch the value for.
     * @return The value of the parameter as a double.
     */
    public double getRadiansParam(String paramName){
        Object value = params.get(paramName);
        if(value!=null)return Functions.dmsToR(value.toString());
        return 0;
    }
    
    /**
     * Dumps the list of parameters and values into a string.
     * @return String containing all currently set params and values.
     */
    public String toString(){
        return params.toString();
    }
}
