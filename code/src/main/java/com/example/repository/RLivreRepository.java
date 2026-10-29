package com.example.repository;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.entity.RLivre;

/**
 * Accès base de données pour la table {@code livres}.
 * Bean Spring (@Repository) : la DataSource est injectée depuis applicationContext.xml.
 */
@Repository
public class RLivreRepository {
    private final JdbcTemplate jdbc;

    public RLivreRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /** Retourne tous les livres de la table. */
    public List<RLivre> findAll() {
        return jdbc.query(
                "SELECT id, titre, auteur FROM livres",
                (rs, rowNum) -> {
                    RLivre l = new RLivre();
                    l.setId(rs.getInt("id"));
                    l.setTitre(rs.getString("titre"));
                    l.setAuteur(rs.getString("auteur"));
                    return l;
                });
    }
}
