package randy.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.annotation.RestApi;
import randy.framework.helper.ViewRenderer;
import randy.framework.model.Mapping;
import randy.framework.model.ModelAndView;
import randy.framework.model.UrlKey;
import randy.framework.util.HtmlViewRenderer;
import randy.framework.util.JspViewRenderer;

public class FrontControllerServlet extends HttpServlet {
    private Map<UrlKey, Mapping> urlList = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, ViewRenderer> RENDERERS = Map.of(
            ".jsp", new JspViewRenderer(),
            ".html", new HtmlViewRenderer(),
            ".htm", new HtmlViewRenderer());
    private String prefix;
    private String suffix;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        Exception deploymentError = (Exception) context.getAttribute("deploymentError");
        if (deploymentError != null) {
            throw new ServletException(
                    "[FRAMEWORK] Échec du démarrage : " + deploymentError.getMessage(),
                    deploymentError);
        }
        Map<UrlKey, Mapping> load = (Map<UrlKey, Mapping>) context.getAttribute("urlList");
        if (load != null) {
            this.urlList = load;
            System.out.println("[FrontController] " + this.urlList.size() + " routes chargées.");
        } else {
            throw new ServletException("[FRAMEWORK] urlList introuvable dans le contexte !");
        }
        this.prefix = (String) context.getAttribute("prefix");
        this.suffix = (String) context.getAttribute("suffix");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        StringBuilder buf = new StringBuilder();

        String contextPath = request.getContextPath();
        String requestURL = request.getRequestURI();
        String pathInfo = requestURL.substring(contextPath.length());
        String httpMethod = request.getMethod();
        UrlKey key = new UrlKey(pathInfo, httpMethod);
        ApplicationContext springContext = (ApplicationContext) request.getServletContext()
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        buf.append("<html>");
        buf.append("<head><title>Front Controller - Routage</title></head>");
        buf.append("<body style='font-family: Arial, sans-serif; margin: 40px;'>");
        buf.append("<h1>Front Controller</h1>");
        buf.append("<p>URL demandée : <strong>").append(pathInfo).append("</strong> | Méthode : <strong>")
                .append(httpMethod).append("</strong></p>");
        buf.append("<hr/>");

        // Si l'url et la méthode sont supportées
        if (urlList != null && urlList.containsKey(key)) {
            Mapping map = urlList.get(key);
            buf.append("<h3 style='color: green;'>✔ URL supportée</h3>");
            buf.append(
                    "<p style='font-size: 16px; background-color: #f0fdf4; padding: 15px; border-left: 5px solid green;'>");
            buf.append("<strong>[").append(httpMethod).append("] ").append(pathInfo).append("</strong> &rarr; ")
                    .append(map.getClassName()).append(" &rarr; ").append(map.getMethod()).append("()");
            buf.append("</p>");
            try {
                Class<?> clazz = Class.forName(map.getClassName());
                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                Method targetMethod = null;
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equals(map.getMethod())) {
                        targetMethod = m;
                        break;
                    }
                }
                if (targetMethod == null) {
                    throw new ServletException("Méthode introuvable : " + map.getMethod());
                }
                Class<?>[] paramTypes = targetMethod.getParameterTypes();
                Object result;
                if (paramTypes.length == 1
                        && ApplicationContext.class.isAssignableFrom(paramTypes[0])) {
                    result = targetMethod.invoke(controllerInstance, springContext);
                } else {
                    result = targetMethod.invoke(controllerInstance);
                }
                if (targetMethod.isAnnotationPresent(RestApi.class)) {
                    response.setContentType("application/json;charset=UTF-8");
                    try {
                        response.getWriter().print(toJson(result));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (result instanceof ModelAndView mv) {
                    for (Map.Entry<String, Object[]> entry : mv.getModel().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                    renderView(mv.getView(), request, response);
                    return;
                } else if (result instanceof String viewName) {
                    renderView(viewName, request, response);
                    return;
                }
                buf.append("<p style='color: blue;'>[Succès] Méthode exécutée.</p>");
                if (result != null) {
                    buf.append("<p><strong>Résultat :</strong> ").append(result).append("</p>");
                }
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException
                    | NoSuchMethodException | SecurityException | InvocationTargetException | ServletException e) {
                buf.append("<h3 style='color: red;'>✘ Erreur lors de l'exécution de la méthode</h3>");
                buf.append("<pre style='background: #fee2e2; padding: 10px;'>");
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                buf.append(sw.toString());
                buf.append("</pre>");
            }
        }
        // Si la combinaison URL / Méthode n'est pas supportée
        else {
            buf.append("<h3 style='color: red;'>✘ Erreur : L'URL exacte n'est pas supportée pour cette méthode</h3>");
            buf.append("<p>Aucune correspondance exacte trouvée pour <code>").append(pathInfo)
                    .append("</code> en mode <strong>").append(httpMethod).append("</strong>.</p>");
            buf.append("<h4>Routes suggérées utilisant <code>").append(pathInfo).append("</code> comme préfixe :</h4>");

            buf.append(
                    "<table border='1' cellpadding='10' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
            buf.append("<tr style='background-color: #f3f4f6;'>");
            buf.append("<th>Méthode HTTP</th><th>URL</th><th>Controller</th><th>Méthode Class</th>");
            buf.append("</tr>");

            boolean aDesSuggestions = false;

            if (urlList != null && !urlList.isEmpty()) {
                for (Map.Entry<UrlKey, Mapping> entry : urlList.entrySet()) {
                    String routeDisponible = entry.getKey().getUrl();
                    if (routeDisponible.startsWith(pathInfo)) {
                        aDesSuggestions = true;
                        buf.append("<tr>");
                        buf.append(
                                "<td><span style='background: #e5e7eb; padding: 3px 8px; border-radius: 4px; font-weight: bold;'>")
                                .append(entry.getKey().getHttpMethod()).append("</span></td>");
                        buf.append("<td><code>").append(routeDisponible).append("</code></td>");
                        buf.append("<td>").append(entry.getValue().getClassName()).append("</td>");
                        buf.append("<td>").append(entry.getValue().getMethod()).append("()</td>");
                        buf.append("</tr>");
                    }
                }
            }
            if (!aDesSuggestions) {
                buf.append("<tr><td colspan='4' style='text-align:center; color: gray;'>");
                buf.append("Aucune sous-route trouvée. Voici toutes les configurations de l'application :");
                buf.append("</td></tr>");

                for (Map.Entry<UrlKey, Mapping> entry : urlList.entrySet()) {
                    buf.append("<tr>");
                    buf.append(
                            "<td><span style='background: #e5e7eb; padding: 3px 8px; border-radius: 4px; font-weight: bold;'>")
                            .append(entry.getKey().getHttpMethod()).append("</span></td>");
                    buf.append("<td><code>").append(entry.getKey().getUrl()).append("</code></td>");
                    buf.append("<td>").append(entry.getValue().getClassName()).append("</td>");
                    buf.append("<td>").append(entry.getValue().getMethod()).append("()</td>");
                    buf.append("</tr>");
                }
            }
            buf.append("</table>");
        }
        buf.append("</body>");
        buf.append("</html>");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(buf);
    }

    private void renderView(String viewName, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = prefix + viewName + suffix;
        ViewRenderer renderer = RENDERERS.get(suffix.toLowerCase());
        if (renderer == null) {
            throw new ServletException("Aucun renderer configuré pour le suffixe : " + suffix);
        }
        renderer.render(request, response, path);
    }

    private String toJson(Object obj) throws Exception {
        if (obj instanceof String s) {
            return "\"" + s + "\""; // pas besoin de Jackson pour une String
        }
        return mapper.writeValueAsString(obj); // List, Objet, etc.
    }
}