package com.dawes.serviciosImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dawes.modelo.Plato;
import com.dawes.modelo.Rol;
import com.dawes.modelo.UsuarioRol;
import com.dawes.repositorio.RolRepository;
import com.dawes.servicios.ServicioRol;

@Service
public class ServicioRolImpl implements ServicioRol {
	@Autowired
	private RolRepository rr;
	
	@Override
	public Rol findByRoleName(String rolename) {		
		return rr.findByRoleName(rolename);
	}

	@Override
	public List<Rol> findAll() {		
		return rr.findAll();
	}

	@Override
	public Optional<Rol> findById(Integer id) {		
		return rr.findById(id);
	}

	@Override
	public List<UsuarioRol> buscarRolesDeUsuario(String username) {
		
		return rr.buscarRolesDeUsuario(username);
	}			

	public List<Rol> findAllExcludingRoles(List<String> excludedRoles) {
	    return rr.findAll().stream()
	            .filter(rol -> !excludedRoles.contains(rol.getRoleName()))
	            .collect(Collectors.toList());
	}
	
	public List<Rol> findRolesForClientes() {	   
	    List<String> excludedRoles = List.of("ADMIN", "GESTOR", "EDITOR");
	    return rr.findAll().stream()
	            .filter(rol -> !excludedRoles.contains(rol.getRoleName()))
	            .collect(Collectors.toList());
	}
	
	public List<Rol> findByRoleNames(List<String> roleNames) {
	    return rr.findByRoleNameIn(roleNames); 
	}

}
