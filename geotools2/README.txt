        GeoTools2 - Desideratum release
	===============================

Thank you for downloading the GeoTools 2.0 Beta 1.

GeoTools 2.0 has been in development for about 19 months but this is the first release that we have made.

This release contains only a subset of the current GeoTools 2.0 codebase.  It is specifically targeted at developers of server-side code and command line tools.  It does not contain interactive GUI components.

The full GeoTools 2.0 codebase is made up of over 30 modules; 17 of these, which are considered mature enough for general use, have been selected to be included in this release.

The 17 modules which will be included are:
core
defaultcore
cts-coordtrans
filter
gcs-gridcoverage
arcgrid
gml
gtopo30
oracle
opengis
postgis
shapefile
lite-rendering
sld
svg
resources
science

With this set, we will have excellent support for a wide range of data types, a flexible feature model, a powerful renderer and a host of other capabilities.

You can find out more about each of the major modules by going to http//modules.geotools.org  Each module has its own homepage with full javadoc, test reports and source code.

NOTE
====
The details contained in the rest of this file are provided as a quick start reference.  For full details on requirements and build process please see the developers' guide at:
http//geotools.sourceforge.net/gt2docs/developersguide.html

REQUIREMENTS
============
To use the geotools libraries you will need:
Java 1.4.x or higher.
The Java Advanced Imaging Libraries (JAI)
The Java Advanced Imaging Image I/O Tools

The JAI and the JAI I/O Tools are standard extensions to Java created by SUN.  They have versions specific to different platforms so we cannot distribute them as part of the release. You can obtain the latest versions of both of these from:
http//java.sun.com/products/java-media/jai/current.html

BUILDING
========
In order to build GeoTools you will need a copy of Maven.  Maven is a project management tool from the Apache group.  You can obtain a copy of maven from: 
http//maven.apache.org/start/download.html

At the time of writing, the build process was known to work with Maven Beta 10.

Once you have maven installed, you can perform a full build by moving to the geotools2 folder and typing:
maven build

For information on common problems which can occur when building, see the FAQ at
http://www.geotools.org/index.php?module=articles&func=view&catid=37&itemtype=4

FUTURE CHANGES
==============
This is our first release and, whilst the API has stabilized somewhat, it is still undergoing change, so code written against this release may have to be modified to work with future releases.  We will always document what changes are necessary and we will leave existing API methods working but deprecated where possible.

You will find - in a few places - code which is already marked as deprecated.  Please do not use these methods (even if we do in some tests and examples) as there is a good chance these will be removed in the next release.

GETTING INVOLVED
================
If you are interested in the future development of GeoTools2 then feel free to join the geotools-devel mailing list and join in the frequent IRC sessions.

We welcome contributions of new modules as well as keen developers who want to work on the project as a whole.

Note that the devel list is for developers and not users.  If you have more general queries then please send them to the gt2-users mailing list instead.

You can find out more about the mailing lists and recent news by going to the GeoTools homepage at:
http://www.geotools.org

Good luck and many thanks for your interest in GeoTools2

The GeoTools2 Project Management Committee
