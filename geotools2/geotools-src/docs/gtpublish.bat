
echo off
rem saxon: http://www.ibiblio.org/maven/saxon/jars/saxon-6.5.2.jar
rem resolver: http://xml.apache.org/commons/dist/resolver-latest.jar

set SRC=sdocbook
set LIBS=lib
set SAXON_CLASSPATH=%LIBS%/saxon-6.5.2.jar;%LIBS%/resolver-latest.jar;%LIBS%
set TARGET=www

echo "Checking environment..."
if not exist %TARGET%                        mkdir %TARGET%
if not exist %TARGET%\images                 mkdir %TARGET%\images
if not exist %TARGET%\images\core            mkdir %TARGET%\images\core
if not exist %TARGET%\images\defaultcore     mkdir %TARGET%\images\defaultcore
if not exist %TARGET%\images\design          mkdir %TARGET%\images\design
if not exist %TARGET%\images\developersguide mkdir %TARGET%\images\developersguide

echo "Copying images to target directory..."
xcopy /y /c /q /r %SRC%\images\design\*            %TARGET%\images\design
xcopy /y /c /q /r %SRC%\images\developersguide\*   %TARGET%\images\developersguide
xcopy /y /c /q /r ..\defaultcore\sdocbook\images\* %TARGET%\images\defaultcore
xcopy /y /c /q /r ..\core\sdocbook\images\*        %TARGET%\images\core
xcopy /y /c /q /r /s %SRC%\resources %TARGET%

echo "Including all parts into master documents..."

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/developersguide_all.xml %SRC%/developersguide.xml %LIBS%/xinclude.xsl

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/design_all.xml %SRC%/design.xml %LIBS%/xinclude.xsl

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/userguide_all.xml %SRC%/userguide.xml %LIBS%/xinclude.xsl

echo "Generating HTML pages..."

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/developersguide.html %TARGET%/developersguide_all.xml docbook-xsl/html/docbook.xsl section.autolabel=1 toc.section.depth=5

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/design.html %TARGET%/design_all.xml docbook-xsl/html/docbook.xsl section.autolabel=1 toc.section.depth=5

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/userguide.html %TARGET%/userguide_all.xml docbook-xsl/html/docbook.xsl section.autolabel=1 toc.section.depth=5

rem goto end

echo "Generating FOP files..."
rem docbook-xsl: http://umn.dl.sourceforge.net/sourceforge/docbook/docbook-xsl-1.62.0.tar.gz

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/developersguide.fo %TARGET%/developersguide_all.xml docbook-xsl/fo/docbook.xsl section.autolabel=1 toc.section.depth=5 paper.type=a4 hyphenate=false refentry.generate.name=1 refentry.generate.title=0 refentry.separator=1 refentry.xref.manvolnum=1 shade.verbatim=1 xref.with.number.and.title=0

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/design.fo %TARGET%/design_all.xml docbook-xsl/fo/docbook.xsl section.autolabel=1 toc.section.depth=5 paper.type=a4 hyphenate=false refentry.generate.name=1 refentry.generate.title=0 refentry.separator=1 refentry.xref.manvolnum=1 shade.verbatim=1 xref.with.number.and.title=0

%JAVA_HOME%\bin\java -cp %SAXON_CLASSPATH% com.icl.saxon.StyleSheet -x org.apache.xml.resolver.tools.ResolvingXMLReader -y org.apache.xml.resolver.tools.ResolvingXMLReader -r org.apache.xml.resolver.tools.CatalogResolver -w0 -u -o %TARGET%/userguide.fo %TARGET%/userguide_all.xml docbook-xsl/fo/docbook.xsl section.autolabel=1 toc.section.depth=5 paper.type=a4 hyphenate=false refentry.generate.name=1 refentry.generate.title=0 refentry.separator=1 refentry.xref.manvolnum=1 shade.verbatim=1 xref.with.number.and.title=0

goto end

:pdf

echo "Generating PDF files..."
rem FOP: http://sunsite.icm.edu.pl/pub/www/apache/dist/xml/fop/fop-current-bin.tar.gz
set FL=%LIBS%/fop/lib
set FOP_BOOTCLASSPATH=%FL%/xml-apis.jar;%FL%/xercesImpl-2.2.1.jar;%FL%/xalan-2.4.1.jar
rem set FOP_CLASSPATH=%LIBS%/fop/build/fop.jar;%FL%/batik.jar;%FL%/avalon-framework-cvs-20020806.jar;%FL%/jimi.jar;%FL%/jai_core.jar;%FL%/jai_codec.jar
set FOP_CLASSPATH=%LIBS%/fop/build/fop.jar;%FL%/batik.jar;%FL%/avalon-framework-cvs-20020806.jar;%FL%/jimi.jar

%JAVA_HOME%\bin\java -verbose:class -Xbootclasspath/p:%FOP_BOOTCLASSPATH% -cp %FOP_CLASSPATH% org.apache.fop.apps.Fop -d -fo %TARGET%/developersguide.fo -pdf %TARGET%/developersguide.pdf

rem %JAVA_HOME%\bin\java -Xbootclasspath/p:%FOP_BOOTCLASSPATH% -cp %FOP_CLASSPATH% org.apache.fop.apps.Fop -q -fo %TARGET%/design.fo -pdf %TARGET%/design.pdf

rem %JAVA_HOME%\bin\java -Xbootclasspath/p:%FOP_BOOTCLASSPATH% -cp %FOP_CLASSPATH% org.apache.fop.apps.Fop -q -fo %TARGET%/userguide.fo -pdf %TARGET%/userguide.pdf

:end
rem del /f/q %TARGET%\*_all.xml
rem del /f/q %TARGET%\*.fo
pause
