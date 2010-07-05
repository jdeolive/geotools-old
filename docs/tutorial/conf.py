import sys, os
sys.path.append(os.path.abspath('..'))
from common import *

html_title='GeoTools %s Tutorial' % release
html_theme = 'geotools-tutorial'

# extension for pdf
# extensions = ['sphinx.ext.autodoc','rst2pdf.pdfbuilder']

# options for pdf
#pdf_documents = [
#  ('quickstart/eclipse', u'eclipseQuickstart', u'Eclipse Quickstart', u'Jody Garnett\\Micheal Bedward'),
#]