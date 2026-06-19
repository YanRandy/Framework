package randy.framework.model;

public class Mapping {
    private String className;  // Nom de la classe du contrôleur (ex: com.test.controller.MonController)
    private String method;     // Nom de la méthode à exécuter (ex: afficherAccueil)

    public Mapping(String className, String method) {
        this.className = className;
        this.method = method;
    }

    // Getters et Setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}