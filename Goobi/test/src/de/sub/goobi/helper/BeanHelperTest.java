package de.sub.goobi.helper;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Docket;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.junit.Before;
import org.junit.Test;

public class BeanHelperTest {

    private Process process;
    private Template template;
    private Masterpiece master;

    @Before
    public void setUp() {
        process = new Process();
        process.setTitel("process");
        process.setDocket(new Docket());
        process.setDocketId(0);
        process.setMetadatenKonfigurationID(0);
        process.setIstTemplate(true);

        Project project = new Project();
        project.setTitel("Project");
        process.setProjekt(project);

        process.setEigenschaften(new ArrayList<Processproperty>());

        template = new Template();
        template.setEigenschaften(new ArrayList<Templateproperty>());
        template.setProzess(process);
        List<Template> tl = new ArrayList<>();
        tl.add(template);
        process.setVorlagen(tl);

        master = new Masterpiece();
        master.setEigenschaften(new ArrayList<Masterpieceproperty>());
        master.setProzess(process);
        List<Masterpiece> ml = new ArrayList<>();
        ml.add(master);
        process.setWerkstuecke(ml);
    }

    @Test
    public void testConstructor() {
        BeanHelper helper = new BeanHelper();
        assertNotNull(helper);
    }

    @Test
    public void testAddProcessProperty() {
        BeanHelper helper = new BeanHelper();
        helper.EigenschaftHinzufuegen(process, "title", "value");
        assertEquals(1, process.getEigenschaften().size());
    }

    @Test
    public void testAddTemplateProperty() {
        BeanHelper helper = new BeanHelper();
        helper.EigenschaftHinzufuegen(template, "title", "value");
        assertEquals(1, template.getEigenschaften().size());
    }

    @Test
    public void testAddMasterProperty() {
        BeanHelper helper = new BeanHelper();
        helper.EigenschaftHinzufuegen(master, "title", "value");
        assertEquals(1, master.getEigenschaften().size());
    }

    @Test
    public void testFindTemplateProperty() {
        BeanHelper helper = new BeanHelper();
        helper.EigenschaftHinzufuegen(template, "title", "value");
        String fixture = helper.ScanvorlagenEigenschaftErmitteln(process, "title");
        assertEquals("value", fixture);
    }

    @Test
    public void testFindMasterpieceProperty() {
        BeanHelper helper = new BeanHelper();
        helper.EigenschaftHinzufuegen(master, "title", "value");
        String fixture = helper.WerkstueckEigenschaftErmitteln(process, "title");
        assertEquals("value", fixture);
    }
}
