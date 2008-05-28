	<connect>
		<!-- value DBCP or JNDI -->		
		<dstype value="DBCP"/>				
<!-- 		<jndiReferenceName value=""/>		 -->

		<username value="" />
		<password value="" />		
		
		<jdbcUrl value="jdbc:h2:target/h2/testdata" />		
  		<driverClassName value="org.h2.Driver"/> 
		<maxActive value="10"/>
		<maxIdle value="0"/>		
		
	</connect>
