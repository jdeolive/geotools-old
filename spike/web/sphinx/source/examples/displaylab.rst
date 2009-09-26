.. _mapdisplaylab:

Map Display Lab
===============

This tutorial covers:

 * Creating a custom map pane tool to select features that are clicked with the mouse
 * Adding a toolbar button for the tool to JMapFrame
 * Creating rendering styles to draw selected and unselected features in different colours

At the end of the tutorial you will be able to create your own rendering styles and customize JMapFrame (or use these techniques
in your own GUI classes).

Dependencies
------------
 
Please ensure your pom.xml includes the following::

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-swing</artifactId>
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

Example
-------

The example code is available
 * Directly from svn: MapDisplayLab.java_
 * Included in the demo directory when you download the GeoTools source code

.. _MapDisplayLab.java: http://svn.osgeo.org/geotools/trunk/demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
 
Main Application
----------------
1. Please create the file **MapDisplayLab.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
      :language: java
      :start-after: // docs start source
      :end-before: // docs end main

The class constants and variables in the code above will be explained as we work through each of the methods in the following sections.

Shapefile viewer with custom map tool
-------------------------------------

This is the central method of the application. The first part, which connects to the shapefile, should be familiar to your from the :ref:`quickstart` and :ref:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
      :language: java
      :start-after: // docs start display shapefile
      :end-before: // docs end display shapefile



