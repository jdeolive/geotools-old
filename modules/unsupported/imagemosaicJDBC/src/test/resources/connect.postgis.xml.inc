	<connect>
		<!-- value DBCP or JNDI -->		
		<dstype value="DBCP"/>				
<!-- 		<jndiReferenceName value=""/>		 -->

		<username value="postgres" />
		<password value="postgres" />		
		
		<jdbcUrl value="jdbc:postgresql://localhost:5432/gis" />		

  		<driverClassName value="org.postgresql.Driver"/> 
		<maxActive value="10"/>
		<maxIdle value="0"/>		
	</connect>
