package org.geotools.data.sde;

import org.geotools.data.*;
import org.geotools.feature.*;

import java.io.IOException;

/**
 * will become to life shortly
 * @author Gabriel Roldan
 * @version $Id: SdeFeatureWriter.java,v 1.4 2003/11/17 17:12:41 groldan Exp $
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