#
# The registry file for Geotools Grid Coverage implementation
# This file is automatically parsed by JAI under initialization
#
# Version: $Id: registryFile.jai,v 1.2 2003/07/11 16:57:47 desruisseaux Exp $
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


#
# "rendered" factory objects
#
rendered  org.geotools.gp.jai.CombineCRIF  org.geotools  org.geotools.Combine Combine
