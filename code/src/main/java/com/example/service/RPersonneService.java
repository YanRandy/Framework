package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.RPersonne;
import com.example.repository.RPersonneRepository;

/**
 * Logique métier des personnes.
 * Bean Spring (@Service) : un controller le récupère avec
 * {@code ctx.getBean(RPersonneService.class)} via le paramètre ApplicationContext.
 */
@Service
public class RPersonneService {
    private final RPersonneRepository repo;

    public RPersonneService(RPersonneRepository repo) {
        this.repo = repo;
    }

    public List<RPersonne> getToutesLesPersonnes() {
        return repo.findAll();
    }

    public void save(String nom, String prenom, int age) {
        repo.save(nom, prenom, age);
    }

    public void save(RPersonne personne) {
        repo.save(personne);
    }
}
