

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
    <!-- FeatureTypeStyles describe how to render different features -->
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
      <FeatureTypeName>feature</FeatureTypeName>
      <Rule>
        <!--Mid-->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>M_5_9</PropertyName>
            <LowerBoundary>
              <Literal>5000</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>10000</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#FF0000</CssParameter>
           </Fill>     
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <!--bottom-->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsLessThan>
           <PropertyName>M_5_9</PropertyName>
           <Literal>5001</Literal>
          </PropertyIsLessThan>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#00FF00</CssParameter>
           </Fill>     
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <!--top-->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsGreaterThan>
           <PropertyName>M_5_9</PropertyName>
           <Literal>20000</Literal>
          </PropertyIsGreaterThan>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#0000FF</CssParameter>
           </Fill>     
        </PolygonSymbolizer>
      </Rule>
      <Rule>
        <LineSymbolizer>
           <Stroke/>    
        </LineSymbolizer>
      </Rule>
    </FeatureTypeStyle>
</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>

