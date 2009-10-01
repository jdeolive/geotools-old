.. _imagelab:

.. admonition:: This is a new page 

   We've only just added this page and it's still incomplete

Image Lab
===========

In the earlier examples we looked at reading and displaying shapefiles. Now we are going add raster (ie. gridded data) maps to the mix by displaying a three-band global satellite image and overlaying it with country boundaries from a shapefile.

Dependencies
------------
 
Please ensure your pom.xml includes the following::

    <dependencies>
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
        <dependency>
            <groupId>org.geotools</groupId>
            <artifactId>gt-geotiff</artifactId>
            <version>${geotools.version}</version>
        </dependency>
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
    </dependencies>

Most of these dependencies in the earlier examples such as :ref:`quickstart` and :ref:`crslab`. The module that we've added is **gt-geotiff** which allows us to read raster map data from a GeoTIFF file.
 
Example
-------

The example code is available
 * Directly from svn: ImageLab.java_
 * Included in the demo directory when you download the GeoTools source code

.. _ImageLab.java: http://svn.osgeo.org/geotools/trunk/demo/example/src/main/java/org/geotools/demo/ImageLab.java 

Main Application
----------------
1. Please create the file **ImageLab.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start source
      :end-before: // docs end main

Prompting for input data
------------------------

We could use JFileDataStoreChooser to prompt the user for the GeoTIFF file and then again for the shapefile, but instead we'll use a dialog created with the GeoTools JWizard class. We'll look at the construction of this dialog later. 

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start get layers
      :end-before: // docs end get layers

Displaying the map data
-----------------------

As usual, we are using JMapFrame to display the data. We use the **SLD** class, which contains a variety of handy style-related methods, to create a basic rendering style for the shapefile. For the GeoTIFF image we create a style with a method that we'll examine next.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start display layers
      :end-before: // docs end display layers

Creating a Style for the raster layer
-------------------------------------

Words to come...  This method's code will probably change.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start create style
      :end-before: // docs end create style

The file prompt wizard
----------------------

Words to come... This code may change.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start wizard
      :end-before: // docs end source

To be continued...


