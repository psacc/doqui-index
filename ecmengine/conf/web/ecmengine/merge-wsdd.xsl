<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="xml" indent="yes" />

	<xsl:param name="source2" />

	<xsl:template match="/">

<deployment
    xmlns="http://xml.apache.org/axis/wsdd/"
    xmlns:java="http://xml.apache.org/axis/wsdd/providers/java">
 <globalConfiguration>
  <parameter name="sendMultiRefs" value="true"/>
  <parameter name="disablePrettyXML" value="true"/>
  <parameter name="adminPassword" value="admin"/>
  <parameter name="dotNetSoapEncFix" value="true"/>
  <parameter name="enableNamespacePrefixOptimization" value="false"/>
  <parameter name="sendXMLDeclaration" value="true"/>
  <parameter name="sendXsiTypes" value="true"/>
  <parameter name="attachments.implementation" value="org.apache.axis.attachments.AttachmentsImpl"/>
 </globalConfiguration>
 <handler name="LocalResponder" type="java:org.apache.axis.transport.local.LocalResponder"/>
 <handler name="URLMapper" type="java:org.apache.axis.handlers.http.URLMapper"/>
 <handler name="Authenticate" type="java:org.apache.axis.handlers.SimpleAuthenticationHandler"/>

			<xsl:copy-of select="child::*/child::*" />
			<xsl:copy-of select="document($source2)/child::*/child::*" />

 <transport name="http">
  <requestFlow>
   <handler type="URLMapper"/>
   <handler type="java:org.apache.axis.handlers.http.HTTPAuthHandler"/>
  </requestFlow>
  <parameter name="qs:list" value="org.apache.axis.transport.http.QSListHandler"/>
  <parameter name="qs:wsdl" value="org.apache.axis.transport.http.QSWSDLHandler"/>
  <parameter name="qs.list" value="org.apache.axis.transport.http.QSListHandler"/>
  <parameter name="qs.method" value="org.apache.axis.transport.http.QSMethodHandler"/>
  <parameter name="qs:method" value="org.apache.axis.transport.http.QSMethodHandler"/>
  <parameter name="qs.wsdl" value="org.apache.axis.transport.http.QSWSDLHandler"/>
 </transport>
 <transport name="local">
  <responseFlow>
   <handler type="LocalResponder"/>
  </responseFlow>
 </transport>

</deployment>

	</xsl:template>

</xsl:stylesheet>
