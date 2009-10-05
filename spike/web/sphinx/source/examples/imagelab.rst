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

We use one of GeoTools' data wizards, **JParameterListWizard**, to prompt for the two input files:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start get layers
      :end-before: // docs end get layers

Note the use of **Parameter** objects for each input file. The arguments passed to the Parameter constructor are:

:key: a key an identifier for the Parameter

:type: the Class of the object that the Parameter refers to

:title: a title which the wizard will use to label the text field

:description: a brief description which the wizard will display below the text field

:metadata: a Map containing additional data for the Parameter - in our case this is the file extension

**KVP** is a handy class for creating a Map of String key : Object value pairs. It's particularly useful with multiple pairs::

  // Create a new Map with three key:value pairs
  KVP foo = new KVP(Foo.THING, thing, Foo.STUFF, stuff, Foo.BAR, bar);

Displaying the map data
-----------------------

To display the map on screen we create a **MapContext**, add the image and the shapefile to it and pass it
to a **JMapFrame**. 

Rather than using the static JMapFrame.showMap method, as we have in previous examples, we create a map frame and customize it
by adding a menu to choose the image display mode. 

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start display layers
      :end-before: // docs end display layers

Note that we are creating a **Style** for each of the map layers...

* A greyscale Style for the image, created with a method that we'll examine next
* A simple outline style for the shapefile using the **SLD** utility class

Creating a Style for the raster layer
-------------------------------------

We want the user to be able to choose between greyscale display of a selected image band, or RGB display
(assuming that the image contains at least three bands).

Creating a greyscale Style
~~~~~~~~~~~~~~~~~~~~~~~~~~

Two methods are involved here: the first to prompt the user for the image band; and the second to actually
create the Style.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start create greyscale style
      :end-before: // docs end create greyscale style

Creating an RGB Style
~~~~~~~~~~~~~~~~~~~~~

To create an RGB Style we specify the image bands to use for the red, green and blue *channels*. In the method here,
we examine the image to see if its bands (known as *sample dimensions* in GeoTools-speak) have labels indicating which
to use. If not, we just use the first three bands and hope for the best !

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java
      :start-after: // docs start create rgb style
      :end-before: // docs end source

Running the application
-----------------------

Extra things to try
-------------------


