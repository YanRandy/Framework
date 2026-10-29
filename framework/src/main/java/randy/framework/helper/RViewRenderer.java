package randy.framework.helper;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Stratégie de rendu d'une vue. Une implémentation par type de fichier (.jsp, .html),
 * choisie par RViewRegistry selon le suffix configuré.
 */
public interface RViewRenderer {
    void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException;
}