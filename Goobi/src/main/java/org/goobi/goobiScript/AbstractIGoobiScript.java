/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.sub.goobi.helper.Helper;

public abstract class AbstractIGoobiScript implements IGoobiScript {

    protected List<Integer> processes;
    protected Map<String, String> parameters;
    protected String command;
    protected String username;
    protected long starttime;

    /**
     * Return if this GoobiScript shall be publicly visible in the user interface
     * 
     * @return boolean about if the GoobiScript shall be visible
     */
    @Override
    public boolean isVisible() {
        return true;
    }

    /**
     * Return a sample call as yaml snippet this GoobiScript
     * 
     * @return the sample call itself
     */
    @Override
    public String getSampleCall() {
        return "There is currently no sample call available for this GoobiScript.";
    }

    /**
     * method to append yaml information to a string builder
     * 
     * @param sb StringBuilder to apppend to
     * @param comment a comment as documentation for this GoobiScript
     */
    protected void addNewActionToSampleCall(StringBuilder sb, String comment) {
        sb.append("---\\n# ");
        sb.append(comment);
        sb.append("\\naction: ");
        sb.append(getAction());
    }

    /**
     * method to append a description about a parameter as yaml to a given yaml StringBuilder
     * 
     * @param sb StringBuilder to append to
     * @param parameter the name of the parameter
     * @param value the default value for the parameter
     * @param comment a documentation for the parameter as comment
     */
    protected void addParameterToSampleCall(StringBuilder sb, String parameter, String value, String comment) {
        sb.append("\\n\\n# ");
        sb.append(comment);
        sb.append("\\n");
        sb.append(parameter + ": " + value);
    }

    /**
     * method to append a description about a parameter as yaml to a given yaml StringBuilder
     * 
     * @param sb StringBuilder to append to
     * @param parameter the name of the parameter
     * @param value the default value for the parameter
     */
    protected void addParameterToSampleCall(StringBuilder sb, String parameter, String value) {
        sb.append("\\n\\n");
        sb.append(parameter + ": " + value);
    }

    protected AbstractIGoobiScript() {
        super();
    }

    /**
     * initialize the GoobiScript with all needed information
     * 
     * @param processes a list of processes identifiers to run through
     * @param command the original command to use it for displaying only
     * @param parameters a hashmap of all parameters for this GoobiScript
     * 
     * @return true if the initialisation could be done without any validation issues
     */
    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        this.processes = processes;
        this.parameters = parameters;
        this.command = command;
        username = Helper.getCurrentUser().getNachVorname();
        starttime = System.currentTimeMillis();
        return new ArrayList<>();
    }

    /**
     * return a parameter als boolean value. If the parameter is missing or null then false is returned.
     * 
     * @param name the name of the parameter to check
     * @return boolean of the value
     */
    public boolean getParameterAsBoolean(String name) {
        boolean value = false;
        if (parameters.get(name) != null && parameters.get(name).equals("true")) {
            value = true;
        }
        return value;
    }
}
