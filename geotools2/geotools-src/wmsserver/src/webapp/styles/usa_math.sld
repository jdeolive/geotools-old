

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
  <Name>div test</Name>
    <!-- FeatureTypeStyles describe how to render different features -->
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
        <FeatureTypeName>feature</FeatureTypeName>
        <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        
        <PolygonSymbolizer>
           <Fill>
              <!-- CssParameters allowed are fill (the color) and fill-opacity -->
              <CssParameter name="fill">#000000</CssParameter>
              <CssParameter name="opacity">
                <Div>
                    <PropertyName>MANUAL</PropertyName>
                    <PropertyName>WORKERS</PropertyName>
                </Div>
              </CssParameter>
           </Fill>
           <stroke/>     
        </PolygonSymbolizer>
        
      </Rule>
    </FeatureTypeStyle>
</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>

