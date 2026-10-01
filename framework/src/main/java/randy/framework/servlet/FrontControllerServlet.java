package randy.framework.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.model.Mapping;
import randy.framework.model.UrlKey;
import randy.framework.util.RouteDebugger;
import randy.framework.util.ViewRendererRegistry;

public class FrontControllerServlet extends HttpServlet {
    private Map<UrlKey, Mapping> urlList = new HashMap<>();
    private ViewRendererRegistry registry;
    private ControllerExecServlet invoker;
    private ResultHandlerServlet  resultHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext ctx = config.getServletContext();

        // erreur de démarrage
        Exception err = (Exception) ctx.getAttribute("deploymentError");
        if (err != null) throw new ServletException(err.getMessage(), err);

        // urlList
        Map<UrlKey, Mapping> load = (Map<UrlKey, Mapping>) ctx.getAttribute("urlList");
        if (load == null) throw new ServletException("urlList introuvable !");
        this.urlList = load;

        // collaborateurs
        String prefix = (String) ctx.getAttribute("prefix");
        String suffix = (String) ctx.getAttribute("suffix");
        this.registry      = new ViewRendererRegistry();
        this.invoker       = new ControllerExecServlet();
        this.resultHandler = new ResultHandlerServlet(registry, prefix, suffix);
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo   = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod();
        UrlKey key        = new UrlKey(pathInfo, httpMethod);

        ApplicationContext spring = (ApplicationContext) request.getServletContext()
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        if (!urlList.containsKey(key)) {
            RouteDebugger.renderNotFound(pathInfo, httpMethod, urlList, response);
            return;
        }

        Mapping map = urlList.get(key);
        try {
            Class<?> clazz      = Class.forName(map.getClassName());
            Object   instance   = clazz.getDeclaredConstructor().newInstance();
            Method   method     = invoker.findMethod(clazz, map.getMethod());
            Object   result     = invoker.invoke(method, instance, spring, request);
            boolean  handled    = resultHandler.handle(method, result, request, response);

            if (!handled) {
                RouteDebugger.renderSuccess(pathInfo, httpMethod, map, result, response);
            }
        } catch (Exception e) {
            RouteDebugger.renderError(pathInfo, httpMethod, map, e, response);
        }
    }
}