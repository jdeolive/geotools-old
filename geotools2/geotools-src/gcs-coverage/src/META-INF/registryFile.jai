#
# The registry file for Geotools Grid Coverage implementation
# This file is automatically parsed by JAI under initialization
#
# Version: $Id: registryFile.jai,v 1.3 2003/07/18 13:49:56 desruisseaux Exp $
#
############################################################################
#
# Each line can be in one of the formats described below. Space or tab
# characters separate keywords in each line. The comment character is
# '#'; on each line all characters following the first comment character
# are ignored. The file must be encoded in UTF-8.
#
# 1. To register descriptors :
#
#	descriptor <descriptor-class-name>
#
# 2. To register factory objects under a product against a specific mode :
#
#	<registry-mode-name>	<factory-class-name>	<descriptor-name>
#
############################################################################
#
# Image operation descriptors :
#
descriptor  org.geotools.gp.jai.CombineDescriptor
descriptor  org.geotools.gp.jai.HysteresisDescriptor
descriptor  org.geotools.gp.jai.NodataFilterDescriptor


#
# "rendered" factory objects
#
rendered  org.geotools.gp.jai.CombineCRIF       org.geotools  org.geotools.Combine       Combine
rendered  org.geotools.gp.jai.HysteresisCRIF    org.geotools  org.geotools.Hysteresis    Hysteresis
rendered  org.geotools.gp.jai.NodataFilterCRIF  org.geotools  org.geotools.NodataFilter  NodataFilter
