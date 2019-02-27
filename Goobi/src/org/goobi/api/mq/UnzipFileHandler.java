package org.goobi.api.mq;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.goobi.production.enums.PluginReturnValue;

import de.sub.goobi.helper.StorageProvider;
import lombok.extern.log4j.Log4j;

@Log4j
public class UnzipFileHandler implements TicketHandler<PluginReturnValue> {

    @Override
    public String getTicketHandlerName() {
        return "unzip";
    }

    @Override
    public PluginReturnValue call(TaskTicket ticket) {

        String source = ticket.getProperties().get("filename");
        String destination = ticket.getProperties().get("destination");

        try {
            Path workDir = Files.createTempDirectory(UUID.randomUUID().toString());
            Path zipFile = Paths.get(source);
            unzip(zipFile, workDir);
            //            Files.delete(zipFile);
            // alto files are imported into alto directory
            List<Path> altoFiles = new ArrayList<>();
            // objects are imported into the master directory
            List<Path> objectFiles = new ArrayList<>();

            try (DirectoryStream<Path> folderFiles = Files.newDirectoryStream(workDir)) {
                for (Path file : folderFiles) {
                    String fileName = file.getFileName().toString();
                    String fileNameLower = fileName.toLowerCase();
                    if (fileNameLower.endsWith(".xml") && !fileNameLower.startsWith(".")) {
                        altoFiles.add(file);
                    } else if (!fileNameLower.startsWith(".")) {
                        objectFiles.add(file);
                    }
                }
            } catch (IOException e1) {
                log.error(e1);
            }

            Path imagesDir = Paths.get(destination);
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }
            for (Path object : objectFiles) {
                StorageProvider.getInstance().copyFile(object, imagesDir.resolve(object.getFileName()));
            }

            String deleteFiles = ticket.getProperties().get("deleteFiles");
            if (StringUtils.isNotBlank(deleteFiles) && deleteFiles.equalsIgnoreCase("true")) {
                FileUtils.deleteQuietly(workDir.toFile());
            }

        } catch (IOException e) {
            log.error(e);
            return PluginReturnValue.ERROR;
        }

        return PluginReturnValue.FINISH;
    }

    private void unzip(final Path zipFile, final Path output) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final Path toPath = output.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(toPath);
                } else {
                    Files.copy(zipInputStream, toPath);
                }
            }
        }
    }
}
