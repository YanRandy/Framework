<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<% pageContext.setAttribute("b", request.getContextPath()); %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Page protégée</title>
</head>
<body>
    <p><a href="${b}/sprint/auth/test">← Test auth</a></p>
    <h1>Page protégée (@RAuthenticated sans rôle)</h1>
    <p>User : ${sessionScope.user}</p>
    <p>Rôle : ${empty sessionScope.role ? "(aucun)" : sessionScope.role}</p>
</body>
</html>
