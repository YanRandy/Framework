<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Liste des personnes</title>
</head>
<body>
    <h1>Personnes enregistrées</h1>
    <p><a href="${pageContext.request.contextPath}/formulaire">Ajouter une personne</a></p>
    <table border="1" cellpadding="8">
        <tr>
            <th>ID</th>
            <th>Nom</th>
            <th>Prénom</th>
            <th>Âge</th>
        </tr>
        <c:forEach var="p" items="${personnes}">
            <tr>
                <td>${p.id}</td>
                <td>${p.nom}</td>
                <td>${p.prenom}</td>
                <td>${p.age}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>
