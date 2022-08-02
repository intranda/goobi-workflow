package org.goobi.production.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IServletPlugin;

import lombok.extern.log4j.Log4j2;

/**
 * Servlet implementation class PluginServlet
 */
@Log4j2
public class PluginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public PluginServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Map<String, String[]> parameter = request.getParameterMap();
        if (parameter.get("action") != null) {
            String action = parameter.get("action")[0];
            IServletPlugin servlet = (IServletPlugin) PluginLoader.getPluginByTitle(PluginType.Servlet, action);
            try {
                servlet.execute(request, response);
            } catch (ServletException | IOException e) {
                log.error(e);
            }
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            doGet(request, response);
        } catch (ServletException | IOException e) {
            log.error(e);
        }
    }

}
