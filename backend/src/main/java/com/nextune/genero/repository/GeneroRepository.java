package com.nextune.genero.repository;

import com.nextune.genero.entity.GeneroModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneroRepository extends JpaRepository<GeneroModel, Long> {
    boolean existsByNome(String nome);
    Optional<GeneroModel> findByNome(String nome);
}
