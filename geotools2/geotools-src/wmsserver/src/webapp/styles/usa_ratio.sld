

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
  <Name>bbox test</Name>
    <!-- FeatureTypeStyles describe how to render different features -->
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
        <FeatureTypeName>feature</FeatureTypeName>
        <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter  xmlns:gml="http://www.opengis.net/gml">
            <PropertyIsLessThan>
                <PropertyName>P_MALE</PropertyName>
                <PropertyName>P_FEMALE</PropertyName>
            </PropertyIsLessThan>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#ffaaaa</CssParameter>
              <CssParameter name="opacity">.5</CssParameter>
           </Fill>
           <stroke/>     
        </PolygonSymbolizer>
        
      </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter  xmlns:gml="http://www.opengis.net/gml">
            <PropertyIsLessThan>
                <PropertyName>P_FEMALE</PropertyName>
                <PropertyName>P_MALE</PropertyName>
            </PropertyIsLessThan>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#aaaaff</CssParameter>
              <CssParameter name="opacity">.5</CssParameter>
           </Fill> 
           <stroke/>    
        </PolygonSymbolizer>
        
      </Rule>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <Filter  xmlns:gml="http://www.opengis.net/gml">
            <PropertyIsEqualTo>
                <PropertyName>P_FEMALE</PropertyName>
                <PropertyName>P_MALE</PropertyName>
            </PropertyIsEqualTo>
        </Filter>
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#ffaaff</CssParameter>
              <CssParameter name="opacity">.5</CssParameter>
           </Fill> 
           <stroke/>    
        </PolygonSymbolizer>
        
      </Rule>
    </FeatureTypeStyle>
</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>

