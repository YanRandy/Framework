package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.RLivre;
import com.example.repository.RLivreRepository;

/**
 * Logique métier des livres.
 * Bean Spring (@Service) : un controller le récupère avec
 * {@code ctx.getBean(RLivreService.class)} via le paramètre ApplicationContext.
 */
@Service
public class RLivreService {
    private final RLivreRepository repo;

    public RLivreService(RLivreRepository repo) {
        this.repo = repo;
    }

    public List<RLivre> getTousLesLivres() {
        return repo.findAll();
    }
}
