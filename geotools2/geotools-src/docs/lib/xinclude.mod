<!-- ...................................................................... -->
<!-- XInclude definition for Simplified DocBook DTD V1.0 .................. -->

<!--
        Generguide - Generic Software Developers' Guide
        http://generguide.sourceforge.net/
        (C) 2003, Generic Software Developers'
                  Guide Project Management Committee (PMC)

        xinclude.mod,v 1.2 2003/08/31 20:14:01 kobit Exp

        This file is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
        Lesser General Public License for more details.

        If you modify the XInclude MOD in any way, except for
        declaring and referencing additional sets of general entities and
        declaring additional notations, label your DTD as a variant of
        this xinclude.mod.

        Please direct all questions, bug reports, or suggestions for
        changes to the generguide-devel@lists.sourceforge.net mailing list.
        For more information, see http://generguide.sourceforge.net/.

        Author: Artur Hefczyc <kobit@users.sourceforge.net>
        Content of this file is based on article availble under
        following address: http://www.sagehill.net/docbookxsl/ModularDoc.html
-->

<!-- ...................................................................... -->

<!--
     This is the driver file for the XInclude mod for V1.0 Simplified
     DocBook XML DTD. Please use the following formal public identifier
     to identify it:

     "-//GenerGuide//ELEMENTS XInclude for SDocBook V1.0//EN"

     For example:

     <!ENTITY % xinclude
        PUBLIC "-//GenerGuide//ELEMENTS XInclude for SDocBook V1.0//EN"
        "http://generguide.sourceforge.net/xinclude/1.0/xinclude.mod">

     This mod is well tested with SDocBook V1.0, however it should work
     with other versions too.
-->

<!-- ...................................................................... -->




<!ELEMENT xi:include (xi:fallback?)>
<!ATTLIST xi:include
    xmlns:xi   CDATA       #FIXED    "http://www.w3.org/2001/XInclude"
    href       CDATA       #REQUIRED
    parse      (xml|text)  "xml"
    encoding   CDATA       #IMPLIED>

<!ELEMENT xi:fallback ANY>
<!ATTLIST xi:fallback
    xmlns:xi   CDATA   #FIXED   "http://www.w3.org/2001/XInclude" >

<!ENTITY % local.chapter.class "| xi:include">
<!ENTITY % local.section.class "| xi:include">
<!ENTITY % local.divcomponent.mix "| xi:include">
