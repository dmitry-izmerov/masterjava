<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="yes"/>
    <xsl:output method="html" doctype-system="about:legacy-compat" />

    <xsl:param name="projectName"/>

    <xsl:template match="/">
        <html lang="en">
        <head>
            <meta charset="UTF-8"/>
            <title>Title</title>
        </head>
        <body>
            <table>
                <thead>
                    <tr>
                        <th>Group name</th>
                        <th>Group type</th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:apply-templates/>
                </tbody>
            </table>
        </body>
        </html>
    </xsl:template>

    <xsl:template match="/*[name()='Payload']/*[name()='Projects']/*[name()='Project']">
        <xsl:if test="@name=$projectName">
            <xsl:apply-templates/>
        </xsl:if>
    </xsl:template>
    <xsl:template match="/*[name()='Payload']/*[name()='Projects']/*[name()='Project']/*[name()='Group']">
        <tr>
            <td><xsl:value-of select="@name"/></td>
            <td><xsl:value-of select="@type"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="text()"/>
</xsl:stylesheet>