<!--
 This XSL contains a simple XInclude processor.

 the following code is certainly not conformant since:
 - it relies on ID attributes being named "id"
 - does not support parse='text'
 - does not merge infoset
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xi="http://www.w3.org/2001/XInclude"
		xmlns:xlink='http://www.w3.org/1999/xlink'
		xmlns='http://www.w3.org/1999/xhtml'
		version="1.0">
  
  <xsl:output method="xml" />
  
  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template> 
  
  <xsl:template match="xi:include">
    <xsl:variable name="href" select="@href"/>
    <xsl:variable name="parse" select="@parse"/>
    <xsl:choose>
      <xsl:when test="$parse='text'">
	<xsl:message>XInclude: parse='text' not supported</xsl:message>
      </xsl:when>
      <xsl:when test="$parse!='xml' and $parse!=''">
	<xsl:message>XInclude: Invalid value for the parse
	  attribute of xi:include element.</xsl:message>
      </xsl:when>
      <xsl:when test="$href != ''
		and ($parse='xml' or not(@parse))">
	<xsl:choose>
	  <xsl:when test="contains($href, '#')">
	    <xsl:variable name='uri' select="substring-before($href,'#')"/>
	    <xsl:variable name='fragment' select="substring-after($href,'#')"/>
	    <xsl:choose>
	      <xsl:when test="contains($fragment, '(')">
		<xsl:variable name='xscheme' select="substring-before($fragment,'(')"/>
		<xsl:variable name='param' select="substring-before(substring-after($fragment,'('),')')"/>
		<xsl:choose>
		  <xsl:when test="$xscheme='element' and not(contains($param, '/'))">
		    <xsl:message>XInclude: Including <xsl:value-of select="$uri"/> with element(<xsl:value-of select="$param"/>)</xsl:message>
		    <xsl:apply-templates select="document($uri)//*[@id=$param]"/>
		  </xsl:when>
		  <xsl:when test="$xscheme='element' and contains($param, '/')">
		    <xsl:variable name='element-name' select="substring-before($param, '/')" />
		    <xsl:variable name='position' select="substring-after($param, '/')" />
		    <xsl:choose>
		      <xsl:when test="$element-name=''">
			<xsl:call-template name='process-element-child-sequence'>
			  <xsl:with-param name='node-set' select="document($uri)"/>
			  <xsl:with-param name='position' select='$position'/>
			</xsl:call-template>
		      </xsl:when>
		      <xsl:otherwise>
			<xsl:call-template name='process-element-child-sequence'>
			  <xsl:with-param name='node-set' select="document($uri)//*[@id=$element-name]/*"/>
			  <xsl:with-param name='position' select='$position'/>
			</xsl:call-template>
		      </xsl:otherwise>
		    </xsl:choose>
		  </xsl:when>
		  <xsl:when test="$xscheme='xpointer'
			    and not(contains($param, '/'))
			    and not(contains($param, '['))
			    and not(contains($param, '('))">
		    <!-- really simple xpointer scheme support: xpointer(<local-name>) -->
		    <xsl:message>XInclude: Including <xsl:value-of select="$uri"/> with xpointer(<xsl:value-of select="$param"/>)</xsl:message>
		    <xsl:apply-templates select="document($uri)//*[local-name()=$param]"/>
		  </xsl:when>
		  <xsl:otherwise>
		    <xsl:message>XInclude: XPointer scheme <xsl:value-of select="$xscheme"/> not supported in <xsl:value-of select="$href"/></xsl:message>	  
		  </xsl:otherwise>
		</xsl:choose>
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:message>XInclude: Including <xsl:value-of select="$uri"/> with @id="<xsl:value-of select="$fragment"/>"</xsl:message>
		<xsl:apply-templates select="document($uri)//*[@id=$fragment]"/>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:message>XInclude: Including <xsl:value-of select="$href"/></xsl:message>	  
	    <xsl:apply-templates select="document($href)"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      <xsl:otherwise>
	<xsl:message>XInclude: Failed to get a value for the href <xsl:value-of select="$href"/>
	  attribute of xi:include element <xsl:value-of select="$parse"/>.</xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!--
  this template recurse in the child sequence of the xpointer
  scheme element()
  -->
  <xsl:template name='process-element-child-sequence'>
    <xsl:param name='node-set' />
    <xsl:param name='position' />
    
    <!-- if no nodes in the node set, not even worth trying -->
    <xsl:if test='count($node-set)'>
      <xsl:choose>
	<!-- are we done with recursing in the child sequence ? -->
	<xsl:when test="contains($position, '/')">
	  <xsl:call-template name='process-element-child-sequence'>
	    <xsl:with-param name='node-set'
			    select="$node-set[position()=number(substring-before($position, '/'))]/*"/>
	    <xsl:with-param name='position' select="substring-after($position, '/')"/>
	  </xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates select="$node-set[position()=$position]"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
    
  <!-- End of XInclude support -->
</xsl:stylesheet>
