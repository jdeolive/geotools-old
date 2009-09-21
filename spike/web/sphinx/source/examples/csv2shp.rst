.. _csv2shp:

CSV 2 SHP Lab
=============

The tutorial covers the following:

 * Producing Features from Scratch
 * Use of GeometryFactory to build a Point
 * Writing out a Shapefile
 * Forcing a Projection
 * Building a FeatureType

Comma Seperated Value
---------------------
To start with you will need a CSV file.
# Create a text file *location.csv*
# Copy and paste the following locations into the file::

  "Longitude","Latitude","Name"
  -123.31,48.4,Victoria
  0,52,London
  
 # Feel free to add your own location!
 # Save

Dependencies
------------

This example uses the same dependencies as the :ref:`quickstart`. Please ensure your pom.xml includes the following::

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
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

Please note that the jars mentioned above will pull in a host of other dependencies (such as the hsql database driver).

Example
-------

The example code is available
 * Directly from svn: Csv2Shape.java_.
 * included in the demo directory when you downloaded geotools source

.. _Csv2Shape.java:  http://svn.geotools.org/trunk/demo/example/src/main/java/org/geotools/demo/Csv2Shape.java

Main Application
----------------
1. Please create the file **Csv2Shape.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Csv2Shape.java
      :language: java
      :lines: 1-63,124-125,212
   
Prompt for CSV File
-------------------

Here is the method to get the input csv file name from the command line or, if not provided, via a
**JFileDataStoreChooser** dialog:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Csv2Shape.java
      :language: java
      :start-after: // start getNewShapeFile
      :end-before: // end getNewShapeFile

Prompt for Shape File
---------------------

The next method will prompt the user for an appropriate shapefile to write
out to. The original csv file will be used to determine a good default
shapefile name.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Csv2Shape.java
      :language: java
      :start-after: // start getCSVFile
      :end-before: // end getCSVFile

Read into a FeatureCollection
-----------------------------
We can now read the CSV File into a FeatureCollection; please note the following:

 * Use of FeatureCollections.newCollection() to create a FeatureCollection
 * Creation of a SimpleFeatureType with location and name attributes
 * Use of GeometryFactory to create new Points
 * Creation of a SimpleFeature using SimpleFeatureBuilder

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Csv2Shape.java
      :language: java
      :start-after: // read csv file into feature collection
      :end-before: // create shapefile from feature collection

Create a Shapefile From a FeatureCollection
-------------------------------------------

Things to note as we create the shapefile:

 * Use of ShapefileDataStoreFactory with a parameter indicating we want a spatial index
 * We are using createSchema( SimpleFeatureType ) to set up the shapefile
 * Our SimpleFeatureType did not include CoordinateReferenceSystem information (needed to make a .prj file) so we are going to call forceSchemaCRS ourself
 * Use of a Transaction to safely add the FeatureCollection in one go

Here is the main method with the additional code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Csv2Shape.java
      :language: java
      :start-after: // start main
      :end-before: // end main

Running the Application
-----------------------

1. When you run this application it will prompt you for:

 * the location of a CSV file to read; and then
 * a shapefile to create

Building a SimpleFeatureType
----------------------------

The above example was very quick; please review the following details to better understand how you can control the process of creating a SimpleFeatureType with all the required information

We are going to build a SimpleFeatureType using SimpleFeatureTypeBuilder. In the example above we created a SimpleFeatureType using the following snippet::

    final SimpleFeatureType TYPE = DataUtilities.createType("Location", "location:Point,name:String");

I often use a constant to hold the SimpleFeatureType; because the SimpleFeatureType class is immutable I find tracking them as final variables helps me remember what they are.

The createSchema method is fine for a quick example; but has a couple of disadvantages:

 * you cannot specify the CoordinateReferneceSystem of your "location" attribute
 * you cannot specify the max string length (so your DBF files may be bigger than strictly required).
 * the javadocs on the createSchema parameters are a bit hard to follow

Here is how to use SimpleFeatureTypeBuilder to accomplish the same result:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/Csv2Shape.java
      :language: java
      :start-after: // start createFeatureType
      :end-before: // end createFeatureType


With this new improved SimpleFeatureType (that contains a CoordinateReferenceSystem) we will no longer need to call forceSchemaCRS to generate our ".prj" file.

