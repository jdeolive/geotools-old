package org.geotools.data.sde;

import java.io.IOException;

import org.geotools.data.FeatureWriter;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

/**
 * will become to life shortly
 * @author Gabriel Roldan
 * @version $Id: SdeFeatureWriter.java,v 1.5 2004/01/09 16:58:23 aaime Exp $
 */

public class SdeFeatureWriter
 implements FeatureWriter
{
  private SdeFeatureReader reader;
  private FeatureType schema;

  private Feature currentFeature;

  public SdeFeatureWriter(SdeFeatureReader reader)
  {
    this.reader = reader;
  }

  public FeatureType getFeatureType()
  {
    if(schema == null)
    {
      schema = reader.getFeatureType();
    }
    return schema;
  }

  public Feature next() throws IOException
  {
    /**@todo Implement this org.geotools.data.FeatureWriter method*/
    throw new java.lang.UnsupportedOperationException("Method not yet implemented.");
  }

  public void remove() throws IOException
  {
    /**@todo Implement this org.geotools.data.FeatureWriter method*/
    throw new java.lang.UnsupportedOperationException("Method remove() not yet implemented.");
  }
  public void write() throws IOException
  {
    /**@todo Implement this org.geotools.data.FeatureWriter method*/
    throw new java.lang.UnsupportedOperationException("Method write() not yet implemented.");
  }
  public boolean hasNext() throws IOException
  {
    /**@todo Implement this org.geotools.data.FeatureWriter method*/
    throw new java.lang.UnsupportedOperationException("Method hasNext() not yet implemented.");
  }
  public void close() throws IOException
  {
    /**@todo Implement this org.geotools.data.FeatureWriter method*/
    throw new java.lang.UnsupportedOperationException("Method close() not yet implemented.");
  }

}