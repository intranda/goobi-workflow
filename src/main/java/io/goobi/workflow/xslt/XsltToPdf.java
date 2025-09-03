package io.goobi.workflow.xslt;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.xmlgraphics.util.MimeConstants;
import org.goobi.beans.Process;
import org.xml.sax.SAXException;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;
import net.sf.saxon.lib.FeatureKeys;

/**
 * This class provides generating a docket based on the generated xml log.
 * 
 * @author Steffen Hankiewicz
 */

@Log4j2
public class XsltToPdf {

    /**
     * Generates the docket file (configurable type) for one process and writes the result to the given output stream. For the configuration of the
     * parser, goobi/xslt/config.xml must be present. The provided template (xslt file) is used to build the docket file.
     *
     * @param process The process to export
     * @param os The output stream to write the docket file to
     * @param xsltfile The provided template file
     * @param inexport The xslt preparator instance to start the export process
     * @param type The type of the exported docker file. This could be MimeConstants.MIME_PDF or MimeConstants.MIME_TIFF for example
     * @param dpi The resolution of the output file (in dots per inch)
     * @throws IOException If anything went wrong while writing to the output stream
     */
    public void startExport(Process process, OutputStream os, String xsltfile, IXsltPreparator inexport, String type, int dpi) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inexport.startExport(process, out, null);

        this.writeExportToFile(os, out, xsltfile, type, dpi, false);
    }

    /**
     * Generates the docket file (configurable type) for multiple processes and writes the result to the given output stream. For the configuration of
     * the parser, goobi/xslt/config.xml must be present. The provided template (xslt file) is used to build the docket file.
     *
     * @param processList The list of processes to export
     * @param os The output stream to write the PDF file to
     * @param xsltfile The provided template file
     * @param type The type of the exported docker file. This could be MimeConstants.MIME_PDF or MimeConstants.MIME_TIFF for example
     * @param dpi The resolution of the output file (in dots per inch)
     * @throws IOException If anything went wrong while writing to the output stream
     */
    public void startExport(List<Process> processList, OutputStream os, String xsltfile, String type, int dpi) throws IOException {

        XsltPreparatorDocket inexport = new XsltPreparatorDocket();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inexport.startExport(processList, out, null);

        this.writeExportToFile(os, out, xsltfile, type, dpi, true);
    }

    public void startExport(Process process, OutputStream os, String xsltfile, IXsltPreparator inexport, String type, int dpi, boolean includeImage)
            throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inexport.startExport(process, out, null, includeImage);

        this.writeExportToFile(os, out, xsltfile, type, dpi, false);
    }

    public void startExport(List<Process> processList, OutputStream os, String xsltfile, String type, int dpi, boolean includeImage)
            throws IOException {

        XsltPreparatorDocket inexport = new XsltPreparatorDocket();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inexport.startExport(processList, out, null, includeImage);

        this.writeExportToFile(os, out, xsltfile, type, dpi, true);
    }

    /**
     * Generates the docket PDF file for one process and writes the result to the given output stream. For the configuration of the parser,
     * goobi/xslt/config.xml must be present. The provided template (xslt file) is used to build the PDF file. The default DPI (dots per inch value)
     * is 300.
     *
     * @param process The process to export
     * @param os The output stream to write the PDF file to
     * @param xsltfile The provided template file
     * @param inexport The xslt preparator instance to start the export process
     * @throws IOException If anything went wrong while writing to the output stream
     */
    public void startExport(Process process, OutputStream os, String xsltfile, IXsltPreparator inexport) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inexport.startExport(process, out, null);

        this.writeExportToFile(os, out, xsltfile, MimeConstants.MIME_PDF, 300, false);
    }

    /**
     * Generates the docket PDF file for multiple processes and writes the result to the given output stream. For the configuration of the parser,
     * goobi/xslt/config.xml must be present. The provided template (xslt file) is used to build the PDF file. The default DPI (dots per inch value)
     * is 300.
     *
     * @param processList The list of processes to export
     * @param os The output stream to write the PDF file to
     * @param xsltfile The provided template file
     * @throws IOException If anything went wrong while writing to the output stream
     */
    public void startExport(List<Process> processList, OutputStream os, String xsltfile) throws IOException {

        XsltPreparatorDocket inexport = new XsltPreparatorDocket();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        inexport.startExport(processList, out, null);

        this.writeExportToFile(os, out, xsltfile, MimeConstants.MIME_PDF, 300, true);
    }

    /**
     * Generates the docket file (using the xslt file and the prepared byte output stream) and writes the byte output stream to the output stream. For
     * the configuration of the parser, goobi/xslt/config.xml must be present.
     *
     * @param os The output stream to write the docket output to
     * @param out The prepared byte output stream from the other methods
     * @param xsltfile The xslt template file to generate the docket file with
     * @param type The type of the exported docker file. This could be MimeConstants.MIME_PDF or MimeConstants.MIME_TIFF for example
     * @param dpi The resolution of the output file (in dots per inch)
     * @param isList Must be true if the above method was called with a list of processes, otherwise false
     * @throws IOException If something went wrong while writing to the output streams
     */
    private void writeExportToFile(OutputStream os, ByteArrayOutputStream out, String xsltfile, String type, int dpi, boolean isList)
            throws IOException {

        // generate pdf file
        StreamSource source = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        StreamSource transformSource = new StreamSource(xsltfile);

        FopConfParser parser = this.initializeFopConfParser();
        if (parser == null) {
            return;
        }
        //building the factory with the user options
        FopFactoryBuilder builder = parser.getFopFactoryBuilder();
        FopFactory fopFactory = builder.build();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        // transform xml
        try {
            Transformer xslfoTransformer;
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            factory.setAttribute(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, false);
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            foUserAgent.setTargetResolution(dpi);
            Fop fop;
            if (isList) {
                xslfoTransformer = factory.newTransformer(transformSource);
            } else {
                xslfoTransformer = XsltToPdf.getTransformer(transformSource);
            }
            fop = fopFactory.newFop(type, foUserAgent, outStream);
            Result res = new SAXResult(fop.getDefaultHandler());
            xslfoTransformer.transform(source, res);
        } catch (FOPException e) {
            throw new IOException("FOPException occured", e);
        } catch (TransformerException e) {
            throw new IOException("TransformerException occured", e);
        }

        // write the content to output stream
        byte[] pdfBytes = outStream.toByteArray();
        os.write(pdfBytes);
    }

    /**
     * Initializes and returns the FOP parser. The config.xml file from goobi/xslt is used to setup the parser.
     *
     * @return The configured parser
     */
    private FopConfParser initializeFopConfParser() {
        File xconf = new File(ConfigurationHelper.getInstance().getXsltFolder() + "config.xml");
        try {
            //parsing configuration
            return new FopConfParser(xconf);
        } catch (SAXException | IOException e) {
            log.error(e);
            return null;
        }
    }

    /**
     * internal method to get a transformer object
     * 
     * @param streamSource
     * @return
     */
    private static Transformer getTransformer(StreamSource streamSource) {
        // setup the xslt transformer
        net.sf.saxon.TransformerFactoryImpl impl = new net.sf.saxon.TransformerFactoryImpl();
        try {
            return impl.newTransformer(streamSource);
        } catch (TransformerConfigurationException exception) {
            log.error(exception);
        }
        return null;
    }

}
