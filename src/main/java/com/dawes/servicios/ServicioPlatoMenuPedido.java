package com.dawes.servicios;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import com.dawes.modelo.Pedido;
import com.dawes.modelo.PlatoMenuPedido;

public interface ServicioPlatoMenuPedido {	
	List<PlatoMenuPedido> findByFechaEnvioSemanalGreaterThanEqual(LocalDate date);
	<S extends PlatoMenuPedido> S save(S entity);
	<S extends PlatoMenuPedido> List<S> saveAll(Iterable<S> entities);
	<S extends PlatoMenuPedido> Optional<S> findOne(Example<S> example);
	List<PlatoMenuPedido> findAll(Sort sort);
	void flush();
	Page<PlatoMenuPedido> findAll(Pageable pageable);
	<S extends PlatoMenuPedido> S saveAndFlush(S entity);
	<S extends PlatoMenuPedido> List<S> saveAllAndFlush(Iterable<S> entities);
	List<PlatoMenuPedido> findAll();
	List<PlatoMenuPedido> findAllById(Iterable<Integer> ids);
	void deleteInBatch(Iterable<PlatoMenuPedido> entities);
	<S extends PlatoMenuPedido> Page<S> findAll(Example<S> example, Pageable pageable);
	Optional<PlatoMenuPedido> findById(Integer id);
	void deleteAllInBatch(Iterable<PlatoMenuPedido> entities);
	boolean existsById(Integer id);
	<S extends PlatoMenuPedido> long count(Example<S> example);
	void deleteAllByIdInBatch(Iterable<Integer> ids);
	<S extends PlatoMenuPedido> boolean exists(Example<S> example);
	void deleteAllInBatch();
	PlatoMenuPedido getOne(Integer id);
	<S extends PlatoMenuPedido, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction);
	long count();
	void deleteById(Integer id);
	PlatoMenuPedido getById(Integer id);
	void delete(PlatoMenuPedido entity);
	void deleteAllById(Iterable<? extends Integer> ids);
	PlatoMenuPedido getReferenceById(Integer id);
	void deleteAll(Iterable<? extends PlatoMenuPedido> entities);
	<S extends PlatoMenuPedido> List<S> findAll(Example<S> example);
	<S extends PlatoMenuPedido> List<S> findAll(Example<S> example, Sort sort);
	void deleteAll();

}