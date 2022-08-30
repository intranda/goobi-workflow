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
package de.sub.goobi.metadaten;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import lombok.extern.log4j.Log4j2;

/**
 * Class for saving comments per image file
 * 
 * @author joel
 *
 */

@Log4j2
public class ImageCommentHelper {

    private static Gson gson = new Gson();
    private HashMap<String, ImageComments> commentFiles;
    private static final String PATH_ADJUSTMENT = "../";
    private static final String COMMENTS_FILE_NAME = "comments";
    private static final String JSON_EXTENSION = ".json";

    public ImageCommentHelper() {
        commentFiles = new HashMap<>();
    }

    private ImageComments getCommentFile(String imageFolderName) {
        if (!commentFiles.containsKey(imageFolderName)) {
            try {
                final String strCommentFile = buildCommentPath(imageFolderName);
                File commentsFile = new File(strCommentFile);
                if (commentsFile.exists()) {
                    BufferedReader br;
                    br = new BufferedReader(new FileReader(strCommentFile));
                    ImageComments commentsClassNew = gson.fromJson(br, ImageComments.class);
                    commentFiles.put(imageFolderName, commentsClassNew);
                } else {
                    ImageComments commentsClassNew = new ImageComments();
                    commentFiles.put(imageFolderName, commentsClassNew);
                }
            } catch (FileNotFoundException e) {
                log.error(e);
            }
        }
        return commentFiles.get(imageFolderName);
    }

    public String getComment(String imageFolderName, String imageName) {
        ImageComments commentsClass = getCommentFile(imageFolderName);
        return commentsClass.getComment(imageName);
    }

    public void setComment(String imageFolderName, String imageName, String comment) {
        ImageComments commentsClass = getCommentFile(imageFolderName);
        commentsClass.setComment(imageName, comment);
        final String strCommentFile = buildCommentPath(imageFolderName);
        try (Writer writer = new FileWriter(strCommentFile)) {
            gson.toJson(commentsClass, writer);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public Map<String, String> getComments(String imageFolderName) {
        return getCommentFile(imageFolderName).comments;
    }

    private final String buildCommentPath(String folderName) {
        final String type = getType(folderName);
        StringBuilder commentPath = new StringBuilder(appendSlash(folderName)); //NOSONAR, folderName cannot be NULL
        commentPath.append(PATH_ADJUSTMENT);
        commentPath.append(COMMENTS_FILE_NAME);
        commentPath.append(type);
        commentPath.append(JSON_EXTENSION);
        return commentPath.toString();
    }

    // Determine whether we are dealing with media or master
    private String getType(String path) {
        if (path == null) {
            return null;
        }
        String folderName = appendSlash(path);
        int lastUnderscoreIndex = folderName.lastIndexOf("_");
        if (lastUnderscoreIndex >= 0 && folderName.length() > 0) {
            return folderName.substring(lastUnderscoreIndex, folderName.length() - 1);
        } else {
            return "";
        }
    }

    private String appendSlash(String folderName) {
        String fixedFolderName = folderName;
        if (!fixedFolderName.endsWith("/")) {
            fixedFolderName += ("/");
        }
        return fixedFolderName;
    }

    public class ImageComments {
        private HashMap<String, String> comments;

        public ImageComments() {
            comments = new HashMap<>();
        }

        public String getComment(String imageName) {
            return comments.get(imageName);
        }

        public void setComment(String imageName, String comment) {
            comments.put(imageName, comment);
        }
    }

}
