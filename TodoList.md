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