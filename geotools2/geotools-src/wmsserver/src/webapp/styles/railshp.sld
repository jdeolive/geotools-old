

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
  <Name>railshp</Name>
    <!-- FeatureTypeStyles describe how to render different features -->
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
      <FeatureTypeName>feature</FeatureTypeName>
      <Rule>
        <LineSymbolizer>
          <Stroke>
                <CssParameter name="stroke-width">1</CssParameter>
                <CssParameter name="stroke-dasharray">5 5</CssParameter>
                <CssParameter name="stroke">#AAAAAA</CssParameter>
          </Stroke>
        </LineSymbolizer>
      </Rule>
    </FeatureTypeStyle>
</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>

