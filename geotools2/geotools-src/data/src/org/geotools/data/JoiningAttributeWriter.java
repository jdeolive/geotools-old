/*
 * Created on 28/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.AttributeType;

/** Provides ...
 * 
 *  @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public class JoiningAttributeWriter implements AttributeWriter {
    private AttributeWriter[] writers;
    private int[] index;
    private AttributeType[] metaData;

    /**
     * 
     */
    public JoiningAttributeWriter(AttributeWriter[] writers) {
        this.writers = writers;
        metaData = joinMetaData(writers);
    }

    private AttributeType[] joinMetaData(AttributeWriter[] writers) {
        int total = 0;
        index = new int[writers.length];
        for (int i = 0, ii = writers.length; i < ii; i++) {
            index[i] = total;
            total += writers[i].getAttributeCount();
        }
        AttributeType[] md = new AttributeType[total];
        int idx = 0;
        for (int i = 0, ii = writers.length; i < ii; i++) {
            for (int j = 0, jj = writers[i].getAttributeCount(); j < jj; j++) {
                md[idx] = writers[i].getAttributeType(j);
                idx++;
            }
        }
        return md;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AttributeWriter#close()
     */
    public void close() throws IOException {
        IOException dse = null;
        for (int i = 0, ii = writers.length; i < ii; i++) {
            try {
                writers[i].close();
            } catch (IOException e) {
                dse = e;
            }
        }
        if (dse != null)
            throw dse;

    }

    public boolean hasNext() throws IOException {
        for (int i = 0, ii = writers.length; i < ii; i++) {
            if (writers[i].hasNext()) {
                System.out.println("This has next" + writers[i]);
                return true;
            }
        }
        return false;
    }

    public void next() throws IOException {
        System.out.println("Joining next()");
        for (int i = 0, ii = writers.length; i < ii; i++) {            
            //if (writers[i].hasNext()) Dont want to check this, need to be able to insert
                writers[i].next();
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AttributeWriter#write(int, java.lang.Object)
     */
    public void write(int position, Object attribute) throws IOException {
        AttributeWriter writer = null;
        for (int i = index.length - 1; i >= 0; i--) {
            if (position >= index[i]) {
                position -= index[i];
                writer = writers[i];
                break;
            }
        }
        if (writer == null)
            throw new ArrayIndexOutOfBoundsException(position);

        writer.write(position, attribute);
    }
    
    public int getAttributeCount() {
        return metaData.length;
    }

    public AttributeType getAttributeType(int i) {
        return metaData[i];
    }
}
