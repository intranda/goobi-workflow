package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.goobi.api.display.helper.MetadataGeneration;
import org.goobi.api.display.helper.MetadataGeneration.MetadataGenerationParameter;
import org.goobi.beans.Process;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.fileformats.mets.ModsHelper;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({  Helper.class })
//@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })

public class MetadataGenerationTest extends AbstractTest {

    private Prefs prefs;
    private Process process;

    @Before
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();
    }

    @Test
    public void testMetadataConversion() throws Exception {
        Fileformat ff = process.readMetadataFile();
        DocStruct ds = ff.getDigitalDocument().getLogicalDocStruct();
        Document fixture = ModsHelper.generateModsSection(ds, prefs);
        assertNotNull(fixture);
        assertEquals("mods", fixture.getRootElement().getName());
        assertEquals("extension", fixture.getRootElement().getChildren().get(0).getName());
        assertEquals("goobi", fixture.getRootElement().getChildren().get(0).getChildren().get(0).getName());
        List<Element> metadataList = fixture.getRootElement().getChildren().get(0).getChildren().get(0).getChildren();
        assertEquals(4, metadataList.size());
    }

    @Test
    public void testConstructor() {
        MetadataGeneration mg = new MetadataGeneration();
        assertNotNull(mg);
    }

    @Test
    public void testMetadataName() {
        MetadataGeneration mg = new MetadataGeneration();
        mg.setMetadataName("fixture");
        assertEquals("fixture", mg.getMetadataName());
    }

    @Test
    public void testCondition() {
        MetadataGeneration mg = new MetadataGeneration();
        mg.setCondition("fixture");
        assertEquals("fixture", mg.getCondition());
    }

    @Test
    public void testDefaultValue() {
        MetadataGeneration mg = new MetadataGeneration();
        mg.setDefaultValue("fixture");
        assertEquals("fixture", mg.getDefaultValue());
    }

    @Test
    public void testParameter() {
        MetadataGeneration mg = new MetadataGeneration();
        MetadataGenerationParameter param = mg.new MetadataGenerationParameter();

        assertNotNull(param);
        param.setParameterName("fixture");
        assertEquals("fixture", param.getParameterName());

        param.setType("fixture");
        assertEquals("fixture", param.getType());

        param.setField("fixture");
        assertEquals("fixture", param.getField());

        param.setRegularExpression("fixture");
        assertEquals("fixture", param.getRegularExpression());

        param.setReplacement("fixture");
        assertEquals("fixture", param.getReplacement());
    }

    @Test
    public void testGenerateValue() throws Exception {
        // preparation

        XPathFactory xpfac = XPathFactory.instance();

        Fileformat ff = process.readMetadataFile();
        DigitalDocument dd = ff.getDigitalDocument();
        DocStruct ds = dd.getLogicalDocStruct();

        Document doc = ModsHelper.generateModsSection(ds, prefs);
        Element mods = doc.getRootElement();
        Element metadataSection = mods.getChild("extension", ModsHelper.MODS_NAMESPACE).getChild("goobi", ModsHelper.GOOBI_NAMESPACE);

        MetadataGeneration mg = new MetadataGeneration();
        mg.setMetadataName("TitleDocMain");
        mg.setCondition("goobi:metadata[@name='TitleDocMain'][text()='main title']");
        mg.setDefaultValue("abcdef [FIRST VALUE] gehij [SECOND VALUE] klmn");

        // successful replacement
        MetadataGenerationParameter param1 = mg.new MetadataGenerationParameter();
        param1.setParameterName("FIRST VALUE");
        param1.setType("xpath");
        param1.setField("goobi:metadata[@name='TitleDocMain']");
        mg.addParameter(param1);

        MetadataGenerationParameter param2 = mg.new MetadataGenerationParameter();
        param2.setParameterName("SECOND VALUE");
        param2.setType("variable");
        param2.setField("{meta.TitleDocMain}");
        param2.setRegularExpression("(\\w+) .*");
        param2.setReplacement("$1");
        mg.addParameter(param2);

        assertEquals("abcdef main title gehij main klmn", mg.generateValue(process, prefs, dd, xpfac, metadataSection));

        // no matching value found
        param1.setField("goobi:metadata[@name='nonExisting']");
        param2.setField("{meta.nonExisting}");
        assertEquals("abcdef [FIRST VALUE] gehij [SECOND VALUE] klmn", mg.generateValue(process, prefs, dd, xpfac, metadataSection));

        // second parameter is ignored, it is not used in the template text
        param1.setField("goobi:metadata[@name='TitleDocMain']");
        param2.setField("{meta.TitleDocMain}");
        mg.setDefaultValue("abcdef [FIRST VALUE] gehij");
        assertEquals("abcdef main title gehij", mg.generateValue(process, prefs, dd, xpfac, metadataSection));

    }

}
