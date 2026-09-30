package randy.framework.util;

import java.util.HashMap;
import java.util.Map;

import randy.framework.helper.ViewRenderer;

public class ViewRendererRegistry {
    private static final Map<String, ViewRenderer> render = new HashMap<>();
    
    public ViewRendererRegistry(){
        register(".jsp", new JspViewRenderer());
        register(".html", new HtmlViewRenderer());
        register(".htm", new HtmlViewRenderer());
    }
    
    public static void register(String suffix, ViewRenderer renderer) {
        render.put(suffix.toLowerCase(), renderer);
    }

    public static ViewRenderer getRenderer(String suffix) {
        return render.get(suffix.toLowerCase());
    }

    public ViewRenderer get(String suffix) {
        return getRenderer(suffix);
    }
}
