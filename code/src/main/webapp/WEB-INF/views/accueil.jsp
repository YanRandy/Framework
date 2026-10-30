<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% pageContext.setAttribute("b", request.getContextPath()); %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Accueil</title>
    <style>
        :root {
            --bg: #f0f4f8;
            --card: #ffffff;
            --ink: #1a2332;
            --muted: #5a6a7a;
            --accent: #0d6e6e;
            --border: #d5dee8;
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem 1rem;
            font-family: "Segoe UI", system-ui, sans-serif;
            color: var(--ink);
            background:
                radial-gradient(ellipse at top left, #d9ecec 0%, transparent 50%),
                radial-gradient(ellipse at bottom right, #d6e0f0 0%, transparent 45%),
                var(--bg);
        }

        main { width: min(640px, 100%); }

        h1 {
            margin: 0 0 0.35rem;
            font-size: 2rem;
            font-weight: 650;
            letter-spacing: -0.02em;
        }

        .subtitle {
            margin: 0 0 1.75rem;
            color: var(--muted);
        }

        .tuiles {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
            gap: 0.9rem;
        }

        .tuile {
            display: block;
            padding: 1.1rem 1.2rem;
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: 12px;
            color: inherit;
            text-decoration: none;
            box-shadow: 0 6px 20px rgba(26, 35, 50, 0.05);
            transition: border-color 0.15s, transform 0.15s;
        }

        .tuile:hover { border-color: var(--accent); transform: translateY(-2px); }

        .tuile strong { display: block; margin-bottom: 0.2rem; }

        .tuile span { color: var(--muted); font-size: 0.9rem; }

        .principale { grid-column: 1 / -1; background: var(--accent); border-color: var(--accent); color: #fff; }

        .principale span { color: #d9ecec; }
    </style>
</head>
<body>
    <main>
        <h1>Bienvenue</h1>
        <p class="subtitle">Application de démonstration du framework.</p>

        <nav class="tuiles">
            <a class="tuile principale" href="${b}/sprint/auth/test">
                <strong>Authentification et rôles</strong>
                <span>Se connecter et tester les routes protégées</span>
            </a>
            <a class="tuile" href="${b}/formulaire">
                <strong>Nouvelle personne</strong>
                <span>Formulaire d'ajout</span>
            </a>
            <a class="tuile" href="${b}/personnes">
                <strong>Personnes</strong>
                <span>Liste des personnes enregistrées</span>
            </a>
            <a class="tuile" href="${b}/livres">
                <strong>Livres</strong>
                <span>Liste des livres</span>
            </a>
        </nav>
    </main>
</body>
</html>
