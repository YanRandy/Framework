### Liste a faire

- [x] 1-liste ou Objet[]
- [x] 2- liste<Objet[]>
- [x] 3- formulaire avec Objet Complexe multiple
        ex: Objet study(Personne, Batiment) {}
        Ca marche hono XD(Claude) avec le switch case d'invoke ca peut.
- [] 4- Map<key, value[]>
  - Easier one, adding a key to multiple value.
<!-- plusieurs valeurs pour la même clé -->
<input type="checkbox" name="couleurs" value="rouge"/>
<input type="checkbox" name="couleurs" value="bleu"/>
<input type="checkbox" name="tailles"  value="S"/>
<input type="checkbox" name="tailles"  value="M"/>
<input type="checkbox" name="tailles"  value="XL"/>
 - Type de sortie attendu :
Map<String, String[]> filtres = {
    "couleurs" → ["rouge", "bleu"],
    "tailles"  → ["S", "M", "XL"]
}
 - Como ?
 - req.getParamaterMap();
- [] 5- Map <Objet[],List<Objet[]>>
- [] 6- ?

# Primitifs
curl "http://localhost:8080/code-test/api/binding/primitifs?nom=Rakoto&age=22&actif=true"

# String[]
curl "http://localhost:8080/code-test/api/binding/array?tags=java&tags=spring&tags=mvc"

# List<String>
curl "http://localhost:8080/code-test/api/binding/list?tags=java&tags=spring"

# int[]
curl "http://localhost:8080/code-test/api/binding/notes?notes=18&notes=15&notes=20"

# Objet complexe simple
curl "http://localhost:8080/code-test/api/binding/personne?nom=Rakoto&prenom=Jean&age=22"

# Deux objets complexes — préfixe obligatoire
curl "http://localhost:8080/code-test/api/binding/deux?personne.nom=Rakoto&personne.age=22&livre.titre=CleanCode&livre.auteur=Martin"

# Map<String, String[]>
curl "http://localhost:8080/code-test/api/binding/map?couleurs=rouge&couleurs=bleu&tailles=S&tailles=M"