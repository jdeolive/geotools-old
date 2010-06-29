.. _netbeans-quickstart:

**********************
 Netbeans Quickstart 
**********************

:Author: Jody Garnett
:Author: Micheal Bedward
:Thanks: geotools-user list
:Version: |release|
:License: Create Commons with attribution
   
Welcome NetBeans Developers
===========================

.. sidebar:: Lab

   In a lab setting the instructor will provide you with a local repository to place in your
   home directory. This step will allow you to avoid the long download.

Welcome to Geospatial for Java -this workbook is aimed at Java developers who are new to geospatial
and would like to get started.

We are going to start out carefully with the steps needed to set up your IDE; and are pleased this
year to cover both NetBeans and Eclipse. The build tool Maven (http://maven.apache.org/) is our
preferred option for downloading and managing jars for GeoTools projects because there tend to a
large number of jars involved. If you are already familiar with Maven that is an advantage but if
not, don't worry, we will be explaining things step by step and we will also document how to set
up things by hand as an alternative to using Maven.

Extra care has been taken to make this year's tutorial visual right from the get go. While these
examples will make use of Swing please be assured that that this is only an aid in making the
examples easy and fun to use so if your own work is based on another GUI framework the material
that we cover here will still be relevant.

These sessions are applicable to both server side and client side development.

We would like thank members of the GeoTools users list for their feedback while were preparing the
course material, with special thanks to Tom Williamson for reviewing early drafts.
    
Java Install
============

We are going to be making use of Java so if you don't have a Java Development Kit installed now is
the time to do so. Even if you have Java installed already check out the optional Java Advanced
Imaging and Java Image IO section – both of these libraries are used by GeoTools.

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
   
   * jai-1_1_3-lib-windows-i586-jdk.exe
   * jai-1_1_3-lib-windows-i586-jre.exe
     
#. Optional – ImageIO Is used to read and write raster files. GeoTools uses version 1_1 of the
   ImageIO library:
   
   https://jai-imageio.dev.java.net/binary-builds.html
   
   Both a JDK and JRE installer are available:   
   
   * jai_imageio-1_1-lib-windows-i586-jdk.exe 
   * jai_imageio-1_1-lib-windows-i586-jre.exe
   
NetBeans Install
================

The NetBeans IDE is a popular choice for Java development and features excellent Maven integration.

#. Download NetBeans (The Java SE download will be fine).

     http://www.netbeans.org/ 

#. At the time of netbeans-6.9-ml-javase-windows.exe was the latest installer.

#. Click through the steps of the installer. You will notice it will pick up on the JDK you
   installed earlier.

Quickstart
==========

The GeoTools development community uses the build tool Maven which is integrated into the latest
releases of NetBeans.

The advantages of using Maven are:

* You only download as much of GeoTools as your application requires Jars are downloaded to a single
  location in your home directory (in a hidden folder called .m2/repository)

* Source code and javadocs are automatically downloaded and hooked up

Although Maven is a build tool it works by describing the contents of a project. This is a different
approach then used by the Make or Ant tools which list the steps required to build.

Part of the description of a project is the required jars and a repository on the internet where
they can be downloaded from. We will be using these facilities to bring GeoTools jars as needed
into our project.

Creating the Project
--------------------

Let's get started:
#. Start with File > New Project to open the New Project wizard
#. Select the Maven category; choose Maven Project and press Next.

   .. image:: images/nbNewProject.png

#. On the Maven Archetype page select “Maven Quickstart Archetype” and press Next.

   .. image:: images/nbNewProjectArchetype.png
   
#. We can now fill in the blanks

   * Project name: tutorial
   * GroupId: org.geotools

#. Click on the Finish button and the new project will be created.

#. If this is your first time using Maven with NetBeans it will want to confirm that it is okay to
   use the copy of Maven included with NetBeans (it is also possible to use an external Maven
   executable from within Netbeans which is convenient if, for instance, you want to work with the
   same version of Maven within the IDE and from the command line).

Adding Jars to Your Project
---------------------------

.. sidebar:: Lab

   We are going to cheat in order to save time; the local maven repository has already been
   populated with the latest copy of geotools allowing us to run in "offline" mode.
   
   To turn on offline mode:
   
   #. Open :menuselection:`Windows --> Preferences`
   #. Select :guilabel:`Maven` preference page
   #. Ensure :guilabel:`offline` is checked
    
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

#. The next step is for us to make it a GeoTools project by adding information to Maven's project
   description file (“project object model” in Maven-speak) - pom.xml
   
   In the Projects panel open up the Project Files folder and double click on pom.xml to open it.
   
#. We are going to start by defining the version number of GeoTools we wish to use. 

   .. literalinclude:: artifacts/pom.xml
        :language: xml
        :start-after: <url>http://maven.apache.org</url>
        :end-before: <dependencies>
  
  If you make any mistakes when editing the xml file you'll see that your project will be renamed
  “<Badley formed Maven project>” in the Projects window. You can choose “Format” as a quick way to
  check if the tags line up. Or just hit undo and try again. 
  
#. Next we add two GeoTools modules to the dependencies section: gt-shapefile and gt-swing for our
   project.

   .. literalinclude:: artifacts/pom.xml
        :language: xml
        :start-after: </properties>
        :end-before: <repositories>
  
#. And the repositories where these jars can be downloaded from.

   .. literalinclude:: artifacts/pom.xml
        :language: xml
        :start-after: </dependencies>
        :end-before: </project>
    
#. You can now right click on Libraries in the Projects window, then Download missing Dependencies
   from the pop-up menu. When downloading it will check the repositories we have listed
   above.

#. We will continue to add dependencies on different parts of the GeoTools library as we work through these exercises; this fine grain control and the ability to download exactly what is needed is one of the advantages of using Maven.

#. Here is what the completed :file:`pom.xml` looks like:

   .. literalinclude:: artifacts/pom.xml
        :language: xml
   
   * You may find cutting and pasting from the documentation to be easier then typing.
   
   * You may also :download:`download this file <artifacts/pom.xml`

Quickstart Application
-----------------------

Now that your environment is setup we can put together a simple Quickstart. This example will display a shapefile on screen.

#. Create the org.geotools.tutorial.Quickstart class using your IDE.
   
#. Fill in the following code:

   .. literalinclude:: ../../src/main/java/org/geotools/tutorial/quickstart/Quickstart.java
        :language: java

#. Build the application and check that all is well in the Output window.

#. We need to download some sample data to work with. The http://www.naturalearthdata.com/ project
   is a great project supported by the North American Cartographic Information Society.
   
   * http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/110m/cultural/110m-cultural.zip 
   
   Please unzip the above data into a location you can find easily such as the desktop.

#. Run the application to open a file chooser. Choose a shapefile from the example dataset.

   .. image:: images/QuickstartOpen.jpg
   
#. The application will connect to your shapefile, 1.produce a map context and display the shapefile.

   .. image:: images/QuickstartMap.jpg
   
#. A couple of things to note about the code example:
   
   * The shapefile is not loaded into memory – instead it is read from disk each and every time it is needed
     This approach allows you to work with data sets larger then available memory.
   
   * We are using a very basic display style here that just shows feature outlines. In the examples that follow we will see how to specify more sophisticated styles.

   
Things to Try
=============

Each tutorial consists of very detailed steps followed by a series of extra questions. If you get
stuck at any point please ask your instructor; or sign up to the geotools-users email list.

Here are some additional challenges for you to try:

* Try out the different sample data sets

* You can zoom in, zoom out and show the full extents and Use the select tool to examine individual
  countries in the sample countries.shp file

* Download the largest shapefile you can find and see how quickly it can be rendered. You should
  find that the very first time it will take a while as a spatial index is generated. After that
  performance should be very good when zoomed in.
  
* Fast: We know that one of the ways people select a spatial library is based on speed. By design
  GeoTools does not load the above shapefile into memory (instead it streams it off of disk
  each time it is drawn using a spatial index to only bring the content required for display).
  
  If you would like to ask GeoTools to cache the shapefile in memory try the following code:

    .. literalinclude:: ../../src/main/java/org/geotools/tutorial/quickstart/QuickstartCache.java
     :language: java
     :start-after: // docs start cache
     :end-before:  // docs end cache
  
  * For the above example to compile you will need the following import:
    
    .. code-block:: java

       import org.geotools.data.CachingFeatureSource;
  
  
* Try and sort out what all the different “side car” files are – and what they are for. The sample
  data set includes “shp”, “dbf” and “shx”. How many other side car files are there?

  .. This exercise asks users to locate the geotools user guide or wikipedia
  
* Advanced: The use of FileDataStoreFinder allows us to work easily with files. The other way to do
  things is with a map of connection parameters. This techniques gives us a little more control over
  how we work with a shapefile and also allows us to connect to databases and web feature servers.

    .. literalinclude:: ../../src/main/java/org/geotools/tutorial/quickstart/QuickstartNotes.java
     :language: java
     :start-after: // start datastore
     :end-before:  // end datastore
     

* Important: GeoTools is an active open source project – you can quickly use maven to try out the
  latest nightly build by changing your pom.xml file to use a “SNAPSHOT” release.
  
  At the time of writing |version|-SNAPSHOT under active development.
  
  .. codeblock: java
  
   .. literalinclude:: artifacts/pom2.xml
        :language: xml
        :start-after: <url>http://maven.apache.org</url>
        :end-before: <dependencies>
    
  You will also need to change your pom.xml file to include the following snapshot repository:
  
     .. literalinclude:: artifacts/pom2.xml
        :language: xml
        :start-after: </dependencies>
        :end-before: </project>
        
* NetBeans has an interesting feature to show how the dependency system works - Right click on
  Libraries and choose Show Dependency
  
     .. image:: images/nbGraph.png
  
  We will be making use of some of the project is greater depth in the remaining tutorials.
