<StyledLayerDescriptor version="0.7.2">
<!-- a named layer is the basic building block of an sld document -->
<NamedLayer>
<Name>A Test Layer</Name>
<title>The title of the layer</title>
<abstract>
A styling layer used for the unit tests of sldstyler
</abstract>
<!-- with in a layer you have Named Styles -->
<UserStyle>
    <!-- again they have names, titles and abstracts -->
  <Name>MyStyle</Name>
    <!-- FeatureTypeStyles describe how to render different features -->
    <FeatureTypeStyle>
        <!-- this describes the featureTypeName to apply this style to e.g. road -->
      <FeatureTypeName>linefeature</FeatureTypeName>
       <!-- the actual rule describes the style -->
      <Rule>
        <!-- these are lines so we need a line symbolizer -->
        <LineSymbolizer>
           <!-- A stroke describes how the line looks -->
          <Stroke>
            <!-- the CssParameters describe the actual style 
                you can set stroke (color of line), stroke-width, stroke-opacity, stroke-linejoin
                stroke-linecap, stroke-dasharray and stroke-dashoffset -->
            <CssParameter name="stroke">#FF0000</CssParameter>
            <CssParameter name="stroke-width">10</CssParameter>
            <GraphicFill>
                <Graphic>
                    <mark>
                        <size>10</size>
                        <wellknownname>square</wellknownname>
                        <fill>
                            <CssParameter name="fill">#00FF00</CssParameter>
                        </fill>
                    </mark>
                </Graphic>
            </GraphicFill>    
          </Stroke>
        </LineSymbolizer>
        <!-- multiple linesyombolizers are applied one after the other -->
        <LineSymbolizer>
          <Stroke>
            <CssParameter name="stroke">#0000FF</CssParameter>
            <CssParameter name="width">
                <Add>
                    <literal>1</literal>
                    <literal>2</literal>
                </Add>
            </CssParameter>
          </Stroke>
        </LineSymbolizer>
      </Rule>
    </FeatureTypeStyle>
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
      <FeatureTypeName>polygon</FeatureTypeName>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <PolygonSymbolizer>
            <Stroke>
                <CssParameter name="stroke-width">
                <Literal>3 </Literal>
                </CssParameter>
                <CssParameter name="stoke">#0000FF</CssParameter>
          </Stroke>
        </PolygonSymbolizer>
        <!-- again these are applied in order so the following "overdraws" the previous one 
            giving the dashed blue and yellow line -->
        <PolygonSymbolizer>
        <!-- describes the fill of the polygon - if missing the polygon is empty -->  
          <Fill>
            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
            <CssParameter name="fill">#FF0000</CssParameter>
            <CssParameter name="fill-opacity">0.5</CssParameter>
            <GraphicFill>
                <Graphic>
                    <size>10</size>
                    <mark>
                        <wellknownname>circle</wellknownname>
                        <Fill>
                            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                            <CssParameter name="fill">#FFFF00</CssParameter>
                        </Fill>
                        <!-- stroke>
                            <CssParameter name="stroke-width">1</CssParameter>
                            <CssParameter name="stroke">#00FFFF</CssParameter>
                        </stroke -->
                    </mark>
                </Graphic>
            </GraphicFill>
          </Fill>
          <Stroke>
            <CssParameter name="stroke">#FFFF00</CssParameter>
            <CssParameter name="stroke-width">3</CssParameter>
            <CssParameter name="stroke-dasharray">1 2 </CssParameter>
          </Stroke>
        </PolygonSymbolizer>
        
      </Rule>
    </FeatureTypeStyle>
    <FeatureTypeStyle>
      <FeatureTypeName>polygontest</FeatureTypeName>
      <Rule>
        
        <PolygonSymbolizer>
        <!-- describes the fill of the polygon - if missing the polygon is empty -->  
          <Fill>
            <GraphicFill>
                <graphic>
                    <size>20</size>
                    <externalGraphic>
                        <onLineResource 
                            xmlns:xlink="http://www.w3.org/1999/xlink"
                            xlink:type="simple"
                            xlink:href="file:///d:/ian/development/geotools2/geotools-src/sldstyling/tests/unit/testData/blob.gif"/>
                        <format>image/gif</format>
                    </externalGraphic>
                    
                    <mark>
                        <wellknownname>triangle</wellknownname>
                        <Fill>
                    
                            <CssParameter name="fill">#FF00FF</CssParameter>
                            <CssParameter name="fill-opacity">0.5</CssParameter>
                        </Fill>
                    </mark>
                </graphic>
            </GraphicFill>
            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
            <CssParameter name="fill">#FF00FF</CssParameter>
            <CssParameter name="fill-opacity">0.5</CssParameter>
          </Fill>
          <Stroke>
            <!-- default stroke #0000000 -->
          </Stroke>
        </PolygonSymbolizer>
        
      </Rule>
    </FeatureTypeStyle>
    <FeatureTypeStyle>
        <FeatureTypeName>pointfeature</FeatureTypeName>
        <rule>
            <PointSymbolizer>
                <graphic>
                    <size>10</size>
                    
                    <rotation>45.0</rotation>
                    <externalGraphic>
                        <onLineResource 
                            xmlns:xlink="http://www.w3.org/1999/xlink"
                            xlink:type="simple"
                            xlink:href="file:///d:/ian/development/geotools2/geotools-src/sldstyling/tests/unit/testData/blob.gif"/>
                        <format>image/gif</format>
                    </externalGraphic>
                    <mark>
                    
                    <!-- since cross is not implemented yet should draw next mark -->
                        <wellknownname>triangle</wellknownname>
                        <Fill>
                            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                            <CssParameter name="fill">#FF00FF</CssParameter>
                            <CssParameter name="fill-opacity">0.5</CssParameter>
                        </Fill>
                    </mark>
                    <mark>
                        <wellknownname>square</wellknownname>
                        <Fill>
                            <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                            <CssParameter name="fill">#00FF00</CssParameter>
                        </Fill>
                    </mark>
                </graphic>
            </PointSymbolizer>
        </rule>
    </FeatureTypeStyle>
</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>