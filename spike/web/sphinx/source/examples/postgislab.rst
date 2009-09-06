.. _postgislab:

PostGIS Lab
===========

Up until now we have been working with shape files, in this lab we bring out the big guns - a real spatial database.

If you are working in an enterprise that has as spatial database (Oracle, DB2) or geospatial middleware (ArcSDE)
can connect to your existing infrastructure. We do hope you take home
another lesson - PostGIS is a very capable alternative brought to you by the nice folks at Refractions Research.

Dependencies
------------
 
We are going to add another couple dependencies here::
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-postgis</artifactId>
      <version>2.5.7</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-epsg-hsql</artifactId>
      <version>2.5.7</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-swing</artifactId>
      <version>${project.version}</version>
    </dependency>
  </dependencies>

Please add these dependencies to your pom.xml and update your IDE as required.

Application
-----------

TBA