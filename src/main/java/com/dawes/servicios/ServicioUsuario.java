package com.dawes.servicios;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import com.dawes.modelo.Usuario;

public interface ServicioUsuario {	
	Usuario registrarUsuario(Usuario usuario);
	Optional<Usuario> findByUsernameAndPassword(String username, String password);
	Optional<Usuario> findByUsername(String username);
	Usuario findFirstByUsername(String username);
	List<Usuario> findByRoles_Rol_RoleNameAndRolesSize(String roleName, int size);
	Optional<Usuario> findByEmail(String email);

	<S extends Usuario> S save(S entity);
	<S extends Usuario> List<S> saveAll(Iterable<S> entities);
	<S extends Usuario> Optional<S> findOne(Example<S> example);
	List<Usuario> findAll(Sort sort);
	void flush();
	Page<Usuario> findAll(Pageable pageable);
	<S extends Usuario> S saveAndFlush(S entity);
	<S extends Usuario> List<S> saveAllAndFlush(Iterable<S> entities);
	List<Usuario> findAll();
	List<Usuario> findAllById(Iterable<Integer> ids);
	void deleteInBatch(Iterable<Usuario> entities);
	<S extends Usuario> Page<S> findAll(Example<S> example, Pageable pageable);
	Optional<Usuario> findById(Integer id);
	void deleteAllInBatch(Iterable<Usuario> entities);
	boolean existsById(Integer id);
	<S extends Usuario> long count(Example<S> example);
	void deleteAllByIdInBatch(Iterable<Integer> ids);
	<S extends Usuario> boolean exists(Example<S> example);
	void deleteAllInBatch();
	Usuario getOne(Integer id);
	<S extends Usuario, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction);
	long count();
	void deleteById(Integer id);
	Usuario getById(Integer id);
	void delete(Usuario entity);
	void deleteAllById(Iterable<? extends Integer> ids);
	Usuario getReferenceById(Integer id);
	void deleteAll(Iterable<? extends Usuario> entities);
	<S extends Usuario> List<S> findAll(Example<S> example);
	<S extends Usuario> List<S> findAll(Example<S> example, Sort sort);
	void deleteAll();
	
}