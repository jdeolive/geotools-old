.. _crslab:

CRS Lab
=======

This tutorial gives a visual demonstration of coordinate reference systems by displaying
a shapefile and showing how changing the map projection morphs the shape of the features.

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
      <artifactId>gt-render</artifactId>
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
 * Directly from svn: CRSLab.java_
 * Included in the demo directory when you download the GeoTools source code

.. _CRSLab.java: http://svn.osgeo.org/geotools/trunk/demo/example/src/main/java/org/geotools/demo/CRSLab.java 
 
Main Application
----------------
1. Please create the file **CRSLab.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java

This method opens and connects to a shapefile and uses a **JMapFrame** to display it. This should look familiar to you from 
the :ref:`quickstart` example.

Note how we are customizing the map frame:

* ``mapFrame.enableTool(JMapFrame.Tool.NONE)`` requests that an empty toolbar be created
* Next we create a JButton and add it to the toolbar
* Finally we set an action for the button so that when it is clicked a chooser dialog will be displayed to select a new coordinate reference system which is then set as the new CRS of the map.

Running the application
-----------------------

A good shapefile to use with this example is the **bc_border** map which can be downloaded from http://udig.refractions.net/docs/data.zip

Run the application and open the bc_border map.

.. image:: CRSLab_4326.gif

Now click the 'Change CRS' button and select the EPSG:3005 BC Albers projection. Hint: you can type 3005 rather than scrolling through the very long list.

.. image:: CRSLab_chooser.gif

When you click OK the map will be re-displayed in this new map projection. As well as the change in shape of the border, notice that the units in the status bar have changed from degrees to meters.

.. image:: CRSLab_3005.gif

If you want to return to the original map project choose EPSG:4326.


