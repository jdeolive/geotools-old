.. _eclipse-quickstart:

**********************
  Eclipse Quickstart 
**********************

.. sectionauthor:: Jody Garnett <jody.garnett@gmail.org>

:Author: Jody Garnett
:Author: Micheal Bedward
:Thanks: geotools-user list
:Version: $Revision: 5801 $
:License: Create Commons with attribution

Welcome Eclipse Developers
==========================

Welcome to Geospatial for Java -this workbook is aimed at Java developers who are new to geospatial
and would like to get started.

We are going to start out carefully with the steps needed to set up your IDE and are pleased this
year to cover both NetBeans and Eclipse. If you are comfortable with the build tool Maven, it is
our preferred option for downloading and managing jars but we will also document how to set up
things by hand.

This is our second year offering visual tutorials allowing you to see what you are working with
while learning. While these examples will make use of Swing, please be assured that that this is
only an aid in making the examples easy and fun to use. 

These sessions are applicable to both server side and client side development.

Java Install
============

.. sidebar:: Lab

   If you are following this workbook in a lab setting you will find installer on the DVD.
   
We are going to be making use of Java – so if you don't have a Java Development Kit installed now is
the time to do so. Even if you have Java installed already check out the optional Java Advanced
Imaging and Java Image IO section.
   
#. Download the latest JDK from the the java.sun.com website:

   http://java.sun.com/javase/downloads/index.jsp
   
#. At the time of writing the latest JDK was:
   
   jdk-6u20-windows-i586.exe
   
#. Click through the installer you will need to set an acceptance a license agreement and so forth.
   By default this will install to:     
   
   C:\\Program Files\\Java\\jdk1.6.0_20\\
     
#. Optional – Java Advanced Imaging is used by GeoTools for raster support. If you install JAI 1.1.3 
   performance will be improved:   
   
   https://jai.dev.java.net/binary-builds.html
   
   Both a JDK and JRE installer are available:   
   jai-1_1_3-lib-windows-i586-jdk.exe
   jai-1_1_3-lib-windows-i586-jre.exe
     
#. Optional – ImageIO Is used to read and write raster files. GeoTools uses version 1_1 of the
   ImageIO library:
   
   https://jai-imageio.dev.java.net/binary-builds.html
   
   Both a JDK and JRE installer are available:   
   jai_imageio-1_1-lib-windows-i586-jdk.exe 
   jai_imageio-1_1-lib-windows-i586-jre.exe

Eclipse
=======

.. sidebar:: Lab

   In a lab setting you instructor will have downloaded these files for you, and often have a ready
   to go Eclipse zipped up and ready to use.
   
Eclipse is a popular integrated development environment most often used for all kinds of Java
development. For this tutorial we are doing straight up Java programming using the smallest
download available - if you already have an Eclipse download please go ahead and use it and
switch to the “Java Perspective”.
   
#. Visit the Eclipse download page (http://www.eclipse.org/downloads/) and download “Eclipse IDE for
   Java developers”.
   
   These instructions were written with the Eclipse Helios 3.6.0 release.
   
#. Eclipse does not provide an installer; just a directory to unzip and run.
#. To start out with create the folder C:\\java to keep all our java development in one spot.
#. Unzip the downloaded eclipse-java-galileo-SR1-win32.zip file to your C:\\java directory – the
   folder C:\\java\\eclipse will be created.
#. Navigate to C:\\java\\eclipse and right-click on the eclipse.exe file and select
   Send To->Desktop (create shortcut).
#. Open up the eclipse.ini file (notepad will be fine) and change the following line::

     -Xmx756m
   
   If you have plenty of memory to burn on development you may wish to provide yourself some more memory.

#. Double click on your desktop short cut to start up eclipse.
#. When you start up eclipse for the first time it will prompt you for a workspace. To keep our
   java work in one spot you can type in:
   
   C:\\java\\workspace
   
#. On the Welcome view press Workbench along the right hand side and we can get started

M2Eclipse
---------

.. sidebar:: Lab

  The bundled eclipse includes M2Eclipse you may skip this section.
  
Maven is build system for Java which is very good at managing dependencies. The GeoTools library is
plugin based and you get to pick and choose what features you need for your application. While this
is useful when determining just what is needed for delivery - it can be a pain to manage by hand
so we encourage the use of a tool such as maven.

In previous years we used the command line (gasp!) when working with maven. This year we are going
to be using the M2Eclipse plugin from Sonyatype.

To install the M2Eclipse plugin:

#. Open the *Install* dialog using :menuselection:`Select Help --> Install New Software` from the
   menubar.

#. In the *work with:* field enter the update site url:
    
   m2eclipse - http://m2eclipse.sonatype.org/sites/m2e
   
#. You be prompted by an *Add Repository* dialog, check the Name and Location and press OK

#. From the list of available plugins and components select *Maven Integration for Eclipse* and
   press *Next*

#. The *Install Details* page checks to see if the plugin will work with you eclipse, press *Next*

#. For *Review Licenses* we get check *I accept the terms of the license agreement* and *Finish*

#. The *Installing Software* dialog will download the software, when it is ready Eclipse will ask
   you to restart your IDE

At the end of this workbook we offer two alternatives to using the M2Eclipse plugin:
* Using maven from the command line
* Downloading GeoTools and throwing out the parts that conflict

Quickstart
==========

For this Quickstart we are going to produce a simple maven project, hook it up to GeoTools, and
then display a shapefile.

This tutorial is really focused on your development environment and making sure you have GeoTools
ready to go. We will cover what a shapefile is and how the map is displayed shortly.

Creating a Simple Maven project
-------------------------------

Maven works by asking you to describe your project, the name, the version number, where the source
code is, how you want it packaged, and what libraries it makes use of. Based on the description it
can figure out most things: how to compile your code, creating javadocs, or even downloading the
library jars for you.

To use M2Eclipse plugin to create a create a new maven project:

#. File > New > Other from the menu bar

#. Select the wizard *Maven > Maven Project* and press *Next* to open the *New Maven Project* wizard

#. The *New Maven project* page defaults are fine, press *Next*

   .. image:: images/newmaven.jpg
   
#. Select the default *maven-archtype-quickstart* and press *Next*
 
   .. image:: images/archetype.jpg
   
#. The archtype acts a template using the parameters we supply to create the project.
   
   * Group Id: org.geotools
   * Artifact Id: tutorial
   * Version: 0.0.1-SNAPSHOT (default)
   * Package: org.geotools.tutorial
   
   .. image:: images/artifact.jpg
   
#. Press *Finish* to create the new project.
#. You can see that an application has been created; complete with *App.java* and a JUnit test case
#. Open up src/main/java and select *org.geotools.tutorial.App* and press the *Run* button in the
   toolbar::
   
     Hello World!
   
Adding Jars to your Project
---------------------------

.. sidebar:: Lab

   We are going to cheat in order to save time; the local maven repository has already been
   populated with the latest copy of geotools allowing us to run in "offline" mode.
   
   To turn on offline mode:
   
   #. Open :menuselection:`Windows --> Preferences`
   #. Select :guilabel:`Maven` preference page
   #. Ensure :guielabel:`offline` is checked
    
   This setting is useful when wanting to work quickly once everything is downloaded.
    
The *pom.xml* file is used to describe the care and feeding of your maven project; we are going to
focus on the dependencies needed for your project 

When downloading jars maven makes use of a "local repository" to store jars.

  ==================  ========================================================
     PLATFORM           LOCAL REPOSITORY
  ==================  ========================================================
     Windows XP:      :file:`C:\Documents and Settings\Jody\.m2\repository`
     Windows:         :file:`C:\Users\Jody\.m2\repository`
     Linux and Mac:   :file:`~/.m2/repository`
  ==================  ========================================================

When downloading jars maven makes use of public maven repositories on the internet where projects
such as GeoTools publish their work.

#. Open up :file:`pom.xml` in your new project. You can see some of the information we entered
   earlier.
   
   .. image:: images/pomOverview.jpg
   
#. This editor allows you to describe all kinds of things; in the interest of time we are going to
   skip the long drawn out explanation and ask you to click on the :guilabel:`pom.xml` tab.
   
   .. code-block:: xml
   
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>

      <groupId>org.geotools</groupId>
      <artifactId>tutorial</artifactId>
      <version>0.0.1-SNAPSHOT</version>
      <packaging>jar</packaging>
    
      <name>tutorial</name>
      <url>http://maven.apache.org</url>
    
      <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>
    
      <dependencies>
        <dependency>
          <groupId>junit</groupId>
          <artifactId>junit</artifactId>
          <version>3.8.1</version>
          <scope>test</scope>
        </dependency>
      </dependencies>
    </project>

#. To make use of GeoTools we are going to add three things to this pom.xml file.
   
#. At the top after moduleVersion add a *properties* element defining the version of GeoTools that
   we want to use (|version| for this example).
   
   .. literalinclude:: artifacts/pom.xml
        :language: xml
        :start-after: <url>http://maven.apache.org</url>
        :end-before: <dependencies>
        
#. We are going to add a dependence to GeoTools :file:`gt-main` and :file:`gt-swing` jars. Note we
   are making use of the geotools.version defined above.
   
   .. literalinclude:: artifacts/pom.xml
        :language: xml
        :start-after: </properties>
        :end-before: <repositories>
    
#. Finally we need to list the external *repositories* where maven can download GeoTools and and
   other required jars from.

   .. literalinclude:: artifacts/pom.xml
        :language: xml
        :start-after: </dependencies>
        :end-before: </project>

#. Here is what the completed :file:`pom.xml` looks like:

   .. literalinclude:: artifacts/pom.xml
        :language: xml
   
   
  