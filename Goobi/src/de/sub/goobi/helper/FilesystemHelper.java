/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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

package de.sub.goobi.helper;

import org.apache.log4j.Logger;

import de.sub.goobi.config.ConfigurationHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Helper class for file system operations.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class FilesystemHelper {
    private static final Logger logger = Logger.getLogger(FilesystemHelper.class);

    /**
     * Creates a directory with a name given. Under Linux a script is used to set the file system permissions accordingly. This cannot be done from
     * within java code before version 1.7.
     * 
     * @param dirName Name of directory to create
     * @throws InterruptedException If the thread running the script is interrupted by another thread while it is waiting, then the wait is ended and
     *             an InterruptedException is thrown.
     * @throws IOException If an I/O error occurs.
     */

    public static void createDirectory(String dirName) throws IOException, InterruptedException {
        if (!Files.exists(Paths.get(dirName))) {
            ShellScript createDirScript = new ShellScript(Paths.get(ConfigurationHelper.getInstance().getScriptCreateDirMeta()));
            createDirScript.run(Arrays.asList(new String[] { dirName }));
        }
    }

    /**
     * Creates a directory with a name given and assigns permissions to the given user. Under Linux a script is used to set the file system
     * permissions accordingly. This cannot be done from within java code before version 1.7.
     * 
     * @param dirName Name of directory to create
     * @throws InterruptedException If the thread running the script is interrupted by another thread while it is waiting, then the wait is ended and
     *             an InterruptedException is thrown.
     * @throws IOException If an I/O error occurs.
     */

    public static void createDirectoryForUser(String dirName, String userName) throws IOException, InterruptedException {
        if (!Files.exists(Paths.get(dirName))) {
            ShellScript createDirScript = new ShellScript(Paths.get(ConfigurationHelper.getInstance().getScriptCreateDirUserHome()));
            createDirScript.run(Arrays.asList(new String[] { userName, dirName }));
        }
    }

    public static void deleteSymLink(String symLink) {
        String command = ConfigurationHelper.getInstance().getScriptDeleteSymLink();
        ShellScript deleteSymLinkScript;
        try {
            deleteSymLinkScript = new ShellScript(Paths.get(command));
            deleteSymLinkScript.run(Arrays.asList(new String[] { symLink }));
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException in deleteSymLink()", e);
            Helper.setFehlerMeldung("Couldn't find script file, error", e.getMessage());
        } catch (IOException e) {
            logger.error("IOException in deleteSymLink()", e);
            Helper.setFehlerMeldung("Aborted deleteSymLink(), error", e.getMessage());
        } catch (InterruptedException e) {
            logger.error("InterruptedException in deleteSymLink()", e);
            Helper.setFehlerMeldung("Command '" + command + "' is interrupted in deleteSymLink()!");
        }
    }

   
}