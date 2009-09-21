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
      <version>${geotools.version}</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-shapefile</artifactId>
      <version>${geotools.version}</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-epsg-hsql</artifactId>
      <version>${geotools.version}</version>
    </dependency>
  </dependencies>

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
      :start-after: // begin source
      :end-before: // begin main

Getting the input shapefile
~~~~~~~~~~~~~~~~~~~~~~~~~~~

The following method reads a file name from the command line or, if not provided, displays a dialog:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Shp2Shp.java
      :language: java
      :start-after: // begin promptShapefile
      :end-before: // end promptShapefile

Getting a new projection
~~~~~~~~~~~~~~~~~~~~~~~~

Here we get a list of EPSG_ identifiers for coordinate reference systems, plus their summary descriptions,
and display them for the user to select the new CRS:

.. _EPSG: http://www.epsg-registry.org/ 

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Shp2Shp.java
      :language: java
      :start-after: // begin getCRS
      :end-before: // end getCRS

Getting the output shapefile
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The following method prompts the user for an output shapefile:


   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Shp2Shp.java
      :language: java
      :start-after: // begin getNewShapefile
      :end-before: // end getNewShapefile

Putting it all together in the main method
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Finally, here is the main method that does the work of reading the input shapefile, re-projecting
the features and writing them out to the new shapefile:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Shp2Shp.java
      :language: java
      :start-after: // begin main
      :end-before: // end main

