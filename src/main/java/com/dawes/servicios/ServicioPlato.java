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

import com.dawes.modelo.Categoria;
import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Plato;

public interface ServicioPlato {	
	List<Plato> findByCategoria(Categoria categoria);	
	Page<Plato> findByCategoriaId(int idCategoria, Pageable pageable);	
	int findNumPlatosByCategoria(Categoria categoria);	
	Page<Plato> findAllByNombreContainingIgnoreCase(String nombre, Pageable pageable);	
	List<Plato> findByOrden(Integer orden);	
	long countPedidosByPlatoIdAndEstado(@Param("platoId") Integer platoId, @Param("estado") EstadoPedidoEnum estado);
	long countMenusByPlatoId(@Param("platoId") int platoId);
	Optional<Plato> findById(int id);
	Optional<Plato> findByNombre(String nombre);
	List<Plato> findByNombreContainsIgnoreCase(String cadena);

	<S extends Plato> S save(S entity);
	<S extends Plato> List<S> saveAll(Iterable<S> entities);
	<S extends Plato> Optional<S> findOne(Example<S> example);
	List<Plato> findAll(Sort sort);
	void flush();
	Page<Plato> findAll(Pageable pageable);
	<S extends Plato> S saveAndFlush(S entity);
	<S extends Plato> List<S> saveAllAndFlush(Iterable<S> entities);
	List<Plato> findAll();
	List<Plato> findAllById(Iterable<Integer> ids);
	void deleteInBatch(Iterable<Plato> entities);
	<S extends Plato> Page<S> findAll(Example<S> example, Pageable pageable);
	Optional<Plato> findById(Integer id);
	void deleteAllInBatch(Iterable<Plato> entities);
	boolean existsById(Integer id);
	<S extends Plato> long count(Example<S> example);
	void deleteAllByIdInBatch(Iterable<Integer> ids);
	<S extends Plato> boolean exists(Example<S> example);
	void deleteAllInBatch();
	Plato getOne(Integer id);
	<S extends Plato, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction);
	long count();
	void deleteById(Integer id);
	Plato getById(Integer id);
	void delete(Plato entity);
	void deleteAllById(Iterable<? extends Integer> ids);
	Plato getReferenceById(Integer id);
	void deleteAll(Iterable<? extends Plato> entities);
	<S extends Plato> List<S> findAll(Example<S> example);
	<S extends Plato> List<S> findAll(Example<S> example, Sort sort);
	void deleteAll();	
	
}