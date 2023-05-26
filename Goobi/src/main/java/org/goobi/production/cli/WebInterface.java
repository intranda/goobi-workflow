package org.goobi.production.cli;

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

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.ICommandPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class WebInterface extends HttpServlet {
    private static final long serialVersionUID = 6187229284187412768L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        if (ConfigurationHelper.getInstance().isEnableWebApi()) {
            String ip = "";
            String password = "";
            try {
                ip = req.getRemoteHost();
                if (ip.startsWith("127.0.0.1")) {
                    ip = req.getHeader("x-forwarded-for");
                    if (ip == null) {
                        ip = "127.0.0.1";
                    }
                }

                Map<String, String[]> map = req.getParameterMap();
                String[] pwMap = map.get("token");
                password = pwMap[0];
            } catch (Exception e) {
                resp.setContentType("");
                generateAnswer(resp, 401, "Internal error", "Missing credentials");
                return;

            }

            Map<String, String[]> parameter = req.getParameterMap();
            // command
            if (parameter.size() == 0) {
                generateAnswer(resp, 400, "Empty request", "no parameters given");
                return;
            }
            if (parameter.get("command") == null) {
                // error, no command found
                generateAnswer(resp, 400, "Empty command", "no command given");
                return;
            }

            String command = parameter.get("command")[0];
            if (command == null) {
                // error, no command found
                generateAnswer(resp, 400, "Empty command", "No command given. Use help as command to get more information.");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("command: " + command);
            }

            // check if command is allowed for used IP
            List<String> allowedCommandos = WebInterfaceConfig.getCredencials(ip, password);
            if (!allowedCommandos.contains(command)) {
                // error, no command found
                generateAnswer(resp, 401, "command not allowed", "command " + command + " is not allowed for your IP (" + ip
                        + ") and credentials. You are allowed to use the following commands:" + allowedCommandos);
                return;
            }

            // hand parameters over to command
            Map<String, String[]> map = req.getParameterMap();
            HashMap<String, String> params = new HashMap<>();
            Iterator<Entry<String, String[]>> i = map.entrySet().iterator();
            while (i.hasNext()) {
                Entry<String, String[]> entry = i.next();
                if (entry.getValue()[0] != null) {
                    params.put(entry.getKey(), entry.getValue()[0]);
                }
            }

            if (command.equals("help")) {
                if (params.containsKey("for")) {
                    try {
                        generateHelp(resp, params.get("for"));
                    } catch (IOException e) {
                        log.error(e);
                    }
                } else {
                    try {
                        generateHelp(resp, null);
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
                return;
            }

            // get correct plugin from list
            ICommandPlugin myCommandPlugin = (ICommandPlugin) PluginLoader.getPluginByTitle(PluginType.Command, command);
            if (myCommandPlugin == null) {
                generateAnswer(resp, 400, "invalid command", "command not found in list of command plugins");
                return;
            }

            myCommandPlugin.setParameterMap(params);

            // let command validate if all parameters are correct: null means
            // valid
            CommandResponse cr = myCommandPlugin.validate();
            if (cr != null) {
                generateAnswer(resp, cr);
                return;
            }

            // no validation errors, so call the command
            if (myCommandPlugin.usesHttpSession()) {
                myCommandPlugin.setHttpRequest(req);
                myCommandPlugin.setHttpResponse(resp);
            }
            cr = myCommandPlugin.execute();
            generateAnswer(resp, cr.getStatus(), cr.getTitle(), cr.getMessage());
        } else {
            generateAnswer(resp, 404, "web api deactivated", "web api not configured");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doGet(req, resp);
        } catch (ServletException | IOException e) {
            log.error(e);
        }
    }

    private void generateHelp(HttpServletResponse resp, String forCommand) throws IOException {
        StringBuilder allHelpBuilder = new StringBuilder();
        List<IPlugin> mycommands = PluginLoader.getPluginList(PluginType.Command);
        Collections.sort(mycommands, pluginComparator);
        for (IPlugin iPlugin : mycommands) {
            ICommandPlugin icp = (ICommandPlugin) iPlugin;
            if (forCommand == null || forCommand.equals(icp.help().getTitle()) || ("Command " + forCommand).equals(icp.help().getTitle())) {
                allHelpBuilder.append("<h4>").append(icp.help().getTitle()).append("</h4>");
                allHelpBuilder.append(icp.help().getMessage());
                allHelpBuilder.append("<br/><br/>");
            }
        }
        if (forCommand == null) {
            allHelpBuilder.append(
                    "<h4>You are searching for a description of one command only?</h4>Use the parameter 'for' to get the help only for one specific command.<br/><br/>Sample: 'for=AddToProcessLog'<br/><br/>");
        }
        generateAnswer(resp, 200, "Goobi Web API Help", allHelpBuilder.toString());
    }

    private void generateAnswer(HttpServletResponse resp, int status, String title, String message) {
        generateAnswer(resp, new CommandResponse(status, title, message));
    }

    private void generateAnswer(HttpServletResponse resp, CommandResponse cr) {
        resp.setStatus(cr.getStatus());
        String answer = "";
        answer += "<!DOCTYPE HTML>";
        answer += "<html>";
        answer += "<head>";
        answer += "<title>Goobi</title>";
        answer += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>";
        answer += "<style type=\"text/css\">";
        answer += "body{font-family: Helvetica, sans-serif;background:white;padding:10px;}";
        answer += "h1{font-size:34px;color:#FF7A00;text-align:center;margin-top:25px;}";
        answer += "h4{border-bottom: solid #448BC8 1px;margin-bottom: 10px; padding-bottom: 10px;}";
        answer += ".img1{position:fixed;left:15px;top:15px;}";
        answer += ".img2{position:fixed;right:15px;bottom:15px;}";
        answer +=
                ".content{background: #f9f9f9; border:solid #ddd 1px; padding: 20px; color:#999;font-size:14x;margin:0px auto;text-align:center;width:60%;}";
        answer += "</style>";
        answer += "</head>";
        answer += "<body>";
        answer += "<a href=\".\" target=\"_blank\"><img class=\"img1\" src=\"uii/template/img/webapi_1.png\"></a>";
        answer += "<a href=\"http://www.intranda.com\" target=\"_blank\"><img class=\"img2\" src=\"uii/template/img/webapi_2.png\"></a>";
        answer += "<h1>";
        answer += cr.getTitle();
        answer += "</h1>";
        answer += "<div class=\"content\">";
        answer += cr.getMessage();
        answer += " </div>";
        answer += "</body>";
        answer += "</html>";
        try {
            resp.getOutputStream().print(answer);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static Comparator<IPlugin> pluginComparator = (plugin1, plugin2) -> {
        return plugin1.getTitle().compareTo(plugin2.getTitle());
    };
}
