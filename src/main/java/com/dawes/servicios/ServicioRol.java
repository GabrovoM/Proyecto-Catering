package com.dawes.servicios;

import java.util.List;
import java.util.Optional;

import com.dawes.modelo.Rol;
import com.dawes.modelo.UsuarioRol;

public interface ServicioRol {
	Rol findByRoleName(String rolename);
	List<Rol> findAll();
	Optional<Rol> findById(Integer id);
	List<UsuarioRol> buscarRolesDeUsuario(String username);
}
