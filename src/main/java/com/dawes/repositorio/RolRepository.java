package com.dawes.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dawes.modelo.Rol;
import com.dawes.modelo.UsuarioRol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
	List<Rol> findAll();
	Rol findByRoleName(String rolename);	
	@Query("select ur from UsuarioRol ur where ur.usuario.username = ?1")
	List<UsuarioRol> buscarRolesDeUsuario(String username);		
	List<Rol> findByRoleNameIn(List<String> roleNames);		
}
