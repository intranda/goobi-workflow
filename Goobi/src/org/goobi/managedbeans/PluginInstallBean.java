package org.goobi.managedbeans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.goobi.production.plugin.PluginInstallConflict;
import org.goobi.production.plugin.PluginInstaller;
import org.jdom2.JDOMException;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@ManagedBean
@SessionScoped
@Log4j2
public class PluginInstallBean {

    @Getter
    @Setter
    private Part uploadedPluginFile;
    @Getter
    @Setter
    private PluginInstaller pluginInstaller;

    private Path currentExtractedPluginPath;

    private InputStream streamToStoreArchiveFile;

    public String parseUploadedPlugin() {
        if (currentExtractedPluginPath != null && Files.exists(currentExtractedPluginPath)) {
            FileUtils.deleteQuietly(currentExtractedPluginPath.toFile());
        }
        InputStream input = null;
        TarInputStream tarIn = null;
        try {
            input = uploadedPluginFile.getInputStream();
            this.streamToStoreArchiveFile = uploadedPluginFile.getInputStream();
            tarIn = new TarInputStream(input);
            currentExtractedPluginPath = Files.createTempDirectory("plugin_extracted_");
            TarEntry tarEntry = tarIn.getNextEntry();
            while (tarEntry != null) {
                if (!tarEntry.isDirectory()) {
                    Path dest = currentExtractedPluginPath.resolve(tarEntry.getName());
                    if (!Files.isDirectory(dest.getParent())) {
                        Files.createDirectories(dest.getParent());
                    }
                    Files.copy(tarIn, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                tarEntry = tarIn.getNextEntry();
            }
        } catch (IOException ioException) {
            log.error(ioException);
        } finally {
            try {
                input.close();
            } catch (IOException ioException) {
                log.error(ioException);
            }
            try {
                tarIn.close();
            } catch (IOException ioException) {
                log.error(ioException);
            }
        }
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        try {
            this.pluginInstaller = PluginInstaller.createFromExtractedArchive(currentExtractedPluginPath);
        } catch (JDOMException | IOException exception) {
            log.error(exception);
        }
        return "";
    }

    public String install() {
        try {
            File uploadedArchive = new File(this.uploadedPluginFile.getSubmittedFileName());
            FileUtils.copyInputStreamToFile(this.streamToStoreArchiveFile, uploadedArchive);
            this.pluginInstaller.setUploadedArchiveFile(uploadedArchive.toPath());
        } catch (IOException ioException) {
            log.error(ioException);
            ioException.printStackTrace();
        }
        this.pluginInstaller.install();
        this.pluginInstaller = null;
        return "";
    }

    public String cancelInstall() {
        FileUtils.deleteQuietly(this.currentExtractedPluginPath.toFile());
        try {
            uploadedPluginFile.delete();
        } catch (IOException e) {
            log.error(e);
        }
        this.pluginInstaller = null;
        return "";
    }

    public boolean getAreAllConflictsFixed() {
        Object[] conflicts = this.pluginInstaller.getCheck().getConflicts().values().toArray();
        for (int index = 0; index < conflicts.length; index++) {
            PluginInstallConflict conflict = (PluginInstallConflict)(conflicts[index]);
            if (!conflict.isFixed()) {
                return false;
            }
        }
        return true;
    }

}
