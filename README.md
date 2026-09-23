# Framework

A lightweight Java MVC framework built from scratch, inspired by Spring MVC. Handles HTTP routing, annotation scanning, view rendering, and optional Spring container integration — without depending on Spring MVC itself.

---

## How it works

```
HTTP Request
    └── FrontControllerServlet
            └── looks up UrlKey(path, httpMethod) in urlList
            └── finds Mapping(className, methodName)
            └── instantiates the controller via reflection
            └── invokes the method (with or without ApplicationContext)
            └── renders the result (ModelAndView, String, or void)
```

The URL map is built once at startup by `InitListener`, which scans the classpath for `@Controller` classes and collects every `@UrlMapping` method into a `Map<UrlKey, Mapping>` stored in the `ServletContext`.

---

## Project structure

```
src/main/java/randy/framework/
├── annotation/
│   ├── Controller.java       — marks a class as a controller
│   └── UrlMapping.java       — maps a method to a URL + HTTP method(s)
├── listener/
│   └── InitListener.java     — scans packages at startup, builds urlList
├── model/
│   ├── Mapping.java          — holds className + methodName for a route
│   ├── ModelAndView.java     — holds view name + data model (Map<String, Object[]>)
│   └── UrlKey.java           — composite key (url + httpMethod) for the route map
├── servlet/
│   └── FrontControllerServlet.java  — single entry point for all requests
├── helper/
│   └── ViewRenderer.java     — interface for view rendering strategies
└── util/
    ├── HtmlViewRenderer.java — replaces {placeholder} tokens in .html files
    ├── JspViewRenderer.java  — forwards to .jsp files via RequestDispatcher
    └── Utilitaire.java       — classpath scanner using ClassGraph
```

---

## Annotations

### `@Controller`
Marks a class as a controller. The scanner picks it up during startup.

```java
@Controller
public class BookController { }
```

### `@UrlMapping`
Maps a method to a URL path and one or more HTTP methods. Defaults to both GET and POST if `method` is omitted.

```java
@UrlMapping(value = "/books", method = "GET")
public ModelAndView list(ApplicationContext ctx) { ... }

@UrlMapping(value = "/books", method = "POST")
public String save() { ... }

@UrlMapping("/")   // default → GET + POST
public void home() { ... }
```

---

## Controller method return types

| Return type | What happens |
|---|---|
| `ModelAndView` | Sets model attributes on the request, renders the named view |
| `String` | Treated as the view name, rendered with no model data |
| `void` | Method executes, framework shows a debug confirmation page |

---

## Spring container integration

If a controller method declares `ApplicationContext` as its only parameter, the framework fetches the Spring container from the `ServletContext` and passes it automatically:

```java
@UrlMapping(value = "/books", method = "GET")
public ModelAndView list(ApplicationContext ctx) {
    BookService service = ctx.getBean(BookService.class);
    // ...
}
```

The framework instantiates controllers itself. Spring manages services and repositories only.

---

## View rendering

Views are resolved as `prefix + viewName + suffix`, configured in `web.xml`.

**JSP** — forwards via `RequestDispatcher`. Use JSTL EL expressions:
```jsp
<c:forEach var="item" items="${myList}">
    <li>${item.title}</li>
</c:forEach>
```

**HTML** — replaces `{placeholder}` tokens with values from the model:
```html
<p>{message}</p>
```

The renderer is selected automatically based on the configured suffix (`.jsp`, `.html`, `.htm`).

---

## web.xml configuration

```xml
<!-- package your @Controller classes live in -->
<init-param>
    <param-name>packageToScan</param-name>
    <param-value>com.example.controller</param-value>
</init-param>

<!-- view resolver -->
<context-param>
    <param-name>prefix</param-name>
    <param-value>/WEB-INF/views/</param-value>
</context-param>
<context-param>
    <param-name>suffix</param-name>
    <param-value>.jsp</param-value>
</context-param>
```

---

## Database entry
mysql -u root -p < librairie.sql

---
## Build and install

```bash
# From the Framework directory
mvn clean install
# Publishes randy.framework:framework:1.0 to your local ~/.m2 repository
# Run this every time you change the framework before rebuilding Code-Test
```
