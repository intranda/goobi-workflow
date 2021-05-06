package org.goobi.managedbeans;

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

    public String parseUploadedPlugin() {
        if (currentExtractedPluginPath != null && Files.exists(currentExtractedPluginPath)) {
            FileUtils.deleteQuietly(currentExtractedPluginPath.toFile());
        }
        try (InputStream input = uploadedPluginFile.getInputStream();
                TarInputStream tarIn = new TarInputStream(input)) {
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
        } catch (IOException e) {
            log.error(e);
        }
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        try {
            this.pluginInstaller = PluginInstaller.createFromExtractedArchive(currentExtractedPluginPath);
        } catch (JDOMException | IOException e) {
            // TODO write error to GUI
            log.error(e);
        }
        return "";
    }

    public void cancelInstall() {
        FileUtils.deleteQuietly(this.currentExtractedPluginPath.toFile());
        try {
            uploadedPluginFile.delete();
        } catch (IOException e) {
            log.error(e);
        }
        this.pluginInstaller = null;
    }

}
