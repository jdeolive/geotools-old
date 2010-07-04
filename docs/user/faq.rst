.. _faq:

============================ 
 Frequently Asked Questions
============================

Please direct any comments or suggestions about this page to the `GeoTools user list
<https://lists.sourceforge.net/lists/listinfo/geotools-gt2-users>`_.

.. contents::

General Information
===================

What is GeoTools ?  
------------------

GeoTools is a free, open source Java geospatial toolkit for working with both vector and raster data. It is made up of a
large number of modules that allow you to:

 * access GIS data in many file formats and spatial databases
 * work with an extensive range of map projections
 * filter and analyze data in terms of spatial and non-spatial attributes
 * compose and display maps with complex styling
 * create and analyze graphs and networks

GeoTools implements specifications of the `Open Geospatial Consortium <http://www.osgeo.org/>`_ including:

 * Simple Features
 * GridCoverage
 * Styled Layer Descriptor
 * Filter Encoding

GeoTools can be readily extended by adding new modules, either for custom applications or as contributions to the
library.


What licence does GeoTools use ?
--------------------------------

All GeoTools modules are released under the GNU Lesser General Public License (LGPL). GeoTools can be used for
commercial applications.


How do I search the archives of the GeoTools mailing lists ?
------------------------------------------------------------

Go to `this page <http://n2.nabble.com/GeoTools-the-java-GIS-toolkit-f1936684.html>`_.


Why can't I find module X in the GeoTools distribution or javadocs ?
--------------------------------------------------------------------

If you're working with a recent GeoTools release then chances are the module that you're looking for is an
:ref:`unsupported module <unsupported_modules>`. These are not part of the standard GeoTools distribution but are
available from the `Subversion repository <http://svn.osgeo.org/geotools>`_ in the **modules/unsupported** folder. If
you are using Maven as your build tool you can include a dependency for an unsupported module as you would any other
GeoTools module.

.. _unsupported_modules:

What is an unsupported module ?
-------------------------------

Unsupported modules are those found in the **modules/unsupported** folder of each GeoTools version in the `Subversion
repository <http://svn.osgeo.org/geotools>`_. They are not part of the standard GeoTools distribution but are still
available for use via Subversion, Maven and manual download.

A module can be unsupported for one or more of the following reasons:

* It is under development and has not yet met all of the criteria for usability, test coverage, documentation etc to be
  included in the general GeoTools distribution.

* It lacks a module maintainer.

* It has been superseded by another module and dropped from the general distribution, but still has enough useful bits
  or active users to make it worth keeping (at least for a while).

Unsupported modules are a mixed bag: some are reliable and regularly used while others are in various states of
development or decay. The best way to find out the status of any particular module is to look in the `user list archives
<http://n2.nabble.com/geotools-gt2-users-f1936685.html>`_ and then, if you want to check further, post a question to the
list.



Displaying maps
===============

How do I display a shapefile ?
------------------------------

Have a look at the GeoTools :ref:`quickstart` which illustrates simple use of JMapFrame to do this.


Why does does my program crash when I try to display a shapefile over a WMS layer ?
-----------------------------------------------------------------------------------

This can happen when the axis definition (ie. which way is X ?) differs between the coordinate reference system used by
the shapefile and that used by the WMS layer.  It can be fixed by requesting GeoTools to enforce lon - lat axis order by 
including this statement in your code prior to displaying layers...

.. sourcecode:: java

  Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE); 

If this doesn't work you can try this more brutal, System wide approach...

.. sourcecode:: java

  System.setProperty("org.geotools.referencing.forceXY", "true"); 

See also:

* A `Jira issue <http://jira.codehaus.org/browse/GEOT-2995>`_ discussing this problem

* GeoTools user guide: `What Axis is X <http://docs.codehaus.org/display/GEOTDOC/04+What+Axis+is+X>`_


Building GeoTools applications with Maven 
=========================================

How do I create an executable jar for my GeoTools app ?  
-------------------------------------------------------

If you're familiar with Maven you might have used the `assembly plugin
<http://maven.apache.org/plugins/maven-assembly-plugin/>`_ to create self-contained, executable jars. The bad news is
that this generally won't work with GeoTools. The problem is that GeoTools modules often define one or more files in its
META-INF/services directory with the same names as files defined in other modules.  The assembly plugin just copies
files with the same name over the top of each other rather than merging their contents.

The good news is that the `Maven shade plugin <http://maven.apache.org/plugins/maven-shade-plugin/index.html>`_ can be
used instead and it will correctly merge the META-INF/services files from each of the GeoTools modules used by your
application.

The POM below will create an executable jar for the GeoTools Quickstart module which includes all of the required
GeoTools modules and their dependencies.

.. sourcecode:: xml

  <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.geotools.demo</groupId>
    <artifactId>quickstart</artifactId>
    <packaging>jar</packaging>
    <version>1.0</version>
    <name>GeoTools Quickstart example</name>
    <url>http://geotools.org</url>

    <properties>
        <geotools.version>2.6.2</geotools.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <encoding>UTF-8</encoding>
                    <target>1.5</target>
                    <source>1.5</source>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>1.3.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <!-- This bit sets the main class for the executable jar as you otherwise -->
                                <!-- would with the assembly plugin                                       -->
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <manifestEntries>
                                        <Main-Class>org.geotools.demo.Quickstart</Main-Class>
                                    </manifestEntries>
                                </transformer>
                                <!-- This bit merges the various GeoTools META-INF/services files         -->
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
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
            <artifactId>gt-swing</artifactId>
            <version>${geotools.version}</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.5</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

  </project>


