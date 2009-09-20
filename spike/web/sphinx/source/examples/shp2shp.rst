.. _shp2shp:

SHP 2 SHP Lab
=============

In this tutorial we will see how to read a shapefile, change its map projection, and then save the results as
a new shapefile. We will use of maven to build the example application (as introduced in the :ref:`quickstart`).

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
      <version>2.6-M2</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-shapefile</artifactId>
      <version>2.6-M2</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-epsg-hsql</artifactId>
      <version>2.6-M2</version>
    </dependency>
  </dependencies>

Although 2.6-M2 is shown above please please use make use of the correct "version" for the GeoTools you
wish towork with.

.. Tip:: If you are using Netbeans you can edit the pom.xml file within the IDE (it is listed
         under Project files in the Projects window). When you save your edits the library
         entries for your project will update automatically.

         If you are using Eclipse you will need to kick the dependencies into your IDE as
         follows:

         * At the command line enter ``mvn eclipse:eclipse``
         * Hit refresh in Eclipse

Example Code
------------

The example code is available directly from svn: Shp2Shp.java_ or, if you download the GeoTools
source you can find it in the demo directory.

.. _Shp2Shp.java:  http://svn.geotools.org/trunk/demo/example/src/main/java/org/geotools/demo/Shp2Shp.java

Application
-----------
1. Please create the file **Shp2Shp.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Shp2Shp.java
      :language: java
      :lines: 10-60, 102-127, 175

Similar to the :ref:`quickstart` example, we get an input file path and name with the **promptShapeFile**
method that takes the name from the command line arg if provided, or else displays a **JFileDataStoreChooser**
dialog. Once we have the file name we connect to a **DataStore** (it will be an instance of **ShapefileDataStore**),
then read the data and print the header details to the console.


