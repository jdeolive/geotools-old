/*
 * DefaultExternalGraphic.java
 *
 * Created on 29 May 2002, 11:11
 */

package org.geotools.styling;
import java.net.*;
import java.io.*;
import org.apache.log4j.Category;
/**
 *
 * @author  iant
 */
public class DefaultExternalGraphic implements ExternalGraphic {
    URL location = null;
    String format = "";
    
    private static Category _log = Category.getInstance("defaultcore.styling");
    /** Creates a new instance of DefaultExternalGraphic */
    public DefaultExternalGraphic(){
    }
    public DefaultExternalGraphic(String uri) {
        try{
            setLocation(new URL(uri));
            URLConnection conn = location.openConnection();
            format = conn.getContentType();
        } catch (IOException e){
        _log.info("Exception opening uri "+uri+"\n"+e);
        }
    }
    public void setURI(String uri){
        try{
            setLocation(new URL(uri));
        } catch (MalformedURLException e){
            _log.info("Exception setting uri "+uri+"\n"+e);
        }
    }
    /**
     * Provides the format of the external graphic.
     * @return The format of the external graphic.  Reported as its MIME type
     * in a String object.
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * Provides the URL for where the external graphic resouce can be located.
     * @return The URL of the ExternalGraphic
     */
    public URL getLocation() {
        return location;
    }
    
    /** Setter for property Format.
     * @param Format New value of property Format.
     */
    public void setFormat(java.lang.String format) {
        this.format = format;
    }
    
    /** Setter for property location.
     * @param location New value of property location.
     */
    public void setLocation(java.net.URL location) {
        this.location = location;
    }
    
    public InputStream getGraphic(){
        try{
            return location.openStream();
        } catch (IOException e){
            return null;
        }
    }
    
}
