/*
 * ParamSet.java
 *
 * Created on 20 February 2002, 01:58
 */

package org.geotools.proj4j;

import java.util.Hashtable;

/**
 *
 * @author  James Macgill
 */
public class ParamSet {
Hashtable params = new Hashtable();

    /** Creates a new instance of ParamSet */
    public ParamSet() {
    }
    
    public void addParam(String param){
        int split = param.indexOf('=');
        if(split>=0){
            params.put(param.substring(0,split),param.substring(split+1));
        }
        else{
            params.put(param,param);
        }
    }
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
        
          
    
    public void addParam(String param,String value){
        params.put(param,value);
    }
    
    public boolean contains(String paramName){
     return params.get(paramName)!=null;   
    }

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
    
    public String getStringParam(String paramName){
        Object value = params.get(paramName);
        if(value!=null)return value.toString();
        return null;
    }
    
    public double getRadiansParam(String paramName){
        Object value = params.get(paramName);
        if(value!=null)return Functions.dmsToR(value.toString());
        return 0;
    }
    public String toString(){
        return params.toString();
    }
}
