package com.dawes.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dawes.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
	Optional<Usuario> findByEmail(String email);
	Optional<Usuario> findByUsernameAndPassword(String username, String password);
	Optional<Usuario> findByUsername(String username);
	Optional<Usuario> findFirstByEmail(String email);
	Usuario findFirstByUsername(String username);
	List<Usuario> findByRoles_Rol_RoleName(String roleName);	
	List<Usuario> findByRoles_Rol_RoleNameNot(String roleName);	
	@Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.rol.roleName = 'ROLE_USER' AND SIZE(u.roles) = 1")
	List<Usuario> findByRoles_Rol_RoleNameAndRolesSize(String roleName, int size);		
	@Query("SELECT u FROM Usuario u JOIN u.roles ur JOIN ur.rol r WHERE r.roleName = :roleName")
	List<Usuario> findUsersByRole(@Param("roleName") String roleName);		
	Usuario findByResetPasswordToken(String token);	
}
