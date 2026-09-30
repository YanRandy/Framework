package randy.framework.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import randy.framework.model.Mapping;
import randy.framework.model.UrlKey;

public class RouteDebugger {

    public static void renderNotFound(String pathInfo, String httpMethod,
            Map<UrlKey, Mapping> urlList, HttpServletResponse response) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append("<html>");
        buf.append("<head><title>Front Controller - Routage</title></head>");
        buf.append("<body style='font-family: Arial, sans-serif; margin: 40px;'>");
        buf.append("<h1>Front Controller</h1>");
        buf.append("<p>URL demandée : <strong>").append(pathInfo)
                .append("</strong> | Méthode : <strong>").append(httpMethod).append("</strong></p>");
        buf.append("<hr/>");
        buf.append("<h3 style='color: red;'>✘ Erreur : L'URL exacte n'est pas supportée pour cette méthode</h3>");
        buf.append("<p>Aucune correspondance exacte trouvée pour <code>").append(pathInfo)
                .append("</code> en mode <strong>").append(httpMethod).append("</strong>.</p>");
        buf.append("<h4>Routes suggérées utilisant <code>").append(pathInfo)
                .append("</code> comme préfixe :</h4>");
        buf.append("<table border='1' cellpadding='10' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        buf.append("<tr style='background-color: #f3f4f6;'>");
        buf.append("<th>Méthode HTTP</th><th>URL</th><th>Controller</th><th>Méthode Class</th>");
        buf.append("</tr>");

        boolean hasSuggestions = false;
        if (urlList != null && !urlList.isEmpty()) {
            for (Map.Entry<UrlKey, Mapping> entry : urlList.entrySet()) {
                String availableRoute = entry.getKey().getUrl();
                if (availableRoute.startsWith(pathInfo)) {
                    hasSuggestions = true;
                    appendRouteRow(buf, entry.getKey(), entry.getValue());
                }
            }
        }

        if (!hasSuggestions) {
            buf.append("<tr><td colspan='4' style='text-align:center; color: gray;'>");
            buf.append("Aucune sous-route trouvée. Voici toutes les configurations de l'application :");
            buf.append("</td></tr>");

            if (urlList != null) {
                for (Map.Entry<UrlKey, Mapping> entry : urlList.entrySet()) {
                    appendRouteRow(buf, entry.getKey(), entry.getValue());
                }
            }
        }

        buf.append("</table>");
        buf.append("</body>");
        buf.append("</html>");

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(buf);
    }

    public static void renderSuccess(String pathInfo, String httpMethod,
            Mapping map, Object result, HttpServletResponse response) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append("<html><head><title>Front Controller - Succès</title></head>");
        buf.append("<body style='font-family: Arial, sans-serif; margin: 40px;'>");
        buf.append("<h1>Front Controller</h1>");
        buf.append("<p>URL demandée : <strong>").append(pathInfo)
                .append("</strong> | Méthode : <strong>").append(httpMethod).append("</strong></p>");
        buf.append("<hr/>");
        buf.append("<h3 style='color: green;'>✔ URL supportée</h3>");
        buf.append("<p style='font-size: 16px; background-color: #f0fdf4; padding: 15px; border-left: 5px solid green;'>");
        buf.append("<strong>[").append(httpMethod).append("] ").append(pathInfo).append("</strong> &rarr; ")
                .append(map.getClassName()).append(" &rarr; ").append(map.getMethod()).append("()");
        buf.append("</p>");
        buf.append("<p style='color: blue;'>[Succès] Méthode exécutée.</p>");
        if (result != null) {
            buf.append("<p><strong>Résultat :</strong> ").append(result).append("</p>");
        }
        buf.append("</body></html>");

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(buf);
    }

    public static void renderError(String pathInfo, String httpMethod,
            Mapping map, Exception e, HttpServletResponse response) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append("<html><head><title>Front Controller - Erreur</title></head>");
        buf.append("<body style='font-family: Arial, sans-serif; margin: 40px;'>");
        buf.append("<h1>Front Controller</h1>");
        buf.append("<p>URL demandée : <strong>").append(pathInfo)
                .append("</strong> | Méthode : <strong>").append(httpMethod).append("</strong></p>");
        buf.append("<hr/>");
        buf.append("<h3 style='color: red;'>✘ Erreur lors de l'exécution de la méthode</h3>");
        if (map != null) {
            buf.append("<p><strong>Mapping:</strong> ").append(map.getClassName())
                    .append("#").append(map.getMethod()).append("()</p>");
        }
        buf.append("<pre style='background: #fee2e2; padding: 10px;'>");
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        buf.append(sw);
        buf.append("</pre>");
        buf.append("</body></html>");

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(buf);
    }

    private static void appendRouteRow(StringBuilder buf, UrlKey key, Mapping map) {
        buf.append("<tr>");
        buf.append("<td><span style='background: #e5e7eb; padding: 3px 8px; border-radius: 4px; font-weight: bold;'>")
                .append(key.getHttpMethod()).append("</span></td>");
        buf.append("<td><code>").append(key.getUrl()).append("</code></td>");
        buf.append("<td>").append(map.getClassName()).append("</td>");
        buf.append("<td>").append(map.getMethod()).append("()</td>");
        buf.append("</tr>");
    }
}
