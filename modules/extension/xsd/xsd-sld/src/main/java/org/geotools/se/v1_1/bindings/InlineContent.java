package org.geotools.se.v1_1.bindings;

/**
 * Container for se:InlineContent element.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class InlineContent {

    String encoding;
    Object content;
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setContent(Object content) {
        this.content = content;
    }
    
    public Object getContent() {
        return content;
    }
}
