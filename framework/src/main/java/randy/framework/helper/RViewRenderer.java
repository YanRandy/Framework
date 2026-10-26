package randy.framework.helper;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RViewRenderer {
    void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException;
}