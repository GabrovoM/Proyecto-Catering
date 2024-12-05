package com.dawes.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.dawes.modelo.UsuarioRol;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Integer> {
	
	@Modifying
    @Transactional
    @Query("DELETE FROM UsuarioRol ur WHERE ur.usuario.id = :userId")
    void deleteByUsuarioId(@Param("userId") Integer userId);

}
