package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.goobi.managedbeans.LoginBean;

import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.Helper;

public abstract class AbstractIGoobiScript implements IGoobiScript {
    protected List<Integer> processes;
    protected HashMap<String, String> parameters;
    protected String command;
    protected String username;
    protected GoobiScriptManager gsm;
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
    protected void addParameterToSampleCall(StringBuilder sb,String parameter, String value, String comment) {
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
    protected void addParameterToSampleCall(StringBuilder sb,String parameter, String value) {
        sb.append("\\n\\n");
        sb.append(parameter + ": " + value);
    }    

    public AbstractIGoobiScript() {
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
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        this.processes = processes;
        this.parameters = parameters;
        this.command = command;
        SessionForm sf = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        gsm = sf.getGsm();
        LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        username = login.getMyBenutzer().getNachVorname();
        starttime = System.currentTimeMillis();
        return true;
    }

    /**
     * return a parameter als boolean value. If the parameter is missing or null then false is returned.
     * 
     * @param name the name of the parameter to check
     * @return boolean of the value
     */
    public boolean getParameterAsBoolean(String name) {
        boolean value = false;
        if (parameters.get(name) != null && parameters.get(name).equals("true")){
            value = true;
        }
        return value;
    }
    
    /**
     * Main method of the GoobiScript where the processing of the job is executed
     */
    @Override
    public void execute() {
    }   
    
}
