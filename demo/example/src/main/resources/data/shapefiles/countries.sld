<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
    xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
    xmlns="http://www.opengis.net/sld"
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <NamedLayer>
    <Name>Simple polygon</Name>
    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#000000</CssParameter>
            </Stroke>
            <Fill>
              <CssParameter name="fill">
                <ogc:Function name="colorramp">
                    <ogc:Function name="log">
                        <ogc:PropertyName>POP_CNTRY</ogc:PropertyName>
                    </ogc:Function>
                    <ogc:Literal>5</ogc:Literal>
                    <ogc:Literal>22</ogc:Literal>
                    <ogc:Literal>0.8</ogc:Literal>
                    <ogc:Literal>0.8</ogc:Literal>
                </ogc:Function>
              </CssParameter>
            </Fill>
          </PolygonSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>

