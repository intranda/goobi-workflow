<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:goobi="http://www.goobi.io/logfile" version="1.0" exclude-result-prefixes="fo">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="goobi:process">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <!-- general layout -->
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A5" page-width="14.8cm" page-height="21.0cm" margin-left="1cm" margin-top="1cm" margin-right="1cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <!-- // general layout -->
            <fo:page-sequence master-reference="A5">
                <fo:flow flow-name="xsl-region-body" font-family="opensans, unicode">
                    
                    <fo:block-container position="fixed" left="1cm" top="19.2cm">
                        <fo:block>
                                <fo:external-graphic src="logo.png" content-width="22mm"/>
                        </fo:block>
                    </fo:block-container>
                    <fo:block-container position="fixed" left="11.85cm" top="19.8cm">
                        <fo:block font-size="7pt">
                            https://goobi.io
                        </fo:block>
                    </fo:block-container>
                    
                    <!-- title of process -->
                    <fo:block text-align="center" font-weight="bold" font-size="16pt" margin-top="20pt">
                        <xsl:value-of select="goobi:title"/>
                    </fo:block>
                    <!-- // title of process -->
                    <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt"/>
                    <!-- table with all details -->
                    <fo:block margin-top="20pt" font-size="9pt">
                        <fo:table line-height="13pt" table-layout="fixed">
                            <fo:table-column column-width="4cm"/>
                            <fo:table-column column-width="9cm"/>
                            <fo:table-body>
                                <!-- project -->
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>
                                            Projekt:
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="goobi:project"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <!-- // project -->
                                
                                <!-- goobi identifier -->
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>
                                            Goobi identifier:
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="@processID"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <!-- // goobi identifier -->
                                
                                <!-- creation time -->
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>
                                            Anlegedatum:
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="goobi:creationDate"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <!-- // creation time -->
                                
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>Regelsatz:</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="goobi:ruleset" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                
                                <xsl:for-each select="goobi:properties/goobi:property">
                                    <xsl:if test="@propertyIdentifier ='Template'">
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Workflow:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>
                                                    <xsl:value-of select="@value" />
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>
                                
                                <xsl:for-each select="goobi:metadatalist/goobi:metadata">
                                    <xsl:if test="@name ='CatalogIDDigital'">
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Katalog-Identifier:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>
                                                    <xsl:value-of select="." />
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>
                                
                                <xsl:for-each select="goobi:metadatalist/goobi:metadata">
                                    <xsl:if test="@name ='DocStruct'">
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Publikationstyp:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>
                                                    <xsl:value-of select="." />
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>
                                
                                <xsl:for-each select="goobi:metadatalist/goobi:metadata">
                                    <xsl:if test="@name ='TitleDocMain'">
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Haupttitel:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>
                                                    <xsl:value-of select="." />
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>
                                
                                <xsl:for-each select="goobi:metadatalist/goobi:metadata">
                                    <xsl:if test="@name ='PlaceOfPublication'">
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Erscheinungsort:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>
                                                    <xsl:value-of select="." />
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>

                                <xsl:for-each select="goobi:metadatalist/goobi:metadata">
                                    <xsl:if test="@name ='shelfmarksource'">
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Signatur:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>
                                                    <xsl:value-of select="." />
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>
                                
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <!-- // table with all details -->
                    <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt" margin-bottom="20pt"/>
                    <!-- Barcode generation for process title -->
                    <xsl:variable name="barcodemessage1" select="goobi:title"/>
                    <fo:block text-align="center">
                        <fo:instream-foreign-object>
                            <barcode:barcode xmlns:barcode="http://barcode4j.krysalis.org/ns" message="{$barcodemessage1}">
                                <barcode:code128>
                                    <barcode:module-width>0.21mm</barcode:module-width>
                                    <barcode:height>20mm</barcode:height>
                                </barcode:code128>
                            </barcode:barcode>
                        </fo:instream-foreign-object>
                    </fo:block>
                    <!-- // Barcode generation for process title -->
                    <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="10pt" margin-bottom="20pt"/>
                    
                    
                </fo:flow>
            </fo:page-sequence>
            
            <xsl:if test="goobi:log/goobi:file">
              <fo:page-sequence master-reference="A5">
                <fo:flow flow-name="xsl-region-body" font-family="opensans, unicode">
            
                  <!-- title of process -->
                  <fo:block text-align="center" font-weight="bold" font-size="11pt" margin-top="0pt">
                      <xsl:text disable-output-escaping="yes">Schadensdokumentation für: </xsl:text>
                      <xsl:value-of select="goobi:title"/>
                  </fo:block>
                  <!-- // title of process -->
                  <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="10pt"/>
            
                  <!-- show all images uploaded into the process log -->
                  <xsl:for-each select="goobi:log/goobi:file">
                    <xsl:if test="not(position() > 20)">
                      <fo:block text-align="center" font-size="12pt" margin-top="10pt">
                        <fo:external-graphic src="url('{@url}')" content-height="80mm"/>
                      </fo:block>
                      <fo:block text-align="center" font-size="9pt" margin-top="5pt">
                        <xsl:value-of select="@comment" />
                      </fo:block>
                    </xsl:if>
                  </xsl:for-each>
                  <!-- // show all images uploaded into the process log -->
            
                </fo:flow>
              </fo:page-sequence>
            </xsl:if>
            
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
