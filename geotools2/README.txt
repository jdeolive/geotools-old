        GeoTools2 - Monthly release - @VERSION@
	=====================================

Thank you for downloading a GeoTools 2.0 Beta.

GeoTools 2.0 is now releasing betas at the start of each month, this release was
created on: @DATE@

This release contains only a subset of the current GeoTools 2.0 codebase.

The full GeoTools 2.0 codebase is made up of over 30 modules; 
Only the modules which are considered to be stable will be included in each
release.  In a binary release the most important of these are merged together to
create the gt-main jar, the others are included as optional extra jars.

These optional jars typicaly provide support for specific data formats or sources
but they may also provide specific additional functionality.

For this release the following modules comprise the main set:
@REQUIRED@

The following modules comprise the 'optional' set:
@OPTIONAL@

With these set, we have excellent support for a wide range of data types, a 
flexible feature model, a powerful renderer and a host of other capabilities.

You can find out more about each of the major modules by going to:
http//modules.geotools.org  Each module has its own homepage with full javadoc,
test reports and source code.

NOTE
====
The details contained in the rest of this file are provided as a quick start
reference.  For full details on requirements and build process please see
the developers' guide at:
http://geotools.sourceforge.net/gt2docs/developersguide.html

REQUIREMENTS
============
To use the geotools libraries you will need:
Java 1.4.x or higher.
The Java Advanced Imaging Libraries (JAI)
The Java Advanced Imaging Image I/O Tools

The JAI and the JAI I/O Tools are standard extensions to Java created by SUN.  
They have versions specific to different platforms so we cannot distribute them
as part of the release. You can obtain the latest versions of both of these from:
http://java.sun.com/products/java-media/jai/current.html

MODULE SPECIFIC REQUIREMENTS
============================
Oracle:
Due to licensing restrictions, we are not able to distribute all the jars needed
by the Oracle module.  
Please see the readme file in that module for more details.

BUILDING
========
In order to build GeoTools you will need a copy of Maven.  Maven is a project
management tool from the Apache group.  You can obtain a copy of maven from: 
http://maven.apache.org/start/download.html

At the time of writing, the build process was known to work with Maven 1.0 RC-1

Once you have maven installed, you can perform a full build by moving to the
geotools2 folder and typing:
maven build

NOTE: The build may fail the first time, please try at least twice. 
For more information on common problems which can occur when building, see the
FAQ at:
http://www.geotools.org/index.php?module=articles&func=view&catid=37&itemtype=4


FUTURE CHANGES
==============
This is only a beta release and, whilst the API has stabilized somewhat, it is 
still undergoing change, so code written against this release may have to be 
modified to work with future releases.  We will always document what changes are
necessary and we will leave existing API methods working but deprecated where
possible.

Some APIs may change more radically.  For example, as we bring the GUI
components to maturity we will probably have to overhaul the rendering 
architecture.  In addition, we are planning to use a set of geometry interfaces
in the near future - we will provide as much information as possible when these
changes occur.

You will find - in a few places - code which is already marked as deprecated.  
Please do not use these methods (even if we do in some tests and examples) as
there is a good chance these will be removed in the next release.

GETTING INVOLVED
================
If you are interested in the future development of GeoTools2 then feel free to
join the geotools-devel mailing list and join in the frequent IRC sessions.

We welcome contributions of new modules as well as keen developers who want to
work on the project as a whole.

Note that the devel list is for developers and not users.  If you have more
general queries then please send them to the gt2-users mailing list instead.

You can find out more about the mailing lists and recent news by going to the
GeoTools homepage at:
http://www.geotools.org

Good luck and many thanks for your interest in GeoTools2

The GeoTools2 Project Management Committee
