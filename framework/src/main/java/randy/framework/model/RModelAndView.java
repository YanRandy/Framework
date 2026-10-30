package randy.framework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Retour d'un controller qui veut afficher une vue avec des données.
 * Chaque entrée du modèle devient un attribut de requête utilisable dans la JSP (${cle}).
 */
public class RModelAndView {
    private String view;
    private Map<String, Object[]> model;

    public RModelAndView(String view) {
        this.view = view;
        this.model = new HashMap<>();
    }

    public RModelAndView() {
        this.model = new HashMap<>();
    }

    public String getView() {
        return view;
    }

    public Map<String, Object[]> getModel() {
        return model;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setAttribute(String key, Object[] value) {
        this.model.put(key, value);
    }
}