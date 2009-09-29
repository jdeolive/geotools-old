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
      <groupId>org.geotools</groupId>
      <artifactId>gt-swing</artifactId>
      <version>${geotools.version}</version>
      <!-- For this module we explicitly exclude some of its own -->
      <!-- dependencies from being downloaded because they are   -->
      <!-- big and we don't need them                            -->
      <exclusions>
        <exclusion>
          <groupId>org.apache.xmlgraphics</groupId>
          <artifactId>batik-transcoder</artifactId>
          </exclusion>
        </exclusions>
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

This is the central method of the application. The first part, which connects to the shapefile, should be familiar to your from the :ref:`quickstart` example.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
      :language: java
      :start-after: // docs start display shapefile
      :end-before: // docs end display shapefile

We create a JMapFrame and add an extra button to its toolbar. Then we set button action to create a new **CursorTool** object which will be our custom feature selection tool. Note that because our tool is relatively simple (only responding to mouse clicks) we are creating it as an anonymous class in the button action.

Finally, we size the map frame and display it.

In the **displayShapefile** method above we called two other class methods: **setGeometry** and **createDefaultStyle**. Let's have a look at these now.

Geometry type of the shapefile features
---------------------------------------

The application needs to know what type of geometry the shapefile features represent: points, lines or polygons. This information will be used in creating rendering styles for the features and also influences how we query what feature the user has clicked on.

The **setGeometry** method records the geometry type as well as the unit of distance that shapefile feature coordinates are expressed in.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
      :language: java
      :start-after: // docs start set geometry
      :end-before: // docs end set geometry

Creating a default rendering style
----------------------------------

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
      :language: java
      :start-after: // docs start default style
      :end-before: // docs end default style

The createRule method to set the symbolizer
-------------------------------------------

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
      :language: java
      :start-after: // docs start create rule
      :end-before: // docs end create rule

What features did the user click on ?
-------------------------------------

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
      :language: java
      :start-after: // docs start select features
      :end-before: // docs end select features

Highlighting selected features in the display
---------------------------------------------

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/MapDisplayLab.java
      :language: java
      :start-after: // docs start selected style
      :end-before: // docs end selected style
