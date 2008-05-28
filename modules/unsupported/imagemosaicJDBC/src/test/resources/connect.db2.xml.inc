	<connect>
		<!-- value DBCP or JNDI -->		
		<dstype value="DBCP"/>				
<!-- 		<jndiReferenceName value=""/>		 -->

		<username value="db2inst1" />
		<password value="db2inst1" />		
		
<!--		<jdbcUrl value="jdbc:db2//pp20:60000/sde" />-->		
		<jdbcUrl value="jdbc:db2:mosaic" />		
  		<driverClassName value="com.ibm.db2.jcc.DB2Driver"/> 
		<maxActive value="10"/>
		<maxIdle value="0"/>		
	</connect>
