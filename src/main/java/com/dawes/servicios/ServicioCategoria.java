package com.dawes.servicios;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import com.dawes.modelo.Categoria;

public interface ServicioCategoria {
	Optional<Categoria> findById(int id);
	Optional<Categoria> findByNombre(String nombre);
	
	<S extends Categoria> S save(S entity);
	<S extends Categoria> List<S> saveAll(Iterable<S> entities);
	<S extends Categoria> Optional<S> findOne(Example<S> example);
	List<Categoria> findAll(Sort sort);
	void flush();
	Page<Categoria> findAll(Pageable pageable);
	<S extends Categoria> S saveAndFlush(S entity);
	<S extends Categoria> List<S> saveAllAndFlush(Iterable<S> entities);
	List<Categoria> findAll();
	List<Categoria> findAllById(Iterable<Integer> ids);
	void deleteInBatch(Iterable<Categoria> entities);
	<S extends Categoria> Page<S> findAll(Example<S> example, Pageable pageable);
	Optional<Categoria> findById(Integer id);
	void deleteAllInBatch(Iterable<Categoria> entities);
	boolean existsById(Integer id);
	<S extends Categoria> long count(Example<S> example);
	void deleteAllByIdInBatch(Iterable<Integer> ids);
	<S extends Categoria> boolean exists(Example<S> example);
	void deleteAllInBatch();
	Categoria getOne(Integer id);
	<S extends Categoria, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction);
	long count();
	void deleteById(Integer id);
	Categoria getById(Integer id);
	void delete(Categoria entity);
	void deleteAllById(Iterable<? extends Integer> ids);
	Categoria getReferenceById(Integer id);
	void deleteAll(Iterable<? extends Categoria> entities);
	<S extends Categoria> List<S> findAll(Example<S> example);
	<S extends Categoria> List<S> findAll(Example<S> example, Sort sort);
	void deleteAll();	
	
}