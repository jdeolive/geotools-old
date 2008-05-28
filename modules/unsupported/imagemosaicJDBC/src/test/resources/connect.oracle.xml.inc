	<connect>
		<!-- value DBCP or JNDI -->		
		<dstype value="DBCP"/>				
<!-- 		<jndiReferenceName value=""/>		 -->

		<username value="HR" />
		<password value="hr" />		
		
		<jdbcUrl value="jdbc:oracle:thin:@localhost:1521:XE" />
  		<driverClassName value="oracle.jdbc.OracleDriver"/> 
		<maxActive value="10"/>
		<maxIdle value="0"/>		
	</connect>
