.. _shapelab:

Shape Lab
=========

In this example, we are going to open up a shape file and draw in on the screen. This time out you can have considerable flexibility to modify the style.
 * You can create rules using the same Filter concepts covered in the PostGIS lab. Rules select which features are to be drawn and how to style them.
 * You can define "Symbolizers" that define how lines, points, polygons and text are displayed. The symbolizers can make use of expression - allowing both the use of feature data and on the fly calculations during rendering.
 * Finally you can define several rules each with more than one symbolizer making for some very sophisticated effects.

Example
-------

The example code is available:
 * Directly from svn: ShapeLab.java_.
 * included in the demo directory when you downloaded geotools source

.. _ShapeLab.java:  http://svn.geotools.org/trunk/demo/example/src/main/java/org/geotools/demo/ShapeLab.java

Main Application
----------------
1. Please create the file **ShapeLab.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ShapeLab.java
      :language: java
      :start-after: // start source
      :end-before: // end main method
 
Note the use of the static **JMapFrame.showMap** method which is the simplest way to display a shapefile or other data store type with GeoTools.

.. image:: JMapFrame.gif

Creating a Style
----------------

In the code above we called the createStyle method to make a new Style object. This method examines the *default geometry* contained in the *schema* to figure out what kind of content is being displayed (points, lines or polygons). Based on that, it delegates the task of creating an appropriate Style to geometry-specific methods.

Here is the code for the method:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ShapeLab.java
      :language: java
      :start-after: // start createStyle
      :end-before: // end createStyle

StyleFactory and FilterFactory
------------------------------

We use two factory classes to create styles, StyleFactory, to create a Style and its component objects, and FilterFactory to create the Expressions used to look up feature properties, perform calculations or hold literal values.

We can see this in action in its simplest form in the first method called by createStyle: createPolygonStyle. In the code below we use a FilterFactory object to create literal expressions (Expression objects holding constants) as the arguments to the StyleFactory methods that create Stroke and Fill objects:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ShapeLab.java
      :language: java
      :start-after: // start createPolygonStyle
      :end-before: // mid createPolygonStyle

Symbolizers, Rules and FeatureTypeStyles
----------------------------------------

In the second part of the the createPolygonStyle method we pass the Stroke and Fill objects to create a PolygonSymbolizer. This is then wrapped in a Rule, which in turn is wrapped in a FeatureTypeStyle, and finally a Style:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ShapeLab.java
      :language: java
      :start-after: // mid createPolygonStyle
      :end-before: // end createPolygonStyle

Creating simple Styles for Line and Point features
--------------------------------------------------

The methods called by createStyle for feature types with LineString or Point geometries are very similar to createPolygonStyle. Each creates a Symbolizer and then wraps this in the hierarchy of Rule, FeatureTypeStyle and Style:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ShapeLab.java
      :language: java
      :start-after: // end createPolygonStyle
      :end-before: // end createPointStyle

Creating Styles from SLD documents
----------------------------------

The methods above create Style objects programmatically. GeoTools can also create Styles declaratively, from SLD (Styled Layer Descriptor) documents. ShapeLab.java includes a method that demonstrates this:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ShapeLab.java
      :language: java
      :start-after: // start createFromSLD
      :end-before: // end createFromSLD

Prompt for shapefile
--------------------

Finally, the remaining method in the ShapeLab class get the input shapefile from the command line argument or by prompting the user with a JFileDataStoreChooser dialog: 

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/ShapeLab.java
      :language: java
      :start-after: // start promptShapeFile
      :end-before: // end promptShapeFile


