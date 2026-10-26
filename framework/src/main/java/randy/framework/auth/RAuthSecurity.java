package randy.framework.auth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpMethod;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import randy.framework.annotation.RAuthenticated;
import randy.framework.model.RMapping;

public class RAuthSecurity {
    private final String userKey, roleKey, loginUrl;

    public RAuthSecurity(String userKey, String roleKey, String loginUrl) {
        this.userKey = userKey;
        this.roleKey = roleKey;
        this.loginUrl = loginUrl;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public boolean allow(RMapping map, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!map.isAuthRequired()) {
            return true;
        }
        // session existante ou non, verification de l'attribut userKey
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(userKey) == null) {
            deny(map, req, resp, 401);
            return false;
        }

        // verification des roles
        List<String> roles = map.getRoles();
        if (!roles.isEmpty() && !hasAnyRole(session.getAttribute(roleKey), roles)) {
            deny(map, req, resp, 403);
            return false;
        }
        return true;
    }

    public boolean hasAnyRole(Object have, List<String> reqRoles) {
        if (have == null) {
            return false;
        }
        Collection<?> roles = (have instanceof Collection<?> collection) ? collection
                : (have instanceof Object[] array) ? Arrays.asList(array)
                        : List.of(have);
        for (String role : reqRoles) {
            for (Object userRole : roles) {
                if (String.valueOf(userRole).equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deny(RMapping map, HttpServletRequest req, HttpServletResponse resp, int status) throws IOException {
        if (map.isRestApi()) {
            resp.setStatus(status);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"error\":\"" + (status == 401 ? "non authentifié" : "accès refusé") + "\"}");
        } else if (status == 401) {
            resp.sendRedirect(req.getContextPath() + loginUrl);
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    public static RAuthenticated resolveAuth(Class<?> clazz, Method method) {
        RAuthenticated onMethod = clazz.getAnnotation(RAuthenticated.class);
        if (onMethod != null) {
            return onMethod;
        }
        return null;
    }
}