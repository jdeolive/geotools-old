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
  <Name>polyshp</Name>
  <Title>Leeds Test User Style</Title>
    <!-- FeatureTypeStyles describe how to render different features -->
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
      <FeatureTypeName>feature</FeatureTypeName>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsLessThan>
           <PropertyName>color</PropertyName>
           <Literal>0.001</Literal>
          </PropertyIsLessThan>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#FFFFFF</CssParameter>
           </Fill> 
           <Stroke>                                                                                                                 
             <CssParameter name="stroke">#000000</CssParameter>
             <CssParameter name="stroke-width">5.0</CssParameter>
           </Stroke>    
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>color</PropertyName>
            <LowerBoundary>
              <Literal>0.001</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>10.0</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#7FFF00</CssParameter>
           </Fill>     
           <Stroke>
             <CssParameter name="stroke">#000000</CssParameter>
             <CssParameter name="stroke-width">5.0</CssParameter>
           </Stroke>    
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>color</PropertyName>
            <LowerBoundary>
              <Literal>10.0</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>20.0</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#ADFF2F</CssParameter>
           </Fill>     
           <Stroke>
             <CssParameter name="stroke">#000000</CssParameter>
             <CssParameter name="stroke-width">5.0</CssParameter>
           </Stroke>    
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>color</PropertyName>
            <LowerBoundary>
              <Literal>20.0</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>40.0</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#FFFF00</CssParameter>
           </Fill>
           <Stroke>
             <CssParameter name="stroke">#000000</CssParameter>
             <CssParameter name="stroke-width">2.0</CssParameter>
           </Stroke>         
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>color</PropertyName>
            <LowerBoundary>
              <Literal>40.0</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>60.0</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#FFA500</CssParameter>
           </Fill>     
           <Stroke>
             <CssParameter name="stroke">#000000</CssParameter>
             <CssParameter name="stroke-width">2.0</CssParameter>
           </Stroke>    
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>color</PropertyName>
            <LowerBoundary>
              <Literal>60.0</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>80.0</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#FF1493</CssParameter>
           </Fill>     
           <Stroke>
             <CssParameter name="stroke">#000000</CssParameter>
             <CssParameter name="stroke-width">2.0</CssParameter>
           </Stroke>    
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsGreaterThan>
            <PropertyName>color</PropertyName>
           <Literal>80.0</Literal>
          </PropertyIsGreaterThan>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#ff0000</CssParameter>
           </Fill>     
           <Stroke>
             <CssParameter name="stroke">#000000</CssParameter>
             <CssParameter name="stroke-width">2.0</CssParameter>
           </Stroke>    
        </PolygonSymbolizer>
      </Rule>
    </FeatureTypeStyle>
</UserStyle>

<UserStyle>
    <!-- again they have names, titles and abstracts -->
  <Name>PointTest</Name>
  <Title>Leeds Test Point User Style</Title>
    <!-- FeatureTypeStyles describe how to render different features -->
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
      <FeatureTypeName>feature</FeatureTypeName>
      <Rule>
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsLessThan>
           <PropertyName>TestValue</PropertyName>
           <Literal>0.001</Literal>
          </PropertyIsLessThan>
        </Filter>
        <PointSymbolizer>
           <Graphic>
             <Mark>
               <WellKnownName>star</WellKnownName>
               <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#7FFF00</CssParameter>
              </Fill>
            </Mark>
            <Size>2.0</Size>  
          </Graphic>
        </PointSymbolizer>
      </Rule>
      <Rule>
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>TestValue</PropertyName>
            <LowerBoundary>
              <Literal>0.001</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>5.0</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PointSymbolizer>
           <Graphic>
             <Mark>
               <WellKnownName>star</WellKnownName>
               <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#7FFF00</CssParameter>
              </Fill>
            </Mark>
            <Size>3.0</Size>  
          </Graphic>
        </PointSymbolizer>
      </Rule>
      <Rule>
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>TestValue</PropertyName>
            <LowerBoundary>
              <Literal>5</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>10.0</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PointSymbolizer>
           <Graphic>
             <Mark>
               <WellKnownName>star</WellKnownName>
               <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#7FFF00</CssParameter>
              </Fill>
            </Mark>
            <Size>4.0</Size> 
          </Graphic>
        </PointSymbolizer>
      </Rule>
      <Rule>
       <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>TestValue</PropertyName>
            <LowerBoundary>
              <Literal>10.0</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>20.0</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PointSymbolizer>
           <Graphic>
             <Mark>
               <WellKnownName>star</WellKnownName>
               <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#7FFF00</CssParameter>
              </Fill>
            </Mark>
            <Size>5.0</Size>  
          </Graphic>
        </PointSymbolizer>
      </Rule>
      <Rule>
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>TestValue</PropertyName>
            <LowerBoundary>
              <Literal>20</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>30</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PointSymbolizer>
           <Graphic>
             <Mark>
               <WellKnownName>star</WellKnownName>
               <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#7FFF00</CssParameter>
              </Fill>
            </Mark>
            <Size>6.0</Size>  
          </Graphic>
        </PointSymbolizer>
      </Rule>
      <Rule>
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>TestValue</PropertyName>
            <LowerBoundary>
              <Literal>30</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>50</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PointSymbolizer>
           <Graphic>
             <Mark>
               <WellKnownName>star</WellKnownName>
               <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#7FFF00</CssParameter>
              </Fill>
            </Mark>
            <Size>7.0</Size>
          </Graphic>
        </PointSymbolizer>
      </Rule>
      <Rule>
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>TestValue</PropertyName>
            <LowerBoundary>
              <Literal>50</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>400</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PointSymbolizer>
           <Graphic>
             <Mark>
               <WellKnownName>star</WellKnownName>
               <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#fFFF00</CssParameter>
              </Fill>
            </Mark>
            <Size>15.0</Size>  
          </Graphic>
        </PointSymbolizer>
      </Rule>
    </FeatureTypeStyle>
</UserStyle>

</NamedLayer>
</StyledLayerDescriptor>

