.. _shp2shp:

SHP 2 SHP Lab
=============

This page covers how to write a shapefile out to disk. We are going to make use of maven (as covered in quickstart).

Additional information:
 * `Shapefile Plugin <http://docs.codehaus.org/display/GEOTDOC/Shapefile+Plugin>`_
 * :ref:`examples`

Dependencies
------------

This example uses the same dependencies as the quickstart; please ensure your pom.xml includes the following::

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-main</artifactId>
      <version>2.5.7</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-shapefile</artifactId>
      <version>2.5.7</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-epsg-hsql</artifactId>
      <version>2.5.7</version>
    </dependency>
  </dependencies>

Although 2.5.7 is shown above please please use make use of the correct "version" for the GeoTools you
wish towork with.

Refresh your IDE Project Files
------------------------------

1. You will need to kick these dependencies into your IDE with another::

    C:\java\example>mvn eclipse:eclipse

2. Hit refresh in Eclipse
3. You can now see the new dependencies - and everything else they make use of!

Example Code
------------

The example code is available directly from::
 * Directly from svn: Shp2Shp.java_.
 * included in the demo directory when you downloaded geotools source

.. _Shp2Shp.java:  http://svn.geotools.org/trunk/demo/example/src/main/java/org/geotools/demo/Shp2Shp.java


The code has been cut & pasted into the document here; but please consider either of the above sources as
they may have useful corrections or clarifications added since this document has been written.

Application
-----------
1. Please create the file **Shp2Shp.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Shp2Shp.java
      :language: java

