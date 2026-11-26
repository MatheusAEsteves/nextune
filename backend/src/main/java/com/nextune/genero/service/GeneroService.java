package com.nextune.genero.service;

import com.nextune.genero.dto.GeneroResponse;
import com.nextune.genero.repository.GeneroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GeneroService {

    private final GeneroRepository generoRepository;

    public GeneroService(GeneroRepository generoRepository) {
        this.generoRepository = generoRepository;
    }

    @Transactional(readOnly = true)
    public List<GeneroResponse> listar() {
        return generoRepository.findAll(Sort.by("nome")).stream()
                .map(g -> new GeneroResponse(g.getId(), g.getNome()))
                .toList();
    }
}
