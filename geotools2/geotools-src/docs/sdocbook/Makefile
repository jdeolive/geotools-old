#
# $Id: Makefile,v 1.1 2002/12/05 10:48:55 camerons Exp $
# Makefile for building sgml files that contain <xi:include> tags
# To run:
# 1. Ensure that you are running on a unix compatable system.
# 2. Ensure that you have xsltproc installed.
# 3. cd <this directory>
# 4. make
# 5. make install
# 6. view output in <this directory>/../www
#

#www/masterdesign.html : sdocbook/masterdesign.sgml
#	xsltproc --novalid --xinclude -o www/masterdesign.html /home/cameron/work/geotools2/extbin/gtbuild/docbook-xsl/html/docbook.xsl sdocbook/masterdesign.sgml

INSTALL_DIR = "../www"

docs = developersguide.html masterdesign.html glossary.html

all : $(docs) install

$(docs) : %.html : %.sgml
	xsltproc --novalid --xinclude -o $@ /home/cameron/work/geotools2/extbin/gtbuild/docbook-xsl/html/docbook.xsl $<

clean :
	rm -f *.html

install :
	mkdir -p $(INSTALL_DIR)
	cp *.html $(INSTALL_DIR)
