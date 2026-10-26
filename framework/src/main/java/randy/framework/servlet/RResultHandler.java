package randy.framework.servlet;

import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.annotation.RRestApi;
import randy.framework.helper.RViewRenderer;
import randy.framework.model.RModelAndView;
import randy.framework.util.RViewRegistry;

public class RResultHandler {
    private final RViewRegistry registry;
    private final String prefix;
    private final String suffix;
    private final ObjectMapper mapper = new ObjectMapper();

    public RResultHandler(RViewRegistry registry, String prefix, String suffix) {
        this.registry = registry;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    // Retourne true si la réponse est complète (JSON ou vue rendue)
    public boolean handle(Method method, Object result,
                          HttpServletRequest req, HttpServletResponse res)
            throws Exception {

        // @RestApi → JSON
        if (method.isAnnotationPresent(RRestApi.class)) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().print(toJson(result));
            return true;
        }

        // ModelAndView → vue + modèle
        if (result instanceof RModelAndView mv) {
            mv.getModel().forEach(req::setAttribute);
            renderView(mv.getView(), req, res);
            return true;
        }

        // String → nom de vue
        if (result instanceof String viewName) {
            renderView(viewName, req, res);
            return true;
        }

        return false; // void ou autre → debug page
    }

    private void renderView(String viewName, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        String path = prefix + viewName + suffix;
        RViewRenderer renderer = registry.get(suffix);
        if (renderer == null) throw new ServletException("Aucun renderer pour : " + suffix);
        renderer.render(req, res, path);
    }

    private String toJson(Object obj) throws Exception {
        if (obj instanceof String s) return "\"" + s + "\"";
        return mapper.writeValueAsString(obj);
    }
}
