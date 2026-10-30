<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<body>
    <h1>Liste des livres</h1>
    <table border="1" cellpadding="8">
        <tr>
            <th>ID</th>
            <th>Titre</th>
            <th>Auteur</th>
        </tr>
        <c:forEach var="livre" items="${livres}">
            <tr>
                <td>${livre.id}</td>
                <td>${livre.titre}</td>
                <td>${livre.auteur}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>