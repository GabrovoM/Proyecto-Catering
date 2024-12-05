package com.dawes.servicios;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Pedido;
import com.dawes.modelo.Usuario;

public interface ServicioPedido {	
	Optional<List<Pedido>> findByUsuario(Usuario usuario);	
	List<Pedido> buscarUltimaDireccion(@Param("usuarioId") Integer usuarioId);	
	Optional<List<Pedido>> findByFechaCreacionBetween(LocalDate fechaini, LocalDate fechafin);	
	List<Pedido> findByEstadoEnum(EstadoPedidoEnum estadoEnum);	
	List<Pedido> findDireccionMasReciente(Usuario usuario);		
	Optional<List<Pedido>> findByEstadoEnumAndFechaCreacionBetween(EstadoPedidoEnum estado, LocalDate fecha_ini, LocalDate fecha_fin);
    Page<Pedido> findByFechaCreacionBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);	 
	Page<Pedido> findByUsuario(Usuario usuario, Pageable pageable);	 
	Optional<List<Pedido>> findByEstadoAndFechaCreacionBetween(LocalDate fechaInicio, LocalDate fechaFin);	 
	Page<Pedido> findByEstadoEnumAndFechaCreacionBetween(EstadoPedidoEnum estado, LocalDate startDate, LocalDate endDate, Pageable pageable);
	List<Pedido> findPartialWithFiveDaysWarning(@Param("nextDay") LocalDate nextDay, @Param("fiveDaysAgo") LocalDate fiveDaysAgo);
	
	<S extends Pedido> S save(S entity);
	<S extends Pedido> List<S> saveAll(Iterable<S> entities);
	<S extends Pedido> Optional<S> findOne(Example<S> example);
	List<Pedido> findAll(Sort sort);
	void flush();
	Page<Pedido> findAll(Pageable pageable);
	<S extends Pedido> S saveAndFlush(S entity);
	<S extends Pedido> List<S> saveAllAndFlush(Iterable<S> entities);
	List<Pedido> findAll();
	List<Pedido> findAllById(Iterable<Integer> ids);
	void deleteInBatch(Iterable<Pedido> entities);
	<S extends Pedido> Page<S> findAll(Example<S> example, Pageable pageable);
	Optional<Pedido> findById(Integer id);
	void deleteAllInBatch(Iterable<Pedido> entities);
	boolean existsById(Integer id);
	<S extends Pedido> long count(Example<S> example);
	void deleteAllByIdInBatch(Iterable<Integer> ids);
	<S extends Pedido> boolean exists(Example<S> example);
	void deleteAllInBatch();
	Pedido getOne(Integer id);
	<S extends Pedido, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction);
	long count();
	void deleteById(Integer id);
	Pedido getById(Integer id);
	void delete(Pedido entity);
	void deleteAllById(Iterable<? extends Integer> ids);
	Pedido getReferenceById(Integer id);
	void deleteAll(Iterable<? extends Pedido> entities);
	<S extends Pedido> List<S> findAll(Example<S> example);
	<S extends Pedido> List<S> findAll(Example<S> example, Sort sort);
	void deleteAll();	

}