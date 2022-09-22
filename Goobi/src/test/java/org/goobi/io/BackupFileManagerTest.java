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
package org.goobi.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BackupFileManagerTest {
    //
    //    public final String BACKUP_PATH_NAME = "./test/";
    //    public final String BACKUP_FILE_NAME = "File-BackupFileManagerTest.xml";
    //    public final String TEST_FILE_CONTENT = "Hello\nWorld!";
    //    public int maximumNumberOfBackupFiles = 1;
    //    public List<String> createdFiles = new ArrayList<>();
    //
    //    @BeforeClass
    //    public static void oneTimeSetUp() {
    //    }
    //
    //    @Before
    //    public void setUp() throws IOException {
    //        String fileName = this.getTestFileName();
    //        this.createFile(fileName);
    //        this.writeFile(fileName, this.TEST_FILE_CONTENT);
    //    }
    //
    //    @After
    //    public void tearDown() throws IOException {
    //        this.deleteFile(this.getTestFileName());
    //        for (int index = 0; index < this.createdFiles.size(); index++) {
    //            if (this.createdFiles.get(index) != null) {
    //                this.deleteFileIfExists(this.createdFiles.get(index));
    //            }
    //        }
    //    }
    //
    //    //@Test
    //    public void shouldCreateOneBackupFile() throws IOException {
    //        this.maximumNumberOfBackupFiles = 1;
    //        String fileName = this.runBackup();
    //
    //        this.assertFileExists(fileName);
    //    }
    //
    //    //@Test
    //    public void firstBackupFileShouldContainSameContentAsOriginalFile() throws IOException {
    //        this.maximumNumberOfBackupFiles = 1;
    //        String fileName = this.runBackup();
    //
    //        this.assertFileHasContent(fileName, this.TEST_FILE_CONTENT);
    //    }
    //
    //    //@Test
    //    public void modifiedDateShouldNotChangedOnBackup() throws IOException {
    //        this.maximumNumberOfBackupFiles = 1;
    //        long firstModifiedDate = this.getLastModifiedFileDate(this.getTestFileName());
    //        this.runBackup();
    //
    //        this.assertLastModifiedDate(this.getTestFileName(), firstModifiedDate);
    //    }
    //
    //    //@Test
    //    public void shouldCreateTwoBackupFiles() throws IOException {
    //        this.maximumNumberOfBackupFiles = 2;
    //
    //        String firstFileName = this.runBackup();
    //        String secondFileName = this.runBackup();
    //
    //        this.assertFileExists(firstFileName);
    //        this.assertFileExists(secondFileName);
    //    }
    //
    //    //@Test
    //    public void secondBackupFileShouldContainSameContentAsOriginalFile() throws IOException {
    //        this.maximumNumberOfBackupFiles = 2;
    //
    //        this.runBackup();
    //        String secondFileName = this.runBackup();
    //
    //        this.assertFileHasContent(secondFileName, this.TEST_FILE_CONTENT);
    //    }
    //
    //    //@Test
    //    public void backupFileModifiedDateLaterThanOriginalFileModifiedDate() throws IOException {
    //        this.maximumNumberOfBackupFiles = 1;
    //
    //        String fileName = this.runBackup();
    //
    //        long originalFileLastModifiedTimestamp = this.getLastModifiedFileDate(this.getTestFileName());
    //        long backupFileLastModifiedTimestamp = this.getLastModifiedFileDate(fileName);
    //        boolean older = originalFileLastModifiedTimestamp <= backupFileLastModifiedTimestamp;
    //
    //        Assert.assertTrue("The backup file \"" + fileName + "\" is older than the original file. \"" + this.getTestFileName() + "\"", older);
    //    }
    //
    //    //@Test
    //    public void shouldCreateThreeBackupFiles() throws IOException {
    //        this.maximumNumberOfBackupFiles = 3;
    //
    //        String firstFileName = this.runBackup();
    //        String secondFileName = this.runBackup();
    //        String thirdFileName = this.runBackup();
    //
    //        this.assertFileExists(firstFileName);
    //        this.assertFileExists(secondFileName);
    //        this.assertFileExists(thirdFileName);
    //    }
    //
    //    //@Test
    //    public void thirdBackupFileShouldContainSameContentAsOriginalFile() throws IOException {
    //        this.maximumNumberOfBackupFiles = 3;
    //
    //        this.runBackup();
    //        this.runBackup();
    //        String thirdFileName = this.runBackup();
    //
    //        this.assertFileHasContent(thirdFileName, this.TEST_FILE_CONTENT);
    //    }
    //
    //    //@Test
    //    public void noBackupIsPerformedWithNumberOfBackupsSetToZero() throws IOException {
    //        this.maximumNumberOfBackupFiles = 0;
    //
    //        String fileName = this.runBackup();
    //
    //        Assert.assertNull("The backup file \"" + fileName + "\" was created, but the the backup limit was set to 0.", fileName);
    //    }
    //
    //    //@Test
    //    public void oldestBackupGetsDeletedWithNumberOfBackupsSetToOne() throws IOException {
    //        this.maximumNumberOfBackupFiles = 1;
    //
    //        String firstFileName = this.runBackup();
    //        String secondFileName = this.runBackup();
    //
    //        this.assertFileNotExists(firstFileName);
    //        this.assertFileExists(secondFileName);
    //    }
    //
    //    //@Test
    //    public void oldestBackupGetsDeletedWithNumberOfBackupsSetToTwo() throws IOException {
    //        this.maximumNumberOfBackupFiles = 2;
    //
    //        String firstFileName = this.runBackup();
    //        String secondFileName = this.runBackup();
    //        String thirdFileName = this.runBackup();
    //
    //        this.assertFileNotExists(firstFileName);
    //        this.assertFileExists(secondFileName);
    //        this.assertFileExists(thirdFileName);
    //    }
    //
    //    private void assertLastModifiedDate(String fileName, long originalLastModifiedDate) {
    //        long currentLastModifiedDate = this.getLastModifiedFileDate(fileName);
    //        Assert.assertEquals("Last modified date of file " + fileName + " differ:", originalLastModifiedDate, currentLastModifiedDate);
    //    }
    //
    //    private String getTestFileName() {
    //        return this.BACKUP_PATH_NAME + this.BACKUP_FILE_NAME;
    //    }
    //
    //    private long getLastModifiedFileDate(String fileName) {
    //        File testFile = new File(this.BACKUP_PATH_NAME + fileName);
    //        return testFile.lastModified();
    //    }
    //
    //    /**
    //     * Creates a single backup file with the backup file manager. The returned file name is the name of the created backup file since it cannot be
    //     * reproduced because it contains the time stamp of the creation time.
    //     *
    //     * @return The file name of the created backup file or null in case of an error
    //     */
    //    private String runBackup() {
    //        // Wait one millisecond to avoid two files with the same name if the processor is too fast...
    //        try {
    //            Thread.sleep(1);
    //        } catch (InterruptedException interruptedException) {
    //            interruptedException.printStackTrace();
    //        }
    //        String fileName = null;
    //        try {
    //            fileName = BackupFileManager.createBackup(this.BACKUP_PATH_NAME, this.BACKUP_PATH_NAME, this.BACKUP_FILE_NAME, this.maximumNumberOfBackupFiles, false);
    //        } catch (IOException ioException) {
    //            Assert.fail(ioException.getMessage());
    //        }
    //        this.createdFiles.add(fileName);
    //        return fileName;
    //    }
    //
    //    private void assertFileHasContent(String fileName, String expectedContent) throws IOException {
    //        File testFile = new File(this.BACKUP_PATH_NAME + fileName);
    //        FileReader reader = new FileReader(testFile);
    //        BufferedReader br = new BufferedReader(reader);
    //        StringBuffer string = new StringBuffer();
    //        while (true) {
    //            String line = br.readLine();
    //            if (line != null) {
    //                if (string.length() > 0) {
    //                    string.append("\n");
    //                }
    //                string.append(line);
    //            } else {
    //                break;
    //            }
    //        }
    //        String content = string.toString();
    //        br.close();
    //        reader.close();
    //        Assert.assertEquals("File " + fileName + " does not contain expected content:", expectedContent, content);
    //    }
    //
    //    private void assertFileExists(String fileName) throws IOException {
    //        File newFile = new File(this.BACKUP_PATH_NAME + fileName);
    //        if (!newFile.exists()) {
    //            Assert.fail("File " + fileName + " does not exist.");
    //        }
    //    }
    //
    //    private void assertFileNotExists(String fileName) throws IOException {
    //        File newFile = new File(this.BACKUP_PATH_NAME + fileName);
    //        if (newFile.exists()) {
    //            Assert.fail("File " + fileName + " should not exist.");
    //        }
    //    }
    //
    //    private void createFile(String fileName) throws IOException {
    //        File testFile = new File(fileName);
    //        FileWriter writer = new FileWriter(testFile);
    //        writer.close();
    //    }
    //
    //    private void deleteFile(String fileName) throws IOException {
    //        File testFile = new File(fileName);
    //        testFile.delete();
    //    }
    //
    //    private void deleteFileIfExists(String fileName) throws IOException {
    //        File testFile = new File(fileName);
    //        if (testFile.exists()) {
    //            testFile.delete();
    //        }
    //    }
    //
    //    private void writeFile(String fileName, String content) throws IOException {
    //        File testFile = new File(fileName);
    //        FileWriter writer = new FileWriter(testFile);
    //        writer.write(content);
    //        writer.close();
    //    }
    //
}
