<?xml version="1.0" encoding="UTF-8"?>

<!-- Created by iant on 11 March 2002, 10:54 -->

<ccg:featureCollection xmlns:ccg="http://www.ccg.leeds.ac.uk/gml" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.ogc.net/gml/base">  
    <ccg:queryExtent>
        <gml:Box gid="1" srsName="http://?/epsg.xml#ESPG:4326">
          <gml:coordinates>
          0,0 30,30
          </gml:coordinates>
        </gml:Box>
    </ccg:queryExtent>
    <ccg:cartographicMember>
        <ccg:road fid="road1">   
            <ccg:description>The East Highway</ccg:description>
                <gml:centerLineOf>
                    <gml:LineString gid="1" srsName="http://?/epsg.xml#ESPG:4326">
                        <gml:coordinates>
                            0,25 30,25
                        </gml:coordinates>
                    </gml:LineString>
                </gml:centerLineOf>
                <ccg:size>5</ccg:size>
        </ccg:road>         
    </ccg:cartographicMember>
    <ccg:cartographicMember>
        <ccg:road fid="road2">
            <ccg:description>
                Local dirt track
            </ccg:description>
            <gml:centerLineOf>
                <gml:LineString gid="2" srsName="http://?/epsg.xml#ESPG:4326">
                    <gml:coordinates>
                        25,0 25,25
                    </gml:coordinates>
                </gml:LineString>
            </gml:centerLineOf>
            <ccg:size>3</ccg:size>
        </ccg:road>
    </ccg:cartographicMember>
    <ccg:cartographicMember>
        <ccg:river fid="river">
            <ccg:description>
                Tevior
            </ccg:description>
            <gml:centerLineOf>
                <gml:LineString gid="8" srsName="http://?/epsg.xml#ESPG:4326">
                    <gml:coordinates>
                        0,10 10,20 30,30
                    </gml:coordinates>
                </gml:LineString>
            </gml:centerLineOf>
        </ccg:river>
    </ccg:cartographicMember>
    <ccg:cartographicMember>
        <ccg:building fid="building1">
            <ccg:description>South Building</ccg:description>
            <ccg:buildingOutline>
                <gml:Polygon gid="3" srsName="http://?/epsg.xml#ESPG:4326">
                  <gml:outerBoundaryIs>
                    <gml:LinearRing>
                      <gml:coordinates>
                        12,4 12,8 18,8 18,4 12,4
                      </gml:coordinates>
                    </gml:LinearRing>
                  </gml:outerBoundaryIs>
                </gml:Polygon>
            </ccg:buildingOutline>
            <gml:location>
                <gml:Point>
                    <gml:coordinates>
                        15,6
                    </gml:coordinates>
                </gml:Point>
            </gml:location>
        </ccg:building>
    </ccg:cartographicMember>
    <ccg:cartographicMember>
        <ccg:building fid="building2">
            <ccg:description>North Building</ccg:description>
            <ccg:buildingOutline>
                <gml:Polygon gid="4" srsName="http://?/epsg.xml#ESPG:4326">
                  <gml:outerBoundaryIs>
                    <gml:LinearRing>
                      <gml:coordinates>
                        8,26 8,29 12,29 12,26 8,26
                      </gml:coordinates>
                    </gml:LinearRing>
                  </gml:outerBoundaryIs>
                </gml:Polygon>
            </ccg:buildingOutline>
            <gml:location>
                <gml:Point>
                    <gml:coordinates>
                        10,27.5
                    </gml:coordinates>
                </gml:Point>
            </gml:location>
        </ccg:building>
    </ccg:cartographicMember>
    <ccg:cartographicMember>
        <ccg:zone fid="zone1">
            <gml:Polygon gid="5" srsName="http://?/epsg.xml#ESPG:4326">
              <gml:outerBoundaryIs>
                <gml:LinearRing>
                  <gml:coordinates>
                    2,20 6,30 17,30 23,20 20,10 10,10 2,20
                  </gml:coordinates>
                </gml:LinearRing>
              </gml:outerBoundaryIs>
            </gml:Polygon>
        </ccg:zone>
    </ccg:cartographicMember>
    <ccg:cartographicMember>
        <ccg:railway fid="rail">
            <ccg:description>Main Railway</ccg:description>
            <gml:centerLineOf>
                <gml:LineString gid="8" srsName="http://?/epsg.xml#ESPG:4326">
                    <gml:coordinates>
                        5,30 20,15 30,15
                    </gml:coordinates>
                </gml:LineString>
            </gml:centerLineOf>
        </ccg:railway>
    </ccg:cartographicMember>
</ccg:featureCollection>

