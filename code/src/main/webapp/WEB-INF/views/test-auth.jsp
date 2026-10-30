<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<% pageContext.setAttribute("b", request.getContextPath()); %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Authentification et rôles</title>
    <style>
        :root {
            --bg: #f0f4f8;
            --card: #ffffff;
            --ink: #1a2332;
            --muted: #5a6a7a;
            --accent: #0d6e6e;
            --accent-hover: #0a5757;
            --border: #d5dee8;
            --focus: #0d6e6e33;
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            min-height: 100vh;
            padding: 2rem 1rem;
            font-family: "Segoe UI", system-ui, sans-serif;
            color: var(--ink);
            background:
                radial-gradient(ellipse at top left, #d9ecec 0%, transparent 50%),
                radial-gradient(ellipse at bottom right, #d6e0f0 0%, transparent 45%),
                var(--bg);
        }

        main { width: min(920px, 100%); margin: 0 auto; }

        a { color: var(--accent); }

        .retour { font-size: 0.9rem; text-decoration: none; }

        h1 {
            margin: 0.75rem 0 0.35rem;
            font-size: 1.9rem;
            font-weight: 650;
            letter-spacing: -0.02em;
        }

        h2 { margin: 0 0 1.1rem; font-size: 1.1rem; }

        .subtitle { margin: 0 0 1.5rem; color: var(--muted); }

        .statut {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            justify-content: space-between;
            gap: 0.75rem;
            margin-bottom: 1.25rem;
            padding: 0.8rem 1.1rem;
            background: var(--card);
            border: 1px solid var(--border);
            border-left: 4px solid var(--muted);
            border-radius: 10px;
        }

        .statut.connecte { border-left-color: var(--accent); }

        .colonnes {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.25rem;
            align-items: start;
        }

        .card {
            padding: 1.5rem;
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: 12px;
            box-shadow: 0 12px 40px rgba(26, 35, 50, 0.08);
        }

        .field { margin-bottom: 1rem; }

        label {
            display: block;
            margin-bottom: 0.35rem;
            font-size: 0.875rem;
            font-weight: 600;
        }

        input {
            width: 100%;
            padding: 0.65rem 0.8rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            font: inherit;
            color: var(--ink);
            background: #fafbfc;
        }

        input:focus {
            outline: none;
            border-color: var(--accent);
            background: #fff;
            box-shadow: 0 0 0 3px var(--focus);
        }

        button {
            padding: 0.7rem 1rem;
            border: none;
            border-radius: 8px;
            background: var(--accent);
            color: #fff;
            font: inherit;
            font-weight: 600;
            cursor: pointer;
        }

        button:hover { background: var(--accent-hover); }

        .pleine { width: 100%; }

        .discret {
            padding: 0.4rem 0.8rem;
            background: transparent;
            border: 1px solid var(--border);
            color: var(--ink);
            font-size: 0.875rem;
        }

        .discret:hover { background: var(--bg); }

        .note { margin: 0.9rem 0 0; color: var(--muted); font-size: 0.85rem; }

        .route {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 0.75rem;
            padding: 0.6rem 0;
            border-top: 1px solid var(--border);
            font-size: 0.875rem;
            text-decoration: none;
        }

        .route:first-of-type { border-top: none; }

        .route:hover { text-decoration: underline; }

        .acces {
            flex-shrink: 0;
            padding: 0.15rem 0.55rem;
            border-radius: 999px;
            background: #e6f2f2;
            color: var(--accent-hover);
            font-size: 0.75rem;
            font-weight: 600;
        }

        .acces.public { background: var(--bg); color: var(--muted); }

        .acces.admin { background: var(--accent); color: #fff; }
    </style>
</head>
<body>
    <main>
        <a class="retour" href="${b}/">← Accueil</a>
        <h1>Authentification et rôles</h1>
        <p class="subtitle">Connecte-toi avec un rôle, puis ouvre les routes pour voir qui passe.</p>

        <c:choose>
            <c:when test="${empty sessionScope.user}">
                <div class="statut">Non connecté</div>
            </c:when>
            <c:otherwise>
                <div class="statut connecte">
                    <span>
                        Connecté : <strong><c:out value="${sessionScope.user}"/></strong>,
                        rôle <strong><c:out value="${empty sessionScope.role ? 'aucun' : sessionScope.role}"/></strong>
                    </span>
                    <form action="${b}/sprint/auth/logout" method="POST">
                        <button class="discret" type="submit">Se déconnecter</button>
                    </form>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="colonnes">
            <section class="card">
                <h2>Connexion</h2>
                <form action="${b}/sprint/auth/login" method="POST">
                    <div class="field">
                        <label for="username">Utilisateur</label>
                        <input id="username" name="username" placeholder="Rakoto" required>
                    </div>
                    <div class="field">
                        <label for="password">Mot de passe</label>
                        <input id="password" name="password" type="password" placeholder="1234" required>
                    </div>
                    <div class="field">
                        <label for="role">Rôle</label>
                        <input id="role" name="role" list="roles" placeholder="Vide = aucun rôle">
                        <datalist id="roles">
                            <option value="USER">
                            <option value="MANAGER">
                            <option value="ADMIN">
                        </datalist>
                    </div>
                    <button class="pleine" type="submit">Se connecter</button>
                </form>
                <p class="note">La réponse s'affiche en JSON : reviens ici et recharge pour voir le statut.</p>
            </section>

            <section class="card">
                <h2>Routes à tester</h2>
                <a class="route" href="${b}/sprint/auth/whoami">/sprint/auth/whoami <span class="acces public">Public</span></a>
                <a class="route" href="${b}/sprint/auth/public">/sprint/auth/public <span class="acces public">Public</span></a>
                <a class="route" href="${b}/sprint/auth/page">/sprint/auth/page <span class="acces">Connecté</span></a>
                <a class="route" href="${b}/sprint/auth/profil">/sprint/auth/profil <span class="acces">USER, MANAGER, ADMIN</span></a>
                <a class="route" href="${b}/sprint/auth/staff">/sprint/auth/staff <span class="acces">MANAGER, ADMIN</span></a>
                <a class="route" href="${b}/sprint/admin/rapport">/sprint/admin/rapport <span class="acces">MANAGER, ADMIN</span></a>
                <a class="route" href="${b}/sprint/auth/admin">/sprint/auth/admin <span class="acces admin">ADMIN</span></a>
                <a class="route" href="${b}/sprint/admin/dashboard">/sprint/admin/dashboard <span class="acces admin">ADMIN</span></a>
            </section>
        </div>
    </main>
</body>
</html>
