<?xml version="1.0" encoding="UTF-8"?>

<!-- Created by iant on 11 March 2002, 10:54 -->

<osgb:featureCollection xmlns:osgb="http://www.ordanacesurvey.co.uk/xml/namespaces/osgb" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.ogc.net/gml/base">  

<osgb:queryExtent>
    <gml:Box gid="1" srsName="http://?/epsg.xml#ESPG:4326">
      <gml:coordinates>
      -90.5485,16.2633  32.5485,54.2633  
      </gml:coordinates>
    </gml:Box>
</osgb:queryExtent>
<osgb:cartographicMember>
    <osgb:polygon fid="polygon1">   
        <osgb:description>Some text to describe the polygon</osgb:description>
        <gml:Polygon gid="1" srsName="http://?/epsg.xml#ESPG:4326">
          <gml:outerBoundaryIs>
            <gml:LinearRing>
              <gml:coordinates>
                32.5485,16.2633 -90.5485,24.2633 -85.5485,34.2633 30,30 32.5485,16.2633
              </gml:coordinates>
            </gml:LinearRing>
          </gml:outerBoundaryIs>
          <gml:innerBoundaryIs>
            <gml:LinearRing>
              <gml:coordinates>
                -72,24 -70,24 -70,25 -72,24
              </gml:coordinates>
            </gml:LinearRing>
          </gml:innerBoundaryIs>
        </gml:Polygon>
    </osgb:polygon>    
</osgb:cartographicMember>
<osgb:cartographicMember>
    <osgb:polyline fid="line1">
        <osgb:description>
            Lots of text to describe this line,
            infact so much that it goes over 
            three lines.
        </osgb:description>
        
    <gml:LineString gid="7" srsName="http://?/epsg.xml#ESPG:4326">
        <gml:coordinates>
            32.5485,16.2633 -78.5485,30.2633 -20,43 10,42 23,50
        </gml:coordinates>
    </gml:LineString>
        </osgb:polyline>
</osgb:cartographicMember>
<osgb:cartographicMember>
    <osgb:point fid="point1">
        <gml:Point gid="1" srsName="http://?/epsg.xml#ESPG:4326">
          <gml:coordinates>
            -88.5485,37.2633
          </gml:coordinates>
        </gml:Point>
    </osgb:point>
</osgb:cartographicMember>
<osgb:cartographicMember>
    <osgb:point fid="point2">
        <gml:Point gid="2" srsName="http://?/epsg.xml#ESPG:4326">
          <gml:coordinates>
            -60.5485,24
          </gml:coordinates>
        </gml:Point>
    </osgb:point>
</osgb:cartographicMember>
<osgb:cartographicMember>
    <osgb:point fid="point3">
        <gml:Point gid="3" srsName="http://?/epsg.xml#ESPG:4326">
          <gml:coordinates>
            -71,24.5
          </gml:coordinates>
        </gml:Point>
    </osgb:point>
</osgb:cartographicMember>
<osgb:cartographicMember>
    <osgb:multiline fid="multiline1">
        <gml:MultiLineString srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
            <gml:lineStringMember>
                <gml:LineString>
                  <gml:coordinates>
                        10,48, 10,21, 10,0
                  </gml:coordinates>
                </gml:LineString>
            </gml:lineStringMember>
            <gml:lineStringMember>
                <gml:LineString>
                  <gml:coordinates>
                        10,48, 10,21, 10,0
                  </gml:coordinates>
                </gml:LineString>
            </gml:lineStringMember>
        </gml:MultiLineString>
    </osgb:multiline>
</osgb:cartographicMember> 
<osgb:cartographicMember>
    <osgb:multipolygon fid="109">
        <gml:MultiPolygon srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
            <gml:polygonMember>
                <gml:Polygon gid="1" srsName="http://?/epsg.xml#ESPG:4326">
                  <gml:outerBoundaryIs>
                    <gml:LinearRing>
                      <gml:coordinates>
                        32.5485,16.2633 -90.5485,24.2633 -85.5485,34.2633 30,30 32.5485,16.2633
                      </gml:coordinates>
                    </gml:LinearRing>
                  </gml:outerBoundaryIs>
                  <gml:innerBoundaryIs>
                    <gml:LinearRing>
                      <gml:coordinates>
                        -72,24 -70,24 -70,25 -72,24
                      </gml:coordinates>
                    </gml:LinearRing>
                  </gml:innerBoundaryIs>
                </gml:Polygon>
            </gml:polygonMember>
        </gml:MultiPolygon>
    </osgb:multipolygon>     
</osgb:cartographicMember>
</osgb:featureCollection>

