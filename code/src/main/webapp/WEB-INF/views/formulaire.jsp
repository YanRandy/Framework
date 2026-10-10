<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Formulaire Personne</title>
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
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: "Segoe UI", system-ui, sans-serif;
            color: var(--ink);
            background:
                radial-gradient(ellipse at top left, #d9ecec 0%, transparent 50%),
                radial-gradient(ellipse at bottom right, #d6e0f0 0%, transparent 45%),
                var(--bg);
        }

        .card {
            width: min(420px, 92vw);
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 2rem 2rem 1.75rem;
            box-shadow: 0 12px 40px rgba(26, 35, 50, 0.08);
        }

        h1 {
            margin: 0 0 0.35rem;
            font-size: 1.5rem;
            font-weight: 650;
            letter-spacing: -0.02em;
        }

        .subtitle {
            margin: 0 0 1.5rem;
            color: var(--muted);
            font-size: 0.95rem;
        }

        .field {
            margin-bottom: 1.1rem;
        }

        label {
            display: block;
            margin-bottom: 0.4rem;
            font-size: 0.875rem;
            font-weight: 600;
        }

        input {
            width: 100%;
            padding: 0.7rem 0.85rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            font: inherit;
            color: var(--ink);
            background: #fafbfc;
            transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
        }

        input:focus {
            outline: none;
            border-color: var(--accent);
            background: #fff;
            box-shadow: 0 0 0 3px var(--focus);
        }

        button {
            width: 100%;
            margin-top: 0.5rem;
            padding: 0.8rem 1rem;
            border: none;
            border-radius: 8px;
            background: var(--accent);
            color: #fff;
            font: inherit;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.15s, transform 0.1s;
        }

        button:hover { background: var(--accent-hover); }
        button:active { transform: translateY(1px); }
    </style>
</head>
<body>
    <main class="card">
        <h1>Nouvelle personne</h1>
        <p class="subtitle">Renseignez les informations puis validez.</p>

        <form action="${pageContext.request.contextPath}/save" method="POST">
            <div class="field">
                <label for="nom">Nom</label>
                <input type="text" id="nom" name="nom" required placeholder="Dupont" autocomplete="family-name">
            </div>

            <div class="field">
                <label for="prenom">Prénom</label>
                <input type="text" id="prenom" name="prenom" required placeholder="Jean" autocomplete="given-name">
            </div>

            <div class="field">
                <label for="age">Âge</label>
                <input type="number" id="age" name="age" required min="0" max="150" placeholder="30">
            </div>

            <button type="submit">Enregistrer</button>
        </form>
    </main>
</body>
</html>
