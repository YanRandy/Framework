package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.Livre;
import com.example.repository.LivreRepository;

@Service
public class LivreService {
    private final LivreRepository repo;

    public LivreService(LivreRepository repo) {
        this.repo = repo;
    }

    public List<Livre> getTousLesLivres() {
        return repo.findAll();
    }
}