package org.geotools.data.arcsde;

import org.geotools.data.*;
import org.geotools.feature.*;
import java.io.IOException;
import com.esri.sde.sdk.client.*;


public class ArcSDEAttributeReader implements AttributeReader
{
  private ArcSDEQuery query;
  private FeatureType schema;
  private SeRow currentRow;
  private SeShape currentShape;
  private GeometryBuilder geometryBuilder;
  private int geometryTypeIndex = -1;

  StringBuffer fidPrefix;
  int fidPrefixLen;

  private boolean hasNextAlreadyCalled = false;

  public ArcSDEAttributeReader(ArcSDEQuery query)
  throws IOException
  {
    this.query = query;
    this.schema = query.getSchema();

    this.fidPrefix = new StringBuffer(schema.getTypeName()).append(".");
    this.fidPrefixLen = fidPrefix.length();

    Class geometryClass = schema.getDefaultGeometry().getType();
    this.geometryBuilder = GeometryBuilder.builderFor(geometryClass);
    String geometryTypeName = schema.getDefaultGeometry().getName();
    AttributeType[]types = schema.getAttributeTypes();

    for(int i = 0; i < types.length; i++)
      if(types[i].getName().equals(geometryTypeName))
      {
        geometryTypeIndex = i;
        break;
      }
  }

  public int getAttributeCount()
  {
    return schema.getAttributeCount();
  }

  public AttributeType getAttributeType(int index) throws ArrayIndexOutOfBoundsException
  {
    return schema.getAttributeType(index);
  }

  public void close() throws IOException
  {
    query.close();
  }

  public boolean hasNext() throws IOException
  {
    if(!hasNextAlreadyCalled)
    {
      try {
        currentRow = query.fetch();
        hasNextAlreadyCalled = true;
        //ensure closing the query to release the connection, may be the
        //user is not so smart to doing it itself
        if(currentRow == null)
          query.close();
        else
          currentShape = currentRow.getShape(geometryTypeIndex);
      }catch (SeException ex) {
        query.close();
        throw new DataSourceException("Fetching row:" + ex.getMessage(), ex);
      }
    }

    return currentRow != null;
  }

  public void next() throws IOException
  {
    hasNextAlreadyCalled = false;
    if(currentRow == null)
      throw new DataSourceException("There are no more rows");
  }

  public Object read(int index) throws IOException, ArrayIndexOutOfBoundsException
  {
    try {
      if(index == geometryTypeIndex)
        return geometryBuilder.construct(currentShape);
      else
        return currentRow.getObject(index);
    }
    catch (SeException ex) {
      throw new DataSourceException("Error retrieveing column " + index +
                                    ": " + ex.getMessage(), ex);
    }
  }

  public String readFID() throws IOException
  {
    fidPrefix.setLength(fidPrefixLen);
    try {
      fidPrefix.append(currentShape.getFeatureId().longValue());
    }
    catch (SeException ex) {
      throw new DataSourceException("Can't read FID value: " +
                                    ex.getMessage(), ex);
    }
    return fidPrefix.toString();
  }
}