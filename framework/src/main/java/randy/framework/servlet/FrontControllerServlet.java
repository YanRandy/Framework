package randy.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.model.Mapping;
import randy.framework.util.Utilitaire;

public class FrontControllerServlet extends HttpServlet {
    private Map<String, Mapping> urlList = new java.util.HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String packageToScan = config.getInitParameter("packageToScan");
        this.urlList = Utilitaire.scanPaths(packageToScan);
        System.out.println("URLs mapped: " + urlList.size());
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)     throws ServletException, IOException {
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
        // chemin relatif de la requete (ex: /code-test/accueil) -> de le accueil raisiny
        String contextPath = request.getContextPath();
        // String requestURL = request.getRequestURL().toString();
        String requestURL = request.getRequestURI();
        String pathInfo = requestURL.substring(contextPath.length());

        //affchage top
        out.println("<html>");
        out.println("<head><title>Front Controller - Routage</title></head>");
        out.println("<body style='font-family: Arial, sans-serif; margin: 40px;'>");
        out.println("<h1>Front Controller</h1>");
        out.println("<p>URL demandée : <strong>" + pathInfo + "</strong></p>");
        out.println("<hr/>");
        // si l'url est supportee
        if(urlList != null && urlList.containsKey(pathInfo)) {
            Mapping map = urlList.get(pathInfo);
            out.println("<h3 style='color: green;'>✔ URL supportée</h3>");
            out.println("<p style='font-size: 16px; background-color: #f0fdf4; padding: 15px; border-left: 5px solid green;'>");
            //Format de l'affichage : url -> controller -> methode
            out.println("<strong>" + pathInfo + "</strong> &rarr; " + map.getClassName() + " &rarr; " + map.getMethod() + "()");
            out.println("</p>");
        }
        // si url non supportee
        else {
            out.println("<h3 style='color: red;'>✘ Erreur : L'URL n'est pas supportée</h3>");
            out.println("<p>Voici la liste des URLs disponibles dans l'application :</p>");
            
            out.println("<table border='1' cellpadding='10' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
            out.println("<tr style='background-color: #f3f4f6;'>");
            out.println("<th>URL</th><th>Controller</th><th>Méthode</th>");
            out.println("</tr>");
            // affichage de tous les trucs dispo url -> controller -> methode
            if (urlList != null && !urlList.isEmpty()) {
                for (Map.Entry<String, Mapping> entry : urlList.entrySet()) {
                    out.println("<tr>");
                    out.println("<td><code>" + entry.getKey() + "</code></td>");
                    out.println("<td>" + entry.getValue().getClassName() + "</td>");
                    out.println("<td>" + entry.getValue().getMethod() + "()</td>");
                    out.println("</tr>");
                }
            }
            else {
                out.println("<tr><td colspan='3' style='text-align:center;'>Aucun contrôleur disponible dans le classpath.</td></tr>");
            }
            out.println("</table>");
        }
        out.println("</body>");
        out.println("</html>");
    }
}
