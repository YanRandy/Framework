package com.example.repository;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.entity.RPersonne;

/**
 * Accès base de données pour la table {@code Personne}.
 * Bean Spring (@Repository) : la DataSource est injectée depuis applicationContext.xml.
 */
@Repository
public class RPersonneRepository {
    private final JdbcTemplate jdbc;

    public RPersonneRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /** Retourne toutes les personnes de la table. */
    public List<RPersonne> findAll() {
        return jdbc.query(
                "SELECT id, nom, prenom, age FROM Personne",
                (rs, rowNum) -> {
                    RPersonne p = new RPersonne();
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setAge(rs.getInt("age"));
                    return p;
                });
    }

    /** Insère une personne à partir de valeurs séparées (binding primitif). */
    public void save(String nom, String prenom, int age) {
        jdbc.update(
                "INSERT INTO Personne (nom, prenom, age) VALUES (?, ?, ?)",
                nom, prenom, age);
    }

    /** Insère une personne à partir d'un objet déjà bindé (binding objet). */
    public void save(RPersonne personne) {
        jdbc.update(
                "INSERT INTO Personne (nom, prenom, age) VALUES (?, ?, ?)",
                personne.getNom(), personne.getPrenom(), personne.getAge());
    }
}
