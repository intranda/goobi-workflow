package org.goobi.api.mq;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
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

    public static String HANDLERNAME = "automatic_thumbnail";

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
            generateThumbnailsFromDirectory(process, defaultImageDirectory, sizes, command);
        }
        if (media) {
            defaultImageDirectory = process.getImagesTifDirectory(false);
            generateThumbnailsFromDirectory(process, defaultImageDirectory, sizes, command);
        }
        if (!imgDirectory.isEmpty()) {
            generateThumbnailsFromDirectory(process, imgDirectory, sizes, command);
        }
        if (!command.isEmpty()) {
            new HelperSchritte().executeScriptForStepObject(step, command, false);
        }
    }

    private void generateThumbnailsFromDirectory(Process process, String imageDirectory, int[] sizes, String command)
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
                            new FileOutputStream(thumbnailDirectory + FileSystems.getDefault().getSeparator() + basename + "_" + size + ".jpg"); //NOSONAR, parameter are checked in calling method
                    ImageRequest request = new ImageRequest(new URI(img.getAbsolutePath().replace(" ", "%20")), RegionRequest.FULL, scale,
                            Rotation.NONE, Colortype.DEFAULT, ImageFileFormat.JPG, Map.of("ignoreWatermark", "true")); //remove spaces from url
                    new GetImageAction().writeImage(request, out);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }

    }

    private void generateThumbnailsWithSettings(Step step, Process process) throws IOException, InterruptedException, SwapException, DAOException {
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
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public PluginReturnValue call(TaskTicket ticket) {
        Step step = StepManager.getStepById(ticket.getStepId());
        Process process = ProcessManager.getProcessById(ticket.getProcessId());
        try {
            this.generateThumbnailsWithSettings(step, process);
        } catch (Exception e) {
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
            Helper.addMessageToProcessLog(step.getProcessId(), LogType.DEBUG, "Step '" + step.getTitel() + "' opened.");
        }

        return PluginReturnValue.FINISH;
    }
}
