<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:goobi="http://www.goobi.io/logfile"
    version="1.0"
    exclude-result-prefixes="fo"
>
    <xsl:output
        method="xml"
        indent="yes" />
    <xsl:template match="goobi:process">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <!-- general layout -->
            <fo:layout-master-set>
                <fo:simple-page-master
                    master-name="A4"
                    page-width="21cm"
                    page-height="29.7cm"
                    margin-left="1.5cm"
                    margin-top="1.5cm"
                    margin-right="1.5cm"
                    margin-bottom="1.5cm"
                >
                    <fo:region-body />
                </fo:simple-page-master>
            </fo:layout-master-set>
            <!-- // general layout -->
            <fo:page-sequence master-reference="A4">
                <fo:flow
                    flow-name="xsl-region-body"
                    font-family="opensans, unicode"
                >
                    <!-- thumbnail on right side -->
                    <fo:block-container
                        position="fixed"
                        left="11.5cm"
                        top="1cm"
                    >
                        <fo:block>
                            <fo:external-graphic
                                src="url('{goobi:representative/@url}')"
                                content-height="100mm" />
                        </fo:block>
                    </fo:block-container>
                    <!-- // thumbnail on right side -->
                    <!-- general process data -->
                    <fo:block-container
                        position="fixed"
                        left="1.2cm"
                        top="1cm"
                        width="9.5cm"
                    >
                        <!-- goobi logo -->
                        <fo:block-container left="20pt">
                            <fo:block>
                                <fo:external-graphic
                                    src="logo.png"
                                    content-width="22mm" />
                            </fo:block>
                        </fo:block-container>
                        <!-- big header -->
                        <fo:block
                            border-top-width="1pt"
                            border-top-style="solid"
                            border-top-color="#cccccc"
                            margin-top="0pt"
                            margin-bottom="7pt" />
                        <fo:block
                            text-align="center"
                            font-weight="bold"
                            font-size="14pt"
                        >
                            <xsl:value-of select="goobi:title" />
                        </fo:block>
                        <fo:block
                            border-top-width="1pt"
                            border-top-style="solid"
                            border-top-color="#cccccc"
                            margin-top="7pt" />
                        <!-- table with more data -->
                        <fo:table
                            line-height="13pt"
                            font-size="9pt"
                            margin-top="0.5cm"
                            table-layout="fixed"
                        >
                            <fo:table-column column-width="4cm" />
                            <fo:table-column column-width="5.5cm" />
                            <fo:table-body>
                                <!-- title -->
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>Process title:</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="goobi:title" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <!-- goobi identifier -->
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>Goobi identifier:</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="@processID" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <!-- project -->
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
                                <!-- ruleset -->
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>Ruleset:</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="goobi:ruleset" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <!-- creation data -->
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>Process creation date:</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="goobi:creationDate" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <!-- timestamp -->
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>PDF generation date:</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="goobi:pdfGenerationDate" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                    </fo:block-container>
                    <!-- // general process data -->
                    <!-- separator -->
                    <fo:block
                        border-top-width="1pt"
                        border-top-style="solid"
                        border-top-color="#cccccc"
                        margin-top="10.5cm"
                        margin-bottom="0.5cm" />
                    <!-- main docstruct -->
                    <fo:block-container>
                        <xsl:for-each select="goobi:node">
                            <fo:block
                                font-weight="bold"
                                font-size="11pt"
                                margin="0.5cm 0 0"
                            >
                                <xsl:value-of select="@type" />
                            </fo:block>
                            <!-- main docstruct persons -->
                            <xsl:choose>
                                <xsl:when test="child::goobi:person">
                                    <fo:table
                                        line-height="12pt"
                                        font-size="9pt"
                                        margin="0.2cm 0 0 1cm"
                                        background-color="#eeeeee"
                                        table-layout="fixed"
                                    >
                                        <fo:table-column column-width="5cm" />
                                        <fo:table-column column-width="12cm" />
                                        <fo:table-body
                                            start-indent="0"
                                            end-indent="0"
                                        >
                                            <xsl:for-each select="child::goobi:person">
                                                <fo:table-row>
                                                    <fo:table-cell>
                                                        <fo:block margin="2pt">
                                                            <xsl:value-of select="@role" />
                                                            <xsl:text>:</xsl:text>
                                                        </fo:block>
                                                    </fo:table-cell>
                                                    <fo:table-cell>
                                                        <fo:block margin="2pt">
                                                            <xsl:value-of select="@lastname" />
                                                            ,
                                                            <xsl:value-of select="@firstname" />
                                                            <xsl:if test="@id">
                                                                (
                                                                <xsl:value-of select="@uri" />
                                                                <xsl:value-of select="@id" />
                                                                )
                                                            </xsl:if>
                                                        </fo:block>
                                                    </fo:table-cell>
                                                </fo:table-row>
                                            </xsl:for-each>
                                        </fo:table-body>
                                    </fo:table>
                                </xsl:when>
                                <xsl:otherwise>
                                    <fo:block
                                        margin="0.2cm 0 0.2cm 1cm"
                                        font-size="9pt"
                                        color="#bbbbbb"
                                    > - no person available -
                                    </fo:block>
                                </xsl:otherwise>
                            </xsl:choose>
                            <!-- // main docstruct persons -->
                            <!-- main docstruct metadata -->
                            <xsl:choose>
                                <xsl:when test="child::goobi:metadata">
                                    <fo:table
                                        line-height="12pt"
                                        font-size="9pt"
                                        margin="0.2cm 0 0 1cm"
                                        background-color="#eeeeee"
                                        table-layout="fixed"
                                    >
                                        <fo:table-column column-width="5cm" />
                                        <fo:table-column column-width="12cm" />
                                        <fo:table-body
                                            start-indent="0"
                                            end-indent="0"
                                        >
                                            <xsl:for-each select="child::goobi:metadata">
                                                <fo:table-row>
                                                    <fo:table-cell>
                                                        <fo:block margin="2pt">
                                                            <xsl:value-of select="@name" />
                                                            <xsl:text>:</xsl:text>
                                                        </fo:block>
                                                    </fo:table-cell>
                                                    <fo:table-cell>
                                                        <fo:block margin="2pt">
                                                            <xsl:value-of select="." />
                                                        </fo:block>
                                                    </fo:table-cell>
                                                </fo:table-row>
                                            </xsl:for-each>
                                        </fo:table-body>
                                    </fo:table>
                                </xsl:when>
                                <xsl:otherwise>
                                    <fo:block
                                        margin="0.2cm 0 0.2cm 1cm"
                                        font-size="9pt"
                                        color="#bbbbbb"
                                    > - no metadata available -
                                    </fo:block>
                                </xsl:otherwise>
                            </xsl:choose>
                            <!-- // main docstruct metadata -->
                            <!-- 1. level docstructs -->
                            <xsl:for-each select="child::goobi:node">
                                <fo:block
                                    font-weight="bold"
                                    font-size="11pt"
                                    margin="0.5cm 0 0 1cm"
                                >
                                    <xsl:value-of select="@type" />
                                </fo:block>
                                <xsl:choose>
                                    <xsl:when test="child::goobi:person">
                                        <fo:table
                                            line-height="12pt"
                                            font-size="9pt"
                                            margin="0.2cm 0 0 2cm"
                                            background-color="#eeeeee"
                                            table-layout="fixed"
                                        >
                                            <fo:table-column column-width="5cm" />
                                            <fo:table-column column-width="11cm" />
                                            <fo:table-body
                                                start-indent="0"
                                                end-indent="0"
                                            >
                                                <xsl:for-each select="child::goobi:person">
                                                    <fo:table-row>
                                                        <fo:table-cell>
                                                            <fo:block margin="2pt">
                                                                <xsl:value-of select="@role" />
                                                                <xsl:text>:</xsl:text>
                                                            </fo:block>
                                                        </fo:table-cell>
                                                        <fo:table-cell>
                                                            <fo:block margin="2pt">
                                                                <xsl:value-of select="@lastname" />
                                                                ,
                                                                <xsl:value-of select="@firstname" />
                                                                <xsl:if test="@id">
                                                                    (
                                                                    <xsl:value-of select="@uri" />
                                                                    <xsl:value-of select="@id" />
                                                                    )
                                                                </xsl:if>
                                                            </fo:block>
                                                        </fo:table-cell>
                                                    </fo:table-row>
                                                </xsl:for-each>
                                            </fo:table-body>
                                        </fo:table>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <fo:block
                                            margin="0.2cm 0 0.2cm 2cm"
                                            font-size="9pt"
                                            color="#bbbbbb"
                                        > - no person available -
                                        </fo:block>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                    <xsl:when test="child::goobi:metadata">
                                        <fo:table
                                            line-height="12pt"
                                            font-size="9pt"
                                            margin="0.2cm 0 0.2cm 2cm"
                                            background-color="#eeeeee"
                                            table-layout="fixed"
                                        >
                                            <fo:table-column column-width="5cm" />
                                            <fo:table-column column-width="11cm" />
                                            <fo:table-body
                                                start-indent="0"
                                                end-indent="0"
                                            >
                                                <xsl:for-each select="child::goobi:metadata">
                                                    <fo:table-row>
                                                        <fo:table-cell>
                                                            <fo:block margin="2pt">
                                                                <xsl:value-of select="@name" />
                                                                <xsl:text>:</xsl:text>
                                                            </fo:block>
                                                        </fo:table-cell>
                                                        <fo:table-cell>
                                                            <fo:block margin="2pt">
                                                                <xsl:value-of select="." />
                                                            </fo:block>
                                                        </fo:table-cell>
                                                    </fo:table-row>
                                                </xsl:for-each>
                                            </fo:table-body>
                                        </fo:table>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <fo:block
                                            margin="0.2cm 0 0.2cm 2cm"
                                            font-size="9pt"
                                            color="#bbbbbb"
                                        > - no metadata available -
                                        </fo:block>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <!-- 2. level docstructs -->
                                <xsl:for-each select="child::goobi:node">
                                    <fo:block
                                        font-weight="bold"
                                        font-size="11pt"
                                        margin="0.5cm 0 0 2cm"
                                    >
                                        <xsl:value-of select="@type" />
                                    </fo:block>
                                    <xsl:choose>
                                        <xsl:when test="child::goobi:person">
                                            <fo:table
                                                line-height="12pt"
                                                font-size="9pt"
                                                margin="0.2cm 0 0 3cm"
                                                background-color="#eeeeee"
                                                table-layout="fixed"
                                            >
                                                <fo:table-column column-width="5cm" />
                                                <fo:table-column column-width="10cm" />
                                                <fo:table-body
                                                    start-indent="0"
                                                    end-indent="0"
                                                >
                                                    <xsl:for-each select="child::goobi:person">
                                                        <fo:table-row>
                                                            <fo:table-cell>
                                                                <fo:block margin="2pt">
                                                                    <xsl:value-of select="@role" />
                                                                    <xsl:text>:</xsl:text>
                                                                </fo:block>
                                                            </fo:table-cell>
                                                            <fo:table-cell>
                                                                <fo:block margin="2pt">
                                                                    <xsl:value-of select="@lastname" />, <xsl:value-of select="@firstname" />
                                                                    <xsl:if test="@id">
                                                                        (<xsl:value-of select="@uri" /><xsl:value-of select="@id" />)
                                                                    </xsl:if>
                                                                </fo:block>
                                                            </fo:table-cell>
                                                        </fo:table-row>
                                                    </xsl:for-each>
                                                </fo:table-body>
                                            </fo:table>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block
                                                margin="0.2cm 0 0.2cm 3cm"
                                                font-size="9pt"
                                                color="#bbbbbb"
                                            > - no person available -
                                            </fo:block>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:choose>
                                        <xsl:when test="child::goobi:metadata">
                                            <fo:table
                                                line-height="12pt"
                                                font-size="9pt"
                                                margin="0.2cm 0 0.2cm 3cm"
                                                background-color="#eeeeee"
                                                table-layout="fixed"
                                            >
                                                <fo:table-column column-width="5cm" />
                                                <fo:table-column column-width="10cm" />
                                                <fo:table-body
                                                    start-indent="0"
                                                    end-indent="0"
                                                >
                                                    <xsl:for-each select="child::goobi:metadata">
                                                        <fo:table-row>
                                                            <fo:table-cell>
                                                                <fo:block margin="2pt">
                                                                    <xsl:value-of select="@name" />
                                                                    <xsl:text>:</xsl:text>
                                                                </fo:block>
                                                            </fo:table-cell>
                                                            <fo:table-cell>
                                                                <fo:block margin="2pt">
                                                                    <xsl:value-of select="." />
                                                                </fo:block>
                                                            </fo:table-cell>
                                                        </fo:table-row>
                                                    </xsl:for-each>
                                                </fo:table-body>
                                            </fo:table>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block
                                                margin="0.2cm 0 0.2cm 3cm"
                                                font-size="9pt"
                                                color="#bbbbbb"
                                            > - no metadata available -
                                            </fo:block>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <!-- 3. level docstructs -->
                                    <xsl:for-each select="child::goobi:node">
                                        <fo:block
                                            font-weight="bold"
                                            font-size="11pt"
                                            margin="0.5cm 0 0 3cm"
                                        >
                                            <xsl:value-of select="@type" />
                                        </fo:block>
                                        <xsl:choose>
                                            <xsl:when test="child::goobi:person">
                                                <fo:table
                                                    line-height="12pt"
                                                    font-size="9pt"
                                                    margin="0.2cm 0 0 4cm"
                                                    background-color="#eeeeee"
                                                    table-layout="fixed"
                                                >
                                                    <fo:table-column column-width="5cm" />
                                                    <fo:table-column column-width="9cm" />
                                                    <fo:table-body
                                                        start-indent="0"
                                                        end-indent="0"
                                                    >
                                                        <xsl:for-each select="child::goobi:person">
                                                            <fo:table-row>
                                                                <fo:table-cell>
                                                                    <fo:block margin="2pt">
                                                                        <xsl:value-of select="@role" />
                                                                        <xsl:text>:</xsl:text>
                                                                    </fo:block>
                                                                </fo:table-cell>
                                                                <fo:table-cell>
                                                                    <fo:block margin="2pt">
                                                                        <xsl:value-of select="@lastname" />
                                                                        ,
                                                                        <xsl:value-of select="@firstname" />
                                                                        <xsl:if test="@id">
                                                                            (
                                                                            <xsl:value-of select="@uri" />
                                                                            <xsl:value-of select="@id" />
                                                                            )
                                                                        </xsl:if>
                                                                    </fo:block>
                                                                </fo:table-cell>
                                                            </fo:table-row>
                                                        </xsl:for-each>
                                                    </fo:table-body>
                                                </fo:table>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <fo:block
                                                    margin="0.2cm 0 0.2cm 4cm"
                                                    font-size="9pt"
                                                    color="#bbbbbb"
                                                > - no person available -
                                                </fo:block>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:choose>
                                            <xsl:when test="child::goobi:metadata">
                                                <fo:table
                                                    line-height="12pt"
                                                    font-size="9pt"
                                                    margin="0.2cm 0 0.2cm 4cm"
                                                    background-color="#eeeeee"
                                                    table-layout="fixed"
                                                >
                                                    <fo:table-column column-width="5cm" />
                                                    <fo:table-column column-width="9cm" />
                                                    <fo:table-body
                                                        start-indent="0"
                                                        end-indent="0"
                                                    >
                                                        <xsl:for-each select="child::goobi:metadata">
                                                            <fo:table-row>
                                                                <fo:table-cell>
                                                                    <fo:block margin="2pt">
                                                                        <xsl:value-of select="@name" />
                                                                        <xsl:text>:</xsl:text>
                                                                    </fo:block>
                                                                </fo:table-cell>
                                                                <fo:table-cell>
                                                                    <fo:block margin="2pt">
                                                                        <xsl:value-of select="." />
                                                                    </fo:block>
                                                                </fo:table-cell>
                                                            </fo:table-row>
                                                        </xsl:for-each>
                                                    </fo:table-body>
                                                </fo:table>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <fo:block
                                                    margin="0.2cm 0 0.2cm 4cm"
                                                    font-size="9pt"
                                                    color="#bbbbbb"
                                                > - no metadata available -
                                                </fo:block>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <!-- 4. level docstructs -->
                                        <xsl:for-each select="child::goobi:node">
                                            <fo:block
                                                font-weight="bold"
                                                font-size="11pt"
                                                margin="0.5cm 0 0 4cm"
                                            >
                                                <xsl:value-of select="@type" />
                                            </fo:block>
                                            <xsl:choose>
                                                <xsl:when test="child::goobi:person">
                                                    <fo:table
                                                        line-height="12pt"
                                                        font-size="9pt"
                                                        margin="0.2cm 0 0 5cm"
                                                        background-color="#eeeeee"
                                                        table-layout="fixed"
                                                    >
                                                        <fo:table-column column-width="5cm" />
                                                        <fo:table-column column-width="8cm" />
                                                        <fo:table-body
                                                            start-indent="0"
                                                            end-indent="0"
                                                        >
                                                            <xsl:for-each select="child::goobi:person">
                                                                <fo:table-row>
                                                                    <fo:table-cell>
                                                                        <fo:block margin="2pt">
                                                                            <xsl:value-of select="@role" />
                                                                            <xsl:text>:</xsl:text>
                                                                        </fo:block>
                                                                    </fo:table-cell>
                                                                    <fo:table-cell>
                                                                        <fo:block margin="2pt">
                                                                            <xsl:value-of select="@lastname" />
                                                                            ,
                                                                            <xsl:value-of select="@firstname" />
                                                                            <xsl:if test="@id">
                                                                                (
                                                                                <xsl:value-of select="@uri" />
                                                                                <xsl:value-of select="@id" />
                                                                                )
                                                                            </xsl:if>
                                                                        </fo:block>
                                                                    </fo:table-cell>
                                                                </fo:table-row>
                                                            </xsl:for-each>
                                                        </fo:table-body>
                                                    </fo:table>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <fo:block
                                                        margin="0.2cm 0 0.2cm 5cm"
                                                        font-size="9pt"
                                                        color="#bbbbbb"
                                                    > - no person available -
                                                    </fo:block>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <xsl:choose>
                                                <xsl:when test="child::goobi:metadata">
                                                    <fo:table
                                                        line-height="12pt"
                                                        font-size="9pt"
                                                        margin="0.2cm 0 0.2cm 5cm"
                                                        background-color="#eeeeee"
                                                        table-layout="fixed"
                                                    >
                                                        <fo:table-column column-width="5cm" />
                                                        <fo:table-column column-width="8cm" />
                                                        <fo:table-body
                                                            start-indent="0"
                                                            end-indent="0"
                                                        >
                                                            <xsl:for-each select="child::goobi:metadata">
                                                                <fo:table-row>
                                                                    <fo:table-cell>
                                                                        <fo:block margin="2pt">
                                                                            <xsl:value-of select="@name" />
                                                                            <xsl:text>:</xsl:text>
                                                                        </fo:block>
                                                                    </fo:table-cell>
                                                                    <fo:table-cell>
                                                                        <fo:block margin="2pt">
                                                                            <xsl:value-of select="." />
                                                                        </fo:block>
                                                                    </fo:table-cell>
                                                                </fo:table-row>
                                                            </xsl:for-each>
                                                        </fo:table-body>
                                                    </fo:table>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <fo:block
                                                        margin="0.2cm 0 0.2cm 5cm"
                                                        font-size="9pt"
                                                        color="#bbbbbb"
                                                    > - no metadata available -
                                                    </fo:block>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <!-- 5. level docstructs -->
                                            <xsl:for-each select="child::goobi:node">
                                                <fo:block
                                                    font-weight="bold"
                                                    font-size="11pt"
                                                    margin="0.5cm 0 0 5cm"
                                                >
                                                    <xsl:value-of select="@type" />
                                                </fo:block>
                                                <xsl:choose>
                                                    <xsl:when test="child::goobi:person">
                                                        <fo:table
                                                            line-height="12pt"
                                                            font-size="9pt"
                                                            margin="0.2cm 0 0 6cm"
                                                            background-color="#eeeeee"
                                                            table-layout="fixed"
                                                        >
                                                            <fo:table-column column-width="5cm" />
                                                            <fo:table-column column-width="7cm" />
                                                            <fo:table-body
                                                                start-indent="0"
                                                                end-indent="0"
                                                            >
                                                                <xsl:for-each select="child::goobi:person">
                                                                    <fo:table-row>
                                                                        <fo:table-cell>
                                                                            <fo:block margin="2pt">
                                                                                <xsl:value-of select="@role" />
                                                                                <xsl:text>:</xsl:text>
                                                                            </fo:block>
                                                                        </fo:table-cell>
                                                                        <fo:table-cell>
                                                                            <fo:block margin="2pt">
                                                                                <xsl:value-of select="@lastname" />
                                                                                ,
                                                                                <xsl:value-of select="@firstname" />
                                                                                <xsl:if test="@id">
                                                                                    (
                                                                                    <xsl:value-of select="@uri" />
                                                                                    <xsl:value-of select="@id" />
                                                                                    )
                                                                                </xsl:if>
                                                                            </fo:block>
                                                                        </fo:table-cell>
                                                                    </fo:table-row>
                                                                </xsl:for-each>
                                                            </fo:table-body>
                                                        </fo:table>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <fo:block
                                                            margin="0.2cm 0 0.2cm 6cm"
                                                            font-size="9pt"
                                                            color="#bbbbbb"
                                                        > - no person available -
                                                        </fo:block>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:choose>
                                                    <xsl:when test="child::goobi:metadata">
                                                        <fo:table
                                                            line-height="12pt"
                                                            font-size="9pt"
                                                            margin="0.2cm 0 0.2cm 6cm"
                                                            background-color="#eeeeee"
                                                            table-layout="fixed"
                                                        >
                                                            <fo:table-column column-width="5cm" />
                                                            <fo:table-column column-width="7cm" />
                                                            <fo:table-body
                                                                start-indent="0"
                                                                end-indent="0"
                                                            >
                                                                <xsl:for-each select="child::goobi:metadata">
                                                                    <fo:table-row>
                                                                        <fo:table-cell>
                                                                            <fo:block margin="2pt">
                                                                                <xsl:value-of select="@name" />
                                                                                <xsl:text>:</xsl:text>
                                                                            </fo:block>
                                                                        </fo:table-cell>
                                                                        <fo:table-cell>
                                                                            <fo:block margin="2pt">
                                                                                <xsl:value-of select="." />
                                                                            </fo:block>
                                                                        </fo:table-cell>
                                                                    </fo:table-row>
                                                                </xsl:for-each>
                                                            </fo:table-body>
                                                        </fo:table>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <fo:block
                                                            margin="0.2cm 0 0.2cm 6cm"
                                                            font-size="9pt"
                                                            color="#bbbbbb"
                                                        > - no metadata available -
                                                        </fo:block>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                <!-- 6. level docstructs -->
                                                <xsl:for-each select="child::goobi:node">
                                                    <fo:block
                                                        font-weight="bold"
                                                        font-size="11pt"
                                                        margin="0.5cm 0 0 6cm"
                                                    >
                                                        <xsl:value-of select="@type" />
                                                    </fo:block>
                                                    <xsl:choose>
                                                        <xsl:when test="child::goobi:person">
                                                            <fo:table
                                                                line-height="12pt"
                                                                font-size="9pt"
                                                                margin="0.2cm 0 0 7cm"
                                                                background-color="#eeeeee"
                                                                table-layout="fixed"
                                                            >
                                                                <fo:table-column column-width="5cm" />
                                                                <fo:table-column column-width="6cm" />
                                                                <fo:table-body
                                                                    start-indent="0"
                                                                    end-indent="0"
                                                                >
                                                                    <xsl:for-each select="child::goobi:person">
                                                                        <fo:table-row>
                                                                            <fo:table-cell>
                                                                                <fo:block margin="2pt">
                                                                                    <xsl:value-of select="@role" />
                                                                                    <xsl:text>:</xsl:text>
                                                                                </fo:block>
                                                                            </fo:table-cell>
                                                                            <fo:table-cell>
                                                                                <fo:block margin="2pt">
                                                                                    <xsl:value-of select="@lastname" />, <xsl:value-of select="@firstname" />
                                                                                    <xsl:if test="@id">
                                                                                        (<xsl:value-of select="@uri" /><xsl:value-of select="@id" />)
                                                                                    </xsl:if>
                                                                                </fo:block>
                                                                            </fo:table-cell>
                                                                        </fo:table-row>
                                                                    </xsl:for-each>
                                                                </fo:table-body>
                                                            </fo:table>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <fo:block
                                                                margin="0.2cm 0 0.2cm 7cm"
                                                                font-size="9pt"
                                                                color="#bbbbbb"
                                                            > - no person available -
                                                            </fo:block>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                    <xsl:choose>
                                                        <xsl:when test="child::goobi:metadata">
                                                            <fo:table
                                                                line-height="12pt"
                                                                font-size="9pt"
                                                                margin="0.2cm 0 0.2cm 7cm"
                                                                background-color="#eeeeee"
                                                                table-layout="fixed"
                                                            >
                                                                <fo:table-column column-width="5cm" />
                                                                <fo:table-column column-width="6cm" />
                                                                <fo:table-body
                                                                    start-indent="0"
                                                                    end-indent="0"
                                                                >
                                                                    <xsl:for-each select="child::goobi:metadata">
                                                                        <fo:table-row>
                                                                            <fo:table-cell>
                                                                                <fo:block margin="2pt">
                                                                                    <xsl:value-of select="@name" />
                                                                                    <xsl:text>:</xsl:text>
                                                                                </fo:block>
                                                                            </fo:table-cell>
                                                                            <fo:table-cell>
                                                                                <fo:block margin="2pt">
                                                                                    <xsl:value-of select="." />
                                                                                </fo:block>
                                                                            </fo:table-cell>
                                                                        </fo:table-row>
                                                                    </xsl:for-each>
                                                                </fo:table-body>
                                                            </fo:table>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <fo:block
                                                                margin="0.2cm 0 0.2cm 7cm"
                                                                font-size="9pt"
                                                                color="#bbbbbb"
                                                            > - no metadata available -
                                                            </fo:block>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                    <!-- 7. level docstructs -->
                                                    <xsl:for-each select="child::goobi:node">
                                                        <fo:block
                                                            font-weight="bold"
                                                            font-size="11pt"
                                                            margin="0.5cm 0 0 7cm"
                                                        >
                                                            <xsl:value-of select="@type" />
                                                        </fo:block>
                                                        <xsl:choose>
                                                            <xsl:when test="child::goobi:person">
                                                                <fo:table
                                                                    line-height="12pt"
                                                                    font-size="9pt"
                                                                    margin="0.2cm 0 0 8cm"
                                                                    background-color="#eeeeee"
                                                                    table-layout="fixed"
                                                                >
                                                                    <fo:table-column column-width="5cm" />
                                                                    <fo:table-column column-width="5cm" />
                                                                    <fo:table-body
                                                                        start-indent="0"
                                                                        end-indent="0"
                                                                    >
                                                                        <xsl:for-each select="child::goobi:person">
                                                                            <fo:table-row>
                                                                                <fo:table-cell>
                                                                                    <fo:block margin="2pt">
                                                                                        <xsl:value-of select="@role" />
                                                                                        <xsl:text>:</xsl:text>
                                                                                    </fo:block>
                                                                                </fo:table-cell>
                                                                                <fo:table-cell>
                                                                                    <fo:block margin="2pt">
                                                                                        <xsl:value-of select="@lastname" />
                                                                                        ,
                                                                                        <xsl:value-of select="@firstname" />
                                                                                        <xsl:if test="@id">
                                                                                            (
                                                                                            <xsl:value-of select="@uri" />
                                                                                            <xsl:value-of select="@id" />
                                                                                            )
                                                                                        </xsl:if>
                                                                                    </fo:block>
                                                                                </fo:table-cell>
                                                                            </fo:table-row>
                                                                        </xsl:for-each>
                                                                    </fo:table-body>
                                                                </fo:table>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <fo:block
                                                                    margin="0.2cm 0 0.2cm 8cm"
                                                                    font-size="9pt"
                                                                    color="#bbbbbb"
                                                                > - no person available -
                                                                </fo:block>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                        <xsl:choose>
                                                            <xsl:when test="child::goobi:metadata">
                                                                <fo:table
                                                                    line-height="12pt"
                                                                    font-size="9pt"
                                                                    margin="0.2cm 0 0.2cm 8cm"
                                                                    background-color="#eeeeee"
                                                                    table-layout="fixed"
                                                                >
                                                                    <fo:table-column column-width="5cm" />
                                                                    <fo:table-column column-width="5cm" />
                                                                    <fo:table-body
                                                                        start-indent="0"
                                                                        end-indent="0"
                                                                    >
                                                                        <xsl:for-each select="child::goobi:metadata">
                                                                            <fo:table-row>
                                                                                <fo:table-cell>
                                                                                    <fo:block margin="2pt">
                                                                                        <xsl:value-of select="@name" />
                                                                                        <xsl:text>:</xsl:text>
                                                                                    </fo:block>
                                                                                </fo:table-cell>
                                                                                <fo:table-cell>
                                                                                    <fo:block margin="2pt">
                                                                                        <xsl:value-of select="." />
                                                                                    </fo:block>
                                                                                </fo:table-cell>
                                                                            </fo:table-row>
                                                                        </xsl:for-each>
                                                                    </fo:table-body>
                                                                </fo:table>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <fo:block
                                                                    margin="0.2cm 0 0.2cm 8cm"
                                                                    font-size="9pt"
                                                                    color="#bbbbbb"
                                                                > - no metadata available -
                                                                </fo:block>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                    <!-- // 7. level docstructs -->
                                                </xsl:for-each>
                                                <!-- // 6. level docstructs -->
                                            </xsl:for-each>
                                            <!-- // 5. level docstructs -->
                                        </xsl:for-each>
                                        <!-- // 4. level docstructs -->
                                    </xsl:for-each>
                                    <!-- // 3. level docstructs -->
                                </xsl:for-each>
                                <!-- // 2. level docstructs -->
                            </xsl:for-each>
                            <!-- // 1. level docstructs -->
                        </xsl:for-each>
                    </fo:block-container>
                    <!-- // main docstruct -->
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
