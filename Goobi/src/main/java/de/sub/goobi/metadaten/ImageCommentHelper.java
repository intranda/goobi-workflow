package de.sub.goobi.metadaten;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

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
    //dictionary of comment files, so each is only read once
    private HashMap<String, ImageComments> commentFiles;

    public ImageCommentHelper() {

        commentFiles = new HashMap<>();

    }

    private ImageComments getCommentFile(String imageFolderName) {

        if (!commentFiles.containsKey(imageFolderName)) {
            try {

                String strCommentFile = imageFolderName + "imageComments.json";

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

        try (Writer writer = new FileWriter(imageFolderName + "imageComments.json")) {
            gson.toJson(commentsClass, writer);
        } catch (IOException e) {
            log.error(e);
        }

    }

    public HashMap<String, String> getComments(String imageFolderName) {

        return getCommentFile(imageFolderName).comments;
    }

    public class ImageComments {

        public HashMap<String, String> comments;

        public ImageComments() {
            comments = new HashMap<>();
        }

        public String getComment(String imageName) {

            return comments.get(imageName);
        }

        public void setComment(String imageName, String comment) {

            //            String currentUser = Helper.getCurrentUser().getNachVorname();
            //            Date dateNow = new Date();
            comments.put(imageName, comment);
        }

    }

}
