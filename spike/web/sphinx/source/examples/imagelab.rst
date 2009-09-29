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
        <dependency>
            <groupId>org.geotools</groupId>
            <artifactId>gt-coverage</artifactId>
            <version>${geotools.version}</version>
        </dependency>
        <dependency>
            <groupId>org.geotools</groupId>
            <artifactId>gt-geotiff</artifactId>
            <version>${geotools.version}</version>
        </dependency>
    </dependencies>

Most of these dependencies in the earlier examples such as :ref:`quickstart` and :ref:`crslab`. The two modules that we've added are **gt-coverage** which contains GeoTools classes for working with raster data and **gt-geotiff** which will allow us to read a GeoTIFF example data file.
 
Example
-------

The example code is available
 * Directly from svn: ImageLab.java_
 * Included in the demo directory when you download the GeoTools source code

.. _ImageLab.java: http://svn.osgeo.org/geotools/trunk/demo/example/src/main/java/org/geotools/demo/ImageLab.java 

Main Application
----------------
1. Please create the file **ImageLabl.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ImageLab.java
      :language: java


