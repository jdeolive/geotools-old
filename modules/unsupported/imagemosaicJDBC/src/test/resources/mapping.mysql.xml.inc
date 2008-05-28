	<!-- possible values: universal,postgis,db2,mysql,oracle -->
	<spatialExtension name="mysql"/>		
	<mapping>		
		<masterTable name="MOSAIC" >
			<coverageNameAttribute name="NAME"/>
			<maxXAttribute name="maxX"/>
			<maxYAttribute name="maxY"/>
			<minXAttribute name="minX"/>
			<minYAttribute name="minY"/>
			<resXAttribute name="resX"/>
			<resYAttribute name="resY"/>
			<tileTableNameAtribute	name="TileTable" />
			<spatialTableNameAtribute name="SpatialTable" />
		</masterTable>
		<tileTable>
			<blobAttributeName name="data" />
			<keyAttributeName name="location" />
		</tileTable>
		<spatialTable>
			<keyAttributeName name="location" />
			<geomAttributeName name="GEOM" />
			<tileMaxXAttribute name="maxX"/>
			<tileMaxYAttribute name="maxY"/>
			<tileMinXAttribute name="minX"/>
			<tileMinYAttribute name="minY"/>
		</spatialTable>	
	</mapping>
