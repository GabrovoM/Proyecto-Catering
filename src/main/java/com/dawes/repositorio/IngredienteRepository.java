package com.dawes.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dawes.modelo.Ingrediente;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Integer> {

}
