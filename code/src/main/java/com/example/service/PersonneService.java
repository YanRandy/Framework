package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.Personne;
import com.example.repository.PersonneRepository;

@Service
public class PersonneService {
    private final PersonneRepository repo;

    public PersonneService(PersonneRepository repo) {
        this.repo = repo;
    }

    public List<Personne> getToutesLesPersonnes() {
        return repo.findAll();
    }

    public void save(String nom, String prenom, int age) {
        repo.save(nom, prenom, age);
    }

    public void save(Personne personne) {
        repo.save(personne);
    }
}
