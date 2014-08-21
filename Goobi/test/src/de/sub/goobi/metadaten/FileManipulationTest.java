package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.mock.MockUploadedFile;

public class FileManipulationTest {

	private Metadaten metadataBean;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		metadataBean = new Metadaten();
		Process testProcess = MockProcess.createProcess(folder);
		metadataBean.setMyProzess(testProcess);
	}

	@Test
	public void testFileManipulation() {
		FileManipulation fixture = new FileManipulation(metadataBean);
		assertNotNull(fixture);
	}

	@Test
	public void testUploadFile() throws FileNotFoundException {
		FileManipulation fixture = new FileManipulation(metadataBean);
		// uploadedFile is null
		fixture.uploadFile();

		InputStream stream = new FileInputStream(
				"/opt/digiverso/junit/data/metadata.xml");
		UploadedFile uploadedFile = new MockUploadedFile(stream, "fixture.tif");

		fixture.setUploadedFile(uploadedFile);
		fixture.uploadFile();

	}

	// @Test
	// public void testGetUploadedFileName() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetUploadedFileName() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetUploadedFile() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetUploadedFile() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetInsertPage() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetInsertPage() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetInsertMode() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetInsertMode() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetImageSelection() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetImageSelection() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testDownloadFile() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testExportFiles() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetSelectedFiles() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetSelectedFiles() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testIsDeleteFilesAfterMove() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetDeleteFilesAfterMove() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testIsMoveFilesInAllFolder() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetMoveFilesInAllFolder() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetAllImportFolder() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetAllImportFolder() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testImportFiles() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetCurrentFolder() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetCurrentFolder() {
	// fail("Not yet implemented");
	// }

}
