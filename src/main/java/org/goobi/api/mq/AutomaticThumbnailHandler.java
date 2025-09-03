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
package org.goobi.api.mq;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.goobi.api.mail.SendMail;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginReturnValue;
import org.json.JSONArray;
import org.json.JSONObject;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageFileFormat;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageType.Colortype;
import de.unigoettingen.sub.commons.contentlib.imagelib.transform.RegionRequest;
import de.unigoettingen.sub.commons.contentlib.imagelib.transform.Rotation;
import de.unigoettingen.sub.commons.contentlib.imagelib.transform.Scale;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetImageAction;
import de.unigoettingen.sub.commons.contentlib.servlet.model.ImageRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AutomaticThumbnailHandler implements TicketHandler<PluginReturnValue> {

    public static final String HANDLERNAME = "automatic_thumbnail";

    @Override
    public String getTicketHandlerName() {
        return HANDLERNAME;
    }

    //returns true if changed and false if not
    private boolean checkIfChanged(File outputDirectory, File[] fileList) {
        File thumbnailFile = outputDirectory.listFiles()[0];
        for (File img : fileList) {
            if (img.lastModified() > thumbnailFile.lastModified()) {
                return true;
            }
        }
        return false;
    }

    private void generateThumbnails(Process process, boolean master, boolean media, String imgDirectory, String command, int[] sizes, Step step)
            throws IOException, SwapException, DAOException, ContentLibException {
        String defaultImageDirectory;
        if (master) {
            defaultImageDirectory = process.getImagesOrigDirectory(false);
            generateThumbnailsFromDirectory(process, defaultImageDirectory, sizes);
        }
        if (media) {
            defaultImageDirectory = process.getImagesTifDirectory(false);
            generateThumbnailsFromDirectory(process, defaultImageDirectory, sizes);
        }
        if (!imgDirectory.isEmpty()) {
            generateThumbnailsFromDirectory(process, imgDirectory, sizes);
        }
        if (!command.isEmpty()) {
            new HelperSchritte().executeScriptForStepObject(step, command, false);
        }
    }

    private void generateThumbnailsFromDirectory(Process process, String imageDirectory, int[] sizes)
            throws SwapException, DAOException, ContentLibException {
        try {
            File[] fileList = new File(imageDirectory).listFiles();
            for (int size : sizes) {
                String thumbnailDirectory = process.getThumbsDirectory() + Paths.get(imageDirectory).getFileName().toString() + "_" + size;
                File outputDirectory = new File(thumbnailDirectory);
                if (outputDirectory.exists()) {
                    if (!checkIfChanged(outputDirectory, fileList)) {
                        return;
                    }
                } else {
                    outputDirectory.mkdirs();
                }
                Scale scale = new Scale.ScaleToBox(new Dimension(size, size));
                for (File img : fileList) {
                    if (img.isDirectory()) {
                        continue;
                    }
                    String basename = FilenameUtils.getBaseName(img.toString());

                    OutputStream out =
                            new FileOutputStream(thumbnailDirectory + FileSystems.getDefault().getSeparator() + basename + ".jpg"); //NOSONAR
                    //, parameter are checked in calling method
                    ImageRequest request = new ImageRequest(new URI(img.getAbsolutePath().replace(" ", "%20")), RegionRequest.FULL, scale,
                            Rotation.NONE, Colortype.DEFAULT, ImageFileFormat.JPG, Map.of("ignoreWatermark", "true")); //remove spaces from url
                    new GetImageAction().writeImage(request, out);
                }
            }
        } catch (IOException | URISyntaxException e) {
            log.error(e);
        }

    }

    private void generateThumbnailsWithSettings(Step step, Process process) throws IOException, SwapException, DAOException {
        JSONObject settings = step.getAutoThumbnailSettingsJSON();
        boolean master = false;
        boolean media = false;
        String imgDirectory = "";
        String scriptCommand = "";
        int[] sizes = {};

        if (settings.has("Master")) {
            master = settings.getBoolean("Master");
        }
        if (settings.has("Media")) {
            media = settings.getBoolean("Media");
        }
        if (settings.has("Img_directory")) {
            imgDirectory = settings.getString("Img_directory");
        }
        if (settings.has("Custom_script_command")) {
            scriptCommand = settings.getString("Custom_script_command");
        }
        if (settings.has("Sizes")) {
            JSONArray arr = settings.getJSONArray("Sizes");
            sizes = new int[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                sizes[i] = arr.optInt(i);
            }
        }

        try {
            this.generateThumbnails(process, master, media, imgDirectory, scriptCommand, sizes, step);
        } catch (IOException | ContentLibException e) {
            log.error(e);
        }
    }

    @Override
    public PluginReturnValue call(TaskTicket ticket) {
        Step step = StepManager.getStepById(ticket.getStepId());
        Process process = ProcessManager.getProcessById(ticket.getProcessId());
        try {
            this.generateThumbnailsWithSettings(step, process);
        } catch (IOException | SwapException | DAOException e) {
            return PluginReturnValue.ERROR;
        }
        if (step.isTypAutomatisch()) {
            // close automatic task
            new HelperSchritte().CloseStepObjectAutomatic(step);
        } else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
            // open manual task
            step.setBearbeitungsstatusEnum(StepStatus.OPEN);
            SendMail.getInstance().sendMailToAssignedUser(step, StepStatus.OPEN);
            HistoryManager.addHistory(new Date(), step.getReihenfolge().doubleValue(), step.getTitel(), HistoryEventType.stepOpen.getValue(),
                    (step.getProcessId()));
            try {
                StepManager.saveStep(step);
            } catch (DAOException e) {
                log.error(e);
            }
            Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, "Step '" + step.getTitel() + "' opened.");
        }

        return PluginReturnValue.FINISH;
    }
}
