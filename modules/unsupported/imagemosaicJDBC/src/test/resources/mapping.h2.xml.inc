	<!-- possible values: universal,postgis,db2,mysql,oracle -->
	<spatialExtension name="universal"/>		
	<mapping>
        <masterTable name="META" >
            <coverageNameAttribute name="Name"/>
            <maxXAttribute name="MaxX"/>
            <maxYAttribute name="MaxY"/>
            <minXAttribute name="MinX"/>
            <minYAttribute name="MinY"/>
            <resXAttribute name="ResX"/>
            <resYAttribute name="RresY"/>
            <tileTableNameAtribute    name="TileTable" />
            <spatialTableNameAtribute name="SpatialTable" />
        </masterTable>
        <tileTable>
            <blobAttributeName name="Data" />
            <keyAttributeName name="Location" />
        </tileTable>
        <spatialTable>
            <keyAttributeName name="Location" />
            <geomAttributeName name="Geom" />
            <tileMaxXAttribute name="MaxX"/>
            <tileMaxYAttribute name="MaxY"/>
            <tileMinXAttribute name="MinX"/>
            <tileMinYAttribute name="MinY"/>
        </spatialTable>
    </mapping>