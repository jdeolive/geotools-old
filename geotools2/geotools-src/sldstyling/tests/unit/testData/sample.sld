<StyledLayerDescriptor version="0.7.2">
<NamedLayer>
<Name>A Random Layer</Name>
<UserStyle>
  <Name>MyStyle</Name>
    <FeatureTypeStyle>
      <FeatureTypeName>polygon</FeatureTypeName>
      <Rule>
        <PolygonSymbolizer>
          <Fill>
            <CssParameter name="fill">#FF0000</CssParameter>
          </Fill>
          <Stroke>
            <CssParameter name="width">3</CssParameter>
          </Stroke>
        </PolygonSymbolizer>
      </Rule>
    </FeatureTypeStyle>

    <FeatureTypeStyle>
      <FeatureTypeName>line</FeatureTypeName>
      <Rule>
        <LineSymbolizer>
          <Stroke>
            <CssParameter name="stroke">#0000FF</CssParameter>
            <CssParameter name="width">2</CssParameter>
          </Stroke>
        </LineSymbolizer>
      </Rule>
    </FeatureTypeStyle>

</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>