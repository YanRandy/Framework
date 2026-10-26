package randy.framework.util;

import java.util.HashMap;
import java.util.Map;

import randy.framework.helper.RViewRenderer;

public class RViewRegistry {
    private static final Map<String, RViewRenderer> render = new HashMap<>();
    
    public RViewRegistry(){
        register(".jsp", new RJspViewRenderer());
        register(".html", new RHtmlViewRenderer());
        register(".htm", new RHtmlViewRenderer());
    }
    
    public static void register(String suffix, RViewRenderer renderer) {
        render.put(suffix.toLowerCase(), renderer);
    }

    public static RViewRenderer getRenderer(String suffix) {
        return render.get(suffix.toLowerCase());
    }

    public RViewRenderer get(String suffix) {
        return getRenderer(suffix);
    }
}
