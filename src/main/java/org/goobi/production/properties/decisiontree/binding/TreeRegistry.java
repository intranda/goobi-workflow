package org.goobi.production.properties.decisiontree.binding;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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
import java.util.List;
import java.util.Optional;

import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.goobi.production.properties.decisiontree.parser.LoadReport;
import org.goobi.production.properties.decisiontree.parser.LoadReport.LoadMessage;
import org.goobi.production.properties.decisiontree.parser.TreeFileParser;
import org.goobi.production.properties.decisiontree.parser.TreeLoadException;

import lombok.extern.log4j.Log4j2;

/**
 * Holds the trees of the trees file. The file is parsed once and again whenever it changes (modification time or size), like the other configuration
 * files. A file that cannot be read leaves no trees and a file error; the last good state is not kept, so a broken edit is noticed at once.
 */
@Log4j2
public class TreeRegistry {

    private final Path file;
    private long lastModified = Long.MIN_VALUE;
    private long size = -1;
    private LoadReport report = new LoadReport(java.util.Map.of(), List.of());
    private String fileError;

    public TreeRegistry(Path file) {
        this.file = file;
    }

    /**
     * Looks up a tree.
     *
     * @param id id of the tree
     * @return the tree with this id, empty if there is none or it was rejected
     */
    public synchronized Optional<TreeDefinition> tree(String id) {
        reloadIfChanged();
        return Optional.ofNullable(report.trees().get(id));
    }

    /**
     * Explains why a tree is not available.
     *
     * @param id id of the tree
     * @return why the tree is not available: the error of the file, the load errors of the tree, or that it does not exist; empty if the tree is
     *         usable
     */
    public synchronized List<String> problems(String id) {
        reloadIfChanged();
        if (fileError != null) {
            return List.of(fileError);
        }
        if (report.trees().containsKey(id)) {
            return List.of();
        }
        List<String> errors = report.errorsOf(id).stream().map(LoadMessage::toString).toList();
        return errors.isEmpty() ? List.of("There is no tree with the id '" + id + "' in " + file.getFileName()) : errors;
    }

    /** @return all messages of the last load */
    public synchronized List<LoadMessage> messages() {
        reloadIfChanged();
        return report.messages();
    }

    private void reloadIfChanged() {
        long modified;
        long length;
        try {
            modified = Files.getLastModifiedTime(file).toMillis();
            length = Files.size(file);
        } catch (IOException e) {
            if (lastModified != Long.MIN_VALUE || fileError == null) {
                log.error("The trees file {} cannot be read: {}", file, e.getMessage());
            }
            lastModified = Long.MIN_VALUE;
            size = -1;
            report = new LoadReport(java.util.Map.of(), List.of());
            fileError = "The trees file " + file.getFileName() + " cannot be read: " + e.getMessage();
            return;
        }
        if (modified == lastModified && length == size) {
            return;
        }
        lastModified = modified;
        size = length;
        try (InputStream in = Files.newInputStream(file)) {
            report = new TreeFileParser().parse(in);
            fileError = null;
            report.messages().forEach(m -> log.warn("decision tree: {}", m));
        } catch (TreeLoadException | IOException e) {
            log.error("The trees file {} is not valid: {}", file, e.getMessage());
            report = new LoadReport(java.util.Map.of(), List.of());
            fileError = e.getMessage();
        }
    }
}
