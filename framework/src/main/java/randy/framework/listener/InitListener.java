package randy.framework.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.Map;
import randy.framework.model.Mapping;
import randy.framework.model.UrlKey;
import randy.framework.util.Utilitaire;

@WebListener
public class InitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        // 1. Récupération du paramètre de configuration global de l'application
        String packageToScan = context.getInitParameter("packageToScan");
        
        System.out.println("[FRAMEWORK] Initialisation au déploiement de l'application...");
        
        // 2. Exécution du scan des contrôleurs
        Map<UrlKey, Mapping> urlList = Utilitaire.scanPaths(packageToScan);
        
        // 3. Stockage de la liste des routes dans le contexte applicatif global
        context.setAttribute("urlList", urlList);
        
        System.out.println("[FRAMEWORK] Initialisation réussie. " + urlList.size() + " routes chargées en mémoire.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Code de nettoyage éventuel lors de l'arrêt ou du redéploiement de l'application
        ServletContext context = sce.getServletContext();
        context.removeAttribute("urlList");
        System.out.println("[FRAMEWORK] Contexte détruit et ressources libérées.");
    }
}