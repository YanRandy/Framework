# Framework

A small Java MVC framework built from scratch, inspired by Spring MVC. It does HTTP routing,
annotation scanning, request-parameter binding and view rendering, and it can reuse a Spring
application context for its beans. Spring MVC itself is not used.

The code targets Java 21 and runs inside a Jakarta Servlet 5 container (tested with Tomcat 10).

---

## How a request is handled

```
HTTP request
  └── FrontControllerServlet              (single entry point, mapped on /)
        ├── builds UrlKey(pathInfo, httpMethod)
        ├── looks it up in the urlList map   ── miss ──▶ RouteDebugger.renderNotFound()
        ├── Class.forName(className) → newInstance() → findMethod()
        ├── ControllerExecServlet.invoke()   → BindingParameter.bindAll()
        └── ResultHandlerServlet.handle()
              ├── @RestApi            → JSON via Jackson
              ├── returns ModelAndView → attributes set, view rendered
              ├── returns String       → rendered as a view name
              └── anything else        → RouteDebugger.renderSuccess()
```

The route table is built once at startup. `InitListener` runs on `contextInitialized`, scans the
configured package with ClassGraph, and stores a `Map<UrlKey, Mapping>` in the `ServletContext`
under the key `urlList`. `FrontControllerServlet.init()` reads that map, together with the view
prefix and suffix, and wires up the renderer registry.

Controllers are not cached: a new instance is created for every request through its no-argument
constructor. Spring is only used to resolve beans that the controller asks for.

---

## Project layout

```
framework/src/main/java/randy/framework/
├── annotation/
│   ├── Controller.java          marks a class as a controller
│   ├── UrlMapping.java          maps a method to a URL and HTTP method(s)
│   └── RestApi.java             marks a method whose result is serialized as JSON
├── binding/
│   ├── TypeResolver.java        classifies a parameter (primitive, object, list, map, array…)
│   └── BindingParameter.java    builds the argument array from the request
├── listener/
│   └── InitListener.java        scans packages at startup, builds urlList
├── model/
│   ├── Mapping.java             className + methodName for one route
│   ├── ModelAndView.java        view name + model (Map<String, Object[]>)
│   └── UrlKey.java              composite map key (url + httpMethod)
├── servlet/
│   ├── FrontControllerServlet.java   routing, the only servlet you map in web.xml
│   ├── ControllerExecServlet.java    locates and invokes the target method
│   └── ResultHandlerServlet.java     turns the return value into a response
├── helper/
│   └── ViewRenderer.java        rendering strategy interface
└── util/
    ├── Utilitaire.java          ClassGraph scan + duplicate-route detection
    ├── ViewRendererRegistry.java maps a suffix to a renderer
    ├── JspViewRenderer.java     forwards to a JSP via RequestDispatcher
    ├── HtmlViewRenderer.java    substitutes {placeholder} tokens in an HTML file
    └── RouteDebugger.java       HTML debug pages for 404s, successes and exceptions
```

---

## Annotations

### `@Controller`

Placed on the class. The scanner only looks at methods inside classes carrying this annotation.

```java
@Controller
public class BookController { }
```

### `@UrlMapping`

Placed on a method. `value` is the exact URL path; `method` is one or more HTTP verbs and defaults
to `{"GET", "POST"}`.

```java
@UrlMapping(value = "/books", method = "GET")
public ModelAndView list() { ... }

@UrlMapping(value = "/books", method = "POST")
public String save(BookForm form) { ... }

@UrlMapping("/")                 // GET + POST
public void home() { ... }
```

Two methods declaring the same URL and verb are a startup error: the scan throws, the listener
stores the failure as `deploymentError`, and `FrontControllerServlet.init()` rethrows it so the
application fails fast instead of silently choosing one route.

Matching is exact. There are no path variables, no wildcards and no trailing-slash tolerance.

### `@RestApi`

Placed on a method whose return value should be written as JSON rather than resolved as a view.

```java
@UrlMapping(value = "/api/books", method = "GET")
@RestApi
public List<Book> all() { ... }
```

---

## Method parameters

Parameter binding is handled by `BindingParameter.bindAll()`, which classifies each parameter
through `TypeResolver.resolve()` and then builds it from the request.

| Parameter type | Bound from |
|---|---|
| `ApplicationContext` | the Spring context registered in the `ServletContext` |
| `String`, `int`/`Integer`, `long`/`Long`, `float`/`Float`, `double`/`Double`, `boolean`/`Boolean` | `request.getParameter(name)`, converted to the declared type |
| `T[]` | `request.getParameterValues(name)`, each element converted or bound as an object |
| `List<T>` | same as an array; element type is read from the generic signature |
| `Map<K, V>` | `request.getParameterMap()`; `V` may be an array, a primitive wrapper or an object |
| any other class | instantiated and populated through its setters |

Objects are filled by calling setters: the parameter (or field) name is matched against the request
parameter of the same name. Nested objects use dot notation, and a guard set prevents infinite
recursion when a type refers back to itself.

```
/books?title=CleanCode&author.nom=Martin&author.prenom=Robert
```

```java
public ModelAndView save(Book book) { ... }   // binds title and a nested author object
```

Array and `List` elements are addressed by index:

```
/tags?tags=java&tags=spring   →  List<String> or String[]
/items?items[0].name=a&items[1].name=b   →  List<Item>
```

Two things to keep in mind:

- Binding reads parameter names through `Parameter.getName()`, which only survives compilation
  because the compiler plugin is configured with `-parameters`. Removing that flag breaks binding
  for every parameter that is not explicitly named.
- Nested object binding skips fields whose type is an array, `List` or `Map`; those still have to
  be bound from a top-level parameter.

---

## Return values

`ResultHandlerServlet.handle()` inspects the method annotation first, then the returned object.

| Return value | Response |
|---|---|
| `@RestApi` on the method | `application/json;charset=UTF-8`, body serialized by Jackson |
| `ModelAndView` | each entry of the model is set as a request attribute, then the view is rendered |
| `String` | the value is used as the view name |
| `void` or anything else | the route debug page, confirming the method ran |

Note that with `@RestApi`, a method returning `String` produces a JSON string literal, not a view.

---

## View rendering

A view is resolved as `prefix + viewName + suffix`, where both parts come from the servlet context.
`ViewRendererRegistry` picks the renderer from the suffix:

| Suffix | Renderer |
|---|---|
| `.jsp` | `JspViewRenderer` — forwards to the file with `RequestDispatcher` |
| `.html`, `.htm` | `HtmlViewRenderer` — reads the file and substitutes `{key}` tokens from request attributes |

For a JSP, the model is available through the usual EL and JSTL:

```jsp
<c:forEach var="item" items="${myList}">
    <li>${item.title}</li>
</c:forEach>
```

For static HTML, request attributes referenced as `{key}` are replaced in place. An attribute whose
value is an `Object[]` is rendered as a `<ul>` list:

```html
<p>{message}</p>
<ul>{lines}</ul>
```

The registry is populated in its constructor and can be extended at runtime with
`ViewRendererRegistry.register(suffix, renderer)`.

---

## Spring integration

The framework is servlet-based, but it can read the Spring root application context that a
`ContextLoaderListener` has published. Any controller method may declare `ApplicationContext` as a
parameter and receive it:

```java
@UrlMapping(value = "/books", method = "GET")
public ModelAndView list(ApplicationContext ctx) {
    BookService service = ctx.getBean(BookService.class);
    ModelAndView mv = new ModelAndView("books");
    mv.setAttribute("books", service.findAll());
    return mv;
}
```

The context is fetched from the `ServletContext` under
`WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE`. If no listener registered one, the
parameter is simply `null`. Controllers themselves are created by the framework, not by Spring, so
they are not candidates for injection; Spring owns services and repositories.

---

## Configuration

`web.xml` needs the listener, the front controller, and the scan and view settings:

```xml
<context-param>
    <param-name>prefix</param-name>
    <param-value>/WEB-INF/views/</param-value>
</context-param>
<context-param>
    <param-name>suffix</param-name>
    <param-value>.jsp</param-value>
</context-param>

<listener>
    <listener-class>randy.framework.listener.InitListener</listener-class>
</listener>

<servlet>
    <servlet-name>front</servlet-name>
    <servlet-class>randy.framework.servlet.FrontControllerServlet</servlet-class>
    <init-param>
        <param-name>packageToScan</param-name>
        <param-value>com.example.controller</param-value>
    </init-param>
</servlet>
<servlet-mapping>
    <servlet-name>front</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

All three values are read through `ServletContext.getInitParameter`, so a context parameter works
just as well as a servlet init parameter. `packageToScan` may be omitted, in which case the whole
classpath is scanned.
