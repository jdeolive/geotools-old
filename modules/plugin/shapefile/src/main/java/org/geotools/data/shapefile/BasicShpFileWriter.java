/**
 * 
 */
package org.geotools.data.shapefile;


public final class BasicShpFileWriter implements FileWriter {
    private String id;

    public BasicShpFileWriter( String id ) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}