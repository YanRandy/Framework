package randy.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.helper.RViewRenderer;

public class RHtmlViewRenderer implements RViewRenderer {
    @Override
    public void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (InputStream in = request.getServletContext().getResourceAsStream(viewPath)) {
            if (in == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Vue introuvable : " + viewPath);
                return;
            }
            // 1- Lecture du fichier
            String htmlContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            // 2- Dynamisation: parcours les attributs renvoyes du controlleurr
            Enumeration<String> attributeNames = request.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String key = attributeNames.nextElement();
                Object value = request.getAttribute(key);
                if (value != null) {
                    String replacement;
                    if (value instanceof Object[] array) {
                        StringBuilder sb = new StringBuilder("<ul>");
                        for (Object item : array) {
                            sb.append("<li>").append(item).append("</li>");
                        }
                        sb.append("</ul>");
                        replacement = sb.toString();
                    }
                    else {
                        replacement = value.toString();
                    }
                    htmlContent = htmlContent.replace("{" + key + "}", replacement);
                }
            }
            response.getWriter().write(htmlContent);
        }
    }
}