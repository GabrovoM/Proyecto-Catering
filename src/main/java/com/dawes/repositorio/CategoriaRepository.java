package com.dawes.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dawes.modelo.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
	Optional<Categoria> findById(int id);
	Optional<Categoria> findByNombre(String nombre);
}
