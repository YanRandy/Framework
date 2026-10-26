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
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.auth.RAuthSecurity;
import randy.framework.model.RMapping;
import randy.framework.model.RUrlKey;
import randy.framework.util.RRouteDebugger;
import randy.framework.util.RViewRegistry;

@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 20 * 1024 * 1024)
public class RFrontController extends HttpServlet {
    private Map<RUrlKey, RMapping> urlList = new HashMap<>();
    private RViewRegistry registry;
    private RControllerExec invoker;
    private RResultHandler resultHandler;
    private RAuthSecurity authSecurity;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext ctx = config.getServletContext();

        Exception err = (Exception) ctx.getAttribute("deploymentError");
        if (err != null)
            throw new ServletException(err.getMessage(), err);

        Map<RUrlKey, RMapping> load = (Map<RUrlKey, RMapping>) ctx.getAttribute("urlList");
        if (load == null)
            throw new ServletException("urlList introuvable !");
        this.urlList = load;

        String prefix = (String) ctx.getAttribute("prefix");
        String suffix = (String) ctx.getAttribute("suffix");
        this.registry = new RViewRegistry();
        this.invoker = new RControllerExec();
        this.resultHandler = new RResultHandler(registry, prefix, suffix);
        this.authSecurity = new RAuthSecurity(
                param(ctx, "authUserKey", "user"),
                param(ctx, "authRoleKey", "role"),
                param(ctx, "loginUrl", "/login"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod();
        RUrlKey key = new RUrlKey(pathInfo, httpMethod);

        ApplicationContext spring = (ApplicationContext) request.getServletContext()
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        if (!urlList.containsKey(key)) {
            RRouteDebugger.renderNotFound(pathInfo, httpMethod, urlList, response);
            return;
        }

        RMapping map = urlList.get(key);
        if (!authSecurity.allow(map, request, response)) {
            return; // access denied, response already handled
        }
        try {
            Class<?> clazz = Class.forName(map.getClassName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method method = invoker.findMethod(clazz, map.getMethod());
            Object result = invoker.invoke(method, instance, spring, request);
            boolean handled = resultHandler.handle(method, result, request, response);

            if (!handled) {
                RRouteDebugger.renderSuccess(pathInfo, httpMethod, map, result, response);
            }
        } catch (Exception e) {
            RRouteDebugger.renderError(pathInfo, httpMethod, map, e, response);
        }
    }

    public static String param(ServletContext ctx, String name, String def) {
        String v = ctx.getInitParameter(name);
        return (v == null || v.isBlank()) ? def : v;
    }
}