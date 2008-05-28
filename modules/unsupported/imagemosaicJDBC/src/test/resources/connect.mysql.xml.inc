	<connect>
		<!-- value DBCP or JNDI -->		
		<dstype value="DBCP"/>				
<!-- 		<jndiReferenceName value=""/>		 -->

		<username value="root" />
		<password value="" />		
		
		<jdbcUrl value="jdbc:mysql://localhost:3306/gis" />		

  		<driverClassName value="com.mysql.jdbc.Driver"/> 
		<maxActive value="10"/>
		<maxIdle value="0"/>		
	</connect>
