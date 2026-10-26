package randy.framework.listener;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import randy.framework.model.RMapping;
import randy.framework.model.RUrlKey;
import randy.framework.util.RAnnotationHandler;

public class RInitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String packageToScan = context.getInitParameter("packageToScan");
        String prefix = context.getInitParameter("prefix");
        String suffix = context.getInitParameter("suffix");
        System.out.println("[FRAMEWORK] Initialisation au déploiement de l'application...");
        try {
            Map<RUrlKey, RMapping> urlList = new HashMap<>();
            RAnnotationHandler.scanPaths(packageToScan, urlList);
            context.setAttribute("urlList", urlList);
            context.setAttribute("prefix", prefix);
            context.setAttribute("suffix", suffix);
            System.out.println("[FRAMEWORK] Initialisation réussie. " + urlList.size() + " routes chargées en mémoire.");
        } catch (IllegalStateException e) {
            System.err.println("[FRAMEWORK] Erreur critique de routage interceptée au démarrage.");
            context.setAttribute("deploymentError", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        context.removeAttribute("urlList");
        context.removeAttribute("deploymentError");
        System.out.println("[FRAMEWORK] Contexte détruit et ressources libérées.");
    }
}