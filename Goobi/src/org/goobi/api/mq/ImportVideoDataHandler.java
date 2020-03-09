package org.goobi.api.mq;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginReturnValue;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

import de.sub.goobi.helper.CloseStepHelper;
import de.sub.goobi.helper.S3FileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * This class is used to import video data from s3 upload storage into a given process.
 * The upload is considered as complete if the process contains either a jpg + mpg or a jpg + mp4 + mxf file.
 * If the upload was completed, the current open step gets closed.
 * 
 */

@Log4j2
public class ImportVideoDataHandler implements TicketHandler<PluginReturnValue> {

    @Override
    public PluginReturnValue call(TaskTicket ticket) {
        String bucket = ticket.getProperties().get("bucket");

        String s3Key = ticket.getProperties().get("s3Key");

        Path destinationFolder = Paths.get(ticket.getProperties().get("destination"));

        Path tempDir = Paths.get(ticket.getProperties().get("targetDir"));
        log.debug("download {} to {}", s3Key, destinationFolder);

        AmazonS3 s3 = S3FileUtils.createS3Client();

        int index = s3Key.lastIndexOf('/');
        Path tempFile;
        Path destinationFile;
        if (index != -1) {
            tempFile = tempDir.resolve(s3Key.substring(index + 1));
            destinationFile = destinationFolder.resolve(s3Key.substring(index + 1));
        } else {
            tempFile = tempDir.resolve(s3Key);
            destinationFile = destinationFolder.resolve(s3Key);
        }

        try (S3Object obj = s3.getObject(bucket, s3Key); InputStream in = obj.getObjectContent()) {
            Files.copy(in, tempFile);
        } catch (IOException e) {
            log.error(e);
            return PluginReturnValue.ERROR;
        }

        log.info("saved file to temporary folder {}", tempFile.toString());

        try {
            StorageProvider.getInstance().copyFile(tempFile, destinationFile);
        } catch (IOException e) {
            log.error(e);
            return PluginReturnValue.ERROR;
        }

        // check if the upload is complete
        List<String> filenamesInFolder = StorageProvider.getInstance().list(destinationFolder.toString());
        boolean posterFound = false;
        boolean mpegFound = false;
        boolean mp4Found = false;
        boolean mxfFound = false;

        for (String filename : filenamesInFolder) {
            String suffix = filename.substring(filename.indexOf("."));
            switch (suffix) {

                case "jpg":
                case "JPG":
                case "jpeg":
                case "JPEG":
                    posterFound = true;
                    break;
                case "mpg":
                case "MPG":
                case "mpeg":
                case "MPEG":
                    mpegFound = true;
                    break;
                case "mp4":
                case "MP4":
                    mp4Found = true;
                    break;
                case "mxf":
                case "MXF":
                    mxfFound = true;
                    break;
            }
        }

        // upload is complete, if poster + mpg or poster + mp4 + mxf are available
        if ((posterFound && mpegFound) || (posterFound && mp4Found && mxfFound)) {
            // close current task
            Process process = ProcessManager.getProcessById(ticket.getProcessId());
            Step stepToClose = null;

            for (Step processStep : process.getSchritte()) {
                if (processStep.getBearbeitungsstatusEnum() == StepStatus.OPEN || processStep.getBearbeitungsstatusEnum() == StepStatus.INWORK) {
                    stepToClose = processStep;
                    break;
                }
            }
            if (stepToClose != null) {
                CloseStepHelper.closeStep(stepToClose, null);
            }
        }

        String deleteFiles = ticket.getProperties().get("deleteFiles");
        if (StringUtils.isNotBlank(deleteFiles) && deleteFiles.equalsIgnoreCase("true")) {
            s3.deleteObject(bucket, s3Key);
            log.info("deleted file from bucket");
        }

        // delete temporary files
        FileUtils.deleteQuietly(tempFile.toFile());
        FileUtils.deleteQuietly(tempDir.toFile());

        return PluginReturnValue.FINISH;
    }

    @Override
    public String getTicketHandlerName() {
        return "importVideoData";
    }

}
