package de.sub.goobi.forms;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({AdditionalFieldTest.class, AdministrationFormTest.class, MassImportFormTest.class, HelperFormTest.class, LongRunningTasksForm.class})
public class TestAll {

}
