

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
  <Name>sillypolyshp</Name>
    <!-- FeatureTypeStyles describe how to render different features -->
    <!-- a feature type for polygons -->
    <FeatureTypeStyle>
      <FeatureTypeName>feature</FeatureTypeName>
      <Rule>
        <!-- like a linesymbolizer but with a fill too -->
        <PolygonSymbolizer>
            <Stroke>
                <CssParameter name="stroke-width">
                <Literal>2 </Literal>
                </CssParameter>
                <CssParameter name="stroke">#FFaaaa</CssParameter>
          </Stroke>
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
                    </mark>
                </Graphic>
                <Stroke>
                    <CssParameter name="stroke">#FFFF00</CssParameter>
                     <CssParameter name="stroke-width">1</CssParameter>
               </Stroke>
            </GraphicFill>
          </Fill>
        </PolygonSymbolizer>
      </Rule>
    </FeatureTypeStyle>
</UserStyle>
</NamedLayer>
</StyledLayerDescriptor>

