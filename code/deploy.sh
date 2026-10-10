#!/bin/bash

# ============================================================
#  Script de déploiement : Code-Test → Tomcat
#  Usage : bash deploy.sh [chemin/vers/Code-Test]
# ============================================================

set -e  # arrêt immédiat si une commande échoue

# ── Configuration ──────────────────────────────────────────
PROJECT_DIR="${1:-$(pwd)}"          # dossier du projet (arg ou répertoire courant)
TOMCAT_WEBAPPS="/home/randylam/Documents/tomcat/Tomcat/webapps"
APP_NAME="code-test"                   # nom du contexte dans Tomcat → http://localhost:8080/Code-Test
WAR_FILE="$PROJECT_DIR/target/${APP_NAME}-1.0.war"
DEST="$TOMCAT_WEBAPPS/${APP_NAME}.war"

# ── Couleurs ───────────────────────────────────────────────
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✔ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠ $1${NC}"; }
err()  { echo -e "${RED}✘ $1${NC}"; exit 1; }

echo ""
echo "========================================"
echo "  Déploiement Code-Test → Tomcat"
echo "========================================"
echo ""

# ── 1. Vérifications préliminaires ────────────────────────
[ -f "$PROJECT_DIR/pom.xml" ] || err "pom.xml introuvable dans : $PROJECT_DIR"
[ -d "$TOMCAT_WEBAPPS" ]      || err "Dossier Tomcat introuvable : $TOMCAT_WEBAPPS"
command -v mvn &>/dev/null    || err "Maven (mvn) n'est pas installé ou pas dans le PATH"

ok "Projet trouvé      : $PROJECT_DIR"
ok "Tomcat webapps     : $TOMCAT_WEBAPPS"

# ── 2. Build Maven ─────────────────────────────────────────
echo ""
echo "[ 1/3 ] Build Maven (mvn clean package)..."
cd "$PROJECT_DIR"
mvn clean package -DskipTests -q && ok "Build réussi → $WAR_FILE" || err "Échec du build Maven"

# ── 3. Suppression de l'ancienne version ───────────────────
echo ""
echo "[ 2/3 ] Nettoyage de l'ancienne version dans Tomcat..."

if [ -f "$DEST" ]; then
    rm -f "$DEST"
    warn "Ancien WAR supprimé : $DEST"
fi

if [ -d "$TOMCAT_WEBAPPS/${APP_NAME}" ]; then
    rm -rf "$TOMCAT_WEBAPPS/${APP_NAME}"
    warn "Ancien dossier déployé supprimé : $TOMCAT_WEBAPPS/${APP_NAME}"
fi

# ── 4. Copie du nouveau WAR ────────────────────────────────
echo ""
echo "[ 3/3 ] Déploiement du nouveau WAR..."
cp "$WAR_FILE" "$DEST" && ok "WAR copié → $DEST"

# ── Résumé ─────────────────────────────────────────────────
echo ""
echo "========================================"
ok "Déploiement terminé !"
echo ""
echo "  → Démarrer Tomcat si ce n'est pas fait :"
echo "    /home/randylam/Documents/tomcat/Tomcat/bin/startup.sh"
echo ""
echo "  → Accéder à l'application :"
echo "    http://localhost:8080/${APP_NAME}/"
echo ""
echo "  → Voir les logs en temps réel :"
echo "    tail -f /home/randylam/Documents/tomcat/Tomcat/logs/catalina.out"
echo "========================================"
