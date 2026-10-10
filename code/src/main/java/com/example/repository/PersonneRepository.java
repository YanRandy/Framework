package com.example.repository;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.entity.Personne;

@Repository
public class PersonneRepository {
    private final JdbcTemplate jdbc;

    public PersonneRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public List<Personne> findAll() {
        return jdbc.query(
                "SELECT id, nom, prenom, age FROM Personne",
                (rs, rowNum) -> {
                    Personne p = new Personne();
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setAge(rs.getInt("age"));
                    return p;
                });
    }

    public void save(String nom, String prenom, int age) {
        jdbc.update(
                "INSERT INTO Personne (nom, prenom, age) VALUES (?, ?, ?)",
                nom, prenom, age);
    }

    public void save(Personne personne) {
        jdbc.update(
                "INSERT INTO Personne (nom, prenom, age) VALUES (?, ?, ?)",
                personne.getNom(), personne.getPrenom(), personne.getAge());
    }
}
