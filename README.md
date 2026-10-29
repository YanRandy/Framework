# Randy Framework

Petit framework MVC Java écrit from scratch, inspiré de Spring MVC : routage HTTP par annotations,
binding automatique des paramètres, rendu de vues (JSP / HTML), JSON, upload de fichiers, session
et authentification par rôles. Spring n'est utilisé que comme conteneur de beans (services,
repositories) ; Spring MVC n'est pas utilisé.

- Java 21, Jakarta Servlet 5 (testé avec Tomcat 10)
- Convention de nommage : **toutes les classes commencent par `R`** (R = Randy), dans le framework
  comme dans le projet de test.

---

## Structure du dépôt

```
Framework/
├── pom.xml                  pom parent (modules : framework, puis code)
├── framework/               le framework (jar)
│   ├── TodoList.md          suivi des sprints 10 / 10-bis (auth)
│   ├── librairie.sql        base MySQL de test (tables livres, Personne)
│   └── src/main/java/randy/framework/
└── code/                    application de test (war) qui utilise le framework
    ├── deploy.sh            build + copie du war dans Tomcat
    └── src/main/
        ├── java/com/example/{controller,entity,service,repository}/
        └── webapp/WEB-INF/{web.xml, applicationContext.xml, views/}
```

---

## Installation et lancement

Prérequis : JDK 21, Maven, Tomcat 10, MySQL (seulement pour les routes livres / personnes).

```bash
# 1. Base de données (optionnel)
mysql -u root -p < framework/librairie.sql

# 2. Build complet depuis la racine (framework puis code)
mvn clean package

# 3. Déploiement dans Tomcat (adapter TOMCAT_WEBAPPS dans le script)
cd code && bash deploy.sh
```

L'application est ensuite disponible sur `http://localhost:8080/code-test/`.

> **Toujours builder depuis le pom parent** (ce que fait `deploy.sh`). Un `mvn package` lancé dans
> `code/` seul utilise le `framework-1.0.jar` de `~/.m2`, qui n'est pas mis à jour : les
> modifications du framework n'apparaîtraient pas. Alternative : `mvn install` à la racine.

---

## Cycle d'une requête

```
Requête HTTP
  └── RFrontController                (servlet unique, mappée sur "/")
        ├── clé = RUrlKey(url sans contexte, verbe HTTP)
        ├── recherche dans la table des routes ── absente ──▶ RRouteDebugger.renderNotFound (404 debug)
        ├── RAuthSecurity.allow()           ── refus ──▶ 401 / 403 / redirection loginUrl
        ├── new Controller() + RControllerExec.findMethod()
        ├── RControllerExec.invoke()  → RBindingParameter.bindAll()  (arguments depuis la requête)
        └── RResultHandler.handle()
              ├── @RRestApi        → JSON (Jackson)
              ├── RModelAndView    → attributs de requête + vue
              ├── String           → nom de vue
              └── void / autre     → RRouteDebugger.renderSuccess (page debug)
```

La table des routes est construite **une seule fois au démarrage** : `RInitListener` appelle
`RAnnotationHandler.scanPaths()`, qui scanne `packageToScan` avec ClassGraph et range une
`Map<RUrlKey, RMapping>` dans le `ServletContext` (`urlList`). Les infos d'auth et `@RRestApi`
sont lues à ce moment-là et stockées dans le `RMapping`.

Les controllers ne sont pas mis en cache : une nouvelle instance est créée à chaque requête via le
constructeur sans argument.

---

## Organisation du framework

```
randy/framework/
├── annotation/
│   ├── RController.java        marque une classe comme controller
│   ├── RUrlMapping.java        URL + verbe(s) HTTP d'une méthode
│   ├── RRestApi.java           retour écrit en JSON
│   └── RAuthenticated.java     protège une méthode ou une classe (option roles)
├── auth/
│   └── RAuthSecurity.java      vérifie connexion / rôles, envoie 401, 403 ou redirection
├── binding/
│   ├── RTypeResolver.java      classe un paramètre (primitif, objet, liste, map, fichier, session...)
│   └── RBindingParameter.java  construit les arguments depuis la requête
├── listener/
│   └── RInitListener.java      scan au démarrage, table des routes dans le ServletContext
├── model/
│   ├── RMapping.java           classe + méthode + auth/roles/rest d'une route
│   ├── RUrlKey.java            clé (url, verbe) de la table des routes
│   ├── RModelAndView.java      vue + modèle
│   ├── RMapFile.java           fichier uploadé
│   └── RSession.java           surcouche de HttpSession
├── servlet/
│   ├── RFrontController.java   point d'entrée unique
│   ├── RControllerExec.java    trouve et appelle la méthode
│   └── RResultHandler.java     transforme le retour en réponse
├── helper/
│   └── RViewRenderer.java      interface de rendu de vue
└── util/
    ├── RAnnotationHandler.java scan ClassGraph + détection des URL en double
    ├── RViewRegistry.java      suffix → renderer
    ├── RJspViewRenderer.java   forward JSP
    ├── RHtmlViewRenderer.java  HTML avec remplacement de {cle}
    └── RRouteDebugger.java     pages debug (404, succès, exception)
```

---

## Annotations

### `@RController` et `@RUrlMapping`

```java
@RController
public class RLivreController {

    @RUrlMapping(value = "/livres", method = "GET")
    public RModelAndView afficherLivres(ApplicationContext ctx) { ... }

    @RUrlMapping("/")            // sans "method" → GET et POST
    public String index() { return "accueil"; }
}
```

- L'URL est comparée **exactement** : pas de variables de chemin, pas de wildcard, pas de `/` final toléré.
- Deux méthodes avec la même URL et le même verbe → **erreur au démarrage** (l'application ne se lance pas).
- La méthode est retrouvée **par son nom** : pas de surcharge (deux méthodes du même nom) dans un controller.

### `@RRestApi`

Le retour est sérialisé en JSON (`application/json;charset=UTF-8`). Une `String` retournée devient
une chaîne JSON, pas une vue.

### `@RAuthenticated` (Sprint 10 / 10-bis)

| Annotation | Effet |
|---|---|
| *(aucune)* | route publique |
| `@RAuthenticated` | **auth simple** : il faut être connecté |
| `@RAuthenticated(roles = {"ADMIN", "MANAGER"})` | **auth complexe** : connecté **et** au moins un des rôles |

- Sur une **classe** : s'applique à toutes ses routes. Sur une **méthode** : remplace celle de la classe.
- « Connecté » = l'attribut de session `authUserKey` existe. Les rôles sont lus dans `authRoleKey`
  (une `String`, un tableau ou une `Collection`).
- Refus :

| Cas | Route `@RRestApi` | Route vue |
|---|---|---|
| non connecté | `401` + `{"error":"non authentifié"}` | redirection vers `loginUrl` |
| rôle manquant | `403` + `{"error":"accès refusé"}` | erreur `403` |

Se connecter revient simplement à poser ces attributs en session :

```java
@RRestApi
@RUrlMapping(value = "/sprint/auth/login", method = "POST")
public Object login(String username, String password, String role, RSession session) {
    // ... vérification du mot de passe ...
    session.set("user", username);
    session.set("role", role);
    return Map.of("message", "connecté");
}
```

Exemples complets : `RAuthController` (annotations sur les méthodes) et `RAdminController`
(annotation sur la classe). Les tests curl sont dans [framework/TodoList.md](framework/TodoList.md).

---

## Paramètres des méthodes

Le nom du paramètre Java = le nom du paramètre HTTP. Cela marche grâce à l'option de compilation
`-parameters` (dans les deux `pom.xml`) : **ne pas la retirer**.

| Type du paramètre | Rempli avec |
|---|---|
| `ApplicationContext` | le contexte Spring (pour `ctx.getBean(...)`) |
| `RSession` | la session HTTP (créée si besoin) |
| `String`, `int`/`Integer`, `long`, `float`, `double`, `boolean`, `byte`, `char`, `short` | `request.getParameter(nom)` converti |
| `T[]`, `List<T>` | `request.getParameterValues(nom)` ; objets via `nom[i].champ` |
| `Map<K, V>` | tous les paramètres (`V` = tableau, primitif ou objet) |
| `RMapFile` | le fichier uploadé du champ `nom` (formulaire `multipart/form-data`) |
| `Map<String, RMapFile>` | tous les fichiers uploadés |
| autre classe | objet instancié puis rempli par ses setters |

Binding d'objet :

```
?nom=Rakoto&age=20                        → un seul objet : pas de préfixe
?personne.nom=Rakoto&livre.titre=Java     → plusieurs objets : préfixe = nom du paramètre
?nom=Rakoto&ami.nom=Rabe                  → objet imbriqué (notation pointée)
?personnes=a&personnes=b&personnes[0].nom=..&personnes[1].nom=..   → List<RPersonne>
```

Une protection empêche la boucle infinie quand un type se contient lui-même (`RPersonne.ami`).
Les champs de type tableau / `List` / `Map` d'un objet ne sont pas bindés.

---

## Valeurs de retour et vues

| Retour | Réponse |
|---|---|
| `@RRestApi` | JSON |
| `RModelAndView` | chaque entrée du modèle devient un attribut de requête, puis la vue est rendue |
| `String` | nom de vue |
| `void` / autre | page debug du framework (« URL supportée ») |

Chemin de la vue = `prefix + nom + suffix`, par ex. `/WEB-INF/views/` + `livres` + `.jsp`.

| Suffix | Rendu |
|---|---|
| `.jsp` | forward JSP : `${livres}`, `<c:forEach>`... |
| `.html`, `.htm` | lecture du fichier et remplacement de `{cle}` ; un tableau devient `<ul><li>` |

Nouveau type de vue : `RViewRegistry.register(".ext", monRenderer)`.

---

## Session (`RSession`, Sprint 9)

Déclarer un paramètre `RSession` suffit. Méthodes principales : `set`, `get(cle)`,
`get(cle, Type.class)`, `has`, `remove`, `getAll`, `clear`, `invalidate`, `regenerateId(req)`,
`setMaxInactiveInterval`. `getCache()` donne la Map locale : y écrire directement ne touche pas la
vraie session tant qu'on n'appelle pas `flush()`. Exemples : `RSessionController`.

---

## Intégration Spring

`ContextLoaderListener` charge `applicationContext.xml` (scan de `com.example.service` et
`com.example.repository`, DataSource MySQL). Un controller récupère un bean via le paramètre
`ApplicationContext` :

```java
@RUrlMapping(value = "/api/livres", method = "GET")
@RRestApi
public Object getLivres(ApplicationContext ctx) {
    return ctx.getBean(RLivreService.class).getTousLesLivres();
}
```

Les controllers sont créés par le framework, pas par Spring : pas d'injection dans les controllers.

---

## Configuration `web.xml`

```xml
<context-param><param-name>packageToScan</param-name><param-value>com.example</param-value></context-param>
<context-param><param-name>prefix</param-name><param-value>/WEB-INF/views/</param-value></context-param>
<context-param><param-name>suffix</param-name><param-value>.jsp</param-value></context-param>

<!-- Auth (valeurs par défaut si absent : user, role, /login) -->
<context-param><param-name>authUserKey</param-name><param-value>user</param-value></context-param>
<context-param><param-name>authRoleKey</param-name><param-value>role</param-value></context-param>
<context-param><param-name>loginUrl</param-name><param-value>/login</param-value></context-param>

<listener><listener-class>org.springframework.web.context.ContextLoaderListener</listener-class></listener>
<listener><listener-class>randy.framework.listener.RInitListener</listener-class></listener>

<servlet>
    <servlet-name>FrontController</servlet-name>
    <servlet-class>randy.framework.servlet.RFrontController</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>FrontController</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

Sans `packageToScan`, tout le classpath est scanné.

---

## Application de test (`code/`)

| Controller | Contenu |
|---|---|
| `RAccueilController` | `/`, `/accueil`, routes de test GET/POST, `/api/popo` |
| `RLivreController` | `/livres` (JSP), `/api/livres` (JSON) |
| `RPersonneController` | formulaire, liste, insertions (`/save`, `/api/save`, `/complexe/save`...) |
| `RBindController` | `/api/binding/*` (tous les cas de binding), `/upload` (formulaire + upload) |
| `RSessionController` | `/sprint/session/*`, `/sprint/panier/*` (Sprint 9) |
| `RAuthController` | `/login`, `/sprint/auth/*` (Sprint 10 / 10-bis, annotations sur les méthodes) |
| `RAdminController` | `/sprint/admin/*` (annotation sur la classe) |

Entités `RLivre`, `RPersonne` ; services `RLivreService`, `RPersonneService` ; repositories
`RLivreRepository`, `RPersonneRepository` (JdbcTemplate).
