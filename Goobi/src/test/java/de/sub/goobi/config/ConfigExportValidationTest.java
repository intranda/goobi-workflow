package de.sub.goobi.config;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.goobi.beans.ExportValidator;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class ConfigExportValidationTest extends AbstractTest {

    @Test
    public void canReadEntriesFromExportValidationConfigFile() {
        List<ExportValidator> validators = ConfigExportValidation.getConfiguredExportValidators();
        assertEquals(2, validators.size());
    }

    @Test
    public void canGetCorrectIDFromExistantLabel() {
        int id = ConfigExportValidation.getExportValidatorIdFromLabel("XML validity");
        assertEquals(1, id);
    }
}