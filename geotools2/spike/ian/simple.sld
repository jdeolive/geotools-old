<StyledLayerDescriptor version="0.7.2">
<!-- a named layer is the basic building block of an sld document -->
<NamedLayer>
<Name>A Simple Layer</Name>
<title>A Small Town with a river running though it</title>
<abstract>
This is for testing only, it has two 'buildings' and two 
roads.  The roads meet at a junction and a river runs 
under them.  There is a boundary of some sort round
one part of the map that includes one but not both of the
buildings.  One road and the river run through the zone.
</abstract>
<!-- with in a layer you have Named Styles -->
<UserStyle>
    <!-- again they have names, titles and abstracts -->
  <Name>SimpleStyle</Name>
    <!-- FeatureTypeStyles describe how to render different features -->
    
    <FeatureTypeStyle>
        <!-- this describes the featureTypeName to apply this style to i.e. river -->
      <FeatureTypeName>river</FeatureTypeName>
       <!-- the actual rule describes the style -->
      <Rule>
        <!-- these are lines so we need a line symbolizer -->
        <LineSymbolizer>
           <!-- A stroke describes how the line looks -->
          <Stroke>
            <!-- the CssParameters describe the actual style 
                you can set stroke (color of line), stroke-width, stroke-opacity, stroke-linejoin
                stroke-linecap, stroke-dasharray and stroke-dashoffset -->
            <CssParameter name="stroke">#0000FF</CssParameter>
            <CssParameter name="stroke-width">3</CssParameter>
          </Stroke>
        </LineSymbolizer>
      </Rule>
    </FeatureTypeStyle>
    <FeatureTypeStyle>
        <FeatureTypeName>railway</FeatureTypeName>
        <rule>
            <LineSymbolizer>
           <!-- A stroke describes how the line looks -->
          <Stroke>
            <!-- the CssParameters describe the actual style 
                you can set stroke (color of line), stroke-width, stroke-opacity, stroke-linejoin
                stroke-linecap, stroke-dasharray and stroke-dashoffset -->
            <CssParameter name="stroke">#FF0000</CssParameter>
            <CssParameter name="stroke-width">10</CssParameter>
            <GraphicStroke>
                <Graphic>
                    <size>10</size>
                    <ExternalGraphic>
                        <onLineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://www.ccg.leeds.ac.uk/ian/geotools/icons/rail.gif"/>
                        <format>image/gif</format>
                    </ExternalGraphic>
                    <mark>
                        <wellknownname>arrow</wellknownname>
                        <fill>
                            <CssParameter name="fill">#0000FF</CssParameter>
                        </fill>
                    </mark>
                </Graphic>
            </GraphicStroke>    
          </Stroke>
        </LineSymbolizer>
    </rule>
    </FeatureTypeStyle>
    <!-- part one of the styling for the roads, this provides the outer case -->
    <FeatureTypeStyle>
        <!-- this describes the featureTypeName to apply this style to e.g. road -->
      <FeatureTypeName>road</FeatureTypeName>
       <!-- the actual rule describes the style -->
      <Rule>
        <Filter>
            <PropertyIsGreaterThan>
                <PropertyName>size</PropertyName>
                <Literal>4</Literal>
            </PropertyIsGreaterThan>
        </Filter>
        <!-- these are lines so we need a line symbolizer -->
        <LineSymbolizer>
           <!-- A stroke describes how the line looks -->
          <Stroke>
            <!-- the CssParameters describe the actual style 
                you can set stroke (color of line), stroke-width, stroke-opacity, stroke-linejoin
                stroke-linecap, stroke-dasharray and stroke-dashoffset -->
            <CssParameter name="stroke">#FF0000</CssParameter>
            <CssParameter name="stroke-width">
                <add>
                    <propertyName>size</propertyName>
                    <literal>2</literal>
                </add>
            </CssParameter>
          </Stroke>
        </LineSymbolizer>
    </Rule>
</FeatureTypeStyle>
<FeatureTypeStyle>
        <FeatureTypeName>road</FeatureTypeName>
        <Rule>
        <LineSymbolizer>
          <Stroke>
            <CssParameter name="stroke">#88FF00</CssParameter>
            <CssParameter name="width"><propertyName>size</propertyName></CssParameter>
          </Stroke>
        </LineSymbolizer>
      </Rule>
    </FeatureTypeStyle>
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
      <FeatureTypeName>building</FeatureTypeName>
      <Rule>
        <Filter>
            <Within>
                <PropertyName>buildingOutline</PropertyName>
                <gml:Box gid="1" srsName="http://?/epsg.xml#ESPG:4326">
                    <gml:coordinates>
                      0,0 30,15
                     </gml:coordinates>
                </gml:Box>
            </Within>
        </Filter>
        <PolygonSymbolizer>
            <Stroke>
                <CssParameter name="stroke-width">3</CssParameter>
                <CssParameter name="stoke">#330000</CssParameter>
          </Stroke>
          <Fill>
            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
            <CssParameter name="fill">#00FF00</CssParameter>
            
          </Fill>
        </PolygonSymbolizer>
      </Rule>
      <Rule>
      <ElseFilter/>
        <!-- like a linesymbolizer but with a fill too -->
        <PolygonSymbolizer>
            <Stroke>
                <CssParameter name="stroke-width">3</CssParameter>
                <CssParameter name="stoke">#330000</CssParameter>
          </Stroke>
          <Fill>
            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
            <CssParameter name="fill">#FF0000</CssParameter>
            
          </Fill>
        </PolygonSymbolizer>
      </Rule>
  </FeatureTypeStyle>
  <FeatureTypeStyle>
    <FeatureTypeName>building</FeatureTypeName>
      <Rule>
        <TextSymbolizer>
                <Geometry>
                    <PropertyName>location</PropertyName>
                </Geometry>
                <Label><PropertyName>description</PropertyName></Label>
                <Font>
                    <CssParameter name="font-family">Times New Roman</CssParameter>
                    <CssParameter name="font-Size">
                        <Literal>10</Literal>
                    </CssParameter>
                </Font>
                <Font>
                    <CssParameter name="font-family">Arial</CssParameter>
                    <CssParameter name="font-Size">
                        <Literal>10</Literal>
                    </CssParameter>
                </Font>
                
                <Fill>
                    <CssParameter name="fill">#000000</CssParameter>
                </Fill>
                <Halo/>
            </TextSymbolizer>
        </Rule>
            
    </FeatureTypeStyle>
    <FeatureTypeStyle>
      <FeatureTypeName>zone</FeatureTypeName>
      <Rule>
        
        <PolygonSymbolizer>
            <Fill>
            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
            <CssParameter name="fill">#FF0000</CssParameter>
            <CssParameter name="fill-opacity">0.5</CssParameter>
            <GraphicFill>
                <Graphic>
                    <size>8</size>
                    <mark>
                        <wellknownname>circle</wellknownname>
                        <Fill>
                            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                            <CssParameter name="fill">#FFFF00</CssParameter>
                            <CssParameter name="fill-opacity">0.5</CssParameter>
                        </Fill>
                        <!-- stroke>
                            <CssParameter name="stroke-width">1</CssParameter>
                            <CssParameter name="stroke">#00FFFF</CssParameter>
                        </stroke -->
                    </mark>
                </Graphic>
            </GraphicFill>
          </Fill>
        <!-- describes the fill of the polygon - if missing the polygon is empty -->  
          <!-- Fill>
          <CssParameter name="fill">#FF0000</CssParameter>
            <CssParameter name="fill-opacity">0.5</CssParameter>
          <GraphicFill>
            <graphic>
                <size>8</size>
                <mark>
                    <wellknownname>circle</wellknownname>
                    <fill>
                    <CssParameters allowed are fill (the color) and fill-opacity>
                    <CssParameter name="fill">#FFFF88</CssParameter>
                    <CssParameter name="fill-opacity">0.5</CssParameter>
                    </fill>
                </mark>
            </graphic>
            </GraphicFill>
          </Fill -->
          <Stroke>
            <CssParameter name="stroke-dasharray">2 2 1 2</CssParameter>
          </Stroke>
        </PolygonSymbolizer>
      </Rule>
    </FeatureTypeStyle> 
</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>