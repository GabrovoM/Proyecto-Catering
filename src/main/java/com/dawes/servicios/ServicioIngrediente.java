package com.dawes.servicios;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import com.dawes.modelo.Ingrediente;

public interface ServicioIngrediente {
	<S extends Ingrediente> S save(S entity);
	<S extends Ingrediente> List<S> saveAll(Iterable<S> entities);
	<S extends Ingrediente> Optional<S> findOne(Example<S> example);
	List<Ingrediente> findAll(Sort sort);
	void flush();
	Page<Ingrediente> findAll(Pageable pageable);
	<S extends Ingrediente> S saveAndFlush(S entity);
	<S extends Ingrediente> List<S> saveAllAndFlush(Iterable<S> entities);
	List<Ingrediente> findAll();
	List<Ingrediente> findAllById(Iterable<Integer> ids);
	void deleteInBatch(Iterable<Ingrediente> entities);
	<S extends Ingrediente> Page<S> findAll(Example<S> example, Pageable pageable);
	Optional<Ingrediente> findById(Integer id);
	void deleteAllInBatch(Iterable<Ingrediente> entities);
	boolean existsById(Integer id);
	<S extends Ingrediente> long count(Example<S> example);
	void deleteAllByIdInBatch(Iterable<Integer> ids);
	<S extends Ingrediente> boolean exists(Example<S> example);
	void deleteAllInBatch();
	Ingrediente getOne(Integer id);
	<S extends Ingrediente, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction);
	long count();
	void deleteById(Integer id);
	Ingrediente getById(Integer id);
	void delete(Ingrediente entity);
	void deleteAllById(Iterable<? extends Integer> ids);
	Ingrediente getReferenceById(Integer id);
	void deleteAll(Iterable<? extends Ingrediente> entities);
	<S extends Ingrediente> List<S> findAll(Example<S> example);
	<S extends Ingrediente> List<S> findAll(Example<S> example, Sort sort);
	void deleteAll();
}