<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<body>
    <h2>Test — un fichier</h2>
    <form action="${pageContext.request.contextPath}/upload" 
          method="POST" 
          enctype="multipart/form-data">
        <input type="file" name="photo"/>
        <button type="submit">Envoyer</button>
    </form>

    <hr/>

    <h2>Test — plusieurs fichiers</h2>
    <form action="${pageContext.request.contextPath}/upload/multi" 
          method="POST" 
          enctype="multipart/form-data">
        <input type="file" name="photo"/>
        <input type="file" name="cv"/>
        <button type="submit">Envoyer</button>
    </form>
</body>
</html>