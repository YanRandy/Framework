package randy.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.model.Mapping;
import randy.framework.model.UrlKey;

public class FrontControllerServlet extends HttpServlet {
    private Map<UrlKey, Mapping> urlList = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        this.urlList = (Map<UrlKey, Mapping>) context.getAttribute("urlList");
        if (urlList != null) {
            System.out.println("[FrontController] Récupération des " + this.urlList.size() + " routes effectuée.");
        } else {
            System.err.println("[FrontController] Erreur : Aucune table de routage trouvée dans le contexte !");
        }
        System.out.println("URLs mapped: " + urlList.size());
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

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String contextPath = request.getContextPath();
        String requestURL = request.getRequestURI();
        String pathInfo = requestURL.substring(contextPath.length());
        String httpMethod = request.getMethod();
        UrlKey key = new UrlKey(pathInfo, httpMethod);

        out.println("<html>");
        out.println("<head><title>Front Controller - Routage</title></head>");
        out.println("<body style='font-family: Arial, sans-serif; margin: 40px;'>");
        out.println("<h1>Front Controller</h1>");
        out.println("<p>URL demandée : <strong>" + pathInfo + "</strong> | Méthode : <strong>" + httpMethod
                + "</strong></p>");
        out.println("<hr/>");

        // Si l'url et la méthode sont supportées
        if (urlList != null && urlList.containsKey(key)) {
            Mapping map = urlList.get(key);
            out.println("<h3 style='color: green;'>✔ URL supportée</h3>");
            out.println(
                    "<p style='font-size: 16px; background-color: #f0fdf4; padding: 15px; border-left: 5px solid green;'>");
            out.println("<strong>[" + httpMethod + "] " + pathInfo + "</strong> &rarr; " + map.getClassName()
                    + " &rarr; " + map.getMethod() + "()");
            out.println("</p>");

            // Invocation de la méthode par réflexion
            try {
                Class<?> clazz = Class.forName(map.getClassName());
                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                Method targetMethod = clazz.getDeclaredMethod(map.getMethod());
                Object result = targetMethod.invoke(controllerInstance);
                // targetMethod.invoke(controllerInstance);
                out.println("<p style='color: blue; font-weight: bold;'>[Succès] La méthode du contrôleur a été exécutée. Veuillez consulter les logs de votre serveur.</p>");
                if (result != null) {
                    out.println("<p style='color: green;'><strong>Résultat :</strong> " + result + "</p>");
                }
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException
                    | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                out.println("<h3 style='color: red;'>✘ Erreur lors de l'exécution de la méthode</h3>");
                out.println("<pre style='background: #fee2e2; padding: 10px;'>");
                e.printStackTrace(out);
                out.println("</pre>");
            }
        }
        // Si la combinaison URL / Méthode n'est pas supportée
        else {
            out.println("<h3 style='color: red;'>✘ Erreur : L'URL exacte n'est pas supportée pour cette méthode</h3>");
            out.println("<p>Aucune correspondance exacte trouvée pour <code>" + pathInfo + "</code> en mode <strong>"
                    + httpMethod + "</strong>.</p>");
            out.println("<h4>Routes suggérées utilisant <code>" + pathInfo + "</code> comme préfixe :</h4>");

            out.println(
                    "<table border='1' cellpadding='10' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
            out.println("<tr style='background-color: #f3f4f6;'>");
            out.println("<th>Méthode HTTP</th><th>URL</th><th>Controller</th><th>Méthode Class</th>");
            out.println("</tr>");

            boolean aDesSuggestions = false;

            if (urlList != null && !urlList.isEmpty()) {
                for (Map.Entry<UrlKey, Mapping> entry : urlList.entrySet()) {
                    String routeDisponible = entry.getKey().getUrl();
                    if (routeDisponible.startsWith(pathInfo)) {
                        aDesSuggestions = true;
                        out.println("<tr>");
                        out.println(
                                "<td><span style='background: #e5e7eb; padding: 3px 8px; border-radius: 4px; font-weight: bold;'>"
                                        + entry.getKey().getHttpMethod() + "</span></td>");
                        out.println("<td><code>" + routeDisponible + "</code></td>");
                        out.println("<td>" + entry.getValue().getClassName() + "</td>");
                        out.println("<td>" + entry.getValue().getMethod() + "()</td>");
                        out.println("</tr>");
                    }
                }
            }
            if (!aDesSuggestions) {
                out.println("<tr><td colspan='4' style='text-align:center; color: gray;'>");
                out.println("Aucune sous-route trouvée. Voici toutes les configurations de l'application :");
                out.println("</td></tr>");

                for (Map.Entry<UrlKey, Mapping> entry : urlList.entrySet()) {
                    out.println("<tr>");
                    out.println(
                            "<td><span style='background: #e5e7eb; padding: 3px 8px; border-radius: 4px; font-weight: bold;'>"
                                    + entry.getKey().getHttpMethod() + "</span></td>");
                    out.println("<td><code>" + entry.getKey().getUrl() + "</code></td>");
                    out.println("<td>" + entry.getValue().getClassName() + "</td>");
                    out.println("<td>" + entry.getValue().getMethod() + "()</td>");
                    out.println("</tr>");
                }
            }
            out.println("</table>");
        }
        out.println("</body>");
        out.println("</html>");
    }
}