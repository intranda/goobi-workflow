<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:goobi="http://www.goobi.io/logfile" exclude-result-prefixes="fo">
	<xsl:output method="xml" indent="yes" />
	<xsl:template match="goobi:processes">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<!-- <fo:layout-master-set> <fo:simple-page-master master-name="A5-landscape" page-width="14.8cm" page-height="21.0cm" margin-left="1cm" margin-top="1cm" 
				margin-right="1cm"> <fo:region-body /> </fo:simple-page-master> </fo:layout-master-set> -->

			<fo:layout-master-set>
				<fo:simple-page-master master-name="A5" page-width="14.8cm" page-height="21.0cm" margin-left="1cm" margin-top="1cm" margin-right="1cm">
					<fo:region-body />
				</fo:simple-page-master>
			</fo:layout-master-set>

			<xsl:if test="goobi:process/goobi:batch">
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
						
						<fo:block font-weight="bold" font-size="16pt" margin-top="20pt" text-align="center">
							<xsl:value-of select="goobi:process/goobi:project" />
							<xsl:text> [Batch: </xsl:text>
							<xsl:value-of select="goobi:process/goobi:batch" />
							<xsl:text>]</xsl:text>
						</fo:block>
						<fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt" />
						<fo:block margin-top="20pt" font-size="9pt">
							<fo:table line-height="13pt" table-layout="fixed">
								<fo:table-column column-width="4cm" />
								<fo:table-column column-width="9cm" />
								<fo:table-body>
								
									<fo:table-row>
										<fo:table-cell>
											<fo:block>
												<xsl:text>Goobi batch identifier: </xsl:text>
											</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block>
												<xsl:value-of select="goobi:process/goobi:batch" />
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
								
									<fo:table-row>
										<fo:table-cell>
											<fo:block>Date of creation:</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block>
												<xsl:value-of select="goobi:process/goobi:creationDate" />
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
									
								</fo:table-body>
							</fo:table>
						</fo:block>
						
						<fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt" margin-bottom="20pt" />
						
						<xsl:variable name="barcodebatch" select="goobi:process/goobi:batch" />
						<fo:block text-align="center">
							<fo:instream-foreign-object>
								<barcode:barcode xmlns:barcode="http://barcode4j.krysalis.org/ns" message="batch:{$barcodebatch}">
									<barcode:code128>
										<barcode:module-width>0.21mm</barcode:module-width>
										<barcode:height>20mm</barcode:height>
									</barcode:code128>
								</barcode:barcode>
							</fo:instream-foreign-object>
						</fo:block>
						
						<fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt" margin-bottom="20pt" />
						
					</fo:flow>
				</fo:page-sequence>
			</xsl:if>

			<xsl:for-each select="goobi:process">
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
						
						<fo:block font-weight="bold" font-size="16pt" margin-top="20pt" text-align="center">
							<xsl:value-of select="goobi:title" />
							<xsl:text> [Batch: </xsl:text>
							<xsl:value-of select="goobi:batch" />
							<xsl:text>]</xsl:text>
						</fo:block>
						<fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt" />
						<fo:block margin-top="20pt" font-size="9pt">
							<fo:table line-height="13pt" table-layout="fixed">
								<fo:table-column column-width="4cm" />
								<fo:table-column column-width="9cm" />
								<fo:table-body>
									
									<fo:table-row>
										<fo:table-cell>
											<fo:block>Project:</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block>
												<xsl:value-of select="goobi:project" />
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
									
									<fo:table-row>
										<fo:table-cell>
											<fo:block>Goobi batch number:</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block>
												<xsl:value-of select="goobi:batch" />
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
									
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
									
									<fo:table-row>
										<fo:table-cell>
											<fo:block>Metadata schema:</fo:block>
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
													<fo:block>Catalogue identifier:</fo:block>
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
													<fo:block>Publication type:</fo:block>
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
													<fo:block>Main title:</fo:block>
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
													<fo:block>Publishing place:</fo:block>
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
													<fo:block>Shelfmark:</fo:block>
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
						
						
						<fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt" margin-bottom="20pt" />
						
						<xsl:variable name="barcodebatch2" select="goobi:batch" />
						<fo:block text-align="center">
							<fo:instream-foreign-object>
								<barcode:barcode xmlns:barcode="http://barcode4j.krysalis.org/ns" message="{$barcodebatch2}">
									<barcode:code128>
										<barcode:module-width>0.21mm</barcode:module-width>
										<barcode:height>20mm</barcode:height>
									</barcode:code128>
								</barcode:barcode>
							</fo:instream-foreign-object>
						</fo:block>
						
						<fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt" margin-bottom="20pt" />
						
						<xsl:variable name="itembarcode" select="goobi:title" />
						<fo:block text-align="center">
							<fo:instream-foreign-object>
								<barcode:barcode xmlns:barcode="http://barcode4j.krysalis.org/ns" message="{$itembarcode}">
									<barcode:code128>
										<barcode:module-width>0.21mm</barcode:module-width>
										<barcode:height>20mm</barcode:height>
									</barcode:code128>
								</barcode:barcode>
							</fo:instream-foreign-object>
						</fo:block>
						
						<fo:block border-top-width="1pt" border-top-style="solid" border-top-color="#cccccc" margin-top="20pt" margin-bottom="20pt" />
						
					</fo:flow>
				</fo:page-sequence>
			</xsl:for-each>
		</fo:root>
	</xsl:template>
</xsl:stylesheet>

