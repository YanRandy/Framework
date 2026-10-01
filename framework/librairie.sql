-- ============================================
-- Base de données : bibliotheque
-- Projet : Code-Test / Randy Framework
-- Tables : livres
-- ============================================

CREATE DATABASE IF NOT EXISTS bibliotheque
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bibliotheque;

-- ============================================
-- Table : livres
-- Colonnes attendues par LivreRepository :
--   id, titre, auteur
-- ============================================

DROP TABLE IF EXISTS livres;

CREATE TABLE livres (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    titre  VARCHAR(255) NOT NULL,
    auteur VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Données de test
-- ============================================

INSERT INTO livres (titre, auteur) VALUES
    ('Clean Code',                        'Robert C. Martin'),
    ('The Pragmatic Programmer',          'Andrew Hunt'),
    ('Design Patterns',                   'Gang of Four'),
    ('Refactoring',                       'Martin Fowler'),
    ('Introduction to Algorithms',        'Thomas H. Cormen'),
    ('The Mythical Man-Month',            'Frederick P. Brooks'),
    ('Code Complete',                     'Steve McConnell'),
    ('Head First Design Patterns',        'Eric Freeman'),
    ('Domain-Driven Design',             'Eric Evans'),
    ('You Don''t Know JS',               'Kyle Simpson');

-- ============================================
-- Vérification
-- ============================================

CREATE TABLE Personne(
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    age INT NOT NULL
);

INSERT INTO Personne (nom, prenom, age) VALUES
    ('Dupont', 'Jean', 30),
    ('Durand', 'Marie', 25),
    ('Martin', 'Pierre', 40),
    ('Bernard', 'Sophie', 35),
    ('Petit', 'Lucie', 28);