<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Confirmation</title>
</head>
<body>
    <h1>Personne enregistrée</h1>
    <p>Nom : ${param.nom}</p>
    <p>Prénom : ${param.prenom}</p>
    <p>Âge : ${param.age}</p>
    <p>
        <a href="${pageContext.request.contextPath}/personnes">Voir toutes les personnes</a>
        ·
        <a href="${pageContext.request.contextPath}/formulaire">Ajouter une autre</a>
    </p>
</body>
</html>
