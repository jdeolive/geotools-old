

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
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsLessThan>
           <PropertyName>PERSONS</PropertyName>
           <Literal>2000000</Literal>
          </PropertyIsLessThan>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#00FF00</CssParameter>
           </Fill>     
        </PolygonSymbolizer>
                    <name>less than 2000000 persons</name>
                </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsBetween>
            <PropertyName>PERSONS</PropertyName>
            <LowerBoundary>
              <Literal>2000000</Literal>
            </LowerBoundary>
            <UpperBoundary>
              <Literal>4000000</Literal>
            </UpperBoundary>
          </PropertyIsBetween>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#FF0000</CssParameter>
           </Fill>     
        </PolygonSymbolizer>
                    <name>between 2000000 and 4000000 persons</name>
                </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter xmlns:gml="http://www.opengis.net/gml">
          <PropertyIsGreaterThan>
           <PropertyName>PERSONS</PropertyName>
           <Literal>4000000</Literal>
          </PropertyIsGreaterThan>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#0000FF</CssParameter>
           </Fill>     
        </PolygonSymbolizer>
                    <name>more than 4000000 persons</name>
                </Rule>
      <Rule>
        <LineSymbolizer>
           <Stroke/>    
        </LineSymbolizer>
                    <name> state boundaries</name>
                </Rule>
    </FeatureTypeStyle>
</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>

