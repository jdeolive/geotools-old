/*
 * TextFeatureFormatter.java
 *
 * Created on 11 November 2002, 11:12
 */

package org.geotools.wms.gtserver;

import com.vividsolutions.jts.geom.Geometry;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalFeatureException;

/**
 *
 * @author  jamesm
 */
public class TextFeatureFormatter implements org.geotools.wms.WMSFeatureFormatter {
    
    /** Creates a new instance of TextFeatureFormatter */
    public TextFeatureFormatter() {
    }
    
    /** 
     * Formats the given array of Features as this Formatter's mime-type and writes it to the given OutputStream
     * @task HACK: Exception handleling is not elegant
     */
    public void formatFeatures(Feature[] features, OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        try{
            for(int i = 0; i < features.length; i++) {
                FeatureType schema = features[i].getSchema();
                AttributeType[] types = schema.getAllAttributeTypes();
                writer.println("------");
                for(int j = 0; j < types.length; j++) {
                    if(Geometry.class.isAssignableFrom(types[j].getType())){
                        writer.println(types[j].getName()+ " = [GEOMETRY]");
                    }
                    else{
                        writer.println( types[j].getName() + " = " + features[i].getAttribute(types[j].getName()));
                    }
                }
            }
        }
        catch(IllegalFeatureException ife){
            writer.println("Unable to generate information " + ife);
        }
        writer.flush();
    }
    
    /** Gets the mime-type of the stream written to by formatFeatures()
     *
     */
    public String getMimeType() {
        return ("text/plain");
    }
    
}
