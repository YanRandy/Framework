package com.example.repository;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.entity.Livre;

@Repository
public class LivreRepository {
    private final JdbcTemplate jdbc;

    public LivreRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public List<Livre> findAll() {
        return jdbc.query(
                "SELECT id, titre, auteur FROM livres",
                (rs, rowNum) -> {
                    Livre l = new Livre();
                    l.setId(rs.getInt("id"));
                    l.setTitre(rs.getString("titre"));
                    l.setAuteur(rs.getString("auteur"));
                    return l;
                });
    }
}
