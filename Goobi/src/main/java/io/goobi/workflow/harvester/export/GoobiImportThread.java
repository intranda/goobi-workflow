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
 */
package io.goobi.workflow.harvester.export;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.goobi.workflow.harvester.DataManager;


public class GoobiImportThread extends Thread {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(GoobiImportThread.class);

    private File file;

    public GoobiImportThread(File file) {
        this.file = file;
    }

    @Override
    public void run() {
        String cmd = "java -jar \"" + file.getAbsolutePath() + "\"";
        logger.debug(cmd);
        Scanner scanner = null;
        try {
            DataManager.getInstance().setImportRunning(true);
            Process p = Runtime.getRuntime().exec(cmd);
            scanner = new Scanner(p.getInputStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // System.out.println(line);
                if (line.contains("Exception")) {
                    logger.error("Goobi ProcessImportModule has thrown an exception. Make sure it is properly configured.");
                    p.destroy();
                    break;
                }
            }
            scanner.close();
            // scanner = new Scanner(p.getErrorStream());
            // while (scanner.hasNextLine()) {
            // System.err.println(scanner.nextLine());
            // }
            // scanner.close();
            // p.destroy();
            logger.debug("Return code: " + p.exitValue());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            DataManager.getInstance().setImportRunning(false);
        }
    }
}
