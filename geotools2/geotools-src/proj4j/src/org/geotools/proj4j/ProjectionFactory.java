/*
 * ProjectionFactory.java
 *
 * Created on 21 February 2002, 17:12
 */

package org.geotools.proj4j;

/**
 *
 * @author  ian
 */
public class ProjectionFactory {
    
    /** Creates a new instance of ProjectionFactory */
    public ProjectionFactory() {
    }
    /** Creates a new instance of Projection from an argument set*/
    public static Projection createProjection(String[] args) throws ProjectionException{
        ParamSet params = new ParamSet();
        for(int i=0;i<args.length;i++){
            params.addParam(args[i]);
        }
        if(params.contains("init")){
            throw new ProjectionException("Init files not supported yet - please contribute code");
        }
        if(!params.contains("proj")){
            throw new ProjectionException("No proj parameter provided");
        }
        String proj = params.getStringParam("proj");
        proj=Character.toUpperCase(proj.charAt(0))+proj.substring(1); // convert to Java naming conventions
        Projection pin;
        try{
            pin = (Projection)Class.forName("org.geotools.proj4j.projections."+proj).newInstance();
        }catch(ClassNotFoundException e){
            throw new ProjectionException("Projection "+proj+" is not supported\n");
        }
        catch(Exception ex){
            throw new ProjectionException("Error creating instance of "+proj+"\n"+ex);
        }
        pin.setParams(params);
        return pin;
    }
}
