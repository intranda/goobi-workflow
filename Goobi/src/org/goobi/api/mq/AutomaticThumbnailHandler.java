package org.goobi.api.mq;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginReturnValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.goobi.beans.Process;

import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
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

public class AutomaticThumbnailHandler implements TicketHandler<PluginReturnValue> {
	
	public static String HANDLERNAME = "automatic_thumbnail";
	
	@Override
    public String getTicketHandlerName() {
        return HANDLERNAME;
    }
	
	//returns true if changed and false if not
	private boolean checkIfChanged(File outputDirectory, File[] fileList) {
		File thumbnailFile = outputDirectory.listFiles()[0];
		for(File img: fileList) {
			if(img.lastModified() > thumbnailFile.lastModified()) {
				return true;
			}
		}
		return false;
	}
	
	private void generateThumbnails(Process process, Boolean master, Boolean media, String imgDirectory, String command, int[] sizes, Step step) throws IOException, InterruptedException, SwapException, DAOException, ContentLibException {
		String defaultImageDirectory;
    	if(master) {
    		defaultImageDirectory = process.getImagesOrigDirectory(false);
    		generateThumbnailsFromDirectory(process, defaultImageDirectory, sizes, command);
    	}
    	if(media) {
    		defaultImageDirectory = process.getImagesTifDirectory(false);
    		generateThumbnailsFromDirectory(process, defaultImageDirectory, sizes, command);
    	}
    	if(! imgDirectory.isEmpty()) {
    		generateThumbnailsFromDirectory(process, imgDirectory, sizes, command);
    	}
    	if(! command.isEmpty()) {
    		new HelperSchritte().executeScriptForStepObject(step, command, false);
    	}
    }
    
    private void generateThumbnailsFromDirectory(Process process, String imageDirectory, int[] sizes, String command) throws SwapException, DAOException, ContentLibException {
		try {
			File[] fileList = new File(imageDirectory).listFiles();
			for(int size : sizes) {
				String thumbnailDirectory = process.getThumbsDirectory()+Paths.get(imageDirectory).getFileName().toString()+"_"+String.valueOf(size);
				File outputDirectory = new File(thumbnailDirectory);
				if(outputDirectory.exists()) {
					if(! checkIfChanged(outputDirectory, fileList)) {
						return;
					}
				}else {
					outputDirectory.mkdirs();
				}
				Scale scale = new Scale.ScaleToBox(new Dimension(size,size));
				for(File img : fileList) {
					if(img.isDirectory()) {
						continue;
					}
					String basename = FilenameUtils.getBaseName(img.toString());
					
					OutputStream out = new FileOutputStream(thumbnailDirectory+FileSystems.getDefault().getSeparator()+basename+"_"+size+".jpg");
					ImageRequest request = new ImageRequest(new URI(img.getAbsolutePath().replace(" ", "%20")), RegionRequest.FULL, scale, Rotation.NONE, Colortype.DEFAULT, ImageFileFormat.JPG, Map.of("ignoreWatermark", "true"));	//remove spaces from url
					new GetImageAction().writeImage(request, out);
				}
			}
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
    }
    
    private void generateThumbnailsWithSettings(Step step, Process process) throws IOException, InterruptedException, SwapException, DAOException {
    	JSONObject settings = step.getAutoThumbnailSettingsJSON();
    	Boolean master = false;
    	Boolean media = false;
    	String imgDirectory = "";
    	String scriptCommand = "";
    	int[] sizes = {};
    	
    	if(settings.has("Master")) {
    		master = settings.getBoolean("Master");
    	}
    	if(settings.has("Media")) {
    		media = settings.getBoolean("Media");
    	}
    	if(settings.has("Img_directory")) {
    		imgDirectory = settings.getString("Img_directory");
    	}
    	if(settings.has("Custom_script_command")) {
    		scriptCommand = settings.getString("Custom_script_command");
    	}
    	if(settings.has("Sizes")) {
    		JSONArray arr = settings.getJSONArray("Sizes");
    		sizes = new int[arr.length()];
    		for(int i = 0; i < arr.length(); i++) {
    			sizes[i] = arr.optInt(i);
    		}
    	}
    	
    	try {
			this.generateThumbnails(process, master, media, imgDirectory, scriptCommand, sizes, step);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public PluginReturnValue call(TaskTicket ticket) {
		Step step = StepManager.getStepById(ticket.getStepId());
		Process process = ProcessManager.getProcessById(ticket.getProcessId());
		try {
			this.generateThumbnailsWithSettings(step,process);
		}catch(Exception e) {
			return PluginReturnValue.ERROR;
		}
		return PluginReturnValue.FINISH;
	}
}
