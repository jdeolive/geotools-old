/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.styling;
import java.net.*;
import java.io.*;
import org.apache.log4j.Category;
/**
 * @version $Id: DefaultExternalGraphic.java,v 1.2 2002/06/04 16:50:55 loxnard Exp $
 * @author Ian Turton, CCG
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
    
    /**
     * Setter for property Format.
     * @param Format New value of property Format.
     */
    public void setFormat(java.lang.String format) {
        this.format = format;
    }
    
    /**
     * Setter for property location.
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
