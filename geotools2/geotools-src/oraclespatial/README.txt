Oracle Spatial Data Source Implementation for Geotools

**  Build Issues  **

The Oracle spatial data source requires jars that are non-distributable by 
the Geotools team.  These jars are the sodapi.jar and the Oracle thin JDBC
driver (classes12.jar).  We do provide a dummy jar that contains stub classes
to allow the Oracle data source to build, however the real jars will be required
to run anything that uses the data source.  

Note: It seems that a bug occurs when adding features to the database if you
do not use the classes12.jar file that comes with your Oracle install.  If you
get an error message to the effect of "Invalid logical handle" make sure you
are using the JDBC driver from your Oracle installation.

You can either just include these jars in your runtime or copy the jars to you 
Maven repository under the oracle/jars directory.  If you choose the second 
option, you can make maven use the real jars over the stub jar by commenting out this:

<!-- Use this when you dont have classes12 and sdoapi -->
<dependency>
  <artifactId>dummy_spatial</artifactId>
  <groupId>oracle</groupId>
  <version>8.1.7</version>
 </dependency>
 <!-- End -->

in the project.xml file and uncommenting this:

 <!-- Commented out so we can build using the dummy_spatial jar 
<dependency>
  <artifactId>sdoapi</artifactId>
  <groupId>oracle</groupId>
  <version>8.1.7</version>
</dependency>
<dependency>
  <artifactId>classes</artifactId>
  <groupId>oracle</groupId>
  <version>12</version>
</dependency>
-->

This is also required for running the tests.

**  Running Tests **

Firstly, you must setup an Oracle database with spatial support and load
the tables and data found in tests/unit/testData/testData.sql. Then edit
the test.properties file to point to your database.  Then you need to
edit the project.xml file.  Find the section that has the comment saying
"This section excludes the tests" and comment it out, then uncomment the section
that says "Uncomment to run tests".  Then you can type maven test in the
oraclespatial directory.


**  Further Help **
If you are still having problems getting the Oracle Data Source
to work, contact Sean Geoghegan at geotools-devel@lists.sourceforge.net