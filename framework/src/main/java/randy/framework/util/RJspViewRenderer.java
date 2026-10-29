package randy.framework.util;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.helper.RViewRenderer;

/**
 * Rendu JSP : forward vers le fichier, qui lit les attributs de requête en EL/JSTL.
 */
public class RJspViewRenderer implements RViewRenderer {
    @Override
    public void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        // request.setAttribute("org.apache.catalina.jsp_file", viewPath);
        // request.getServletContext().getNamedDispatcher("jsp").forward(request, response);
        request.getRequestDispatcher(viewPath).forward(request, response);
    }
}