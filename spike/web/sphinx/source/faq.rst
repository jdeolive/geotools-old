.. _faq:

============================ 
 Frequently Asked Questions
============================

.. NOTE TO MAINTAINERS: Please add new questions to the end of their sections, so section/question numbers remain stable.

GeoTools is big. Big enough to get lost in. Big enough to miss tips and tricks that could save you hours of coding. Many
of these gems can be found by searching the `GeoTools user list archive
<http://n2.nabble.com/geotools-gt2-users-f1936685.html>`_ or by studying the unit tests in the library. Now we've begun
harvesting this collective wisdom so that you don't have to spend so much time searching. We've only just started so
there's not much here yet. Please help by suggesting questions to add (and answers if you have them) via the `user list
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

Building GeoTools applications with Maven 
=========================================

How do a create an executable jar for my GeoTools app ?  
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


