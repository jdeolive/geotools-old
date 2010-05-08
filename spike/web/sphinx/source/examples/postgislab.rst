.. _postgislab:

.. admonition:: This is a new page

   We're only just adding this page and it's still incomplete

PostGIS Lab
===========

Up until now we have been working with shape files, in this lab we bring out the big guns - a real
spatial database.

If you are working in an enterprise that has as spatial database (Oracle, DB2) or geospatial
middleware (ArcSDE) can connect to your existing infrastructure. In this example we will use
PostGIS - an extension for PostgreSQL supporting "simple featues for sql".

Dependencies
------------
 
We are going to add another couple dependencies here::
 
  <properties>
      <geotools.version>2.6.3</geotools.version>
  </properties>

  <dependencies>
      <dependency>
          <groupId>org.geotools</groupId>
          <artifactId>gt-postgis</artifactId>
          <version>{gt-version}</version>
      </dependency>
      <dependency>
          <groupId>org.geotools</groupId>
          <artifactId>gt-epsg-hsql</artifactId>
          <version>{gt-version}</version>
      </dependency>
      <dependency>
          <groupId>org.geotools</groupId>
          <artifactId>gt-swing</artifactId>
          <version>${geotools.version}</version>
      </dependency>
      <dependency>
          <groupId>org.geotools</groupId>
          <artifactId>gt-render</artifactId>
          <version>${geotools.version}</version>
      </dependency>
  </dependencies>

Please add these dependencies to your pom.xml and update your IDE as required.

Application
-----------

TBA
