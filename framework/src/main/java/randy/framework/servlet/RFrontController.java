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
import randy.framework.util.RControllerRegistry;
import randy.framework.util.RRouteDebugger;
import randy.framework.util.RViewRegistry;

/**
 * Servlet unique de l'application (mappée sur "/") : toutes les requêtes
 * passent ici.
 * Pour chaque requête : trouve la route → vérifie l'auth → instancie le
 * controller
 * → binde les paramètres et appelle la méthode → transforme le résultat en
 * réponse.
 * 
 * @MultipartConfig permet de recevoir des fichiers (RMapFile).
 */
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 20 * 1024 * 1024)
public class RFrontController extends HttpServlet {
    private Map<RUrlKey, RMapping> urlList = new HashMap<>();
    private RViewRegistry registry;
    private RControllerExec invoker;
    private RResultHandler resultHandler;
    private RAuthSecurity authSecurity;
    private RControllerRegistry controllerRegistry;

    /**
     * Récupère ce que RInitListener a préparé et crée les objets réutilisés à
     * chaque requête.
     * Si le scan a échoué (doublon d'URL), on relance l'erreur : l'application ne
     * démarre pas.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext ctx = config.getServletContext();

        Exception err = (Exception) ctx.getAttribute("deploymentError");
        if (err != null)
            throw new ServletException(err.getMessage(), err);

        this.controllerRegistry = (RControllerRegistry) ctx.getAttribute("controllerRegistry");
        if (controllerRegistry == null)
            throw new ServletException("controllerRegistry introuvable !");

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

    /**
     * Cœur du framework. L'URL utilisée est celle sans le contexte
     * (ex : /code-test/livres → /livres), combinée au verbe HTTP pour former la
     * clé.
     */
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
            Object instance = controllerRegistry.get(map.getClassName());
            Class<?> clazz = instance.getClass();
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

    /**
     * Lit un context-param du web.xml, ou retourne la valeur par défaut s'il est
     * absent/vide.
     */
    public static String param(ServletContext ctx, String name, String def) {
        String v = ctx.getInitParameter(name);
        return (v == null || v.isBlank()) ? def : v;
    }
}