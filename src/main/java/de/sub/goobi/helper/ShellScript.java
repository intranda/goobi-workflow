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
package de.sub.goobi.helper;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.StringUtils;
import org.goobi.production.enums.LogType;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ShellScript {

    public static final int ERRORLEVEL_ERROR = 1;

    private final String command;
    private List<String> outputChannel;
    private List<String> errorChannel;
    @Getter
    private Integer errorLevel;

    /**
     * This returns the command.
     * 
     * @return the command
     */
    public Path getCommand() {
        return Paths.get(command);
    }

    /**
     * This returns the command string.
     * 
     * @return the command
     */
    public String getCommandString() {
        return command;
    }

    /**
     * Provides the results of the script written on standard out. Null if the script has not been run yet.
     * 
     * @return the output channel
     */
    public List<String> getStdOut() {
        return outputChannel;
    }

    /**
     * Provides the content of the standard error channel. Null if the script has not been run yet.
     * 
     * @return the error channel
     */
    public List<String> getStdErr() {
        return errorChannel;
    }

    /**
     * A shell script must be initialised with an existing file on the local file system.
     * 
     * @param executable Script to run
     * @throws FileNotFoundException is thrown if the given executable does not exist.
     */
    public ShellScript(Path executable) throws FileNotFoundException {
        if (!StorageProvider.getInstance().isFileExists(executable)) {
            throw new FileNotFoundException("Could not find executable: " + executable.toString());
        }
        command = executable.toString();
    }

    /**
     * The function run() will execute the system command. This is a shorthand to run the script without arguments.
     *
     * @return result
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting, then the wait is ended and an
     *             InterruptedException is thrown.
     */
    public int run() throws IOException, InterruptedException {
        return run(null);
    }

    /**
     * The function run() will execute the system command. First, the call sequence is created, including the parameters passed to run(). Then, the
     * underlying OS is contacted to run the command. Afterwards, the results are being processed and stored.
     * 
     * The behaviour is slightly different from the legacy callShell2() command, as it returns the error level as reported from the system process.
     * Use this to get the old behaviour:
     * 
     * <pre>
     * Integer err = scr.run(args);
     * if (scr.getStdErr().size() &gt; 0)
     *     err = ShellScript.ERRORLEVEL_ERROR;
     * </pre>
     * 
     * @param args A list of arguments passed to the script. May be null.
     * @return result
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting, then the wait is ended and an
     *             InterruptedException is thrown.
     */
    public int run(List<String> args) throws IOException, InterruptedException {

        List<String> commandLine = new ArrayList<>();
        commandLine.add(command);
        if (args != null) {
            commandLine.addAll(args);
        }
        Process process = null;
        try {
            String[] callSequence = commandLine.toArray(new String[commandLine.size()]);
            ProcessBuilder pb = new ProcessBuilder(callSequence);
            // If we call another java process with JDK_JAVA_OPTIONS set, we will get a message on stderr
            // which in turn will make Goobi think this process failed. For this reason,
            // JDK_JAVA_OPTIONS is unset here.
            pb.environment().remove("JDK_JAVA_OPTIONS");
            ConfigurationHelper config = ConfigurationHelper.getInstance();
            if (config.useCustomS3()) {
                pb.environment().put("CUSTOM_S3", "true");
                pb.environment().put("S3_ENDPOINT_URL", config.getS3Endpoint());
                pb.environment().put("S3_ACCESSKEYID", config.getS3AccessKeyID());
                pb.environment().put("S3_SECRETACCESSKEY", config.getS3SecretAccessKey());
            }
            process = pb.start();
            InputStream stdOut = process.getInputStream();
            InputStream stdErr = process.getErrorStream();

            FutureTask<List<String>> stdOutFuture = new FutureTask<>(() -> inputStreamToLinkedList(stdOut));
            Thread stdoutThread = new Thread(stdOutFuture);
            stdoutThread.setDaemon(true);
            stdoutThread.start();
            FutureTask<List<String>> stdErrFuture = new FutureTask<>(() -> inputStreamToLinkedList(stdErr));
            Thread stderrThread = new Thread(stdErrFuture);
            stderrThread.setDaemon(true);
            stderrThread.start();

            outputChannel = stdOutFuture.get();
            errorChannel = stdErrFuture.get();
        } catch (IOException | ExecutionException error) {
            throw new IOException(error.getMessage());
        } finally {
            if (process != null) {
                closeStream(process.getInputStream());
                closeStream(process.getOutputStream());
                closeStream(process.getErrorStream());
            }
        }
        errorLevel = process.waitFor();
        return errorLevel;
    }

    /**
     * The function inputStreamToLinkedList() reads an InputStream and returns it as a LinkedList.
     * 
     * @param myInputStream Stream to convert
     * @return A linked list holding the single lines.
     */
    public static List<String> inputStreamToLinkedList(InputStream myInputStream) {
        List<String> result = new LinkedList<>();
        try (Scanner inputLines = new Scanner(myInputStream)) {
            while (inputLines.hasNextLine()) {
                String myLine = inputLines.nextLine();
                result.add(myLine);
            }
        }
        return result;
    }

    /**
     * This behaviour was already implemented. I can’t say if it’s necessary.
     * 
     * @param inputStream A stream to close.
     */
    private static void closeStream(Closeable inputStream) {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            log.warn("Could not close stream.", e);
            Helper.setFehlerMeldung("Could not close open stream.");
        }
    }

    /**
     * Call a shell script/program and write the output of that call as message into the user interface if the output is not empty.
     * 
     * @param parameter List of parameters to call the shell command
     * @param processID ID of the process to allow writing of messages into the process log in case of errors
     * @return result
     * @throws IOException
     * @throws InterruptedException
     */
    public static ShellScriptReturnValue callShell(List<String> parameter, Integer processID) throws IOException, InterruptedException {
        int returnCode = ShellScript.ERRORLEVEL_ERROR;
        String outputText = "";
        String errorText = "";
        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();
        if (parameter.isEmpty()) {
            return new ShellScriptReturnValue(0, null, null);
        }

        if (log.isDebugEnabled()) {
            log.debug(parameter);
        }

        String scriptname = parameter.get(0);
        List<String> parameterWithoutCommand = null;
        if (parameter.size() > 1) {
            parameterWithoutCommand = parameter.subList(1, parameter.size());
        }
        try {
            ShellScript s = new ShellScript(Paths.get(scriptname));

            returnCode = s.run(parameterWithoutCommand);

            for (String line : s.getStdOut()) {
                outputBuilder.append(line);
                outputBuilder.append("\n");
            }
            outputText = outputBuilder.toString();
            Helper.addMessageToProcessJournal(processID, LogType.DEBUG, "Script '" + scriptname + "' was executed with result: " + outputText);
            if (!outputText.isEmpty()) {
                Helper.setMeldung(outputText);
            }
            if (!s.getStdErr().isEmpty()) {
                returnCode = ShellScript.ERRORLEVEL_ERROR;
                for (String line : s.getStdErr()) {
                    errorBuilder.append(line);
                    errorBuilder.append("\n");
                }
                errorText = errorBuilder.toString();
                Helper.addMessageToProcessJournal(processID, LogType.ERROR,
                        "Error occured while executing script '" + scriptname + "': " + errorText);
                Helper.setFehlerMeldung(errorText);
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException in callShell2()", e);
            Helper.addMessageToProcessJournal(processID, LogType.ERROR,
                    "Exception occured while executing script '" + scriptname + "': " + e.getMessage());
            Helper.setFehlerMeldung("Couldn't find script file in callShell2(), error", e.getMessage());
        }
        return new ShellScriptReturnValue(returnCode, outputText, errorText);
    }

    /**
     * This implements the legacy Helper.callShell2() command. This is subject to whitespace problems and is maintained here for backward
     * compatibility only. Please don’t use.
     * 
     * @param nonSpacesafeScriptingCommand A single line command which mustn’t contain parameters containing white spaces.
     * @param processID
     * @return error level on success, 1 if an error occurs
     * @throws InterruptedException In case the script was interrupted due to concurrency
     * @throws IOException If an I/O error happens
     */
    public static ShellScriptReturnValue legacyCallShell2(String nonSpacesafeScriptingCommand, Integer processID)
            throws IOException, InterruptedException {
        ShellScript s;
        int returnCode = ShellScript.ERRORLEVEL_ERROR;
        String outputMessage = "";
        String errorMessage = "";
        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();
        try {
            String scriptname = "";
            String paramList = "";
            if (nonSpacesafeScriptingCommand.contains(" ")) {
                scriptname = nonSpacesafeScriptingCommand.substring(0, nonSpacesafeScriptingCommand.indexOf(" "));
                paramList = nonSpacesafeScriptingCommand.substring(nonSpacesafeScriptingCommand.indexOf(" ") + 1);
            } else {
                scriptname = nonSpacesafeScriptingCommand;
            }
            s = new ShellScript(Paths.get(scriptname));

            List<String> scriptingArgs = new ArrayList<>();
            if (paramList != null && !paramList.isEmpty()) {
                String[] params = null;
                if (paramList.contains("\"")) {
                    params = paramList.split("\"");
                } else {
                    params = paramList.split(" ");
                }
                for (String param : params) {
                    if (!param.trim().isEmpty()) {
                        scriptingArgs.add(param.trim());
                    }
                }
            } else {
                scriptingArgs.add(paramList);
            }
            returnCode = s.run(scriptingArgs);

            for (String line : s.getStdOut()) {
                outputBuilder.append(line);
                outputBuilder.append("\n");
            }
            outputMessage = outputBuilder.toString();
            Helper.addMessageToProcessJournal(processID, LogType.DEBUG,
                    "Script '" + nonSpacesafeScriptingCommand + "' was executed with result: " + outputMessage);
            if (StringUtils.isNotBlank(outputMessage)) {
                Helper.setMeldung(outputMessage);
            }
            if (!s.getStdErr().isEmpty()) {
                returnCode = ShellScript.ERRORLEVEL_ERROR;

                for (String line : s.getStdErr()) {
                    errorBuilder.append(line);
                    errorBuilder.append("\n");
                }
                errorMessage = errorBuilder.toString();
                Helper.addMessageToProcessJournal(processID, LogType.ERROR,
                        "Error occured while executing script '" + nonSpacesafeScriptingCommand + "': " + errorMessage);
                if (StringUtils.isNotBlank(errorMessage)) {
                    Helper.setFehlerMeldung(errorMessage);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException in callShell2()", e);
            Helper.addMessageToProcessJournal(processID, LogType.ERROR,
                    "Exception occured while executing script '" + nonSpacesafeScriptingCommand + "': " + e.getMessage());
            Helper.setFehlerMeldung("Couldn't find script file in callShell2(), error", e.getMessage());
        }
        return new ShellScriptReturnValue(returnCode, outputMessage, errorMessage);
    }
}