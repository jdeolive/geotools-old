package org.geotools.referencing.factory.epsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class SqlScripReader {
    boolean fetched = true;
    StringBuilder builder = new StringBuilder();
    BufferedReader reader;

    public SqlScripReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }
    
    public boolean hasNext() throws IOException {
        // do we have an un-fetched command?
        if(!fetched) {
            return builder.length() > 0;
        }
        
        builder.setLength(0);
        String line = null;
        while((line = reader.readLine()) != null) {
            line = line.trim();
            if(!"".equals(line))
                builder.append(line).append("\n");
            if(line.endsWith(";")) {
                fetched = false;
                break;
            }
        }
        
        if(line == null && builder.length() > 0) {
            throw new IOException("The file ends with a non ; terminated command");
        }
        
        return line != null;
    }
    
    public String next() throws IOException  {
        if(fetched)
            throw new IOException("hasNext was not called, or was called and it returned false");
            
        fetched = true;
        return builder.toString();
    }
    
    public void dispose() {
        try {
            reader.close();
        } catch(IOException e) {
            // never mind
        }
    }
    

}
